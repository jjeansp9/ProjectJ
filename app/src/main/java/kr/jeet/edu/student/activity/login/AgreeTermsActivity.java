package kr.jeet.edu.student.activity.login;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.activity.PrivacySeeContentActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.sns.AppleLoginManager;
import kr.jeet.edu.student.sns.GoogleLoginManager;
import kr.jeet.edu.student.sns.KaKaoLoginManager;
import kr.jeet.edu.student.sns.NaverLoginManager;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.CustomAppbarLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AgreeTermsActivity extends BaseActivity {

    private static final String TAG = "AgreeTermsActivity";

    private CheckBox mAllCheckBox, mCheckBox1, mCheckBox2;
    private TextView mTvCheck1, mTvCheck2;
    private Button mBtnNext;
    private int mLoginType = Constants.LOGIN_TYPE_NORMAL;

    private String url = "";

    private String snsName = "";
    private String snsGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree_terms);
        setStatusAndNavigatinBar(true);
        mContext = this;

        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_LOGIN_TYPE)){
                mLoginType = intent.getIntExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);

            }else{
                LogMgr.e("no intent extra");
            }
            if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_NAME)) snsName = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_NAME);
            if (intent.hasExtra(IntentParams.PARAM_LOGIN_USER_GENDER)) snsGender = intent.getStringExtra(IntentParams.PARAM_LOGIN_USER_GENDER);

            LogMgr.e(TAG,
                    "snsName: " + snsName +
                    "\nsnsGender: " + snsGender
            );
        }
        initAppbar();
        initView();
    }


    void initAppbar(){
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.terms_agree);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    void initView() {
        //findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.layout_all_check).setOnClickListener(this);
        findViewById(R.id.layout_check1).setOnClickListener(this);
        findViewById(R.id.layout_check2).setOnClickListener(this);
        findViewById(R.id.layout_view_check1).setOnClickListener(this);
        findViewById(R.id.layout_view_check2).setOnClickListener(this);

        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);

        mTvCheck1 = findViewById(R.id.tv_check1);
        mTvCheck2 = findViewById(R.id.tv_check2);

        mAllCheckBox = (CheckBox) findViewById(R.id.cb_all);
        mCheckBox1 = (CheckBox) findViewById(R.id.check1);
        mCheckBox2 = (CheckBox) findViewById(R.id.check2);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.btn_next:
                if(mAllCheckBox.isChecked() == false) {
                    Toast.makeText(mContext, R.string.terms_agreement_msg, Toast.LENGTH_SHORT).show();
                    return ;
                }
                startActivity(snsName, snsGender, mLoginType);
                break;

            case R.id.layout_all_check:
                if(!mAllCheckBox.isChecked()) {
                    mAllCheckBox.setChecked(true);
                    mCheckBox1.setChecked(true);
                    mCheckBox2.setChecked(true);
                    mBtnNext.setBackgroundResource(R.drawable.selector_bt_ubderbox);
                } else {
                    mAllCheckBox.setChecked(false);
                    mCheckBox1.setChecked(false);
                    mCheckBox2.setChecked(false);
                    mBtnNext.setBackgroundResource(R.drawable.bt_click_cancel);
                }
                break;

            case R.id.layout_check1:
                mCheckBox1.setChecked(!mCheckBox1.isChecked());
                allCheck();
                break;
            case R.id.layout_check2:
                mCheckBox2.setChecked(!mCheckBox2.isChecked());
                allCheck();
                break;

            case R.id.layout_view_check1:
                url = Constants.POLICY_SERVICE;
                startPvyActivity(mTvCheck1.getText().toString());
                break;
            case R.id.layout_view_check2:
                url = Constants.POLICY_PRIVACY;
                startPvyActivity(mTvCheck2.getText().toString());
                break;

        }
    }

    private void allCheck(){
        if(mCheckBox1.isChecked() && mCheckBox2.isChecked()) {
            mAllCheckBox.setChecked(true);
            mBtnNext.setBackgroundResource(R.drawable.selector_bt_ubderbox);

        } else {
            mAllCheckBox.setChecked(false);
            mBtnNext.setBackgroundResource(R.drawable.bt_click_cancel);
        }
    }

    private void startActivity(String name, String gender, int snsType){
        Intent intent = new Intent(mContext, JoinActivity.class);
        intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, snsType);
        if (!TextUtils.isEmpty(name)) intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
        if (!TextUtils.isEmpty(gender)) intent.putExtra(IntentParams.PARAM_LOGIN_USER_GENDER, gender);
        startActivity(intent);
        finish();
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }
}