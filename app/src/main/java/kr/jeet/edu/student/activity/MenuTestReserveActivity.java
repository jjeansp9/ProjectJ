package kr.jeet.edu.student.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.TestReserveAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.data.TestTimeData;
import kr.jeet.edu.student.model.response.TestReserveListResponse;
import kr.jeet.edu.student.model.response.TestTimeResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
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
    private Button btnReserve;

    private int position = -1;

    private final ArrayList<TestReserveData> mList = new ArrayList<>();
    private int _memberSeq = 0;
    private final int LTC_CODE = 0; // 원생, 학부모인 경우는 0으로 고정

    boolean isNew = false;

    // 등록했을 때 result =ActivityResult{resultCode=RESULT_CANCELED, data=null}
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            boolean added = false;
            if (intent != null) {
                if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_ADDED)) {
                    LogMgr.e(TAG, "resultLauncher Event ADD");
                    added = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_ADDED, false);
                    if (added) {
                        requestTestReserveList();
                        Utils.createNotification(mContext, "예약완료", getString(R.string.informed_question_success));
                    }

                }else if (intent.hasExtra(IntentParams.PARAM_TEST_RESERVE_EDITED)){
                    LogMgr.e(TAG, "resultLauncher Event EDIT");
                    boolean edited = intent.getBooleanExtra(IntentParams.PARAM_TEST_RESERVE_EDITED, false);
                    if (edited) {
                        requestTestReserveList();
                        Utils.createNotification(mContext, "수정완료", getString(R.string.informed_question_update_success));
                    }
                } else if (intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD) && Constants.FINISH_COMPLETE.equals(intent.getAction())) { // 신규원생을 추가했을 경우
                    LogMgr.e(TAG, "event new stu2");
                    added = intent.getBooleanExtra(IntentParams.PARAM_TEST_NEW_CHILD, false);
                    //isNew = true;
                    if (added) {
                        LogMgr.e(TAG, "event new stu3");
                        //requestTestReserveList();
                        Utils.createNotification(mContext, "예약완료", getString(R.string.informed_question_success));
                        intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD, true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
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
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {

        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_test_reserve);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);
        btnReserve = findViewById(R.id.btn_reserve);
        btnReserve.setOnClickListener(this);
        setRecycler();

        mSwipeRefresh.setOnRefreshListener( this::requestTestReserveList );

        requestTestReserveList();
        requestTestTime();
    }

    private void setRecycler(){
        mAdapter = new TestReserveAdapter(mContext, mList, this::startActivity);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    // 희망일 순 정렬된 리스트 데이터의 마지막 seq로 페이징 요청
                    // 컨셉 변경 -> 리스트 내에서 최소 seq 값을 lastNoticeSeq 로 변경
                    // 컨셉 변경 -> 서버에서 등록일 순으로 데이터를 정렬해서 주는 것으로 변경-> 앱에서는 rollback
                    int lastNoticeSeq = mList.get(mList.size() - 1).seq;
                    LogMgr.w(TAG, "scroll listener lastNoticeSeq= " + lastNoticeSeq);
                    requestTestReserveList(lastNoticeSeq);
//                    Optional optional = mList.stream().min(new Comparator<TestReserveData>() {
//                        @Override
//                        public int compare(TestReserveData t1, TestReserveData t2) {
//                            if (t1.seq > t2.seq) return 1;
//                            else if(t1.seq < t2.seq) return -1;
//                            else return 0;
//                        }
//                    });
//                    if(optional.isPresent()){
//                        int lastNoticeSeq = ((TestReserveData)optional.get()).seq;
//                        LogMgr.w(TAG, "scroll listener lastNoticeSeq= " + lastNoticeSeq);
//                        requestTestReserveList(lastNoticeSeq);
//                    }else{
//                        LogMgr.w(TAG, "optional is not present");
//                    }

                }
            }
        });
    }

    private void startActivity(TestReserveData item, int position){
        if (item != null){
            this.position = position;

            Intent intent = new Intent(mContext, MenuTestReserveDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_LIST_ITEM, item);
            resultLauncher.launch(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("item is null ");
    }

    private void requestTestReserveList(int... lastSeq) {
        showProgressDialog();

        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];

        if (RetrofitClient.getInstance() != null) {
            int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getTestReserveListResponse(_memberSeq, LTC_CODE, lastNoticeSeq).enqueue(new Callback<TestReserveListResponse>() {
                @Override
                public void onResponse(Call<TestReserveListResponse> call, Response<TestReserveListResponse> response) {
                    if(finalLastNoticeSeq == 0) if (mList.size() > 0) mList.clear();
                    mList.add(0, new TestReserveData());
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<TestReserveData> list = response.body().data;
                                if (list != null && !list.isEmpty()) mList.addAll(list);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<TestReserveListResponse> call, Throwable t) {
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(mList.size() <= 1 ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestTestTime() {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getTestTime().enqueue(new Callback<TestTimeResponse>() {
                @Override
                public void onResponse(Call<TestTimeResponse> call, Response<TestTimeResponse> response) {

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                ArrayList<TestTimeData> getData = response.body().data;

                                if (getData == null || getData.isEmpty()){
                                    btnReserve.setOnClickListener(null);
                                    btnReserve.setBackgroundResource(R.drawable.bt_click_cancel);
                                    Toast.makeText(mContext, R.string.menu_test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            btnReserve.setOnClickListener(null);
                            btnReserve.setBackgroundResource(R.drawable.bt_click_cancel);
                            Toast.makeText(mContext, R.string.menu_test_reserve_test_time_empty, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestTime() Exception : ", e.getMessage());
                        btnReserve.setOnClickListener(null);
                        btnReserve.setBackgroundResource(R.drawable.bt_click_cancel);

                    }

                }

                @Override
                public void onFailure(Call<TestTimeResponse> call, Throwable t) {
                    btnReserve.setOnClickListener(null);
                    btnReserve.setBackgroundResource(R.drawable.bt_click_cancel);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_reserve, menu);
//        int positionOfMenuItem = 0;
//        try {
//            MenuItem item = menu.getItem(positionOfMenuItem);
//            SpannableString span = new SpannableString(item.getTitle());
//            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
//            span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), 0);
//
//            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mContext.getResources().getDisplayMetrics());
//            span.setSpan(new AbsoluteSizeSpan(sizeInPx), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//            item.setTitle(span);
//        }catch(Exception ex){}
//        return (super.onCreateOptionsMenu(menu));
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.action_test_reserve:
//                Intent intent = new Intent(mContext, InformedConsentActivity.class);
//                resultLauncher.launch(intent);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_reserve:
                Intent intent = new Intent(mContext, InformedConsentActivity.class);
                resultLauncher.launch(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (isNew) intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD, true);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.none, R.anim.vertical_exit);
    }
}













