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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.dialog.SearchAddressDialog;

public class MenuTestReserveWriteActivity extends BaseActivity {

    private static final String TAG = "MenuTestReserveWriteActivity";

    private TextView mTvReserveDate, mTvBirthDate, mTvAddress;
    private EditText mEtName, mEtAddressDetail, mEtStuPhone, mEtParentName, mEtparentPhone, mEtCashReceipt;
    private EditText[] mEditList;
    private RadioGroup mRgGender;
    private RadioButton mGenderRbMale, mGenderRbFemale;
    private PowerSpinnerView mSpinnerSchool, mSpinnerGrade, mSpinnerFunnel, mSpinnerCampus, mSpinnerTestDay, mSpinnerTestTime;

    private String _stuGender = "";
    private String _ltcCode = "";
    private String _ltcName = "";
    private String _scName = "";
    private int _scCode = 0;
    private String _stGrade = "";
    private String _stReason = "";

    private List<String> testTime;
    private String strTestTime = "";
    Date _selectedDate;
    private int birthMinYear = 1950;
    private int birthMaxYear = 0;
    private int testDateMinYear = 0;
    private int testDateMaxYear = 2100;

    private int gender = 1;

    private String elementary = "초등";
    private String middleSchool = "중학교";
    private String highSchool = "고등";

    private LevelTestRequest request;
    private SearchAddressDialog mDialog = null;

    private TestReserveData mInfo;

