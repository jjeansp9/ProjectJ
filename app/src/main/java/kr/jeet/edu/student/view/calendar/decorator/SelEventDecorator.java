package kr.jeet.edu.student.view.calendar.decorator;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;

public class SelEventDecorator implements DayViewDecorator {

    private static final String TAG = "SelEventDecorator";

    private int color;
    private Context context;
    private CalendarDay selDay;

    public SelEventDecorator(Context mContext) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.white);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {return day != null && day.equals(selDay);}

    @Override
    public void decorate(DayViewFacade view) { if (view != null && context != null) {view.addSpan(new DotSpan(7f, color));} }
}
