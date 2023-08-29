package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.NoticeListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.utils.LogMgr;
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

    private String[] noticeType;
    private String allType = "";
    private String systemType = "";
    private String attendanceType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_notice);

        noticeType = getResources().getStringArray(R.array.notice_type);
        //allType = noticeType[0];
        systemType = noticeType[0];
        attendanceType = noticeType[1];

        mContext = this;
        getListData(systemType);
        initAppbar();
        initView();
    }

    private void getListData(String selType){
        if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(false);

        new Thread(() -> {
            List<PushMessage> item = JeetDatabase.getInstance(mContext).pushMessageDao().getAllMessage();
            List<PushMessage> newMessages = new ArrayList<>();

            for (PushMessage msg : item){
                LogMgr.w("DBTest",
                        "pushType : " + msg.pushType + "\n" +
                                "acaCode : " + msg.acaCode + "\n" +
                                "date : " + msg.date + "\n" +
                                "body : " + msg.body + "\n" +
                                "id : " + msg.id + "\n" +
                                "pushId : " + msg.pushId + "\n" +
                                "title : " + msg.title + "\n" +
                                "connSeq : " + msg.connSeq + "\n" +
                                "isRead : " + msg.isRead + "\n"
                );

//                if (selType.equals(allType)) {
//                    if (msg.pushType.equals(FCMManager.MSG_TYPE_SYSTEM) || msg.pushType.equals(FCMManager.MSG_TYPE_ATTEND)){
//                        newMessages.add(msg);
//                    }
//
//                }
                if (selType.equals(systemType)){
                    if (msg.pushType.equals(FCMManager.MSG_TYPE_SYSTEM)) newMessages.add(msg);

                }else if (selType.equals(attendanceType)){
                    if (msg.pushType.equals(FCMManager.MSG_TYPE_ATTEND)) newMessages.add(msg);
                }
            }

            // TODO : 페이징처리 , 전체 항목은 제거

            runOnUiThread(() -> {
                if (txtEmpty != null) txtEmpty.setVisibility(newMessages.isEmpty() ? View.VISIBLE : View.GONE);
                updateList(newMessages);
            });
        }).start();
    }

    private void updateList(List<PushMessage> newMessage){
        for (int i = mList.size() - 1; i >= 0; i--) {
            if (!newMessage.contains(mList.get(i))) {
                mList.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }

        for (int i = 0; i < newMessage.size(); i++) {
            if (!mList.contains(newMessage.get(i))) {
                mList.add(i, newMessage.get(i));
                mAdapter.notifyItemInserted(i);
            }
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_notice);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.btn_notice_previous).setOnClickListener(this);
        findViewById(R.id.btn_notice_next).setOnClickListener(this);

        mTvCalendar = findViewById(R.id.tv_notice_calendar);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_notice);
        _spinnerType = findViewById(R.id.spinner_notice_type);

        mTvCalendar.setOnClickListener(this);
        mTvCalendar.setText(_dateFormat.format(_selectedDate));

        setSpinner();
        setRecycler();
        mSwipeRefresh.setOnRefreshListener(() -> getListData(_spinnerType.getText().toString()));
    }

    private void setSpinner(){
        _spinnerType.setText(systemType);
        _spinnerType.setIsFocusable(true);

        _spinnerType.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            getListData(newItem.toString());
        });

        _spinnerType.setSpinnerOutsideTouchListener((view, motionEvent) -> _spinnerType.dismiss());
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
                if(!mRecyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    //int lastNoticeSeq = mList.get(mList.size() - 1).seq;
                    getListData(_spinnerType.getText().toString());
                }
            }
        });
    }

    private void startActivity(PushMessage item){
        if (item != null){
            Intent intent = new Intent(mContext, MenuBoardDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_NOTICE_INFO, item);
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_notice));
            intent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.connSeq);
            startActivity(intent);
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
                navigateMonth(-1);
                break;

            case R.id.btn_notice_next:
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
        _selectedDate = calendar.getTime();

        mTvCalendar.setText(_dateFormat.format(_selectedDate));
    }
}