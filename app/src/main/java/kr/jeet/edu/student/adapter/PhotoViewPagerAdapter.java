package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.PhotoViewActivity;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.DrawableAlwaysCrossFadeFactory;
import kr.jeet.edu.student.view.photoview.PhotoView;

public class PhotoViewPagerAdapter extends PagerAdapter {
    private String TAG = PhotoViewPagerAdapter.class.getSimpleName();

    public enum Action{Download};
    public interface onItemClickListener{ public void onActionBtnClick(FileData file, BoardDetailFileListAdapter.Action action); }

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private TextView mTvPage;

    public PhotoViewPagerAdapter(ArrayList<FileData> mImageList, TextView mTvPage) {
        this.mImageList = mImageList;
        this.mTvPage = mTvPage;
    }

    // pager의 전체 페이지 수 설정
    @Override
    public int getCount() {
        if (mImageList == null) return 0;
        return mImageList.size();
    }

    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());

        int paddingInPx = dpToPx(container.getContext(), 2);
        photoView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);


        if (mImageList != null && mImageList.size() > 0) { // 이미지가 있는 경우
            String imageUrl = RetrofitApi.FILE_SUFFIX_URL + mImageList.get(position).path + "/" + mImageList.get(position).saveName;

            Glide.with(container)
                    .load(imageUrl)
                    //.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.ic_vector_image_error) // 로딩 실패 시 이미지
//                    .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                    .into(photoView);
        }

        // Now just add PhotoView to ViewPager and return it
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // 사이즈는 MATCH_PARENT 크기로 add

        return photoView;
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) { // ViewPager에서 페이지를 제거할 때 호출되는 메소드
        // 뷰페이저에서 삭제
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
