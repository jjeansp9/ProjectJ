package kr.jeet.edu.student.view.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

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

//    @Override
//    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        int position = parent.getChildAdapterPosition(view);
//        int column = position % spanCount;
//
//        // 여백 초기화
//        outRect.top = 0;
//        outRect.bottom = 0;
//        outRect.left = 0;
//        outRect.right = 0;
//
//        // 첫 번째 열의 아이템
//        if (column == 0) {
//            outRect.left = space;
//            outRect.right = space / 2;
//        }
//        // 마지막 열의 아이템
//        else if (column == spanCount - 1) {
//            outRect.left = space / 2;
//            outRect.right = space;
//        }
//        // 중간 열의 아이템
//        else {
//            outRect.left = space / 2;
//            outRect.right = space / 2;
//        }
//
//        // 첫 번째 행의 아이템
//        if (position < spanCount) {
//            outRect.top = space;
//        }
//
//        // 마지막 행의 아이템
//        if (position >= Objects.requireNonNull(parent.getAdapter()).getItemCount() - spanCount) {
//            outRect.bottom = space;
//        }
//    }
}
