package kr.jeet.edu.student.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.model.response.BriefingDetailResponse;
import kr.jeet.edu.student.model.response.ScheduleDetailResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule_detail);
        mContext = this;
        initView();
    }

    private void initData(){
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
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(getString(R.string.main_menu_campus_schedule));
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
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMd", Locale.KOREA);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREA);

            String getDate = String.format(Locale.KOREA, "%d%d%d", mInfo.year, mInfo.month, mInfo.day);
            Date date = inputDateFormat.parse(getDate);

            if (date != null) tvDate.setText(outputFormat.format(date));

        } catch (ParseException e) { e.printStackTrace(); }

        String str = TextUtils.isEmpty(mInfo.acaName) ? "" : mInfo.acaName;
        tvCampus.setText(str);

        str = TextUtils.isEmpty(mInfo.title) ? "" : mInfo.title;
        tvTitle.setText(str);

        str = TextUtils.isEmpty(mInfo.target) ? "" : "["+mInfo.target+"]";
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
}