package kr.jeet.edu.student.view.calendar.decorator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;

public class UnSelEventDecorator implements DayViewDecorator {

    private int color;
    private AppCompatActivity activity;
    private CalendarDay selDay;

    public UnSelEventDecorator(AppCompatActivity mActivity) {
        this.activity = mActivity;
        this.color = ContextCompat.getColor(this.activity, R.color.red);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        LogMgr.i("UnSel >>> ",day.equals(selDay) ? "true" + "day: " + day + ", selDay: " + selDay : "false" + "day: " + day + ", selDay: " + selDay);
        return day != null && day.equals(selDay);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && activity != null) activity.runOnUiThread(() -> view.addSpan(new DotSpan(10f, color)));
    }
}
