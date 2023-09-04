package kr.jeet.edu.student.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ScheduleListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.dialog.ScheduleDialog;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.model.response.HolidayData;
import kr.jeet.edu.student.model.response.ScheduleDetailResponse;
import kr.jeet.edu.student.model.response.ScheduleListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.calendar.decorator.EventDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelectionDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelEventDecorator;
import kr.jeet.edu.student.view.calendar.decorator.TodayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.UnSelEventDecorator;
import kr.jeet.edu.student.view.calendar.formatter.CustomTitleFormatter;
import kr.jeet.edu.student.view.calendar.formatter.CustomWeekDayFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuScheduleActivity extends BaseActivity {

    private static final String TAG = "ScheduleActivity";

    private TextView mTvListEmpty;
    private PowerSpinnerView mSpinnerCampus;
    private MaterialCalendarView mCalendarView;
    private RecyclerView mRecyclerSchedule;
    private ScheduleListAdapter mAdapter;

    private ArrayList<ScheduleData> mList = new ArrayList<>();
    private ArrayList<ScheduleData> mListDay = new ArrayList<>();
    private ArrayList<CalendarDay> calendarDayList = new ArrayList<>();
    Set<CalendarDay> cal = new HashSet<>();

    private String _acaCode = "";
    private String _acaName = "";
    private String _userType = "";

    private CalendarDay calUnSelDay;
    private CalendarDay calSelDay;

    Date _selectedDate = new Date();
    private int selYear = 0;
    private int selMonth = 0;
    private int selDay = 0;

    private int eventYear = 0;
    private int eventMonth = 0;
    private int eventDay = 0;

    private AppCompatActivity mActivity;

    private final int CMD_VIEW_INIT = 0;       // View init

    EventDecorator eventDecorator = null;
    HashSet<CalendarDay> calendarDaySet = null;
    HolidayDecorator holidayDec = null;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_VIEW_INIT:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule);
        mContext = this;
        mActivity = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        _userType = PreferenceUtil.getUserType(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);

        Calendar cal = Calendar.getInstance();
        selYear = cal.get(Calendar.YEAR);
        selMonth = cal.get(Calendar.MONTH)+1;
        selDay = cal.get(Calendar.DAY_OF_MONTH);

        LogMgr.i(TAG, "day: " + selDay);

        if (_userType.equals(Constants.MEMBER)) requestScheduleList(_acaCode);
        else requestScheduleList("");
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_campus_schedule);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mSpinnerCampus = findViewById(R.id.spinner_schedule_campus);
        mCalendarView = findViewById(R.id.cv_schedule);
        mRecyclerSchedule = findViewById(R.id.recycler_schedule);
        mTvListEmpty = findViewById(R.id.tv_schedule_empty_list);

        setSpinner();
        setCalendar();
        setRecycler();
    }

    private void setSpinner(){
        List<ACAData> spinList = DataManager.getInstance().getACAList();
        List<String> acaNames = new ArrayList<>();

        acaNames.add(getString(R.string.announcement_spinner_default_text));

        for (ACAData data : spinList) acaNames.add(data.acaName);

        if (_userType.equals(Constants.MEMBER)) mSpinnerCampus.setText(_acaName);
        else mSpinnerCampus.setText(acaNames.get(0));

        mSpinnerCampus.setItems(acaNames);
        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex > 0) _acaCode = spinList.get(newIndex - 1).acaCode;
            else _acaCode = "";

            requestScheduleList(_acaCode);
        });
        mSpinnerCampus.setSpinnerOutsideTouchListener((view, motionEvent) -> mSpinnerCampus.dismiss());
    }

    SelEventDecorator selEventDec= null;
    UnSelEventDecorator unSelEventDec= null;
    SelectionDecorator selectionDec= null;
    private void setCalendar(){
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;

        TodayDecorator todayDec = new TodayDecorator();
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mActivity);
        selEventDec = new SelEventDecorator(mActivity);
        unSelEventDec = new UnSelEventDecorator(mActivity);
        eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));

        mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_DEFAULTS);
        mCalendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        mCalendarView.addDecorators(eventDecorator, todayDec, saturdayDec, sundayDec, holidayDec, selectionDec, selEventDec, unSelEventDec);
        mCalendarView.setTitleFormatter(new CustomTitleFormatter(mContext));
        mCalendarView.state().edit()
                .setMinimumDate(CalendarDay.from(Constants.PICKER_MIN_YEAR, MIN_MONTH, MIN_DAY))
                .setMaximumDate(CalendarDay.from(Constants.PICKER_MAX_YEAR, MAX_MONTH, MAX_DAY))
                .commit();

        mCalendarView.setOnTitleClickListener(v -> {
            int currentYear = mCalendarView.getCurrentDate().getYear();
            int currentMonth = mCalendarView.getCurrentDate().getMonth();

            Utils.yearMonthPicker(mContext, (month, year) -> {
                //LocalDate localDate = LocalDate.of(year, month + 1, 1);
                LogMgr.e(TAG, year+"년 "+month+"월");
                CalendarDay newDate = CalendarDay.from(year, month, 1);
                runOnUiThread( () -> mCalendarView.setCurrentDate(newDate) );
                selYear = year;
                selMonth = month+1;
                selDay = 1;
                LogMgr.e(TAG, selYear+"년 "+selMonth+"월");
            }, currentYear, currentMonth);
        });

        mCalendarView.setOnDateChangedListener((view, date, selected) -> {
            if (mListDay.size() > 0) mListDay.clear();
            unSelEventDec.setSelectedDay(null);

            selYear = date.getYear();
            selMonth = date.getMonth()+1;
            selDay = date.getDay();

            if (calendarDaySet != null){
                if (calendarDaySet.contains(date)) {
                    selEventDec.setSelectedDay(date);
                    calUnSelDay = date;

                } else { if (calUnSelDay != null) unSelEventDec.setSelectedDay(calUnSelDay); }
            }

            selectionDec.setSelectedDay(date);

            calSelDay = date;

            LogMgr.i("DateTest", "year: "+selYear + ", " +"month: "+ selMonth + ", " + "day: " + selDay);
            for (int i=0; i < mList.size(); i++) if (selDay == mList.get(i).day) mListDay.add(mList.get(i));
            mAdapter.notifyDataSetChanged();

            view.invalidateDecorators();

            mTvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
        });

        mCalendarView.setOnMonthChangedListener((view, date) -> {
            unSelEventDec.setSelectedDay(null);

            selYear = date.getYear();
            selMonth = date.getMonth()+1;
            selDay = date.getDay();

//            if (calSelDay != null){
//                view.setSelectedDate(date);
//
//                if (calendarDaySet != null){
//                    if (calendarDaySet.contains(date)) {
//                        selEventDec.setSelectedDay(date);
//                        calUnSelDay = date;
//
//                    }
//                    else {
//                        if (calUnSelDay != null){
//                            unSelEventDec.setSelectedDay(calUnSelDay);
//                        }
//                    }
//                }
//
//                selectionDec.setSelectedDay(date);
//            }

            view.setSelectedDate(date);

            if (calendarDaySet != null){
                if (calendarDaySet.contains(date)) {
                    selEventDec.setSelectedDay(date);
                    calUnSelDay = date;

                } else {
                    if (calUnSelDay != null){
                        unSelEventDec.setSelectedDay(calUnSelDay);
                    }
                }
            }

            selectionDec.setSelectedDay(date);

            LogMgr.i("DateTestMonth", "year: "+selYear + ", " +"month: "+ selMonth + ", " + "day: " + selDay);
            requestScheduleList(_acaCode);
        });
    }

    private void showItemDay(){

    }

    private void setRecycler(){

        mAdapter = new ScheduleListAdapter(mContext, mListDay, this::getDetailData);
        mRecyclerSchedule.setAdapter(mAdapter);
        mRecyclerSchedule.addItemDecoration(Utils.setDivider(mContext));

        mAdapter.notifyDataSetChanged();
    }

    private void getDetailData(ScheduleData item){
        if (item != null){
            showDialog(item);
        }else{
            requestDetailSchedule(item.seq);
        }

    }

    private void showDialog(ScheduleData item){
        ScheduleDialog dialog = new ScheduleDialog(mContext);
        dialog.setData(item);
        dialog.setOnCloseClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    int index = 0;

    private void requestScheduleList(String acaCode) {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getScheduleList(acaCode, selYear, selMonth).enqueue(new Callback<ScheduleListResponse>() {
                @Override
                public void onResponse(Call<ScheduleListResponse> call, Response<ScheduleListResponse> response) {
                    if (mList.size() > 0) mList.clear();
                    if (mListDay.size() > 0) mListDay.clear();

                    try {
                        if (response.isSuccessful()) {
                            List<ScheduleData> getData = new ArrayList<>();
                            List<HolidayData> getHoliday = new ArrayList<>();

                            if (response.body().data.scheduleList != null) {
                                getData = response.body().data.scheduleList;

                                if (!getData.isEmpty()) {

                                    mList.addAll(getData);
                                    for (int i=0; i < getData.size(); i++) if (selDay == getData.get(i).day) mListDay.add(getData.get(i));
                                    for (ScheduleData item : getData) calendarDayList.add(CalendarDay.from(item.year, item.month-1, item.day));

                                    calendarDaySet = new HashSet<>(calendarDayList);
                                    eventDecorator.setDates(calendarDayList);

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }

                            if (response.body().data.holidayList != null){
                                getHoliday = response.body().data.holidayList;

                                if (!getHoliday.isEmpty()){
                                    getHoliday.forEach(holiday -> cal.add(CalendarDay.from(selYear, Integer.parseInt(holiday.month) - 1, Integer.parseInt(holiday.day))));

                                    holidayDec.setDates(cal);
                                }

                                holidayDec.setDates(cal);
                                LogMgr.i(TAG, "calSize : " + cal.size());
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestScheduleList() Exception: ", e.getMessage());
                    }

                    CalendarDay oneDay = CalendarDay.from(selYear, selMonth-1, selDay);

                    LogMgr.i(TAG, "requestScheduleList() Date : "+ selYear+","+selMonth+","+selDay);

                    mCalendarView.setSelectedDate(oneDay);
                    selectionDec.setSelectedDay(oneDay);

                    if (calendarDaySet != null){
                        if (calendarDaySet.contains(oneDay)) {
                            selEventDec.setSelectedDay(oneDay);
                            calUnSelDay = oneDay;

                        } else {
                            if (calUnSelDay != null){
                                unSelEventDec.setSelectedDay(calUnSelDay);
                            }
                        }
                    }

                    mCalendarView.invalidateDecorators();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
                    //mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ScheduleListResponse> call, Throwable t) {
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    try {
                        LogMgr.e(TAG, "requestScheduleList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    mCalendarView.invalidateDecorators();
                    mTvListEmpty.setVisibility(mListDay.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    private void requestDetailSchedule(int seq){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getScheduleDetail(seq).enqueue(new Callback<ScheduleDetailResponse>() {
                @Override
                public void onResponse(Call<ScheduleDetailResponse> call, Response<ScheduleDetailResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            ScheduleData getData = null;

                            if (response.body() != null) {
                                getData = response.body().data;

                                if (getData != null) showDialog(getData);

                                LogMgr.i(TAG, "mListSize : " + mList.size());
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestDetailSchedule() Exception: ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ScheduleDetailResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestDetailSchedule() onFailure >> " + t.getMessage());
                    } catch (Exception e) {}
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }
}































