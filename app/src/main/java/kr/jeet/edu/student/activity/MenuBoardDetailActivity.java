package kr.jeet.edu.student.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.student.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.data.SystemNoticeData;
import kr.jeet.edu.student.model.response.BoardDetailResponse;
import kr.jeet.edu.student.model.response.SystemNoticeResponse;
import kr.jeet.edu.student.receiver.DownloadReceiver;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBoardDetailActivity extends BaseActivity {

    private String TAG = MenuBoardDetailActivity.class.getSimpleName();

    private TextView mTvTitle, mTvName, mTvDate, mTvContent;
    private RecyclerView mRecyclerViewImages, mRecyclerViewFiles;
    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;
    private RetrofitApi mRetrofitApi;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();
    private DownloadReceiver _downloadReceiver = null;
    AnnouncementData _currentData = null;
    PushMessage _pushData = null;
    SystemNoticeData _systemNoticeData = null;
    private int _currentSeq = -1;   //PushMessage 용
    private String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_board_detail);
        mContext = this;
        initIntentData();
        initView();
        initAppbar();
    }

    Parcelable result = null;
    private int dataType = -1;

    private final int TYPE_PUSH = 0;
    private final int TYPE_ANNOUNCEMENT = 1;
    private final int TYPE_SYSTEM = 2;

    private void initIntentData(){
        Intent intent = getIntent();
        if(intent != null){

//            if(intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
//                LogMgr.w("param is recived");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, AnnouncementData.class);
//                }else{
//                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO);
//                }
//
//            }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
//                PushMessage message = null;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
//                }else{
//                    message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
//                }
//                _currentSeq = message.connSeq;
//
//            }else if (intent.hasExtra(IntentParams.PARAM_NOTICE_INFO)){
//                PushMessage message = null;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    message = intent.getParcelableExtra(IntentParams.PARAM_NOTICE_INFO, PushMessage.class);
//                }else{
//                    message = intent.getParcelableExtra(IntentParams.PARAM_NOTICE_INFO);
//                }
//                _currentSeq = message.connSeq;
//            }


            String extraKey = null;

            if (intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
                LogMgr.w("param is recived");
                extraKey = IntentParams.PARAM_ANNOUNCEMENT_INFO;
                dataType = TYPE_ANNOUNCEMENT;

            } else if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                extraKey = IntentParams.PARAM_PUSH_MESSAGE;
                dataType = TYPE_PUSH;

            } else if (intent.hasExtra(IntentParams.PARAM_NOTICE_INFO) && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE) && intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) {
                extraKey = IntentParams.PARAM_NOTICE_INFO;
                title = intent.getStringExtra(IntentParams.PARAM_APPBAR_TITLE);
                _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, _currentSeq);
                dataType = TYPE_SYSTEM;
                LogMgr.e("Event here1");
            }

            if (extraKey != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    if (extraKey.equals(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
                        result = intent.getParcelableExtra(extraKey, AnnouncementData.class);

                    } else if (extraKey.equals(IntentParams.PARAM_PUSH_MESSAGE)){
                        result = intent.getParcelableExtra(extraKey, PushMessage.class);
                    } else{
                        LogMgr.e("Event here2");
                    }

                } else {

                    if (extraKey.equals(IntentParams.PARAM_ANNOUNCEMENT_INFO) || extraKey.equals(IntentParams.PARAM_PUSH_MESSAGE)) {
                        result = intent.getParcelableExtra(extraKey);

                    }else{
                        LogMgr.e("Event here2");
                    }
                }
            }

            if (result instanceof AnnouncementData) {
                _currentData = (AnnouncementData) result;

            } else if (result instanceof PushMessage) {
                _pushData = (PushMessage) result;
                //_currentSeq = ((PushMessage) result).connSeq;
            }else{

            }
        }
        LogMgr.w("currentData = " + _currentData);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        mTvTitle = findViewById(R.id.tv_board_detail_title);
        mTvName = findViewById(R.id.tv_board_detail_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        mTvContent = findViewById(R.id.tv_board_detail_content);

        mRecyclerViewImages = findViewById(R.id.recycler_board_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_board_files);

        setImageRecycler();
        setFileRecycler();

        initData();
    }
    private void initData() {
        if (_currentData != null) {
            LogMgr.w(TAG,"memberResponseVO = " + _currentData.memberResponseVO);
            mTvTitle.setText(_currentData.title); // 제목
            mTvName.setText(_currentData.memberResponseVO.name); // 작성자 이름
            mTvDate.setText(_currentData.insertDate); // 작성날짜
            mTvContent.setText(_currentData.content); // 내용

            if(_currentData.fileList != null && _currentData.fileList.size() > 0) {

                for(FileData data : _currentData.fileList) {
                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
                    LogMgr.w(data.saveName + " / " + mimeType);

                    // mimeType is checked for null here.
                    if (mimeType != null && mimeType.startsWith("image")) mImageList.add(data);
                    else mFileList.add(data);

                }
            }
            if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
            if(mFileAdapter != null && mFileList.size() > 0) mFileAdapter.notifyDataSetChanged();

        }else if (dataType == TYPE_PUSH){
            if (_pushData.pushType.equals(FCMManager.MSG_TYPE_NOTICE)) requestNoticeDetail(_pushData.connSeq);
            else if (_pushData.pushType.equals(FCMManager.MSG_TYPE_SYSTEM)) requestSystemDetail();

        }else if (dataType == TYPE_SYSTEM){
            requestSystemDetail();
        }
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

    // 공지사항 글 상세정보 조회
    private void requestNoticeDetail(int boardSeq){
        if (RetrofitClient.getInstance() != null){

            showProgressDialog();

            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBoardDetail(boardSeq).enqueue(new Callback<BoardDetailResponse>() {
                @Override
                public void onResponse(Call<BoardDetailResponse> call, Response<BoardDetailResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            if (response.body() != null) {

                                AnnouncementData data = response.body().data;
                                if (data != null){
                                    _currentData = data;
                                    initData();

                                }else LogMgr.e(TAG+" requestNoticeDetail is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestNoticeDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BoardDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestNoticeDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 시스템알림 상세정보 조회
    private void requestSystemDetail(){
        if (RetrofitClient.getInstance() != null){

            showProgressDialog();

            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getSystemNoticeDetail(_currentSeq).enqueue(new Callback<SystemNoticeResponse>() {
                @Override
                public void onResponse(Call<SystemNoticeResponse> call, Response<SystemNoticeResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            if (response.body() != null) {

                                SystemNoticeData data = response.body().data;
                                if (data != null){
                                    _systemNoticeData = data;
                                    mTvTitle.setText(TextUtils.isEmpty(_systemNoticeData.title) ? "" : _systemNoticeData.title); // 제목
                                    //mTvName.setText(_systemNoticeData.); // 작성자 이름
                                    mTvDate.setText(TextUtils.isEmpty(_systemNoticeData.insertDate) ? "" : _systemNoticeData.insertDate); // 작성날짜
                                    mTvContent.setText(TextUtils.isEmpty(_systemNoticeData.content) ? "" : _systemNoticeData.content); // 내용

                                    if(_systemNoticeData.fileList != null && _systemNoticeData.fileList.size() > 0) {

                                        for(FileData file : _systemNoticeData.fileList) {
                                            String mimeType = FileUtils.getMimeTypeFromExtension(file.extension);
                                            LogMgr.w(file.saveName + " / " + mimeType);

                                            // mimeType is checked for null here.
                                            if (mimeType != null && mimeType.startsWith("image")) mImageList.add(file);
                                            else mFileList.add(file);

                                        }
                                    }
                                    if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
                                    if(mFileAdapter != null && mFileList.size() > 0) mFileAdapter.notifyDataSetChanged();

                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestNoticeDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<SystemNoticeResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestNoticeDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
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