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
    private ArrayList<ReportDetailData> getTestList = new ArrayList<>();

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private int _stCode = 0;
    private String _stuName = "";
    private int _userGubun = 0;
    private String _acaCode = "";

    private int etTitleGubun = -1;

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

        etTitleGubun = 3; // 임시
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

    @Override
    void initView() {
        initData();
        testData(); // TODO : 개발 진행중
        mRecycler = (RecyclerView) findViewById(R.id.report_detail_recycler);
        mAdapter = new ReportDetailAdapter(mContext, mList, etTitleGubun);
        mRecycler.setAdapter(mAdapter);
    }

    private void testData() {
        ReportDetailData testData = new ReportDetailData();
        testData.esScore = "10";

        for (int i = 0; i < 70; i++) mList.add(testData);

        ReportDetailData sub = new ReportDetailData();
        sub.esSub = "event";
        for (int i = 0; i < mList.size(); i++) if (i % 6 == 0) mList.add(i, sub);

        int currentSize = mList.size();
        int targetSize = (currentSize + 5) / 6 * 6;

        ReportDetailData dummy = new ReportDetailData();
        dummy.esScore = "";
        while (mList.size() < targetSize) {
            mList.add(dummy);
        }
    }

}