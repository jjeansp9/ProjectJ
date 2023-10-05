package kr.jeet.edu.student.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.skydoves.powerspinner.PowerSpinnerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.SchoolListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.dialog.SchoolListBottomSheetDialog;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.request.BriefingReserveRequest;
import kr.jeet.edu.student.model.response.BriefingReserveResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.ClearableTextView;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingWriteActivity extends BaseActivity {

    private final static String TAG = "BriefingWriteActivity";

    private CheckBox cbPrivacy;
    private TextView mTvPrivacy;
    private Button btnComplete;
    private ImageView mBtnSub, mBtnAdd;
    private EditText mEtName, mEtPhoneNum, mEtEmail, mEtSchool;
    private EditText[] mEtList;
    ClearableTextView tvSchool;
    SchoolListBottomSheetDialog _schoolListBottomSheetDialog;
    SchoolListAdapter _schoolListAdapter;
    private PowerSpinnerView mSpinnerGrade;

    private RetrofitApi mRetrofitApi;

    private String _scName ="";
    private String _stGrade = "";
    private int _scCode = 0;

    private BriefingReserveRequest request;

    private boolean isFilterTriggerChanged = false;
    private SchoolData _selectedSchoolData = null;
    List<SchoolData> _schoolList;

    private int ptSeq = 0;
    private int _memberSeq = 0;
    private int perCnt = 0;

    private final int ADD = 1;
    private final int SUBTRACT = -1;
    private final boolean TYPE_ADD = false;
    private final boolean TYPE_SUB = true;
    private final int MIN_CNT = 1;
    private final int MAX_CNT = 9999;

    private String url = "";
    private final int PUT_CNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_write);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        Intent intent= getIntent();
        if (intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)){
            ptSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, ptSeq);
        }
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _selectedSchoolData = new SchoolData("", 0);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_write_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void initView() {
        findViewById(R.id.root_brf_write).setOnClickListener(this);
        findViewById(R.id.layout_brf_privacy).setOnClickListener(this);
        findViewById(R.id.layout_brf_view_privacy).setOnClickListener(this);
        findViewById(R.id.btn_brf_write_complete).setOnClickListener(this);
        //findViewById(R.id.img_cnt_sub).setOnClickListener(this);
        //findViewById(R.id.img_cnt_add).setOnClickListener(this);

        tvSchool = findViewById(R.id.tv_content_school);
        btnComplete = findViewById(R.id.btn_brf_write_complete);

        mTvPrivacy = findViewById(R.id.tv_brf_privacy);
        cbPrivacy = findViewById(R.id.cb_brf_check_privacy);

        mEtName = findViewById(R.id.et_brf_write_name);
        mEtPhoneNum = findViewById(R.id.et_brf_write_phone_num);
        //mEtEmail = findViewById(R.id.et_brf_write_email);
        //mEtPersonnel = findViewById(R.id.et_brf_write_personnel);
        //mEtSchool = findViewById(R.id.et_brf_write_school);
        mSpinnerGrade = findViewById(R.id.spinner_reserve_grade);

        mEtList = new EditText[]{mEtName, mEtPhoneNum};

        mSpinnerGrade.setIsFocusable(true);
        mSpinnerGrade.setOnTouchListener(spinnerTouchListener);

        mSpinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex > 0) _stGrade = newItem.toString();
            else {
                mSpinnerGrade.setText("");
                _stGrade = "";
            }
            _scCode = 0;
            //_schoolList = null;
            if (!TextUtils.isEmpty(_stGrade)) setSchoolSpinner();
            tvSchool.setText("");
        });

        setSchoolSpinner();

        //mEtPersonnel.setText("1");
        //perCnt = Integer.parseInt(mEtPersonnel.getText().toString());

        // 0 입력 막기
//        mEtPersonnel.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() == 1) if ("0".equals(s.toString())) mEtPersonnel.setText("");
//                if (s.length() >= 2 && s.charAt(0) == '0') mEtPersonnel.getText().replace(0, 1, "");
//                if (s.length() > 4){
//                    mEtPersonnel.setText(s.subSequence(0, 4));
//                    mEtPersonnel.setSelection(4);
//                }
//                if (s.length() > 0) perCnt = Integer.parseInt(s.toString());
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//        mEtPersonnel.setOnFocusChangeListener((view, hasFocus) -> {
//            if (hasFocus) mEtPersonnel.setText("");
//            else mEtPersonnel.setText(String.valueOf(perCnt));
//        });

