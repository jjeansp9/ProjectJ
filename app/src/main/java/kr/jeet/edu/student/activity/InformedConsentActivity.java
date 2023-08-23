package kr.jeet.edu.student.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class InformedConsentActivity extends BaseActivity {

    private static final String TAG = "informedConsentActivity";

    private CheckBox mCbAll, mCbPvyCollection, mCbPvyThirdParty, mCbPvyConsignment, mCbPvyMarketing;
    private TextView mTvAll, mTvPvyCollection, mTvPvyThirdParty, mTvPvyConsignment, mTvPvyMarketing;

    private String check1, check2, check3, check4 = "";
    private ArrayList<String> checkItem = new ArrayList<>();

    private static final String CHECKED = "Y";
    private static final String NON_CHECKED = "N";

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED) && "finish_activity".equals(intent.getAction())) {
                intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
                setResult(RESULT_OK, intent);

            }
        }
        finish();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informed_consent);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        for (int i = 0; i <= 3; i++) checkItem.add(CHECKED);
    }

    @Override
    void initView() {
        mCbAll = findViewById(R.id.cb_check_all);

        mCbPvyCollection = findViewById(R.id.cb_check_privacy_collection);
        mCbPvyThirdParty = findViewById(R.id.cb_check_privacy_third_party);
        mCbPvyConsignment = findViewById(R.id.cb_check_privacy_consignment);
        mCbPvyMarketing = findViewById(R.id.cb_check_privacy_marketing);

        mTvPvyCollection = findViewById(R.id.tv_privacy_collection);
        mTvPvyThirdParty = findViewById(R.id.tv_privacy_third_party);
        mTvPvyConsignment = findViewById(R.id.tv_privacy_consignment);
        mTvPvyMarketing = findViewById(R.id.tv_privacy_marketing);

        findViewById(R.id.layout_check_all).setOnClickListener(this);

        // 체크박스 Text
        findViewById(R.id.layout_privacy_collection).setOnClickListener(this);
        findViewById(R.id.layout_privacy_third_party).setOnClickListener(this);
        findViewById(R.id.layout_privacy_consignment).setOnClickListener(this);
        findViewById(R.id.layout_privacy_marketing).setOnClickListener(this);

        // 내용 보기
        findViewById(R.id.layout_view_collection).setOnClickListener(this);
        findViewById(R.id.layout_view_third_party).setOnClickListener(this);
        findViewById(R.id.layout_view_consignment).setOnClickListener(this);
        findViewById(R.id.layout_view_marketing).setOnClickListener(this);

        // 다음 버튼
        findViewById(R.id.btn_informed_consent_next).setOnClickListener(this);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.informed_consent_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_check_all:
                mCbAll.setChecked(!mCbAll.isChecked());
                if(mCbAll.isChecked()) {
                    checkAll(true);
                }
                else {
                    checkAll(false);
                }
                break;

            case R.id.layout_privacy_collection:
                mCbPvyCollection.setChecked(!mCbPvyCollection.isChecked());
                if (mCbPvyCollection.isChecked()) checkItem.set(0, CHECKED);
                else checkItem.set(0, NON_CHECKED);
                checkConfirm();
                break;

            case R.id.layout_privacy_third_party:
                mCbPvyThirdParty.setChecked(!mCbPvyThirdParty.isChecked());
                if (mCbPvyThirdParty.isChecked()) checkItem.set(1, CHECKED);
                else checkItem.set(1, NON_CHECKED);
                checkConfirm();
                break;

            case R.id.layout_privacy_consignment:
                mCbPvyConsignment.setChecked(!mCbPvyConsignment.isChecked());
                if (mCbPvyConsignment.isChecked()) checkItem.set(2, CHECKED);
                else checkItem.set(2, NON_CHECKED);
                checkConfirm();
                break;

            case R.id.layout_privacy_marketing:
                mCbPvyMarketing.setChecked(!mCbPvyMarketing.isChecked());
                if (mCbPvyMarketing.isChecked()) checkItem.set(3, CHECKED);
                else checkItem.set(3, NON_CHECKED);
                checkConfirm();
                break;

            case R.id.layout_view_collection: // 내용보기 (개인정보 수집 및 이용)

                startPvyActivity(mTvPvyCollection.getText().toString());
                break;

            case R.id.layout_view_third_party: // 내용보기 (개인정보 제2자 정보 제공)
                startPvyActivity(mTvPvyThirdParty.getText().toString());
                break;

            case R.id.layout_view_consignment: // 내용보기 (개인정보 위탁)
                startPvyActivity(mTvPvyConsignment.getText().toString());
                break;

            case R.id.layout_view_marketing: // 내용보기 (개인정보 마케팅 활용 동의)
                startPvyActivity(mTvPvyMarketing.getText().toString());
                break;

            case R.id.btn_informed_consent_next: // 다음
                startWriteActivity();
                break;
        }
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        startActivity(intent);
    }

    private void checkAll(boolean setCheck){
        mCbPvyCollection.setChecked(setCheck);
        mCbPvyThirdParty.setChecked(setCheck);
        mCbPvyConsignment.setChecked(setCheck);
        mCbPvyMarketing.setChecked(setCheck);

        if (setCheck) for (int i= 0; i < checkItem.size(); i++) checkItem.set(i, CHECKED);
        else for (int i= 0; i < checkItem.size(); i++) checkItem.set(i, NON_CHECKED);
    }

    private void checkConfirm(){
        if (mCbPvyCollection.isChecked() &&
                mCbPvyThirdParty.isChecked() &&
                mCbPvyConsignment.isChecked() &&
                mCbPvyMarketing.isChecked()) {
            mCbAll.setChecked(true);
        }
        else mCbAll.setChecked(false);
    }
    
    private void startWriteActivity(){
        if (mCbPvyCollection.isChecked() &&
                mCbPvyThirdParty.isChecked() &&
                mCbPvyConsignment.isChecked()){
            LevelTestRequest request = new LevelTestRequest();

            request.check1 = checkItem.get(0);
            request.check2 = checkItem.get(1);
            request.check3 = checkItem.get(2);
            request.check4 = checkItem.get(3);

            Intent intent = new Intent(mContext, MenuTestReserveWriteActivity.class);
            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_WRITE, request);
            resultLauncher.launch(intent);
        }
        else Toast.makeText(mContext, R.string.informed_consent_impossible_activity_transition, Toast.LENGTH_SHORT).show();
        
    }
}































