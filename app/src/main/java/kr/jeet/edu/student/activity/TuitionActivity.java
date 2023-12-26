package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.AnnouncementListAdapter;
import kr.jeet.edu.student.adapter.MonthlyAttendanceListAdapter;
import kr.jeet.edu.student.adapter.TuitionListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.AttendanceData;
import kr.jeet.edu.student.model.data.AttendanceSummaryData;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.HolidayData;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.model.data.TuitionHeaderData;
import kr.jeet.edu.student.model.response.TuitionResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.calendar.decorator.AttendanceDecorator;
import kr.jeet.edu.student.view.calendar.decorator.HolidayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherMonthDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSaturdayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.OtherSundayDecorator;
import kr.jeet.edu.student.view.calendar.decorator.SelectionDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TuitionActivity extends BaseActivity {

    private final String TAG = "TuitionActivity";

    private TextView mTvTuitionEmpty;
    private RecyclerView mRecyclerTuition;
    private TuitionListAdapter mTuitionAdapter;
    private RetrofitApi mRetrofitApi;

    private ArrayList<Constants.PayListItem> mTuitionList = new ArrayList<>();

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private int _clsCode = 0;
//    private String _clsName = "";
    private String currentDate = "";
    private PushMessage _pushData = null;

    private static final String WEB_VIEW_URL = "https://www.shinhandamoa.com/common/login#payer";

    private ArrayList<TeacherClsData> mListCls = new ArrayList<>();
    private Set<TuitionData> acaName = new HashSet<>();
    private ArrayList<TuitionData> payList = new ArrayList<>();

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent != null && result.getResultCode() != RESULT_CANCELED) {
            requestTuitionList(currentDate);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition);
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DOWN);
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_tuition);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(_pushData == null);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData(){
        currentDate = Utils.currentDate(Constants.DATE_FORMATTER_YYYYMM);

        _userType = PreferenceUtil.getUserIsOriginal(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);

        Intent intent = getIntent();
        if(intent != null) {

            if (intent.hasExtra(IntentParams.PARAM_STU_STCODE)) {
                _stCode = intent.getIntExtra(IntentParams.PARAM_STU_STCODE, _stCode);
            }else {
                _stCode = PreferenceUtil.getUserSTCode(mContext);
            }

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                _pushData = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }
            if (_pushData != null) _stCode = _pushData.stCode;
            else _stCode = PreferenceUtil.getUserSTCode(mContext);
        }
    }


    void initView() {
        initData();

        mTvTuitionEmpty = findViewById(R.id.tv_tuition_empty);

        mRecyclerTuition = findViewById(R.id.recycler_tuition);
        mTuitionAdapter = new TuitionListAdapter(mContext, mTuitionList, this::startWebView);
        mRecyclerTuition.setAdapter(mTuitionAdapter);

        requestTuitionList(currentDate);
    }

    private void startWebView(Constants.PayListItem item) {

        if ( ((TuitionHeaderData)item).acaLink != null || !TextUtils.isEmpty( ((TuitionHeaderData)item).acaLink) ) {

            if (((TuitionHeaderData)item).accountNO == null || ((TuitionHeaderData)item).accountNO.isEmpty()) {
                Toast.makeText(mContext, R.string.menu_stu_info_get_clipboard_empty, Toast.LENGTH_SHORT).show();

            } else {
                Utils.setClipData(mContext, ((TuitionHeaderData)item).accountNO);
                Toast.makeText(mContext, R.string.menu_stu_info_get_account_no, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.menu_stu_info_tuition_title));
                intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, ((TuitionHeaderData)item).acaLink);
                intent.putExtra(IntentParams.PARAM_ACCOUNT_NO, ((TuitionHeaderData)item).accountNO);
                resultLauncher.launch(intent);
            }

        } else { // 가상계좌번호 url이 없는 경우

            if (((TuitionHeaderData)item).acaLinkIsNull == null || ((TuitionHeaderData)item).acaLinkIsNull.isEmpty()){ // 가상계좌번호가 있는 경우
                if (((TuitionHeaderData)item).accountNO == null || ((TuitionHeaderData)item).accountNO.isEmpty()) {
                    Toast.makeText(mContext, R.string.menu_stu_info_get_clipboard_empty, Toast.LENGTH_SHORT).show();

                } else {
                    showDialog(item);
                }
            } else { // acaLink가 없고, acaLinkIsNull이 있는 경우
                showDialog(item);
            }
        }
    }

    private void showDialog(Constants.PayListItem item) {
        String title = getString(R.string.dialog_title_alarm);
        String msg = "";
        if (TextUtils.isEmpty(((TuitionHeaderData)item).accountNO)) {
            msg = "계좌정보가 없습니다";
        } else {
            msg = ((TuitionHeaderData)item).accountNO;

            String result = Utils.setClipData(mContext, msg);
            if (!TextUtils.isEmpty(result)) msg += "\n계좌번호를 복사하였습니다.";
            else msg += "\n계좌번호를 복사하지 못했습니다.";
        }

        showMessageDialog(title, msg, v -> hideMessageDialog(), null, false);
    }

    private void initHeaderData(ArrayList<TuitionData> item) {

        acaName.clear();
        payList.clear();

        acaName.addAll(item);
        payList.addAll(item);

        int payTotal = 0;

        for (TuitionData data : acaName) {
            LogMgr.e(TAG, "acaName: " + data.acaName);

            if (!TextUtils.isEmpty(data.acaName)){
                TuitionHeaderData headerData = new TuitionHeaderData();
                try {
                    for (TuitionData pay : payList) if (data.acaName.equals(pay.acaName)) payTotal += Integer.parseInt(pay.payment.replace(",", ""));
                }catch (NumberFormatException e){

                }

                headerData.acaName = data.acaName;
                headerData.accountNO = data.accountNO;
                headerData.acaLink = data.acaLink;
                headerData.acaLinkIsNull = data.acaLinkIsNull;
                headerData.payment = Utils.decimalFormat(payTotal);
                headerData.gubun = Constants.PayType.TUITION.getNameKor();
                mTuitionList.add(headerData);
                payTotal = 0;
            }else{
                TuitionHeaderData headerData = new TuitionHeaderData();

                try {
                    for (TuitionData pay : payList) if (data.acaName.equals(pay.acaName)) payTotal += Integer.parseInt(pay.payment.replace(",", ""));
                }catch (NumberFormatException e){

                }

                headerData.acaName = data.acaName;
                headerData.accountNO = data.accountNO;
                headerData.acaLink = data.acaLink;
                headerData.acaLinkIsNull = data.acaLinkIsNull;
                headerData.payment = Utils.decimalFormat(payTotal);
                headerData.gubun = Constants.PayType.BOOK_PAY.getNameKor();
                mTuitionList.add(headerData);
                payTotal = 0;
            }
        }
    }

    // 수강료, 교재비 조회
    private void requestTuitionList(String yearMonth){
        showProgressDialog();
        if (RetrofitClient.getInstance() != null){
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getTuitionList(yearMonth, _stCode).enqueue(new Callback<TuitionResponse>() {
                @Override
                public void onResponse(Call<TuitionResponse> call, Response<TuitionResponse> response) {
                    if (mTuitionList != null && mTuitionList.size() > 0) mTuitionList.clear();
                    if (response.isSuccessful()) {

                        if (response.body() != null && response.body().data != null) {

                            ArrayList<TuitionData> getData = response.body().data;
                            initHeaderData(getData);
                            getData.forEach(t -> t.isHeader());

                            LogMgr.e(TAG, "data2:" + getData.size());

                            for (TuitionData data : getData) {

                                try {
                                    int payment = Integer.parseInt(data.payment);
                                    data.payment = Utils.decimalFormat(payment);

                                    mTuitionList.add(data);

                                    LogMgr.e(TAG, "data: " + data.acaName);

                                } catch (NumberFormatException e) {
                                    LogMgr.e(TAG, "Payment is not a valid integer: " + data.payment);
                                }
                            }

                            Collections.sort(mTuitionList);

                        } else {
                            LogMgr.e(TAG, "Response or ListData is null");
                        }
                    } else {
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                    //updateList();
                    if (mTuitionAdapter != null) mTuitionAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mTuitionList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<TuitionResponse> call, Throwable t) {
                    //updateList();
                    if (mTuitionAdapter != null) mTuitionAdapter.notifyDataSetChanged();
                    mTvTuitionEmpty.setVisibility(mTuitionList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestTuitionList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}