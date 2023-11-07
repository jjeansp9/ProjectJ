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
import kr.jeet.edu.student.model.data.LTCSubjectData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.request.SigninRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.LevelTestSubjectResponse;
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
     * 테스트예약 과목 조회
     * */
    public static void requestLTCSubjectList(){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getLTCSubject().enqueue(new Callback<LevelTestSubjectResponse>() {
                @Override
                public void onResponse(Call<LevelTestSubjectResponse> call, Response<LevelTestSubjectResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<LTCSubjectData> list = response.body().data;
                                DataManager.getInstance().setLTCSubjectList(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestLTCSubjectList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestLTCSubjectList() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<LevelTestSubjectResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLTCSubjectList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLTCSubjectList() Exception : ", e.getMessage()); }

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
