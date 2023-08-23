package kr.jeet.edu.student.view.calendar.formatter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.view.calendar.CenteredImageSpan;

public class CustomTitleFormatter implements TitleFormatter {

    private Context context;

    public CustomTitleFormatter(Context mContext) {
        this.context = mContext;
    }

    @Override
    public CharSequence format(CalendarDay day) {
        int year = day.getYear();
        int month = day.getMonth() + 1;
        String formattedTitle = year + "년 " + month + "월 ";

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.bt_calendar_nor);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }

        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        spannableBuilder.append(formattedTitle);

        CenteredImageSpan imageSpan = new CenteredImageSpan(drawable);

        spannableBuilder.append(" ");
        spannableBuilder.setSpan(imageSpan, spannableBuilder.length() - 1, spannableBuilder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        // int textSize = (int) context.getResources().getDimension(R.dimen.font_size_large);
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(18, true);
        spannableBuilder.setSpan(sizeSpan, 0, formattedTitle.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        int titleColor = ContextCompat.getColor(context, R.color.font_color_default);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(titleColor);
        spannableBuilder.setSpan(colorSpan, 0, formattedTitle.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableBuilder;
    }
}
