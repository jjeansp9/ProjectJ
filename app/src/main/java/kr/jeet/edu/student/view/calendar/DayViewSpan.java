package kr.jeet.edu.student.view.calendar;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class DayViewSpan implements LineBackgroundSpan {
    private String text;
    private int backgroundColor;
    private int textColor;

    public DayViewSpan(String text, int backgroundColor, int textColor) {
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom,
            CharSequence text, int start, int end, int lineNumber
    ) {
        // 텍스트 뒷 배경을 그리는 부분
        Paint.Style originalStyle = paint.getStyle();
        int originalColor = paint.getColor();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);

        // 백그라운드 위치를 조정하여 아래쪽으로 이동
        int adjustedTop = bottom - 20;
        canvas.drawRect(left, adjustedTop, right, bottom, paint);

        paint.setStyle(originalStyle);
        paint.setColor(originalColor);

        // 텍스트를 그리는 부분
        paint.setColor(textColor);

        // 텍스트 위치를 조정하여 가운데 정렬
        int x = (left + right) / 2;
        int y = bottom; // 텍스트 위치를 날짜의 하단으로 이동

        canvas.drawText(this.text, x, y, paint);
    }
}
