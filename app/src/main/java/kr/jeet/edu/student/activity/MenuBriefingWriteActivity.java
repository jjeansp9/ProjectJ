package kr.jeet.edu.student.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.skydoves.powerspinner.PowerSpinnerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.request.BriefingReserveRequest;
import kr.jeet.edu.student.model.response.BriefingReserveResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingWriteActivity extends BaseActivity {

    private final static String TAG = "BriefingWriteActivity";

    private CheckBox cbPrivacy;
    private TextView mTvPrivacy;
    private EditText mEtName, mEtPhoneNum, mEtEmail, mEtPersonnel, mEtSchool;
    private EditText[] mEtList;
    //private PowerSpinnerView mSpinnerSchool, mSpinnerSchoolType;

    private RetrofitApi mRetrofitApi;

    private String _scName ="";

    private BriefingReserveRequest request;

    private int ptSeq = 0;
    private int _memberSeq = 0;

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
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_write_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.root_brf_write).setOnClickListener(this);
        findViewById(R.id.layout_brf_privacy).setOnClickListener(this);
        findViewById(R.id.layout_brf_view_privacy).setOnClickListener(this);
        findViewById(R.id.btn_brf_write_complete).setOnClickListener(this);

        mTvPrivacy = findViewById(R.id.tv_brf_privacy);
        cbPrivacy = findViewById(R.id.cb_brf_check_privacy);

        mEtName = findViewById(R.id.et_brf_write_name);
        mEtPhoneNum = findViewById(R.id.et_brf_write_phone_num);
        mEtEmail = findViewById(R.id.et_brf_write_email);
        mEtPersonnel = findViewById(R.id.et_brf_write_personnel);
        mEtSchool = findViewById(R.id.et_brf_write_school);

        mEtPersonnel.setText("1");

        mEtPersonnel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) if ("0".equals(s.toString())) mEtPersonnel.setText("");
                if (s.length() >= 2 && s.charAt(0) == '0') mEtPersonnel.getText().replace(0, 1, "");
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cleanedEmail = mEtEmail.getText().toString().replaceAll(" ", "");
                mEtEmail.setText(cleanedEmail);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mEtList = new EditText[]{mEtName, mEtPhoneNum, mEtEmail, mEtPersonnel, mEtSchool};

        //setSpinner();
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private void setSpinner(){
//        mSpinnerSchool = findViewById(R.id.spinner_brf_write_school);
//        mSpinnerSchoolType = findViewById(R.id.spinner_brf_write_school_type);
//
//        mSpinnerSchool.setOnTouchListener((v, event) -> {
//            switch (event.getAction()){
//                case MotionEvent.ACTION_UP:
//                    if (mSpinnerSchoolType.getText().toString().equals("")){
//                        Toast.makeText(mContext, R.string.briefing_write_please_sel, Toast.LENGTH_SHORT).show();
//                    }
//                    Utils.clearFocus(mEtList);
//                    Utils.hideKeyboard(mContext, mEtList);
//                    break;
//            }
//
//            return false;
//        });
//        mSpinnerSchoolType.setOnTouchListener((v, event) -> {
//            switch (event.getAction()){
//                case MotionEvent.ACTION_UP:
//                    Utils.clearFocus(mEtList);
//                    Utils.hideKeyboard(mContext, mEtList);
//                    break;
//            }
//            return false;
//        });
//
//        mSpinnerSchoolType.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
//            setSpinnerSchool();
//        });
//    }
//
//    private void setSpinnerSchool(){
//        List<SchoolData> schoolList = DataManager.getInstance().getSchoolList();
//        List<String> scNames = new ArrayList<>();
//
//        final String ELEMENTARY = "초등";
//        final String MIDDLE_SCHOOL = "중학교";
//        final String HIGH_SCHOOL = "고등";
//
//        if (mSpinnerSchoolType.getText().toString().contains(getString(R.string.informed_question_elementary))){
//            for (SchoolData data : schoolList) {
//                if (!data.scName.contains(MIDDLE_SCHOOL) && !data.scName.contains(HIGH_SCHOOL)){
//                    scNames.add(data.scName);
//                }
//            }
//        }else if (mSpinnerSchoolType.getText().toString().contains(getString(R.string.informed_question_middle))){
//            for (SchoolData data : schoolList) {
//                if (!data.scName.contains(ELEMENTARY) && !data.scName.contains(HIGH_SCHOOL)){
//                    scNames.add(data.scName);
//                }
//            }
//        }else{
//            for (SchoolData data : schoolList) {
//                if (!data.scName.contains(ELEMENTARY) && !data.scName.contains(MIDDLE_SCHOOL)){
//                    scNames.add(data.scName);
//                }
//            }
//        }
//
//        mSpinnerSchool.setText("");
//        mSpinnerSchool.setItems(scNames);
//
//        mSpinnerSchool.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> _scName = scNames.get(newIndex));
//    }

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
                break;

            case R.id.layout_brf_view_privacy:
                startPvyActivity(mTvPrivacy.getText().toString());
                break;

            case R.id.btn_brf_write_complete:
                if (checkWrite()) requestBrfReserve();
                break;
        }
    }

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
        request.email = mEtEmail.getText().toString().trim();
        request.participantsCnt = Integer.parseInt(mEtPersonnel.getText().toString().trim());
        //request.schoolNm = _scName;
        request.schoolNm = mEtSchool.getText().toString();
    }

    private boolean checkWrite(){
        if (mEtName.getText().toString().equals("") ||
                mEtEmail.getText().toString().equals("") ||
                mEtPhoneNum.getText().toString().equals("") ||
                mEtPersonnel.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.write_empty, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!cbPrivacy.isChecked()){
            Toast.makeText(mContext, R.string.write_privacy_empty, Toast.LENGTH_SHORT).show();
            return false;
        }else if (Integer.parseInt(mEtPersonnel.getText().toString()) < 1){
            Toast.makeText(mContext, R.string.briefing_write_please_per_cnt, Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        startActivity(intent);
    }
}