package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ACA_SCHEDULE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_COUNSEL;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_PT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_SYSTEM;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_TEST_APPT;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.AnnouncementListAdapter;
import kr.jeet.edu.student.adapter.MainMenuListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.dialog.PopupDialog;
import kr.jeet.edu.student.dialog.PushPopupDialog;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.BoardAttributeData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.MainMenuItemData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.BoardAttributeResponse;
import kr.jeet.edu.student.model.response.GetACAListResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.SchoolListResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.decoration.GridSpaceItemDecoration;
import kr.jeet.edu.student.view.decoration.LastIndexDeleteDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView, announceRecycler;
    private MainMenuListAdapter mAdapter;
    private AnnouncementListAdapter announceAdapter;
    private ArrayList<MainMenuItemData> mList = new ArrayList<>();
    private ArrayList<AnnouncementData> announceList = new ArrayList<>();
    private TextView mTvStudentName, mTvSchoolAndGradeName, mTvStudentCampus, mTvGrade, mTvNonMember,
            mTvAttendance, mTvAttendanceDate, mTvNonMemberNoti, mTvNotifyContent, mTvTeacherName, mTvListEmpty, mTvNameSub;
    private ImageView imgStudentAttendance;
    private LinearLayoutCompat mLayoutBottom;
    private ConstraintLayout layoutAttend, layoutNotify;

    private RetrofitApi mRetrofitApi;
    boolean doubleBackToExitPressedOnce = false;

    private final int CMD_GET_ACALIST = 1;  // ACA정보 가져오기
    private final int CMD_GET_MEMBER_INFO = 2;       // 자녀정보 가져오기
    private final int CMD_GET_NOTIFY_INFO = 3;       // 공지사항 정보 가져오기
    private final int CMD_GET_BOARD_ATTRIBUTE = 4;       // 게시판 속성 조회하기
    private final int CMD_GET_SCHOOL_LIST = 5;       // 학교 목록 조회하기
    private final int CMD_GET_LTC_LIST = 6;       // LTC 목록 가져오기
    private final int CMD_GET_TEACHER = 7;       // LTC 목록 가져오기

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private String acaCode = "";
    private int teacherCnt = 0;
    private int _memberSeq = 0;
    private int stCodeParent = 0;

    private PushMessage _pushMessage;

    private boolean isMain = true;

    private BroadcastReceiver pushNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED.equals(intent.getAction())){
                LogMgr.w(TAG, "broadcast onReceived ");
                if(intent.hasExtra(IntentParams.PARAM_ATTENDANCE_INFO)) {
                    String type = intent.getStringExtra(IntentParams.PARAM_ATTENDANCE_INFO);
                    if(type.equals(MSG_TYPE_ATTEND)) {
                        new Thread(() -> {
                            try {
                                List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_ATTEND);
                                if(pushMessages.isEmpty()) {
                                    setNewCounselContent(false);
                                }else{
                                    setNewCounselContent(true);
                                }
                            }catch(Exception e){

                            }
                        }).start();
                    }
                }
            }
        }
    };

    void setNewCounselContent(boolean isNew) {
        runOnUiThread(()->{
            if(layoutNotify != null) {
                if (isNew) {
                    layoutNotify.setBackground(getDrawable(R.drawable.selector_main_box_new));
                } else {
                    layoutNotify.setBackground(getDrawable(R.drawable.selector_main_box));
                }
            }
        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACALIST:
                    requestACAList();
                    break;
                case CMD_GET_MEMBER_INFO:
                    requestMemberInfo(_stuSeq, _stCode);
                    if (_userGubun == Constants.USER_TYPE_PARENTS) requestMemberInfo(_memberSeq, stCodeParent);
                    break;
                case CMD_GET_NOTIFY_INFO:
                    requestBoardList(acaCode);
                    break;
                case CMD_GET_BOARD_ATTRIBUTE:
                    requestBoardAttribute();
                    break;
                case CMD_GET_SCHOOL_LIST:
                    requestSchoolList();
                    break;
                case CMD_GET_LTC_LIST:
                    requestLTCList();
                    break;
                case CMD_GET_TEACHER:
                    requestTeacherCls();
                    break;
            }
        }
    };

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent != null && result.getResultCode() != RESULT_CANCELED) {
            if(intent.hasExtra(IntentParams.PARAM_RD_CNT_ADD)) {
                isMain = true;
                announceAdapter = new AnnouncementListAdapter(mContext, announceList, isMain, this::startBoardDetailActivity);
                announceRecycler.setAdapter(announceAdapter);
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_RD_CNT_ADD, false);
                if(added) requestBoardList(acaCode);
            }
            //mHandler.sendEmptyMessage(CMD_GET_ACALIST);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initAppbar();
        initView();
    }

    @Override
    public void onBackPressed() {
        if (PreferenceUtil.getNumberOfChild(mContext) > 0){ // 자녀 인원수가 1명 이상인 경우 뒤로가기 버튼 활성화
            startActivity(new Intent(mContext, SelectStudentActivity.class));
            finish();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_backbutton_to_exit, Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setLogoVisible(true);
        setSupportActionBar(customAppbar.getToolbar());

        if (PreferenceUtil.getNumberOfChild(mContext) > 0){ // 자녀 인원수가 1명 이상인 경우 뒤로가기 버튼 활성화
            customAppbar.setMainBtnLeftClickListener(v -> { startActivity(new Intent(mContext, SelectStudentActivity.class)); });
        }

    }
    @Override
    void initView() {
        findViewById(R.id.btn_teacher).setOnClickListener(this);
        findViewById(R.id.btn_attendance_state).setOnClickListener(this);

        layoutAttend = findViewById(R.id.btn_attendance_state);

        mTvStudentName = findViewById(R.id.tv_student_name);
        mTvStudentCampus = findViewById(R.id.tv_student_campus);
        mTvSchoolAndGradeName = findViewById(R.id.tv_student_school_and_grade);
        imgStudentAttendance = findViewById(R.id.img_student_attendance);
        mTvAttendance = findViewById(R.id.tv_attendance);
        mTvAttendanceDate = findViewById(R.id.tv_attendance_date);
        mTvNonMember = findViewById(R.id.tv_non_member);
        mTvNonMemberNoti = findViewById(R.id.tv_non_member_notice);
        mTvTeacherName = findViewById(R.id.tv_teacher_name);
        mTvListEmpty = findViewById(R.id.tv_main_empty_list);
        mTvNameSub = findViewById(R.id.tv_name_sub);

        mLayoutBottom = findViewById(R.id.layout_bottom);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_menu);
        announceRecycler = (RecyclerView) findViewById(R.id.recycler_announcement);

        initData();

        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(mContext);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        mRecyclerView.setLayoutManager(fblManager);

        mAdapter= new MainMenuListAdapter(mContext, mList, this::startMenuActivity);
        mRecyclerView.setAdapter(mAdapter);

        setAnnounceRecycler();
    }

    private void setAnnounceRecycler(){
        announceAdapter = new AnnouncementListAdapter(mContext, announceList, isMain, this::startBoardDetailActivity);
        announceRecycler.setAdapter(announceAdapter);

        Drawable dividerDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_line);
        LastIndexDeleteDecoration dividerItemDecoration = new LastIndexDeleteDecoration(dividerDrawable);
        announceRecycler.addItemDecoration(dividerItemDecoration);
    }

    private void startBoardDetailActivity(AnnouncementData clickItem){
        if (clickItem != null){
            Intent targetIntent = new Intent(mContext, MenuBoardDetailActivity.class);
            targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, clickItem);
            targetIntent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
            resultLauncher.launch(targetIntent);

        }else LogMgr.e("clickItem is null ");
    }

    private void startMenuActivity(MainMenuItemData clickItem){
        if(clickItem.getTargetClass() != null) {
            Intent targetIntent = new Intent(mContext, clickItem.getTargetClass());
            resultLauncher.launch(targetIntent);
        }else{
            LogMgr.d("targetIntent is null at " + getString(clickItem.getTitleRes()));
        }
    }

    private void initData(){
        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                LogMgr.e(TAG, "push msg ");
                _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                LogMgr.e(TAG, "msg = " + _pushMessage.body);
            } else {
                LogMgr.e(TAG, "push msg is null");
            }
