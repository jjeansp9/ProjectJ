package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.common.Constants.TIME_FORMATTER_YYYY_M_D_E;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.model.response.ScheduleDetailResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuScheduleDetailActivity extends BaseActivity {

    private static final String TAG = "ScheduleDetailActivity";

    private TextView tvDate, tvCampus, tvTitle, tvTarget, tvContent;

    private ScheduleData mInfo;
    private int _currentSeq = -1;
    private String title = "";

    private int _stCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule_detail);
        overridePendingTransition(R.anim.horizon_enter, R.anim.none);
        mContext = this;
        initView();
        Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_ACA_SCHEDULE);
    }

    private void initData(){

        _stCode = PreferenceUtil.getUserSTCode(mContext);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(IntentParams.PARAM_SCHEDULE_INFO)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO, ScheduleData.class);
            }else{
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO);
            }

            setView();

        }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
            PushMessage message = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }else{
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
            }
            _currentSeq = message.connSeq;

            if (_currentSeq != -1) requestScheduleDetail(_currentSeq);

            if (message.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(message, _stCode);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(getString(R.string.title_detail));
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        tvDate = findViewById(R.id.tv_sc_detail_date);
        tvCampus = findViewById(R.id.tv_sc_detail_campus);
        tvTarget = findViewById(R.id.tv_sc_detail_target);
        tvTitle = findViewById(R.id.tv_sc_detail_title);
        tvContent = findViewById(R.id.tv_sc_detail_content);

        initData();


        initAppbar();
    }

    private void setView(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(mInfo.year, mInfo.month - 1, mInfo.day);

        // 원하는 날짜 형식으로 포맷합니다.
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMATTER_YYYY_M_D_E, Locale.getDefault());
        Date date = calendar.getTime();

        tvDate.setText(sdf.format(date));

        String str = "";

        str = TextUtils.isEmpty(mInfo.acaName) ? "" : mInfo.acaName;
        tvCampus.setText(str);

        str = TextUtils.isEmpty(mInfo.title) ? "" : mInfo.title;
        tvTitle.setText(str);

        str = TextUtils.isEmpty(mInfo.acaGubunName) ? "" : "["+mInfo.acaGubunName +"]";
        viewVisibility(tvTarget, str);

        str = TextUtils.isEmpty(mInfo.content) ? "" : mInfo.content;
        viewVisibility(tvContent, str);
    }

    private void viewVisibility(TextView tv, String str){
        if (!TextUtils.isEmpty(str)) tv.setText(str);
        else tv.setVisibility(View.GONE);
    }

    private void requestScheduleDetail(int ptSeq){
        if (RetrofitClient.getInstance() != null){
            showProgressDialog();
            RetrofitClient.getApiInterface().getScheduleDetail(ptSeq).enqueue(new Callback<ScheduleDetailResponse>() {
                @Override
                public void onResponse(Call<ScheduleDetailResponse> call, Response<ScheduleDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            if (response.body() != null) {

                                ScheduleData data = response.body().data;
                                if (data != null){
                                    mInfo = data;
                                    setView();
                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            finish();
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestScheduleDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<ScheduleDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestScheduleDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    finish();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.horizon_exit);
    }
}