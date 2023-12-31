package kr.jeet.edu.student.activity.menu.notice;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.activity.PhotoViewActivity;
import kr.jeet.edu.student.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.student.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.data.SystemNoticeData;
import kr.jeet.edu.student.model.data.SystemNoticeListData;
import kr.jeet.edu.student.model.response.SystemNoticeResponse;
import kr.jeet.edu.student.receiver.DownloadReceiver;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.DBUtils;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuNoticeDetailActivity extends BaseActivity {

    private final String TAG = "MenuNoticeDetailActivity";

    private ImageView mImgRdCnt;
    private TextView mTvTitle, mTvName, mTvDate, mTvContent, mTvRdCnt;
    private RecyclerView mRecyclerViewImages, mRecyclerViewFiles;
    private BoardDetailImageListAdapter mImageAdapter;
    private BoardDetailFileListAdapter mFileAdapter;

    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<FileData> mFileList = new ArrayList<>();
    private DownloadReceiver _downloadReceiver = null;
    PushMessage _pushData = null;
    SystemNoticeListData _systemNoticeListData = null;
    SystemNoticeData _systemNoticeData = null;
    private int _currentSeq = -1;
    private String title = "";

    private int _memberSeq = -1;
    private String _stName = "";
    private int _stCode = 0;

    private int _currentDataPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_board_detail);
        mContext = this;
        initDownloadReceiver();
        initView();
        initAppbar();
    }

    private void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(getString(R.string.title_detail));
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(_pushData == null);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);

        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_BOARD_POSITION)) {
            _currentDataPosition = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
        }

        if(intent.hasExtra(IntentParams.PARAM_NOTICE_INFO)) {
            LogMgr.w("param is recived");
            _systemNoticeListData = Utils.getParcelableExtra(intent, IntentParams.PARAM_NOTICE_INFO, SystemNoticeListData.class);
            if (_systemNoticeListData != null) {

                _pushData = new PushMessage();
                _pushData.pushId = _systemNoticeListData.pushId;
                _pushData.pushType = FCMManager.MSG_TYPE_SYSTEM;
                _pushData.connSeq = _systemNoticeListData.connSeq;
                _pushData.stCode = _stCode;

                new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
            }
        }

        Bundle bundle = intent.getExtras();
        if (bundle != null) _pushData = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);

        if (_pushData != null) {
            _currentSeq = _pushData.connSeq;
            if (_pushData.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
        }
    }

    private void initView() {
        initData();
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

        if (_systemNoticeListData != null) { // 목록 -> 상세
            requestSystemDetail(_systemNoticeListData.connSeq);   //조회수 때문에 detail 을 호출해야만 함...
        }else if(_currentSeq != -1) { // 푸쉬 -> 상세
            requestSystemDetail(_currentSeq);
        }
    }

    private void setImageRecycler(){
        mImageAdapter = new BoardDetailImageListAdapter(mContext, mImageList, this:: startPhotoViewActivity);
        mRecyclerViewImages.setAdapter(mImageAdapter);
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

    private void startPhotoViewActivity(ArrayList<FileData> clickImg, int position){
        if (clickImg != null) {
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, mImageList);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
            startActivity(intent);
        } else LogMgr.e("item is null");
    }

    private void setView(){
//        mTvTitle.setText(_currentData.title); // 제목
//        mTvName.setText(_currentData.memberResponseVO.name); // 작성자 이름
//        mTvDate.setText(_currentData.insertDate); // 작성날짜
//        mTvContent.setText(_currentData.content); // 내용
//        mTvRdCnt.setText(Utils.getStr(Utils.decimalFormat(_currentData.rdcnt)));
//
//        mImgRdCnt.setVisibility(View.VISIBLE);
//        mTvRdCnt.setVisibility(View.VISIBLE);
//
//        if(_currentData.fileList != null && _currentData.fileList.size() > 0) {
//
//            for(FileData data : _currentData.fileList) {
//                String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
//                LogMgr.w(data.saveName + " / " + mimeType);
//
//                // mimeType is checked for null here.
//                if (mimeType != null && mimeType.startsWith("image")) mImageList.add(data);
//                else {
//                    data.initTempFileName();
//                    mFileList.add(data);
//                }
//
//            }
//        }
//        if(mImageAdapter != null && mImageList.size() > 0) mImageAdapter.notifyDataSetChanged();
//        if(mFileAdapter != null && mFileList.size() > 0) mFileAdapter.notifyDataSetChanged();
    }

    void changeMessageState2Read() {
        new Thread(() -> {
            try {
                List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_SYSTEM);
                if(!pushMessages.isEmpty()) {
                    for(PushMessage message : pushMessages) {
                        if (message.stCode == _stCode){
                            message.isRead = true;
                            JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().update(message);
                        }
                    }
                }
            }catch (Exception e){

            }
        }).start();
    }

    // 시스템알림 상세정보 조회
    private void requestSystemDetail(int boardSeq){
        if (RetrofitClient.getInstance() != null){

            showProgressDialog();
            if (RetrofitClient.getApiInterface() != null) {
                RetrofitClient.getApiInterface().getSystemNoticeDetail(boardSeq).enqueue(new Callback<SystemNoticeResponse>() {
                    @Override
                    public void onResponse(Call<SystemNoticeResponse> call, Response<SystemNoticeResponse> response) {
                        try {
                            if (response.isSuccessful()){

                                if (response.body() != null) {

                                    SystemNoticeData data = response.body().data;
                                    if (data != null){
                                        _systemNoticeData = data;
                                        _systemNoticeData.isRead = true;
                                        mTvTitle.setText(TextUtils.isEmpty(_systemNoticeData.title) ? "" : _systemNoticeData.title); // 제목
                                        mTvName.setVisibility(View.VISIBLE);
                                        mTvName.setText(TextUtils.isEmpty(_systemNoticeData.writerName) ? "" : _systemNoticeData.writerName); // 작성자 이름
                                        mTvDate.setText(Utils.formatDate(
                                                _systemNoticeData.insertDate,
                                                Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss,
                                                Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm
                                        )); // 작성날짜
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

                                        //DBUtils.insertReadDB(mContext, _systemNoticeData, _memberSeq, FCMManager.MSG_TYPE_SYSTEM);
                                        changeMessageState2Read();

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
    }

    private void initDownloadReceiver() {
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
        intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _systemNoticeListData);
        intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

}