//            if (intent.getExtras() != null) {
//                Bundle map = intent.getExtras();
//                for (String key : map.keySet()) {
//                    LogMgr.e(TAG, "key = " + key + " : value = " + map.get(key));
//                }
//            }
        }

        _memberSeq = PreferenceUtil.getUserSeq(mContext);
        _userType = PreferenceUtil.getUserType(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);

        try {
            initMenusMember();

            if (_userType != null) {
                if (_userType.equals(Constants.MEMBER)) { // 회원

                    imgStudentAttendance.setVisibility(View.VISIBLE);
                    //mTvAttendance.setVisibility(View.VISIBLE);
                    mTvAttendanceDate.setVisibility(View.VISIBLE);
                    mTvNonMember.setVisibility(View.INVISIBLE);

                    if (_userGubun == Constants.USER_TYPE_STUDENT){ // 원생
                        mTvNameSub.setText("님");
                    }else{ // 원생이 아닌경우
                        mTvNameSub.setText("학부모님");
                    }

                }else{ // 비회원
                    mTvSchoolAndGradeName.setVisibility(View.GONE);
                    mTvStudentCampus.setVisibility(View.GONE);
                    mTvNonMemberNoti.setVisibility(View.VISIBLE);
                    imgStudentAttendance.setVisibility(View.INVISIBLE); // 출석 배지
                    mTvAttendance.setVisibility(View.GONE); // 출석 text
                    mTvAttendanceDate.setVisibility(View.GONE); // 출석날짜
                    mTvNonMember.setVisibility(View.VISIBLE); // 비회원 배지

                    mLayoutBottom.setVisibility(View.GONE); // 화면 하단 레이아웃 gone

                    LogMgr.e(TAG, "No Member");
                }
            }

        }catch (Exception e){ LogMgr.e(TAG + "initData() Exception : ", e.getMessage()); }
        if(_pushMessage != null) {

            LogMgr.e("EVENT", _pushMessage.pushType);

            switch(_pushMessage.pushType) {
                case MSG_TYPE_NOTICE:   //공지사항의 경우 공지사항 상세페이지로 이동
                {
                    intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
                    startDetailActivity(intent, MenuBoardDetailActivity.class);
                }
                break;
                case MSG_TYPE_ATTEND: // 출결알림
                {
                    if (PreferenceUtil.getNumberOfChild(mContext) < 2){
                        PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
                        pushPopupDialog.setOnOkButtonClickListener(view -> {
                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {
                                List<String> list = new ArrayList<>();
                                list.add(_pushMessage.pushId);
                                pushPopupDialog.getFCMManager().requestPushConfirmToServer(_pushMessage);
                            }
                            pushPopupDialog.dismiss();
                        });
                        pushPopupDialog.show();
                    }
                }
                case MSG_TYPE_TEST_APPT:
                case MSG_TYPE_COUNSEL:
                {
//                    PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
//                    pushPopupDialog.setOnOkButtonClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {
//                                List<String> list = new ArrayList<>();
//                                list.add(_pushMessage.pushId);
//                                pushPopupDialog.getFCMManager().requestPushConfirmToServer(list);
//                            }
//                            pushPopupDialog.dismiss();
//                        }
//                    });
//                    pushPopupDialog.show();
                }
                break;
                case MSG_TYPE_PT: // 설명회예약
                {
                    startDetailActivity(intent, MenuBriefingDetailActivity.class);
                }
                break;
                case MSG_TYPE_SYSTEM: // 시스템알림
                {
                    intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.push_type_system));
                    startDetailActivity(intent, MenuBoardDetailActivity.class);
                }
                break;
                case MSG_TYPE_ACA_SCHEDULE: // 캠퍼스일정
                {
                    startDetailActivity(intent, MenuScheduleDetailActivity.class);
                }
                break;
                default:
                    break;
            }
        }

        mHandler.sendEmptyMessage(CMD_GET_ACALIST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED);
        registerReceiver(pushNotificationReceiver, intentFilter);

        new Thread(() -> {
            try {
                List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_ATTEND);
                if(pushMessages.isEmpty()) {
                    setNewCounselContent(false);
                }else{
                    setNewCounselContent(true);
                }
            }catch(Exception e){

            }
        }).start();
    }

    private void startDetailActivity(Intent intent, Class<?> targetActivity) {
        if (targetActivity != null) {
            Intent noticeIntent = new Intent(this, targetActivity);
            noticeIntent.putExtras(intent);
            resultLauncher.launch(noticeIntent);
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(pushNotificationReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_attendance_state:
                startActivityBottomMenu(MenuNoticeActivity.class);
                break;

            case R.id.btn_teacher:
                LogMgr.i(TAG, teacherCnt +"");
                if (teacherCnt > 0) startActivityBottomMenu(TeacherInfoActivity.class);
                break;
        }
    }

    private void startActivityBottomMenu(Class<?> cls){
        Intent targetIntent = new Intent(mContext, cls);

        if (cls == MenuNoticeActivity.class){
            targetIntent.putExtra(IntentParams.PARAM_TYPE_FROM_BOTTOM_MENU, true);
            startActivity(targetIntent);

        }else if (cls == TeacherInfoActivity.class){
            startActivity(targetIntent);
        }
    }

    private void initMenusMember() {
        if(mList != null) mList.clear();

        if (_userType.equals(Constants.MEMBER)) { // 회원
            //원생정보
            mList.add(new MainMenuItemData(R.drawable.icon_menu_student, R.string.main_menu_student_info, MenuStudentInfoActivity.class));
            //공지사항
            mList.add(new MainMenuItemData(R.drawable.icon_menu_attention, R.string.main_menu_announcement, MenuAnnouncementActivity.class));
            //캠퍼스일정
            mList.add(new MainMenuItemData(R.drawable.icon_menu_schedule, R.string.main_menu_campus_schedule,MenuScheduleActivity.class));
            //알림장
            mList.add(new MainMenuItemData(R.drawable.icon_menu_notice, R.string.main_menu_notice, MenuNoticeActivity.class));
            //테스트예약
            mList.add(new MainMenuItemData(R.drawable.icon_menu_test_reserve, R.string.main_menu_test_reserve, MenuTestReserveActivity.class));
            //차량정보
            mList.add(new MainMenuItemData(R.drawable.icon_menu_bus, R.string.main_menu_vehicle_info, MenuBusActivity.class));
            //설명회예약
            mList.add(new MainMenuItemData(R.drawable.icon_menu_briefing, R.string.main_menu_briefing_reserve, MenuBriefingActivity.class));

        }else{ // 비회원
            //공지사항
            mList.add(new MainMenuItemData(R.drawable.icon_menu_attention, R.string.main_menu_announcement, MenuAnnouncementActivity.class));
            //캠퍼스일정
            mList.add(new MainMenuItemData(R.drawable.icon_menu_schedule, R.string.main_menu_campus_schedule,MenuScheduleActivity.class));
            //테스트예약
            mList.add(new MainMenuItemData(R.drawable.icon_menu_test_reserve, R.string.main_menu_test_reserve, MenuTestReserveActivity.class));
            //설명회예약
            mList.add(new MainMenuItemData(R.drawable.icon_menu_briefing, R.string.main_menu_briefing_reserve, MenuBriefingActivity.class));
        }

    }

    // 캠퍼스 목록 조회
    private void requestACAList(){
        showProgressDialog();
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getACAList().enqueue(new Callback<GetACAListResponse>() {
                @Override
                public void onResponse(Call<GetACAListResponse> call, Response<GetACAListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<ACAData> list = response.body().data;
                                DataManager.getInstance().setACAList(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestACAList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_MEMBER_INFO);
                }

                @Override
                public void onFailure(Call<GetACAListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestACAList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_MEMBER_INFO);
                }
            });
        }
    }

    // 학교 목록 조회
    private void requestSchoolList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getSchoolList().enqueue(new Callback<SchoolListResponse>() {
                @Override
                public void onResponse(Call<SchoolListResponse> call, Response<SchoolListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<SchoolData> list = response.body().data;
                                DataManager.getInstance().setSchoolList(list);
                                DataManager.getInstance().initSchoolListMap(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestSchoolList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_LTC_LIST);
                }

                @Override
                public void onFailure(Call<SchoolListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestSchoolList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestSchoolList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_LTC_LIST);
                }
            });
        }
    }

    // 테스트예약 캠퍼스 목록 조회
    private void requestLTCList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getLTCList().enqueue(new Callback<LTCListResponse>() {
                @Override
                public void onResponse(Call<LTCListResponse> call, Response<LTCListResponse> response) {
                    try {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                List<LTCData> list = response.body().data;
                                DataManager.getInstance().setLTCList(list);
                            }
                        } else {
                            LogMgr.e(TAG, "requestLTCList() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_TEACHER);
                }

                @Override
                public void onFailure(Call<LTCListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestLTCList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestLTCList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_TEACHER);
                }
            });
        }
    }

    // 원생 정보 조회
    private void requestMemberInfo(int stuSeq, int stCode){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.studentInfo(stuSeq, stCode).enqueue(new Callback<StudentInfoResponse>() {
                @Override
                public void onResponse(Call<StudentInfoResponse> call, Response<StudentInfoResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            StudentInfo getData = new StudentInfo();
                            if (response.body() != null)  getData= response.body().data;

                            if (getData != null) {
                                mTvAttendanceDate.setText(Utils.currentDate("yy.MM.dd"));

                                if (getData.acaName != null) { // 캠퍼스명
                                    PreferenceUtil.setAcaName(mContext, getData.acaName);
                                    mTvStudentCampus.setText(getData.acaName);
                                }
                                if (getData.acaCode != null) {
                                    acaCode = getData.acaCode;
                                    PreferenceUtil.setAcaCode(mContext, getData.acaCode);
                                }

                                List<ACAData> item = DataManager.getInstance().getACAList();
                                for (ACAData data : item){
                                    if (acaCode.equals(data.acaCode)) {
                                        PreferenceUtil.setAcaTel(mContext, data.acaTel);
                                    }
                                }

                                PreferenceUtil.setStuGender(mContext, getData.gender);
                                //PreferenceUtil.setParentPhoneNum(mContext, getData.parentPhoneNumber);
                                PreferenceUtil.setStuBirth(mContext, getData.birth);

                                if (_userType.equals(Constants.MEMBER)){

                                    if (_userGubun == Constants.USER_TYPE_PARENTS){
                                        LogMgr.e("EVENT1");
                                        if (stCode == 0) {
                                            PreferenceUtil.setParentName(mContext, getData.name);
                                            PreferenceUtil.setParentPhoneNum(mContext, getData.phoneNumber);
                                        }else{
                                            PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                            PreferenceUtil.setStName(mContext, getData.name);

                                            if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용
                                                if (_stName != null) mTvStudentName.setText(_stName); // 자녀선택화면의 이름
                                            }else mTvStudentName.setText(getData.name); // 원생 오리지널 이름
                                        }
                                    }else{
                                        LogMgr.e("EVENT2");
                                        PreferenceUtil.setParentPhoneNum(mContext, getData.parentPhoneNumber);
                                        PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                        PreferenceUtil.setStName(mContext, getData.name);
                                        PreferenceUtil.setParentName(mContext, "");

                                        mTvStudentName.setText(getData.name); // 원생 오리지널 이름
                                    }
                                }else{
                                    if (_userGubun == Constants.USER_TYPE_PARENTS){
                                        if (stCode == 0) {
                                            LogMgr.e("EVENT3");
                                            PreferenceUtil.setParentName(mContext, getData.name);
                                            PreferenceUtil.setParentPhoneNum(mContext, getData.phoneNumber);

                                        }else{
                                            LogMgr.e("EVENT4");
                                            PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                            PreferenceUtil.setStName(mContext, getData.name);
                                        }

                                        if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용
                                            if (_stName != null) mTvStudentName.setText(_stName); // 자녀선택화면의 이름
                                        }else mTvStudentName.setText(Utils.getStr(getData.name)); // 원생 오리지널 이름

                                    }else{
                                        LogMgr.e("EVENT5");
                                        PreferenceUtil.setParentPhoneNum(mContext, getData.parentPhoneNumber);
                                        PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                        PreferenceUtil.setStName(mContext, getData.name);
                                        PreferenceUtil.setParentName(mContext, "");

                                        if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용
                                            if (_stName != null) mTvStudentName.setText(_stName); // 자녀선택화면의 이름
                                        }else mTvStudentName.setText(Utils.getStr(getData.name)); // 원생 오리지널 이름
                                    }
                                }


                                if (getData.scName.equals("")) mTvSchoolAndGradeName.setText(getData.stGrade);
                                else if (getData.stGrade.equals("")) mTvSchoolAndGradeName.setText(getData.scName);
                                else mTvSchoolAndGradeName.setText(getData.scName + " " + getData.stGrade); // 학교, 학년

                                mTvAttendance.setText(R.string.main_null_attendance); // 출결 현황 text

                            }

                        }else{
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
                }
            });
        }
    }

    // 게시판 속성 조회
    private void requestBoardAttribute(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getBoardAttribute().enqueue(new Callback<BoardAttributeResponse>() {
                @Override
                public void onResponse(Call<BoardAttributeResponse> call, Response<BoardAttributeResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<BoardAttributeData> getData = null;
                            if (response.body() != null)  {
                                getData= response.body().data;
                                if (getData != null) {
                                    List<BoardAttributeData> list = response.body().data;
                                    for(BoardAttributeData data : list){
                                        DataManager.getInstance().setBoardInfo(data);
                                    }
                                }
                            }
                        }else{
                            LogMgr.e(TAG, "requestBoardAttribute() errBody : " + response.errorBody().string());

                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestBoardAttribute() Exception : ", e.getMessage()); }


                    mHandler.sendEmptyMessage(CMD_GET_SCHOOL_LIST);
                }
                @Override
                public void onFailure(Call<BoardAttributeResponse> call, Throwable t) {
                    try{
                        LogMgr.e(TAG, "requestBoardAttribute() onFailure >> " + t.getMessage());

                    }catch (Exception e){ LogMgr.e(TAG + "requestBoardAttribute() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_SCHOOL_LIST);
                }
            });
        }
    }

    // 공지사항 목록 정보 조회
    private void requestBoardList(String acaCodes) {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getAnnouncementList(0, acaCodes).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<AnnouncementData> getData = new ArrayList<>();

                            if (announceList.size() > 0) announceList.clear();

                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    for (int i = 0; i < 3; i++) {
                                        AnnouncementData item = new AnnouncementData();
                                        item.seq = getData.get(i).seq;
                                        item.rdcnt = getData.get(i).rdcnt;
                                        item.title = getData.get(i).title;
                                        item.insertDate = getData.get(i).insertDate;
                                        item.fileList = getData.get(i).fileList;
                                        announceList.add(item);
                                    }

                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }
                        }

                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardList() Exception: ", e.getMessage());
                    }
                    mTvListEmpty.setVisibility(announceList.isEmpty() ? View.VISIBLE : View.GONE);
                    LogMgr.e(TAG, "ListData is null4: " +announceList.get(0).insertDate);
                    announceAdapter.notifyDataSetChanged();

                    mHandler.sendEmptyMessage(CMD_GET_BOARD_ATTRIBUTE);
                }

                @Override
                public void onFailure(Call<AnnouncementListResponse> call, Throwable t) {
                    mTvListEmpty.setVisibility(announceList.isEmpty() ? View.VISIBLE : View.GONE);
                    announceAdapter.notifyDataSetChanged();

                    mHandler.sendEmptyMessage(CMD_GET_BOARD_ATTRIBUTE);
                }
            });
        }
    }

    // 원생 학급 정보조회
    private void requestTeacherCls(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.requestTeacherCls(_stCode, Utils.currentDate("yyyyMM")).enqueue(new Callback<TeacherClsResponse>() {
                @Override
                public void onResponse(Call<TeacherClsResponse> call, Response<TeacherClsResponse> response) {
                    try {
                        if (response.isSuccessful()){

                            TeacherClsResponse getData = response.body();
                            if (getData != null && getData.data != null && !getData.data.isEmpty()){
                                DataManager.getInstance().initClsListMap(getData.data);
                                String str = getData.data.get(0).sfName;
                                teacherCnt = getData.data.size();

                                if (teacherCnt <= 1) mTvTeacherName.setText(TextUtils.isEmpty(str) ? "" : str+" 선생님");
                                else mTvTeacherName.setText(TextUtils.isEmpty(str) ? "" : str+" 외 선생님");
                            }

                            LogMgr.e(TAG, "TeacherCnt: " + teacherCnt);

                        }else{
                            LogMgr.e(TAG, "requestTeacherCls() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<TeacherClsResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestTeacherCls() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }
                    hideProgressDialog();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(mContext, SettingsActivity.class);
                resultLauncher.launch(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}