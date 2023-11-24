package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.common.Constants.USER_TYPE_STUDENT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.model.request.SigninRequest;
import kr.jeet.edu.student.model.response.LoginResponse;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.sns.AppleLoginManager;
import kr.jeet.edu.student.sns.GoogleLoginManager;
import kr.jeet.edu.student.utils.HttpUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import kr.jeet.edu.student.sns.KaKaoLoginManager;
import kr.jeet.edu.student.sns.NaverLoginManager;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends BaseActivity {

    private String TAG = IntroActivity.class.getSimpleName();

    private RetrofitApi mRetrofitApi;

    // handler
    private final int HANDLER_AUTO_LOGIN = 1;  // 자동로그인
    private final int HANDLER_REQUEST_LOGIN = 2;       // 로그인 화면으로 이동

    private NaverLoginManager mNaverLogin = null;
    private KaKaoLoginManager mKaKaoLogin = null;
    private GoogleLoginManager mGoogleLogin = null;
    private AppleLoginManager mAppleLogin = null;

    private AppCompatActivity mActivity = null;
    private PushMessage _pushMessage = null;

    private int stCodeParent = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case HANDLER_AUTO_LOGIN :
                    int loginType = PreferenceUtil.getLoginType(mContext);
                    LogMgr.e(TAG,"LoginType = " + loginType);

                    if(loginType == Constants.LOGIN_TYPE_NORMAL) {
                        String userId = PreferenceUtil.getUserId(mContext);
                        String userPw = PreferenceUtil.getUserPw(mContext);

                        if(!userId.isEmpty() && !userPw.isEmpty()) {
                            SigninRequest request = new SigninRequest();
                            request.id = userId;
                            request.pw = userPw;
                            request.userGubun = PreferenceUtil.getUserGubun(mContext);

                            requestLogin(request);
                        } else {
                            Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            startLogin();
                        }
                    }
                    else {
                        String SnsUserId = PreferenceUtil.getSNSUserId(mContext);
                        LogMgr.e(TAG,"SnsUserId = " + SnsUserId);

                        if(loginType == Constants.LOGIN_TYPE_SNS_NAVER) {
                            if(SnsUserId != null && !SnsUserId.equals("")) {
                                mNaverLogin = new NaverLoginManager(mContext);
                                mNaverLogin.setHandler(mHandler);
                                mNaverLogin.LoginProcess();
                            }
                            else { emptyUserInfo(); }
                        }
                        else if(loginType == Constants.LOGIN_TYPE_SNS_KAKAO) {
                            if(SnsUserId != null && !SnsUserId.equals("")) {
                                mKaKaoLogin = new KaKaoLoginManager(mContext);
                                mKaKaoLogin.setHandler(mHandler);
                                mKaKaoLogin.LoginProcess();
                            }
                            else { emptyUserInfo(); }
                        }
                        else if(loginType == Constants.LOGIN_TYPE_SNS_GOOGLE) {
                            if (SnsUserId != null && !SnsUserId.equals("")){
                                mGoogleLogin.setHandler(mHandler);
                                mGoogleLogin.LoginProcess();
                            }
                            else { emptyUserInfo(); }
                        }
                        else if(loginType == Constants.LOGIN_TYPE_SNS_APPLE) {
                            if (SnsUserId != null && !SnsUserId.equals("")){
                                mAppleLogin = new AppleLoginManager(mActivity);
                                mAppleLogin.setHandler(mHandler);
                                mAppleLogin.LoginProcess();
                            }
                            else { emptyUserInfo(); }
                        }
                    }

                    break;

                case HANDLER_REQUEST_LOGIN :
                    startLogin();
                    break;

                case Constants.HANDLER_SNS_LOGIN_COMPLETE:
                    LogMgr.e(TAG, "SNS_LOGIN_COMPLETE");

                    String snsId = (String) msg.obj;
                    if(snsId != null && !snsId.isEmpty()) {
                        requestLoginFromSns(snsId);
                    }
                    break;
            }
        }
    };

    private void emptyUserInfo(){
        Toast.makeText(mContext, R.string.empty_user_info, Toast.LENGTH_SHORT).show();
        //Login 화면으로..
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setStatusAndNavigatinBar(true);
        mContext = this;
        mActivity = this;

        mGoogleLogin = new GoogleLoginManager(mActivity);

        // todo. 앱 버전 체크
        // todo. 권한 처리
        initIntentData();
        initView();
        checkPermissions();
    }
    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)){
                LogMgr.e(TAG, "push msg ");
                _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                LogMgr.e(TAG, "msg = " + _pushMessage.body + ", " + _pushMessage.connSeq);
            }else{
                LogMgr.e(TAG, "push msg is null");
            }
