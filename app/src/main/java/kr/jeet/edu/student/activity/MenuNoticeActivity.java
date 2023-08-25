package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.NoticeListAdapter;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.NoticeListData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class MenuNoticeActivity extends BaseActivity {

    private static final String TAG = "NoticeActivity";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private NoticeListAdapter mAdapter;
    private TextView txtEmpty;

    private final ArrayList<PushMessage> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_notice);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){
        if (mList.size() > 0) mList.clear();
        if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(false);

        new Thread(() -> {
            List<PushMessage> item = JeetDatabase.getInstance(mContext).pushMessageDao().getAllMessage();
            for (PushMessage msg : item){
                LogMgr.w("DBTest",
                        msg.pushType + "\n" +
                        msg.acaCode + "\n" +
                        msg.date + "\n" +
                        msg.body + "\n" +
                        msg.id + "\n" +
                        msg.pushId + "\n" +
                        msg.title + "\n" +
                        msg.connSeq + "\n" +
                        msg.isRead + "\n"
                );
            }
            mList.addAll(item);

            runOnUiThread(() -> {
                if (txtEmpty != null) txtEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_notice);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_notice);
        txtEmpty = (TextView) findViewById(R.id.tv_empty_list);

        setRecycler();
        mSwipeRefresh.setOnRefreshListener(this::initData);
    }

    private void setRecycler(){
//        for (int i = 0; i< 15; i++) {
//            mList.add(new PushMessage("시스템알림", "결석", "2023-08-30 18:30", "홍길동 외 12명", R.drawable.img_dot_woman));
//        }
        mAdapter = new NoticeListAdapter(mContext, mList, this::startActivity);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }

    private void startActivity(PushMessage item){
        if (item != null){
            Intent intent = new Intent(mContext, MenuBoardDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_NOTICE_INFO, item);
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_notice));
            startActivity(intent);
        }else LogMgr.e("item is null ");
    }
}