package kr.jeet.edu.student.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class ScheduleDialog extends Dialog {

    private Context context;
    private TextView tvDate, tvCampus, tvTitle, tvTarget, tvContent;
    private ImageButton btnClose;

    public ScheduleDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public ScheduleDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    protected ScheduleDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        initView();
    }

    private void initView(){
        this.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_dialog_schedule);

        findViewById(R.id.dialog_ly).setClipToOutline(true);

        tvDate = findViewById(R.id.tv_date);
        tvCampus = findViewById(R.id.tv_campus);
        tvTitle = findViewById(R.id.tv_title);
        tvTarget = findViewById(R.id.tv_class);
        tvContent = findViewById(R.id.tv_schedule);

        btnClose = findViewById(R.id.btn_dialog_close);
    }

    public void setData(ScheduleData item){

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd ", Locale.KOREA);
        Date date = null;

        String resultDate = "";

        try {

            //String getDate = item.year+""+item.month+""+item.day;
            String getDate = String.format(Locale.KOREA, "%d%d%d", item.year, item.month, item.day);
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

            resultDate = formattedDate+"("+dayOfWeekStr+")";

        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvDate.setText(
                resultDate
        );
        tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : "["+item.acaName+"]");
        tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
        tvTarget.setText(TextUtils.isEmpty(item.target) ? "" : "["+item.target+"]");
        tvContent.setText(TextUtils.isEmpty(item.content) ? "" : item.content);
    }

    public void setOnCloseClickListener(View.OnClickListener listener){ btnClose.setOnClickListener(listener); }
}
