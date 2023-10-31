package kr.jeet.edu.student.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.Confirm2LeaveDialog;
import kr.jeet.edu.student.dialog.PopupDialog;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.sns.AppleLoginManager;
import kr.jeet.edu.student.sns.GoogleLoginManager;
import kr.jeet.edu.student.sns.KaKaoLoginManager;
import kr.jeet.edu.student.sns.NaverLoginManager;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetAccountActivity extends BaseActivity {

    private String TAG = SetAccountActivity.class.getSimpleName();

    private RetrofitApi mRetrofitApi;

    private ConstraintLayout layoutPw, layoutPhone;
    private TextView mTvUserGubun, mTvName, mTvUpdatePhoneNum, mTvPhoneNum, mTvUpdatePw, mTvUpdateSns, mTvUpdateProfile;
    private ImageView mImgSns;
    private View line_1, line_2, line_3;
    private EditText mEditPw;

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private String _stName = "";
    private int _stCode = 0;
    private int _userGubun = 0;
    private int _loginType = 0;
    private String userGubun = "";
    private String phoneNum = "";
    private String memberName = "";
    private int stCodeParent = 0;

    private final String MEMBER = "Y";
    Confirm2LeaveDialog _leaveDialog;

    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;
    private AppCompatActivity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_account);
        mActivity = this;
        mContext = this;
        mGoogleLogin = new GoogleLoginManager(mActivity);
        initData();
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 부모인 경우 자신의 이름을 가져오기 위해 자신의 seq, stCode는 0으로
        if (_userGubun == Constants.USER_TYPE_PARENTS) requestMemberInfo(_memberSeq, stCodeParent);
        else requestMemberInfo(_stuSeq, _stCode);
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _loginType = PreferenceUtil.getLoginType(mContext);

        switch (_userGubun){
            case Constants.USER_TYPE_STUDENT:
                userGubun = getString(R.string.student);
                break;

            case Constants.USER_TYPE_PARENTS:
                userGubun = getString(R.string.parents);
                break;
        }
    }

    /**
     * 타입별 보여지는 View
     *
     * 회원 일반 : 비밀번호 수정
     * 비회원 일반 : 프로필정보 수정, 전화번호 수정, 비밀번호 수정
     *
     * 회원 SNS : SNS 로그인
     * 비회원 SNS : 프로필정보 수정, 전화번호 수정, SNS 로그인
     *
     * */
    @Override
    void initView() {
        mTvName = (TextView) findViewById(R.id.tv_account_name);
        mTvUserGubun = (TextView) findViewById(R.id.tv_account_user_gubun);
        mTvUpdateProfile = (TextView) findViewById(R.id.tv_update_profile);
        mTvUpdatePhoneNum = (TextView) findViewById(R.id.tv_update_phone_num);
        mTvPhoneNum = (TextView) findViewById(R.id.tv_account_phone_num);
        mTvUpdatePw = (TextView) findViewById(R.id.tv_update_pw);
        mTvUpdateSns = (TextView) findViewById(R.id.tv_update_sns_login);

        layoutPw = findViewById(R.id.layout_update_pw);
        layoutPhone = findViewById(R.id.layout_update_phone_num);

        mImgSns = (ImageView) findViewById(R.id.img_update_sns_login);

        switch(PreferenceUtil.getLoginType(mContext)){
            case Constants.LOGIN_TYPE_NORMAL:
                mImgSns.setVisibility(View.GONE);
                break;
            case Constants.LOGIN_TYPE_SNS_NAVER:
                mImgSns.setImageResource(R.drawable.btn_sns_naver);
                break;
            case Constants.LOGIN_TYPE_SNS_KAKAO:
                mImgSns.setImageResource(R.drawable.btn_sns_kakao);
                break;
            case Constants.LOGIN_TYPE_SNS_GOOGLE:
                mImgSns.setImageResource(R.drawable.btn_sns_google);
                break;
            case Constants.LOGIN_TYPE_SNS_APPLE:
                mImgSns.setImageResource(R.drawable.btn_sns_apple);
                break;
        }

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout customDialogLayout= (RelativeLayout) vi.inflate(R.layout.custom_dialog, null, false);

        mEditPw = customDialogLayout.findViewById(R.id.edit);

        line_1 = findViewById(R.id.line_account_first);
        line_2 = findViewById(R.id.line_account_second);
        line_3 = findViewById(R.id.line_account_third);

        findViewById(R.id.layout_update_profile).setOnClickListener(this);
        findViewById(R.id.layout_update_phone_num).setOnClickListener(this);
        findViewById(R.id.layout_update_pw).setOnClickListener(this);
        findViewById(R.id.layout_update_sns_login).setOnClickListener(this);
        findViewById(R.id.layout_account_un_link).setOnClickListener(this);
        findViewById(R.id.layout_account_logout).setOnClickListener(this);

        int loginType = PreferenceUtil.getLoginType(mContext);
        String isOriginalMember = PreferenceUtil.getUserIsOriginal(mContext);

        if (loginType == Constants.LOGIN_TYPE_NORMAL){ // 일반 로그인인 경우
            if (isOriginalMember.equals(MEMBER)){ // 회원
                mTvUpdateProfile.setVisibility(View.GONE);
                mTvUpdatePhoneNum.setVisibility(View.VISIBLE);
                mTvPhoneNum.setVisibility(View.VISIBLE);
                mTvUpdateSns.setVisibility(View.GONE);
                line_1.setVisibility(View.GONE);
                line_2.setVisibility(View.VISIBLE);

                layoutPhone.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_set_click_event_top));
                layoutPw.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_set_click_event_bottom));

            }else{ // 비회원
                mTvUpdateSns.setVisibility(View.GONE);

                TypedValue outValue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                layoutPhone.setBackgroundResource(outValue.resourceId);
                layoutPw.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_set_click_event_bottom));
            }
            line_3.setVisibility(View.GONE);
            layoutPw.setForeground(null);

        }else{ // sns 로그인인 경우
            if (isOriginalMember.equals(MEMBER)){ // 회원
                mTvUpdateProfile.setVisibility(View.GONE);
                mTvUpdatePhoneNum.setVisibility(View.VISIBLE);
                mTvPhoneNum.setVisibility(View.VISIBLE);
                mTvUpdatePw.setVisibility(View.GONE);
                line_1.setVisibility(View.GONE);
                line_2.setVisibility(View.VISIBLE);
                line_3.setVisibility(View.GONE);

                layoutPhone.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_set_click_event_top));

            }else{
                mTvUpdatePw.setVisibility(View.GONE);
                line_2.setVisibility(View.GONE);

                TypedValue outValue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                layoutPhone.setBackgroundResource(outValue.resourceId);
            }
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_account_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private PopupDialog popupDialog = null;
    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.layout_update_profile:
                Intent intent = new Intent(mContext, SetProfileInfoActivity.class);
                intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, memberName);
                startActivity(intent);
                break;

            case R.id.layout_update_phone_num:
                startActivity(new Intent(mContext, SetPhoneNumActivity.class));
                break;

            case R.id.layout_update_pw:
                showDialog();
                break;

            case R.id.layout_update_sns_login:
                break;

            case R.id.layout_account_un_link:
                showConfirm2LeaveDialog();
                break;

            case R.id.layout_account_logout:
                showMessageDialog(getString(R.string.settings_logout), getString(R.string.msg_confirm_logout), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hideMessageDialog();
                                requestLogOut(_memberSeq);
                            }
                        },
                        new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                hideMessageDialog();
                            }
                        }, false);

                break;
        }
    }

    private void showConfirm2LeaveDialog() {
        if(_leaveDialog != null && _leaveDialog.isShowing()) {
            _leaveDialog.dismiss();
        }
        _leaveDialog = new Confirm2LeaveDialog(mContext);
        _leaveDialog.setOnOkButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideConfirm2LeaveDialog();
                switch(_loginType){
                    case Constants.LOGIN_TYPE_NORMAL:
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_NAVER:
                        mNaverLogin = new NaverLoginManager(mContext);
                        mNaverLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_KAKAO:
                        mKaKaoLogin = new KaKaoLoginManager(mContext);
                        mKaKaoLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_GOOGLE:
                        mGoogleLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                    case Constants.LOGIN_TYPE_SNS_APPLE:
                        mAppleLogin = new AppleLoginManager(mActivity);
                        mAppleLogin.DeleteAccountProcess();
                        requestLeave(_memberSeq);
                        break;
                }
            }
        });
        _leaveDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideConfirm2LeaveDialog();
            }
        });
        _leaveDialog.show();
    }
    private void hideConfirm2LeaveDialog() {
        if(_leaveDialog != null) _leaveDialog.dismiss();
    }
    private void showDialog(){
        if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss();

        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(getString(R.string.dialog_title_pw_confirm));
        popupDialog.setContent(getString(R.string.msg_pw_confirm));
        popupDialog.setEdit(true);
        popupDialog.setOnOkButtonClickListener(ok -> { if(checkLogin()) { confirmPw(); } });
        popupDialog.setOnCancelButtonClickListener(cancel -> { if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss(); });

        popupDialog.show();
    }

    private boolean checkLogin() {
        if(popupDialog.getInputText().isEmpty()) {
            Toast.makeText(mContext, getString(R.string.password) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmPw(){
        String pw = PreferenceUtil.getUserPw(mContext);
        String confirmPw = popupDialog.getInputText();
        LogMgr.e("pwConfirmTest", pw + "," + confirmPw);
        if (pw.equals(confirmPw)){
            if(popupDialog != null && popupDialog.isShowing()) popupDialog.dismiss();
            startActivity(new Intent(mContext, SetPasswordActivity.class));

        }else{
            if(popupDialog != null && popupDialog.isShowing()) popupDialog.setNotMatchTv(true);
        }
    }

    // 원생 정보 조회
    private void requestMemberInfo(int stuSeq, int stCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.studentInfo(stuSeq, stCode).enqueue(new Callback<StudentInfoResponse>() {
                @Override
                public void onResponse(Call<StudentInfoResponse> call, Response<StudentInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            StudentInfo getData = new StudentInfo();
                            if (response.body() != null)  getData= response.body().data;

                            if (getData != null) {

                                if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 원생이름 사용

                                    if (_stName != null) mTvName.setText(_stName); // 자녀선택화면의 이름

                                }else {
                                    mTvName.setText(getData.name); // 오리지널 이름
                                    memberName = getData.name;
                                }

                                mTvUserGubun.setText(userGubun);
                                mTvUserGubun.setVisibility(View.VISIBLE);

                                if (getData.phoneNumber != null) {
                                    mTvPhoneNum.setText(Utils.formatPhoneNumber(getData.phoneNumber.replace("-", "")));
                                    phoneNum = getData.phoneNumber;

                                } else mTvPhoneNum.setText(getText(R.string.empty_phonenumber));
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void requestLogOut(int managerSeq){
        showProgressDialog();
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.logout(managerSeq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            //Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                            PreferenceUtil.setUserSeq(mContext, -1);
                            PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                            PreferenceUtil.setUserId(mContext, "");
                            PreferenceUtil.setUserPw(mContext, "");
                            PreferenceUtil.setSNSUserId(mContext, "");
                            PreferenceUtil.setStuPhoneNum(mContext, "");
                            PreferenceUtil.setParentPhoneNum(mContext, "");
                            PreferenceUtil.setStuGender(mContext, "");
                            PreferenceUtil.setStuBirth(mContext, "");
                            PreferenceUtil.setStName(mContext, "");
                            PreferenceUtil.setUserSTCode(mContext, -1);
                            PreferenceUtil.setParentName(mContext, "");
                            PreferenceUtil.setNumberOfChild(mContext, 0);
                            PreferenceUtil.setAutoLogin(mContext, false);
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestLogOut() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestLogOut() Exception : ", e.getMessage()); }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLogOut() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLogOut() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }
    // 회원탈퇴
    private void requestLeave(int userSeq){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.leave(userSeq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            requestLogOut(_memberSeq);
                            Toast.makeText(mContext, R.string.success_leave, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}