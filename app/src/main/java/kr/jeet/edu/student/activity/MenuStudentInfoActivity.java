package kr.jeet.edu.student.activity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.TuitionListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.AttendanceData;
import kr.jeet.edu.student.model.data.AttendanceSummaryData;
import kr.jeet.edu.student.model.data.HolidayData;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.model.data.TuitionHeaderData;
import kr.jeet.edu.student.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.model.response.TuitionResponse;
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

public class MenuStudentInfoActivity extends BaseActivity {

    private final String TAG = "studentInfo Activity";

    public interface PayListItem extends Comparable<PayListItem> {
        boolean isHeader();
        Constants.PayType getPay();
    }

    private TextView mTvTotalPayment, mTvYear, mTvMonth, mTvStuName, mTvStuBirth, mTvStuCampus, mTvStuPhoneNum, mTvParentPhoneNum,
            mTvDeptName, mTvStGrade, mTvClstName, mTvTuitionEmpty, mTvBookPayEmpty;
    private ImageView mImgStuProfile;

    private RecyclerView mRecyclerTuition;
    private TuitionListAdapter mTuitionAdapter;
    private RetrofitApi mRetrofitApi;
    private MaterialCalendarView _calendarView;
    private PowerSpinnerView mSpinnerCls;
    private ChipGroup chipGroupLegend;  //범례

    private Calendar calendar;
    String strYear = "";
    String strMonth = "";
    private SimpleDateFormat yearFormat, monthFormat;

    private ArrayList<PayListItem> mTuitionList = new ArrayList<>();

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private int _clsCode = 0;
    private String _clsName = "";

    private final String MAN = "M";
    private final String WOMAN = "F";

    private String currentYear = "";
    private String currentMonth = "";
    private String currentDate = "";

    private static final int ADD = 1;
    private static final int SUBTRACT = -1;
    private static final String NEXT = "CLICK_NEXT";
    private static final String PREVIOUS = "CLICK_PREVIOUS";

    private static final String WEB_VIEW_URL = "https://www.shinhandamoa.com/common/login#payer";

    private ArrayList<AttendanceData> _attendanceList = new ArrayList<>();
    private ArrayList<TeacherClsData> mListCls = new ArrayList<>();
    SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
    //calendar
    private Set<AttendanceSummaryData> calendarDaySet = new HashSet<>();
    private ArrayList<HolidayData> calHolidayList = new ArrayList<>();
    private Set<CalendarDay> calHoliday = new HashSet<>();

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

    private final int CMD_GET_TUITION_INFO = 0;
    private static final int CMD_GET_STU_INFO = 1;
    private static final int CMD_GET_PARENT_NOTIFICATION_INFO = 2;
    private static final int CMD_GET_ATTENDANCE_INFO = 3;
    private static final int CMD_GET_CLS_INFO = 4;

    private Handler _handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ATTENDANCE_INFO:
                    requestGetAttendanceList();
                    break;
                case CMD_GET_CLS_INFO:
                    requestCls();
                    break;
                case CMD_GET_TUITION_INFO:
                    requestTuitionList(currentDate);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_student_info);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _handler.sendEmptyMessage(CMD_GET_TUITION_INFO);
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
    }

    private void startWebView(PayListItem item) {

        ClipboardManager clipMgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Account No Clipboard", ((TuitionHeaderData)item).accountNO);
        if (clipMgr != null) {
            clipMgr.setPrimaryClip(clipData);
            Toast.makeText(mContext, R.string.menu_stu_info_get_clipboard, Toast.LENGTH_SHORT).show();
        }
//        else {
//            Toast.makeText(mContext, "error: 클립보드 서비스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
//        }

        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, WEB_VIEW_URL);
        startActivity(intent);

    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_student_info);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.btn_consultation_request).setOnClickListener(this);
//        findViewById(R.id.layout_year_month).setOnClickListener(this);
//        findViewById(R.id.img_tuition_back).setOnClickListener(this);
//        findViewById(R.id.img_tuition_next).setOnClickListener(this);

