package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ScheduleListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.model.data.HolidayData;
import kr.jeet.edu.student.model.data.StudentGradeData;
import kr.jeet.edu.student.model.response.ScheduleListResponse;
import kr.jeet.edu.student.model.response.StudentGradeListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.calendar.decorator.EventDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherMonthDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelBackgroundDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelectionDecorator;
import kr.jeet.edu.student.view.calendar.decorator.TodayBackgroundDecorator;
import kr.jeet.edu.student.view.calendar.formatter.CustomTitleFormatter;
import kr.jeet.edu.student.view.calendar.formatter.CustomWeekDayFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuScheduleActivity extends BaseActivity {

    private static final String TAG = "ScheduleActivity";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private static final int CMD_GET_SCHEDULES = 2;

    private TextView mTvListEmpty, mTvHolidayDate, mTvHoliday;
    private PowerSpinnerView _spinnerCampus, _spinnerGrade;
    private MaterialCalendarView mCalendarView;
    private RecyclerView mRecyclerSchedule;
    private ScheduleListAdapter mAdapter;

    private ArrayList<ScheduleData> mList = new ArrayList<>();
    private ArrayList<ScheduleData> mListDay = new ArrayList<>();
    //private ArrayList<CalendarDay> calendarDayList = new ArrayList<>();
    private Set<CalendarDay> calendarDaySet = new HashSet<>();

    private SimpleDateFormat _holidayFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_M_D_E, Locale.KOREA);
    private Set<CalendarDay> calHoliday = new HashSet<>();
    private ArrayList<HolidayData> calHolidayList = new ArrayList<>();

    private String _acaCode = "";
    private String _appAcaCode = "";
    private String _acaName = "";
    private String _userType = "";
    List<ACAData> _ACAList = new ArrayList<>();
    List<StudentGradeData> _GradeList = new ArrayList<>();
    ACAData _selectedACA = null;
    private StudentGradeData _selectedGrade = null;

    Date _selectedDate = new Date();

    EventDecorator eventDecorator = null;
    HolidayDecorator holidayDec = null;
    SelectionDecorator selectionDec= null;
    OtherMonthDecorator otherDec = null;
    OtherSundayDecorator otherSundayDec = null;
    OtherSaturdayDecorator otherSaturdayDec = null;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null && !_GradeList.isEmpty()) {
//                        _spinnerGrade.setEnabled(true);
                        Utils.updateSpinnerList(_spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            _spinnerGrade.selectItemByIndex(index);
                        }else{
                            _spinnerGrade.selectItemByIndex(0);
                        }
                    }else {
                        _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
                    }
                    break;

                case CMD_GET_SCHEDULES:
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(_selectedDate);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    requestScheduleList(year, month);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_schedule);
        mContext = this;
        initData();
        initView();
        initAppbar();
        setAnim(true);
    }

    private void initData(){
        _userType = PreferenceUtil.getUserType(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);

        Calendar cal = Calendar.getInstance();
        cal.setTime(_selectedDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

//        if (_userType.equals(Constants.MEMBER)) requestScheduleList(_acaCode, year, month);
//        else requestScheduleList("", year, month);
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
        _spinnerCampus = findViewById(R.id.spinner_schedule_campus);
        _spinnerGrade = findViewById(R.id.spinner_schedule_grade);

        mCalendarView = findViewById(R.id.cv_schedule);
        mRecyclerSchedule = findViewById(R.id.recycler_schedule);
        mTvListEmpty = findViewById(R.id.tv_schedule_empty_list);
        mTvHolidayDate = findViewById(R.id.tv_holiday_date);
        mTvHoliday = findViewById(R.id.tv_holiday);

        setSpinner();
        setCalendar();
        setRecycler();

        setTvHolidayDate();
    }

    private void setSpinner(){
        _spinnerCampus.setIsFocusable(true);
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        List<String> acaNames = new ArrayList<>();
        for (ACAData data : _ACAList) { acaNames.add(data.acaName); }

        _spinnerCampus.setItems(acaNames);
        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedACA = (ACAData) optional.get();
//                _listAdapter.setACACode(_selectedACA.acaCode);
                LogMgr.w(TAG,"selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
            }
            if(_selectedACA != null) {
                LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (_spinnerGrade != null) _spinnerGrade.clearSelectedItem();
                if(!TextUtils.isEmpty(_selectedACA.acaCode)) {
                    requestGradeList(_selectedACA.acaCode);
                }else{
                    if(_GradeList != null) _GradeList.clear();
                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
                LogMgr.w(TAG, "_spinnerGrade == null" + (_spinnerGrade == null));
                if(TextUtils.isEmpty(_selectedACA.acaCode)){   //전체
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(false);
                }else{
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(true);
                }
            }


        });
        _spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerCampus.dismiss();
            }
        });
        _spinnerCampus.setLifecycleOwner(this);
        if(!TextUtils.isEmpty(_appAcaCode) && Constants.MEMBER.equals(_userType)){
            ACAData selectedACA = null;
            Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(_appAcaCode)).findFirst();
            if(option.isPresent()) {
                selectedACA = (ACAData) option.get();
            }

            try {
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    _spinnerCampus.selectItemByIndex(selectedIndex); //전체

                } else {
                    _spinnerCampus.selectItemByIndex(0);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }else{
            _spinnerCampus.selectItemByIndex(0);
        }

        _spinnerGrade.setIsFocusable(true);
        _spinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedGrade = (StudentGradeData) optional.get();
