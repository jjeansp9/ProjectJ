package kr.jeet.edu.student.sns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.JoinActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;

public class AppleLoginManager extends SNSLoginManager {
    private static final String TAG = "AppleLoginManager";

    private ComponentActivity mActivity;
    private OAuthProvider.Builder provider;
    private FirebaseAuth mAuth;
    private AlertDialog mProgressDialog = null;

    public AppleLoginManager(AppCompatActivity activity) {
        super(activity);
        this.mActivity = activity;
        initAuth();
    }

    private void initAuth(){
        if (provider == null){
            provider = OAuthProvider.newBuilder("apple.com");
            List<String> scopes = new ArrayList<>();
            scopes.add("email");
            scopes.add("name");
            provider.setScopes(scopes);
            provider.addCustomParameter("locale", Constants.LOCALE_LANGUAGE_KR); // 한국어로 설정
        }
    }

    @Override
    public void LoginProcess() {
        showProgressDialog();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();

            Task<AuthResult> pending = mAuth.getPendingAuthResult();
            if (pending != null) {
                pending.addOnSuccessListener( authResult -> {
                    FirebaseUser user = authResult.getUser();
                    //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null){
                        String name = user.getDisplayName();
                        String email = Utils.getStr(user.getEmail());
                        String additionalUserInfo = Utils.getStr(Objects.requireNonNull(authResult.getAdditionalUserInfo()).getUsername());
                        String credential = Utils.getStr(Objects.requireNonNull(authResult.getCredential()).toString());
                        String uId = Utils.getStr(user.getUid());
                        String tenantId = Utils.getStr(user.getTenantId());
                        String providerId = Utils.getStr(user.getProviderId());

                        LogMgr.i(
                                TAG, "apple Info check: " +
                                        "\n name: " + name +
                                        "\n email: " + email +
                                        "\n additionalUserInfo: " + additionalUserInfo +
                                        "\n credential: " + credential +
                                        "\n uId: " + uId +
                                        "\n tenantId: " + tenantId +
                                        "\n providerId: " + providerId
                        );

                        PreferenceUtil.setSNSUserId(mActivity, uId);
                        PreferenceUtil.setLoginType(mActivity, Constants.LOGIN_TYPE_SNS_APPLE);

                        if (mIsJoinStatus){
                            Intent intent = new Intent(mActivity, JoinActivity.class);
                            intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_SNS_APPLE);
                            intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
                            intent.putExtra(IntentParams.PARAM_LOGIN_USER_SNSID, uId);
                            mActivity.startActivity(intent);
                            mActivity.finish();
                        }else{
                            LogMgr.e("login complete -> ");
                            if(mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                                msg.obj = uId;
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                    hideProgressDialog();

                }).addOnFailureListener( error -> {
                    LogMgr.e(TAG, error.getMessage());
                    hideProgressDialog();
                });
            } else {
                startAppleActivity();
            }
        }
    }

    private void startAppleActivity(){
        if (mAuth != null && provider != null){
            mAuth.startActivityForSignInWithProvider(mActivity, provider.build())
                    .addOnSuccessListener( authResult -> {
                        FirebaseUser user = authResult.getUser();
                        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null){
                            String name = user.getDisplayName();
                            String email = Utils.getStr(user.getEmail());
                            String uId = Utils.getStr(user.getUid());
                            String providerId = Utils.getStr(user.getProviderId());

                            LogMgr.i(
                                    TAG, "apple Info: " +
                                            "\n name: " + name +
                                            "\n email: " + email +
                                            "\n uId: " + uId +
                                            "\n providerId: " + providerId
                            );

                            PreferenceUtil.setSNSUserId(mActivity, uId);
                            PreferenceUtil.setLoginType(mActivity, Constants.LOGIN_TYPE_SNS_APPLE);

                            if (mIsJoinStatus){
                                Intent intent = new Intent(mActivity, JoinActivity.class);
                                intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_SNS_APPLE);
                                intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
                                intent.putExtra(IntentParams.PARAM_LOGIN_USER_SNSID, uId);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            }else{
                                LogMgr.e("login complete -> ");
                                if(mHandler != null) {
                                    Bundle data = new Bundle();
                                    data.putString("name", name);
                                    data.putInt("loginType", Constants.LOGIN_TYPE_SNS_APPLE);

                                    Message msg = Message.obtain();
                                    msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                                    msg.obj = uId;
                                    msg.setData(data);
                                    mHandler.sendMessage(msg);
                                }
                            }

//                                    for (UserInfo profile : user.getProviderData()) {
//                                        // Id of the provider (ex: google.com)
//                                        String providerId = profile.getProviderId();
//
//                                        // UID specific to the provider
//                                        String uid = profile.getUid();
//
//                                        // Name, email address
//                                        String name = profile.getDisplayName();
//                                        String email = profile.getEmail();
//
//                                        LogMgr.i(
//                                                TAG, "apple Info: " +
//                                                        "\n name: " + name +
//                                                        "\n email: " + email +
//                                                        "\n providerId: " + providerId +
//                                                        "\n uid: " + uid
//                                        );
//                                    }
                        }
                        hideProgressDialog();

                    }).addOnFailureListener( error -> {
                        LogMgr.e(TAG, error.getMessage());
                        hideProgressDialog();
                    });
        }
    }

    @Override
    public void LogoutProcess() {

    }

    @Override
    public void DeleteAccountProcess() {
        if (mAuth != null){
            mAuth.signOut();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null){
            View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(mActivity.getString(R.string.requesting));

            mProgressDialog = new AlertDialog.Builder(mActivity)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
}