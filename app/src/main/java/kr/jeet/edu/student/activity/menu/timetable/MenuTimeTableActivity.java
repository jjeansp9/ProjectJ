package kr.jeet.edu.student.activity.menu.timetable;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.adapter.TeacherListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuTimeTableActivity extends BaseActivity {

    private static final String TAG = "MenuRollBookActivity";

    private RecyclerView mRecyclerView;
    private TextView mTvListEmpty;
    private TeacherListAdapter mAdapter;

    private ArrayList<TeacherClsData> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_time_table);
        mContext = this;
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    private void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_rollbook);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {

    }

    private void initView() {
        initData();

        mRecyclerView = findViewById(R.id.recycler_time_table);
        mTvListEmpty = findViewById(R.id.tv_time_table_empty);

        mAdapter = new TeacherListAdapter(mContext, mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

        requestTeacherCls();
    }

    private void requestTeacherCls(){

        int stCode = PreferenceUtil.getUserSTCode(mContext);

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().requestTeacherCls(stCode, Utils.currentDate("yyyyMM")).enqueue(new Callback<TeacherClsResponse>() {
                @Override
                public void onResponse(Call<TeacherClsResponse> call, Response<TeacherClsResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            if (mList.size() > 0) mList.clear();

                            List<TeacherClsData> list = response.body().data;

                            if (list != null && !list.isEmpty()) mList.addAll(list);

                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            LogMgr.e(TAG, "requestTeacherCls() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(Call<TeacherClsResponse> call, Throwable t) {
                    mList.clear();
                    try { LogMgr.e(TAG, "requestTeacherCls() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(IntentParams.PARAM_RD_CNT_ADD, true);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.none, R.anim.vertical_exit);
    }
}