//        mTvTotalPayment = findViewById(R.id.tv_total_payment);
//        mTvYear = findViewById(R.id.tv_year);
//        mTvMonth = findViewById(R.id.tv_month);
        mTvStuName = findViewById(R.id.tv_stu_info_name);
        mTvStuBirth = findViewById(R.id.tv_stu_info_birth);
        mTvStuCampus = findViewById(R.id.tv_stu_info_campus);
        mTvDeptName = findViewById(R.id.tv_stu_info_dept_name);
        mTvStGrade = findViewById(R.id.tv_stu_info_st_grade);
        mTvClstName = findViewById(R.id.tv_stu_info_clst_name);
        mTvStuPhoneNum = findViewById(R.id.tv_stu_info_stu_phone_num);
        mTvParentPhoneNum = findViewById(R.id.tv_stu_info_parent_phone_num);
        mTvTuitionEmpty = findViewById(R.id.tv_tuition_empty);
        //mTvBookPayEmpty = findViewById(R.id.tv_book_pay_empty);

        mImgStuProfile = findViewById(R.id.img_stu_info_profile);

        mSpinnerCls = findViewById(R.id.spinner_cls);
        mSpinnerCls.setIsFocusable(true);

        strYear = currentYear + getString(R.string.year);
        strMonth = currentMonth + getString(R.string.month);

        requestMemberInfo(_stuSeq, _stCode);

        mRecyclerTuition = findViewById(R.id.recycler_tuition);
        mTuitionAdapter = new TuitionListAdapter(mContext, mTuitionList, this::startWebView);
        mRecyclerTuition.setAdapter(mTuitionAdapter);

        setCalendar();
        initChipGroup();
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
//        SelBackgroundDecorator bgDec = new SelBackgroundDecorator(mContext);
        otherDec = new OtherMonthDecorator(mContext);
        otherSundayDec = new OtherSundayDecorator(mContext);
        otherSaturdayDec = new OtherSaturdayDecorator(mContext);
        holidayDec = new HolidayDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        selectionDec = new SelectionDecorator(mContext);
//        eventDecorator = new EventDecorator(mContext, new HashSet<CalendarDay>(Collections.<CalendarDay>emptyList()));
        attendDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ATTENDANCE);
        absenceDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ABSENCE);
        earlyLeaveDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.EARLY_LEAVE);
        tardyDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.TARDY);
        makeupClassDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.MAKEUP_CLASS);
        onlineLectureDecorator = new AttendanceDecorator(mContext, new HashSet<AttendanceSummaryData>(Collections.<AttendanceSummaryData>emptyList()), Constants.AttendanceStatus.ONLINE_LECTURE);
        CalendarDay today = CalendarDay.from(_selectedDate);
        todayDec.setSelectedDay(today);
