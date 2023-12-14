package kr.jeet.edu.student.activity.menu.reportcard;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.adapter.ReportCardDetailListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.ReportCardData;
import kr.jeet.edu.student.model.data.ReportCardSummaryData;
import kr.jeet.edu.student.model.response.ReportCardSummaryResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportCardDetailActivity extends BaseActivity {
    private static final String TAG = "detailReportCard";
    private static final int CMD_GET_REPORT_CARD = 1;
    private RecyclerView _recyclerView;
    private ReportCardDetailListAdapter _listAdapter;
    private TextView tvStudentName, tvCampus, tvContent;
    private TextView tvEmptyList;

    private ArrayList<ReportCardData> _reportCardList = new ArrayList<ReportCardData>();
    ReportCardSummaryData _reportData = null;
    PushMessage _pushData = null;
    int _userGubun = 1;
    int _seq = 0;
    int _stCode = 0;

    int reportSeq = -15;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_REPORT_CARD:
                    //todo request logic
                    requestReportCardDetailList();
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_card_detail);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _seq = PreferenceUtil.getUserSeq(this);
        _stCode = PreferenceUtil.getUserSTCode(this);
        initIntentData();
        initAppbar();
        initView();
        initData();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_LIST_ITEM)) {
                LogMgr.w("param is recived");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _reportData = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, ReportCardSummaryData.class);
                }else{
                    _reportData = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
                if (_reportData != null) reportSeq = _reportData.seq;
            }

            Bundle bundle = intent.getExtras();
            if (bundle != null) _pushData = Utils.getSerializableExtra(bundle, IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);

            if (_pushData != null) {
                reportSeq = _pushData.connSeq;
                if (_pushData.stCode == _stCode) new FCMManager(mContext).requestPushConfirmToServer(_pushData, _stCode);
            }

        }
        if(reportSeq == -15) {
            finish();
            Toast.makeText(mContext, R.string.server_error_2, Toast.LENGTH_SHORT).show();
        }
    }
    void initView() {
        tvStudentName = findViewById(R.id.tv_name);
        tvCampus = findViewById(R.id.tv_campus);
        tvContent = findViewById(R.id.tv_content);
        tvEmptyList = findViewById(R.id.tv_empty_list);
        //region set recyclerview
        _recyclerView = findViewById(R.id.recyclerview_report_card);
        _listAdapter = new ReportCardDetailListAdapter(mContext, _reportCardList, new ReportCardDetailListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, ReportCardData item) {
                if(item.etTitleGubun == Constants.ReportCardType.MIDDLE.getCode()) {
                    Toast.makeText(mContext, R.string.err_msg_middle_not_displayed, Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(mContext, ReportCardShowActivity.class);
                    intent.putExtra(IntentParams.PARAM_LIST_ITEM, item);
                    intent.putExtra(IntentParams.PARAM_REPORT_SEQ, reportSeq);
                    intent.putExtra(IntentParams.PARAM_BOARD_SEQ, item.seq);
                    startActivity(intent);
                    overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
                }
            }

        });
        _recyclerView.setAdapter(_listAdapter);
        _recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }
    void initData() {
        _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);

    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(_pushData == null);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void requestReportCardDetailList(){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getReportCardDetailList(reportSeq).enqueue(new Callback<ReportCardSummaryResponse>() {
                @Override
                public void onResponse(Call<ReportCardSummaryResponse> call, Response<ReportCardSummaryResponse> response) {

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                _reportCardList.clear();
                                ReportCardSummaryData data = response.body().data;
                                if (data != null) {
                                    _reportCardList.addAll(data.reportList);
                                }
                                tvStudentName.setText(data.stName);
                                tvCampus.setText(data.acaName);
                                tvContent.setText(data.content);
                            }
                        } else {
                            int code = response.code();
                            if (code == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                Toast.makeText(mContext, R.string.reportcard_not_found, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(_listAdapter != null) {
                        _listAdapter.notifyDataSetChanged();
                    }
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);

                }

                @Override
                public void onFailure(Call<ReportCardSummaryResponse> call, Throwable t) {
                    _reportCardList.clear();
                    if(_listAdapter != null) _listAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(_reportCardList.isEmpty() ? View.VISIBLE : View.GONE);


                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}