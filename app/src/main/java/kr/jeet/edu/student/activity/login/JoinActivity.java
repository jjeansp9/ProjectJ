package kr.jeet.edu.student.activity.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.request.SignupRequest;
import kr.jeet.edu.student.model.request.SignupSNSRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.AuthPhoneNumberView;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends BaseActivity {

    private String TAG = JoinActivity.class.getSimpleName();

    private ViewGroup mViewGroupIdPwd;
    private EditText mEditName, mEditPhoneNo, mEditAuthNo, mEditId, mEditPassword1, mEditPassword2;
    private EditText[] mEditList;
    private TextView mCheckPwTxt, mCheckPhoneTxt;
    private RadioGroup mUserTypeRadioGroup, mGnderRadioGroup;
    private RadioButton mGenderRadioMale, mGenderRadioFemale;
    AuthPhoneNumberView _authPhoneNoView;

    private int mLoginType = Constants.LOGIN_TYPE_NORMAL;
    private String mUserName = null;
    private String mUserGender = null;
    private String mUserSnsId = null;

    private RetrofitApi mRetrofitApi;

    private final int MIN_ID_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        setStatusAndNavigatinBar(true);
        mContext = this;

        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_LOGIN_TYPE)){
                mLoginType = intent.getIntExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);

                if(mLoginType == Constants.LOGIN_TYPE_SNS_NAVER || mLoginType == Constants.LOGIN_TYPE_SNS_KAKAO || mLoginType == Constants.LOGIN_TYPE_SNS_GOOGLE || mLoginType == Constants.LOGIN_TYPE_SNS_APPLE) {
                    if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_NAME)) mUserName = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_NAME);
                    else mUserName = "";
                    if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_GENDER)) mUserGender = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_GENDER);
                    else mUserGender = "";
                    //mUserSnsId = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_SNSID);

                }
            }else{
                LogMgr.e("no intent extra");
            }
        }
        mUserSnsId = PreferenceUtil.getSNSUserId(mContext);
        initAppbar();
        initView();
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.join_member);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        findViewById(R.id.btn_next).setOnClickListener(this);
        //findViewById(R.id.btn_check_phone).setOnClickListener(this);
        _authPhoneNoView = findViewById(R.id.cv_auth_phoneno);
        _authPhoneNoView.setShowProgressDelegate(new AuthPhoneNumberView.ShowProgressDialogDelegate() {
            @Override
            public void onRequestShow() {
                showProgressDialog();
            }

            @Override
            public void onRequestHide() {
                hideProgressDialog();
            }
        });

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout authPhoneLayout= (LinearLayout) vi.inflate(R.layout.view_auth_phone_number, null);

        mViewGroupIdPwd = findViewById(R.id.layout_idpwd);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditId = (EditText) findViewById(R.id.edit_id);
        mEditPassword1 = (EditText) findViewById(R.id.edit_pw);
        mEditPassword2 = (EditText) findViewById(R.id.edit_pw_confirm);
        mCheckPwTxt = (TextView) findViewById(R.id.check_txt_pw);
        mCheckPhoneTxt = (TextView) findViewById(R.id.check_txt_phone);
        mUserTypeRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mGnderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);

        mEditPhoneNo = authPhoneLayout.findViewById(R.id.edit_phonenum);
        mEditAuthNo = authPhoneLayout.findViewById(R.id.edit_phone_authnum);

        mGenderRadioMale = (RadioButton) findViewById(R.id.radio_male);
        mGenderRadioFemale = (RadioButton) findViewById(R.id.radio_female);

        mEditList = new EditText[]{mEditName, mEditId, mEditPassword1, mEditPassword2, mEditPhoneNo, mEditAuthNo};

        if(mLoginType == Constants.LOGIN_TYPE_NORMAL) {
            mViewGroupIdPwd.setVisibility(View.VISIBLE);
        } else {
            mViewGroupIdPwd.setVisibility(View.GONE);

//            if(mUserName != null) {
//                mEditName.setText(mUserName);
//            }

            if(mUserGender != null) {
                if (mUserGender.equals("M")) {
                    mGenderRadioMale.setChecked(true);
                    mGenderRadioFemale.setChecked(false);
                    mGenderRadioMale.setEnabled(false);
                    mGenderRadioFemale.setEnabled(false);

                } else if (mUserGender.equals("F")){
                    mGenderRadioMale.setChecked(false);
                    mGenderRadioFemale.setChecked(true);
                    mGenderRadioMale.setEnabled(false);
                    mGenderRadioFemale.setEnabled(false);
                }else{
                    mGenderRadioMale.setChecked(true);
                    mGenderRadioFemale.setChecked(false);
                    mGenderRadioMale.setEnabled(true);
                    mGenderRadioFemale.setEnabled(true);
                }
            }
        }

        setTextWatcher(mEditPassword1);
        setTextWatcher(mEditPassword2);
    }

    private void setTextWatcher(EditText et){
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Utils.removeSpace(et);
                if(checkPassword(editable.toString())) {
                    mCheckPwTxt.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_next:
                if(checkJoin()) {
                    if(mLoginType == Constants.LOGIN_TYPE_NORMAL) {
                        userJoin();
                    }
                    else {
                        //request sns Join

                        LogMgr.e(TAG, "userJoinFromSns");
                        userJoinFromSns();
                    }

                }
                break;
        }
    }

    private boolean checkJoin() {

        String name = mEditName.getText().toString();

        if(TextUtils.isEmpty(name)) {
            Toast.makeText(mContext, R.string.empty_name, Toast.LENGTH_SHORT).show();
            showKeyboard(mEditName);
            return false;
        }

        if(!Utils.nameCheck(name)) {
            Toast.makeText(mContext, R.string.check_name_pattern, Toast.LENGTH_SHORT).show();
            showKeyboard(mEditName);
            return false;
        }

        // 일반 회원가입인 경우 패스워드 체크
        if(mLoginType == Constants.LOGIN_TYPE_NORMAL) {
            if (mEditId.getText().toString().length() < MIN_ID_LENGTH){
                showKeyboard(mEditId);
                Toast.makeText(mContext, R.string.check_id_min_length, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(mEditPassword1.getText().toString())){
                showKeyboard(mEditPassword1);
                Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(mEditPassword2.getText().toString())){
                showKeyboard(mEditPassword2);
                Toast.makeText(mContext, R.string.password_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
            if(!mEditPassword1.getText().toString().equals(mEditPassword2.getText().toString())) {
                Toast.makeText(mContext, R.string.password_does_not_match, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (mCheckPwTxt.getVisibility() == View.VISIBLE){
                Toast.makeText(mContext, R.string.password_pattern_not_match, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if(_authPhoneNoView != null) { // 휴대폰번호 인증
            return _authPhoneNoView.checkValid();
        }

        return true;
    }

    private boolean checkPassword(String pwd) {
        // https://pingfanzhilu.tistory.com/entry/Java-%EC%9E%90%EB%B0%94-%EB%B9%84%EB%B0%80%EB%B2%88%ED%98%B8-%EC%A0%95%EA%B7%9C%EC%8B%9D-%ED%8C%A8%ED%84%B4Pattern-%EB%A9%94%EC%86%8C%EB%93%9C

        // 정규식 (숫자, 영문 포함 최소 8글자)
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        Matcher matcher = pattern.matcher(pwd);

        if(!matcher.find()) {
            // "비밀번호는 영문과 특수문자 숫자를 포함하며 8자 이상이어야 합니다.";
            mCheckPwTxt.setText(getString(R.string.check_password));
            mCheckPwTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // 반복된 문자 확인
        Pattern pattern1 = Pattern.compile("(\\w)\\1\\1\\1");
        Matcher matcher1 = pattern1.matcher(pwd);

        if(matcher1.find()){
            // "비밀번호에 동일한 문자를 과도하게 연속해서 사용할 수 없습니다.";
            mCheckPwTxt.setText(getString(R.string.check_password2));
            mCheckPwTxt.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void userJoin() {
        // 회원가입 요청
        SignupRequest request = new SignupRequest();
        request.name = mEditName.getText().toString();
        //request.phoneNumber = mEditPhone1.getText().toString();
        request.phoneNumber = _authPhoneNoView.getAuthPhoneNo();

        if(mUserTypeRadioGroup.getCheckedRadioButtonId() == R.id.radioBtn1) {
            request.userGubun = Constants.USER_TYPE_PARENTS;
        } else {
            request.userGubun = Constants.USER_TYPE_STUDENT;
        }

        if(mGnderRadioGroup.getCheckedRadioButtonId() == R.id.radio_male) {
            request.gender = "M";
        } else {
            request.gender = "F";
        }

        request.id = mEditId.getText().toString();
        request.pw = mEditPassword1.getText().toString();

        requestJoin(request);
    }

    private void requestJoin(SignupRequest request) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signUp(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() : " + response.toString());
                    // todo. 사용자구분 타입에 따른 아이디 중복여부 체크 처리 필요
//                    JEET 회원정보와 일치하지 않습니다.
//                    학원으로 문의해 주세요.
//                    (대표번호 : 031-276-8003)
                    if(response.isSuccessful()) {
                        new AlertDialog.Builder(mContext)
                                .setMessage(getString(R.string.singup_success))
                                .setPositiveButton(getString(R.string.ok), (dialog, which) -> { {
                                        dialog.dismiss();
                                        Intent intent = new Intent(mContext, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .show();

                    } else {
                        try {
                            String msg = "";
                            try {
                                String errorMsg = response.errorBody().string().trim();
                                LogMgr.e(TAG, "requestJoin() errBody : " + errorMsg);
                                JSONObject jObj = new JSONObject(errorMsg);
                                msg = jObj.getString("msg");
                            } catch (JSONException e) {
                                LogMgr.e(Log.getStackTraceString(e));
                            }
                            if (response.code() == 400){

                                String title = getString(R.string.dialog_title_alarm);
                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);

                                if (msg.equals(Constants.PARAMETER_BINDING_ERROR)){
                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                                }else if (msg.equals(Constants.USER_GUBUN_MISMATCH)){
                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);

                                }else if (msg.equals(Constants.USER_NOT_JEET_MEMBER)){
                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
                                }

                            }else if(response.code() == 409) {
                                // {"msg":"DUPLICATE_ID"}
                                String title = getString(R.string.dialog_title_alarm);
                                if (msg.equals(Constants.DUPLICATE_ID)){
                                    showMessageDialog(title, getString(R.string.duplicate_id), clickOK -> hideMessageDialog(), null, false);
                                }else if (msg.equals(Constants.DUPLICATE_PHONE_NUMBER)){
                                    showMessageDialog(title, getString(R.string.duplicate_phone_no), clickOK -> hideMessageDialog(), null, false);
                                }else if (msg.equals(Constants.DUPLICATE_USER)){
                                    showMessageDialog(title, getString(R.string.duplicate_user), clickOK -> hideMessageDialog(), null, false);
                                }else{
                                    showMessageDialog(title, getString(R.string.msg_user_unknown), clickOK -> hideMessageDialog(), null, false);
                                }

                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private void userJoinFromSns() {
        // 회원가입 요청
        SignupSNSRequest request = new SignupSNSRequest();
        request.name = mEditName.getText().toString();
        //request.phoneNumber = mEditPhone1.getText().toString();
        request.phoneNumber = _authPhoneNoView.getAuthPhoneNo();
        LogMgr.e("phoneno = " + request.phoneNumber);

        if(mUserTypeRadioGroup.getCheckedRadioButtonId() == R.id.radioBtn1) {
            request.userGubun = Constants.USER_TYPE_PARENTS;
        } else {
            request.userGubun = Constants.USER_TYPE_STUDENT;
        }

        if(mGnderRadioGroup.getCheckedRadioButtonId() == R.id.radio_male) {
            request.gender = "M";
        } else {
            request.gender = "F";
        }

        if(mLoginType == Constants.LOGIN_TYPE_SNS_NAVER) request.snsType = "N";
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_KAKAO) request.snsType = "K";
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_GOOGLE) request.snsType = "G";
        else if(mLoginType == Constants.LOGIN_TYPE_SNS_APPLE) request.snsType = "A";
        else {
            request.snsType = "";
        }

        request.snsId = mUserSnsId;

        requestJoinFromSns(request);
    }

    private void requestJoinFromSns(SignupSNSRequest request) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signUpFromSNS(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() : " + response.toString());
                    // todo. 사용자구분 타입에 따른 아이디 중복여부 체크 처리 필요
                    if(response.isSuccessful()) {
                        new AlertDialog.Builder(mContext)
                                .setMessage(getString(R.string.singup_success))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(mContext, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .show();

                    } else {
                        String msg = "";
                        try {
                            String errorMsg = response.errorBody().string().trim();
                            LogMgr.e(TAG, "requestJoin() errBody : " + errorMsg);
                            JSONObject jObj = new JSONObject(errorMsg);
                            msg = jObj.getString("msg");
                        } catch (JSONException | IOException e) {
                            LogMgr.e(Log.getStackTraceString(e));
                        }
                        if (response.code() == 400){

                            String title = getString(R.string.dialog_title_alarm);
                            String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
                            String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);


                            if (msg.equals(Constants.PARAMETER_BINDING_ERROR)){
                                Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();

                            }else if (msg.equals(Constants.USER_GUBUN_MISMATCH)){
                                showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);

                            }else if (msg.equals(Constants.USER_NOT_JEET_MEMBER)){
                                showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
                            }else{
                                showMessageDialog(title, getString(R.string.msg_user_unknown), clickOK -> hideMessageDialog(), null, false);
                            }

                        }else if(response.code() == 409) {
                            // {"msg":"DUPLICATE_ID"}
                            String title = getString(R.string.dialog_title_alarm);
                            if (msg.equals(Constants.DUPLICATE_ID)){
                                showMessageDialog(title, getString(R.string.duplicate_id), clickOK -> hideMessageDialog(), null, false);
                            }else if (msg.equals(Constants.DUPLICATE_PHONE_NUMBER)){
                                showMessageDialog(title, getString(R.string.duplicate_phone_no), clickOK -> hideMessageDialog(), null, false);
                            }else if (msg.equals(Constants.DUPLICATE_USER)){
                                showMessageDialog(title, getString(R.string.duplicate_user), clickOK -> hideMessageDialog(), null, false);
                            }else{
                                showMessageDialog(title, getString(R.string.msg_user_unknown), clickOK -> hideMessageDialog(), null, false);
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    hideProgressDialog();
                    LogMgr.e(TAG, "requestJoin() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(_authPhoneNoView != null) {
            _authPhoneNoView.release();
        }
    }

}