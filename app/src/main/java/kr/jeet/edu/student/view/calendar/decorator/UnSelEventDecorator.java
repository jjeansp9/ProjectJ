package kr.jeet.edu.student.view.calendar.decorator;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;

public class UnSelEventDecorator implements DayViewDecorator {

    private static final String TAG = "UnSelEventDecorator";

    private int color;
    private Context context;
    private CalendarDay selDay;

    public UnSelEventDecorator(Context mContext) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.red);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        //LogMgr.i(TAG + " <<< UnSel >>> ",day.equals(selDay) ? "true" + "day: " + day + ", selDay: " + selDay : "false" + "day: " + day + ", selDay: " + selDay);
        return day != null && day.equals(selDay);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) view.addSpan(new DotSpan(7f, color));
    }
}