//                _listAdapter.setACACode(_selectedACA.acaCode);
                LogMgr.w(TAG,"selectedACA = " + _selectedGrade.gubunCode + " / " + _selectedGrade.gubunName);
            }
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(_selectedDate);
            Message msg = _handler.obtainMessage(CMD_GET_SCHEDULES);//
//            msg.arg1 = calendar.get(Calendar.YEAR);
//            msg.arg2 = calendar.get(Calendar.MONTH);
            _handler.sendMessage(msg);

        });
        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);


    }

    private void setTvHolidayDate(){
        mTvHoliday.setText("");
        mTvHoliday.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);

        mTvHolidayDate.setText(_holidayFormat.format(_selectedDate));

        for (HolidayData item : calHolidayList){
            if ((Integer.parseInt(item.month) == calendar.get(Calendar.MONTH) + 1) && Integer.parseInt(item.day) == calendar.get(Calendar.DATE)){
                mTvHoliday.setText(TextUtils.isEmpty(item.name) ? "" : item.name);
                mTvHoliday.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void setCalendar(){
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;

        TodayBackgroundDecorator todayDec = new TodayBackgroundDecorator(mContext);
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
        SelBackgroundDecorator bgDec = new SelBackgroundDecorator(mContext);
        otherDec = new OtherMonthDecorator(mContext);
        otherSundayDec = new OtherSundayDecorator(mContext);
        otherSaturdayDec = new OtherSaturdayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mContext);
        eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));

        CalendarDay today = CalendarDay.from(_selectedDate);
        todayDec.setSelectedDay(today);
        bgDec.setSelectedDay(today);
        otherDec.setSelectedDay(today);
        otherSundayDec.setSelectedDay(today);
        otherSaturdayDec.setSelectedDay(today);
        holidayDec.setSelectedDay(today);

        mCalendarView.setDynamicHeightEnabled(true);
        mCalendarView.setSelected(false);
        mCalendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        mCalendarView.addDecorators(eventDecorator, todayDec, saturdayDec, sundayDec, bgDec, otherDec, otherSundayDec, otherSaturdayDec, holidayDec, selectionDec);
        mCalendarView.setTitleFormatter(new CustomTitleFormatter(mContext));
        mCalendarView.state().edit()
                .setMinimumDate(CalendarDay.from(Constants.PICKER_MIN_YEAR, MIN_MONTH, MIN_DAY))
                .setMaximumDate(CalendarDay.from(Constants.PICKER_MAX_YEAR, MAX_MONTH, MAX_DAY))
                .commit();

        mCalendarView.setOnTitleClickListener(v -> {
            int currentYear = mCalendarView.getCurrentDate().getYear();
            int currentMonth = mCalendarView.getCurrentDate().getMonth();

            Utils.yearMonthPicker(mContext, (month, year) -> {
                LogMgr.e(TAG, year+"년 "+month+"월");
                CalendarDay newDate = CalendarDay.from(year, month, 1);
                runOnUiThread( () -> mCalendarView.setCurrentDate(newDate) );

                _selectedDate = newDate.getDate();

                _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
            }, currentYear, currentMonth);
        });

        mCalendarView.setOnDateChangedListener((view, date, selected) -> {
            if (mListDay.size() > 0) mListDay.clear();
            _selectedDate = date.getDate();

            setTvHolidayDate();
            setDeco();
            view.invalidateDecorators();

            if (mAdapter != null) mAdapter.getFilter().filter(String.valueOf(date.getDay()));
        });

        mCalendarView.setOnMonthChangedListener((view, date) -> {
            Calendar calendar = date.getCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            _selectedDate = calendar.getTime();
            LogMgr.e(TAG, "_selectedDate: " + _selectedDate);

            otherDec.setSelectedDay(date);
            otherSundayDec.setSelectedDay(date);
            otherSaturdayDec.setSelectedDay(date);
            holidayDec.setSelectedDay(date);

            setDeco();
            view.invalidateDecorators();

            LogMgr.i(TAG, "DateTestMonth >> " + _selectedDate);
            _handler.sendEmptyMessage(CMD_GET_SCHEDULES);
        });
    }

    private void setDeco(){
        if (calendarDaySet != null && calendarDaySet.size() > 0) eventDecorator.setDates(calendarDaySet);
        else eventDecorator.setDates(Collections.emptySet());
    }

    private void setRecycler(){

        mAdapter = new ScheduleListAdapter(mContext, mList, new ScheduleListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(ScheduleData item) { startDetailActivity(item); }
            @Override
            public void onFilteringCompleted() { mTvListEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE); }
        });
        mRecyclerSchedule.setAdapter(mAdapter);
        mRecyclerSchedule.addItemDecoration(Utils.setDivider(mContext));

        mAdapter.notifyDataSetChanged();
    }

    private void startDetailActivity(ScheduleData item){
        if (item != null){
            Intent targetIntent = new Intent(mContext, MenuScheduleDetailActivity.class);
            targetIntent.putExtra(IntentParams.PARAM_SCHEDULE_INFO, item);
            startActivity(targetIntent);
            overridePendingTransition(R.anim.horizon_enter, R.anim.horizontal_out);
        }
    }
    private void requestGradeList(String acaCode){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
                @Override
                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {
                            List<StudentGradeData> list = response.body().data;
                            if(_GradeList != null) _GradeList.clear();
                            _GradeList.add(new StudentGradeData("", "구분 전체"));
                            _GradeList.addAll(list);
                            Collections.sort(_GradeList, new Comparator<StudentGradeData>() {
                                @Override
                                public int compare(StudentGradeData schoolData, StudentGradeData t1) {
                                    return schoolData.gubunCode.compareTo(t1.gubunCode);
                                }
                            });
                            _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }

    private void requestScheduleList(int year, int month) {
        if (RetrofitClient.getInstance() != null) {
            String acaCode = "";
            String acaGubunCode = "";
            if(_selectedACA != null) {
                acaCode = _selectedACA.acaCode;
            }
            if(_selectedGrade != null) {
                acaGubunCode =_selectedGrade.gubunCode;
            }

            RetrofitClient.getApiInterface().getScheduleList(acaCode,acaGubunCode, year, month).enqueue(new Callback<ScheduleListResponse>() {
                @Override
                public void onResponse(Call<ScheduleListResponse> call, Response<ScheduleListResponse> response) {
                    if (mList != null && mList.size() > 0) mList.clear();
                    //if (mListDay != null && mListDay.size() > 0) mListDay.clear();
                    //if (calendarDayList != null && calendarDayList.size() > 0) calendarDayList.clear();
                    if (calendarDaySet != null && calendarDaySet.size() > 0) calendarDaySet.clear();
                    if (calHolidayList != null && calHolidayList.size() > 0) calHolidayList.clear();

                    try {
                        if (response.isSuccessful()) {
                            List<ScheduleData> getData = new ArrayList<>();
                            List<HolidayData> getHoliday = new ArrayList<>();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(_selectedDate);

                            int selYear = calendar.get(Calendar.YEAR);
                            int selMonth = calendar.get(Calendar.MONTH) + 1;
                            int selDay = calendar.get(Calendar.DATE);

                            if (response.body().data.scheduleList != null) {
                                getData = response.body().data.scheduleList;

                                if (!getData.isEmpty()) {
                                    mList.addAll(getData);

                                    for (ScheduleData item : getData) calendarDaySet.add(CalendarDay.from(item.year, item.month-1, item.day));

//                                    mAdapter.setWholeCampusMode(TextUtils.isEmpty(acaCode));
                                }
                            }

                            if (response.body().data.holidayList != null){
                                getHoliday = response.body().data.holidayList;

                                if (!getHoliday.isEmpty()){

                                    calHolidayList.addAll(getHoliday);

                                    for (HolidayData item : getHoliday) {
                                        calHoliday.add(CalendarDay.from(selYear, Integer.parseInt(item.month) - 1, Integer.parseInt(item.day)));
                                    }

                                    holidayDec.setDates(calHoliday);
                                }
                            }

                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestScheduleList() Exception: ", e.getMessage());
                    }

                    updateCalView();

                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ScheduleListResponse> call, Throwable t) {
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    try {
                        LogMgr.e(TAG, "requestScheduleList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    mCalendarView.invalidateDecorators();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();

                    updateCalView();
                }
            });
        }
    }

    private void updateCalView(){
        CalendarDay firstDay = CalendarDay.from(_selectedDate);
        mCalendarView.setSelectedDate(firstDay);
        if(mAdapter != null) mAdapter.getFilter().filter(String.valueOf(firstDay.getDay()));
        setDeco();
        mCalendarView.invalidateDecorators();

        setTvHolidayDate();
    }
}































