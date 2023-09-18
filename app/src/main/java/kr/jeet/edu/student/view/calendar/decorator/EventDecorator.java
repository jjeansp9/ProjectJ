package kr.jeet.edu.student.view.calendar.decorator;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.calendar.DayViewSpan;

public class EventDecorator implements DayViewDecorator {

    private int color;
    private HashSet<CalendarDay> dates;
    private Context context;

    public EventDecorator(Context mContext, HashSet<CalendarDay> dates) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.red);
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {return dates.contains(day);}

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null) {
            view.addSpan(new DotSpan(7f, color));

//            DayViewSpan span = new DayViewSpan("test", ContextCompat.getColor(this.context, R.color.blue), ContextCompat.getColor(this.context, R.color.white));
//            view.addSpan(span);
        }
    }

    public void setDates(Collection<CalendarDay> collection){
        this.dates = new HashSet<>(collection);
    }
}
