package kr.jeet.edu.student.activity.setting;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.activity.IntroActivity;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.AuthPhoneNumberView;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetPhoneNumActivity extends BaseActivity {
    private String TAG = SetPhoneNumActivity.class.getSimpleName();

    private EditText mEtPhoneNum;
    AuthPhoneNumberView _authPhoneNoView;

    private RetrofitApi mRetrofitApi;

    private int _memberSeq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_phone_num);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){ _memberSeq = PreferenceUtil.getUserSeq(mContext); }

    void initView() {
        findViewById(R.id.btn_update_phone_complete).setOnClickListener(this);

        mEtPhoneNum = findViewById(R.id.edit_phonenum);

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

        showKeyboard(mEtPhoneNum);
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.settings_account_phone_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_update_phone_complete:

                String title = getString(R.string.dialog_title_alarm);
                String msgMismatch = getString(R.string.settings_phone_confirm_dialog_content);

                if(checkValid()) {
                    showMessageDialog(title, msgMismatch, clickOK -> updatePhoneNum() , clickCancel -> hideMessageDialog(), false);
                }

                break;

        }
    }

    private boolean checkValid() {
        if(_authPhoneNoView != null) return _authPhoneNoView.checkValid();

        return true;
    }

    private void updatePhoneNum(){

        hideMessageDialog();

        int memberSeq = _memberSeq;
        String phoneNumber = mEtPhoneNum.getText().toString().trim();

        if(RetrofitClient.getInstance() != null) {
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.updatePhoneNum(memberSeq, phoneNumber).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    hideProgressDialog();
                    try {
                        if (response.isSuccessful()){

                            String msg = response.body().msg;
                            reStartActivity();
                            if (msg != null) LogMgr.e(TAG + "updatePhoneNum() : ", msg);

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "updatePhoneNum() Exception : ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "updatePhoneNum() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void reStartActivity(){
        Intent intent = new Intent(mContext, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }


}