package kr.jeet.edu.student.utils;


//import android.graphics.Rect;
//import android.view.View;
//import androidx.recyclerview.widget.RecyclerView;
//
//public class GridItemDecoration extends RecyclerView.ItemDecoration {
//    private final int spanCount;
//    private final int space;
//
//    public GridItemDecoration(int spanCount, int space) {
//        this.spanCount = spanCount;
//        this.space = space;
//    }
//
//    @Override
//    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        int position = parent.getChildAdapterPosition(view);
//        int column = position % spanCount; // 0부터 시작
//
//        if (position < spanCount) {
//            outRect.top = space;
//        }
//        outRect.bottom = space;
//
//        if (column == 0) { // 좌측 여백
//            outRect.left = space;
//            outRect.right = space / 2;
//
//        } else if (column == spanCount - 1) { // 우측 여백
//            outRect.left = space / 2;
//            outRect.right = space;
//        } else { // 중간 여백
//            outRect.left = space / 2;
//            outRect.right = space / 2;
//        }
//    }
//}

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

public class GridItemDecoration extends ItemDecoration {
    private final int spanCount; // Grid의 column 수
    private final int spacing; // 간격

    public GridItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state
    ) {
        int position = parent.getChildAdapterPosition(view);

        if (position >= 0) {
            int column = position % spanCount; // item column
            outRect.set(0, 0, 0, 0);
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = spacing - column * spacing / spanCount;
            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount;
            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
