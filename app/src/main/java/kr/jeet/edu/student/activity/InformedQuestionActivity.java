package kr.jeet.edu.student.activity;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ClassPathListAdapter;
import kr.jeet.edu.student.adapter.PrefCheckListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ClassPathData;
import kr.jeet.edu.student.model.data.PrefAreaData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformedQuestionActivity extends BaseActivity {

    private String TAG = "Informed Question Activity";

    private TextView tvQuestionNo1, tvQuestionNo2, tvQuestionNo3, tvQuestionNo4, tvQuestionNo5, tvQuestionNo6;
    private EditText[] mEditList;
    private EditText mEtWeekLessonVol, mEtClassTerm, mEtLearningProc1, mEtLearningProc2, mEtLearningProc3, mEtAnyQuestion,
            mEtStTime1,  mEtStTime2,  mEtStTime3,  mEtStTime4, mEtStDate1, mEtStDate2, mEtStDate3, mEtStDate4;
    private RadioGroup rgSelDay, rgPrefArea;
    private RadioButton rbSelDay1, rbSelDay2, rbSelDay3, rbSelDay4, rbGiftedPref, rbGiftedNonPref;
    private RadioButton[] rbList;
    private PowerSpinnerView mSpinnerClass, mSpinnerProcess_1, mSpinnerProcess_2, mSpinnerProcess_3;
    private RecyclerView mRecyclerArea, mRecyclerSchool, mRecyclerClassPath;

    private ClassPathListAdapter mAdapterClass;
    private PrefCheckListAdapter mAdapterArea, mAdapterSchool;

    private ArrayList<ClassPathData> mListClass = new ArrayList<>();
    private ArrayList<PrefAreaData> mListArea = new ArrayList<>();
    private ArrayList<PrefAreaData> mListSchool = new ArrayList<>();
    private List<String> clsPath;

    private RetrofitApi mRetrofitApi;

    private LevelTestRequest request;
    private String selDay = "";
    private String selProcess1 = "";
    private String selProcess2 = "";
    private String selProcess3 = "";

    private int _memberSeq = 0;

    private static final String PREF= "Y";
    private static final String NON_PREF= "N";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informed_question);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){

        try {

            if(mListArea != null) mListArea.clear();
            String[] areas = {"응용", "심화", "사고력", "극심화", "경시", "교과연계학습"};
            for (String area : areas) mListArea.add(new PrefAreaData(area));

            if(mListSchool != null) mListSchool.clear();
            String[] schools = {"영재학교", "북과학고", "특성화고", "국제고", "외고", "일반고", "외대부고", "자사고", "기타"};
            for (String school : schools) mListSchool.add(new PrefAreaData(school));

            Intent intent= getIntent();
            if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_WRITE)){
                request = intent.getParcelableExtra(IntentParams.PARAM_TEST_RESERVE_WRITE);
            }
            _memberSeq = PreferenceUtil.getUserSeq(mContext);

        }catch (Exception e) {
            LogMgr.e(TAG, e.getMessage());
        }
    }

    @Override
    void initView() {
        findViewById(R.id.root_inform_question).setOnClickListener(this);
        findViewById(R.id.btn_informed_question_complete).setOnClickListener(this);
        //findViewById(R.id.btn_class_path_add).setOnClickListener(this);

        tvQuestionNo1 = findViewById(R.id.tv_question_no_1);
        tvQuestionNo2 = findViewById(R.id.tv_question_no_2);
        tvQuestionNo3 = findViewById(R.id.tv_question_no_3);
        tvQuestionNo4 = findViewById(R.id.tv_question_no_4);
        tvQuestionNo5 = findViewById(R.id.tv_question_no_5);
        tvQuestionNo6 = findViewById(R.id.tv_question_no_6);

        mEtLearningProc1 = findViewById(R.id.et_learning_process_1);
        mEtLearningProc2 = findViewById(R.id.et_learning_process_2);
        mEtLearningProc3 = findViewById(R.id.et_learning_process_3);
        mEtAnyQuestion = findViewById(R.id.et_any_question);
        mEtStTime1 = findViewById(R.id.et_st_time_1);
        mEtStTime2 = findViewById(R.id.et_st_time_2);
        mEtStTime3 = findViewById(R.id.et_st_time_3);
        mEtStTime4 = findViewById(R.id.et_st_time_4);
        mEtStDate1 = findViewById(R.id.et_st_date_1);
        mEtStDate2 = findViewById(R.id.et_st_date_2);
        mEtStDate3 = findViewById(R.id.et_st_date_3);
        mEtStDate4 = findViewById(R.id.et_st_date_4);

        mRecyclerArea = findViewById(R.id.recycler_pref_area);
        mRecyclerSchool = findViewById(R.id.recycler_pref_school);

        rgSelDay = findViewById(R.id.rg_informed_question_sel_day);
        rgPrefArea = findViewById(R.id.rg_gifted_center_pref_area);
        rbSelDay1 = findViewById(R.id.rb_informed_question_sel_day_1);
        rbSelDay2 = findViewById(R.id.rb_informed_question_sel_day_2);
        rbSelDay3 = findViewById(R.id.rb_informed_question_sel_day_3);
        rbSelDay4 = findViewById(R.id.rb_informed_question_sel_day_4);
        rbGiftedPref = findViewById(R.id.rb_gifted_center_pref);
        rbGiftedNonPref = findViewById(R.id.rb_gifted_center_non_pref);

        mSpinnerProcess_1 = findViewById(R.id.spinner_learning_process_1);
        mSpinnerProcess_2 = findViewById(R.id.spinner_learning_process_2);
        mSpinnerProcess_3 = findViewById(R.id.spinner_learning_process_3);

        setRecycler();
        setSpinner();

        mEditList = new EditText[]{mEtLearningProc1, mEtLearningProc2, mEtLearningProc3, mEtAnyQuestion,
                mEtStTime1, mEtStTime2, mEtStTime3, mEtStTime4, mEtStDate1, mEtStDate2, mEtStDate3, mEtStDate4};
        rbList = new RadioButton[]{rbSelDay1, rbSelDay2, rbSelDay3, rbSelDay4};

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) for (RadioButton rb : rbList) if (buttonView != rb) rb.setChecked(false);
        };

        for (RadioButton rb : rbList) rb.setOnCheckedChangeListener(listener);

        if (request.grade.contains(getString(R.string.informed_question_elementary))) {
            tvQuestionNo6.setVisibility(View.GONE);
            rgPrefArea.setVisibility(View.GONE);

        } else if (request.grade.contains(getString(R.string.informed_question_middle))) {
            tvQuestionNo4.setVisibility(View.GONE);
            mRecyclerArea.setVisibility(View.GONE);
            tvQuestionNo5.setVisibility(View.GONE);
            mRecyclerSchool.setVisibility(View.GONE);

        } else if (request.grade.contains(getString(R.string.informed_question_high))) {
            tvQuestionNo1.setVisibility(View.GONE);
            rgSelDay.setVisibility(View.GONE);
            tvQuestionNo4.setVisibility(View.GONE);
            mRecyclerArea.setVisibility(View.GONE);
            tvQuestionNo5.setVisibility(View.GONE);
            mRecyclerSchool.setVisibility(View.GONE);
            tvQuestionNo6.setVisibility(View.GONE);
            rgPrefArea.setVisibility(View.GONE);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.informed_question_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSpinner(){
        mSpinnerProcess_1.setText(getString(R.string.select));
        mSpinnerProcess_2.setText(getString(R.string.select));
        mSpinnerProcess_3.setText(getString(R.string.select));

        mSpinnerProcess_1.setOnTouchListener(spinnerTouchListener);
        mSpinnerProcess_2.setOnTouchListener(spinnerTouchListener);
        mSpinnerProcess_3.setOnTouchListener(spinnerTouchListener);

        mSpinnerProcess_1.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> selProcess1 = newItem.toString());
        mSpinnerProcess_2.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> selProcess2 = newItem.toString());
        mSpinnerProcess_3.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> selProcess3 = newItem.toString());

    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener spinnerTouchListener = (v, event) -> {
        Utils.clearFocus(mEditList);
        Utils.hideKeyboard(mContext, mEditList);
        return false;
    };

    private ArrayList<String> areaCheckList = new ArrayList<>();
    private ArrayList<String> SchoolCheckList = new ArrayList<>();

    private void setRecycler(){

        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(mContext);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);

        FlexboxLayoutManager fblManagers = new FlexboxLayoutManager(mContext);
        fblManagers.setFlexWrap(FlexWrap.WRAP);
        fblManagers.setFlexDirection(FlexDirection.ROW);
        fblManagers.setJustifyContent(JustifyContent.FLEX_START);

        mRecyclerArea.setLayoutManager(fblManager);
        mRecyclerSchool.setLayoutManager(fblManagers);

        for (PrefAreaData item : mListArea) areaCheckList.add("");
        mAdapterArea = new PrefCheckListAdapter( mContext, mListArea, (item, position) -> areaCheckList.set(position, item) );
        mRecyclerArea.setAdapter(mAdapterArea);

        for (PrefAreaData item : mListSchool) SchoolCheckList.add("");
        mAdapterSchool = new PrefCheckListAdapter(mContext, mListSchool, (item, position) -> SchoolCheckList.set(position, item) );
        mRecyclerSchool.setAdapter(mAdapterSchool);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.root_inform_question:
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
                break;

            case R.id.btn_informed_question_complete:
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
                requestTestReserve();
                break;
        }
    }

    private void requestTestReserve(){

        requestData();

        if (!checked()) return;

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.requestLevelTest(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            String getData = "";
                            getData= response.body().msg;

                            if (getData != null) LogMgr.i(TAG, getData);

                            Toast.makeText(mContext, R.string.informed_question_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent("finish_activity");
                            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
                            setResult(RESULT_OK, intent);
                            finish();

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestTestReserve() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestTestReserve() Exception : ", e.getMessage()); }

                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestTestReserve() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestTestReserve() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void requestData(){
        try {
            request.memberSeq = _memberSeq;

            if (rbSelDay1.isChecked()) selDay = "0";
            else if (rbSelDay2.isChecked()) selDay = "1";
            else if (rbSelDay3.isChecked()) selDay = "2";
            else if (rbSelDay4.isChecked()) selDay = "3";

            request.time1 = mEtStTime1.getText().toString();
            request.time2 = mEtStTime2.getText().toString();
            request.time3 = mEtStTime3.getText().toString();
            request.time4 = mEtStTime4.getText().toString();

            request.date1 = mEtStDate1.getText().toString();
            request.date2 = mEtStDate2.getText().toString();
            request.date3 = mEtStDate3.getText().toString();
            request.date4 = mEtStDate4.getText().toString();

            if (rgSelDay.getVisibility() == View.VISIBLE) request.wish = selDay; // 희망요일
            else request.wish = "";

            if (!selProcess1.equals(getString(R.string.select))
                    || !mEtLearningProc1.getText().toString().equals("")){
                request.process1 = selProcess1;
                request.processEtc1 = mEtLearningProc1.getText().toString();

            }
            if (!selProcess2.equals(getString(R.string.select))
                    || !mEtLearningProc2.getText().toString().equals("")){
                request.process2 = selProcess2;
                request.processEtc2 = mEtLearningProc2.getText().toString();

            }
            if (!selProcess3.equals(getString(R.string.select))
                    || !mEtLearningProc3.getText().toString().equals("")){
                request.process3 = selProcess3;
                request.processEtc3 = mEtLearningProc3.getText().toString();
            }

            String strStudy = "";
            StringBuilder studyBuilder = new StringBuilder();

            for (String value : areaCheckList) {
                if (value != null && !value.isEmpty()) {
                    if (studyBuilder.length() > 0) {
                        studyBuilder.append("^");
                    }
                    studyBuilder.append(value);
                }
            }
            strStudy = studyBuilder.toString();

            String strHighSchool = "";
            StringBuilder schoolBuilder = new StringBuilder();

            for (String value : SchoolCheckList) {
                if (value != null && !value.isEmpty()) {
                    if (schoolBuilder.length() > 0) {
                        schoolBuilder.append("^");
                    }
                    schoolBuilder.append(value);
                }
            }
            strHighSchool = schoolBuilder.toString();

            LogMgr.e("FormatTest", strStudy + ", " + strHighSchool);

            request.study = strStudy;
            request.highSchool = strHighSchool;

            if (rgPrefArea.getVisibility() == View.VISIBLE){
                if (rbGiftedPref.isChecked()) request.gifted = PREF;
                else if (rbGiftedNonPref.isChecked()) request.gifted = NON_PREF;

            }else request.gifted = "";

                    request.etc = mEtAnyQuestion.getText().toString();
            request.registerDate = currentDate();

            if (request != null){
                LogMgr.i(TAG+ "putData", "\nmemberSeq : " + request.memberSeq
                        + "\n학생이름 : " + request.name
                        + "\n생년월일 : " + request.birth
                        + "\n성별 : " + request.sex
                        + "\n주소 : " + request.address
                        + "\n상세주소 : " + request.addressSub
                        + "\n학교코드 : " + request.scCode
                        + "\n학년 : " + request.grade
                        + "\n학생 전화번호 : " + request.phoneNumber
                        + "\n학부모 연락처 : " + request.parentPhoneNumber
                        + "\n학부모 성함 : " + request.parentName
                        + "\n유입경로 : " + request.reason
                        + "\n테스트예약일 : " + request.reservationDate
                        + "\n레벨캠퍼스 비고 : " + request.bigo
                        + "\n현금영수증 : " + request.cashReceiptNumber
                        + "\n등록일 : " + request.registerDate
                        + "\n희망요일 : " + request.wish
                        + "\n주간수업량(시간) 학원 : " + request.time1
                        + "\n주간수업량(시간) 과외 : " + request.time2
                        + "\n주간수업량(시간) 가정학습(자기주도) : " + request.time3
                        + "\n주간수업량(시간) 구몬/눈높이/재능 : " + request.time4
                        + "\n수강기간 학원 : " + request.date1
                        + "\n수강기간 과외 : " + request.date2
                        + "\n수강기간 가정학습(자기주도) : " + request.date3
                        + "\n수강기간 구몬/눈높이/재능 : " + request.date4
                        + "\n진도 심화학습여부(1) 과정 : " + request.process1
                        + "\n진도 심화학습여부(1) 사용교재 : " + request.processEtc1
                        + "\n진도 심화학습여부(2) 과정 : " + request.process2
                        + "\n진도 심화학습여부(2) 사용교재 : " + request.processEtc2
                        + "\n진도 심화학습여부(3) 과정 : " + request.process3
                        + "\n진도 심화학습여부(3) 사용교재 : " + request.processEtc3
                        + "\n희망분야 : " + request.study
                        + "\n희망학교 : " + request.highSchool
                        + "\n영재센터 입학희망 : " + request.gifted
                        + "\n궁금사항 : " + request.etc
                        + "\ncheck1 : " + request.check1
                        + "\ncheck2 : " + request.check2
                        + "\ncheck3 : " + request.check3
                        + "\ncheck4 : " + request.check4
                );
            }

        }catch (Exception e){
            LogMgr.e(TAG + "Exception", e.getMessage());
        }
    }

    private boolean checked(){
        if (!mEtStTime1.getText().toString().equals("") && !mEtStDate1.getText().toString().equals("") ||
                !mEtStTime2.getText().toString().equals("") && !mEtStDate2.getText().toString().equals("") ||
                !mEtStTime3.getText().toString().equals("") && !mEtStDate3.getText().toString().equals("") ||
                !mEtStTime4.getText().toString().equals("") && !mEtStDate4.getText().toString().equals("")
        ){

            if (!mEtLearningProc1.getText().toString().equals("") ||
                    !mEtLearningProc2.getText().toString().equals("") ||
                    !mEtLearningProc3.getText().toString().equals("")){
                return true;

            }else {
                Toast.makeText(mContext, R.string.write_empty, Toast.LENGTH_SHORT).show();
                return false;
            }

        }else {
            Toast.makeText(mContext, R.string.write_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String currentDate(){
        Date currentDate = new Date(); // 현재 날짜 가져오기
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate); // 형식에 맞춰 날짜 문자열로 변환

        return formattedDate;
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
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
                requestTestReserve();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}