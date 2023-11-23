package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.NoticeListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.SystemNoticeListData;
import kr.jeet.edu.student.model.response.SystemNoticeListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuNoticeActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener {

    private static final String TAG = "NoticeActivity";

    private PowerSpinnerView _spinnerType;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private NoticeListAdapter mAdapter;
    private TextView txtEmpty, mTvCalendar;
    private AppCompatImageButton btnPrevious, btnNext;

    private final ArrayList<SystemNoticeListData> mList = new ArrayList<>();
    //private final ArrayList<ReportCardSummaryData> mReportList = new ArrayList<>();

    Date _selectedDate = new Date();
    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR, Locale.KOREA);
    SimpleDateFormat _yearFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY, Locale.KOREA);
    SimpleDateFormat _monthFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_MM, Locale.KOREA);

    private String[] noticeType;
    private String allType = "";
    private String systemType = "";
    private String attendanceType = "";
    private String reportCardType = "";
    private String tuitionType = "";

    private String selYear = "";
    private String selMonth = "";

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private int _stCode = 0;
    private String _stuName = "";
    private int _userGubun = 0;
    private String _acaCode = "";

    //private boolean fromBottomMenu = false;
    private String getType = "";
    private String selType = "";

    private final int TYPE_SYSTEM = 3;
    private final int TYPE_REPORT_CARD = 4;
    private final int TYPE_TUITION = 5;

    private final int CMD_GET_LIST = 0;       // roomDB에 저장된 목록 가져오기

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_LIST:
                    getListData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_notice);
        mContext = this;
        initAppbar();
        initView();

        if (getType.equals(FCMManager.MSG_TYPE_ATTEND)) changeMessageState2Read();
        setAnimMove(Constants.MOVE_DOWN);
    }

    void changeMessageState2Read() {
        new Thread(() -> {
            try {
                List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, FCMManager.MSG_TYPE_ATTEND);
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

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_notice);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _stuName = PreferenceUtil.getStName(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);

        noticeType = getResources().getStringArray(R.array.notice_type);
        //allType = noticeType[0];
        systemType = noticeType[0];
        attendanceType = noticeType[1];
        //reportCardType = noticeType[2];
        tuitionType = noticeType[2];

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(IntentParams.PARAM_TYPE_NOTICE)){
            getType = intent.getStringExtra(IntentParams.PARAM_TYPE_NOTICE);

            if (getType != null) {
                if (getType.equals(FCMManager.MSG_TYPE_ATTEND)) selType = attendanceType;
                //else if (getType.equals(FCMManager.MSG_TYPE_REPORT)) selType = reportCardType;
                else if (getType.equals(FCMManager.MSG_TYPE_TUITION)) selType = tuitionType;
                else selType = systemType;
            }
        }
    }

    @Override
    void initView() {
        initData();

        mTvCalendar = findViewById(R.id.tv_notice_calendar);
        btnNext = findViewById(R.id.btn_notice_next);
        btnPrevious = findViewById(R.id.btn_notice_previous);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_notice);
        _spinnerType = findViewById(R.id.spinner_notice_type);

        mTvCalendar.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        mTvCalendar.setText(_dateFormat.format(_selectedDate));
        selYear= _yearFormat.format(_selectedDate);
        selMonth= _monthFormat.format(_selectedDate);

        setSpinner();
        setRecycler();
        mSwipeRefresh.setOnRefreshListener(this::getListData);

        mHandler.sendEmptyMessage(CMD_GET_LIST);
    }

    private void calendarSetVisible(int visible) {
        mTvCalendar.setVisibility(visible);
        btnNext.setVisibility(visible);
        btnPrevious.setVisibility(visible);
    }

    private void setSpinner(){
        _spinnerType.setIsFocusable(true);

        _spinnerType.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            selType = newItem.toString();

//            if (selType.equals(reportCardType)) {
//                calendarSetVisible(View.GONE);
//                getReportListData();
//            }
//            else {
//                calendarSetVisible(View.VISIBLE);
//                getListData();
//            }
            getListData();
        });

        _spinnerType.setSpinnerOutsideTouchListener((view, motionEvent) -> _spinnerType.dismiss());
        switch (getType) {
            case FCMManager.MSG_TYPE_SYSTEM:

                break;
            case FCMManager.MSG_TYPE_ATTEND:
                _spinnerType.selectItemByIndex(1);
                break;
//            case FCMManager.MSG_TYPE_REPORT:
//                _spinnerType.selectItemByIndex(2);
//                break;
            case FCMManager.MSG_TYPE_TUITION:
                _spinnerType.selectItemByIndex(2);
                break;
            default:
                _spinnerType.selectItemByIndex(0);
                break;
        }
    }

    private void setRecycler(){
        mAdapter = new NoticeListAdapter(mContext, mList, this::startActivity);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext,LinearLayoutManager.VERTICAL);
        Drawable dividerColor = new ColorDrawable(ContextCompat.getColor(this, R.color.line_2));

        dividerItemDecoration.setDrawable(dividerColor);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).seq;

