package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.common.Constants.DATE_FORMATTER_YYYY_MM_DD;
import static kr.jeet.edu.student.common.Constants.DATE_FORMATTER_YYYY_MM_DD_KOR;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.DatePickerFragment;
import kr.jeet.edu.student.model.data.BagData;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.request.CounselRequest;
import kr.jeet.edu.student.model.response.BagResponse;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.LimitableEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultationRequestActivity extends BaseActivity {

    private final static String TAG = "Consult Activity";

    private LimitableEditText mEtConsultContent;
    private TextView mTvCal;
    private PowerSpinnerView mSpinnerTeacher, mSpinnerBag;
    private NumberPicker npStartAmPm, npStartTime, npEndAmPm, npEndTime;

    private RetrofitApi mRetrofitApi;

    private int _memberSeq = 0;
    private int _stCodeParent = 0;
    private String _acaCode = "";
    private String _acaName = "";
    private String _acaTel = "";
    private int _stCode = 0;
    private String _sfName = "";
    private int _sfCode = 0;
    private String _clsName = "";
    private String _memberName = "";
    private String _writerName = "";
    private int _userGubun = 0;
    private String _phoneNumber = "";
    private String _managerPhoneNumber = "";
    Date _selectedDate;
    Date _selectedTime;
    private int minYear = 0;
    private int maxYear = 2100;

    private ArrayList<TeacherClsData> mListTeacher = new ArrayList<>();
    private List<BagData> mListBag = new ArrayList<>();
    private List<String> mListBagName = new ArrayList<>();
    private BagData _selectedBag = null;
    private TeacherClsData mInfo;
    private String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation_request);
        mContext = this;
        initAppbar();
        initView();
    }

    private void initData(){
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);
        _acaTel = PreferenceUtil.getAcaTel(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);

        if (_userGubun == Constants.USER_TYPE_PARENTS) {
            _phoneNumber = PreferenceUtil.getParentPhoneNum(mContext);
            _memberName = PreferenceUtil.getStName(mContext);
            _writerName = PreferenceUtil.getParentName(mContext);
        }
        else {
            _phoneNumber = PreferenceUtil.getStuPhoneNum(mContext);
            _memberName = PreferenceUtil.getStName(mContext);
            _writerName = PreferenceUtil.getStName(mContext);
        }

        mListTeacher.addAll(DataManager.getInstance().getClsListMap().values());

        _selectedDate = new Date();
        _selectedTime = new Date();
        Calendar calendar = Calendar.getInstance();
        minYear = calendar.get(Calendar.YEAR);

        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TeacherClsData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
            }
        }

        if(mInfo == null) finish();
    }

    void initView() {

        initData();

        findViewById(R.id.layout_calendar).setOnClickListener(this);

        mEtConsultContent = findViewById(R.id.et_consultation_content);
        mTvCal = findViewById(R.id.tv_consultation_request_cal);
        npStartAmPm = findViewById(R.id.picker_start_am_pm);
        npStartTime = findViewById(R.id.picker_start_time);
        npEndAmPm = findViewById(R.id.picker_end_am_pm);
        npEndTime = findViewById(R.id.picker_end_time);
        mSpinnerBag = findViewById(R.id.spinner_bag);

        npStartAmPm.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                view.clearFocus();
            }
        });


        mTvCal.setText(Utils.currentDate(DATE_FORMATTER_YYYY_MM_DD_KOR));

        setNumberPicker();
        requestBagList();
    }

    private void setNumberPicker() {
        setAmPm(npStartAmPm);
        setAmPm(npEndAmPm);
        setTime(npStartTime);
        setTime(npEndTime);
    }

    private void setAmPm(NumberPicker picker) { // 오전, 오후 설정
        String[] amPm = {"오전", "오후"};
        picker.setMinValue(0);
        picker.setMaxValue(1);
        picker.setDisplayedValues(amPm);

        Calendar calendar = Calendar.getInstance();

        int hour_24 = calendar.get(Calendar.HOUR_OF_DAY); // 현재시간 24시간 단위

        if (hour_24 <= 11) picker.setValue(0); // 오전
        else picker.setValue(1); // 오후
    }

    private void setTime(NumberPicker picker) { // 시간 설정
        int minValue = 0;
        int maxValue = 11;
        ArrayList<String> hours = new ArrayList<>();
        for (int i = minValue + 1; i <= maxValue + 1; i++) hours.add(i+"시");

        String[] hoursArray = hours.toArray(new String[0]);

        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setDisplayedValues(hoursArray);

        Calendar calendar = Calendar.getInstance();
        int hour_12 = calendar.get(Calendar.HOUR); // 현재시간 12시간 단위
        picker.setValue(hour_12 - 1);
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.menu_stu_info_consultation_request_header);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_calendar:
                showDatePicker();
                break;
        }
    }

    void showDatePicker() {
        DatePickerFragment.OnDateSetListener listener = (year, month, day) -> {
            String formattedDate = String.format(Locale.KOREA, getString(R.string.consultation_request_date), year, month + 1, day);
            mTvCal.setText(formattedDate);
        };
        DatePickerFragment datePickerDialog = new DatePickerFragment(listener, true, minYear, maxYear);

        Calendar calendar = Calendar.getInstance();
        String strDate = mTvCal.getText().toString();

        if (strDate.equals("")){
            calendar.setTime(_selectedDate);
        }else{

            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMATTER_YYYY_MM_DD_KOR, Locale.KOREA);
                Date date = null;
                date = sdf.parse(strDate);

                if (date != null) calendar.setTime(date);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        datePickerDialog.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show(getSupportFragmentManager(), "date");
    }

    private void putConsultRequest(){
        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        final int FORMAT_HOUR_12 = 12;
        final String AM = "오전";
        final String PM = "오후";

        String startAmPm = (npStartAmPm.getValue() == 0) ? "오전" : "오후";
        String endAmPm = (npEndAmPm.getValue() == 0) ? "오전" : "오후";

        int startHour = npStartTime.getValue() + 1;
        int endHour = npEndTime.getValue() + 1;

        // 오후인 경우 시간에 12 더하기
        if (startAmPm.equals(PM) && startHour != FORMAT_HOUR_12) startHour += FORMAT_HOUR_12;
        if (endAmPm.equals(PM) && endHour != FORMAT_HOUR_12) endHour += FORMAT_HOUR_12;

        // 오전 12시는 0시로 처리
        if (startAmPm.equals(AM) && startHour == FORMAT_HOUR_12) startHour = 0;
        if (endAmPm.equals(AM) && endHour == FORMAT_HOUR_12) endHour = 0;

        calendarStart.set(Calendar.HOUR_OF_DAY, startHour);
        calendarStart.set(Calendar.MINUTE, 0);

        calendarEnd.set(Calendar.HOUR_OF_DAY, endHour);
        calendarEnd.set(Calendar.MINUTE, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);
        String resultTime = sdf.format(calendarStart.getTime()) + "~" + sdf.format(calendarEnd.getTime());

        String str = "";

        CounselRequest request = new CounselRequest();
        request.memberSeq = _memberSeq;
        request.memberName = _memberName;
        request.userGubun = _userGubun;
        request.writerName = _writerName;

        str = Utils.reformFormatDate(mTvCal.getText().toString(), DATE_FORMATTER_YYYY_MM_DD_KOR, DATE_FORMATTER_YYYY_MM_DD);

        request.counselDate = str;
        request.acaCode = _acaCode;
        request.acaName = _acaName;
        request.stCode = _stCode;
        request.sfCode = mInfo.sfCode;
        request.sfName = mInfo.sfName;
        request.clsName = mInfo.clsName;
        request.managerPhoneNumber = mInfo.phoneNumber;
        request.callWishDate = resultTime;
        if (_selectedBag != null) {
            request.bagCode = _selectedBag.bagCode;
            if (_selectedBag.bagName != null) request.bagName = _selectedBag.bagName;
        }

        str = mEtConsultContent.getText();

        if (Utils.isEmptyContainSpace(str)) request.memo = str.trim();
        else request.memo = str;
        request.phoneNumber = _phoneNumber.replace("-", "");
        request.smsSender = _acaTel.replace("-", "");

        LogMgr.d(TAG + "requestData",
                "\nmemberSeq: " + request.memberSeq +
                "\nmemberName: " + request.memberName +
                "\nwriterName: " + request.writerName +
                "\nuserGubun: " + request.userGubun +
                "\ncounselDate: " + request.counselDate +
                "\nacaCode: " + request.acaCode +
                "\nacaName: " + request.acaName +
                "\nstCode: " + request.stCode +
                "\nsfCode: " + request.sfCode +
                "\nsfName: " + request.sfName +
                "\nmemo: " + request.memo +
                "\nclsName: " + request.clsName +
                "\nphoneNumber: " + request.phoneNumber +
                "\nsmsSender: " + request.smsSender +
                "\ncallWishDate: " + request.callWishDate +
                "\nbagCode: " + request.bagCode +
                "\nbagName: " + request.bagName
        );

        if (request.counselDate.isEmpty()) { // 상담희망일 미선택시
            Toast.makeText(mContext, R.string.please_date, Toast.LENGTH_SHORT).show();

        } else if(mListBagName == null || mListBagName.isEmpty()) { // 분류항목 데이터를 불러오지 못한 경우, 데이터 가져오기
            requestBagList();
            showMessageDialog(
                    getString(R.string.dialog_title_alarm),
                    getString(R.string.loading_bag_info),
                    v-> hideMessageDialog(),
                    null,
                    false
            );
        } else if (request.bagName == null || request.bagName.isEmpty()) { // 분류항목 미선택시
            Toast.makeText(mContext, R.string.please_bag, Toast.LENGTH_SHORT).show();
            if (!mSpinnerBag.isShowing()) mSpinnerBag.show();

        }
        // 상담 내용은 선택사항
//        else if (request.memo.isEmpty()) {
//            Toast.makeText(mContext, R.string.please_content, Toast.LENGTH_SHORT).show();
//            showKeyboard(mEtConsultContent.getEditText());
//
//        }
        else if(TextUtils.isEmpty(request.acaCode) || TextUtils.isEmpty(request.acaName)) { // 캠퍼스 데이터를 불러오지 못한 경우, 데이터 가져오기
            requestMemberInfo(_memberSeq, _stCode);
            showMessageDialog(
                    getString(R.string.dialog_title_alarm),
                    getString(R.string.loading_aca_info),
                    v-> hideMessageDialog(),
                    null,
                    false
            );
        } else {
            showProgressDialog();
            if(RetrofitClient.getInstance() != null) {
                mRetrofitApi = RetrofitClient.getApiInterface();
                mRetrofitApi.requestCounsel(request).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        try {
                            if(response.isSuccessful()) {
                                if(response.body() != null) {
                                    LogMgr.i(TAG, response.message());
                                    Toast.makeText(mContext, R.string.consultation_request_content_success, Toast.LENGTH_SHORT).show();

                                    mTvCal.setText("");
                                    mEtConsultContent.setText("");

                                    finish();
                                }
                            } else {
                                int code = response.code();
                                // TODO : 응답 코드에 따른 Toast 처리
                                if (code == RetrofitApi.RESPONSE_CODE_BINDING_ERROR) {

                                } else if (code == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {

                                }
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                                LogMgr.e(TAG, "putConsultRequest() errBody : " + response.errorBody().string());
                            }

                        }catch (Exception e) { LogMgr.e(TAG + "putConsultRequest() Exception : ", e.getMessage()); }
                        hideProgressDialog();
                    }
                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        try { LogMgr.e(TAG, "putConsultRequest() onFailure >> " + t.getMessage()); }
                        catch (Exception e) { LogMgr.e(TAG + "putConsultRequest() Exception : ", e.getMessage()); }
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                });
            }
        }
    }

    private void requestMemberInfo(int memberSeq, int stCode){
        showProgressDialog();
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.studentInfo(memberSeq, stCode).enqueue(new Callback<StudentInfoResponse>() {
                @Override
                public void onResponse(Call<StudentInfoResponse> call, Response<StudentInfoResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            StudentInfo getData = new StudentInfo();
                            getData = response.body().data;

                            if (getData != null) {
                                _acaCode = getData.acaCode;
                                _acaName = getData.acaName;
                                PreferenceUtil.setAcaCode(mContext, getData.acaCode);
                                PreferenceUtil.setAcaName(mContext, getData.acaName);
                            }

                        }else{
                            // TODO : 응답 코드에 따른 Toast 처리
                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR) {

                            } else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {

                            }
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setBagSpinner() {
        if (mListBag != null && mListBag.size() > 0) mListBagName = mListBag.stream().map(t -> t.bagName).collect(Collectors.toList());
        mSpinnerBag.setItems(mListBagName);
        mSpinnerBag.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            BagData selectedData = null;
            Optional<BagData> optional = mListBag.stream().filter(t -> t.bagName == newItem).findFirst();
            if(optional.isPresent()) selectedData = (BagData) optional.get();

            _selectedBag = selectedData;
        });
        mSpinnerBag.setSpinnerOutsideTouchListener((view, motionEvent) -> mSpinnerBag.dismiss());
        mSpinnerBag.setLifecycleOwner(this);
    }

    private void requestBagList() {
        showProgressDialog();
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBag().enqueue(new Callback<BagResponse>() {
                @Override
                public void onResponse(Call<BagResponse> call, Response<BagResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            List<BagData> getData;
                            getData = response.body().data;

                            if (getData != null && !getData.isEmpty()) {
                                if (mListBag.size() > 0) mListBag.clear();
                                mListBag.addAll(getData);
                                setBagSpinner();
                            }

                        }else{
                            // TODO : 응답 코드에 따른 Toast 처리
                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR) {

                            } else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {

                            }
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestBagList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestBagList() Exception : ", e.getMessage()); }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BagResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestBagList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestBagList() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), 0);

            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mContext.getResources().getDisplayMetrics());
            span.setSpan(new AbsoluteSizeSpan(sizeInPx), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            item.setTitle(span);
        }catch(Exception ex){}

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_complete:
                putConsultRequest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}