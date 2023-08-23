package kr.jeet.edu.student.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.TuitionListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.TuitionResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuStudentInfoActivity extends BaseActivity {

    private final String TAG = "studentInfo Activity";

    private TextView mTvTotalPayment, mTvYear, mTvMonth, mTvStuName, mTvStuBirth, mTvStuCampus, mTvStuPhoneNum, mTvParentPhoneNum, mTvDeptName, mTvStGrade, mTvClstName, mTvTuitionEmpty;
    private ImageView mImgStuProfile, imgBack, imgNext;

    private RecyclerView mRecyclerView;
    private TuitionListAdapter mAdapter;
    private RetrofitApi mRetrofitApi;

    private Calendar calendar;
    private SimpleDateFormat yearFormat, monthFormat;

    private ArrayList<TuitionData> mList = new ArrayList<>();

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;

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

        if (mList != null && mList.size() > 0) {
            LogMgr.e("requestTuitionList Event");
            requestTuitionList(currentDate);
        }
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



    /**
    * 수강료 조회하기 위한 tuitionDate 날짜 형식 : [yyyyMM] ex) 202306
     *
     *      deptName : 부서
     *      stGrade : 학년
     *      clstName : 학급
     *      acaName : 캠퍼스
     *      phoneNumber : 휴대폰 번호
    * */
    @Override
    void initView() {
        findViewById(R.id.btn_consultation_request).setOnClickListener(this);
        findViewById(R.id.layout_year_month).setOnClickListener(this);
        findViewById(R.id.img_tuition_back).setOnClickListener(this);
        findViewById(R.id.img_tuition_next).setOnClickListener(this);

        mTvTotalPayment = findViewById(R.id.tv_total_payment);
        mTvYear = findViewById(R.id.tv_year);
        mTvMonth = findViewById(R.id.tv_month);
        mTvStuName = findViewById(R.id.tv_stu_info_name);
        mTvStuBirth = findViewById(R.id.tv_stu_info_birth);
        mTvStuCampus = findViewById(R.id.tv_stu_info_campus);
        mTvDeptName = findViewById(R.id.tv_stu_info_dept_name);
        mTvStGrade = findViewById(R.id.tv_stu_info_st_grade);
        mTvClstName = findViewById(R.id.tv_stu_info_clst_name);
        mTvStuPhoneNum = findViewById(R.id.tv_stu_info_stu_phone_num);
        mTvParentPhoneNum = findViewById(R.id.tv_stu_info_parent_phone_num);
        mTvTuitionEmpty = findViewById(R.id.tv_tuition_empty);

        mImgStuProfile = findViewById(R.id.img_stu_info_profile);
        imgBack = findViewById(R.id.img_tuition_back);
        imgNext = findViewById(R.id.img_tuition_next);

        mTvYear.setText(currentYear + getString(R.string.year));
        mTvMonth.setText(currentMonth + getString(R.string.month));

        requestMemberInfo(_stuSeq, _stCode);
        requestTuitionList(currentDate);

        mRecyclerView = findViewById(R.id.recycler_tuition);
        mAdapter = new TuitionListAdapter(mContext, mList, this::startWebView);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void startWebView(TuitionData item) {

        ClipboardManager clipMgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Account No Clipboard", item.accountNO);
        clipMgr.setPrimaryClip(clipData);

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
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_consultation_request:
                startActivity(new Intent(mContext, ConsultationRequestActivity.class));
                break;

            case R.id.layout_year_month:
                Utils.yearMonthPicker(mContext, (month, year) -> selectYearMonth(year, month+1), Integer.parseInt(currentYear), Integer.parseInt(currentMonth)-1);
                break;

            case R.id.img_tuition_back:
                nextOrPrevious(SUBTRACT, PREVIOUS);
                break;

            case R.id.img_tuition_next:
                nextOrPrevious(ADD, NEXT);
                break;
        }
    }

    private void selectYearMonth(int year, int month){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);

        Locale currentLocale = Locale.getDefault();

        currentYear = String.valueOf(year);
        currentMonth = String.format(currentLocale, "%02d", month);
        currentDate = currentYear + currentMonth;

        mTvYear.setText(currentYear + getString(R.string.year));
        mTvMonth.setText(currentMonth + getString(R.string.month));

        requestTuitionList(currentDate);
    }

    private void nextOrPrevious(int num, String btnType){
        if (btnType.equals(PREVIOUS)){
            if (Integer.parseInt(yearFormat.format(calendar.getTime())) <= Constants.PICKER_MIN_YEAR &&
                    Integer.parseInt(monthFormat.format(calendar.getTime())) == 1) {
                return;
            }
        }
        if (btnType.equals(NEXT)){
            if (Integer.parseInt(yearFormat.format(calendar.getTime())) >= Constants.PICKER_MAX_YEAR &&
                    Integer.parseInt(monthFormat.format(calendar.getTime())) == 12 ) {
                return;
            }
        }

        calendar.add(Calendar.MONTH, num);

        currentYear = yearFormat.format(calendar.getTime());
        currentMonth = monthFormat.format(calendar.getTime());
        currentDate = currentYear + currentMonth;

        mTvYear.setText(currentYear + getString(R.string.year));
        mTvMonth.setText(currentMonth + getString(R.string.month));

        requestTuitionList(currentDate);
    }

    private void requestTuitionList(String yearMonth){

        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getTuitionList(yearMonth, _stCode).enqueue(new Callback<TuitionResponse>() {
                @Override
                public void onResponse(Call<TuitionResponse> call, Response<TuitionResponse> response) {
                    if (mList != null && mList.size() > 0) mList.clear();
                    try {
                        if (response.isSuccessful()) {

                            int addPayment = 0;

                            if (response.body() != null && response.body().data != null) {

                                for (TuitionData data : response.body().data) {

                                    try {
                                        int payment = Integer.parseInt(data.payment);
                                        addPayment += payment;
                                        data.payment = Utils.decimalFormat(payment);
                                        mList.add(data);

                                    } catch (NumberFormatException e) {
                                        LogMgr.e(TAG, "Payment is not a valid integer: " + data.payment);
                                    }
                                }
                                LogMgr.e("mListSize", mList.size()+"");

                                String totalPayment = Utils.decimalFormat(addPayment) + getString(R.string.won);
                                mTvTotalPayment.setText(totalPayment);

                            } else {
                                LogMgr.e(TAG, "Response or ListData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTuitionList() Exception: ", e.getMessage());
                    }
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(Call<TuitionResponse> call, Throwable t) {
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
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

                                String phoneNumber = "";
                                String parentPhoneNumber = "";

                                if (getData.phoneNumber.length() == 11) {
                                    phoneNumber =
                                            Utils.formatNum(getData.phoneNumber).equals("") ? PreferenceUtil.getStuPhoneNum(mContext) : Utils.formatNum(getData.phoneNumber);
                                }
                                else phoneNumber = getData.phoneNumber;

                                if (getData.parentPhoneNumber.length() == 11) {
                                    phoneNumber =
                                            Utils.formatNum(getData.parentPhoneNumber).equals("") ? PreferenceUtil.getParentPhoneNum(mContext) : Utils.formatNum(getData.parentPhoneNumber);
                                }
                                else parentPhoneNumber = getData.parentPhoneNumber;

                                mTvStuPhoneNum.setText(phoneNumber);
                                mTvParentPhoneNum.setText(parentPhoneNumber);

                                LogMgr.e("phoneTest", getData.phoneNumber.length() + ", " + getData.parentPhoneNumber.length());
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}