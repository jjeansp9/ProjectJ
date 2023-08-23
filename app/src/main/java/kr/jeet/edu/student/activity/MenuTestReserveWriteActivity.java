package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.DatePickerFragment;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.dialog.SearchAddressDialog;

public class MenuTestReserveWriteActivity extends BaseActivity {

    private static final String TAG = "MenuTestReserveWriteActivity";

    private TextView mTvReserveDate, mTvBirthDate, mTvAddress;
    private EditText mEtName, mEtAddressDetail, mEtStuContact, mEtParentName, mEtparentContact, mEtCashReceipt;
    private EditText[] mEditList;
    private RadioGroup mRgGender;
    private RadioButton mGenderRbMale, mGenderRbFemale;
    private PowerSpinnerView mSpinnerSchool, mSpinnerGrade, mSpinnerFunnel, mSpinnerCampus, mSpinnerTestClass, mSpinnerTestDay, mSpinnerTestTime;

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

    private LevelTestRequest request = new LevelTestRequest();
    private SearchAddressDialog mDialog = null;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED) && "finish_activity".equals(intent.getAction())) {
                intent.putExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, true);
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

    private void initData(){
        _selectedDate = new Date();
        Calendar calendar = Calendar.getInstance();
        testDateMinYear = calendar.get(Calendar.YEAR);
        birthMaxYear = calendar.get(Calendar.YEAR);

        try {

            Intent intent= getIntent();
            request = intent.getParcelableExtra(IntentParams.PARAM_TEST_RESERVE_WRITE);

            if (request != null) {

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

        mTvReserveDate = findViewById(R.id.tv_reserve_test_date_cal);
        mTvBirthDate = findViewById(R.id.tv_reserve_birth_date_cal);
        mTvAddress = findViewById(R.id.tv_reserve_address_result);

        mEtName = (EditText) findViewById(R.id.et_reserve_name);
        mEtAddressDetail = (EditText) findViewById(R.id.et_reserve_address_detail);
        mEtStuContact = (EditText) findViewById(R.id.et_reserve_stu_contact);
        mEtParentName = (EditText) findViewById(R.id.et_reserve_parent_name);
        mEtparentContact = (EditText) findViewById(R.id.et_reserve_parent_contact);
        mEtCashReceipt = (EditText) findViewById(R.id.et_reserve_cash_receipts);
        mEditList = new EditText[]{mEtName, mEtAddressDetail, mEtStuContact, mEtParentName, mEtparentContact, mEtCashReceipt};

        mRgGender = (RadioGroup) findViewById(R.id.rg_test_reserve_gender);
        mGenderRbMale = (RadioButton) findViewById(R.id.rb_test_reserve_male);
        mGenderRbFemale = (RadioButton) findViewById(R.id.rb_test_reserve_female);

        mSpinnerSchool = findViewById(R.id.spinner_reserve_sc_name);
        mSpinnerGrade = findViewById(R.id.spinner_reserve_grade);
        mSpinnerFunnel = findViewById(R.id.spinner_reserve_funnel);
        mSpinnerCampus = findViewById(R.id.spinner_reserve_campus);
        mSpinnerTestClass = findViewById(R.id.spinner_reserve_test_class);

        setSpinner();

        mGenderRbMale.setChecked(true);
        mGenderRbFemale.setChecked(false);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.test_reserve_write_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.root_reserve_write:
                Utils.clearFocus(mEditList);
                Utils.hideKeyboard(mContext, mEditList);
                break;

            case R.id.layout_reserve_birth:
                showDatePicker(mTvBirthDate, false, birthMinYear, birthMaxYear);
                break;

            case R.id.layout_reserve_test_date:
                showDatePicker(mTvReserveDate, true, testDateMinYear, testDateMaxYear);
                break;

            case R.id.btn_test_reserve_write_next:
                startQuestionActivity();
                break;

            case R.id.btn_address_search:
                if(mDialog != null) mDialog.dismiss();
                mDialog = SearchAddressDialog.newInstance();
                mDialog.showDialog(this, address -> {

                    runOnUiThread(() -> mTvAddress.setText(address));
                    mDialog.dismiss();

                    mDialog = null;
                });
                break;
        }
    }

    void showDatePicker(TextView tv, boolean setDate, int minYear, int maxYear) {
        DatePickerFragment.OnDateSetListener listener = (year, month, day) -> {
            String formattedDate = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day);
            tv.setText(formattedDate);
        };
        DatePickerFragment datePickerDialog = new DatePickerFragment(listener, setDate, minYear, maxYear);

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
        mSpinnerTestClass.setIsFocusable(true);
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
            setSchoolSpinner();
        });

        mSpinnerFunnel.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _stReason = newItem.toString();
        });

        mSpinnerTestClass.setOnTouchListener(spinnerTouchListener);
        mSpinnerFunnel.setOnTouchListener(spinnerTouchListener);
        mSpinnerCampus.setOnTouchListener(spinnerTouchListener);
        mSpinnerGrade.setOnTouchListener(spinnerTouchListener);
        mSpinnerSchool.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    if (mSpinnerGrade.getText().toString().equals("")) {
                        Toast.makeText(mContext, R.string.test_reserve_grade_sel_please, Toast.LENGTH_SHORT).show();
                    }
                    Utils.clearFocus(mEditList);
                    Utils.hideKeyboard(mContext, mEditList);
                    break;
            }
            return false;
        });

        testTime = new ArrayList<>();

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

        testTime.add(elementary);
        testTime.add(new SpannableString("평일 오후 05:00"));
        testTime.add(new SpannableString("평일 오후 07:00"));
        testTime.add(new SpannableString("토요일 오전 11:00"));
        testTime.add(new SpannableString("토요일 오후 01:00"));

        testTime.add(middleSchool);
        testTime.add(new SpannableString("평일 오후 05:00"));
        testTime.add(new SpannableString("평일 오후 07:00"));
        testTime.add(new SpannableString("토요일 오전 11:00"));
        testTime.add(new SpannableString("토요일 오후 01:00"));

        testTime.add(highSchool);
        testTime.add(new SpannableString("평일 오후 04:00"));
        testTime.add(new SpannableString("평일 오후 07:00"));
        testTime.add(new SpannableString("토요일 오후 03:00"));
        testTime.add(new SpannableString("토요일 오후 07:00"));

        mSpinnerTestClass.setItems(testTime);
        mSpinnerTestClass.setSpinnerPopupHeight(500);

        // 초등 index : 0, 중등 : 5, 고등 : 10
        mSpinnerTestClass.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex == 0){
                strTestTime = testTime.get(newIndex + 1).toString();
                mSpinnerTestClass.setText(testTime.get(newIndex + 1));

            }else if (newIndex == 5){
                strTestTime = testTime.get(newIndex + 1).toString();
                mSpinnerTestClass.setText(testTime.get(newIndex + 1));

            }else if (newIndex == 10){
                strTestTime = testTime.get(newIndex + 1).toString();
                mSpinnerTestClass.setText(testTime.get(newIndex + 1));
            }
        });
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

        mSpinnerSchool.setText("");
        mSpinnerSchool.setItems(scNames);
        mSpinnerSchool.setSpinnerPopupHeight(800);

        mSpinnerSchool.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            _scCode = scCodes.get(newIndex);

        });
    }

    private void startQuestionActivity(){
        Utils.clearFocus(mEditList);
        Utils.hideKeyboard(mContext, mEditList);

        if (mGenderRbMale.isChecked()) gender = 1;
        else gender =2;

        request.name = mEtName.getText().toString(); // 학생이름 [필수]
        request.birth = mTvBirthDate.getText().toString(); // 생년월일 [필수]
        request.sex = gender; // 성별 (1 남자, 2 여자) [필수]
        request.address = mTvAddress.getText().toString(); // 주소 (도로명주소) [선택]
        request.addressSub = mEtAddressDetail.getText().toString(); // 상세주소 [선택]
        request.scCode = _scCode; // 학교코드 [필수]
        request.grade = _stGrade.replace(getString(R.string.test_reserve_write_grade_sub), ""); // 학년 [필수]
        request.phoneNumber = mEtStuContact.getText().toString(); // 학생 전화번호 [선택]
        request.parentPhoneNumber = mEtparentContact.getText().toString(); // 학부모 연락처 [필수]
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

        if (request.name.equals("") || request.birth.equals("") || request.grade.equals("") || mSpinnerSchool.getText().toString().equals("") ||
                request.parentPhoneNumber.equals("") || request.parentName.equals("") || request.reservationDate.equals("") ||
                request.bigo.equals("") || request.address.equals("") || request.reason.equals("")){
            Toast.makeText(mContext, R.string.write_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(mContext, InformedQuestionActivity.class);
        intent.putExtra(IntentParams.PARAM_TEST_RESERVE_WRITE, request);
        resultLauncher.launch(intent);
    }

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