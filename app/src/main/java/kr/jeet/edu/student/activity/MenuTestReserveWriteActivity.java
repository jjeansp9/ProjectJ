package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.SchoolListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.DatePickerFragment;
import kr.jeet.edu.student.dialog.SchoolListBottomSheetDialog;
import kr.jeet.edu.student.model.data.InflowData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.data.TestTimeData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.response.TestInflowResponse;
import kr.jeet.edu.student.model.response.TestTimeResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.utils.comparator.GradeComparator;
import kr.jeet.edu.student.view.ClearableTextView;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.dialog.SearchAddressDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuTestReserveWriteActivity extends BaseActivity {

    private static final String TAG = "MenuTestReserveWriteActivity";

    private TextView mTvReserveDate, mTvBirthDate, mTvAddress, mTvStuPhoneIsEmpty;
    private EditText mEtName, mEtAddressDetail, mEtStuPhone, mEtParentName, mEtparentPhone, mEtCashReceipt;
    private EditText[] mEditList;
    private RadioGroup mRgGender;
    private RadioButton mGenderRbMale, mGenderRbFemale;
    private PowerSpinnerView mSpinnerGrade, mSpinnerFunnel, mSpinnerCampus, mSpinnerTestDay, mSpinnerTestTime;

    ClearableTextView tvSchool;
    SchoolListBottomSheetDialog _schoolListBottomSheetDialog;
    SchoolListAdapter _schoolListAdapter;
    private boolean isFilterTriggerChanged = false;
    private SchoolData _selectedSchoolData = null;

    private String _stuGender = "";
    private String _ltcCode = "";
    private String _ltcName = "";
    private String _scName = "";
    private int _userGubun = -1;
    private int _scCode = 0;
    private String _stGrade = "";
    private String _stReason = "";

    private String _stName = "";
    private String _stPhone = "";
    private String _stParentName = "";
    private String _stParentPhone = "";
    private String _birth = "";

    Date _selectedDate;
    private int birthMinYear = 1950;
    private int birthMaxYear = 0;
    private int testDateMinYear = 0;
    private int testDateMaxYear = 2100;

    private int gender = 1;

    private LevelTestRequest request;
    private SearchAddressDialog mDialog = null;

    private TestReserveData mInfo;
    private String writeMode = "";
    private ArrayList<Integer> gradeIndex = new ArrayList<>();

    private int _childCnt = -1;
    private int testType = -1;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED) && Constants.FINISH_COMPLETE.equals(intent.getAction())) {
                    intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
                    setResult(RESULT_OK, intent);
                    finish();

                }else if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_EDITED) && Constants.FINISH_COMPLETE.equals(intent.getAction())) {
                    intent.putExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, true);
                    intent.putExtra(IntentParams.PARAM_SUCCESS_DATA, request);
                    setResult(RESULT_OK, intent);
                    finish();

                }else if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_SAVED)) {
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

                    if (mInfo == null) LogMgr.i(TAG, "get mInfo null");

                } else if (intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD) && Constants.FINISH_COMPLETE.equals(intent.getAction())) { // 신규원생을 추가했을 경우
                    intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test_reserve_write);
        mContext = this;
        initView();
        initAppbar();
    }

    /**
     == 회원 ==
      - 원생
       1. 테스트등록 - 개인정보 수정 X
      - 부모
       1. 테스트등록 - 개인정보 수정 X
       2. 신규원생 - 개인정보 수정 O

     == 비회원 ==
      - 원생
       1. 테스트등록 - 개인정보 수정 X
      - 부모
       1. 테스트등록 - 개인정보 수정 X
       2. 신규원생 - 개인정보 수정 O [isOriginal "Y"로 변경]
    * */

    private void initData(){
        _selectedDate = new Date();
        Calendar calendar = Calendar.getInstance();
        testDateMinYear = calendar.get(Calendar.YEAR);
        birthMaxYear = calendar.get(Calendar.YEAR);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _stParentPhone = PreferenceUtil.getParentPhoneNum(mContext);
        _stParentName = PreferenceUtil.getParentName(mContext);
        _selectedSchoolData = new SchoolData("", 0);
        _childCnt = PreferenceUtil.getNumberOfChild(mContext);

        try {

            Intent intent= getIntent();
            if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_WRITE)){
                request = intent.getParcelableExtra(IntentParams.PARAM_TEST_RESERVE_WRITE);
            }
            if (intent.hasExtra(IntentParams.PARAM_WRITE_MODE)){
                writeMode = intent.getStringExtra(IntentParams.PARAM_WRITE_MODE);
            }
            if (intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
            }else{
                mInfo = new TestReserveData();
            }
            if (intent.hasExtra(IntentParams.PARAM_TEST_TYPE)){
                testType = intent.getIntExtra(IntentParams.PARAM_TEST_TYPE, testType);
            }

        }catch (Exception e) {
            LogMgr.e(TAG, e.getMessage());
        }

        if (testType != Constants.LEVEL_TEST_TYPE_NEW_CHILD) {
            _stName = PreferenceUtil.getStName(mContext);
            _stuGender = PreferenceUtil.getStuGender(mContext);
            _birth = PreferenceUtil.getStuBirth(mContext);
            _stPhone = PreferenceUtil.getStuPhoneNum(mContext);
        }
    }

    @Override
    void initView() {

        initData();

        findViewById(R.id.layout_reserve_birth).setOnClickListener(this);
        findViewById(R.id.layout_reserve_test_date).setOnClickListener(this);
        findViewById(R.id.root_reserve_write).setOnClickListener(this);
        findViewById(R.id.btn_test_reserve_write_next).setOnClickListener(this);
        findViewById(R.id.btn_address_search).setOnClickListener(this);
        findViewById(R.id.tv_reserve_address_result).setOnClickListener(this);

        mTvReserveDate = findViewById(R.id.tv_reserve_test_date_cal);
        mTvBirthDate = findViewById(R.id.tv_reserve_birth_date_cal);
        mTvAddress = findViewById(R.id.tv_reserve_address_result);
        mTvStuPhoneIsEmpty = findViewById(R.id.tv_reserve_stu_contact_notice);

        mEtName = (EditText) findViewById(R.id.et_reserve_name);
        mEtAddressDetail = (EditText) findViewById(R.id.et_reserve_address_detail);
        mEtStuPhone = (EditText) findViewById(R.id.et_reserve_stu_contact);
        mEtParentName = (EditText) findViewById(R.id.et_reserve_parent_name);
        mEtparentPhone = (EditText) findViewById(R.id.et_reserve_parent_contact);
        mEtCashReceipt = (EditText) findViewById(R.id.et_reserve_cash_receipts);
        mEditList = new EditText[]{mEtName, mEtAddressDetail, mEtStuPhone, mEtParentName, mEtparentPhone, mEtCashReceipt};

        mRgGender = (RadioGroup) findViewById(R.id.rg_test_reserve_gender);
        mGenderRbMale = (RadioButton) findViewById(R.id.rb_test_reserve_male);
        mGenderRbFemale = (RadioButton) findViewById(R.id.rb_test_reserve_female);

        //mSpinnerSchool = findViewById(R.id.spinner_reserve_sc_name);
        mSpinnerGrade = findViewById(R.id.spinner_reserve_grade);
        mSpinnerFunnel = findViewById(R.id.spinner_reserve_funnel);
        mSpinnerCampus = findViewById(R.id.spinner_reserve_campus);
        mSpinnerTestTime = findViewById(R.id.spinner_reserve_test_class);
        tvSchool = findViewById(R.id.tv_content_school);

        LogMgr.e(TAG, "Gender: " + _stuGender);

        if (writeMode.equals(Constants.WRITE_EDIT)) {
            setView();
        } else{
            if (_stuGender.equals("F")){
                mGenderRbMale.setChecked(false);
                mGenderRbFemale.setChecked(true);
            }else{
                mGenderRbMale.setChecked(true);
                mGenderRbFemale.setChecked(false);
            }

//            mEtName.setText(Utils.getStr(_stName));
//            mEtStuPhone.setText(Utils.getStr(_stPhone).replace("-", ""));
//            mEtparentPhone.setText(Utils.getStr(_stParentPhone).replace("-", ""));
//            mTvBirthDate.setText(Utils.getStr(_birth));

            if (!TextUtils.isEmpty(_stName)) {
                mEtName.setText(Utils.getStr(_stName));
                mEtName.setEnabled(false);
            }
            if (!TextUtils.isEmpty(_stPhone)) {
                mEtStuPhone.setText(Utils.getStr(_stPhone).replace("-", ""));
                mEtStuPhone.setEnabled(false);
            }else{
                mTvStuPhoneIsEmpty.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(_stParentPhone)){
                mEtparentPhone.setText(Utils.getStr(_stParentPhone).replace("-", ""));
                mEtparentPhone.setEnabled(false);
            }
            if (!TextUtils.isEmpty(_stParentName)){
                mEtParentName.setText(Utils.getStr(_stParentName));
                mEtParentName.setEnabled(false);
            }

            mTvBirthDate.setText(Utils.getStr(_birth));

        }

        setSpinner();
    }

    private void setView(){
        if (mInfo == null) finish();

        String str = "";

        mEtName.setText(Utils.getStr(mInfo.name));
        mEtName.setEnabled(false);

        LogMgr.i(TAG, "gender: " + mInfo.sex);
        if (mInfo.sex.equals("M")) {
            mGenderRbMale.setChecked(true);
            mGenderRbFemale.setChecked(false);
            gender = 1;
        }else{
            mGenderRbMale.setChecked(false);
            mGenderRbFemale.setChecked(true);
            gender = 2;
        }

        mTvAddress.setText(Utils.getStr(mInfo.address));
        mEtAddressDetail.setText(Utils.getStr(mInfo.addressSub));
        mTvBirthDate.setText(Utils.getStr(mInfo.birth));

        str = Utils.getStr(mInfo.grade)+"학년";
        _stGrade = Utils.getStr(mInfo.grade);
        mSpinnerGrade.setText(str);

        for(SchoolData info : DataManager.getInstance().getSchoolList()) {
            if(mInfo.scCode == info.scCode) {
                tvSchool.setText(Utils.getStr(info.scName));
                _scCode = info.scCode;
                break;
            }
        }
        mEtStuPhone.setText(Utils.getStr(mInfo.phoneNumber));
        mEtStuPhone.setEnabled(false);
        mEtParentName.setText(Utils.getStr(mInfo.parentName));
        mEtParentName.setEnabled(false);
        mEtparentPhone.setText(Utils.getStr(mInfo.parentPhoneNumber));
        mEtparentPhone.setEnabled(false);
        mEtCashReceipt.setText(Utils.getStr(mInfo.cashReceiptNumber.replace("-", "")));
        mSpinnerFunnel.setText(Utils.getStr(mInfo.reason));
        _stReason = Utils.getStr(mInfo.reason);
        mSpinnerCampus.setText(Utils.getStr(mInfo.bigoText));
        _ltcCode = Utils.getStr(mInfo.bigo);
        _ltcName = Utils.getStr(mInfo.bigoText);
        _selectedSchoolData.scName = Utils.getStr(tvSchool.toString());
        _selectedSchoolData.scCode = _scCode;

        String[] dateTimeParts = mInfo.reservationDate.split(" ");

        String date = "";
        String time = "";

        if (dateTimeParts.length == 1) {
            date = dateTimeParts[0]; // 날짜

        } else if (dateTimeParts.length > 1){
            date = dateTimeParts[0]; // 날짜
            time = dateTimeParts[1]; // 시간
        }
        mTvReserveDate.setText(Utils.getStr(date)); // yyyy-MM-dd
        mSpinnerTestTime.setText(Utils.getStr(time)); // HH:mm

        mSpinnerCampus.setOnClickListener(null);
        mSpinnerCampus.setEnabled(false);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        if (writeMode.equals(Constants.WRITE_EDIT)) customAppbar.setTitle(R.string.test_reserve_update_title);
        else customAppbar.setTitle(R.string.test_reserve_write_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.root_reserve_write:
                clearFocusAndHideKeyboard();
                break;

            case R.id.layout_reserve_birth:
                clearFocusAndHideKeyboard();
                showDatePicker(mTvBirthDate, false, birthMinYear, birthMaxYear, true);
                break;

            case R.id.layout_reserve_test_date:
                clearFocusAndHideKeyboard();
                showDatePicker(mTvReserveDate, true, testDateMinYear, testDateMaxYear, false);
                break;

            case R.id.btn_test_reserve_write_next:
                clearFocusAndHideKeyboard();
                startQuestionActivity();
                break;

            case R.id.btn_address_search:
            case R.id.tv_reserve_address_result:
                clearFocusAndHideKeyboard();
                searchAddress();
                break;
        }
    }

    private void clearFocusAndHideKeyboard(){
        Utils.clearFocus(mEditList);
        Utils.hideKeyboard(mContext, mEditList);
    }

    private void searchAddress(){
        dialogDismiss();
        mDialog = SearchAddressDialog.newInstance();
        mDialog.showDialog(this, address -> {
            if (address != null) runOnUiThread(() -> mTvAddress.setText(address));
            dialogDismiss();
        });
    }

    private void dialogDismiss(){
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    DatePickerFragment datePickerDialog = null;

    void showDatePicker(TextView tv, boolean setDate, int minYear, int maxYear, boolean isBirth) {
        if (datePickerDialog != null){
            datePickerDialog.dismiss();
            datePickerDialog = null;
        }
        DatePickerFragment.OnDateSetListener listener = (year, month, day) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, day);
            String formattedDate = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day);
            if (setDate){
                if (selectedDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    tv.setText(formattedDate);
                    requestTestTime();
                    mSpinnerTestTime.setText("");
                    //mSpinnerTestTime.show();
                }else{
                    Toast.makeText(mContext, R.string.test_reserve_sunday_impossible_sel, Toast.LENGTH_SHORT).show();
                    showDatePicker(mTvReserveDate, true, testDateMinYear, testDateMaxYear, false);
                }

            }else{
                tv.setText(formattedDate);
            }
        };
        datePickerDialog = new DatePickerFragment(listener, setDate, minYear, maxYear, isBirth);

        Calendar calendar = Calendar.getInstance();
        String strDate = tv.getText().toString();

        if (strDate.equals("")){
            calendar.setTime(_selectedDate);
        }else{

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setSpinner(){
        mSpinnerCampus.setIsFocusable(true);
        //mSpinnerSchool.setIsFocusable(true);
        mSpinnerTestTime.setIsFocusable(true);
        mSpinnerGrade.setIsFocusable(true);
        mSpinnerFunnel.setIsFocusable(true);

        List<LTCData> ltcList = DataManager.getInstance().getLTCList();
        List<String> ltcNames = new ArrayList<>();

        for (LTCData data : ltcList) ltcNames.add(data.ltcName);
        mSpinnerCampus.setItems(ltcNames);
        mSpinnerCampus.setSpinnerPopupHeight(500);

        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _ltcCode = ltcList.get(newIndex).ltcCode;
            _ltcName = ltcList.get(newIndex).ltcName;
        });

        mSpinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {

            if (newIndex > 0) _stGrade = newItem.toString();
            else {
                mSpinnerGrade.setText("");
                _stGrade = "";
            }
            _scCode = 0;
            if (!TextUtils.isEmpty(_stGrade)) setSchoolSpinner();
            tvSchool.setText("");
            _selectedSchoolData = new SchoolData("", 0);
        });

        mSpinnerFunnel.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _stReason = newItem.toString();
        });

        setSchoolSpinner();

        mSpinnerFunnel.setOnTouchListener(spinnerTouchListener);
        mSpinnerCampus.setOnTouchListener(spinnerTouchListener);
        mSpinnerGrade.setOnTouchListener(spinnerTouchListener);

        mSpinnerTestTime.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    if (TextUtils.isEmpty(mTvReserveDate.getText().toString())) {
                        showDatePicker(mTvReserveDate, true, testDateMinYear, testDateMaxYear, false);
                        Toast.makeText(mContext, R.string.test_reserve_campus_sel_please, Toast.LENGTH_SHORT).show();
                        mSpinnerTestTime.dismiss();
                    }else{
                        if (testTimeList.isEmpty()){
                            Toast.makeText(mContext, R.string.test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                    Utils.clearFocus(mEditList);
                    Utils.hideKeyboard(mContext, mEditList);
                    break;
            }
            return false;
        });

        mSpinnerTestTime.setSpinnerPopupHeight(500);

        mSpinnerTestTime.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (!TextUtils.isEmpty(mTvReserveDate.getText().toString())){

                String originalTime = "";

                if (gradeIndex.contains(newIndex)) originalTime = testTimeList.get(newIndex + 1).toString();
                else originalTime = testTimeList.get(newIndex).toString();

                SimpleDateFormat originalFormat = new SimpleDateFormat("a hh:mm", Locale.getDefault());
                SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                try {
                    Date date = originalFormat.parse(originalTime);
                    if (date != null) {
                        String formattedTime = targetFormat.format(date);
                        mSpinnerTestTime.setText(formattedTime);
                    } else {
                        mSpinnerTestTime.setText("");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    mSpinnerTestTime.setText("");
                }

            }else{
                mSpinnerTestTime.setText("");
            }
        });

        if (!TextUtils.isEmpty(mTvReserveDate.getText().toString())) requestTestTime();
        requestInflow();
    }

    private void setSchoolSpinner(){
        toggleFilterLayout();
        tvSchool.setCloseClickListener(new ClearableTextView.onTextViewClickListener() {
            @Override
            public void onContentClick() {

                if (mSpinnerGrade.getText().toString().equals("")) {
                    mSpinnerGrade.performClick();
                    Toast.makeText(mContext, R.string.test_reserve_grade_sel_please, Toast.LENGTH_SHORT).show();
                }else{
                    if (_schoolListBottomSheetDialog != null) {
                        _schoolListBottomSheetDialog = null;
                    }
                    _schoolListBottomSheetDialog = new SchoolListBottomSheetDialog(_schoolListAdapter);
                    _schoolListBottomSheetDialog.show(getSupportFragmentManager(), TAG);
                }
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
            }

            @Override
            public void onDeleteClick() {
                LogMgr.e(TAG, "onDeleteClick");
                isFilterTriggerChanged = true;
                _selectedSchoolData = new SchoolData("", 0);
                tvSchool.setText("");
            }
        });
    }

    private void toggleFilterLayout() {
        List<SchoolData> _schoolList = new ArrayList<>();
        List<SchoolData> schoolAllList = new ArrayList<>(DataManager.getInstance().getSchoolListMap().values());

        if (_stGrade.contains(getString(R.string.informed_question_elementary))){
            for (SchoolData data : schoolAllList) {
                if (!data.scName.contains(Constants.middleSchool) && !data.scName.contains(Constants.highSchool)){
                    _schoolList.add(new SchoolData(data.scName, data.scCode));
                }
            }
        } else if (_stGrade.contains(getString(R.string.informed_question_middle))){
            for (SchoolData data : schoolAllList) {
                if (!data.scName.contains(Constants.elementary) && !data.scName.contains(Constants.highSchool)){
                    _schoolList.add(new SchoolData(data.scName, data.scCode));
                }
            }
        } else{
            for (SchoolData data : schoolAllList) {
                if (!data.scName.contains(Constants.elementary) && !data.scName.contains(Constants.middleSchool)){
                    _schoolList.add(new SchoolData(data.scName, data.scCode));
                }
            }
        }

        Collections.sort(_schoolList, new Comparator<SchoolData>() {
            @Override
            public int compare(SchoolData schoolData, SchoolData t1) {
                return Collator.getInstance().compare(schoolData.scName, t1.scName);
            }
        });
        _schoolListAdapter = new SchoolListAdapter(mContext, _schoolList, new SchoolListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(SchoolData item) {
                _selectedSchoolData = item;
                tvSchool.setText(item.scName);
                isFilterTriggerChanged = true;
                if(_schoolListBottomSheetDialog != null) {
                    _schoolListBottomSheetDialog.dismiss();
                }
            }

            @Override
            public void onFilteringCompleted() {

            }
        });
    }

    private void startQuestionActivity(){

        String str = "";

        if (mGenderRbMale.isChecked()) gender = 1;
        else gender =2;

        if (writeMode.equals(Constants.WRITE_EDIT)) request = new LevelTestRequest();
        request.userGubun = _userGubun;
        request.name = mEtName.getText().toString(); // 학생이름 [필수]
        request.birth = mTvBirthDate.getText().toString(); // 생년월일 [필수]
        request.sex = gender; // 성별 (1 남자, 2 여자) [필수]
        request.address = mTvAddress.getText().toString(); // 주소 (도로명주소) [선택]
        request.addressSub = mEtAddressDetail.getText().toString(); // 상세주소 [선택]
        request.scCode = _selectedSchoolData.scCode; // 학교코드 [필수]
        request.grade = _stGrade.replace(getString(R.string.test_reserve_write_grade_sub), ""); // 학년 [필수]
        request.phoneNumber = mEtStuPhone.getText().toString(); // 학생 전화번호 [선택]
        request.parentPhoneNumber = mEtparentPhone.getText().toString(); // 학부모 연락처 [필수]
        request.parentName = mEtParentName.getText().toString(); // 학부모 성함 [필수]
        request.reason = _stReason; // 유입경로 [선택]
        str = mTvReserveDate.getText().toString() + " " + mSpinnerTestTime.getText().toString();
        request.reservationDate = str; // 테스트예약일 [필수]
        request.bigo = _ltcCode; // 캠퍼스 비고 [필수]
        request.bigoText = _ltcName; // 캠퍼스 비고(캠퍼스 이름) [필수]
        //request.cashReceiptNumber = TextUtils.isEmpty(mEtCashReceipt.getText().toString()) ? "010-000-1234" : mEtCashReceipt.getText().toString(); // 현금영수증 (010-000-1234) [선택]
        request.cashReceiptNumber = Utils.formatCashReceiptNum(mEtCashReceipt.getText().toString()); // 현금영수증 (010-000-1234) [선택]

        LogMgr.i(TAG+ "putData","\n학생이름 : " + request.name
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
                + "\n레벨캠퍼스 비고(캠퍼스 이름) : " + request.bigoText
                + "\n현금영수증 : " + request.cashReceiptNumber
                + "\ncheck1 : " + request.check1
                + "\ncheck2 : " + request.check2
                + "\ncheck3 : " + request.check3
                + "\ncheck4 : " + request.check4
        );

//        1. 카드일련번호 : 1544 2020 **** 123456(앞 8자리 이후 4개)
//        2. 주민등록번호 : 710011 *******
//        3. 사업자등록번호 : 12* ** *1234
//        4. 휴대전화번호 : 010 **** 1234

//        if (request.name.equals("") || request.birth.equals("") || request.grade.equals("") || mSpinnerSchool.getText().toString().equals("") ||
//                request.parentPhoneNumber.equals("") || request.phoneNumber.equals("") || request.parentName.equals("") || request.reservationDate.equals("") ||
//                request.bigo.equals("") || request.address.equals("") || request.reason.equals("")){
//            Toast.makeText(mContext, R.string.write_empty, Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (request.name.equals("")) {
            Toast.makeText(mContext, R.string.stu_name_empty, Toast.LENGTH_SHORT).show();
            mEtName.requestFocus();
            Utils.showKeyboard(mContext, mEtName);

        } else if (request.address.equals("")) {
            Toast.makeText(mContext, R.string.address_empty, Toast.LENGTH_SHORT).show();

        } else if (request.addressSub.equals("")) {
            Toast.makeText(mContext, R.string.address_sub_empty, Toast.LENGTH_SHORT).show();
            mEtAddressDetail.requestFocus();
            Utils.showKeyboard(mContext, mEtAddressDetail);

        } else if (request.birth.equals("")) {
            Toast.makeText(mContext, R.string.birth_empty, Toast.LENGTH_SHORT).show();
            showDatePicker(mTvBirthDate, false, birthMinYear, birthMaxYear, true);

        } else if (request.grade.equals("")) {
            Toast.makeText(mContext, R.string.grade_empty, Toast.LENGTH_SHORT).show();
            mSpinnerGrade.setSpinnerPopupHeight(600);
            if (mSpinnerGrade != null) mSpinnerGrade.show();

        } else if (TextUtils.isEmpty(_selectedSchoolData.scName)) {
            Toast.makeText(mContext, R.string.school_empty, Toast.LENGTH_SHORT).show();
            toggleFilterLayout();
            if (_schoolListBottomSheetDialog != null) {
                _schoolListBottomSheetDialog = null;
            }
            _schoolListBottomSheetDialog = new SchoolListBottomSheetDialog(_schoolListAdapter);
            _schoolListBottomSheetDialog.show(getSupportFragmentManager(), TAG);

        } else if (request.phoneNumber.equals("")) {
            Toast.makeText(mContext, R.string.phone_empty, Toast.LENGTH_SHORT).show();
            mEtStuPhone.requestFocus();
            Utils.showKeyboard(mContext, mEtStuPhone);

        } else if (!Utils.checkPhoneNumber(request.phoneNumber)){
            Toast.makeText(mContext, R.string.write_phone_impossible, Toast.LENGTH_SHORT).show();
            mEtStuPhone.requestFocus();
            Utils.showKeyboard(mContext, mEtStuPhone);

        } else if (request.parentName.equals("")) {
            Toast.makeText(mContext, R.string.parent_name_empty, Toast.LENGTH_SHORT).show();
            mEtParentName.requestFocus();
            Utils.showKeyboard(mContext, mEtParentName);

        } else if (request.parentPhoneNumber.equals("")) {
            Toast.makeText(mContext, R.string.parent_phone_empty, Toast.LENGTH_SHORT).show();
            mEtparentPhone.requestFocus();
            Utils.showKeyboard(mContext, mEtparentPhone);

        } else if (!Utils.checkPhoneNumber(request.parentPhoneNumber)){
            Toast.makeText(mContext, R.string.write_phone_impossible, Toast.LENGTH_SHORT).show();
            mEtparentPhone.requestFocus();
            Utils.showKeyboard(mContext, mEtparentPhone);

        } else if (request.reason.equals("")) {
            Toast.makeText(mContext, R.string.reason_empty, Toast.LENGTH_SHORT).show();
            mSpinnerFunnel.setSpinnerPopupHeight(500);
            if (mSpinnerFunnel != null) mSpinnerFunnel.show();

        } else if (request.bigo.equals("")) {
            Toast.makeText(mContext, R.string.bigo_empty, Toast.LENGTH_SHORT).show();
            mSpinnerCampus.setSpinnerPopupHeight(500);
            if (mSpinnerCampus != null) mSpinnerCampus.show();

        } else if (mTvReserveDate.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.reservation_date_empty, Toast.LENGTH_SHORT).show();
            showDatePicker(mTvReserveDate, true, birthMinYear, birthMaxYear, false);

        }else if (mSpinnerTestTime.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.reservation_time_empty, Toast.LENGTH_SHORT).show();
            if (!testTimeList.isEmpty()){
                mSpinnerTestTime.setSpinnerPopupHeight(500);
                if (mSpinnerTestTime != null) mSpinnerTestTime.show();
            }

        }else{
            Intent intent = new Intent(mContext, InformedQuestionActivity.class);
            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_WRITE, request);
            intent.putExtra(IntentParams.PARAM_WRITE_MODE, writeMode);
            intent.putExtra(IntentParams.PARAM_TEST_TYPE, testType);
            if (mInfo != null) {
                LogMgr.e(TAG, "Event put mInfo");
                intent.putExtra(IntentParams.PARAM_LIST_ITEM, mInfo);
            }else{
                LogMgr.e(TAG, "Event put mInfo null");
            }
            resultLauncher.launch(intent);
        }
    }

    ArrayList<SpannableString> testTimeList = new ArrayList<>();

    private void requestTestTime() {

        showProgressDialog();

        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getTestTime().enqueue(new Callback<TestTimeResponse>() {
                @Override
                public void onResponse(Call<TestTimeResponse> call, Response<TestTimeResponse> response) {
                    if (testTimeList!=null && testTimeList.size() > 0) testTimeList.clear();
                    if (gradeIndex!=null && gradeIndex.size() > 0) gradeIndex.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                ArrayList<TestTimeData> getData = response.body().data;

                                for (TestTimeData data: getData) LogMgr.i(TAG, "TestTimeList: " + data.time);

                                int redColor;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    redColor = getResources().getColor(R.color.red, null);
                                } else {
                                    redColor = getResources().getColor(R.color.red);
                                }

                                LogMgr.i(TAG, "reserveDate : " + mTvReserveDate.getText().toString());

                                String dateString = mTvReserveDate.getText().toString();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                                try {
                                    Date date = dateFormat.parse(dateString);
                                    Calendar calendar = Calendar.getInstance();
                                    if (date != null) calendar.setTime(date);

                                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                    int weekend = (dayOfWeek == Calendar.SATURDAY) ? 1 : 0;

                                    ArrayList<String> grades = new ArrayList<>();
                                    for (TestTimeData item : getData) if (weekend == item.weekend) if (!grades.contains(item.grade)) grades.add(item.grade);

                                    grades.sort(new GradeComparator());

                                    for (String grade : grades) {
                                        SpannableString gradeSpan = createColoredSpan(grade, redColor);
                                        testTimeList.add(gradeSpan);
                                        int lastIndex = testTimeList.lastIndexOf(gradeSpan);
                                        gradeIndex.add(lastIndex);
                                        getTestTime(testTimeList, getData, grade, weekend);
                                    }

                                    if (testTimeList.isEmpty()) Toast.makeText(mContext, R.string.test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                                    mSpinnerTestTime.setItems(testTimeList);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestTime() Exception : ", e.getMessage());
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<TestTimeResponse> call, Throwable t) {
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }

    // 유입경로 api
    private void requestInflow() {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().requestInflow().enqueue(new Callback<TestInflowResponse>() {
                @Override
                public void onResponse(Call<TestInflowResponse> call, Response<TestInflowResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<InflowData> getData = response.body().data;

                            if (getData != null){
                                List<String> inflowNames = new ArrayList<>();

                                for (InflowData inflowData : getData) {
                                    inflowNames.add(inflowData.inflowName);
                                }
                                Collections.sort(inflowNames, Collections.reverseOrder());

                                mSpinnerFunnel.setItems(inflowNames);
                            }
                        }
                    } else {
                        Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TestInflowResponse> call, Throwable t) {
                    //mSpinnerFunnel.setItems(testTimeList);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private SpannableString createColoredSpan(String text, int color) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void getTestTime(ArrayList<SpannableString> testTimeList, ArrayList<TestTimeData> getData, String grade, int weekend) {

        SimpleDateFormat timeFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_HH_MM, Locale.KOREA);

        for (TestTimeData item : getData) {
            if (grade.equals(item.grade) && item.weekend == weekend) {
                SpannableString timeSpan = new SpannableString(item.time);
                testTimeList.add(timeSpan);
            }
        }

        testTimeList.sort((span1, span2) -> {
            try {
                Date time1 = timeFormat.parse(span1.toString());
                Date time2 = timeFormat.parse(span2.toString());
                return time1 != null ? time1.compareTo(time2) : 0;
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        formatAndAddAmPm(testTimeList);
    }

    private void formatAndAddAmPm(ArrayList<SpannableString> testTimeList) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_HH_MM, Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_A_HH_MM, Locale.KOREA);

        for (int i = 0; i < testTimeList.size(); i++) {
            SpannableString span = testTimeList.get(i);
            try {
                Date date = inputFormat.parse(span.toString());
                if (date != null) {
                    String formattedTime = outputFormat.format(date);
                    testTimeList.set(i, new SpannableString(formattedTime));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener spinnerTouchListener = (v, event) -> {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
                break;
        }
        return false;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);

        boolean visibility = false;

        if (writeMode.equals(Constants.WRITE_EDIT)) {

            visibility = false;

        } else {
            if (_childCnt > 0) {
                if (testType == Constants.LEVEL_TEST_TYPE_NEW_CHILD) {
                    visibility = false;
                } else {
                    visibility = true;
                }
            } else {
                visibility = false;
            }
        }

        menu.findItem(R.id.action_btn_sub).setVisible(visibility);

        try {
            setMenuItemTextStyling(menu.findItem(R.id.action_next), R.color.red, true, 16);
            setMenuItemTextStyling(menu.findItem(R.id.action_btn_sub), R.color.font_color_default, true, 16);

        }catch(Exception ex){}
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_next:
                clearFocusAndHideKeyboard();
                startQuestionActivity();
                return true;
            case R.id.action_btn_sub:
                mEtName.setText("");
                mEtStuPhone.setText("");
                mEtName.setEnabled(true);
                mEtStuPhone.setEnabled(true);
                item.setVisible(false);
                mTvStuPhoneIsEmpty.setVisibility(View.VISIBLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (writeMode.equals(Constants.WRITE_EDIT)){
            finish();
        }else{
            showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.dialog_content_close), ok -> {
                hideMessageDialog();
                finish();
                Log.e(TAG, "Event");
            }, cancel -> {
                hideMessageDialog();
                return;
            }, false);
        }
    }

    private void setMenuItemTextStyling(MenuItem menuItem, int textColorResId, boolean isBold, int textSizeInDp) {
        SpannableString spannableTitle = new SpannableString(menuItem.getTitle());
        spannableTitle.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, textColorResId)), 0, spannableTitle.length(), 0);
        if (isBold) spannableTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableTitle.length(), 0);
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp, mContext.getResources().getDisplayMetrics());
        spannableTitle.setSpan(new AbsoluteSizeSpan(sizeInPx), 0, spannableTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        menuItem.setTitle(spannableTitle);
    }
}