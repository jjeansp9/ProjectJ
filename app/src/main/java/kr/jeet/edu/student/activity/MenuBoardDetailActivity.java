package kr.jeet.edu.student.activity;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.student.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.data.SystemNoticeData;
import kr.jeet.edu.student.model.request.PushConfirmRequest;
import kr.jeet.edu.student.model.response.BoardDetailResponse;
import kr.jeet.edu.student.model.response.SystemNoticeResponse;
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

public class MenuBoardDetailActivity extends BaseActivity {

    private String TAG = MenuBoardDetailActivity.class.getSimpleName();

    private ImageView mImgRdCnt;
    private TextView mTvTitle, mTvName, mTvDate, mTvContent, mTvRdCnt;
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
    private int _currentSeq = -1;
    private String title = "";

    private String _stName = "";
    private int _stCode = 0;

    Parcelable result = null;
    private int dataType = -1;

    private final int TYPE_PUSH = 0;
    private final int TYPE_ANNOUNCEMENT = 1;
    private final int TYPE_ANNOUNCEMENT_FROM_MAIN = 2;
    private final int TYPE_SYSTEM = 3;
    private final int TYPE_REPORT_CARD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_board_detail);
        _downloadReceiver = new DownloadReceiver(new DownloadReceiver.DownloadListener() {
            @Override
            public void onDownloadComplete(int position, FileData fileData, Uri downloadFileUri) {
                hideProgressDialog();
                try {
                    mContext.unregisterReceiver(_downloadReceiver);
                }catch(IllegalArgumentException e){}
//                File destFile = new File(downloadPath);
//                Uri uri = Uri.fromFile(destFile);
                if(downloadFileUri == null) return;
                FileData tempFileData = FileUtils.copyBoardTempFile(mContext, downloadFileUri, fileData);
                LogMgr.e(TAG, "tempFileData = " + tempFileData.tempFileName);
                mFileAdapter.notifyItemChanged(position);
            }

            @Override
            public void onShow(FileData data) {
                showFile(data);
            }
        });
        mContext = this;
        initView();
        initAppbar();
    }

    private void initIntentData(){

        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);

        Intent intent = getIntent();
        if(intent != null){

            String extraKey = null;

            // 공지사항 목록 -> 공지사항 상세
            if (intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO) && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)) {
                LogMgr.w(TAG,"param is recived");
                extraKey = IntentParams.PARAM_ANNOUNCEMENT_INFO;
                title = getString(R.string.title_detail);
                dataType = TYPE_ANNOUNCEMENT;
                LogMgr.e(TAG,"Event heres3");

            // 푸쉬
            } else if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE) && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)) {
                extraKey = IntentParams.PARAM_PUSH_MESSAGE;
                title = getString(R.string.title_detail);
                dataType = TYPE_PUSH;
                LogMgr.e(TAG,"Event heres2");

            // 시스템알림 목록 -> 시스템알림 상세
            } else if (intent.hasExtra(IntentParams.PARAM_NOTICE_INFO) && intent.hasExtra(IntentParams.PARAM_DATA_TYPE) && intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) {
                extraKey = IntentParams.PARAM_NOTICE_INFO;
                title = getString(R.string.title_detail);
                _pushData = intent.getParcelableExtra(IntentParams.PARAM_NOTICE_INFO);
                _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, _currentSeq);
                dataType = intent.getIntExtra(IntentParams.PARAM_DATA_TYPE, dataType);
                if (_pushData.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
                LogMgr.e(TAG,"Event heres1");

            }
