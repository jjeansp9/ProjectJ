package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.data.TestTimeData;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.response.TestTimeResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuTestReserveDetailActivity extends BaseActivity {

    private final static String TAG = MenuTestReserveDetailActivity.class.getSimpleName();


    private TestReserveData mInfo;
    private LevelTestRequest updateInfo;
    boolean edited = false;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_EDITED) && Constants.FINISH_COMPLETE.equals(intent.getAction())){
                LogMgr.e(TAG, "resultLauncher Event EDIT");
                edited = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, false);
                if (edited) {
//                    if(intent.hasExtra(IntentParams.PARAM_SUCCESS_DATA)) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                            updateInfo = intent.getParcelableExtra(IntentParams.PARAM_SUCCESS_DATA, LevelTestRequest.class);
//                        }else{
//                            updateInfo = intent.getParcelableExtra(IntentParams.PARAM_SUCCESS_DATA);
//                        }
//                        initView();
//                    }
                    intent.putExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test_reserve_detail);
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_LIST_ITEM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, TestReserveData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
            }
        }

        if(mInfo == null) finish();

        initAppbar();
        initView();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        String str = "";

        ((TextView)findViewById(R.id.txt_st_nm)).setText(TextUtils.isEmpty(mInfo.name) ? "" : mInfo.name);

        str = "";
        if(mInfo.sex != null) {
            str = mInfo.sex.equals("M") ? getString(R.string.male) : getString(R.string.female);
        }
        ((TextView)findViewById(R.id.txt_gender)).setText(str);

        str = TextUtils.isEmpty(mInfo.address) ? "" : mInfo.address;
        if(!TextUtils.isEmpty(mInfo.addressSub)) {
            str += (TextUtils.isEmpty(str) ? "" : " ") + mInfo.addressSub;
        }
        ((TextView)findViewById(R.id.txt_address)).setText(str);

        ((TextView)findViewById(R.id.txt_birth)).setText(TextUtils.isEmpty(mInfo.birth) ? "" : mInfo.birth);

        str = "";
        for(SchoolData info : DataManager.getInstance().getSchoolList()) {
            if(mInfo.scCode == info.scCode) {
                str = TextUtils.isEmpty(info.scName) ? "" : (info.scName + " ");
                break;
            }
        }
        if(!TextUtils.isEmpty(mInfo.grade)) {
            try {
                if (mInfo.grade.length() > 0) {
                    int lastChar = mInfo.grade.length() - 1;
                    str += TextUtils.isEmpty(mInfo.grade) ? "" : mInfo.grade.replace("학년", "").charAt(lastChar)+"학년";
                }
            }catch (Exception e) {
                LogMgr.e(TAG, e.getMessage());
            }
        }
        ((TextView)findViewById(R.id.txt_school)).setText(str);

        ((TextView)findViewById(R.id.txt_st_phone_num)).setText(TextUtils.isEmpty(mInfo.phoneNumber) ? "" : Utils.formatCashReceiptNum(mInfo.phoneNumber));

        str = "";
        if(!TextUtils.isEmpty(mInfo.parentName)) {
            str = mInfo.parentName +" "+ getString(R.string.parents);
        }
        if(!TextUtils.isEmpty(mInfo.parentPhoneNumber)) {
            str += TextUtils.isEmpty(str) ? "" : "\n" + Utils.formatCashReceiptNum(mInfo.parentPhoneNumber);
        }

        ((TextView)findViewById(R.id.txt_parent)).setText(str);

        ((TextView)findViewById(R.id.txt_cash_receipts)).setText(TextUtils.isEmpty(mInfo.cashReceiptNumber) ? "" : mInfo.cashReceiptNumber);
        ((TextView)findViewById(R.id.txt_reason)).setText(TextUtils.isEmpty(mInfo.reason) ? "" : mInfo.reason);

        for(LTCData info : DataManager.getInstance().getLTCList()) {
            if(info.ltcCode.equals(mInfo.bigo)) {
                ((TextView)findViewById(R.id.txt_campus)).setText(TextUtils.isEmpty(info.ltcName) ? "" : (info.ltcName));
                break;
            }
        }

        ((TextView)findViewById(R.id.txt_subject)).setText(TextUtils.isEmpty(mInfo.subjectName) ? "" : mInfo.subjectName);
        ((TextView)findViewById(R.id.txt_insert_date)).setText(TextUtils.isEmpty(mInfo.registerDate) ? "" : mInfo.registerDate);

        str = "";
        if(!TextUtils.isEmpty(mInfo.reservationDate)) {
            str = mInfo.reservationDate;
        }
        ((TextView)findViewById(R.id.txt_wish_date)).setText(str);

        if(!TextUtils.isEmpty(mInfo.wish)) {
            if (mInfo.wish.equals("0")) {
                str=getString(R.string.informed_question_sel_day_1);
            }else if (mInfo.wish.equals("1")) {
                str=getString(R.string.informed_question_sel_day_2);
            }else if (mInfo.wish.equals("2")) {
                str=getString(R.string.informed_question_sel_day_3);
            }else if (mInfo.wish.equals("3")) {
                str=getString(R.string.informed_question_sel_day_4);
            }else{
                ((ConstraintLayout)findViewById(R.id.layout_wish_day_week)).setVisibility(View.GONE);
            }
        }else{
            ((ConstraintLayout)findViewById(R.id.layout_wish_day_week)).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.txt_wish_day_week)).setText(str);

        {   // 이전학습방법
            //str = "학원 : ";
            str = "";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.progressName1)) {
                if (mInfo.progressName1.contains("교습소")) subStr += mInfo.progressName1;
                else subStr += removeLastSpace(mInfo.progressName1.replace("학원", "")) + " 학원";
            }
            if(!TextUtils.isEmpty(mInfo.time1)) {
                if(TextUtils.isEmpty(mInfo.progressName1)) subStr += mInfo.time1;
                else subStr += "\n" + mInfo.time1;
            }
            if(!TextUtils.isEmpty(mInfo.date1)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date1;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_1)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_1)).setText(str);

            //str += "\n과외 : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time2)) subStr += mInfo.time2;
            if(!TextUtils.isEmpty(mInfo.date2)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date2;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_2)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_2)).setText(str);

            //str += "\n가정학습(자기주도) : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time3)) subStr += mInfo.time3;
            if(!TextUtils.isEmpty(mInfo.date3)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date3;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_3)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_3)).setText(str);

            //str += "\n학습지 : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time4)) subStr += mInfo.time4;
            if(!TextUtils.isEmpty(mInfo.date4)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date4;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_4)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_4)).setText(str);
        }

        {   // 진도/심화학습
            str = "";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc1)) {
                if(!TextUtils.isEmpty(mInfo.processText1)) subStr += "[ "+mInfo.processText1+" ] ";
                subStr += mInfo.processEtc1;
            }
            str += subStr.isEmpty() ? "" : subStr;

            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc2)) {
                if(!TextUtils.isEmpty(mInfo.processText2)) subStr += "[ "+mInfo.processText2+" ] ";
                subStr += mInfo.processEtc2;
            }
            str += subStr.isEmpty() ? "" : (str.isEmpty() ? subStr : "\n" + subStr);

            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc3)) {
                if(!TextUtils.isEmpty(mInfo.processText3)) subStr += "[ "+mInfo.processText3+" ] ";
                subStr += mInfo.processEtc3;
            }
            str += subStr.isEmpty() ? "" : (str.isEmpty() ? subStr : "\n" + subStr);

            ((TextView)findViewById(R.id.txt_process)).setText(str);
        }

        if(!TextUtils.isEmpty(mInfo.study)) {
            str = "";
            String[] arrStudy = mInfo.study.split("\\^");
            for(int i = 0; i < arrStudy.length; i++) {
                str += ", " + arrStudy[i];
            }
            str = str.replaceFirst(", ", "");
            ((TextView)findViewById(R.id.txt_option)).setText(str);
        }else {
            ((ConstraintLayout)findViewById(R.id.ly_option)).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(mInfo.highSchool)) {
            str = "";
            String[] arrStudy = mInfo.highSchool.split("\\^");
            for (String s : arrStudy) {
                str += ", " + s;
            }
            str = str.replaceFirst(", ", "");
            ((TextView)findViewById(R.id.txt_wish_high_school)).setText(str);
        }else {
            ((ConstraintLayout)findViewById(R.id.ly_wish_high_school)).setVisibility(View.GONE);
        }

        str = "";
        if(!TextUtils.isEmpty(mInfo.gifted) && mInfo.grade.contains("중")) {
            str += "※ 지트 중등영재센터 입학 ";
            str += mInfo.gifted.equals("Y") ? "희망" : "미희망";
            if(!TextUtils.isEmpty(mInfo.etc)) str += "\n\n";
        }
        if(!TextUtils.isEmpty(mInfo.etc)) str += mInfo.etc;
        if(!str.isEmpty()) ((TextView)findViewById(R.id.txt_etc)).setText(str);
        else {
            ((ConstraintLayout)findViewById(R.id.ly_etc)).setVisibility(View.GONE);
        }
    }

    private String removeLastSpace(String str) {
        if (str == null || str.length() == 0) return str;

        char lastChar = str.charAt(str.length() - 1);
        if (lastChar == ' ') str = str.substring(0, str.length() - 1);

        return str;
    }

    private void requestTestTime() {
        showProgressDialog();
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getTestTime().enqueue(new Callback<TestTimeResponse>() {
                @Override
                public void onResponse(Call<TestTimeResponse> call, Response<TestTimeResponse> response) {

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                ArrayList<TestTimeData> getData = response.body().data;

                                if (getData == null || getData.isEmpty()){
                                    Toast.makeText(mContext, R.string.menu_test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent editIntent = new Intent(mContext, MenuTestReserveWriteActivity.class);
                                    editIntent.putExtra(IntentParams.PARAM_LIST_ITEM, mInfo);
                                    editIntent.putExtra(IntentParams.PARAM_WRITE_MODE, Constants.WRITE_EDIT);
                                    resultLauncher.launch(editIntent);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.menu_test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestTime() Exception : ", e.getMessage());
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        LogMgr.i(TAG, "Edited is true? : "+edited);
        if (edited) {
            Intent intent = getIntent();
            intent.putExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, true);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit:
                requestTestTime();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}