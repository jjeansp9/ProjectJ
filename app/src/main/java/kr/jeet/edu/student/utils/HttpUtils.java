package kr.jeet.edu.student.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.AgreeTermsActivity;
import kr.jeet.edu.student.activity.LoginActivity;
import kr.jeet.edu.student.activity.MainActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.request.SigninRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.LoginResponse;
import kr.jeet.edu.student.model.response.SchoolListResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    /**
    * 학교목록 조회
    * */
    public static void requestSchoolList(){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getSchoolList().enqueue(new Callback<SchoolListResponse>() {
                @Override
                public void onResponse(Call<SchoolListResponse> call, Response<SchoolListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<SchoolData> list = response.body().data;
                                DataManager.getInstance().setSchoolList(list);
                                DataManager.getInstance().initSchoolListMap(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestSchoolList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<SchoolListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestSchoolList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                }
            });
        }
    }

    /**
    * 테스트예약 캠퍼스목록 조회
    * */
    public static void requestLTCList(){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getLTCList().enqueue(new Callback<LTCListResponse>() {
                @Override
                public void onResponse(Call<LTCListResponse> call, Response<LTCListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<LTCData> list = response.body().data;
                                DataManager.getInstance().setLTCList(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestLTCList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<LTCListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLTCList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }

                }
            });
        }
    }
    /**
     * 자체 로그인
     * */
    public static void requestLogin(AppCompatActivity mContext) {
        SigninRequest request = new SigninRequest();
        request.id = PreferenceUtil.getUserId(mContext);
        request.pw = PreferenceUtil.getUserPw(mContext);

        if(RetrofitClient.getInstance() != null) {
             RetrofitClient.getApiInterface().signIn(request.id, request.pw).enqueue(new Callback<LoginResponse>() {
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

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){
                                        PreferenceUtil.setUserSTCode(mContext, res.data.stCode);
                                        if (res.data.userGubun == Constants.USER_TYPE_STUDENT){
                                            Intent intent = new Intent(mContext, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            mContext.startActivity(intent);
                                            mContext.finish();
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

//                                String title = getString(R.string.dialog_title_alarm);
//                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
//                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);
//
//                                if (response.body().msg.equals(Constants.ALREADY_LOGIN_IN)){
//                                    Toast.makeText(mContext, R.string.msg_already_login_in, Toast.LENGTH_SHORT).show();
//
//                                } else if (response.body().msg.equals(Constants.PASSWORD_MISMATCH)){
//                                    Toast.makeText(mContext, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show();
//
//                                } else if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)) {
//                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();
//
//                                } else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)) {
//                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);
//
//                                } else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)) {
//                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
//                                }
                            }else if(response.code() == 404 || response.code() == 401) {
                                // {"msg":"NOT_FOUND_MEMBER"}
                                Toast.makeText(mContext, R.string.login_not_found_member, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    /**
     * sns 로그인
     * */
    public static void requestLoginFromSns(AppCompatActivity mContext) {

        String snsId = PreferenceUtil.getSNSUserId(mContext);

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().signInSNS(snsId).enqueue(new Callback<LoginResponse>() {
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
//                                    showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.teacher_impossible_login), v -> {
//                                                clearLoginInfo();
//                                                hideMessageDialog();
//                                            },
//                                            null, false);
//
//                                    mAppleLogin.DeleteAccountProcess();

                                }else{
                                    if (!String.valueOf(res.data.stCode).equals("null")){
                                        PreferenceUtil.setUserSTCode(mContext, res.data.stCode);
                                        if (res.data.userGubun == Constants.USER_TYPE_STUDENT){
                                            Intent intent = new Intent(mContext, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            mContext.startActivity(intent);
                                            mContext.finish();
                                        }
                                    }
                                }
                            }

                        }catch (Exception e){
                        }

                    } else {

                        try {
                            LogMgr.e(TAG, "requestLogin() errBody : " + response.errorBody().string());

//                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
//
//                                String title = getString(R.string.dialog_title_alarm);
//                                String msgMismatch = getString(R.string.msg_user_gubun_mismatch);
//                                String msgNotJeetMember = getString(R.string.msg_user_0_not_jeet_member);
//
//                                if (response.body().msg.equals(Constants.PARAMETER_BINDING_ERROR)){
//                                    Toast.makeText(mContext, R.string.msg_parameter_binding_error, Toast.LENGTH_SHORT).show();
//
//                                }else if (response.body().msg.equals(Constants.USER_GUBUN_MISMATCH)){
//                                    showMessageDialog(title, msgMismatch, clickOK -> hideMessageDialog(), null, false);
//
//                                }else if (response.body().msg.equals(Constants.USER_NOT_JEET_MEMBER)){
//                                    showMessageDialog(title, msgNotJeetMember, clickOK -> hideMessageDialog(), null, false);
//                                }
//                                if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
//
//                            }else if(response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
//                                // {"msg":"NOT_FOUND_MEMBER"}
//
//                                //로그인 정보가 없을 때..
//                                if(selectedSNSLoginType != -1) {
//                                    Intent intent = null;
//                                    intent = new Intent(mContext, AgreeTermsActivity.class);
//                                    intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, selectedSNSLoginType);
//                                    intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, snsName);
//                                    startActivity(intent);
//                                }else{
//                                    if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
//                                }
//
//                            } else {
//                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                                if (mAppleLogin != null) mAppleLogin.DeleteAccountProcess();
//                            }

                        } catch (IOException e) {
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestLogin() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                }
            });
        }
    }
    /**
     * 로그아웃
     * */
    public static void requestLogOut(AppCompatActivity mContext){
        int memberSeq = PreferenceUtil.getUserSeq(mContext);
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().logout(memberSeq).enqueue(new Callback<BaseResponse>() {
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

                            Utils.showMessageDialog(
                                    mContext.getString(R.string.dialog_title_alarm),
                                    mContext.getString(R.string.informed_question_new_child_add_success),
                                    v -> {
                                        Utils.hideMessageDialog(mContext);

                                        startLogin(mContext);
                                    },
                                    null,
                                    false,
                                    mContext
                            );

                        }else{

                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            startLogin(mContext);
                            LogMgr.e(TAG, "requestLogOut() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestLogOut() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLogOut() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLogOut() Exception : ", e.getMessage()); }
                    startLogin(mContext);
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static void startLogin(AppCompatActivity mContext){
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mContext.finish();
    }
}