    private String writeMode = "";

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED) && Constants.FINISH_COMPLETE.equals(intent.getAction())) {
                intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
                setResult(RESULT_OK, intent);
                finish();

            }else if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_EDITED) && Constants.FINISH_COMPLETE.equals(intent.getAction())) {
                intent.putExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, true);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test_reserve_write);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private String _stName = "";
    private String _stPhone = "";
    private String _stParentPhone = "";
    private String _birth = "";

    private void initData(){
        _selectedDate = new Date();
        Calendar calendar = Calendar.getInstance();
        testDateMinYear = calendar.get(Calendar.YEAR);
        birthMaxYear = calendar.get(Calendar.YEAR);

        _stName = PreferenceUtil.getStName(mContext);
        _stuGender = PreferenceUtil.getStuGender(mContext);
        _birth = PreferenceUtil.getStuBirth(mContext);
        _stPhone = PreferenceUtil.getStuPhoneNum(mContext);
        _stParentPhone = PreferenceUtil.getParentPhoneNum(mContext);

        try {

            Intent intent= getIntent();
            if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_WRITE)){
                request = intent.getParcelableExtra(IntentParams.PARAM_TEST_RESERVE_WRITE);
            }
            if (intent.hasExtra(IntentParams.PARAM_WRITE_MODE) && intent.hasExtra(IntentParams.PARAM_LIST_ITEM)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
                writeMode = intent.getStringExtra(IntentParams.PARAM_WRITE_MODE);
            }

        }catch (Exception e) {
            LogMgr.e(TAG, e.getMessage());
        }
    }

    @Override
    void initView() {
        findViewById(R.id.layout_reserve_birth).setOnClickListener(this);
        findViewById(R.id.layout_reserve_test_date).setOnClickListener(this);
        findViewById(R.id.root_reserve_write).setOnClickListener(this);
        findViewById(R.id.btn_test_reserve_write_next).setOnClickListener(this);
        findViewById(R.id.btn_address_search).setOnClickListener(this);
        findViewById(R.id.tv_reserve_address_result).setOnClickListener(this);

        mTvReserveDate = findViewById(R.id.tv_reserve_test_date_cal);
        mTvBirthDate = findViewById(R.id.tv_reserve_birth_date_cal);
        mTvAddress = findViewById(R.id.tv_reserve_address_result);

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

        mSpinnerSchool = findViewById(R.id.spinner_reserve_sc_name);
        mSpinnerGrade = findViewById(R.id.spinner_reserve_grade);
        mSpinnerFunnel = findViewById(R.id.spinner_reserve_funnel);
        mSpinnerCampus = findViewById(R.id.spinner_reserve_campus);
        mSpinnerTestTime = findViewById(R.id.spinner_reserve_test_class);


        LogMgr.e(TAG, "Gender: " + _stuGender);

        if (writeMode.equals(Constants.WRITE_EDIT)) {
            setView();
        } else{
            if (_stuGender.equals("M")) mGenderRbMale.setChecked(true);
            else mGenderRbFemale.setChecked(true);

            mEtName.setText(Utils.getStr(_stName));
            mEtStuPhone.setText(Utils.getStr(_stPhone).replace("-", ""));
            mEtparentPhone.setText(Utils.getStr(_stParentPhone).replace("-", ""));
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
        }else{
            mGenderRbMale.setChecked(false);
            mGenderRbFemale.setChecked(true);
        }

        mTvAddress.setText(Utils.getStr(mInfo.address));
        mEtAddressDetail.setText(Utils.getStr(mInfo.addressSub));
        mTvBirthDate.setText(Utils.getStr(mInfo.birth));

        str = Utils.getStr(mInfo.grade)+"학년";
        _stGrade = Utils.getStr(mInfo.grade);
        mSpinnerGrade.setText(str);

        for(SchoolData info : DataManager.getInstance().getSchoolList()) {
            if(mInfo.scCode == info.scCode) {
                mSpinnerSchool.setText(Utils.getStr(info.scName));
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
        mEtCashReceipt.setText(Utils.getStr(mInfo.cashReceiptNumber));
        mSpinnerFunnel.setText(Utils.getStr(mInfo.reason));
        _stReason = Utils.getStr(mInfo.reason);
        mSpinnerCampus.setText(Utils.getStr(mInfo.bigoText));
        _ltcCode = Utils.getStr(mInfo.bigo);
        _ltcName = Utils.getStr(mInfo.bigoText);
        mTvReserveDate.setText(Utils.getStr(mInfo.reservationDate));
        //mSpinnerTestTime.setText(Utils.getStr(mInfo));

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
                    setTestTime();
                    strTestTime = "";
                    mSpinnerTestTime.setText("");
                    mSpinnerTestTime.show();
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
        mSpinnerSchool.setIsFocusable(true);
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
            _stGrade = newItem.toString();
            _scCode = 0;
            if (!TextUtils.isEmpty(_stGrade)) setSchoolSpinner();
            mSpinnerSchool.setText("");
        });

        mSpinnerFunnel.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _stReason = newItem.toString();
        });

        setSchoolSpinner();

        mSpinnerFunnel.setOnTouchListener(spinnerTouchListener);
        mSpinnerCampus.setOnTouchListener(spinnerTouchListener);
        mSpinnerGrade.setOnTouchListener(spinnerTouchListener);
        mSpinnerSchool.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    if (mSpinnerGrade.getText().toString().equals("")) {
                        mSpinnerGrade.performClick();
                        Toast.makeText(mContext, R.string.test_reserve_grade_sel_please, Toast.LENGTH_SHORT).show();
                        mSpinnerSchool.dismiss();
                    }
                    Utils.clearFocus(mEditList);
                    Utils.hideKeyboard(mContext, mEditList);
                    break;
            }
            return false;
        });

        mSpinnerTestTime.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    if (TextUtils.isEmpty(mTvReserveDate.getText().toString())) {
                        showDatePicker(mTvReserveDate, true, testDateMinYear, testDateMaxYear, false);
                        Toast.makeText(mContext, R.string.test_reserve_campus_sel_please, Toast.LENGTH_SHORT).show();
                        mSpinnerTestTime.dismiss();
                    }
                    Utils.clearFocus(mEditList);
                    Utils.hideKeyboard(mContext, mEditList);
                    break;
            }
            return false;
        });

        mSpinnerTestTime.setSpinnerPopupHeight(500);

        // 초등 index : 0, 중등 : 3, 고등 : 6
        mSpinnerTestTime.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (!TextUtils.isEmpty(mTvReserveDate.getText().toString())){

                if (newIndex == 0){
                    strTestTime = testTime.get(newIndex + 1).toString();
                    mSpinnerTestTime.setText(testTime.get(newIndex + 1));

                }else if (newIndex == 3){
                    strTestTime = testTime.get(newIndex + 1).toString();
                    mSpinnerTestTime.setText(testTime.get(newIndex + 1));

                }else if (newIndex == 6){
                    strTestTime = testTime.get(newIndex + 1).toString();
                    mSpinnerTestTime.setText(testTime.get(newIndex + 1));
                }
            }else{
                strTestTime = "";
                mSpinnerTestTime.setText("");
            }
        });

        if (!TextUtils.isEmpty(mTvReserveDate.getText().toString())) setTestTime();
    }

    private void setTestTime(){
        ArrayList<SpannableString> testTime = new ArrayList<>();

        int redColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            redColor = getResources().getColor(R.color.red, null);
        } else {
            redColor = getResources().getColor(R.color.red);
        }

        SpannableString elementary = new SpannableString("초등");
        elementary.setSpan(new ForegroundColorSpan(redColor), 0, elementary.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString middleSchool = new SpannableString("중등");
        middleSchool.setSpan(new ForegroundColorSpan(redColor), 0, middleSchool.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString highSchool = new SpannableString("고등");
        highSchool.setSpan(new ForegroundColorSpan(redColor), 0, highSchool.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        LogMgr.i(TAG, "reserveDate : " + mTvReserveDate.getText().toString());
        String dateString = mTvReserveDate.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            if (date != null) calendar.setTime(date);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                testTime.add(elementary);
                testTime.add(new SpannableString("평일 오후 05:00"));
                testTime.add(new SpannableString("평일 오후 07:00"));
                testTime.add(middleSchool);
                testTime.add(new SpannableString("평일 오후 05:00"));
                testTime.add(new SpannableString("평일 오후 07:00"));
                testTime.add(highSchool);
                testTime.add(new SpannableString("평일 오후 04:00"));
                testTime.add(new SpannableString("평일 오후 07:00"));

            }else if (dayOfWeek == Calendar.SATURDAY){
                testTime.add(elementary);
                testTime.add(new SpannableString("토요일 오전 11:00"));
                testTime.add(new SpannableString("토요일 오후 01:00"));

                testTime.add(middleSchool);
                testTime.add(new SpannableString("토요일 오전 11:00"));
                testTime.add(new SpannableString("토요일 오후 01:00"));

                testTime.add(highSchool);
                testTime.add(new SpannableString("토요일 오후 03:00"));
                testTime.add(new SpannableString("토요일 오후 07:00"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mSpinnerTestTime.setItems(testTime);
    }

    private void setSchoolSpinner(){

        List<SchoolData> schoolList = DataManager.getInstance().getSchoolList();
        List<String> scNames = new ArrayList<>();
        List<Integer> scCodes = new ArrayList<>();

        if (_stGrade.contains(getString(R.string.informed_question_elementary))){
            for (SchoolData data : schoolList) {
                if (!data.scName.contains(middleSchool) && !data.scName.contains(highSchool)){
                    scNames.add(data.scName);
                    scCodes.add(data.scCode);
                }
            }
        }else if (_stGrade.contains(getString(R.string.informed_question_middle))){
            for (SchoolData data : schoolList) {
                if (!data.scName.contains(elementary) && !data.scName.contains(highSchool)){
                    scNames.add(data.scName);
                    scCodes.add(data.scCode);
                }
            }
        }else{
            for (SchoolData data : schoolList) {
                if (!data.scName.contains(elementary) && !data.scName.contains(middleSchool)){
                    scNames.add(data.scName);
                    scCodes.add(data.scCode);
                }
            }
        }

        if (!writeMode.equals(Constants.WRITE_EDIT)) mSpinnerSchool.setText("");
        mSpinnerSchool.setItems(scNames);
        mSpinnerSchool.setSpinnerPopupHeight(800);

        mSpinnerSchool.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _scCode = scCodes.get(newIndex);

            if (_stGrade.contains(getString(R.string.informed_question_elementary))){
                for (SchoolData data : schoolList) {
                    if (!data.scName.contains(middleSchool) && !data.scName.contains(highSchool)){
                        scNames.add(data.scName);
                        scCodes.add(data.scCode);
                    }
                }
            }else if (_stGrade.contains(getString(R.string.informed_question_middle))){
                for (SchoolData data : schoolList) {
                    if (!data.scName.contains(elementary) && !data.scName.contains(highSchool)){
                        scNames.add(data.scName);
                        scCodes.add(data.scCode);
                    }
                }
            }else{
                for (SchoolData data : schoolList) {
                    if (!data.scName.contains(elementary) && !data.scName.contains(middleSchool)){
                        scNames.add(data.scName);
                        scCodes.add(data.scCode);
                    }
                }
            }
        });
    }

    private void startQuestionActivity(){
        Utils.clearFocus(mEditList);
        Utils.hideKeyboard(mContext, mEditList);

        if (mGenderRbMale.isChecked()) gender = 1;
        else gender =2;

        if (writeMode.equals(Constants.WRITE_EDIT)) request = new LevelTestRequest();

        request.name = mEtName.getText().toString(); // 학생이름 [필수]
        request.birth = mTvBirthDate.getText().toString(); // 생년월일 [필수]
        request.sex = gender; // 성별 (1 남자, 2 여자) [필수]
        request.address = mTvAddress.getText().toString(); // 주소 (도로명주소) [선택]
        request.addressSub = mEtAddressDetail.getText().toString(); // 상세주소 [선택]
        request.scCode = _scCode; // 학교코드 [필수]
        request.grade = _stGrade.replace(getString(R.string.test_reserve_write_grade_sub), ""); // 학년 [필수]
        request.phoneNumber = mEtStuPhone.getText().toString(); // 학생 전화번호 [선택]
        request.parentPhoneNumber = mEtparentPhone.getText().toString(); // 학부모 연락처 [필수]
        request.parentName = mEtParentName.getText().toString(); // 학부모 성함 [필수]
        request.reason = _stReason; // 유입경로 [선택]
        request.reservationDate = mTvReserveDate.getText().toString(); // 테스트예약일 [필수]
        request.bigo = _ltcCode; // 캠퍼스 비고 [필수]
        request.bigoText = _ltcName; // 캠퍼스 비고(캠퍼스 이름) [필수]
        request.cashReceiptNumber = TextUtils.isEmpty(mEtCashReceipt.getText().toString()) ? "010-000-1234" : mEtCashReceipt.getText().toString(); // 현금영수증 (010-000-1234) [선택]
        //request.cashReceiptNumber = Utils.formatNum(mEtCashReceipt.getText().toString()); // 현금영수증 (010-000-1234) [선택]
        // TODO 테스트시간 파라미터 추가되면 데이터 만들기

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
            if (mSpinnerGrade != null) mSpinnerGrade.show();

        } else if (mSpinnerSchool.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.school_empty, Toast.LENGTH_SHORT).show();
            if (mSpinnerSchool != null) mSpinnerSchool.show();

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
            if (mSpinnerFunnel != null) mSpinnerFunnel.show();

        } else if (request.bigo.equals("")) {
            Toast.makeText(mContext, R.string.bigo_empty, Toast.LENGTH_SHORT).show();
            if (mSpinnerCampus != null) mSpinnerCampus.show();

        } else if (request.reservationDate.equals("")) {
            Toast.makeText(mContext, R.string.reservation_date_empty, Toast.LENGTH_SHORT).show();
            showDatePicker(mTvReserveDate, true, birthMinYear, birthMaxYear, false);

        }else{
            // TODO : 체크리스트에 테스트시간 값도 입력여부 체크하기
            Intent intent = new Intent(mContext, InformedQuestionActivity.class);
            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_WRITE, request);
            intent.putExtra(IntentParams.PARAM_WRITE_MODE, writeMode);
            if (writeMode.equals(Constants.WRITE_EDIT)) {
                intent.putExtra(IntentParams.PARAM_LIST_ITEM, mInfo);
            }
            resultLauncher.launch(intent);
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
            case R.id.action_next:
                startQuestionActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
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