//            else if (intent.hasExtra(IntentParams.PARAM_BOARD_SEQ) && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)){
//                title = intent.getStringExtra(IntentParams.PARAM_APPBAR_TITLE);
//                _currentSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, _currentSeq);
//                dataType = TYPE_ANNOUNCEMENT_FROM_MAIN;
//                LogMgr.e(TAG, "Event here2");
//            }

            if (extraKey != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    if (extraKey.equals(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
                        result = intent.getParcelableExtra(extraKey, AnnouncementData.class);

                    } else if (extraKey.equals(IntentParams.PARAM_PUSH_MESSAGE)){
                        _pushData = intent.getParcelableExtra(extraKey, PushMessage.class);

                        if (_pushData.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
                        LogMgr.e(TAG,"Event here2");
                    } else{
                        LogMgr.e(TAG,"Event here3");
                    }

                } else {

                    if (extraKey.equals(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
                        result = intent.getParcelableExtra(extraKey);

                    } else if (extraKey.equals(IntentParams.PARAM_PUSH_MESSAGE)) {
                        _pushData = intent.getParcelableExtra(extraKey);
                        _currentSeq = _pushData.connSeq;

                        if (_pushData.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
                        LogMgr.e("Event here4", _pushData.connSeq+"");

                    } else {
                        LogMgr.e("Event here5");
                    }
                }
            }

            if (result instanceof AnnouncementData) {
                _currentData = (AnnouncementData) result;

            } else if (result instanceof PushMessage) {
                _pushData = (PushMessage) result;
                _currentSeq = ((PushMessage) result).connSeq;
            }
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        initIntentData();
        mTvTitle = findViewById(R.id.tv_board_detail_title);
        mTvName = findViewById(R.id.tv_board_detail_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        mTvContent = findViewById(R.id.tv_board_detail_content);
        mTvRdCnt = findViewById(R.id.tv_rd_cnt);
        mImgRdCnt = findViewById(R.id.img_rd_cnt);

        mRecyclerViewImages = findViewById(R.id.recycler_board_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_board_files);

        setImageRecycler();
        setFileRecycler();

        initData();
    }
    private void initData() {
        if (_currentData != null) {
            mImgRdCnt.setVisibility(View.VISIBLE);
            mTvRdCnt.setVisibility(View.VISIBLE);
            LogMgr.e("Event5");
            requestNoticeDetail(_currentData.seq);
            Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_NOTICE);

        }else if (dataType == TYPE_PUSH){ // 푸쉬를 통해 온 경우
            switch (_pushData.pushType) {
                case FCMManager.MSG_TYPE_NOTICE:  // 공지사항
                    LogMgr.e("Event1", _pushData.connSeq + "");
                    requestNoticeDetail(_pushData.connSeq);
                    Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_NOTICE);
                    break;

                case FCMManager.MSG_TYPE_SYSTEM:  // 시스템알림
                    LogMgr.e("Event2");
                    requestSystemDetail();
                    Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_SYSTEM);
                    break;

                case FCMManager.MSG_TYPE_REPORT_CARD:  // 성적표 push
                    // TODO : 푸쉬 -> 성적표 데이터 갱신
                    Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_REPORT_CARD);
                    break;
            }

        }else if (dataType == TYPE_SYSTEM){ // 목록 item 클릭해서 온 경우, dataType = 시스템알림
            LogMgr.e("Event3");
            requestSystemDetail();
            Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_SYSTEM);

        }else if (dataType == TYPE_REPORT_CARD){ // 성적표
            LogMgr.e("Event3-1");
            // TODO : 목록 -> 성적표 데이터 갱신
            Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_REPORT_CARD);

        } else if (dataType == TYPE_ANNOUNCEMENT_FROM_MAIN){ // 메인에서 공지사항
            LogMgr.e("Event4");
            requestNoticeDetail(_currentSeq);
            Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_NOTICE);
        }
    }

    private void setView(){
        LogMgr.w(TAG,"memberResponseVO = " + _currentData.memberResponseVO);
        mTvTitle.setText(_currentData.title); // 제목
        //mTvName.setVisibility(View.GONE);
        mTvName.setText(_currentData.memberResponseVO.name); // 작성자 이름
        mTvDate.setText(_currentData.insertDate); // 작성날짜
        mTvContent.setText(_currentData.content); // 내용
        mTvRdCnt.setText(Utils.getStr(Utils.decimalFormat(_currentData.rdcnt)));

        if(_currentData.fileList != null && _currentData.fileList.size() > 0) {

            for(FileData data : _currentData.fileList) {
                String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
                LogMgr.w(data.saveName + " / " + mimeType);

                // mimeType is checked for null here.
                if (mimeType != null && mimeType.startsWith("image")) mImageList.add(data);
                else {
                    data.initTempFileName();
                    mFileList.add(data);
                }

            }
        }
        if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
        if(mFileAdapter != null && mFileList.size() > 0) mFileAdapter.notifyDataSetChanged();
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
            public void onItemClick(int position, FileData data) {
                LogMgr.e(TAG, "data.tempFileName=" + data.tempFileName);
                String type = data.path.replaceAll("/", "");
                File file = new File(mContext.getExternalFilesDir(type).getPath() + "/" + data.tempFileName);
                if(file.exists()) {
                    showFile(data);
                }else{
                    _downloadReceiver.setRequireRun(true);
                    _downloadReceiver.setCurrentPosition(position);
                    downloadFile(data);
                }
            }

            @Override
            public void onActionBtnClick(int position, FileData data, BoardDetailFileListAdapter.Action action) {
                if(action == BoardDetailFileListAdapter.Action.Download) {
                    _downloadReceiver.setRequireRun(false);
                    _downloadReceiver.setCurrentPosition(position);
                    downloadFile(data);
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
                                    //initData();
                                    setView();

                                }else LogMgr.e(TAG+" requestNoticeDetail is null");
                            }
                        }else{
                            finishActivity();
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
                    finishActivity();
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
                                    mTvName.setVisibility(View.VISIBLE);
                                    mTvName.setText(TextUtils.isEmpty(_systemNoticeData.writerName) ? "" : _systemNoticeData.writerName); // 작성자 이름
                                    mTvDate.setText(TextUtils.isEmpty(_systemNoticeData.insertDate) ? "" : _systemNoticeData.insertDate); // 작성날짜
                                    mTvContent.setText(TextUtils.isEmpty(_systemNoticeData.content) ? "" : _systemNoticeData.content); // 내용

                                    if(_systemNoticeData.fileList != null && _systemNoticeData.fileList.size() > 0) {

                                        for(FileData file : _systemNoticeData.fileList) {
                                            String mimeType = FileUtils.getMimeTypeFromExtension(file.extension);
                                            LogMgr.w(file.saveName + " / " + mimeType);

                                            // mimeType is checked for null here.
                                            if (mimeType != null && mimeType.startsWith("image")) mImageList.add(file);
                                            else {
                                                file.initTempFileName();
                                                mFileList.add(file);
                                            }

                                        }
                                    }
                                    if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
                                    if(mFileAdapter != null && mFileList.size() > 0) mFileAdapter.notifyDataSetChanged();

                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            int responseCode = response.code();
                            if (responseCode == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                Toast.makeText(mContext, R.string.server_not_found_board, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            }
                            finishActivity();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestSystemDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<SystemNoticeResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestSystemDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    finishActivity();
                }
            });
        }
    }

    private void finishActivity() {
        new Handler().postDelayed(() -> {
            finish();
            overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
        }, 500);
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

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(IntentParams.PARAM_RD_CNT_ADD, true);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

    private void showFile(FileData data) {
        String url = data.tempFileName;
        LogMgr.w("view file uri = " + url);
        url = FileUtils.replaceMultipleSlashes(url);

        String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
        Intent intent = new Intent(Intent.ACTION_VIEW);
//                new ShareCompat.IntentBuilder(mContext)
//                        .setStream(Uri.parse(url))
//                        .setType(mimeType)
//                                .startChooser();
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = data.path.replaceAll("/", "");
        File file = new File(mContext.getExternalFilesDir(type).getPath() + "/" + data.tempFileName);
        if(file.exists()) {
            Uri uri = FileProvider.getUriForFile(mContext, getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
            if (intent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(mContext, R.string.msg_empty_open_with, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void downloadFile(FileData data) {
        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
        url = FileUtils.replaceMultipleSlashes(url);
        LogMgr.w("download file uri = " + url);
        String fileName = data.orgName;
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
//
        File downloadFile = new File(destinationPath);
        if(downloadFile.exists()) {
            final String finalUrl = url;
            showMessageDialog(getString(R.string.dialog_title_alarm)
                    , getString(R.string.board_item_confirm_download)
                    , new View.OnClickListener() {  //OKClickListener
                        @Override
                        public void onClick(View view) {
                            try {

                                downloadFile.setWritable(true);
                                downloadFile.delete();
                            }catch(Exception ex) {
                                ex.printStackTrace();
                            }

                            _downloadReceiver.setCurrentFileData(data);
//                            _downloadReceiver.setOriginalDownloadPath(destinationPath);
                            showProgressDialog();
                            FileUtils.downloadFile(mContext, finalUrl, fileName);
                            // BroadcastReceiver 등록

                            mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                            hideMessageDialog();
                        }
                    },
                    new View.OnClickListener() {    //cancelClickListener
                        @Override
                        public void onClick(View view) {
                            hideMessageDialog();
                        }
                    }, false);
        }else {
            _downloadReceiver.setCurrentFileData(data);
//            _downloadReceiver.setOriginalDownloadPath(destinationPath);
            showProgressDialog();
            FileUtils.downloadFile(mContext, url, fileName);
            // BroadcastReceiver 등록

            mContext.registerReceiver(_downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }
}