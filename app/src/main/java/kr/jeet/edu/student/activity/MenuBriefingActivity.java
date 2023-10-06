package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

public class MenuBriefingActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener {

    private final static String TAG = "BriefingActivity";

    private PowerSpinnerView mSpinnerCampus;
    private TextView mTvCalendar, mTvEmptyList;
    private RecyclerView mRecyclerBrf;
    private SwipeRefreshLayout mSwipeRefresh;

    private BriefingListAdapter mAdapter;

    private ArrayList<BriefingData> mList = new ArrayList<>();

    private String _acaCode = "";
    private String _acaName = "";
    private String _userType = "";

    Date _selectedDate = new Date();
    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR, Locale.KOREA);

    private int selYear = 0;
    private int selMonth = 0;
    private int position = -1;
    private boolean added = false;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        Intent intent = result.getData();
        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent != null && intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED)) {
                added = intent.getBooleanExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, false);

                if(added) requestBrfList(_acaCode);
            }
            else if(intent != null && intent.hasExtra(IntentParams.PARAM_RD_CNT_ADD)) {
                boolean rdCntAdd = intent.getBooleanExtra(IntentParams.PARAM_RD_CNT_ADD, false);
                if(rdCntAdd) requestBrfList(_acaCode);
            }
        }
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

        Calendar calendar = Calendar.getInstance();

        selYear = calendar.get(Calendar.YEAR);
        selMonth = calendar.get(Calendar.MONTH);

        if (_userType.equals(Constants.MEMBER)) requestBrfList(_acaCode);
        else requestBrfList("");
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        findViewById(R.id.btn_brf_previous).setOnClickListener(this);
        findViewById(R.id.btn_brf_next).setOnClickListener(this);

        mTvEmptyList = findViewById(R.id.tv_brf_empty_list);
        mTvCalendar = findViewById(R.id.tv_brf_calendar);

        mSpinnerCampus = findViewById(R.id.spinner_brf_campus);
        mRecyclerBrf = findViewById(R.id.recycler_briefing);
        mSwipeRefresh = findViewById(R.id.refresh_layout);

        mTvCalendar.setOnClickListener(this);
        mTvCalendar.setText(_dateFormat.format(_selectedDate));

        setSpinner();
        setRecycler();

        mSwipeRefresh.setOnRefreshListener(() -> requestBrfList(_acaCode));
    }

    private void setSpinner(){
        List<ACAData> spinList = DataManager.getInstance().getACAList();
        List<String> acaNames = new ArrayList<>();

        acaNames.add(getString(R.string.announcement_spinner_default_text));

        for (ACAData data : spinList) acaNames.add(data.acaName);

        if (_userType.equals(Constants.MEMBER)) {
            if (TextUtils.isEmpty(_acaName)) mSpinnerCampus.setText(acaNames.get(0));
            else mSpinnerCampus.setText(_acaName);
        }
        else mSpinnerCampus.setText(acaNames.get(0));

        mSpinnerCampus.setItems(acaNames);
        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex > 0) _acaCode = spinList.get(newIndex - 1).acaCode;
            else _acaCode = "";

            requestBrfList(_acaCode);
        });
        mSpinnerCampus.setSpinnerOutsideTouchListener((view, motionEvent) -> mSpinnerCampus.dismiss());
    }

    private void setRecycler(){
        mAdapter = new BriefingListAdapter(mContext, mList, this::startDetailActivity);
        mRecyclerBrf.setAdapter(mAdapter);
        mRecyclerBrf.addItemDecoration(Utils.setDivider(mContext));
    }

    private void startDetailActivity(BriefingData item, int position){
        if (item != null){

            this.position = position;

            Intent intent = new Intent(mContext, MenuBriefingDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_BRIEFING_INFO, item);
            resultLauncher.launch(intent);
        }else LogMgr.e("clickItem is null ");
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.tv_brf_calendar:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_selectedDate);
                Utils.yearMonthPicker(mContext, this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                break;

            case R.id.btn_brf_previous:
                if (selYear <= Constants.PICKER_MIN_YEAR && selMonth <= 0) break;
                navigateMonth(-1);
                break;

            case R.id.btn_brf_next:
                if (selYear >= Constants.PICKER_MAX_YEAR && selMonth >= 11) break;
                navigateMonth(1);
                break;
        }
    }

    private void navigateMonth(int addMonth){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.add(Calendar.MONTH, addMonth);
        onDateSet(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    @Override
    public void onDateSet(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        _selectedDate = calendar.getTime();

        selYear = calendar.get(Calendar.YEAR);
        selMonth = calendar.get(Calendar.MONTH);

        mTvCalendar.setText(_dateFormat.format(_selectedDate));
        requestBrfList(_acaCode);
    }

    private void requestBrfList(String acaCode){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBriefingList(acaCode, selYear, selMonth+1).enqueue(new Callback<BriefingResponse>() {
                @Override
                public void onResponse(Call<BriefingResponse> call, Response<BriefingResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BriefingData> list = response.body().data;

                                if (list != null) {
                                    if (added){
                                        mList.set(position, list.get(position));
                                        mAdapter.notifyItemChanged(position);

                                        position = -1;
                                        added = false;

                                    }else{
                                        if (mList.size() > 0) mList.clear();
                                        mList.addAll(list);
                                    }

                                    mAdapter.setWholeCampusMode(TextUtils.isEmpty(acaCode));
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfList() Exception : ", e.getMessage());
                    }

                    //if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
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