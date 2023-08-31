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

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ScheduleData;

public class ScheduleDialog extends Dialog {

    private Context context;
    private TextView tvTitle, tvCampus, tvDate, tvClass, tvSchedule;
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

        tvTitle = findViewById(R.id.tv_title);
        tvCampus = findViewById(R.id.tv_campus);
        tvDate = findViewById(R.id.tv_date);
        tvClass = findViewById(R.id.tv_class);
        tvSchedule = findViewById(R.id.tv_schedule);

        btnClose = findViewById(R.id.btn_dialog_close);
    }

    public void setData(ScheduleData item){
        tvTitle.setText(TextUtils.isEmpty(item.year+"") ? "" : item.year+"");
        tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : item.acaName);
        //tvDate.setText(TextUtils.isEmpty(item) ? "" : item);
        //tvClass.setText(TextUtils.isEmpty(item) ? "" : item);
        tvSchedule.setText(TextUtils.isEmpty(item.content) ? "" : item.content);
    }

    public void setTitle(String str){ tvTitle.setText(TextUtils.isEmpty(str) ? "" : str); }
    public void setCampus(String str){ tvCampus.setText(TextUtils.isEmpty(str) ? "" : str); }
    public void setDate(String str){ tvDate.setText(TextUtils.isEmpty(str) ? "" : str); }
    public void setClass(String str){ tvClass.setText(TextUtils.isEmpty(str) ? "" : str); }
    public void setSchedule(String str){ tvSchedule.setText(TextUtils.isEmpty(str) ? "" : str); }

    public void setOnCloseClickListener(View.OnClickListener listener){ btnClose.setOnClickListener(listener); }
}
