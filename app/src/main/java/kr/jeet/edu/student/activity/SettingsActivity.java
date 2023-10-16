package kr.jeet.edu.student.activity;

import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.request.UpdatePushStatusRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends BaseActivity {

    private String TAG = SettingsActivity.class.getSimpleName();

    private TextView mTvUserGubun, mTvName, mTvPhoneNum, mTvAppVersion, mTvAppVersionBadge, mTvPrivacy, mTvService;
    private SwitchMaterial mSwAnnouncement, mSwInformationSession, mSwAttendance, mSwSystem;
    private AppCompatButton btnSetAccount;
    private RetrofitApi mRetrofitApi;

    private UpdatePushStatusRequest updatePushStatus;

    private String pushAnnouncement = "";
    private String pushInformationSession = "";
    private String pushAttendance = "";
    private String pushSystem = "";

    private final String CHECKED_OK = "Y";
    private final String CHECKED_CANCEL = "N";

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private String _stName = "";
    private int _stCode = 0;
    private int _userGubun = 0;
    private String userGubun = "";
    private int stCodeParent = 0;

    private int pushSeq = 0;

    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_userGubun == Constants.USER_TYPE_PARENTS) requestMemberInfo(_memberSeq, stCodeParent);
        else requestMemberInfo(_stuSeq, _stCode);
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);

        updatePushStatus = new UpdatePushStatusRequest();

        switch (_userGubun){
            case Constants.USER_TYPE_STUDENT:
                userGubun = getString(R.string.student);
                break;

            case Constants.USER_TYPE_PARENTS:
                userGubun = getString(R.string.parents);
                break;
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mTvUserGubun = (TextView) findViewById(R.id.tv_set_user_gubun);
        mTvName = (TextView) findViewById(R.id.tv_set_name);
        mTvPhoneNum = (TextView) findViewById(R.id.tv_set_phone_num);
        mTvAppVersionBadge = (TextView) findViewById(R.id.tv_app_version_update);
        mTvAppVersion = (TextView) findViewById(R.id.tv_app_version);
        mTvPrivacy = findViewById(R.id.tv_set_privacy);
        mTvService = findViewById(R.id.tv_set_service);

        mSwAnnouncement = (SwitchMaterial) findViewById(R.id.sw_set_announcement_state);
        mSwInformationSession = (SwitchMaterial) findViewById(R.id.sw_set_information_session_state);
        mSwAttendance = (SwitchMaterial) findViewById(R.id.sw_set_attendance_state);
        mSwSystem = (SwitchMaterial) findViewById(R.id.sw_set_system_state);

        mSwAnnouncement.setOnClickListener(this);
        mSwInformationSession.setOnClickListener(this);
        mSwAttendance.setOnClickListener(this);
        mSwSystem.setOnClickListener(this);

        findViewById(R.id.layout_set_operation_policy).setOnClickListener(this);
        findViewById(R.id.layout_set_PI_use_consent).setOnClickListener(this);
        findViewById(R.id.layout_set_app_info).setOnClickListener(this);

        btnSetAccount = findViewById(R.id.btn_set_account);
        btnSetAccount.setOnClickListener(this);

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            if (versionName != null) mTvAppVersion.setText("v"+versionName);
            else mTvAppVersionBadge.setVisibility(View.GONE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_set_account:
                startActivity();
                break;

            case R.id.sw_set_announcement_state:
                if (mSwAnnouncement.isChecked()) pushAnnouncement = CHECKED_OK;
                else pushAnnouncement = CHECKED_CANCEL;
                requestUpdatePush(true);
                break;

            case R.id.sw_set_information_session_state:
                if (mSwInformationSession.isChecked()) pushInformationSession = CHECKED_OK;
                else pushInformationSession = CHECKED_CANCEL;
                requestUpdatePush(false);
                break;

            case R.id.sw_set_attendance_state:
                if (mSwAttendance.isChecked()) pushAttendance = CHECKED_OK;
                else pushAttendance = CHECKED_CANCEL;
                requestUpdatePush(false);
                break;

            case R.id.sw_set_system_state:
                if (mSwSystem.isChecked()) pushSystem = CHECKED_OK;
                else pushSystem = CHECKED_CANCEL;
                requestUpdatePush(false);
                break;

            case R.id.layout_set_operation_policy:
                url = Constants.POLICY_SERVICE;
                startPvyActivity(mTvService.getText().toString());
                break;

            case R.id.layout_set_PI_use_consent:
                url = Constants.POLICY_PRIVACY;
                startPvyActivity(mTvPrivacy.getText().toString());
                break;

            case R.id.layout_set_app_info:
                break;
        }
    }

    private void requestMemberInfo(int stuSeq, int stCode){
        showProgressDialog();
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

                                if (stCode == stCodeParent) PreferenceUtil.setUserGender(mContext, getData.gender);

                                if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){

                                    if (_stName != null) mTvName.setText(_stName);

                                }else mTvName.setText(getData.name);

                                mTvUserGubun.setText(userGubun);
                                mTvUserGubun.setVisibility(View.VISIBLE);

                                if (getData.phoneNumber != null) {
                                    String phoneNumber = "";

                                    if (getData.phoneNumber.length() == 11) phoneNumber = Utils.formatPhoneNumber(getData.phoneNumber);
                                    else phoneNumber = getData.phoneNumber;

                                    mTvPhoneNum.setText(phoneNumber);
                                }
                                else mTvPhoneNum.setText(getText(R.string.empty_phonenumber));

                                pushSeq = getData.pushStatus.seq;

                                mSwAnnouncement.setChecked(getData.pushStatus.pushNotice.equals(CHECKED_OK));
                                mSwInformationSession.setChecked(getData.pushStatus.pushInformationSession.equals(CHECKED_OK));
                                mSwAttendance.setChecked(getData.pushStatus.pushAttendance.equals(CHECKED_OK));
                                mSwSystem.setChecked(getData.pushStatus.pushSystem.equals(CHECKED_OK));

                                pushAnnouncement = getData.pushStatus.pushNotice;
                                pushInformationSession = getData.pushStatus.pushInformationSession;
                                pushAttendance = getData.pushStatus.pushAttendance;
                                pushSystem = getData.pushStatus.pushSystem;
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }

    private void requestUpdatePush(boolean isUpdateSubscribeTopic){
        updatePushStatus.seq = pushSeq;
        updatePushStatus.pushNotice = pushAnnouncement;
        updatePushStatus.pushInformationSession = pushInformationSession;
        updatePushStatus.pushAttendance = pushAttendance;
        updatePushStatus.pushSystem = pushSystem;

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.updatePushStatus(updatePushStatus).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful()){
                        Log.i("updateTest", response.body().msg);
                        //공지사항
                        PreferenceUtil.setNotificationAnnouncement(SettingsActivity.this, pushAnnouncement.equals("Y"));
                        //설명회
                        PreferenceUtil.setNotificationSeminar(SettingsActivity.this, pushInformationSession.equals("Y"));
                        //출석
                        PreferenceUtil.setNotificationAttendance(SettingsActivity.this, pushAttendance.equals("Y"));
                        //시스템알림
                        PreferenceUtil.setNotificationSystem(SettingsActivity.this, pushSystem.equals("Y"));
//                        if(isUpdateSubscribeTopic) {
//                            Utils.requestUpdatePushTopic(mContext, _memberSeq);
//                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {

                }
            });
        }
    }

    private void startActivity(){
        startActivity(new Intent(mContext, SetAccountActivity.class));
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
}