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

import androidx.annotation.NonNull;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.NoticeListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class MenuNoticeActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener {

    private static final String TAG = "NoticeActivity";

    private PowerSpinnerView _spinnerType;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private NoticeListAdapter mAdapter;
    private TextView txtEmpty, mTvCalendar;

    private final ArrayList<PushMessage> mList = new ArrayList<>();

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

    //private boolean fromBottomMenu = false;
    private String getType = "";

    private final int TYPE_SYSTEM = 3;
    private final int TYPE_REPORT_CARD = 4;
    private final int TYPE_TUITION = 5;

    private final int CMD_GET_LIST = 0;       // roomDB에 저장된 목록 가져오기

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_LIST:
                    if (getType.equals(FCMManager.MSG_TYPE_ATTEND)) getListData(attendanceType);
                    else if (getType.equals(FCMManager.MSG_TYPE_REPORT_CARD)) getListData(reportCardType);
                    else if (getType.equals(FCMManager.MSG_TYPE_TUITION)) getListData(tuitionType);
                    else getListData(systemType);
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

    private void getListData(String selType){

        new Thread(() -> {

            LogMgr.i("year", selYear);
            LogMgr.i("month", selMonth);
            List<PushMessage> item = JeetDatabase.getInstance(mContext).pushMessageDao().getMessagesByYearAndMonth(selYear, selMonth);
            List<PushMessage> newMessage = new ArrayList<>();

            for (PushMessage msg : item){

                Map<String, String> type = new HashMap<>();
                type.put(systemType, FCMManager.MSG_TYPE_SYSTEM);
                type.put(attendanceType, FCMManager.MSG_TYPE_ATTEND);
                type.put(reportCardType, FCMManager.MSG_TYPE_REPORT_CARD);
                type.put(tuitionType, FCMManager.MSG_TYPE_TUITION);

                String mappedType = type.get(selType);
                if (mappedType!=null) {
                    if (msg.pushType.equals(mappedType)){
                        if (_stCode == msg.stCode) newMessage.add(msg);
                    }
                }
                LogMgr.w(TAG,
                        "RoomDB LIST \npushType : " + msg.pushType + "\n" +
                                "acaCode : " + msg.acaCode + "\n" +
                                "date : " + msg.date + "\n" +
                                "body : " + msg.body + "\n" +
                                "id : " + msg.id + "\n" +
                                "pushId : " + msg.pushId + "\n" +
                                "title : " + msg.title + "\n" +
                                "memberSeq : " + msg.memberSeq + "\n" +
                                "connSeq : " + msg.connSeq + "\n" +
                                "isRead : " + msg.isRead + "\n"
                );
            }

            runOnUiThread(() -> {
                if (mList.size() > 0) mList.clear();
                mList.addAll(newMessage);
                mAdapter.notifyDataSetChanged();
                if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(false);
                if (txtEmpty != null) txtEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
            });
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

        noticeType = getResources().getStringArray(R.array.notice_type);
        //allType = noticeType[0];
        systemType = noticeType[0];
        attendanceType = noticeType[1];
        reportCardType = noticeType[2];
        tuitionType = noticeType[3];

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(IntentParams.PARAM_TYPE_NOTICE)){
            getType = intent.getStringExtra(IntentParams.PARAM_TYPE_NOTICE);
        }
    }

    @Override
    void initView() {
        initData();
        findViewById(R.id.btn_notice_previous).setOnClickListener(this);
        findViewById(R.id.btn_notice_next).setOnClickListener(this);

        mTvCalendar = findViewById(R.id.tv_notice_calendar);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_notice);
        _spinnerType = findViewById(R.id.spinner_notice_type);

        mTvCalendar.setOnClickListener(this);
        mTvCalendar.setText(_dateFormat.format(_selectedDate));
        selYear= _yearFormat.format(_selectedDate);
        selMonth= _monthFormat.format(_selectedDate);

        setSpinner();
        setRecycler();
        mSwipeRefresh.setOnRefreshListener(() -> getListData(_spinnerType.getText().toString()));

        mHandler.sendEmptyMessage(CMD_GET_LIST);
    }

    private void setSpinner(){
        _spinnerType.setIsFocusable(true);

        _spinnerType.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            getListData(_spinnerType.getText().toString());
        });

        _spinnerType.setSpinnerOutsideTouchListener((view, motionEvent) -> _spinnerType.dismiss());
        switch (getType) {
            case FCMManager.MSG_TYPE_SYSTEM:

                break;
            case FCMManager.MSG_TYPE_ATTEND:
                _spinnerType.selectItemByIndex(1);
                break;
            case FCMManager.MSG_TYPE_REPORT_CARD:
                _spinnerType.selectItemByIndex(2);
                break;
            case FCMManager.MSG_TYPE_TUITION:
                _spinnerType.selectItemByIndex(3);
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
    }

    private void startActivity(PushMessage item){
        if (item != null){
            Intent intent = new Intent(mContext, MenuBoardDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_NOTICE_INFO, item);

            switch (item.pushType) {
                case FCMManager.MSG_TYPE_SYSTEM:  // 시스템알림
                    startBoardDetailActivity(item, TYPE_SYSTEM);
                    break;

                case FCMManager.MSG_TYPE_REPORT_CARD:  // 성적표
                    startBoardDetailActivity(item, TYPE_REPORT_CARD);
                    break;

                case FCMManager.MSG_TYPE_TUITION:  // 미납
                    startActivity(new Intent(mContext, TuitionActivity.class));
                    break;
            }
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("item is null ");
    }

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
        getListData(_spinnerType.getText().toString());
    }

    private void startBoardDetailActivity(PushMessage item, int type) {
        Intent intent = new Intent(mContext, MenuBoardDetailActivity.class);
        intent.putExtra(IntentParams.PARAM_NOTICE_INFO, item);
        intent.putExtra(IntentParams.PARAM_DATA_TYPE, type);
        intent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.connSeq);
        startActivity(intent);
    }
}