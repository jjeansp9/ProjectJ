package kr.jeet.edu.student.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BriefingListAdapter;
import kr.jeet.edu.student.adapter.ScheduleListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.dialog.ScheduleDialog;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.BriefingResponse;
import kr.jeet.edu.student.model.response.ScheduleListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.calendar.decorator.EventDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSundayDecorator;
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

    private String _acaCode = "";
    private String _acaName = "";
    private String _userType = "";

    private CalendarDay unSelDay;

    Date _selectedDate = new Date();
    private int selYear = 0;
    private int selMonth = 0;
    private int selDay = 0;

    private int eventYear = 0;
    private int eventMonth = 0;
    private int eventDay = 0;

    private AppCompatActivity mActivity;

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
        selMonth = cal.get(Calendar.MONTH);

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

    private void setCalendar(){
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;

        TodayDecorator todayDec = new TodayDecorator();
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
        SelectionDecorator selectionDec = new SelectionDecorator(mActivity);
        SelEventDecorator selEventDec = new SelEventDecorator(mActivity);
        UnSelEventDecorator unSelEventDec = new UnSelEventDecorator(mActivity);
        EventDecorator eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));

        // 테스트용 더미데이터
        ArrayList<CalendarDay> calendarDayList = new ArrayList<>();
        calendarDayList.add(CalendarDay.from(2023, 7, 19));
        calendarDayList.add(CalendarDay.from(2023, 7, 20));
        calendarDayList.add(CalendarDay.from(2023, 7, 21));
        calendarDayList.add(CalendarDay.from(2023, 7, 22));
        calendarDayList.add(CalendarDay.from(2023, 7, 27));

        eventDecorator.setDates(calendarDayList);

        mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_DEFAULTS);
        mCalendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        mCalendarView.addDecorators(eventDecorator, todayDec, saturdayDec, sundayDec, selectionDec, selEventDec, unSelEventDec);
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
            }, currentYear, currentMonth);
        });

        HashSet<CalendarDay> calendarDaySet = new HashSet<>(calendarDayList);

        mCalendarView.setOnDateChangedListener((view, date, selected) -> {
            if (calendarDaySet.contains(date)) {
                selEventDec.setSelectedDay(date);
                unSelDay = date;
            } else {
                if (unSelDay != null) unSelEventDec.setSelectedDay(unSelDay);
            }

            selectionDec.setSelectedDay(date);
            runOnUiThread(view::invalidateDecorators);
            unSelEventDec.setSelectedDay(null);

            selYear = date.getYear();
            selMonth = date.getMonth()+1;
            selDay = date.getDay();
            LogMgr.i("DateTest", "year: "+selYear + ", " +"month: "+ selMonth + ", " + "day: " + selDay);


        });

        mCalendarView.setOnMonthChangedListener((view, date) -> {
            requestScheduleList(_acaCode);
        });
    }

    private void setRecycler(){
//        for (int i=0; i<10; i++) mList.add(
//                new ScheduleData(
//                        "5.3 (수)",
//                        "[악어수학, 중등]",
//                        " - 재원생 레벨테스트 5/8(월) ~ 5/10(수)"
//                ));

        mAdapter = new ScheduleListAdapter(mContext, mList, this::showDialog);
        mRecyclerSchedule.setAdapter(mAdapter);
        mRecyclerSchedule.addItemDecoration(Utils.setDivider(mContext));

        mAdapter.notifyDataSetChanged();
    }

    private void showDialog(ScheduleData item){
        ScheduleDialog dialog = new ScheduleDialog(mContext);
//        dialog.setTitle(item.stDate);
//        dialog.setCampus(item.stClass);
//        dialog.setSchedule(item.stContent);
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
                    if (mList.size() > 0){
                        for (int i = mList.size() - 1; i >= 0; i--) {
                            mList.remove(i);
                            mAdapter.notifyItemRemoved(i);
                        }
                        index = 0;
                    }
                    try {
                        if (response.isSuccessful()) {
                            List<ScheduleData> getData = new ArrayList<>();

                            if (response.body() != null) {
                                getData = response.body().data.scheduleList;
                                if (getData != null && !getData.isEmpty()) {
                                    //mList.addAll(getData);

                                    for (ScheduleData item : getData) {
                                        mList.add(index, item);
                                        mAdapter.notifyItemInserted(index);
                                        index++;
                                    }

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardList() Exception: ", e.getMessage());
                    }
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    //if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    //mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ScheduleListResponse> call, Throwable t) {
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }
}































