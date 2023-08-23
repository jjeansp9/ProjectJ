package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.AnnouncementListAdapter;
import kr.jeet.edu.student.adapter.MainMenuListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.MainMenuItemData;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAnnouncementActivity extends BaseActivity {

    private String TAG = MenuAnnouncementActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mTvListEmpty;
    private AnnouncementListAdapter mAdapter;
    private RetrofitApi mRetrofitApi;
    private PowerSpinnerView mPowerSpinner;
    private SwipeRefreshLayout mSwipeRefresh;

    private ArrayList<AnnouncementData> mList = new ArrayList<>();

    private String _acaCode = "";
    private String _acaName = "";
    String _userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_announcement);
        mContext = this;
        getData();
        initAppbar();
        initView();
    }

    private void getData(){
        _userType = PreferenceUtil.getUserType(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);

        if (_userType.equals(Constants.MEMBER))requestBoardList(_acaCode);
        else requestBoardList("");
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_announcement);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView(){
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = findViewById(R.id.recycler_announcement);
        mPowerSpinner = findViewById(R.id.power_spinner);
        mTvListEmpty = findViewById(R.id.tv_announcement_list_empty);

        setListRecycler();
        setListSpinner();

        mSwipeRefresh.setOnRefreshListener( () -> requestBoardList(_acaCode) );
    }

    private void setListRecycler(){
        mAdapter = new AnnouncementListAdapter(mContext, mList, this::startBoardDetailActivity);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!mRecyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).seq;
                    requestBoardList(_acaCode, lastNoticeSeq);
                }
            }
        });
    }

    private void startBoardDetailActivity(AnnouncementData clickItem){
        if (clickItem != null){
            Intent targetIntent = new Intent(mContext, MenuBoardDetailActivity.class);
            targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, clickItem);
            targetIntent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
            startActivity(targetIntent);

        }else LogMgr.e("clickItem is null ");
    }

    private void setListSpinner(){
        mPowerSpinner.setIsFocusable(true);

        List<ACAData> spinList = DataManager.getInstance().getACAList();
        List<String> acaNames = new ArrayList<>();

        acaNames.add(getString(R.string.announcement_spinner_default_text));

        for (ACAData data : spinList) acaNames.add(data.acaName);

        if (_userType.equals(Constants.MEMBER)) mPowerSpinner.setText(_acaName);
        else mPowerSpinner.setText(acaNames.get(0));

        mPowerSpinner.setItems(acaNames);
        mPowerSpinner.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if (newIndex > 0) _acaCode = spinList.get(newIndex - 1).acaCode;
            else _acaCode = "";

            requestBoardList(_acaCode);
        });
    }

    private void requestBoardList(String acaCodes, int... lastSeq) {
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) {
            lastNoticeSeq = lastSeq[0];
        }
        if(lastNoticeSeq == 0) {
            if (mList.size() > 0) mList.clear();
        }

        if (RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getAnnouncementList(lastNoticeSeq, acaCodes).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<AnnouncementData> getData = null;

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
                        LogMgr.e(TAG + "requestBoardList() Exception: ", e.getMessage());
                    }
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mSwipeRefresh.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<AnnouncementListResponse> call, Throwable t) {
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }
}