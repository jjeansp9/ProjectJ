package kr.jeet.edu.student.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.request.UpdateProfileRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetProfileInfoActivity extends BaseActivity {

    private String TAG = SetProfileInfoActivity.class.getSimpleName();

    private EditText mEtProfileName;
    private EditText[] mEditList;
    private RadioButton mRdProfileMale, mRdProfileFemale;
    private RadioGroup mRgProfileGender;

    private RetrofitApi mRetrofitApi;

    private UpdateProfileRequest updateProfile;

    private int _seq = 0;
    private String _stName = "";
    private String _gender = "";
    private int _userGubun = 0;
    private String memberName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_info);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){

        try{
            Intent intent = getIntent();
            if (intent != null){
                if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_NAME)){
                    memberName = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_NAME);
                }
            }
        }catch(Exception e){
            LogMgr.e(TAG + "initData() Exception : ", e.getMessage());
        }

        _stName = PreferenceUtil.getStName(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);

        if (_userGubun == Constants.USER_TYPE_PARENTS) {
            _seq = PreferenceUtil.getUserSeq(mContext);
            _gender = PreferenceUtil.getUserGender(mContext);
        }
        else {
            _seq = PreferenceUtil.getStuSeq(mContext);
            _gender = PreferenceUtil.getStuGender(mContext);
        }

        updateProfile = new UpdateProfileRequest();
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_account_profile_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        findViewById(R.id.btn_update_complete).setOnClickListener(this);

        mRgProfileGender = findViewById(R.id.rg_profile_gender);
        mRdProfileMale = findViewById(R.id.radio_profile_male);
        mRdProfileFemale = findViewById(R.id.radio_profile_female);

        mEtProfileName = findViewById(R.id.et_profile_name);
        mEditList = new EditText[]{mEtProfileName};

        mEtProfileName.setText(memberName);
        if(_gender != null) {
            if (_gender.equals("M")) {
                mRdProfileMale.setChecked(true);
                mRdProfileFemale.setChecked(false);
            } else {
                mRdProfileMale.setChecked(false);
                mRdProfileFemale.setChecked(true);
            }
        }
    }

    private void clearFocus(View[] focusList){ for (View v : focusList) v.clearFocus(); }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_update_complete:
                if(checkName()) {
                    updateProfile();
                }
                break;
        }
    }

    private boolean checkName() {
        if(TextUtils.isEmpty(mEtProfileName.getText().toString())) {
            Toast.makeText(mContext, R.string.empty_name, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateProfile(){

        updateProfile.seq = _seq;
        updateProfile.name = mEtProfileName.getText().toString().trim();

        if(mRgProfileGender.getCheckedRadioButtonId() == R.id.radio_profile_male) {
            updateProfile.gender = "M";
        } else {
            updateProfile.gender = "F";
        }

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.updateProfile(updateProfile).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    try {
                        if (response.isSuccessful()){

                            Toast.makeText(mContext, R.string.settings_update_profile, Toast.LENGTH_SHORT).show();
                            finish();

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "updateProfile() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "updateProfile() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}