package kr.jeet.edu.student.activity.menu.briefing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.adapter.BriefingListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.NewBoardDao;
import kr.jeet.edu.student.db.NewBoardData;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.ReadData;
import kr.jeet.edu.student.model.data.StudentGradeData;
import kr.jeet.edu.student.model.response.BriefingResponse;
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

public class MenuBriefingActivity extends BaseActivity implements MonthPickerDialog.OnDateSetListener {

    private final static String TAG = "BriefingActivity";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    private static final int CMD_GET_BRIEFINGS = 2;

    private PowerSpinnerView _spinnerCampus, _spinnerGrade;

    private TextView mTvCalendar, mTvEmptyList;
    private RecyclerView mRecyclerBrf;
    private SwipeRefreshLayout mSwipeRefresh;

    private BriefingListAdapter mAdapter;

    private ArrayList<ReadData> mList = new ArrayList<>();

    private String _acaCode = "";
    private String _appAcaCode = "";

    private int _memberSeq = -1;
    private String _acaName = "";
    private String _userType = "";
    List<ACAData> _ACAList = new ArrayList<>();
    ACAData _selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    private StudentGradeData _selectedGrade = null;

    Date _selectedDate = new Date();
    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_KOR, Locale.KOREA);

    private int selYear = 0;
    private int selMonth = 0;
    private int position = -1;
    private boolean added = false;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        Intent intent = result.getData();
        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent != null && intent.hasExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED)) {
                added = intent.getBooleanExtra(IntentParams.PARAM_BRIEFING_RESERVE_ADDED, false);

                if(added) requestBriefingList();

            }
            else if(intent != null && intent.hasExtra(IntentParams.PARAM_RD_CNT_ADD)) {
                boolean rdCntAdd = intent.getBooleanExtra(IntentParams.PARAM_RD_CNT_ADD, false);
                if(rdCntAdd) {
                    BriefingData changedItem = null;
                    if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, BriefingData.class);
                        } else {
                            changedItem = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                        }
                    }
                    LogMgr.w("edited =" + changedItem);
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
                        Utils.updateSpinnerList(_spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                        Optional optional = (_GradeList.stream().filter(t->TextUtils.isEmpty(t.gubunCode)).findFirst());
                        if(optional.isPresent()) {
                            int index = _GradeList.indexOf(optional.get());
                            _spinnerGrade.selectItemByIndex(index);
                        }else{
                            _spinnerGrade.selectItemByIndex(0);
                        }
                    }else {
                        _handler.sendEmptyMessage(CMD_GET_BRIEFINGS);
                    }
                    break;
                case CMD_GET_BRIEFINGS:
                    //todo request logic
                    requestBriefingList();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_briefing);
        mContext = this;
        initData();
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DOWN);
    }

    private void initData(){
        _userType = PreferenceUtil.getUserIsOriginal(mContext);
        _acaCode = PreferenceUtil.getAcaCode(mContext);
        _acaName = PreferenceUtil.getAcaName(mContext);
        _appAcaCode = PreferenceUtil.getAppAcaCode(mContext);
        _memberSeq = PreferenceUtil.getUserSeq(mContext);

        Calendar calendar = Calendar.getInstance();

        selYear = calendar.get(Calendar.YEAR);
        selMonth = calendar.get(Calendar.MONTH);

//        if (_userType.equals(Constants.MEMBER)) requestBrfList(_acaCode);
//        else requestBrfList("");
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.briefing_title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        findViewById(R.id.btn_brf_previous).setOnClickListener(this);
        findViewById(R.id.btn_brf_next).setOnClickListener(this);

        mTvEmptyList = findViewById(R.id.tv_brf_empty_list);
        mTvCalendar = findViewById(R.id.tv_brf_calendar);

        _spinnerCampus = findViewById(R.id.spinner_brf_campus);
        _spinnerGrade = findViewById(R.id.spinner_brf_grade);

        mRecyclerBrf = findViewById(R.id.recycler_briefing);
        mSwipeRefresh = findViewById(R.id.refresh_layout);

        mTvCalendar.setOnClickListener(this);
        mTvCalendar.setText(_dateFormat.format(_selectedDate));

        setSpinner();
        setRecycler();

        mSwipeRefresh.setOnRefreshListener(() -> requestBriefingList());
    }

    private void setSpinner(){
        _ACAList.add(new ACAData("", "전체", ""));
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        List<String> acaNames = new ArrayList<>();
        for (ACAData data : _ACAList) { acaNames.add(data.acaName); }

//        _spinnerCampus.setText(acaNames.get(0));
        _spinnerCampus.setItems(acaNames);
        _spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            if(oldItem != null && oldItem.equals(newItem)) return;
            ACAData selectedData = null;
            Optional optional =  _ACAList.stream().filter(t -> t.acaName == newItem).findFirst();
            if(optional.isPresent()) {
                _selectedACA = (ACAData) optional.get();
                LogMgr.w(TAG,"selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
            }
            if(_selectedACA != null) {
                LogMgr.w("selectedACA = " + _selectedACA.acaCode + " / " + _selectedACA.acaName);
                if (_selectedGrade != null) {
                    _selectedGrade = null;
                }
                if (_spinnerGrade != null) _spinnerGrade.clearSelectedItem();
                if(!TextUtils.isEmpty(_selectedACA.acaCode)) {
                    requestGradeList(_selectedACA.acaCode);
                }else{
                    if(_GradeList != null) _GradeList.clear();
                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
                if(TextUtils.isEmpty(_selectedACA.acaCode)){   //전체
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(false);
                }else{
                    if (_spinnerGrade != null) _spinnerGrade.setEnabled(true);
                }
            }

        });
        _spinnerCampus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerCampus.dismiss();
            }
        });
        _spinnerCampus.setLifecycleOwner(this);
        if(!TextUtils.isEmpty(_appAcaCode) && Constants.MEMBER.equals(_userType)){
            ACAData selectedACA = null;
            Optional option =  _ACAList.stream().filter(t -> t.acaCode.equals(_appAcaCode)).findFirst();
            if(option.isPresent()) {
                selectedACA = (ACAData) option.get();
            }

            try {
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    _spinnerCampus.selectItemByIndex(selectedIndex); //전체

                } else {
                    _spinnerCampus.selectItemByIndex(0);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }else{
            _spinnerCampus.selectItemByIndex(0);
        }


        _spinnerGrade.setIsFocusable(true);
        _spinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
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
            requestBriefingList();
        });
        _spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                _spinnerGrade.dismiss();
            }
        });
        _spinnerGrade.setLifecycleOwner(this);

    }

    private void setRecycler(){
        mAdapter = new BriefingListAdapter(mContext, mList, this::startDetailActivity);
        mRecyclerBrf.setAdapter(mAdapter);
        mRecyclerBrf.addItemDecoration(Utils.setDivider(mContext));
    }

    private void startDetailActivity(BriefingData item, int position){
        if (item != null){

            this.position = position;

            Intent intent = new Intent(mContext, MenuBriefingDetailActivity.class);
            intent.putExtra(IntentParams.PARAM_BRIEFING_INFO, item);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
            resultLauncher.launch(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
        }else LogMgr.e("clickItem is null ");
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.tv_brf_calendar:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_selectedDate);
                Utils.yearMonthPicker(mContext, this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                break;

            case R.id.btn_brf_previous:
                if (selYear <= Constants.PICKER_MIN_YEAR && selMonth <= 0) break;
                navigateMonth(-1);
                break;

            case R.id.btn_brf_next:
                if (selYear >= Constants.PICKER_MAX_YEAR && selMonth >= 11) break;
                navigateMonth(1);
                break;
        }
    }

    private void navigateMonth(int addMonth){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(_selectedDate);
        calendar.add(Calendar.MONTH, addMonth);
        onDateSet(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    @Override
    public void onDateSet(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        _selectedDate = calendar.getTime();

        selYear = calendar.get(Calendar.YEAR);
        selMonth = calendar.get(Calendar.MONTH);

        mTvCalendar.setText(_dateFormat.format(_selectedDate));
        if(_spinnerCampus.getSelectedIndex() >= 0) {
            Message msg = _handler.obtainMessage(CMD_GET_BRIEFINGS);
            _handler.sendMessage(msg);
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
                            LogMgr.e(TAG, "requestGradeList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestGradeList() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }

    private void requestBriefingList(){
        if (RetrofitClient.getInstance() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_selectedDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            String acaCode = "";
            String gradeCode = "";
            if(_selectedACA != null) acaCode = _selectedACA.acaCode;
            if(_selectedGrade != null) gradeCode = String.valueOf(_selectedGrade.gubunCode);

            RetrofitClient.getApiInterface().getBriefingList(acaCode, gradeCode, year, month).enqueue(new Callback<BriefingResponse>() {
                @Override
                public void onResponse(Call<BriefingResponse> call, Response<BriefingResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BriefingData> list = response.body().data;

                                if (list != null) {
                                    if (added){
                                        mList.set(position, list.get(position));
                                        mAdapter.notifyItemChanged(position);

                                        position = -1;
                                        added = false;

                                    }else{
                                        if (mList.size() > 0) mList.clear();
                                        mList.addAll(list);
                                    }
                                    DBUtils.setReadDB(mContext, mList, _memberSeq, DataManager.BOARD_PT, () -> runOnUiThread(() -> mAdapter.notifyDataSetChanged()));

//                                    mAdapter.setWholeCampusMode(TextUtils.isEmpty(acaCode));
                                }
                            }
                        } else {
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBrfList() Exception : ", e.getMessage());
                    }

                    //if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    mSwipeRefresh.setRefreshing(false);
                    if(mList.size() > 0 && mRecyclerBrf!= null) {
                        _handler.postDelayed(() -> mRecyclerBrf.smoothScrollToPosition(0), scrollToTopDelay);
                    }
                }

                @Override
                public void onFailure(Call<BriefingResponse> call, Throwable t) {
                    mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvEmptyList.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}