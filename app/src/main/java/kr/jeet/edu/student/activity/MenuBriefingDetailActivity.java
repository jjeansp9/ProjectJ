package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.student.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.response.BoardDetailResponse;
import kr.jeet.edu.student.model.response.BriefingDetailResponse;
import kr.jeet.edu.student.model.response.BriefingResponse;
import kr.jeet.edu.student.receiver.DownloadReceiver;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingDetailActivity extends BaseActivity {

    private final static String TAG = "BriefingDetailActivity";

    private TextView mTvTitle, mTvDate, mTvTime, mTvLoc, mTvPersonnel, mTvContent, mTvCnt;
    private RecyclerView mRecyclerViewImages, mRecyclerViewFiles;
    private RelativeLayout layoutStartReserve;

    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;
    private DownloadReceiver _downloadReceiver = null;

    private RetrofitApi mRetrofitApi;
    private BriefingData mInfo;
    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();

    private final String RESERVE = "reserve";
    private final String RESERVE_LIST = "reserve_list";

    private int _currentSeq = -1; //PushMessage 용

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if(intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED)) {
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, false);

                if(added) requestBrfDetail(mInfo.seq);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_detail);
        mContext = this;
        initIntentData();
        initView();
        initAppbar();
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
        Log.e(TAG, "Event");
    }

    private void initIntentData(){
        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_BRIEFING_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO, BriefingData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO);
                }

            }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                PushMessage message = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
                }else{
                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                }
                _currentSeq = message.connSeq;
            }

        }
    }
    // GET http://192.168.2.77:7777/mobile/api/pt/-1
    // 400 http://192.168.2.77:7777/mobile/api/pt/-1 (217ms)

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.layout_start_reserve).setOnClickListener(this);
        findViewById(R.id.layout_reserver_list).setOnClickListener(this);

        layoutStartReserve = findViewById(R.id.layout_start_reserve);

        mTvTitle = findViewById(R.id.tv_brf_detail_title);
        mTvDate = findViewById(R.id.tv_brf_detail_date);
        //mTvTime = findViewById(R.id.tv_brf_detail_time);
        mTvLoc = findViewById(R.id.tv_brf_detail_loc);
        mTvPersonnel = findViewById(R.id.tv_brf_detail_personnel);
        mTvContent = findViewById(R.id.tv_brf_detail_content);
        mTvCnt = findViewById(R.id.tv_brf_cnt);

        mRecyclerViewImages = findViewById(R.id.recycler_brf_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_brf_file);

        setImageRecycler();
        setFileRecycler();

        initData();
    }

    private void initData() {
        if (mInfo != null) {

            if(mInfo.fileList != null && mInfo.fileList.size() > 0) {
                new Thread(() -> {
                    for(FileData data : mInfo.fileList) {
                        String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
                        LogMgr.w(data.saveName + " / " + mimeType);

                        // mimeType is checked for null here.
                        if (mimeType != null && mimeType.startsWith("image")) mImageList.add(data);
                        else mFileList.add(data);
                    }
                }).start();
            }
            runOnUiThread(() -> {
                setView();
                if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
                if(mFileAdapter != null && mFileList.size() > 0)mFileAdapter.notifyDataSetChanged();
            });
        }else if(_currentSeq != -1) {
            requestBrfDetail(_currentSeq);
        }
    }

    private void setView(){

        String str = TextUtils.isEmpty(mInfo.title) ? "" : mInfo.title;
        mTvTitle.setText(str);

        str = TextUtils.isEmpty(mInfo.date) || TextUtils.isEmpty(mInfo.ptTime) ? "" : Utils.formatDate(mInfo.date, mInfo.ptTime, true);
        mTvDate.setText(str);

        str = TextUtils.isEmpty(mInfo.place) ? "" : mInfo.place;
        mTvLoc.setText(str);

        str = mInfo.participantsCnt+"명";
        mTvPersonnel.setText(str);

        str = TextUtils.isEmpty(mInfo.content) ? "" : mInfo.content;
        mTvContent.setText(str);

        str = TextUtils.isEmpty(String.valueOf(mInfo.reservationCnt)) ? "" : "("+mInfo.reservationCnt+")";
        if (mInfo.reservationCnt < 1) {
            mTvCnt.setVisibility(View.GONE);
        }
        else {
            mTvCnt.setVisibility(View.VISIBLE);
            mTvCnt.setText(str);
        }

        if (mInfo.reservationCnt == mInfo.participantsCnt) layoutStartReserve.setVisibility(View.GONE);
        else layoutStartReserve.setVisibility(View.VISIBLE);
    }

    private void setImageRecycler(){
        mImageAdapter = new BoardDetailImageListAdapter(mContext, mImageList, this:: startPhotoViewActivity);
        mRecyclerViewImages.setAdapter(mImageAdapter);
    }

    private void startPhotoViewActivity(ArrayList<FileData> clickImg, int position){
        if (clickImg != null) {
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, mImageList);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG_POSITION, position);
            startActivity(intent);
        } else LogMgr.e("item is null");
    }

    private void setFileRecycler(){
        mFileAdapter = new BoardDetailFileListAdapter(mContext, mFileList, BoardDetailFileListAdapter.Action.Download, new BoardDetailFileListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(FileData file) {

            }

            @Override
            public void onActionBtnClick(FileData data, BoardDetailFileListAdapter.Action action) {
                if(action == BoardDetailFileListAdapter.Action.Download) {

                    String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                    url = FileUtils.replaceMultipleSlashes(url);
                    LogMgr.w("download file uri = " + url);
                    String fileName = data.orgName;
                    String destinationPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
                    FileUtils.downloadFile(mContext, url, fileName);

                    // BroadcastReceiver 등록
                    _downloadReceiver = new DownloadReceiver(() -> mContext.unregisterReceiver(_downloadReceiver));
                    mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }
            }
        });
        mRecyclerViewFiles.setAdapter(mFileAdapter);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_start_reserve:
                startActivity(MenuBriefingWriteActivity.class, RESERVE);
                break;

            case R.id.layout_reserver_list:
                startActivity(MenuBriefingReservedListActivity.class, RESERVE_LIST);
                break;
        }
    }

    private void requestBrfDetail(int ptSeq){
        if (RetrofitClient.getInstance() != null){
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBriefingDetail(ptSeq).enqueue(new Callback<BriefingDetailResponse>() {
                @Override
                public void onResponse(Call<BriefingDetailResponse> call, Response<BriefingDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            if (response.body() != null) {

                                BriefingData data = response.body().data;
                                if (data != null){
                                    mInfo = data;
                                    setView();
                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBrfDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BriefingDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startActivity(Class<?> cls, String type){
        Intent intent = new Intent(mContext, cls);
        intent.putExtra(IntentParams.PARAM_BOARD_SEQ, mInfo.seq);

        if (type.equals(RESERVE)) {
            resultLauncher.launch(intent);
        } else {
            intent.putExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT, mInfo.participantsCnt);
            intent.putExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT, mInfo.reservationCnt);
            startActivity(intent);
        }
    }
}