//        bgDec.setSelectedDay(today);
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
                runOnUiThread( () -> _calendarView.setCurrentDate(newDate) );
                _selectedDate = newDate.getDate();

                LogMgr.e(TAG, "selDate = " + _selectedDate);
                _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
            }, currentYear, currentMonth);
        });
        _calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);

        _calendarView.setOnMonthChangedListener((view, date) -> {
            LogMgr.e(TAG, "calendarView onMonthChanged");
            Calendar calendar = date.getCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            _selectedDate = calendar.getTime();

//            setTvHolidayDate();

            otherDec.setSelectedDay(date);
            otherSundayDec.setSelectedDay(date);
            otherSaturdayDec.setSelectedDay(date);
            holidayDec.setSelectedDay(date);
            setDeco();
            view.invalidateDecorators();
            LogMgr.i(TAG, "DateTestMonth >> " + _selectedDate);
            _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
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
            Chip chip = new Chip(MenuStudentInfoActivity.this);
            chip.setText(attendance.getName());
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.bg_white)));
            chip.setChipStrokeWidth(0);
            chip.setCloseIconVisible(false);
            chip.setCheckable(true);
            chip.setChecked(true);
            chip.setCheckedIconVisible(true);
            chip.setClickable(false);
            chip.setFocusable(false);
            chip.setCheckedIcon(getDrawable(R.drawable.ic_vector_circle));
            chip.setCheckedIconTint(ColorStateList.valueOf(ContextCompat.getColor(mContext, attendance.getColorRes())));
            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipIconSize(getResources().getDimensionPixelSize(R.dimen.dot_sex_size));
            chipGroupLegend.addView(chip);
        }
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_consultation_request:
                startActivity(new Intent(mContext, ConsultationRequestActivity.class));
                break;
        }
    }

    private Set<TuitionData> acaName = new HashSet<>();
    private ArrayList<TuitionData> payList = new ArrayList<>();

    private void initHeaderData(ArrayList<TuitionData> item) {

        acaName.clear();
        payList.clear();

        acaName.addAll(item);
        payList.addAll(item);

        int payTotal = 0;

        for (TuitionData data : acaName) {
            LogMgr.e(TAG, "acaName: " + data.acaName);

            if (!TextUtils.isEmpty(data.acaName)){
                TuitionHeaderData headerData = new TuitionHeaderData();
                try {
                    for (TuitionData pay : payList) if (data.acaName.equals(pay.acaName)) payTotal += Integer.parseInt(pay.payment.replace(",", ""));
                }catch (NumberFormatException e){

                }


                headerData.acaName = data.acaName;
                headerData.accountNO = data.accountNO;
                headerData.payment = Utils.decimalFormat(payTotal);
                headerData.gubun = Constants.PayType.TUITION.getNameKor();
                mTuitionList.add(headerData);
                payTotal = 0;
            }else{
                TuitionHeaderData headerData = new TuitionHeaderData();

                try {
                    for (TuitionData pay : payList) if (data.acaName.equals(pay.acaName)) payTotal += Integer.parseInt(pay.payment.replace(",", ""));
                }catch (NumberFormatException e){

                }

                headerData.acaName = data.acaName;
                headerData.accountNO = data.accountNO;
                headerData.payment = Utils.decimalFormat(payTotal);
                headerData.gubun = Constants.PayType.BOOK_PAY.getNameKor();
                mTuitionList.add(headerData);
                payTotal = 0;
            }
        }
    }

    // 수강료, 교재비 조회
    private void requestTuitionList(String yearMonth){

        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getTuitionList(Utils.currentDate("yyyyMM"), 58516).enqueue(new Callback<TuitionResponse>() {
                @Override
                public void onResponse(Call<TuitionResponse> call, Response<TuitionResponse> response) {
                    if (mTuitionList != null && mTuitionList.size() > 0) mTuitionList.clear();
                    if (response.isSuccessful()) {

                        if (response.body() != null && response.body().data != null) {

                            mTuitionList.clear();

                            ArrayList<TuitionData> getData = response.body().data;
                            initHeaderData(getData);
                            getData.forEach(t -> t.isHeader());

                            LogMgr.e(TAG, "data2:" + getData.size());

                                for (TuitionData data : getData) {

                                    try {
                                        int payment = Integer.parseInt(data.payment);
                                        data.payment = Utils.decimalFormat(payment);

                                        mTuitionList.add(data);

                                        LogMgr.e(TAG, "data: " + data.acaName);

                                    } catch (NumberFormatException e) {
                                        LogMgr.e(TAG, "Payment is not a valid integer: " + data.payment);
                                    }
                                }

                            Collections.sort(mTuitionList);

                        } else {
                            LogMgr.e(TAG, "Response or ListData is null");
                        }
                    } else {
                        Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    }
                    if (mTuitionAdapter != null) mTuitionAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mTuitionList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(Call<TuitionResponse> call, Throwable t) {
                    if (mTuitionAdapter != null) mTuitionAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mTuitionList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestTuitionList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 원생 정보 조회
    private void requestMemberInfo(int stuSeq, int stCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.studentInfo(stuSeq, stCode).enqueue(new Callback<StudentInfoResponse>() {
                @Override
                public void onResponse(Call<StudentInfoResponse> call, Response<StudentInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            StudentInfo getData = new StudentInfo();
                            if (response.body() != null)  getData= response.body().data;

                            if (getData != null) {
                                if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용

                                    if (_stName != null) mTvStuName.setText(_stName); // 자녀선택화면의 이름

                                }else mTvStuName.setText(getData.name); // 원생 오리지널 이름

                                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                                SimpleDateFormat targetFormat = new SimpleDateFormat("yyMMdd", Locale.KOREA);

                                Date birth = originalFormat.parse(getData.birth);
                                if (birth != null){
                                    mTvStuBirth.setText(targetFormat.format(birth));
                                }

                                if (getData.acaName != null) { // 캠퍼스명
                                    PreferenceUtil.setAcaName(mContext, getData.acaName);
                                    mTvStuCampus.setText(getData.acaName);
                                    mTvStuCampus.setVisibility(View.VISIBLE);
                                }

                                if (getData.gender.equals(MAN)) mImgStuProfile.setImageResource(R.drawable.img_profile_man);
                                else mImgStuProfile.setImageResource(R.drawable.img_profile_woman);

                                mTvDeptName.setText(getData.deptName); // 부서
                                mTvStGrade.setText(getData.stGrade); // 학년
                                mTvClstName.setText(" / " + getData.clstName); // 학급

                                if (getData.stGrade.equals(getData.clstName)) mTvStGrade.setVisibility(View.GONE);

                                if (getData.deptName.equals("")) mTvDeptName.setVisibility(View.GONE);
                                if (getData.stGrade.equals("")) mTvStGrade.setVisibility(View.GONE);
                                if (getData.clstName.equals("")) mTvClstName.setVisibility(View.GONE);

                                String phoneNumber = formatPhoneNum(getData.phoneNumber, PreferenceUtil.getStuPhoneNum(mContext));
                                String parentPhoneNumber = formatPhoneNum(getData.parentPhoneNumber, PreferenceUtil.getParentPhoneNum(mContext));

                                mTvStuPhoneNum.setText(phoneNumber);
                                mTvParentPhoneNum.setText(parentPhoneNumber);
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                    _handler.sendEmptyMessage(CMD_GET_CLS_INFO);
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    _handler.sendEmptyMessage(CMD_GET_CLS_INFO);
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSpinnerTeacher(){
        try {
            List<String> sfNames = new ArrayList<>();

            for (TeacherClsData data : mListCls) sfNames.add(data.clsName);

            if (sfNames.size() > 0 && sfNames != null) mSpinnerCls.setText(sfNames.get(0));
            mSpinnerCls.setItems(sfNames);
            _clsName = mListCls.get(0).clsName;
            _clsCode = mListCls.get(0).clsCode;
            _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);

            mSpinnerCls.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
                _clsName = mListCls.get(newIndex).clsName;
                _clsCode = mListCls.get(newIndex).clsCode;
                LogMgr.e(TAG,
                                "\nclsName:" + _clsName +
                                "\nclsCode:" + _clsCode
                );
                _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
            });
        }catch (Exception e){}
    }

    // 원생 학급 정보 조회
    private void requestCls(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.requestTeacherCls(_stCode, Utils.currentDate("yyyyMM")).enqueue(new Callback<TeacherClsResponse>() {
                @Override
                public void onResponse(Call<TeacherClsResponse> call, Response<TeacherClsResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
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
                    _handler.sendEmptyMessage(CMD_GET_ATTENDANCE_INFO);
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
                                _attendanceList.addAll(getData);
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestGetAttendanceList() Exception : ", e.getMessage());
                    }finally{
//                        hideProgressDialog();
                        updateCalView();
                    }
                }

                @Override
                public void onFailure(Call<GetAttendanceInfoResponse> call, Throwable t) {
                    try {
                        if(_attendanceList != null && _attendanceList.size() > 0) {
                            _attendanceList.clear();
                        }
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        LogMgr.e(TAG, "requestGetAttendanceList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }finally{
//                        hideProgressDialog();
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

    public String formatPhoneNum(String phoneNum, String defaultNum){
        if (phoneNum != null && !phoneNum.isEmpty()){
            if (phoneNum.length() == 11) return Utils.formatNum(phoneNum).equals("") ? defaultNum : Utils.formatNum(phoneNum);
            else return phoneNum;
        }
        else return "";
    }
}