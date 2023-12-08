package kr.jeet.edu.student.activity;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.PhotoViewPagerAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.receiver.DownloadReceiver;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomViewPager;

public class PhotoViewActivity extends BaseActivity {
    private String TAG = PhotoViewActivity.class.getSimpleName();

    private TextView tvPage;
    private RelativeLayout layoutHeader;

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
                    intent.hasExtra(IntentParams.PARAM_BOARD_POSITION)){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, FileData.class);
                }else{
                    mImageList = intent.getParcelableArrayListExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG);
                }

                position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, position);

                for (FileData file : mImageList) LogMgr.e(TAG+"ImgTest", RetrofitApi.FILE_SUFFIX_URL + file.path + file.saveName + " position : " + position);
            }

        }catch (Exception e){ LogMgr.e(TAG + " Exception : ", e.getMessage()); }

    }

    void initView() {
        tvPage = findViewById(R.id.tv_photoview_page);
        layoutHeader = findViewById(R.id.layout_header);

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

        if (Utils.isLandscapeMode(mContext)) { // 가로모드일때
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutHeader.getLayoutParams();
            params.leftMargin = Utils.fromDpToPx(30); // 양쪽 마진 설정. 네비게이션바 겹쳐져서 다운로드 아이콘 클릭 잘 안됨
            params.rightMargin = Utils.fromDpToPx(30);
            layoutHeader.setLayoutParams(params);

        }else { // 세로모드일때
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutHeader.getLayoutParams();
            params.topMargin = statusBarHeight(mContext); // 상단의 상태 바 size만큼 margin 값 주기
            layoutHeader.setLayoutParams(params);
        }

        setStatusBarTransparent();
        //setFullScreen(mActivity, true);
    }

    private void setStatusBarTransparent() {
        Window window = getWindow();

        window.setFlags( // 바, 상태표시줄의 위치에 제한을 두지않고 레이아웃 확장
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // api level 30 이상
            WindowCompat.setDecorFitsSystemWindows(window, false); // false로 설정하면 바, 상태표시줄 확장
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            }
        }else{
            window = getWindow();
            View decorView = window.getDecorView();

            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                flags += ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            decorView.setSystemUiVisibility(flags);
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //window.setNavigationBarColor(Color.BLACK);
    }

    public int statusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    public int navigationHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    public static void setFullScreen(Activity activity, boolean overLockscreen)
    {
        try{
            Window window = activity.getWindow();
            //			requestWindowFeature(Window.FEATURE_NO_TITLE);
            if(window != null)
            {
                if(overLockscreen)
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                else
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if(Build.VERSION.SDK_INT > 10) {
                    int uiFlag = window.getDecorView().getSystemUiVisibility();
                    boolean isImmersiveModeEnabled = ((uiFlag | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiFlag);
                    int newUiFlag = uiFlag;
                    if (Build.VERSION.SDK_INT >= 14) {
                        newUiFlag ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

                    }
                    if (Build.VERSION.SDK_INT >= 16) {
                        newUiFlag ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                        newUiFlag ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
                        newUiFlag ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                        newUiFlag ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                    }
                    if (Build.VERSION.SDK_INT >= 18) {
                        newUiFlag ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

                    }
                    window.getDecorView().setSystemUiVisibility(newUiFlag);
                    if (Build.VERSION.SDK_INT >= 28 /*P os*/)
                    {
//                        if(Utils.supportLayoutInCutoutMode) {
//                            try {
//                                WindowManager.LayoutParams lp = window.getAttributes();
//                                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//                                window.setAttributes(lp);
//                            }catch(Exception ex){
//                                ex.printStackTrace();
//                            }
//                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

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
        _downloadReceiver = new DownloadReceiver(new DownloadReceiver.DownloadListener() {
            @Override
            public void onDownloadComplete(int position, FileData data, Uri downloadFileUri) { mContext.unregisterReceiver(_downloadReceiver); }

            @Override
            public void onShow(FileData data) {

            }

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












