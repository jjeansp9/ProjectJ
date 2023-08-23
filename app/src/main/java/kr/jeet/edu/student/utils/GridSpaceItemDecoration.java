package kr.jeet.edu.student.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int space;

    public GridSpaceItemDecoration(final int spanCount, int space) {
        this.spanCount = spanCount;
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (position < spanCount) {
            outRect.top = space;
        }
        outRect.bottom = space;

        // 좌측 여백
        final int lastColumn = spanCount - 1;
        if (column == 0) {
            outRect.left = space;
            outRect.right = space / 2;
            // 우측 여백
        } else if (column == lastColumn) {
            outRect.left = space / 2;
            outRect.right = space;
            // 중간 여백
        } else {
            outRect.left = space / 2;
            outRect.right = space / 2;
        }
    }
}
