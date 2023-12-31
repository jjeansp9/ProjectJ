package kr.jeet.edu.student.sns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfile;
import com.navercorp.nid.profile.data.NidProfileResponse;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.login.JoinActivity;
import kr.jeet.edu.student.activity.login.LoginActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;

public class NaverLoginManager extends SNSLoginManager {
    private static final String TAG = NaverLoginManager.class.getSimpleName();

    private Context mContext;
    private NidOAuthLogin mNidOAuthLogin = new NidOAuthLogin();

    public NaverLoginManager(Context context) {
        super(context);
        this.mContext = context;
        NaverIdLoginSDK.INSTANCE.initialize(mContext, mContext.getString(R.string.naver_client_id), mContext.getString(R.string.naver_client_secret), mContext.getString(R.string.naver_client_name));
    }

    @Override
    public void LoginProcess() {
        //super.LoginProcess();
        try {

            NaverIdLoginSDK.INSTANCE.authenticate(mContext, oAuthLoginNaverCallback);
        }
        catch (Exception ex) {
            LogMgr.e(TAG, ex.getMessage());
        }

    }

    @Override
    public void LogoutProcess() {
        try {
            NaverIdLoginSDK.INSTANCE.logout();
        }
        catch (Exception ex) {
            LogMgr.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void DeleteAccountProcess() { // 회원탈퇴
        try{
            if (mNidOAuthLogin != null){
                mNidOAuthLogin.callDeleteTokenApi(mContext, new OAuthLoginCallback() {
                    @Override
                    public void onSuccess() {
                        LogMgr.e(TAG, "Naver Account Delete Success");
                    }
                    @Override
                    public void onFailure(int i, @NonNull String s) {
                        // 네이버로 로그인이 되어있지 않음
                        // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                        // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                        LogMgr.e(TAG, "Naver Account Delete Fail : "+ s);
                    }

                    @Override
                    public void onError(int i, @NonNull String s) {
                        // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                        // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                        LogMgr.e(TAG, "Naver Account Delete Error : "+ s);
                    }
                });
            }else{
                LogMgr.e(TAG,"mNidOAuthLogin is null");
            }
        }catch (Exception e){

        }
    }

    private OAuthLoginCallback oAuthLoginNaverCallback = new OAuthLoginCallback() {
        @Override
        public void onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            LogMgr.e("로그인 성공");
//            LogMgr.e("AccessToken -> " + NaverIdLoginSDK.INSTANCE.getAccessToken());
//            LogMgr.e("RefreshToken -> " + NaverIdLoginSDK.INSTANCE.getRefreshToken());
//            LogMgr.e("Expires -> " + NaverIdLoginSDK.INSTANCE.getExpiresAt());
//            LogMgr.e("Type -> " + NaverIdLoginSDK.INSTANCE.getTokenType());
//            LogMgr.e("State -> " + NaverIdLoginSDK.INSTANCE.getState());
//
            if(mNidOAuthLogin != null) {
                mNidOAuthLogin.callProfileApi(nidProfileCallback);
            }

        }

        @Override
        public void onFailure(int i, @NonNull String s) {
            String errorCode = NaverIdLoginSDK.INSTANCE.getLastErrorCode().getCode();
            String errorDescription = NaverIdLoginSDK.INSTANCE.getLastErrorDescription();
            LogMgr.e("errorCode -> " + errorCode);
            LogMgr.e("errorDescription -> " + errorDescription);
            //Toast.makeText(mContext, "errorCode : " + errorCode, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int i, @NonNull String s) {
            onFailure(i, s);
        }
    };

    private NidProfileCallback nidProfileCallback = new NidProfileCallback() {
        @Override
        public void onSuccess(Object o) {
            NidProfileResponse result = (NidProfileResponse) o ;
            NidProfile profile = result.getProfile();
            String userId = "";
            String gender = "";
            String name = "";
            if(profile != null) {
                userId = profile.getId();
                gender = profile.getGender();
                name = profile.getName();
                LogMgr.e("userId -> " + userId);
                LogMgr.e("gender -> " + gender);
                LogMgr.e("name -> " + name);

                PreferenceUtil.setSNSUserId(mContext, userId);
                PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_SNS_NAVER);
            }

            if(mHandler != null) {
                Bundle data = new Bundle();
                data.putString(IntentParams.PARAM_LOGIN_USER_NAME, name);
                data.putString(IntentParams.PARAM_LOGIN_USER_GENDER, gender);

                Message msg = Message.obtain();
                msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                msg.obj = userId;
                msg.setData(data);
                mHandler.sendMessage(msg);
            }
        }

        // TODO : 로그인 실패, 에러났을 때 사용자에게 Toast등과 같은 알림이 필요해보임
        @Override
        public void onFailure(int i, @NonNull String s) {
//            mContext.startActivity(new Intent(mContext, LoginActivity.class));
//            ((Activity)mContext).finish();
        }

        @Override
        public void onError(int i, @NonNull String s) {
//            mContext.startActivity(new Intent(mContext, LoginActivity.class));
//            ((Activity)mContext).finish();
        }
    };
}