//        mEtEmail.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (count > before) { // 문자를 지울때는 실행 안되게
//                    if (s.charAt(start) == ' ') { // 새로 추가된 문자가 공백인지 체크
//                        mEtEmail.setText(s.toString().replace(" ", ""));
//                        mEtEmail.setSelection(start); // 커서 위치 설정
//                    }
//                }
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener spinnerTouchListener = (v, event) -> {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Utils.clearFocus(mEtList);
                Utils.hideKeyboard(mContext, mEtList);
                break;
        }
        return false;
    };

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
                Utils.clearFocus(mEtList);
                Utils.hideKeyboard(mContext, mEtList);
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.root_brf_write:
                Utils.clearFocus(mEtList);
                Utils.hideKeyboard(mContext, mEtList);
                break;

            case R.id.layout_brf_privacy:
                cbPrivacy.setChecked(!cbPrivacy.isChecked());
                if (cbPrivacy.isChecked()) btnComplete.setBackgroundResource(R.drawable.selector_bt_ubderbox);
                else btnComplete.setBackgroundResource(R.drawable.bt_click_cancel);
                break;

            case R.id.layout_brf_view_privacy: // 개인정보취급방침
                url = Constants.POLICY_PRIVACY_PT;
                startPvyActivity(mTvPrivacy.getText().toString());
                break;

            case R.id.btn_brf_write_complete:
                if (checkWrite()) requestBrfReserve();
                break;

//            case R.id.img_cnt_sub:
//                addOrSubCnt(SUBTRACT, TYPE_SUB);
//                break;
//
//            case R.id.img_cnt_add:
//                addOrSubCnt(ADD, TYPE_ADD);
//                break;
        }
    }

//    private void addOrSubCnt(int num, boolean type){
//
//        if (perCnt > MIN_CNT && type == TYPE_SUB){
//            perCnt += num;
//            mEtPersonnel.setText(String.valueOf(perCnt));
//
//        }else if (perCnt < MAX_CNT && type == TYPE_ADD){
//            perCnt += num;
//            mEtPersonnel.setText(String.valueOf(perCnt));
//        }
//        mEtPersonnel.clearFocus();
//    }

    private void requestBrfReserve(){
        requestData();

        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.requestBrfReserve(request).enqueue(new Callback<BriefingReserveResponse>() {
                @Override
                public void onResponse(Call<BriefingReserveResponse> call, Response<BriefingReserveResponse> response) {
                    try {
                        String getData = "";

                        if (response.isSuccessful() && response.body() != null){
                            getData= response.body().msg;

                            if (getData != null) LogMgr.i(TAG, getData);

                            Intent intent = getIntent();
                            intent.putExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, true);
                            setResult(RESULT_OK, intent);
                            finish();
                            Toast.makeText(mContext, R.string.briefing_write_success, Toast.LENGTH_SHORT).show();

                        }else{
                            try {
                                String body = response.errorBody().string();
                                JSONObject jsonObject = new JSONObject(body);
                                String errMsg = jsonObject.getString("errMsg");
                                Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            LogMgr.e(TAG, "requestBrfReserve() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestBrfReserve() Exception : ", e.getMessage()); }
                }

                @Override
                public void onFailure(Call<BriefingReserveResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestBrfReserve() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestBrfReserve() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestData(){
        request = new BriefingReserveRequest();

        request.ptSeq = ptSeq;
        request.memberSeq = _memberSeq;
        request.name = mEtName.getText().toString();
        request.phoneNumber = mEtPhoneNum.getText().toString().trim();
        //request.email = mEtEmail.getText().toString().trim();
        //request.participantsCnt = Integer.parseInt(mEtPersonnel.getText().toString().trim());
        request.participantsCnt = PUT_CNT; // 참석자는 무조건 1로 보내기
        if (!TextUtils.isEmpty(_selectedSchoolData.scName)) request.schoolNm = _selectedSchoolData.scName;
        if (!TextUtils.isEmpty(_stGrade)) request.grade = _stGrade.replace(getString(R.string.test_reserve_write_grade_sub), "");;

        LogMgr.i(TAG, "== putData ==" +
                "\nptSeq: " + request.ptSeq +
                "\nmemberSeq: " + request.memberSeq +
                "\nname: " + request.name +
                "\nphoneNumber: " + request.phoneNumber +
                "\nparticipantsCnt: " + request.participantsCnt +
                "\nschoolNm: " + request.schoolNm +
                "\ngrade: " + request.grade
                );
    }

    private boolean checkWrite(){

        if (mEtName.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.briefing_write_empty_stu_name, Toast.LENGTH_SHORT).show();
            mEtName.requestFocus();
            Utils.showKeyboard(mContext, mEtName);
            return false;

        } else if (mEtPhoneNum.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.briefing_write_empty_phone_num, Toast.LENGTH_SHORT).show();
            mEtPhoneNum.requestFocus();
            Utils.showKeyboard(mContext, mEtPhoneNum);
            return false;

        } else if (!Utils.checkPhoneNumber(mEtPhoneNum.getText().toString())){
            Toast.makeText(mContext, R.string.write_phone_impossible, Toast.LENGTH_SHORT).show();
            mEtPhoneNum.requestFocus();
            Utils.showKeyboard(mContext, mEtPhoneNum);
            return false;

        } else if (!cbPrivacy.isChecked()) {
            Toast.makeText(mContext, R.string.write_privacy_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
//        else if(!Utils.checkEmailForm(mEtEmail.getText().toString())){
//            Toast.makeText(mContext, R.string.write_check_email, Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        else if (Integer.parseInt(mEtPersonnel.getText().toString()) < 1){
//            Toast.makeText(mContext, R.string.briefing_write_please_per_cnt, Toast.LENGTH_SHORT).show();
//            return false;
//        }
        else {
            return true;
        }
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }
}