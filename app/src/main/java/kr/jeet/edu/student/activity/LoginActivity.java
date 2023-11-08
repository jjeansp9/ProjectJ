package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.model.request.SigninRequest;
import kr.jeet.edu.student.model.response.LoginResponse;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.sns.AppleLoginManager;
import kr.jeet.edu.student.sns.GoogleLoginManager;
import kr.jeet.edu.student.sns.KaKaoLoginManager;
import kr.jeet.edu.student.sns.NaverLoginManager;
import kr.jeet.edu.student.utils.HttpUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    private EditText mEditId, mEditPw;
    private CheckBox mAutoLoginCb;
    private RetrofitApi mRetrofitApi;
    private EditText[] mEditList;

    private int tabMode = Constants.USER_TYPE_PARENTS;
    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;
    private int selectedSNSLoginType = -1;
    private int stCodeParent = 0;

    private AppCompatActivity mActivity = null;
    private String snsName = "";
    private String snsType = "";

    private boolean btnPush = false;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.HANDLER_SNS_LOGIN_COMPLETE:
                    LogMgr.e(TAG, "SNS_LOGIN_COMPLETE");

                    String snsId = (String) msg.obj;

                    Bundle data = msg.getData();
                    if (data != null){
                        snsName = data.getString("name");
                        snsType = data.getString("loginType");
                    }

                    if(snsId != null && !snsId.isEmpty()) {
                        requestLoginFromSns(snsId);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = this;
        mContext = this;
        setStatusAndNavigatinBar(true);
        mGoogleLogin = new GoogleLoginManager(mActivity);
        initAppbar();
        initView();
        //String HashKey = Utility.INSTANCE.getKeyHash(mContext);
        //LogMgr.e(TAG, HashKey);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().isSelectedChild = false;
    }

    @Override
    void initAppbar(){}

    @Override
    void initView() {
        findViewById(R.id.login_root).setOnClickListener(this);
        findViewById(R.id.tv_find).setOnClickListener(this);
        findViewById(R.id.tv_join).setOnClickListener(this);
        findViewById(R.id.btn_naver).setOnClickListener(this);
        findViewById(R.id.btn_kakao).setOnClickListener(this);
        findViewById(R.id.btn_google).setOnClickListener(this);
        findViewById(R.id.btn_apple).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.checkbox_text).setOnClickListener(this);

        mAutoLoginCb = (CheckBox) findViewById(R.id.checkbox_login);
        mAutoLoginCb.setChecked(PreferenceUtil.getAutoLogin(mContext));

        mEditId = findViewById(R.id.edit_id);
        mEditPw = findViewById(R.id.edit_pw);
        mEditList = new EditText[]{mEditId, mEditPw};

        mEditId.setText(PreferenceUtil.getUserId(mContext));
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        Intent intent = null;

        String snsUserId = "";
        int loginType = Constants.LOGIN_TYPE_NORMAL;

        switch (view.getId()) {
            case R.id.login_root:
                mEditId.clearFocus();
                mEditPw.clearFocus();
                Utils.hideKeyboard(mContext, mEditList);
                break;

            case R.id.btn_naver:
                if(mAutoLoginCb.isChecked()) PreferenceUtil.setAutoLogin(mContext, true);

                if (!btnPush) {
                    btnPush = true;
                    selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_NAVER;
                    mNaverLogin = new NaverLoginManager(mContext);
                    mNaverLogin.setHandler(mHandler);
                    mNaverLogin.LoginProcess();
                }
                break;

            case R.id.btn_kakao:

                if(mAutoLoginCb.isChecked()) PreferenceUtil.setAutoLogin(mContext, true);

                if (!btnPush) {
                    btnPush = true;
                    selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_KAKAO;
                    mKaKaoLogin = new KaKaoLoginManager(mContext);
                    mKaKaoLogin.setHandler(mHandler);
                    mKaKaoLogin.LoginProcess();
                }
                break;

            case R.id.btn_google:
                if (mAutoLoginCb.isChecked()) PreferenceUtil.setAutoLogin(mContext, true);

                if (!btnPush) {
                    btnPush = true;
                    selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_GOOGLE;
                    mGoogleLogin.setHandler(mHandler);
                    mGoogleLogin.LoginProcess();
                }
                break;

            case R.id.btn_apple:
                if (mAutoLoginCb.isChecked()) PreferenceUtil.setAutoLogin(mContext, true);

                if (!btnPush){
                    btnPush = true;
                    selectedSNSLoginType = Constants.LOGIN_TYPE_SNS_APPLE;
                    mAppleLogin = new AppleLoginManager(mActivity);
                    mAppleLogin.setHandler(mHandler);
                    mAppleLogin.LoginProcess();
                }

                break;

            case R.id.tv_join :
                intent = new Intent(this, AgreeTermsActivity.class);
                intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
                startActivity(intent);
                break;

            case R.id.tv_find :
                intent = new Intent(this, FindCredentialsActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_login :
                if (!btnPush) {
                    btnPush = true;
                    if(checkLogin()) requestLogin();
                }
                Utils.hideKeyboard(mContext, mEditList);
                // 자동로그인 체크 저장
                if(mAutoLoginCb.isChecked()) PreferenceUtil.setAutoLogin(mContext, true);
                else PreferenceUtil.setAutoLogin(mContext, false);
                break;

            case R.id.checkbox_text:    //체크박스 범위 확대
                mAutoLoginCb.setChecked(!mAutoLoginCb.isChecked());
                break;
        }
    }


    private boolean checkLogin() {
        if(mEditId.getText().toString().trim().isEmpty()) {
            Toast.makeText(mContext, getString(R.string.id) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mEditPw.getText().toString().trim().isEmpty()) {
            Toast.makeText(mContext, getString(R.string.password) + " " + getString(R.string.empty_info), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void clearLoginInfo() {
        if(mEditId != null) mEditId.setText("");
        if(mEditPw != null) mEditPw.setText("");
    }

    //TODO : 동일한 번호로 관리자, 부모앱 2개 다 가입했을 때 어떤 Error Msg가 오는지 확인 필요
    private void requestLogin() {
        showProgressDialog();
        SigninRequest request = new SigninRequest();
        request.id = mEditId.getText().toString().trim();
        request.pw = mEditPw.getText().toString().trim();

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signIn(request.id, request.pw).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response.isSuccessful()) {
                        try {
                            LoginResponse res = response.body();

                            if (res.data != null){
                                PreferenceUtil.setUserSeq(mContext, res.data.seq);
                                PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
                                PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                PreferenceUtil.setUserIsOriginal(mContext, res.data.isOriginalMember);
                                PreferenceUtil.setSNSUserId(mContext, "");
                                PreferenceUtil.setNumberOfChild(mContext, 0);

                                if (res.data.userGubun == Constants.USER_TYPE_PARENTS) HttpUtils.requestMemberInfo(res.data.seq, stCodeParent, mContext);

                                if(res.data.pushStatus != null){
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, res.data.pushStatus.pushNotice.equals("Y"));
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, res.data.pushStatus.pushInformationSession.equals("Y"));
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, res.data.pushStatus.pushAttendance.equals("Y"));
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, res.data.pushStatus.pushSystem.equals("Y"));
                                }else{
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, true);
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, true);
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, true);
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, true);
                                }
                                if (res.data.userGubun <= Constants.USER_TYPE_TEACHER){
                                    //preference 저장값 초기화
                                    //Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                    PreferenceUtil.setPrefPushToken(mContext, "");
                                    PreferenceUtil.setUserSeq(mContext, 0);
                                    PreferenceUtil.setUserId(mContext, "");
                                    PreferenceUtil.setUserPw(mContext, "");
                                    PreferenceUtil.setAutoLogin(mContext, false);

                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.teacher_impossible_login), v -> {
                                                clearLoginInfo();
                                                hideMessageDialog();
                                            },
                                            null, false);

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){

                                        PreferenceUtil.setUserId(mContext, request.id);
                                        PreferenceUtil.setUserPw(mContext, request.pw);
                                        Utils.refreshPushToken(mContext, res.data.seq);

                                        if (res.data.isOriginalMember.equals(Constants.MEMBER)){
                                            LogMgr.e(TAG, "일반 회원 로그인");
                                            if (res.data.userGubun == Constants.USER_TYPE_PARENTS){

                                                requestChildStudentInfo(res.data.seq);

                                            }else if (res.data.userGubun == Constants.USER_TYPE_STUDENT){
                                                startMain(res.data.seq, res.data.stCode);
                                            }
                                            PreferenceUtil.setUserType(mContext, Constants.MEMBER);

                                        }else{
                                            LogMgr.e(TAG, "일반 비회원 로그인");
                                            startMain(res.data.seq, res.data.stCode);
                                            PreferenceUtil.setUserType(mContext, Constants.NON_MEMBER);
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){

                        }

                    } else {

                        try {
                            LogMgr.e(TAG, "requestLogin() errBody : " + response.errorBody().string());

                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR) {

                                String title = getString(R.string.dialog_title_alarm);
                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);

                                if (response.body().msg.equals(Constants.ALREADY_LOGIN_IN)){
                                    Toast.makeText(mContext, R.string.msg_already_login_in, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.PASSWORD_MISMATCH)){
                                    Toast.makeText(mContext, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)) {
                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                                } else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)) {
                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);

                                } else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)) {
                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
                                }
                            }else if(response.code() == 404 || response.code() == 401) {
                                // {"msg":"NOT_FOUND_MEMBER"}
                                Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                        }
                    }

                    hideProgressDialog();
                    btnPush = false;
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    btnPush = false;
                }
            });
        }
    }

    private void requestLoginFromSns(String SnsId) {
        showProgressDialog();

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signInSNS(SnsId).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response.isSuccessful()) {
                        try {

                            LoginResponse res = response.body();

                            if (res.data != null){

                                PreferenceUtil.setUserSeq(mContext, res.data.seq);
                                PreferenceUtil.setUserGubun(mContext, res.data.userGubun);
                                PreferenceUtil.setUserIsOriginal(mContext, res.data.isOriginalMember);
                                PreferenceUtil.setUserId(mContext, "");
                                PreferenceUtil.setUserPw(mContext, "");
                                PreferenceUtil.setNumberOfChild(mContext, 0);

                                if (res.data.userGubun == Constants.USER_TYPE_PARENTS) HttpUtils.requestMemberInfo(res.data.seq, stCodeParent, mContext);

                                if(res.data.pushStatus != null){
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, res.data.pushStatus.pushNotice.equals("Y"));
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, res.data.pushStatus.pushInformationSession.equals("Y"));
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, res.data.pushStatus.pushAttendance.equals("Y"));
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, res.data.pushStatus.pushSystem.equals("Y"));
                                }else{
                                    //공지사항
                                    PreferenceUtil.setNotificationAnnouncement(mContext, true);
                                    //설명회
                                    PreferenceUtil.setNotificationSeminar(mContext, true);
                                    //출석
                                    PreferenceUtil.setNotificationAttendance(mContext, true);
                                    //시스템알림
                                    PreferenceUtil.setNotificationSystem(mContext, true);
                                }
                                if (res.data.userGubun <= Constants.USER_TYPE_TEACHER){
                                    //preference 저장값 초기화
                                    //Toast.makeText(mContext, R.string.teacher_impossible_login, Toast.LENGTH_SHORT).show();
                                    //Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                    PreferenceUtil.setUserSeq(mContext, 0);
                                    PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                    PreferenceUtil.setSNSUserId(mContext, "");
                                    PreferenceUtil.setAutoLogin(mContext, false);
                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.teacher_impossible_login), v -> {
                                                clearLoginInfo();
                                                hideMessageDialog();
                                            },
                                            null, false);

                                    mAppleLogin.DeleteAccountProcess();

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){

                                        Utils.refreshPushToken(mContext, res.data.seq);

                                        if (res.data.isOriginalMember.equals(Constants.MEMBER)){
                                            LogMgr.e(TAG, "sns 회원 로그인");
                                            if (res.data.userGubun == Constants.USER_TYPE_PARENTS){
                                                requestChildStudentInfo(res.data.seq);
                                            }else if (res.data.userGubun == Constants.USER_TYPE_STUDENT){
                                                startSNSMain(res.data.seq, res.data.stCode);

                                            }
                                            PreferenceUtil.setUserType(mContext, Constants.MEMBER);

                                        }else{
                                            LogMgr.e(TAG, "sns 비회원 로그인");
                                            startSNSMain(res.data.seq, res.data.stCode);
                                            PreferenceUtil.setUserType(mContext, Constants.NON_MEMBER);
                                        }
                                    }
                                }
                            }

                        }catch (Exception e){
                        }

                    } else {

                        try {
                            LogMgr.e(TAG, "requestLogin() errBody : " + response.errorBody().string());

                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){

                                String title = getString(R.string.dialog_title_alarm);
                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);

                                if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)){
                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                                }else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)){
                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);

                                }else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)){
                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
                                }
                                if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();

                            }else if(response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                // {"msg":"NOT_FOUND_MEMBER"}

                                //로그인 정보가 없을 때..
                                if(selectedSNSLoginType != -1) {
                                    Intent intent = null;
                                    intent = new Intent(mContext, AgreeTermsActivity.class);
                                    intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, selectedSNSLoginType);
                                    intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, snsName);
                                    startActivity(intent);
                                }else{
                                    if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
                                }

                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                                if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
                            }

                        } catch (IOException e) {
                        }
                        if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
                    }
                    hideProgressDialog();
                    btnPush = false;
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    btnPush = false;
                }
            });
        }
    }

    private void requestChildStudentInfo(int parentMemberSeq) {

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.searchChildStudents(parentMemberSeq).enqueue(new Callback<SearchChildStudentsResponse>() {
                @Override
                public void onResponse(Call<SearchChildStudentsResponse> call, Response<SearchChildStudentsResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            ArrayList<ChildStudentInfo> getData = new ArrayList<>();

                            if (response.body() != null) {
                                getData= response.body().data;

                                if (getData != null){
                                    PreferenceUtil.setNumberOfChild(mContext, getData.size());

                                    if (getData.size() <= 1){ // 자녀가 1명이하인 경우
                                        int lastIndex = getData.size() - 1;
                                        goMain(getData.get(lastIndex));

                                    }else{
                                        Intent intent = new Intent(mContext, SelectStudentActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putParcelableArrayListExtra(IntentParams.PARAM_CHILD_STUDENT_INFO, getData);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                            }

                        }else{

                        }
                    }catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<SearchChildStudentsResponse> call, Throwable t) {

                    try { LogMgr.e(TAG, "requestStudentInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }

    private void startSNSMain(int seq, int stCode){

        PreferenceUtil.setStuSeq(mContext, seq);
        PreferenceUtil.setUserSTCode(mContext, stCode);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void startMain(int seq, int stCode){
        PreferenceUtil.setStuSeq(mContext, seq);
        PreferenceUtil.setUserSTCode(mContext, stCode);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goMain(ChildStudentInfo data) {
        if (data != null){
            DataManager.getInstance().isSelectedChild = true;
            PreferenceUtil.setStuSeq(mContext, data.seq);
            PreferenceUtil.setStName(mContext, data.stName);
            PreferenceUtil.setUserSTCode(mContext, data.stCode);
            PreferenceUtil.setStuGender(mContext, data.gender);

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(mContext, R.string.empty_child_stu_info, Toast.LENGTH_SHORT).show();
        }
    }
}