package kr.jeet.edu.student.activity.menu.briefing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.activity.PhotoViewActivity;
import kr.jeet.edu.student.adapter.BoardDetailFileListAdapter;
import kr.jeet.edu.student.adapter.BoardDetailImageListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.NewBoardDao;
import kr.jeet.edu.student.db.NewBoardData;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.BriefingReservedListData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.BriefingDetailResponse;
import kr.jeet.edu.student.model.response.BriefingReservedListResponse;
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

public class MenuBriefingDetailActivity extends BaseActivity {

    private final static String TAG = "BriefingDetailActivity";

    private ImageView mImgRdCnt, mImgReserve;
    private TextView mTvTitle, mTvDate, mTvTime, mTvLoc, mTvPersonnel, mTvContent, mTvCnt, mTvRdCnt, mTvReserve;
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
    private int _memberSeq = -1;
    private int _userGubun = -1;
    private int _stCode = 0;
    private int _reservationSeq = -1;

    boolean added = false;
    boolean canceled = false;

    PushMessage _pushData = null;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if(intent != null && intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED)) {
                added = intent.getBooleanExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, false);

                if(added) {
                    canceled = true;
                    requestBrfDetail(mInfo.seq);
                    Utils.createNotification(mContext, "예약완료", getString(R.string.briefing_write_success));
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_detail);
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
        Utils.changeMessageState2Read(getApplicationContext(), FCMManager.MSG_TYPE_PT);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (added) {
            intent.putExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, added);
        }else{
            intent.putExtra(IntentParams.PARAM_RD_CNT_ADD, true);
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, mInfo);
        }
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

    private void initIntentData(){

        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);

        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_BRIEFING_INFO)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO, BriefingData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BRIEFING_INFO);
                }

            }
            Bundle bundle = intent.getExtras();
            if (bundle != null) _pushData = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);

            if (_pushData != null) {
                _currentSeq = _pushData.connSeq;
                new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
            }
        }
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(_pushData == null);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        initIntentData();
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
        mTvRdCnt = findViewById(R.id.tv_rd_cnt);
        mTvReserve = findViewById(R.id.tv_brf_reserve);

        mImgRdCnt = findViewById(R.id.img_rd_cnt);
        mImgReserve = findViewById(R.id.img_brf_reserve);

        mRecyclerViewImages = findViewById(R.id.recycler_brf_img);
        mRecyclerViewFiles = findViewById(R.id.recycler_brf_file);

        setImageRecycler();
        setFileRecycler();

        initData();
    }

    private void initData() {
        mTvReserve.setText(getString(R.string.briefing_reserve_loading));
        layoutStartReserve.setBackgroundResource(R.drawable.bg_pref_sel_disabled);
        layoutStartReserve.setStateListAnimator(null);

        if (mInfo != null) requestBrfDetail(mInfo.seq);
        else if(_currentSeq != -1) requestBrfDetail(_currentSeq);
    }

    private void setView(){

        String str = "";
        mTvTitle.setText(Utils.getStr(mInfo.title));

        str = TextUtils.isEmpty(mInfo.date) || TextUtils.isEmpty(mInfo.ptTime) ? "" : Utils.formatDate(mInfo.date, mInfo.ptTime, true);
        mTvDate.setText(str);

        mTvLoc.setText(Utils.getStr(mInfo.place));

        str = mInfo.participantsCnt+"명";
        mTvPersonnel.setText(str);

        mTvContent.setText(Utils.getStr(mInfo.content));

        str = TextUtils.isEmpty(String.valueOf(mInfo.reservationCnt)) ? "" : "("+mInfo.reservationCnt+")";
        if (mInfo.reservationCnt < 1) {
            mTvCnt.setVisibility(View.GONE);

        } else {
            mTvCnt.setVisibility(View.VISIBLE);
            mTvCnt.setText(str);
        }

        mTvRdCnt.setText(Utils.getStr(Utils.decimalFormat(mInfo.rdcnt)));

        if (mImageList.size() > 0) mImageList.clear();
        if (mFileList.size() > 0) mFileList.clear();

        if(mInfo.fileList != null && mInfo.fileList.size() > 0) {

            for(FileData data : mInfo.fileList) {
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

        try {
            String dateStr = mInfo.date+mInfo.ptTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.KOREA);

            Date date = sdf.parse(dateStr);
            Calendar calBrf = Calendar.getInstance();

            if (date != null) calBrf.setTime(date);
            Calendar calCurrent = Calendar.getInstance();

            if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) { // 종료
                setTvInBtn(mTvReserve, getString(R.string.briefing_write_close), mTvCnt, View.GONE, mImgReserve, View.GONE);

            }else {
                if (canceled) {
                    if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) { // 종료
                        setTvInBtn(mTvReserve, getString(R.string.briefing_write_close), mTvCnt, View.GONE, mImgReserve, View.GONE);

                    }else { // 취소
                        mTvReserve.setText(getString(R.string.briefing_reserve_cancel));
                        mTvCnt.setVisibility(View.GONE);
                        mImgReserve.setVisibility(View.VISIBLE);
                        layoutStartReserve.setBackgroundResource(R.drawable.bg_pref_sel_disabled);
                        StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(mContext, R.xml.animate_button_push);
                        layoutStartReserve.setStateListAnimator(stateListAnimator);
                    }
                } else {
                    if (mInfo.reservationCnt >= mInfo.participantsCnt) { // 마감
                        setTvInBtn(mTvReserve, getString(R.string.briefing_write_finish), mTvCnt, View.GONE, mImgReserve, View.GONE);
                        LogMgr.e(TAG, "EVENT");

                    }else { // 예약하기
                        mTvReserve.setText(getString(R.string.briefing_reserve));
                        mImgReserve.setVisibility(View.VISIBLE);
                        layoutStartReserve.setBackgroundResource(R.drawable.bg_pref_sel_checked);
                        StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(mContext, R.xml.animate_button_push);
                        layoutStartReserve.setStateListAnimator(stateListAnimator);
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setTvInBtn(TextView tv, String str, TextView tvCnt, int visibilityCnt, ImageView img, int visibilityImg){
        tv.setText(str);
        tvCnt.setVisibility(visibilityCnt);
        img.setVisibility(visibilityImg);
        layoutStartReserve.setBackgroundResource(R.drawable.bg_pref_sel_disabled);
        layoutStartReserve.setStateListAnimator(null);
    }

    private void setImageRecycler(){
        mImageAdapter = new BoardDetailImageListAdapter(mContext, mImageList, this:: startPhotoViewActivity);
        mRecyclerViewImages.setAdapter(mImageAdapter);
    }

    private void startPhotoViewActivity(ArrayList<FileData> clickImg, int position){
        if (clickImg != null) {
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_DETAIL_IMG, mImageList);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
            startActivity(intent);
        } else LogMgr.e("item is null");
    }

    private void setFileRecycler(){
        mFileAdapter = new BoardDetailFileListAdapter(mContext, mFileList, BoardDetailFileListAdapter.Action.Download, new BoardDetailFileListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position, FileData data) {
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_start_reserve:

                try {
                    String dateStr = mInfo.date+mInfo.ptTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.KOREA);

                    Date date = sdf.parse(dateStr);
                    Calendar calBrf = Calendar.getInstance();

                    if (date != null) calBrf.setTime(date);
                    Calendar calCurrent = Calendar.getInstance();

                    if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) { // 종료

                    }else{
                        if (canceled){ // 취소
                            showMessageDialog(
                                    getString(R.string.dialog_title_alarm),
                                    getString(R.string.briefing_write_confirm),
                                    ok -> {
                                        hideMessageDialog();
                                        canceled = false;
                                        requestBrfCancel();
                                    },
                                    cancel -> hideMessageDialog(),
                                    false
                            );

                        } else if (mInfo.reservationCnt >= mInfo.participantsCnt) { // 마감

                        } else {
                            startActivity(MenuBriefingWriteActivity.class, RESERVE);
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
                                    mInfo.isRead = true;
                                    DBUtils.insertReadDB(mInfo, mContext, _memberSeq, FCMManager.MSG_TYPE_PT);

                                }else LogMgr.e(TAG+" DetailData is null");
                            }
                        }else{
                            finish();
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBrfDetail() Exception : ", e.getMessage());
                    }

                    requestBrfReservedListData(ptSeq);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BriefingDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    finish();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestBrfReservedListData(int ptSeq){
        showProgressDialog();
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBrfReservedList(ptSeq, _memberSeq).enqueue(new Callback<BriefingReservedListResponse>() {
                @Override
                public void onResponse(Call<BriefingReservedListResponse> call, Response<BriefingReservedListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<BriefingReservedListData> getData;

                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    LogMgr.i(TAG, "reserveData: " + getData.size());
                                    canceled = true;
                                    _reservationSeq = getData.get(0).seq;

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                    canceled = false;
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfReservedListData() Exception: ", e.getMessage());
                    }
                    setView();
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BriefingReservedListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfReservedListData() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    setView();
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 예약 취소
    private void requestBrfCancel(){
        if (RetrofitClient.getInstance() != null){
            showProgressDialog();
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.requestBrfCancel(_reservationSeq, _memberSeq, _userGubun).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            Toast.makeText(mContext, R.string.briefing_write_cancel, Toast.LENGTH_SHORT).show();
                            Utils.createNotification(mContext, "취소완료", getString(R.string.briefing_write_cancel));
                            initData();

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBrfDetail() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBrfDetail() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    finish();
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
    private void showFile(FileData data) {
        String url = data.tempFileName;
        url = FileUtils.replaceMultipleSlashes(url);
        LogMgr.w("view file uri = " + url);
        String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);
        Intent intent = new Intent(Intent.ACTION_VIEW);
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