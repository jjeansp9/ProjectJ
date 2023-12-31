package kr.jeet.edu.student.view.calendar.decorator;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

import kr.jeet.edu.student.R;

public class OtherMonthDecorator implements DayViewDecorator {

    private int color;
    private Context context;
    private CalendarDay selDay;

    public OtherMonthDecorator(Context mContext) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.font_color_other_month_cal);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day.getDate());

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);

        return day != null && day.getMonth() != selDay.getMonth() && weekDay != Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) view.addSpan(new ForegroundColorSpan(color));
    }
}
