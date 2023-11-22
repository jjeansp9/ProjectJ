package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ReportCardListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ReportCardSummaryData;
import kr.jeet.edu.student.model.response.SystemReportListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuReportCardActivity extends BaseActivity {

    private static final String TAG = "MenuReportCardActivity";

    private TextView txtEmpty;

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private int _stCode = 0;
    private String _stuName = "";
    private int _userGubun = 0;
    private String _acaCode = "";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private ReportCardListAdapter mAdapter;

    private final ArrayList<ReportCardSummaryData> mList = new ArrayList<>();

    private static final int CMD_GET_REPORT_CARD = 1;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_REPORT_CARD:
                    getReportListData();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_report_card);
        mContext = this;
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_report_card);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _stuName = PreferenceUtil.getStName(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
    }

    @Override
    void initView() {
        initData();

        txtEmpty = findViewById(R.id.tv_empty_list);
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_menu_report_card);

        mSwipeRefresh.setOnRefreshListener(this::getReportListData);

        mAdapter = new ReportCardListAdapter(mContext, mList, this::startActivity);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        Drawable dividerColor = new ColorDrawable(ContextCompat.getColor(this, R.color.line_2));

        dividerItemDecoration.setDrawable(dividerColor);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).seq;
                    getReportListData(lastNoticeSeq);
                }
            }
        });

        _handler.sendEmptyMessage(CMD_GET_REPORT_CARD);
    }

    private void startActivity(ReportCardSummaryData item){
        if (item != null){
            Intent intent = new Intent(mContext, ReportCardDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_LIST_ITEM, item);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("item is null ");
    }

    private void getReportListData(int... lastSeq) {

        if (mList.size() > 0) mList.clear();

        int lastNoticeSeq = 0;
        if (lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];

        if (RetrofitClient.getInstance() != null) {
            final int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getReportList(
                    lastNoticeSeq,
                    _memberSeq,
                    _userGubun,
                    _stCode,
                    "",
                    _acaCode
            ).enqueue(new Callback<SystemReportListResponse>() {
                @Override
                public void onResponse(Call<SystemReportListResponse> call, Response<SystemReportListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if(finalLastNoticeSeq == 0) {
                                if (mList.size() > 0) mList.clear();
                            }
                            if (response.body() != null) {
                                List<ReportCardSummaryData> list = response.body().data;
                                if (list != null) mList.addAll(list);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "getListData() Exception : ", e.getMessage());
                    }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();

                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<SystemReportListResponse> call, Throwable t) {
                    if (mList.size() > 0) mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.vertical_exit);
    }
}