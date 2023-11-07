package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ClassPathListAdapter;
import kr.jeet.edu.student.adapter.PrefCheckListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ClassPathData;
import kr.jeet.edu.student.model.data.PrefAreaData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.HttpUtils;
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
    private ArrayList<String> mListArea = new ArrayList<>();
    private ArrayList<String> mListSchool = new ArrayList<>();

    private ArrayList<String> areaCheckList = new ArrayList<>();
    private ArrayList<String> SchoolCheckList = new ArrayList<>();

    private List<String> clsPath;

    private RetrofitApi mRetrofitApi;

    private LevelTestRequest request;
    private String selDay = "";
    private int selProcess1 = -1;
    private int selProcess2 = -1;
    private int selProcess3 = -1;

    private int _memberSeq = 0;
    private int _userGubun = -1;
    private String _userType = "";
    private int _loginType = -1;
    private int _childCnt = -1;

    private static final String PREF= "Y";
    private static final String NON_PREF= "N";

    private TestReserveData mInfo;
    private String writeMode = "";

    private final String NOT_SEL = "선택";
    private int testType = -1;
    private AppCompatActivity mActivity;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if (intent!= null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_SAVED)) {
                LogMgr.e(TAG, "resultLauncher Event SAVE");
                boolean saved = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_SAVED, false);
                if (saved) {
                    if (intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
                        }else{
                            mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                        }
                    }
                }

            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informed_question);
        mActivity = this;
        mContext = this;

        if (savedInstanceState != null){
            LogMgr.e("onSaveEvent2");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mInfo = savedInstanceState.getParcelable(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
            }else{
                mInfo = savedInstanceState.getParcelable(IntentParams.PARAM_LIST_ITEM);
            }
        }

        Intent intent = getIntent();
        if (intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
            }else{
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
            }
        }else{
            mInfo = new TestReserveData();
        }

        initView();
        initAppbar();
    }

    private void initData(){
        try {
            if(mListArea != null) mListArea.clear();
            String[] areas = {"응용", "심화", "사고력", "극심화", "경시", "교과연계학습"};
            for (String area : areas)
                if (mListArea != null) {
                    mListArea.add(area);
                }

            if(mListSchool != null) mListSchool.clear();
            String[] schools = {"영재학교", "북과학고", "특성화고", "국제고", "외고", "일반고", "외대부고", "자사고", "기타"};
            for (String school : schools) {
                assert mListSchool != null;
                mListSchool.add(school);
            }

            _memberSeq = PreferenceUtil.getUserSeq(mContext);
            _userGubun = PreferenceUtil.getUserGubun(mContext);
            _userType = PreferenceUtil.getUserType(mContext);
            _loginType = PreferenceUtil.getLoginType(mContext);
            _childCnt = PreferenceUtil.getNumberOfChild(mContext);

            LogMgr.e(TAG, "childCnt: " + _childCnt);

            Intent intent= getIntent();

            if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_WRITE)){
                request = intent.getParcelableExtra(IntentParams.PARAM_TEST_RESERVE_WRITE);
            }

            if (intent.hasExtra(IntentParams.PARAM_WRITE_MODE)){

                writeMode = intent.getStringExtra(IntentParams.PARAM_WRITE_MODE);

                if (intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
                    LogMgr.e(TAG, "Event get mInfo");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
                    }else{
                        mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                    }
                }else{
                    mInfo = new TestReserveData();
                }
            }
            if (intent.hasExtra(IntentParams.PARAM_TEST_TYPE)){
                testType = intent.getIntExtra(IntentParams.PARAM_TEST_TYPE, testType);
            }
        }catch (Exception e) {
            LogMgr.e(TAG, e.getMessage());
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        if (writeMode.equals(Constants.WRITE_EDIT)) customAppbar.setTitle(R.string.informed_question_update_title);
        else customAppbar.setTitle(R.string.informed_question_title);
        customAppbar.setLogoVisible(true);
//        customAppbar.setLogoClickable(true);
        LogMgr.e(TAG, "stCode = " + PreferenceUtil.getUserSTCode(mContext));
        boolean isHomeBtnEnable = //Home 이동 가능한 Case 는
                PreferenceUtil.getUserGubun(mContext) == Constants.USER_TYPE_STUDENT    //학생이거나
                        || (DataManager.getInstance().isSelectedChild)    // 자녀를 선택했거나
                        || (PreferenceUtil.getUserSTCode(mContext) == 0)    //비회원이거나
                ;
        customAppbar.setLogoClickable(isHomeBtnEnable);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        initData();
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
        if (writeMode.equals(Constants.WRITE_EDIT)) {
            setView();
            LogMgr.e(TAG, "EVENT EDIT");
        } else{
            if (mInfo != null) {
                setView();
                LogMgr.e(TAG, "EVENT WRITE1");
            }
        }
        setSpinner();

        mEditList = new EditText[]{mEtLearningProc1, mEtLearningProc2, mEtLearningProc3, mEtAnyQuestion,
                mEtStTime1, mEtStTime2, mEtStTime3, mEtStTime4, mEtStDate1, mEtStDate2, mEtStDate3, mEtStDate4};
        rbList = new RadioButton[]{rbSelDay1, rbSelDay2, rbSelDay3, rbSelDay4};

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) for (RadioButton rb : rbList) if (buttonView != rb) rb.setChecked(false);
        };

        for (RadioButton rb : rbList) rb.setOnCheckedChangeListener(listener);

        if (request.grade != null){
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
    }

    private void setView(){
        if (mInfo == null) finish();

        rbList = new RadioButton[]{rbSelDay1, rbSelDay2, rbSelDay3, rbSelDay4};

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) for (RadioButton rb : rbList) if (buttonView != rb) rb.setChecked(false);
        };

        for (RadioButton rb : rbList) rb.setOnCheckedChangeListener(listener);

        if (mInfo.wish != null){
            switch (mInfo.wish) {
                case "0":
                    rbSelDay1.setChecked(true);
                    break;
                case "1":
                    rbSelDay2.setChecked(true);
                    break;
                case "2":
                    rbSelDay3.setChecked(true);
                    break;
                case "3":
                    rbSelDay4.setChecked(true);
                    break;
                default:
                    break;
            }
        }

        mEtStTime1.setText(Utils.getStr(mInfo.time1));
        mEtStTime2.setText(Utils.getStr(mInfo.time2));
        mEtStTime3.setText(Utils.getStr(mInfo.time3));
        mEtStTime4.setText(Utils.getStr(mInfo.time4));

        mEtStDate1.setText(Utils.getStr(mInfo.date1));
        mEtStDate2.setText(Utils.getStr(mInfo.date2));
        mEtStDate3.setText(Utils.getStr(mInfo.date3));
        mEtStDate4.setText(Utils.getStr(mInfo.date4));

        mSpinnerProcess_1.setText(TextUtils.isEmpty(mInfo.processText1) ? getString(R.string.select) : isNotSel(Utils.getStr(mInfo.processText1)));
        mSpinnerProcess_2.setText(TextUtils.isEmpty(mInfo.processText2) ? getString(R.string.select) : isNotSel(Utils.getStr(mInfo.processText2)));
        mSpinnerProcess_3.setText(TextUtils.isEmpty(mInfo.processText3) ? getString(R.string.select) : isNotSel(Utils.getStr(mInfo.processText3)));

        selProcess1 = TextUtils.isEmpty(mInfo.process1) ? -1 : Integer.parseInt(mInfo.process1);
        selProcess2 = TextUtils.isEmpty(mInfo.process2) ? -1 : Integer.parseInt(mInfo.process2);
        selProcess3 = TextUtils.isEmpty(mInfo.process3) ? -1 : Integer.parseInt(mInfo.process3);

        //LogMgr.e(TAG, "selProcess1: " + selProcess1);

        mEtLearningProc1.setText(Utils.getStr(mInfo.processEtc1));
        mEtLearningProc2.setText(Utils.getStr(mInfo.processEtc2));
        mEtLearningProc3.setText(Utils.getStr(mInfo.processEtc3));

        int count = 0;
        for (String item : mListArea) areaCheckList.add("");
        String[] parts = Utils.getStr(mInfo.study).split("\\^");
        for (int i = 0; i < areaCheckList.size() && count < parts.length; i++) {
            if (mListArea.size() > i && mListArea.get(i).equals(parts[count])) {
                areaCheckList.set(i, parts[count]);
                count++;
            }
        }
        mAdapterArea.notifyDataSetChanged();

        count = 0;

        for (String item : mListSchool) SchoolCheckList.add("");
        parts = Utils.getStr(mInfo.highSchool).split("\\^");
        for (int i = 0; i < SchoolCheckList.size() && count < parts.length; i++) {
            if (mListSchool.size() > i && mListSchool.get(i).equals(parts[count])) {
                SchoolCheckList.set(i, parts[count]);
                count++;
            }
        }
        mAdapterSchool.notifyDataSetChanged();

        if (mInfo.gifted != null){
            if (mInfo.gifted.equals("Y")) rbGiftedPref.setChecked(true);
            else rbGiftedNonPref.setChecked(true);
        }

        mEtAnyQuestion.setText(Utils.getStr(mInfo.etc));
    }

    private String isNotSel(String s){
        if (s.equals("미선택")) s = s.replace("미", "");
        return s;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSpinner(){

        mSpinnerProcess_1.setOnTouchListener(spinnerTouchListener);
        mSpinnerProcess_2.setOnTouchListener(spinnerTouchListener);
        mSpinnerProcess_3.setOnTouchListener(spinnerTouchListener);

        mSpinnerProcess_1.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            //selProcess1 = newItem.toString();
            if (newIndex > 0) selProcess1 = newIndex;
            else selProcess1 = -1;
            LogMgr.e("EVENT", selProcess1+"");
        });
        mSpinnerProcess_2.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            //selProcess2 = newItem.toString();
            if (newIndex > 0) selProcess2 = newIndex;
            else selProcess2 = -1;
        });
        mSpinnerProcess_3.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            //selProcess3 = newItem.toString();
            if (newIndex > 0) selProcess3 = newIndex;
            else selProcess3 = -1;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener spinnerTouchListener = (v, event) -> {
        Utils.clearFocus(mEditList);
        Utils.hideKeyboard(mContext, mEditList);
        return false;
    };

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

        if (!writeMode.equals(Constants.WRITE_EDIT)) for (String item : mListArea) areaCheckList.add("");
        mAdapterArea = new PrefCheckListAdapter( mContext, mListArea, areaCheckList, (item, position) -> areaCheckList.set(position, item) );
        mRecyclerArea.setAdapter(mAdapterArea);

        if (!writeMode.equals(Constants.WRITE_EDIT)) for (String item : mListSchool) SchoolCheckList.add("");
        mAdapterSchool = new PrefCheckListAdapter( mContext, mListSchool, SchoolCheckList, (item, position) -> SchoolCheckList.set(position, item) );
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
                if (checked()) requestTestReserve();
                break;
        }
    }

    private void requestTestReserve(){
        showProgressDialog();
        requestData();

        if(RetrofitClient.getInstance() != null) {

            RetrofitClient.getApiInterface().requestLevelTest(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        hideProgressDialog();
                        if (response.isSuccessful() && response.body() != null){
                            String getData = "";
                            getData= response.body().msg;

                            if (getData != null) LogMgr.i(TAG, getData);

                            Intent intent = new Intent(Constants.FINISH_COMPLETE);

                            if (writeMode.equals(Constants.WRITE_EDIT)){
                                intent.putExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, true);
                                intent.putExtra(IntentParams.PARAM_SUCCESS_DATA, request);
                                setResult(RESULT_OK, intent);
                                finish();
                                Toast.makeText(mContext, R.string.informed_question_update_success, Toast.LENGTH_SHORT).show();
                            }else{
                                if (testType == Constants.LEVEL_TEST_TYPE_NEW_CHILD) { // 신규원생 추가하는 경우
                                    if (_userType.equals(Constants.MEMBER)) {
                                        if (_childCnt < 1) {
                                            HttpUtils.requestLogOut(mActivity);
                                        } else {
                                            intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD, true);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                            Toast.makeText(mContext, R.string.informed_question_success, Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        HttpUtils.requestLogOut(mActivity);
                                    }

                                } else {
                                    if (_userType.equals(Constants.MEMBER)) {
                                        intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        Toast.makeText(mContext, R.string.informed_question_success, Toast.LENGTH_SHORT).show();
                                    } else {
//                                            if (_loginType == Constants.LOGIN_TYPE_NORMAL) {
//                                                HttpUtils.requestLogin(mActivity);
//                                            }
//                                            else {
//                                                HttpUtils.requestLoginFromSns(mActivity);
//                                            }
                                        HttpUtils.requestLogOut(mActivity);
                                    }
                                }
                            }

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestTestReserve() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestTestReserve() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestTestReserve() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestTestReserve() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void requestData(){
        try {

            if (writeMode.equals(Constants.WRITE_EDIT)) request.seq = mInfo.seq+"";

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

            request.process1 = selProcess1;
            request.processEtc1 = mEtLearningProc1.getText().toString();
            if (request.process1 == -1) request.processText1 = "미선택";
            else request.processText1 = mSpinnerProcess_1.getText().toString();

            request.process2 = selProcess2;
            request.processEtc2 = mEtLearningProc2.getText().toString();
            if (request.process2 == -1) request.processText2 = "미선택";
            else request.processText2 = mSpinnerProcess_2.getText().toString();

            request.process3 = selProcess3;
            request.processEtc3 = mEtLearningProc3.getText().toString();
            if (request.process3 == -1) request.processText3 = "미선택";
            else request.processText3 = mSpinnerProcess_3.getText().toString();

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

            LogMgr.i(TAG, "areaList: " + areaCheckList.toString() + "\nscList: " + SchoolCheckList.toString());

            for (String value : SchoolCheckList) {
                if (value != null && !value.isEmpty()) {
                    if (schoolBuilder.length() > 0) {
                        schoolBuilder.append("^");
                    }
                    schoolBuilder.append(value);
                }
            }
            strHighSchool = schoolBuilder.toString();

            if (mRecyclerArea.getVisibility() == View.VISIBLE) request.study = Utils.getStr(strStudy);
            else request.study = "";

            if (mRecyclerSchool.getVisibility() == View.VISIBLE) request.highSchool = Utils.getStr(strHighSchool);
            else request.highSchool = "";

            if (rgPrefArea.getVisibility() == View.VISIBLE){
                if (rbGiftedPref.isChecked()) request.gifted = PREF;
                else if (rbGiftedNonPref.isChecked()) request.gifted = NON_PREF;

            }else request.gifted = "";

                    request.etc = mEtAnyQuestion.getText().toString();
            request.registerDate = currentDate();

            if (writeMode.equals(Constants.WRITE_EDIT)){
                request.check1 = mInfo.check1;
                request.check2 = mInfo.check2;
                request.check3 = mInfo.check3;
                request.check4 = mInfo.check4;
            }


            if (request != null){
                LogMgr.i(TAG+ "putData", "\nmemberSeq : " + request.memberSeq
                        + "\nseq : " + request.seq
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
                        + "\n진도 심화학습여부(1) 과정 텍스트 : " + request.processText1
                        + "\n진도 심화학습여부(2) 과정 : " + request.process2
                        + "\n진도 심화학습여부(2) 사용교재 : " + request.processEtc2
                        + "\n진도 심화학습여부(2) 과정 텍스트 : " + request.processText2
                        + "\n진도 심화학습여부(3) 과정 : " + request.process3
                        + "\n진도 심화학습여부(3) 사용교재 : " + request.processEtc3
                        + "\n진도 심화학습여부(3) 과정 텍스트 : " + request.processText3
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
                    !mEtLearningProc3.getText().toString().equals("")
            ){
                return true;
            }else{
                if (mEtLearningProc1.getText().toString().equals("")){
                    mEtLearningProc1.requestFocus();
                    Utils.showKeyboard(mContext, mEtLearningProc1);

                } else if (mEtLearningProc2.getText().toString().equals("")){
                    mEtLearningProc2.requestFocus();
                    Utils.showKeyboard(mContext, mEtLearningProc2);

                } else if (mEtLearningProc3.getText().toString().equals("")){
                    mEtLearningProc3.requestFocus();
                    Utils.showKeyboard(mContext, mEtLearningProc3);
                }
                Toast.makeText(mContext, R.string.write_process_empty, Toast.LENGTH_SHORT).show();
                return false;
            }

        }else{
            if (mEtStTime1.getText().toString().equals("")) {
                checkEtStudy(mEtStTime1);

            } else if (mEtStDate1.getText().toString().equals("")){
                checkEtStudy(mEtStDate1);

            } else if (mEtStTime2.getText().toString().equals("")) {
                checkEtStudy(mEtStTime2);

            } else if (mEtStDate2.getText().toString().equals("")){
                checkEtStudy(mEtStDate2);

            } else if (mEtStTime3.getText().toString().equals("")) {
                checkEtStudy(mEtStTime3);

            }else if(mEtStDate3.getText().toString().equals("")){
                checkEtStudy(mEtStDate3);

            } else if (mEtStTime4.getText().toString().equals("")) {
                checkEtStudy(mEtStTime4);

            }else if (mEtStDate4.getText().toString().equals("")){
                checkEtStudy(mEtStDate4);

            }
            Toast.makeText(mContext, R.string.write_before_study_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void saveData(){
        try {
            mInfo.memberSeq = _memberSeq;

            if (rbSelDay1.isChecked()) selDay = "0";
            else if (rbSelDay2.isChecked()) selDay = "1";
            else if (rbSelDay3.isChecked()) selDay = "2";
            else if (rbSelDay4.isChecked()) selDay = "3";

            mInfo.time1 = mEtStTime1.getText().toString();
            mInfo.time2 = mEtStTime2.getText().toString();
            mInfo.time3 = mEtStTime3.getText().toString();
            mInfo.time4 = mEtStTime4.getText().toString();

            mInfo.date1 = mEtStDate1.getText().toString();
            mInfo.date2 = mEtStDate2.getText().toString();
            mInfo.date3 = mEtStDate3.getText().toString();
            mInfo.date4 = mEtStDate4.getText().toString();

            if (rgSelDay.getVisibility() == View.VISIBLE) mInfo.wish = selDay; // 희망요일
            else mInfo.wish = "";

            mInfo.process1 = selProcess1+"";
            mInfo.processEtc1 = mEtLearningProc1.getText().toString();
            LogMgr.e(TAG, "process1 value" + mInfo.process1);
            if (mInfo.process1.equals("-1")) mInfo.processText1 = getString(R.string.select);
            else mInfo.processText1 = mSpinnerProcess_1.getText().toString();
            LogMgr.e(TAG, "processText1 value" + mInfo.processText1);

            mInfo.process2 = selProcess2+"";
            mInfo.processEtc2 = mEtLearningProc2.getText().toString();
            if (mInfo.process2.equals("-1")) mInfo.processText2 = getString(R.string.select);
            else mInfo.processText2 = mSpinnerProcess_2.getText().toString();

            mInfo.process3 = selProcess3+"";
            mInfo.processEtc3 = mEtLearningProc3.getText().toString();
            if (mInfo.process3.equals("-1")) mInfo.processText3 = getString(R.string.select);
            else mInfo.processText3 = mSpinnerProcess_3.getText().toString();

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

            LogMgr.i(TAG, "areaList: " + areaCheckList.toString() + "\nscList: " + SchoolCheckList.toString());

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

            if (mRecyclerArea.getVisibility() == View.VISIBLE) mInfo.study = strStudy;
            else mInfo.study = "";

            if (mRecyclerSchool.getVisibility() == View.VISIBLE) mInfo.highSchool = strHighSchool;
            else mInfo.highSchool = "";

            if (rgPrefArea.getVisibility() == View.VISIBLE) {
                if (rbGiftedPref.isChecked()) mInfo.gifted = PREF;
                else if (rbGiftedNonPref.isChecked()) mInfo.gifted = NON_PREF;
            } else mInfo.gifted = "";

            mInfo.etc = mEtAnyQuestion.getText().toString();
            mInfo.registerDate = currentDate();

            if (writeMode.equals(Constants.WRITE_EDIT)) {
                request.check1 = mInfo.check1;
                request.check2 = mInfo.check2;
                request.check3 = mInfo.check3;
                request.check4 = mInfo.check4;
            }

            if (mInfo != null) {
                LogMgr.i(TAG + "putData mInfo", "\nmemberSeq : " + mInfo.memberSeq
                        + "\nseq : " + mInfo.seq
                        + "\n학생이름 : " + mInfo.name
                        + "\n생년월일 : " + mInfo.birth
                        + "\n성별 : " + mInfo.sex
                        + "\n주소 : " + mInfo.address
                        + "\n상세주소 : " + mInfo.addressSub
                        + "\n학교코드 : " + mInfo.scCode
                        + "\n학년 : " + mInfo.grade
                        + "\n학생 전화번호 : " + mInfo.phoneNumber
                        + "\n학부모 연락처 : " + mInfo.parentPhoneNumber
                        + "\n학부모 성함 : " + mInfo.parentName
                        + "\n유입경로 : " + mInfo.reason
                        + "\n테스트예약일 : " + mInfo.reservationDate
                        + "\n레벨캠퍼스 비고 : " + mInfo.bigo
                        + "\n현금영수증 : " + mInfo.cashReceiptNumber
                        + "\n등록일 : " + mInfo.registerDate
                        + "\n희망요일 : " + mInfo.wish
                        + "\n주간수업량(시간) 학원 : " + mInfo.time1
                        + "\n주간수업량(시간) 과외 : " + mInfo.time2
                        + "\n주간수업량(시간) 가정학습(자기주도) : " + mInfo.time3
                        + "\n주간수업량(시간) 구몬/눈높이/재능 : " + mInfo.time4
                        + "\n수강기간 학원 : " + mInfo.date1
                        + "\n수강기간 과외 : " + mInfo.date2
                        + "\n수강기간 가정학습(자기주도) : " + mInfo.date3
                        + "\n수강기간 구몬/눈높이/재능 : " + mInfo.date4
                        + "\n진도 심화학습여부(1) 과정 : " + mInfo.process1
                        + "\n진도 심화학습여부(1) 사용교재 : " + mInfo.processEtc1
                        + "\n진도 심화학습여부(1) 과정 텍스트 : " + mInfo.processText1
                        + "\n진도 심화학습여부(2) 과정 : " + mInfo.process2
                        + "\n진도 심화학습여부(2) 사용교재 : " + mInfo.processEtc2
                        + "\n진도 심화학습여부(2) 과정 텍스트 : " + mInfo.processText2
                        + "\n진도 심화학습여부(3) 과정 : " + mInfo.process3
                        + "\n진도 심화학습여부(3) 사용교재 : " + mInfo.processEtc3
                        + "\n진도 심화학습여부(3) 과정 텍스트 : " + mInfo.processText3
                        + "\n희망분야 : " + mInfo.study
                        + "\n희망학교 : " + mInfo.highSchool
                        + "\n영재센터 입학희망 : " + mInfo.gifted
                        + "\n궁금사항 : " + mInfo.etc
                        + "\ncheck1 : " + mInfo.check1
                        + "\ncheck2 : " + mInfo.check2
                        + "\ncheck3 : " + mInfo.check3
                        + "\ncheck4 : " + mInfo.check4
                );
            }

        }catch (Exception e){}

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveData();
        outState.putParcelable(IntentParams.PARAM_LIST_ITEM, mInfo);
        LogMgr.e(TAG,"onSaveEvent");
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        saveData();
        Intent intent = getIntent();

        if (intent != null){
            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_SAVED, true);
            intent.putExtra(IntentParams.PARAM_LIST_ITEM, mInfo);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    private void checkEtStudy(View view){
        view.requestFocus();
        Utils.showKeyboard(mContext, view);
    }

    private String currentDate(){
        Date currentDate = new Date(); // 현재 날짜 가져오기
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate); // 형식에 맞춰 날짜 문자열로 변환

        return formattedDate;
    }
    // 이름, 성별, 생일, 전화번호

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_done, menu);
//        int positionOfMenuItem = 0;
//        try {
//            MenuItem item = menu.getItem(positionOfMenuItem);
//            SpannableString span = new SpannableString(item.getTitle());
//            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
//            span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), 0);
//
//            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mContext.getResources().getDisplayMetrics());
//            span.setSpan(new AbsoluteSizeSpan(sizeInPx), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//            item.setTitle(span);
//        }catch(Exception ex){}
//        return (super.onCreateOptionsMenu(menu));
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.action_complete:
//                Utils.clearFocus(mEditList);
//                Utils.hideKeyboard(mContext, mEditList);
//                if (checked()) requestTestReserve();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}