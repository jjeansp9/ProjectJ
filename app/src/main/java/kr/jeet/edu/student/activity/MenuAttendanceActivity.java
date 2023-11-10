package kr.jeet.edu.student.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.MonthlyAttendanceListAdapter;
import kr.jeet.edu.student.adapter.TuitionListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.model.data.AttendanceData;
import kr.jeet.edu.student.model.data.AttendanceSummaryData;
import kr.jeet.edu.student.model.data.HolidayData;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.calendar.decorator.AttendanceDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HighlightSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherMonthDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelectionDecorator;
import kr.jeet.edu.student.view.calendar.decorator.TodayBackgroundDecorator;
import kr.jeet.edu.student.view.calendar.formatter.CustomTitleFormatter;
import kr.jeet.edu.student.view.calendar.formatter.CustomWeekDayFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAttendanceActivity extends BaseActivity {

    private static final String TAG = "MenuAttendanceActivity";

    private MaterialCalendarView _calendarView;
    private PowerSpinnerView mSpinnerCls;
    private ChipGroup chipGroupLegend;  //범례

    private RecyclerView recyclerViewMonthlyAttend;
    private MonthlyAttendanceListAdapter _attendanceListAdapter;

    private Calendar calendar;
    String strYear = "";
    String strMonth = "";
    private SimpleDateFormat yearFormat, monthFormat;

    private ArrayList<Constants.PayListItem> mTuitionList = new ArrayList<>();

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private int _clsCode = 0;
//    private String _clsName = "";

    private String currentYear = "";
    private String currentMonth = "";
    private String currentDate = "";

    private ArrayList<AttendanceData> _attendanceList = new ArrayList<>();
    private ArrayList<TeacherClsData> mListCls = new ArrayList<>();
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    SimpleDateFormat _apiDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMM);
    //calendar
    private Set<AttendanceSummaryData> calendarDaySet = new HashSet<>();

    AttendanceDecorator attendDecorator = null;
    AttendanceDecorator absenceDecorator = null;
    AttendanceDecorator earlyLeaveDecorator = null;
    AttendanceDecorator tardyDecorator = null;
    AttendanceDecorator makeupClassDecorator = null;
    AttendanceDecorator onlineLectureDecorator = null;
    HolidayDecorator holidayDec = null;
    SelectionDecorator selectionDec= null;
    OtherMonthDecorator otherDec = null;
    OtherSundayDecorator otherSundayDec = null;
    OtherSaturdayDecorator otherSaturdayDec = null;
    private Date _selectedDate = new Date();
    private TeacherClsData _selectedClass = null;

    private static final int CMD_GET_CLASS_INFO = 1;
    private static final int CMD_GET_ATTENDANCE_INFO = 3;

    private Handler _handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_CLASS_INFO:
                    requestCls();
                case CMD_GET_ATTENDANCE_INFO:
                    requestGetAttendanceList();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_attendance);
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DOWN);
    }

    private void initData(){

        calendar = Calendar.getInstance();
        yearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        monthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        currentYear = yearFormat.format(calendar.getTime());
        currentMonth = monthFormat.format(calendar.getTime());
        currentDate = yearFormat.format(calendar.getTime()) + monthFormat.format(calendar.getTime());

        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _userType = PreferenceUtil.getUserType(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);

        mListCls.addAll(DataManager.getInstance().getClsListMap().values());
    }

    @Override
    void initView() {
        initData();
        mSpinnerCls = findViewById(R.id.spinner_cls);
        mSpinnerCls.setSpinnerOutsideTouchListener((view, event) -> mSpinnerCls.dismiss());

        mSpinnerCls.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            Optional optional = mListCls.stream().filter(t->String.valueOf(newItem).equals(t.clsName)).findFirst();
            if(optional.isPresent()) {
                _selectedClass = (TeacherClsData) optional.get();
                _clsCode = _selectedClass.clsCode;
                _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
            }
        });
        mSpinnerCls.setLifecycleOwner(this);
        mSpinnerCls.setIsFocusable(true);

        strYear = currentYear + getString(R.string.year);
        strMonth = currentMonth + getString(R.string.month);

        recyclerViewMonthlyAttend = findViewById(R.id.recyclerview_attendance);
        _attendanceListAdapter = new MonthlyAttendanceListAdapter(mContext, _attendanceList);
        recyclerViewMonthlyAttend.setAdapter(_attendanceListAdapter);

        setCalendar();
        initChipGroup();
        setSpinnerTeacher();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_attendance);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSpinnerTeacher(){
        LogMgr.e(TAG, "setSpinnerClass()");
        try {
            if(mListCls != null && mListCls.size() > 0) {
                if(mSpinnerCls.getVisibility() != View.VISIBLE) {
                    Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                    mSpinnerCls.setVisibility(View.VISIBLE);
                }
            }else{
                mSpinnerCls.setVisibility(View.GONE);
                mSpinnerCls.clearSelectedItem();
                if(_selectedClass != null) {
                    Toast.makeText(mContext, R.string.msg_empty_class, Toast.LENGTH_SHORT).show();
                    _selectedClass = null;
                    _clsCode = 0;
                }
                if(_attendanceList != null) {
                    _attendanceList.clear();
                    _attendanceListAdapter.notifyDataSetChanged();
                }
                return;
            }

            Utils.updateSpinnerList(mSpinnerCls, mListCls.stream().map(t->t.clsName).collect(Collectors.toList()));
            if(_selectedClass != null){
                Optional optional = mListCls.stream().filter(t -> t.clsName.equals(_selectedClass.clsName)).findFirst();
                if (optional.isPresent()) {
                    _selectedClass = (TeacherClsData) optional.get();
                    try {
                        int index = mListCls.indexOf(_selectedClass);
                        mSpinnerCls.selectItemByIndex(index);
                    }catch(Exception ex) {
                        Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                        mSpinnerCls.selectItemByIndex(0);
                    }
                } else {
                    Toast.makeText(mContext, R.string.msg_changed_class, Toast.LENGTH_SHORT).show();
                    mSpinnerCls.selectItemByIndex(0);
                }
            }else{
                mSpinnerCls.selectItemByIndex(0);
            }

        }catch (Exception e){}
    }

    private void setCalendar() {
        final int MIN_MONTH = 0;
        final int MAX_MONTH = 11;
        final int MIN_DAY = 1;
        final int MAX_DAY = 31;
        //calendar
        _calendarView = findViewById(R.id.cv_attendance);
        _calendarView.setCurrentDate(_selectedDate);
        TodayBackgroundDecorator todayDec = new TodayBackgroundDecorator(mContext);
        HighlightSaturdayDecorator saturdayDec = new HighlightSaturdayDecorator(mContext);
        HighlightSundayDecorator sundayDec = new HighlightSundayDecorator(mContext);
        otherDec = new OtherMonthDecorator(mContext);
        otherSundayDec = new OtherSundayDecorator(mContext);
        otherSaturdayDec = new OtherSaturdayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mContext);
        attendDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ATTENDANCE);
        absenceDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ABSENCE);
        earlyLeaveDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.EARLY_LEAVE);
        tardyDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.TARDY);
        makeupClassDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.MAKEUP_CLASS);
        onlineLectureDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ONLINE_LECTURE);
        CalendarDay today = CalendarDay.from(_selectedDate);
        todayDec.setSelectedDay(today);
        otherDec.setSelectedDay(today);
        otherSundayDec.setSelectedDay(today);
        otherSaturdayDec.setSelectedDay(today);
        holidayDec.setSelectedDay(today);

        _calendarView.setDynamicHeightEnabled(true);
        _calendarView.setSelected(false);
        _calendarView.setWeekDayFormatter(new CustomWeekDayFormatter(mContext));
        _calendarView.addDecorators(todayDec, saturdayDec, sundayDec, otherDec, otherSundayDec, otherSaturdayDec, holidayDec, selectionDec, attendDecorator, absenceDecorator, earlyLeaveDecorator, tardyDecorator, makeupClassDecorator, onlineLectureDecorator);
        _calendarView.setTitleFormatter(new CustomTitleFormatter(mContext));

        _calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(Constants.PICKER_MIN_YEAR, MIN_MONTH, MIN_DAY))
                .setMaximumDate(CalendarDay.from(Constants.PICKER_MAX_YEAR, MAX_MONTH, MAX_DAY))
                .commit();

        _calendarView.setOnTitleClickListener(v -> {
            int currentYear = _calendarView.getCurrentDate().getYear();
            int currentMonth = _calendarView.getCurrentDate().getMonth();

            Utils.yearMonthPicker(mContext, (month, year) -> {
                LogMgr.e(TAG, year+"년 "+month+"월");
                CalendarDay newDate = CalendarDay.from(year, month, 1);
                CalendarDay selectedCalendarDay = CalendarDay.from(_selectedDate);
                if(!newDate.isAfter(selectedCalendarDay) && !newDate.isBefore(selectedCalendarDay)){
                    return;
                }
                runOnUiThread( () -> _calendarView.setCurrentDate(newDate) );
                _selectedDate = newDate.getDate();

                LogMgr.e(TAG, "selDate = " + _selectedDate);
                _handler.sendEmptyMessage(CMD_GET_CLASS_INFO);
            }, currentYear, currentMonth);
        });
        _calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);

        _calendarView.setOnMonthChangedListener((view, date) -> {
            LogMgr.e(TAG, "calendarView onMonthChanged");
            Calendar calendar = date.getCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date selectedTime = calendar.getTime();
            if(_selectedDate.compareTo(selectedTime) == 0) return;
            _selectedDate = calendar.getTime();

            otherDec.setSelectedDay(date);
            otherSundayDec.setSelectedDay(date);
            otherSaturdayDec.setSelectedDay(date);
            holidayDec.setSelectedDay(date);
            setDeco();
            view.invalidateDecorators();
            LogMgr.i(TAG, "DateTestMonth >> " + _selectedDate);
            _handler.sendEmptyMessage(CMD_GET_CLASS_INFO);
        });
    }
    private void initChipGroup() {
        chipGroupLegend = findViewById(R.id.chipgroup_legend);
        chipGroupLegend.removeAllViews();
        int paddingDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        chipGroupLegend.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
        for(int i = 0 ; i < 9; i ++ ){
            Constants.AttendanceStatus attendance = Constants.AttendanceStatus.getByCode(i);
            if(attendance == null) break;
            Chip chip = new Chip(MenuAttendanceActivity.this);
            chip.setText(attendance.getName());
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.bg_white)));
            chip.setChipStrokeWidth(0);
            chip.setCloseIconVisible(false);
            chip.setCheckable(false);
            chip.setCheckedIconVisible(true);
            chip.setClickable(false);
            chip.setFocusable(false);
            chip.setTextStartPadding(4f);
            chip.setTextEndPadding(4f);
            chip.setChipIcon(getDrawable(R.drawable.ic_vector_circle));
            chip.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(mContext, attendance.getColorRes())));
            chip.setChipIconSize(getResources().getDimensionPixelSize(R.dimen.dot_sex_size));
            chipGroupLegend.addView(chip);
        }
    }

    // 원생 학급 정보 조회
    private void requestCls(){
        if(RetrofitClient.getInstance() != null) {
            String dateStr = _apiDateFormat.format(_selectedDate);

            RetrofitClient.getApiInterface().requestTeacherCls(_stCode, dateStr).enqueue(new Callback<TeacherClsResponse>() {
                @Override
                public void onResponse(Call<TeacherClsResponse> call, Response<TeacherClsResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            if(mListCls != null) mListCls.clear();
                            mListCls.addAll(response.body().data);
                            setSpinnerTeacher();
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestCls() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestCls() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<TeacherClsResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestCls() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestCls() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 출결조회 (월별)
    private void requestGetAttendanceList(){
        SimpleDateFormat mFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYYMM);
        String date = mFormat.format(_selectedDate);

        if (RetrofitClient.getInstance() != null){
            LogMgr.e(TAG, "clsCode: " + _clsCode);

            RetrofitClient.getApiInterface().getMonthlyAttendanceInfo(date, _clsCode, _stCode).enqueue(new Callback<GetAttendanceInfoResponse>() {
                @Override
                public void onResponse(Call<GetAttendanceInfoResponse> call, Response<GetAttendanceInfoResponse> response) {
                    try {
                        if(_attendanceList != null && _attendanceList.size() > 0) {
                            _attendanceList.clear();
                        }
                        if (response.isSuccessful()){
                            List<AttendanceData> getData = null;
                            if (response.body() != null) {

                                getData = response.body().data;
                                if (getData != null && _attendanceList != null) _attendanceList.addAll(getData);
                            }
                        }else{
                            //Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestGetAttendanceList() Exception : ", e.getMessage());
                    }finally{
//                        hideProgressDialog();
                        _attendanceListAdapter.notifyDataSetChanged();
                        updateCalView();
                    }
                }

                @Override
                public void onFailure(Call<GetAttendanceInfoResponse> call, Throwable t) {
                    try {
                        if(_attendanceList != null && _attendanceList.size() > 0) {
                            _attendanceList.clear();
                        }
                        //Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        LogMgr.e(TAG, "requestGetAttendanceList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }finally{
//                        hideProgressDialog();
                        _attendanceListAdapter.notifyDataSetChanged();
                        updateCalView();
                    }
                }
            });
        }
    }

    private void updateCalView(){
        LogMgr.e(TAG, "updateCalView");
        if (calendarDaySet != null && calendarDaySet.size() > 0) calendarDaySet.clear();
        for(AttendanceData item : _attendanceList) {
            AttendanceSummaryData summaryData = new AttendanceSummaryData();
            summaryData.attendGubun = item.attendGubun;
            try {
                Date attendDate = _dateTransferFormat.parse(item.attendDate);
                summaryData.attendDate = CalendarDay.from(attendDate);
                calendarDaySet.add(summaryData);
            }catch(Exception ex) {
                continue;
            }
        }
        setDeco();
        _calendarView.invalidateDecorators();
    }

    private void setDeco(){
        if (calendarDaySet != null && calendarDaySet.size() > 0){
            attendDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ATTENDANCE.getCode()).collect(Collectors.toSet()));
            absenceDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ABSENCE.getCode()).collect(Collectors.toSet()));
            earlyLeaveDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.EARLY_LEAVE.getCode()).collect(Collectors.toSet()));
            tardyDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.TARDY.getCode()).collect(Collectors.toSet()));
            makeupClassDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.MAKEUP_CLASS.getCode()).collect(Collectors.toSet()));
            onlineLectureDecorator.setDates(calendarDaySet.stream().filter(t->t.attendGubun == Constants.AttendanceStatus.ONLINE_LECTURE.getCode()).collect(Collectors.toSet()));
        }else{
            attendDecorator.setDates(Collections.emptySet());
            absenceDecorator.setDates(Collections.emptySet());
            earlyLeaveDecorator.setDates(Collections.emptySet());
            tardyDecorator.setDates(Collections.emptySet());
            makeupClassDecorator.setDates(Collections.emptySet());
            onlineLectureDecorator.setDates(Collections.emptySet());
        }
    }
}