//            if(intent.getExtras() != null) {
//                Bundle map = intent.getExtras();
//                for (String key : map.keySet()) {
//                    LogMgr.e(TAG, "key = " + key + " : value = " + map.get(key));
//                }
//            }
        }else{
            LogMgr.e(TAG, "intent is null");
        }
    }
    @Override
    void initAppbar() {
        //do nothing
    }
    @Override
    void initView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int radius = screenHeight / 200;
        DataManager.getInstance().DOT_SIZE = radius;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkPermissions() {
        String[] requiredPermissions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            requiredPermissions = new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        LogMgr.e(TAG, "permission granted");
                        startIntro();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        LogMgr.e(TAG, "denied permission = " + deniedPermissions);
                        showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.intro_failed_request_permission), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        hideMessageDialog();
                                        finish();
                                    }
                                },
                                null, false);
                    }
                })
                .setPermissions(requiredPermissions)
                .setDeniedMessage(getString(R.string.msg_intro_denied_permission))
                .setDeniedCloseButtonText(getString(R.string.title_permission_close))
                .setGotoSettingButton(true)
                .setGotoSettingButtonText(getString(R.string.title_permission_setting))
                .check();
    }
    private void startIntro() {
        LogMgr.e(TAG, PreferenceUtil.getUserGubun(this) + " / " + PreferenceUtil.getUserSTCode(this));
        //푸쉬전송을 눌러서 들어온 경우에 로그인이 되어 있을 때
        if(_pushMessage != null && PreferenceUtil.getUserGubun(this) >= USER_TYPE_STUDENT && PreferenceUtil.getUserSeq(this) > 0) {  //isFromPush
            if (PreferenceUtil.getUserGubun(this) == Constants.USER_TYPE_PARENTS){
                requestChildStudentInfo(PreferenceUtil.getUserSeq(this));

            }else if (PreferenceUtil.getUserGubun(this) == USER_TYPE_STUDENT){
                startSNSMain(PreferenceUtil.getStuSeq(this), PreferenceUtil.getUserSTCode(this));
            }
        }else { //일반 실행
            long delayTime = 1000;
            if (PreferenceUtil.getAutoLogin(mContext)) {
                // 자동 로그인
                mHandler.sendEmptyMessageDelayed(HANDLER_AUTO_LOGIN, delayTime);
            } else {
                mHandler.sendEmptyMessageDelayed(HANDLER_REQUEST_LOGIN, delayTime);
            }
        }
    }

    private void startLogin() {
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    private void requestLogin(SigninRequest request) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signIn(request.id, request.pw).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    hideProgressDialog();
                    if(response.isSuccessful()) {
                        try{

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

                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.teacher_impossible_login), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    hideMessageDialog();
                                                    startLogin();
                                                    finish();
                                                }
                                            },
                                            null, false);

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){

                                        Utils.refreshPushToken(mContext, res.data.seq);

                                        if (res.data.isOriginalMember.equals(Constants.MEMBER)){
                                            LogMgr.e(TAG, "회원 로그인");
                                            if (res.data.userGubun == Constants.USER_TYPE_PARENTS){
                                                requestChildStudentInfo(res.data.seq);

                                            }else if (res.data.userGubun == USER_TYPE_STUDENT){
                                                startMain(res.data.seq, res.data.stCode);
                                            }
                                            PreferenceUtil.setUserType(mContext, Constants.MEMBER);

                                        }else{
                                            LogMgr.e(TAG, "비회원 로그인");
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

                            if(response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND || response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR) {
                                // {"msg":"NOT_FOUND_MEMBER"}
                                Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                            startLogin();

                        }catch (Exception e){

                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    hideProgressDialog();
                    try {
                        LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    startLogin();
                }
            });
        }
    }

    private void requestLoginFromSns(String SnsId) {

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.signInSNS(SnsId).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    hideProgressDialog();
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
                                    PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_NORMAL);
                                    PreferenceUtil.setSNSUserId(mContext, "");
                                    PreferenceUtil.setAutoLogin(mContext, false);
                                    //Utils.refreshPushToken(mContext, PreferenceUtil.getUserSeq(mContext), "");
                                    PreferenceUtil.setPrefPushToken(mContext, "");

                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.teacher_impossible_login), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    startLogin();
                                                    hideMessageDialog();
                                                    finish();
                                                }
                                            },
                                            null, false);

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){

                                        Utils.refreshPushToken(mContext, res.data.seq);

                                        if (res.data.isOriginalMember.equals(Constants.MEMBER)){
                                            LogMgr.e(TAG, "sns 회원 인트로에서 로그인");
                                            if (res.data.userGubun == Constants.USER_TYPE_PARENTS){
                                                requestChildStudentInfo(res.data.seq);

                                            }else if (res.data.userGubun == USER_TYPE_STUDENT){
                                                startSNSMain(res.data.seq, res.data.stCode);
                                            }
                                            PreferenceUtil.setUserType(mContext, Constants.MEMBER);

                                        }else{
                                            LogMgr.e(TAG, "sns 비회원 인트로에서 로그인");
                                            startSNSMain(res.data.seq, res.data.stCode);
                                            PreferenceUtil.setUserType(mContext, Constants.NON_MEMBER);
                                        }
                                    }

                                }
                            }

                        }catch (Exception e){ LogMgr.e(TAG, "requestLoginFromSns() Exception : " + e.getMessage()); }

                    } else {

                        try {
                            LogMgr.e(TAG, "requestLoginFromSns() errBody : " + response.errorBody().string());

                            if(response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                // {"msg":"NOT_FOUND_MEMBER"}
                                Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                            startLogin();

                        } catch (IOException e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    hideProgressDialog();
                    try {
                        LogMgr.e(TAG, "requestLoginFromSns() onFailure >> " + t.getMessage());
                        startLogin();
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                    }
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
                    hideProgressDialog();
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
                                        PreferenceUtil.setStuGender(mContext, getData.get(lastIndex).gender);

                                    }else{
                                        Intent intent = new Intent(mContext, SelectStudentActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putParcelableArrayListExtra(IntentParams.PARAM_CHILD_STUDENT_INFO, getData);
                                        if(_pushMessage != null) {
                                            LogMgr.e(TAG, "EVENT intro pushConnSeq: " + _pushMessage.connSeq+"");
                                            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }

                        }else{

                        }
                    }catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<SearchChildStudentsResponse> call, Throwable t) {
                    hideProgressDialog();

                    try {
                        LogMgr.e(TAG, "requestStudentInfo() onFailure >> " + t.getMessage());
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        startLogin();
                    } catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }
                }
            });
        }
    }

    private void startSNSMain(int seq, int stCode){

        PreferenceUtil.setStuSeq(mContext, seq);
        PreferenceUtil.setUserSTCode(mContext, stCode);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(_pushMessage != null) {
            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
        }
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

            Intent intent = new Intent(mContext, MainActivity.class);
            if(_pushMessage != null) {
                intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(mContext, R.string.empty_child_stu_info, Toast.LENGTH_SHORT).show();
        }
    }

}