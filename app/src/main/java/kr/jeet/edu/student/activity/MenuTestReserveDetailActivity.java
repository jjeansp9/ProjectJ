package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class MenuTestReserveDetailActivity extends BaseActivity {

    private final static String TAG = MenuTestReserveDetailActivity.class.getSimpleName();


    private TestReserveData mInfo;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_EDITED) && Constants.FINISH_COMPLETE.equals(intent.getAction())){
                LogMgr.e(TAG, "resultLauncher Event EDIT");
                boolean edited = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, false);
                if (edited) {
                    //requestTestReserveList();
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
        customAppbar.setTitle(R.string.test_reserve_detail_tile);
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
            str = mInfo.sex.equals("Y") ? getString(R.string.male) : getString(R.string.female);
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
            String[] l = mInfo.grade.split("");
            if(l.length > 1) {
//                if(l[0].contains("초")) str = "초등학생 ";
//                else if(l[0].contains("중")) str = "중학생 ";
//                else if(l[0].contains("고")) str = "고등학생 ";
                str += TextUtils.isEmpty(l[1]) ? "" : l[1]+"학년";
            }
        }
        ((TextView)findViewById(R.id.txt_school)).setText(str);

//        str = "";
//        if(!TextUtils.isEmpty(mInfo.grade)) {
//            String[] l = mInfo.grade.split("");
//            if(l.length > 1) {
////                if(l[0].contains("초")) str = "초등학생 ";
////                else if(l[0].contains("중")) str = "중학생 ";
////                else if(l[0].contains("고")) str = "고등학생 ";
//                str += TextUtils.isEmpty(l[1]) ? "" : l[1]+"학년";
//            }
//        }
//        ((TextView)findViewById(R.id.txt_grade)).setText(str);

        ((TextView)findViewById(R.id.txt_st_phone_num)).setText(TextUtils.isEmpty(mInfo.phoneNumber) ? "" : Utils.formatNum(mInfo.phoneNumber));

        str = "";
        if(!TextUtils.isEmpty(mInfo.parentName)) {
            str = mInfo.parentName +" "+ getString(R.string.parents);
        }
        if(!TextUtils.isEmpty(mInfo.parentPhoneNumber)) {
            str += TextUtils.isEmpty(str) ? "" : "\n" + Utils.formatNum(mInfo.parentPhoneNumber);
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
            str = "학원 : ";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.time1)) subStr += mInfo.time1;
            if(!TextUtils.isEmpty(mInfo.date1)) subStr += (subStr.isEmpty()?"":", ") + mInfo.date1;
            str += subStr.isEmpty() ? "-" : subStr;

            str += "\n과외 : ";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time2)) subStr += mInfo.time2;
            if(!TextUtils.isEmpty(mInfo.date2)) subStr += (subStr.isEmpty()?"":", ") + mInfo.date2;
            str += subStr.isEmpty() ? "-" : subStr;

            str += "\n가정학습(자기주도) : ";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time3)) subStr += mInfo.time3;
            if(!TextUtils.isEmpty(mInfo.date3)) subStr += (subStr.isEmpty()?"":", ") + mInfo.date3;
            str += subStr.isEmpty() ? "-" : subStr;

            str += "\n구몬/눈높이/재능 : ";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time4)) subStr += mInfo.time4;
            if(!TextUtils.isEmpty(mInfo.date4)) subStr += (subStr.isEmpty()?"":", ") + mInfo.date4;
            str += subStr.isEmpty() ? "-" : subStr;

            ((TextView)findViewById(R.id.txt_study)).setText(str);
        }

        {   // 진도/심화학습
            str = "";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.processText1)) subStr += "["+mInfo.processText1+"] ";
            if(!TextUtils.isEmpty(mInfo.processEtc1)) subStr += mInfo.processEtc1;
            str += subStr.isEmpty() ? "" : subStr;

            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processText2)) subStr += "["+mInfo.processText2+"] ";
            if(!TextUtils.isEmpty(mInfo.processEtc2)) subStr += mInfo.processEtc2;
            str += subStr.isEmpty() ? "" : (str.isEmpty() ? subStr : "\n" + subStr);

            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processText3)) subStr += "["+mInfo.processText3+"] ";
            if(!TextUtils.isEmpty(mInfo.processEtc3)) subStr += mInfo.processEtc3;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit:
                Intent editIntent = new Intent(mContext, MenuTestReserveWriteActivity.class);
                editIntent.putExtra(IntentParams.PARAM_LIST_ITEM, mInfo);
                editIntent.putExtra(IntentParams.PARAM_WRITE_MODE, Constants.WRITE_EDIT);
                resultLauncher.launch(editIntent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}