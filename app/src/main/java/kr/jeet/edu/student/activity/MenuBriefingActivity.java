package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BriefingListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.response.BriefingResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingActivity extends BaseActivity {

    private final static String TAG = "BriefingActivity";

    private PowerSpinnerView mSpinnerCampus;
    private TextView mTvYear, mTvMonth, mTvEmptyList;
    private RecyclerView mRecyclerBrf;
    private SwipeRefreshLayout mSwipeRefresh;

    private BriefingListAdapter mAdapter;

    private ArrayList<BriefingData> mList = new ArrayList<>();

    private String _acaCode = "";
    private String _acaName = "";
    private String _userType = "";
    private boolean selAllOrNot = false;

    private Calendar calendar;
    private SimpleDateFormat yearFormat, monthFormat;

    private String year = "";
    private String month = "";

    private static final int ADD = 1;
    private static final int SUBTRACT = -1;
    private static final String NEXT = "CLICK_NEXT";
    private static final String PREVIOUS = "CLICK_PREVIOUS";

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) requestBrfList(_acaCode, selAllOrNot);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){
        _userType = PreferenceUtil.getUserType(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);

        calendar = Calendar.getInstance();
        yearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        monthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        year = yearFormat.format(calendar.getTime());
        month = monthFormat.format(calendar.getTime());

        if (_userType.equals(Constants.MEMBER)) {
            selAllOrNot = false;
            requestBrfList(_acaCode, selAllOrNot);
        }
        else {
            selAllOrNot = true;
            requestBrfList("", selAllOrNot);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    String strYear = "";
    String strMonth = "";

    @Override
    void initView() {
        findViewById(R.id.layout_brf_year_month).setOnClickListener(this);
        findViewById(R.id.img_brf_back).setOnClickListener(this);
        findViewById(R.id.img_brf_next).setOnClickListener(this);

        mTvEmptyList = findViewById(R.id.tv_brf_empty_list);
        mTvYear = findViewById(R.id.tv_brf_year);
        mTvMonth = findViewById(R.id.tv_brf_month);

        mSpinnerCampus = findViewById(R.id.spinner_brf_campus);
        mRecyclerBrf = findViewById(R.id.recycler_briefing);
        mSwipeRefresh = findViewById(R.id.refresh_layout);

        strYear = year + getString(R.string.year);
        strMonth = month + getString(R.string.month);

        mTvYear.setText(strYear);
        mTvMonth.setText(strMonth);

        setSpinner();
        setRecycler();

        mSwipeRefresh.setOnRefreshListener(() -> requestBrfList(_acaCode, selAllOrNot));
    }

    private void setSpinner(){
        List<ACAData> spinList = DataManager.getInstance().getACAList();
        List<String> acaNames = new ArrayList<>();

        acaNames.add(getString(R.string.announcement_spinner_default_text));

        for (ACAData data : spinList) acaNames.add(data.acaName);

        if (_userType.equals(Constants.MEMBER)) mSpinnerCampus.setText(_acaName);
        else mSpinnerCampus.setText(acaNames.get(0));

        mSpinnerCampus.setItems(acaNames);
        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex > 0) {
                selAllOrNot = false;
                _acaCode = spinList.get(newIndex - 1).acaCode;
            }
            else {
                selAllOrNot = true;
                _acaCode = "";
            }

            requestBrfList(_acaCode, selAllOrNot);
        });
    }

    private void setRecycler(){
        mAdapter = new BriefingListAdapter(mContext, mList, this::startDetailActivity);
        mRecyclerBrf.setAdapter(mAdapter);
        mRecyclerBrf.addItemDecoration(Utils.setDivider(mContext));
    }

    private void startDetailActivity(BriefingData item){
        if (item != null){
            Intent intent = new Intent(mContext, MenuBriefingDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_BRIEFING_INFO, item);
            resultLauncher.launch(intent);
        }else LogMgr.e("clickItem is null ");
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.layout_brf_year_month:
                Utils.yearMonthPicker(mContext, (month, year) -> selectYearMonth(year, month+1), Integer.parseInt(year), Integer.parseInt(month)-1);
                break;

            case R.id.img_brf_back:
                nextOrPrevious(SUBTRACT, PREVIOUS);
                break;

            case R.id.img_brf_next:
                nextOrPrevious(ADD, NEXT);
                break;
        }
    }

    private void selectYearMonth(int year, int month){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);

        Locale currentLocale = Locale.getDefault();

        this.year = String.valueOf(year);
        this.month = String.format(currentLocale, "%02d", month);

        strYear = this.year + getString(R.string.year);
        strMonth = this.month + getString(R.string.month);

        mTvYear.setText(strYear);
        mTvMonth.setText(strMonth);

        requestBrfList(_acaCode, selAllOrNot);
    }

    private void nextOrPrevious(int num, String btnType){
        if (btnType.equals(PREVIOUS)){
            if (Integer.parseInt(yearFormat.format(calendar.getTime())) <= Constants.PICKER_MIN_YEAR &&
                    Integer.parseInt(monthFormat.format(calendar.getTime())) == 1) {
                return;
            }
        }
        if (btnType.equals(NEXT)){
            if (Integer.parseInt(yearFormat.format(calendar.getTime())) >= Constants.PICKER_MAX_YEAR &&
                    Integer.parseInt(monthFormat.format(calendar.getTime())) == 12 ) {
                return;
            }
        }

        calendar.add(Calendar.MONTH, num);

        year = yearFormat.format(calendar.getTime());
        month = monthFormat.format(calendar.getTime());

        strYear = year + getString(R.string.year);
        strMonth = month + getString(R.string.month);

        mTvYear.setText(strYear);
        mTvMonth.setText(strMonth);

        requestBrfList(_acaCode, selAllOrNot);
    }

    private void requestBrfList(String acaCode, boolean all){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBriefingList(acaCode, year, month).enqueue(new Callback<BriefingResponse>() {
                @Override
                public void onResponse(Call<BriefingResponse> call, Response<BriefingResponse> response) {
                    mList.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BriefingData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    mList.addAll(list);

                                    for (BriefingData data : mList) data.campusAll = all;
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfList() Exception : ", e.getMessage());
                    }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BriefingResponse> call, Throwable t) {
                    mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}