package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.TestReserveAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.TestReserveListResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuTestReserveActivity extends BaseActivity {

    private final static String TAG = "Test Reserve Activity";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private TestReserveAdapter mAdapter;
    private TextView txtEmpty;

    private final ArrayList<TestReserveData> mList = new ArrayList<>();
    private int _memberSeq = 0;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            if (intent!= null && intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED)) {
                LogMgr.e(TAG, "resultLauncher Event");
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, false);
                if (added) requestTestReserveList();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test_reserve);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_test_reserve_title);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_test_reserve);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);

        setRecycler();

        mSwipeRefresh.setOnRefreshListener( this::requestTestReserveList );

        requestTestReserveList();
    }

    private void setRecycler(){
        mAdapter = new TestReserveAdapter(mContext, mList, this::startActivity);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }

    private void startActivity(TestReserveData item){
        if (item != null){
            Intent intent = new Intent(mContext, MenuTestReserveDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_LIST_ITEM, item);
            startActivity(intent);
        }else LogMgr.e("item is null ");
    }

    private void requestTestReserveList() {
        showProgressDialog();

        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getTestReserveListResponse(_memberSeq).enqueue(new Callback<TestReserveListResponse>() {
                @Override
                public void onResponse(Call<TestReserveListResponse> call, Response<TestReserveListResponse> response) {
                    mList.clear();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<TestReserveData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    mList.addAll(list);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<TestReserveListResponse> call, Throwable t) {
                    mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reserve, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), 0);

            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mContext.getResources().getDisplayMetrics());
            span.setSpan(new AbsoluteSizeSpan(sizeInPx), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            item.setTitle(span);
        }catch(Exception ex){}
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_test_reserve:
                Intent intent = new Intent(mContext, InformedConsentActivity.class);
                resultLauncher.launch(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}















