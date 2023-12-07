package kr.jeet.edu.student.activity.menu.qna;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.adapter.QnaListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.QnaData;
import kr.jeet.edu.student.model.data.QnaDetailData;
import kr.jeet.edu.student.model.data.StudentGradeData;
import kr.jeet.edu.student.model.response.QnaListResponse;
import kr.jeet.edu.student.model.response.StudentGradeListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuQNAActivity extends BaseActivity {

    private static final String TAG = "MenuQNAActivity";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;

    private RecyclerView mRecyclerView;
    private QnaListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView mTvListEmpty;
    private PowerSpinnerView mSpinnerCampus, mSpinnerGrade;

    private List<ACAData> _ACAList = new ArrayList<>();
    private List<String> _ACANameList = new ArrayList<>();
    private List<StudentGradeData> _GradeList = new ArrayList<>();
    private ArrayList<QnaData> mList = new ArrayList<>();
    private QnaDetailData _detailData = null;

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private String acaCode = "";
    private int teacherCnt = 0;
    private int _memberSeq = 0;

    private String _appAcaCode = "";
    private ACAData _selectedLocalACA = null;
    private StudentGradeData _selectedGrade = null;

    private Menu _menu;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);

        Intent intent = result.getData();

        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent == null) return;
            if(intent.hasExtra(IntentParams.PARAM_BOARD_ADDED)) { // 작성
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_BOARD_ADDED, false);

                if(added) {
                    if(_selectedLocalACA != null) LogMgr.e("acaCode = " + _selectedLocalACA.acaCode);
                    requestQnaList();
                }
            } else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) { // 삭제
                boolean deleted = intent.getBooleanExtra(IntentParams.PARAM_BOARD_DELETED, false);
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                if(deleted && position >= 0) {
                    mList.remove(position);
                    mAdapter.notifyItemRemoved(position);
                    checkEmptyRecyclerView();
                }
            }else if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) { // 수정
                boolean edited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                if (edited) editThisPosition(intent); // 수정된 상세 데이터를 목록 데이터에 update 하기
            }

        } else { // 상세 -> 뒤로가기 클릭으로 돌아왔을 때
            if(intent != null && intent.hasExtra(IntentParams.PARAM_RD_CNT_ADD)) {
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_RD_CNT_ADD, false);
                if (added) editThisPosition(intent);
            }
        }
    });

    // 수정된 상세data -> 목록data Update
    private void editThisPosition(Intent intent) {

        if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
            QnaData changedItem = new QnaData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, QnaData.class);
            }else{
                changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
            }

            if(intent.hasExtra(IntentParams.PARAM_BOARD_POSITION)){
                int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);

                LogMgr.w("position =" + position);
                if(position >= 0 && changedItem != null) {
                    LogMgr.w("edited =" + changedItem.title);
                    mList.set(position, changedItem);
                    mAdapter.notifyItemChanged(position);
                    checkEmptyRecyclerView();
                }else {
                    LogMgr.w("edited is null");
                }
            }
        }
    }

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null && !_GradeList.isEmpty()) {
//                        _spinnerGrade.setEnabled(true);
                        Utils.updateSpinnerList(mSpinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            mSpinnerGrade.selectItemByIndex(index);
                        }else{
                            mSpinnerGrade.selectItemByIndex(0);
                        }
                    }else{
                        requestQnaList();
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_qna);
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }
    private void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_qna);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void initData() {
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _userType = PreferenceUtil.getUserIsOriginal(mContext);
    }
    private void initView() {
        initData();

        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = findViewById(R.id.recycler_qna);
        mSpinnerCampus = findViewById(R.id.spinner_campus);
        mSpinnerGrade = findViewById(R.id.spinner_grade);
        mTvListEmpty = findViewById(R.id.tv_qna_empty_list);

        setListRecycler();
        setListSpinner();
        setSelectAcaCode();

        mSwipeRefresh.setOnRefreshListener(this::requestQnaList);
    }

    private void setListRecycler() {
        mAdapter = new QnaListAdapter(mContext, mList, this::startQnaDetailActivity);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).seq;
                    requestQnaList(lastNoticeSeq);
                }
            }
        });
    }

    private void startQnaDetailActivity(QnaData clickItem, int position){
        if (clickItem != null){
            Intent intent = new Intent(mContext, MenuQNADetailActivity.class);
            intent.putExtra(IntentParams.PARAM_LIST_ITEM, clickItem);
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_qna));
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
            resultLauncher.launch(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);

        }else LogMgr.e("clickItem is null ");
    }

    private void setListSpinner(){
        _ACAList.clear();
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if(_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());
        mSpinnerCampus.setItems(_ACANameList);
        mSpinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional = _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                selectedData = (ACAData) optional.get();
            }
            _selectedLocalACA = selectedData;
            if(_selectedLocalACA != null) {
                LogMgr.w("selectedACA = " + _selectedLocalACA.acaCode + " / " + _selectedLocalACA.acaName);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (mSpinnerGrade != null) mSpinnerGrade.clearSelectedItem();
                if(!TextUtils.isEmpty(_selectedLocalACA.acaCode)) {
                    requestGradeList(_selectedLocalACA.acaCode);
                }else{
                    if(_GradeList != null) _GradeList.clear();
                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
                if(TextUtils.isEmpty(_selectedLocalACA.acaCode)){   //전체
                    if (mSpinnerGrade != null) mSpinnerGrade.setEnabled(false);
                }else{
                    if (mSpinnerGrade != null) mSpinnerGrade.setEnabled(true);
                }

            }

        });

        mSpinnerCampus.setSpinnerOutsideTouchListener((view, motionEvent) -> mSpinnerCampus.dismiss());
        mSpinnerCampus.setLifecycleOwner(this);

        mSpinnerGrade = findViewById(R.id.spinner_grade);
        mSpinnerGrade.setIsFocusable(true);
        mSpinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            if(oldItem != null && oldItem.equals(newItem)) return;
            StudentGradeData selectedData = null;
            Optional optional = _GradeList.stream().filter(t -> t.gubunName == newItem).findFirst();
            if(optional.isPresent()) {
                selectedData = (StudentGradeData) optional.get();
            }
            _selectedGrade = selectedData;
            if(_selectedGrade != null) {
                LogMgr.w("selectedGubun = " + _selectedGrade.gubunCode + " / " + _selectedGrade.gubunName);
            }
            requestQnaList();
        });

        mSpinnerGrade.setSpinnerOutsideTouchListener((view, motionEvent) -> mSpinnerGrade.dismiss());
        mSpinnerGrade.setLifecycleOwner(this);
    }

    private void setSelectAcaCode() {
        if(!TextUtils.isEmpty(_appAcaCode) && Constants.MEMBER.equals(_userType)){
            ACAData selectedACA = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(_appAcaCode)).findFirst();
                if(option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }
            } else {
                for (ACAData data : _ACAList) {
                    if (data.acaCode == _appAcaCode) {
                        selectedACA = data;
                        break;
                    }
                }
            }
            try {
                if (selectedACA != null) {
//                    LogMgr.e(TAG, "selectedACA != null");
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    if(selectedIndex >= 0) {
                        mSpinnerCampus.selectItemByIndex(selectedIndex);
                    }
                } else {
//                    LogMgr.e(TAG, "selectedACA == null");
                    mSpinnerCampus.selectItemByIndex(0);
                }
            }catch(Exception ex){

            }

        }else{
//            LogMgr.e(TAG, "else~");
            mSpinnerCampus.selectItemByIndex(0);
        }
    }

    private void requestQnaList(int... lastSeq) {
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];
        String acaCode = "";
        String gradeCode = "";
        if(_selectedLocalACA != null) acaCode = _selectedLocalACA.acaCode;
        if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);

        if (RetrofitClient.getInstance() != null) {
            int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getQnaList(lastNoticeSeq, _memberSeq, _userGubun, acaCode, gradeCode, _userType).enqueue(new Callback<QnaListResponse>() {
                @Override
                public void onResponse(Call<QnaListResponse> call, Response<QnaListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<QnaData> getData = new ArrayList<>();
                            if(finalLastNoticeSeq == 0) if (mList.size() > 0) mList.clear();

                            if (response.body() != null) {
                                getData = response.body().data.mainList;
                                if (getData != null && !getData.isEmpty()) mList.addAll(getData);
                                else LogMgr.e(TAG, "MainListData is null");

                                getData = response.body().data.list;
                                if (getData != null && !getData.isEmpty()) mList.addAll(getData);
                                else LogMgr.e(TAG, "ListData is null");
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestQnaList() Exception: ", e.getMessage());
                    }
                    LogMgr.e(TAG, "mListSize: " + mList.size());
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    //if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mSwipeRefresh.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    if(finalLastNoticeSeq == 0 && mList.size() > 0 && mRecyclerView != null) {
                        _handler.postDelayed(() -> mRecyclerView.smoothScrollToPosition(0), scrollToTopDelay);
                    }
                }

                @Override
                public void onFailure(Call<QnaListResponse> call, Throwable t) {
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    try {
                        LogMgr.e(TAG, "requestQnaList() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    private void requestGradeList(String acaCode){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
                @Override
                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {
                            List<StudentGradeData> list = response.body().data;
                            if(_GradeList != null) _GradeList.clear();
                            _GradeList.add(new StudentGradeData("", "구분 전체"));
                            _GradeList.addAll(list);
                            Collections.sort(_GradeList, new Comparator<StudentGradeData>() {
                                @Override
                                public int compare(StudentGradeData schoolData, StudentGradeData t1) {
                                    return schoolData.gubunCode.compareTo(t1.gubunCode);
                                }
                            });
                            _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestGrade errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }
                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestGrade() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (_userType.equals(Constants.MEMBER)) {
            getMenuInflater().inflate(R.menu.menu_board, menu);
            this._menu = menu;
        }

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_userType.equals(Constants.MEMBER)) {
            switch(item.getItemId()) {
                case R.id.action_add:
                    resultLauncher.launch(new Intent(mContext, EditQNAActivity.class));
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkEmptyRecyclerView() {
        if (mAdapter.getItemCount() > 0) {
            mSwipeRefresh.setVisibility(View.VISIBLE);
            mTvListEmpty.setVisibility(View.INVISIBLE);
            return false;
        } else {
            mSwipeRefresh.setVisibility(View.INVISIBLE);
            mTvListEmpty.setVisibility(View.VISIBLE);
            return true;
        }
    }
}