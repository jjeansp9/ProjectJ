package kr.jeet.edu.student.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ReportDetailAdapter;
import kr.jeet.edu.student.model.data.ReportDetailData;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class ReportDetailActivity extends BaseActivity {

    private static final String TAG = "ReportDetailActivity";

    private RecyclerView mRecycler;
    private ReportDetailAdapter mAdapter;

    private ArrayList<ReportDetailData> mList = new ArrayList<>();

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private int _stCode = 0;
    private String _stuName = "";
    private int _userGubun = 0;
    private String _acaCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        initView();
        initAppbar();
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

        mRecycler = (RecyclerView) findViewById(R.id.report_detail_recycler);
        mAdapter = new ReportDetailAdapter(mContext, mList);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle("성적표상세(임시)");
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}