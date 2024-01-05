package kr.jeet.edu.student.activity.menu.announcement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.adapter.AnnouncementListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.NewBoardDao;
import kr.jeet.edu.student.db.NewBoardData;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.ReadData;
import kr.jeet.edu.student.model.data.StudentGradeData;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.StudentGradeListResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.DBUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAnnouncementActivity extends BaseActivity {

    private String TAG = MenuAnnouncementActivity.class.getSimpleName();
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;

    private RecyclerView mRecyclerView;
    private TextView mTvListEmpty;
    private PowerSpinnerView mSpinnerCampus, mSpinnerGrade;
    private SwipeRefreshLayout mSwipeRefresh;
    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    List<StudentGradeData> _GradeList = new ArrayList<>();

    private AnnouncementListAdapter mAdapter;
    private ArrayList<ReadData> mList = new ArrayList<>();
    private ArrayList<PushMessage> mPushList = new ArrayList<>();

    private int _memberSeq = -1;
    private String _acaCode = "";
    private String _acaName = "";
    private String _appAcaCode = "";
    private ACAData _selectedLocalACA = null;
    private StudentGradeData _selectedGrade = null;

    String _userType = "";

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        Intent intent = result.getData();
        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent != null && intent.hasExtra(IntentParams.PARAM_RD_CNT_ADD)) {
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_RD_CNT_ADD, false);
                if (added) {
                    AnnouncementData changedItem = null;
                    if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                        changedItem = Utils.getParcelableExtra(intent, IntentParams.PARAM_BOARD_ITEM, AnnouncementData.class);

                    }
                    LogMgr.w("showed =" + changedItem);
                    int position = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
                    LogMgr.w("position =" + position);
                    if(position >= 0 && changedItem != null) {
                        mList.set(position, changedItem);
                        mAdapter.notifyItemChanged(position);
                    }
                }
            }
        }
    });
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
                        requestBoardList();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_announcement);
        mContext = this;
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    private void getData(){
        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userType = PreferenceUtil.getUserIsOriginal(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
//        if (_userType.equals(Constants.MEMBER)) requestBoardList(_acaCode);
//        else requestBoardList("");
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.main_menu_announcement);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView(){

        getData();

        mSwipeRefresh = findViewById(R.id.refresh_layout);
        mRecyclerView = findViewById(R.id.recycler_announcement);
        mSpinnerCampus = findViewById(R.id.spinner_campus);
        mSpinnerGrade = findViewById(R.id.spinner_grade);
        mTvListEmpty = findViewById(R.id.tv_announcement_list_empty);

        setListRecycler();
        setListSpinner();

        mSwipeRefresh.setOnRefreshListener( () -> requestBoardList() );
        LogMgr.e(TAG, "appACACode = " + _appAcaCode + " userType = " + _userType);
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
//                    if(selectedIndex >= 0) {
//                        mSpinnerCampus.selectItemByIndex(selectedIndex);
//                    }
                    if(selectedIndex >= 0 && selectedIndex <= _ACAList.size()) {
                        mSpinnerCampus.selectItemByIndex(0); //전체
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

    private void setListRecycler(){
        mAdapter = new AnnouncementListAdapter(mContext, mList, false, this::startBoardDetailActivity);
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setItemAnimator(null);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(((!mRecyclerView.canScrollVertically(1)) && mRecyclerView.canScrollVertically(-1))
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && (mList != null && !mList.isEmpty()))
                {
                    int lastNoticeSeq = mList.get(mList.size() - 1).getSeq();
                    requestBoardList(lastNoticeSeq);
                }
            }
        });
    }

    private void startBoardDetailActivity(AnnouncementData clickItem, TextView title, int position){
        if (clickItem != null){
            Intent intent = new Intent(mContext, MenuAnnouncementDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, clickItem);
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
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
        mSpinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mSpinnerCampus.dismiss();
            }
        });
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
            requestBoardList();
        });
        mSpinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mSpinnerGrade.dismiss();
            }
        });
        mSpinnerGrade.setLifecycleOwner(this);
    }

    private void requestBoardList(int... lastSeq) {
        int lastNoticeSeq = 0;
        if(lastSeq != null && lastSeq.length > 0) lastNoticeSeq = lastSeq[0];
        String acaCode = "";
        String gradeCode = "";
        if(_selectedLocalACA != null) acaCode = _selectedLocalACA.acaCode;
        if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);

        if (RetrofitClient.getInstance() != null) {
            int finalLastNoticeSeq = lastNoticeSeq;
            RetrofitClient.getApiInterface().getAnnouncementList(lastNoticeSeq, acaCode, gradeCode).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<AnnouncementData> getData = new ArrayList<>();

                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null) {
                                    if(finalLastNoticeSeq == 0) if (mList.size() > 0) mList.clear();
                                    mList.addAll(getData);
                                    DBUtils.setReadDB(mContext, mList, _memberSeq, DataManager.BOARD_NOTICE, () -> runOnUiThread(() -> mAdapter.notifyDataSetChanged()));

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
                    //if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mSwipeRefresh.setRefreshing(false);
                    //mAdapter.notifyDataSetChanged();
                    if(finalLastNoticeSeq == 0 && mList.size() > 0 && mRecyclerView != null) {
                        _handler.postDelayed(() -> mRecyclerView.smoothScrollToPosition(0), scrollToTopDelay);
                    }
                }

                @Override
                public void onFailure(Call<AnnouncementListResponse> call, Throwable t) {
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
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
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(IntentParams.PARAM_RD_CNT_ADD, true);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.none, R.anim.vertical_exit);
    }
}