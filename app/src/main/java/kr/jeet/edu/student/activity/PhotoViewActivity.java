package kr.jeet.edu.student.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.PhotoViewPagerAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.receiver.DownloadReceiver;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.CustomViewPager;

public class PhotoViewActivity extends BaseActivity {
    private String TAG = PhotoViewActivity.class.getSimpleName();

    private TextView tvPage;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private int position = 0;
    private CustomViewPager mPager;
    private PhotoViewPagerAdapter mAdapter;

    private DownloadReceiver _downloadReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        mContext = this;
        initData();
        initView();
    }

    private void initData(){
        try {
            Intent intent = getIntent();
            if (intent != null &&
                    intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG) &&
                    intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION)){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, FileData.class);
                }else{
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG);
                }

                position = intent.getIntExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION, position);

                for (FileData file : mImageList) LogMgr.e(TAG+"ImgTest", RetrofitApi.FILE_SUFFIX_URL + file.path + file.saveName + " position : " + position);
            }

        }catch (Exception e){ LogMgr.e(TAG + " Exception : ", e.getMessage()); }

    }

    @Override
    void initView() {
        tvPage = findViewById(R.id.tv_photoview_page);

        findViewById(R.id.img_close).setOnClickListener(this);
        findViewById(R.id.img_photoview_download).setOnClickListener(this);

        mPager = (CustomViewPager) findViewById(R.id.view_pager);

        if (mImageList != null && mImageList.size() > 0){

            tvPage.setText(position + 1 + " / " + mImageList.size());

            mAdapter = new PhotoViewPagerAdapter(mImageList, tvPage);
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(position);

            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int index) {
                    tvPage.setText(index + 1 + " / " + mImageList.size());
                    position = index;
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            });
        }
    }

    @Override
    void initAppbar() {}

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.img_close:
                finish();
                break;

            case R.id.img_photoview_download:
                downloadImage();
                break;
        }
    }

    private void downloadImage(){
        String url = RetrofitApi.FILE_SUFFIX_URL + mImageList.get(position).path + "/" + mImageList.get(position).saveName;
        url = FileUtils.replaceMultipleSlashes(url);

        LogMgr.w("download file uri = " + url);

        String fileName = mImageList.get(position).orgName;

        FileUtils.downloadFile(mContext, url, fileName);
        // BroadcastReceiver 등록
        _downloadReceiver = new DownloadReceiver(new DownloadReceiver.DownloadCompleteListener() {
            @Override
            public void onDownloadComplete() { mContext.unregisterReceiver(_downloadReceiver); }
        });
        mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        if(_downloadReceiver != null) {
            try {
                mContext.unregisterReceiver(_downloadReceiver);
            }catch(IllegalArgumentException e) {

            }
        }
        super.onDestroy();
    }
}












