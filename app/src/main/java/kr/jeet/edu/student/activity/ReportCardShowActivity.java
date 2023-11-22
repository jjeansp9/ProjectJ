package kr.jeet.edu.student.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.ReportCardShowType3Adapter;
import kr.jeet.edu.student.common.CustomGridLayoutManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ReportCardData;
import kr.jeet.edu.student.model.data.ReportCardShowData;
import kr.jeet.edu.student.model.data.ReportCardSummaryData;
import kr.jeet.edu.student.model.data.ReportNameData;
import kr.jeet.edu.student.model.data.ReportScoreData;
import kr.jeet.edu.student.model.response.ReportCardShowResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportCardShowActivity extends BaseActivity {

    private static final String TAG = "ReportCardShowActivity";
    public interface ExamListTypeItem extends Comparable<ExamListTypeItem> {
        int getType();
    }

    private TextView tvProcess, tvDate, tvSubject, tvName, tvSchool;

    private RecyclerView mRecycler;
    private ReportCardShowType3Adapter mAdapter;

    private ArrayList<ReportCardShowActivity.ExamListTypeItem> mList = new ArrayList<>();
    private ArrayList<ReportNameData> getTestList = new ArrayList<>();

    ReportCardData _currentData = null;
    ReportCardShowData _showData = null;
    private int reportSeq = 0;
    private int reportListSeq = 0;

    private int _memberSeq = 0;
    private int _stuSeq = 0;
    private int _stCode = 0;
    private String _stuName = "";
    private int _userGubun = 0;
    private String _acaCode = "";

    private int spanCount = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_card_show);
        mContext = this;
        initView();
        initAppbar();
    }

    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_LIST_ITEM)) {
                LogMgr.w("param is recived");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM, ReportCardData.class);
                }else{
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_LIST_ITEM);
                }
            }

            if (intent.hasExtra(IntentParams.PARAM_REPORT_SEQ)) reportSeq = intent.getIntExtra(IntentParams.PARAM_REPORT_SEQ, reportSeq);
            if (intent.hasExtra(IntentParams.PARAM_BOARD_SEQ)) reportListSeq = intent.getIntExtra(IntentParams.PARAM_BOARD_SEQ, reportListSeq);

        }
        if(_currentData == null) finish();
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
        initIntentData();
        initData();
        //testData(); // TODO : 개발 진행중

        tvProcess = findViewById(R.id.tv_process);
        tvDate = findViewById(R.id.tv_date);
        tvSubject = findViewById(R.id.tv_subject);
        tvName = findViewById(R.id.tv_name);
        tvSchool = findViewById(R.id.tv_school);

        mRecycler = (RecyclerView) findViewById(R.id.recycler_exam);

        if(_currentData.etTitleGubun == 0 || _currentData.etTitleGubun == 1) {
            //mAdapter = new ReportCardShowType0Adapter(mContext, mList);
        }else if(_currentData.etTitleGubun == 3) {
            // 악어수학용 Adapter
            CustomGridLayoutManager layoutMgr = new CustomGridLayoutManager(mContext, spanCount);
            mRecycler.setLayoutManager(layoutMgr);
            mAdapter = new ReportCardShowType3Adapter(mContext, mList);
        }

        mRecycler.setAdapter(mAdapter);

        requestReportShow();
    }

    private void testData() {
//        ReportNameData testData = new ReportNameData();
//        testData.esScore = "10";
//
//        for (int i = 0; i < 14; i++) mList.add(testData);
//
//        ReportNameData sub = new ReportNameData();
//        sub.esSub = "event";
//        //for (int i = 0; i < mList.size(); i++) if (i % 6 == 0) mList.add(i, sub);
//
//        int currentSize = mList.size();
//        int targetSize = (currentSize + 5) / 6 * 6 + 2;
//
//        ReportNameData dummy = new ReportNameData();
//        dummy.esScore = "";
//        while (mList.size() < targetSize) {
//            mList.add(dummy);
//        }
    }

    // 성적표 데이터별 상세정보 조회
    private void requestReportShow(){
        if (RetrofitClient.getInstance() != null){

            showProgressDialog();

            RetrofitClient.getApiInterface().getReportCardShowList(reportSeq, reportListSeq).enqueue(new Callback<ReportCardShowResponse>() {
                @Override
                public void onResponse(Call<ReportCardShowResponse> call, Response<ReportCardShowResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            if (response.body() != null) {
                                mList.clear();
                                ReportCardShowData getData = response.body().data;

                                tvProcess.setText(Utils.getStr(getData.etGubun));
                                tvDate.setText(Utils.getStr(getData.regDate));
                                tvSubject.setText(Utils.getStr(getData.etName));
                                tvName.setText(Utils.getStr(getData.stName));
                                tvSchool.setText(Utils.getStr(getData.scName));

                                parsingData(getData);
                            }
                        }else{
                            int code = response.code();
                            if (code == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                Toast.makeText(mContext, R.string.reportcard_not_found, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestReportShow() Exception : ", e.getMessage());
                    }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<ReportCardShowResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestReportShow() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    void parsingData(ReportCardShowData data) {
    }
}