//                    if (selType.equals(reportCardType)) getReportListData(lastNoticeSeq);
//                    else getListData(lastNoticeSeq);
                    getListData(lastNoticeSeq);
                }
            }
        });
    }

    private void startActivity(SystemNoticeListData item){
        if (item != null){
            Intent intent;
            switch (item.searchType) {
                case FCMManager.MSG_TYPE_SYSTEM:  // 시스템알림
                    startBoardDetailActivity(item, TYPE_SYSTEM);
                    break;

                case FCMManager.MSG_TYPE_REPORT:  // 성적표
                    // TODO: 성적표 리스트로 이동해야함
//                    intent = new Intent(mContext, ReportDetailActivity.class);
//                    intent.putExtra(IntentParams.PARAM_LIST_ITEM, item);
//                    startActivity(intent);
                    break;

                case FCMManager.MSG_TYPE_TUITION:  // 미납
                    intent = new Intent(mContext, TuitionActivity.class);
                    startActivity(intent);
                    break;
            }
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("item is null ");
    }

    private void getListData(int... lastSeq){

        String putType = "";
        String date = selYear + selMonth;

        if (selType.equals(systemType)) putType = FCMManager.MSG_TYPE_SYSTEM;
        else if (selType.equals(attendanceType)) putType = FCMManager.MSG_TYPE_ATTEND;
        //else if (selType.equals(reportCardType)) putType = FCMManager.MSG_TYPE_REPORT;
        else if (selType.equals(tuitionType)) putType = FCMManager.MSG_TYPE_TUITION;

        int lastNoticeSeq = 0;
        if (lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];

        if (RetrofitClient.getInstance() != null) {
            final int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getSystemNoticeList(
                    putType,
                    date,
                    0,
                    _stCode,
                    _memberSeq,
                    _userGubun,
                    lastNoticeSeq,
                    _acaCode,
                    ""
            ).enqueue(new Callback<SystemNoticeListResponse>() {
                @Override
                public void onResponse(Call<SystemNoticeListResponse> call, Response<SystemNoticeListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if(finalLastNoticeSeq == 0) {
                                if (mList.size() > 0) mList.clear();
                            }
                            if (response.body() != null) {
                                List<SystemNoticeListData> list = response.body().data;
                                if (list != null) mList.addAll(list);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "getListData() Exception : ", e.getMessage());
                    }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();

                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<SystemNoticeListResponse> call, Throwable t) {
                    if (mList.size() > 0) mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//    private void getReportListData(int... lastSeq) {
//
//        if (mList.size() > 0) mList.clear();
//
//        int lastNoticeSeq = 0;
//        if (lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];
//
//        if (RetrofitClient.getInstance() != null) {
//            final int finalLastNoticeSeq = lastNoticeSeq;
//            RetrofitClient.getApiInterface().getReportList(
//                    lastNoticeSeq,
//                    _memberSeq,
//                    _userGubun,
//                    _stCode,
//                    "",
//                    _acaCode
//            ).enqueue(new Callback<SystemReportListResponse>() {
//                @Override
//                public void onResponse(Call<SystemReportListResponse> call, Response<SystemReportListResponse> response) {
//                    try {
//                        if (response.isSuccessful()) {
//                            if(finalLastNoticeSeq == 0) {
//                                if (mReportList.size() > 0) mReportList.clear();
//                            }
//                            if (response.body() != null) {
//                                List<ReportCardSummaryData> list = response.body().data;
//                                if (list != null) mReportList.addAll(list);
//                            }
//                        } else {
//                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        LogMgr.e(TAG + "getListData() Exception : ", e.getMessage());
//                    }
//
//                    if(mAdapter != null) {
//                        mAdapter = null;
//                        mAdapter = new NoticeListAdapter(mContext, mList, mReportList, MenuNoticeActivity.this::startActivity);
//                        mRecyclerView.setAdapter(mAdapter);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                    txtEmpty.setVisibility(mReportList.size() <= 1 ? View.VISIBLE : View.GONE);
//                    mSwipeRefresh.setRefreshing(false);
//                }
//
//                @Override
//                public void onFailure(Call<SystemReportListResponse> call, Throwable t) {
//                    if (mReportList.size() > 0) mReportList.clear();
//                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
//                    txtEmpty.setVisibility(mReportList.size() <= 1 ? View.VISIBLE : View.GONE);
//
//                    mSwipeRefresh.setRefreshing(false);
//                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.tv_notice_calendar:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_selectedDate);
                Utils.yearMonthPicker(mContext, this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                break;

            case R.id.btn_notice_previous:
                if (Integer.parseInt(selYear) <= Constants.PICKER_MIN_YEAR && Integer.parseInt(selMonth) <= 1) break;
                navigateMonth(-1);
                break;

            case R.id.btn_notice_next:
                if (Integer.parseInt(selYear) >= Constants.PICKER_MAX_YEAR && Integer.parseInt(selMonth) >= 12) break;
                navigateMonth(1);
                break;
        }
    }

    private void navigateMonth(int addMonth){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.add(Calendar.MONTH, addMonth);
        onDateSet(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    @Override
    public void onDateSet(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        _selectedDate = calendar.getTime();

        selYear = _yearFormat.format(_selectedDate);
        selMonth = _monthFormat.format(_selectedDate);

        mTvCalendar.setText(_dateFormat.format(_selectedDate));
        getListData();
    }

    private void startBoardDetailActivity(SystemNoticeListData item, int type) {
        Intent intent = new Intent(mContext, MenuBoardDetailActivity.class);
        intent.putExtra(IntentParams.PARAM_NOTICE_INFO, item);
        intent.putExtra(IntentParams.PARAM_DATA_TYPE, type);
        intent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.seq);
        startActivity(intent);
    }
}