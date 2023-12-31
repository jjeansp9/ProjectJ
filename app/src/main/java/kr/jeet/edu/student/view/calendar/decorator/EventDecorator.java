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
import kr.jeet.edu.student.common.DataManager;

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
            view.addSpan(new DotSpan(DataManager.getInstance().DOT_SIZE, color));
        }
    }

    public void setDates(Collection<CalendarDay> collection){
        this.dates = new HashSet<>(collection);
    }
}
