package kr.jeet.edu.student.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.AbstractReportCardShowAdapter;
import kr.jeet.edu.student.adapter.ReportCardShowType0Adapter;
import kr.jeet.edu.student.adapter.ReportCardShowType3Adapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.CustomGridLayoutMgr;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ReportCardData;
import kr.jeet.edu.student.model.data.ReportCardExamData;
import kr.jeet.edu.student.model.data.ReportCardExamFooterData;
import kr.jeet.edu.student.model.data.ReportCardExamHeaderData;
import kr.jeet.edu.student.model.data.ReportCardShowData;
import kr.jeet.edu.student.model.data.ReportCardShowListItemData;
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
        int getEsGubun();
        int getType();
    }

    private TextView tvProcess, tvDate, tvSubject, tvName, tvSchool, tvEmptyList;

    private RecyclerView mRecycler;
    private AbstractReportCardShowAdapter mAdapter;

    private ArrayList<ReportCardShowActivity.ExamListTypeItem> mList = new ArrayList<>();

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

    private int spanCount = Constants.REPORT_MATH_SPAN_COUNT;

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
        customAppbar.setTitle(getString(R.string.title_report_card));
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
        tvEmptyList = findViewById(R.id.tv_empty_list);

        mRecycler = (RecyclerView) findViewById(R.id.recycler_exam);

        if(_currentData.etTitleGubun == 0 || _currentData.etTitleGubun == 1) {
            LinearLayoutManager layoutMgr = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            mRecycler.setLayoutManager(layoutMgr);
            mAdapter = new ReportCardShowType0Adapter(mContext, mList);

        }else if(_currentData.etTitleGubun == 3) {
            // 악어수학용 Adapter
            CustomGridLayoutMgr layoutMgr = new CustomGridLayoutMgr(mContext, spanCount);
            mRecycler.setLayoutManager(layoutMgr);
            int padding = Utils.fromPxToDp(1);
            mRecycler.setPadding(padding, padding, padding, padding);
            mAdapter = new ReportCardShowType3Adapter(mContext, mList);
        }
        mRecycler.setAdapter(mAdapter);

        requestReportShow();
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

                                initUI(getData);
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

                    }catch (Exception e){
                        LogMgr.e(TAG + "requestReportShow() Exception : ", e.getMessage());
                    }
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<ReportCardShowResponse> call, Throwable t) {
                    mList.clear();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
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
    void initUI(ReportCardShowData data) {
        tvProcess.setText(data.etGubun);
        tvDate.setText(data.regDate);
        tvSubject.setText(data.etName);
        tvName.setText(data.stName);
        String schoolName = TextUtils.isEmpty(data.scName)? "-" : data.scName;
        String gradeName = TextUtils.isEmpty(data.stGrade)? "-" : data.stGrade;
        tvSchool.setText(String.format("%s / %s", schoolName, gradeName));
    }

    void parsingData(ReportCardShowData data) {
        List<ReportCardShowListItemData> listItem = data.list;
        for(int i = 0; i < listItem.size(); i++) {
            ReportCardShowListItemData listItemData = listItem.get(i);
            List<ReportCardExamData> examListData = listItemData.dataList;
            for(int j = 0; j < examListData.size(); j++) {
                ReportCardExamData examItem = examListData.get(j);
                if(_currentData.etTitleGubun == Constants.ReportCardType.KJ_E_MATH.getCode()) { // 악어수학
                    if(j == examListData.size() - 1) { //dataList 마지막에 Footer 추가
                        mList.add(new ReportCardExamFooterData(listItemData.esGubun, listItemData.totalScore, listItemData.totalCount, listItemData.correctCount, listItemData.correctRate, examItem.esTitle, examItem.esNum));
                    }
                } else { // 악어초등, 중등
                    if(j ==0) { //dataList 첫번째에서 Header, Footer 추가
                        mList.add(new ReportCardExamHeaderData(examItem.esGubun, examItem.esTitle));
                        mList.add(new ReportCardExamFooterData(listItemData.esGubun, listItemData.totalScore, listItemData.totalCount, listItemData.correctCount, listItemData.correctRate, examItem.esTitle));
                    }
                }
                mList.add(new ReportCardExamData(examItem.esGubun, examItem.esNum, examItem.esName, examItem.esScore));
            }
        }

        if(_currentData.etTitleGubun == Constants.ReportCardType.KJ_E_MATH.getCode()) { // 악어수학인 경우 spanCount에 맞게 ui 채우기
            int currentSize = mList.size() - 1;
            int targetSize = 0;

            if (currentSize % spanCount != 0) {
                targetSize = (currentSize + spanCount) / spanCount * spanCount + 1;
                ReportCardExamData dummy = new ReportCardExamData();
                dummy.esNum = 0;
                while (mList.size() < targetSize) mList.add(dummy);
            }
        }
        Collections.sort(mList);
    }
}
















