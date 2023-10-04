package kr.jeet.edu.student.activity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.BriefingReservedListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.BriefingReservedListData;
import kr.jeet.edu.student.model.response.BriefingReservedListResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuBriefingReservedListActivity extends BaseActivity {

    private final static String TAG = "BriefingReservedListActivity";

    private TextView mTvEmptyList, mTvReservedCnt;
    private RecyclerView mRecyclerBrf;
    private SwipeRefreshLayout mSwipeRefresh;

    private BriefingReservedListAdapter mAdapter;

    private RetrofitApi mRetrofitApi;

    private ArrayList<BriefingReservedListData> mList = new ArrayList<>();

    private int ptSeq = 0; // 글 seq
    private int ptCnt = 0; // 참가인원
    private int rvCnt = 0; // 현재예약 수
    private int _memberSeq = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing_reserved_list);
        mContext = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        Intent intent= getIntent();
        if (
                intent.hasExtra(IntentParams.PARAM_BOARD_SEQ) &&
                intent.hasExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT) &&
                        intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT)){

            ptSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, ptSeq);
            ptCnt = intent.getIntExtra(IntentParams.PARAM_BRIEFING_PARTICIPANTS_CNT, ptCnt);
            rvCnt = intent.getIntExtra(IntentParams.PARAM_BRIEFING_RESERVATION_CNT, rvCnt);
        }
        _memberSeq = PreferenceUtil.getUserSeq(mContext);

        requestBrfReservedListData();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_reserved_list);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mTvEmptyList = findViewById(R.id.tv_empty_list);
        mTvReservedCnt = findViewById(R.id.tv_reserved_cnt);
        mRecyclerBrf = findViewById(R.id.recycler_brf_reserve);
        //mSwipeRefresh = findViewById(R.id.refresh_layout);

        mAdapter = new BriefingReservedListAdapter(mContext, mList, v -> {});
        mRecyclerBrf.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        Drawable dividerColor = new ColorDrawable(ContextCompat.getColor(this, R.color.line_2));

        dividerItemDecoration.setDrawable(dividerColor);
        mRecyclerBrf.addItemDecoration(dividerItemDecoration);

        mRecyclerBrf.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!mRecyclerBrf.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {

                }
            }
        });
        mAdapter.notifyDataSetChanged();

        String str = getString(R.string.briefing_write_personnel_cnt, rvCnt, ptCnt);
        mTvReservedCnt.setText(str);
    }

    private void requestBrfReservedListData(){
        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBrfReservedList(ptSeq, _memberSeq).enqueue(new Callback<BriefingReservedListResponse>() {
                @Override
                public void onResponse(Call<BriefingReservedListResponse> call, Response<BriefingReservedListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<BriefingReservedListData> getData;

                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    mList.addAll(getData);

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfReservedListData() Exception: ", e.getMessage());
                    }
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    //mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<BriefingReservedListResponse> call, Throwable t) {
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestBrfReservedListData() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    //mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }
}