package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.common.Constants.DATE_FORMATTER_YYYY_MM_DD;
import static kr.jeet.edu.student.common.Constants.DATE_FORMATTER_YYYY_MM_DD_KOR;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.DatePickerFragment;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.request.CounselRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultationRequestActivity extends BaseActivity {

    private final static String TAG = "Consult Activity";

    private EditText mEtConsultContent;
    private TextView mTvCal;
    private PowerSpinnerView mSpinnerTeacher;

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
    private int minYear = 0;
    private int maxYear = 2100;

    private ArrayList<TeacherClsData> mListTeacher = new ArrayList<>();
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
            requestMemberInfo(_memberSeq, _stCodeParent);
        }
        else {
            _phoneNumber = PreferenceUtil.getStuPhoneNum(mContext);
            _memberName = PreferenceUtil.getStName(mContext);
            _writerName = PreferenceUtil.getStName(mContext);
        }

        mListTeacher.addAll(DataManager.getInstance().getClsListMap().values());

        _selectedDate = new Date();
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

    @Override
    void initView() {

        initData();

        findViewById(R.id.layout_calendar).setOnClickListener(this);

        mEtConsultContent = findViewById(R.id.et_consultation_content);
        mTvCal = findViewById(R.id.tv_consultation_request_cal);
        //mSpinnerTeacher = findViewById(R.id.spinner_teacher);

        mTvCal.setText(Utils.currentDate(DATE_FORMATTER_YYYY_MM_DD_KOR));

        //setSpinnerTeacher();
    }

    @Override
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
//        request.sfCode = _sfCode;
//        request.sfName = _sfName;
//        request.clsName = _clsName;
        //request.managerPhoneNumber = _managerPhoneNumber.replace("-", "");
        request.stCode = _stCode;
        request.sfCode = mInfo.sfCode;
        request.sfName = mInfo.sfName;
        request.clsName = mInfo.clsName;
        request.managerPhoneNumber = mInfo.phoneNumber;

        str = mEtConsultContent.getText().toString();

        request.memo = str;
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
                "\nsmsSender: " + request.smsSender
        );

        if (request.counselDate.equals("") ||
                request.memo.equals("")){
            Toast.makeText(mContext, R.string.please_content, Toast.LENGTH_SHORT).show();
        }else{
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

    private void requestMemberInfo(int stuSeq, int stCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.studentInfo(stuSeq, stCode).enqueue(new Callback<StudentInfoResponse>() {
                @Override
                public void onResponse(Call<StudentInfoResponse> call, Response<StudentInfoResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            StudentInfo getData = new StudentInfo();
                            getData = response.body().data;

                            //_memberName = getData.name;
                            _writerName = getData.name;

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