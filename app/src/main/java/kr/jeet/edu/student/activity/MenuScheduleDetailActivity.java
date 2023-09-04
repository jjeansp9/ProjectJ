package kr.jeet.edu.student.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

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
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;

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
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_SCHEDULE_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO, ScheduleData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_SCHEDULE_INFO);
                }

            }else{

            }

        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        initData();

        //tvDate = findViewById(R.id.tv_sc_detail_date);
        tvCampus = findViewById(R.id.tv_sc_detail_campus);
        tvTitle = findViewById(R.id.tv_sc_detail_title);
        tvTarget = findViewById(R.id.tv_sc_detail_target);
        tvContent = findViewById(R.id.tv_sc_detail_content);

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd ", Locale.KOREA);
        Date date = null;

        try {

            String getDate = String.format(Locale.KOREA, "%d%d%d", mInfo.year, mInfo.month, mInfo.day);
            date = inputDateFormat.parse(getDate);

            String formattedDate = "";

            int dayOfWeek = 0;
            if (date != null) {
                formattedDate = outputFormat.format(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            }

            String dayOfWeekStr = Utils.formatDayOfWeek(dayOfWeek).replace("요일", "");

            title = formattedDate+"("+dayOfWeekStr+")";

        } catch (ParseException e) {
            e.printStackTrace();
        }

        String str = TextUtils.isEmpty(mInfo.acaName) ? "" : "["+mInfo.acaName+"]";
        tvCampus.setText(str);

        str = TextUtils.isEmpty(mInfo.title) ? "" : mInfo.title;
        tvTitle.setText(str);

        str = TextUtils.isEmpty(mInfo.target) ? "" : "["+mInfo.target+"]";
        tvTarget.setText(str);

        str = TextUtils.isEmpty(mInfo.content) ? "" : mInfo.content;
        tvContent.setText(str);

        initAppbar();
    }
}