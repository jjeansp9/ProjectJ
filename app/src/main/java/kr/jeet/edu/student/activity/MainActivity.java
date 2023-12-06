package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ACA_SCHEDULE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_PT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_QNA_COMPLETE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_QNA_ING;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_REPORT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_SYSTEM;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_TEST_APPT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_TUITION;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.menu.announcement.MenuAnnouncementActivity;
import kr.jeet.edu.student.activity.menu.announcement.MenuAnnouncementDetailActivity;
import kr.jeet.edu.student.activity.menu.attendance.MenuAttendanceActivity;
import kr.jeet.edu.student.activity.menu.briefing.MenuBriefingActivity;
import kr.jeet.edu.student.activity.menu.briefing.MenuBriefingDetailActivity;
import kr.jeet.edu.student.activity.menu.bus.MenuBusActivity;
import kr.jeet.edu.student.activity.menu.leveltest.MenuTestReserveActivity;
import kr.jeet.edu.student.activity.menu.leveltest.MenuTestReserveDetailActivity;
import kr.jeet.edu.student.activity.menu.notice.MenuNoticeActivity;
import kr.jeet.edu.student.activity.menu.notice.MenuNoticeDetailActivity;
import kr.jeet.edu.student.activity.menu.qna.MenuQNAActivity;
import kr.jeet.edu.student.activity.menu.qna.MenuQNADetailActivity;
import kr.jeet.edu.student.activity.menu.reportcard.MenuReportCardActivity;
import kr.jeet.edu.student.activity.menu.reportcard.ReportCardDetailActivity;
import kr.jeet.edu.student.activity.menu.schedule.MenuScheduleActivity;
import kr.jeet.edu.student.activity.menu.schedule.MenuScheduleDetailActivity;
import kr.jeet.edu.student.activity.setting.SettingsActivity;
import kr.jeet.edu.student.adapter.AnnouncementListAdapter;
import kr.jeet.edu.student.adapter.MainMenuListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.dialog.PushPopupDialog;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.BoardAttributeData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.MainMenuItemData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.StudentInfo;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.BoardAttributeResponse;
import kr.jeet.edu.student.model.response.GetACAListResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.SchoolListResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.HttpUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import kr.jeet.edu.student.view.decoration.LastIndexDeleteDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    //private ImageView ivAttendanceNew;
    private ConstraintLayout btnTeacher, btnTuition, newAnnouncement;

    private RetrofitApi mRetrofitApi;
    boolean doubleBackToExitPressedOnce = false;

    private final int CMD_GET_ACALIST = 1;  // ACA정보
    private final int CMD_GET_LOCAL_ACALIST = 2;  // 지역별 캠퍼스정보
    private final int CMD_GET_MEMBER_INFO = 3;       // 자녀정보
    private final int CMD_GET_NOTIFY_INFO = 4;       // 공지사항 정보
    private final int CMD_GET_BOARD_ATTRIBUTE = 5;       // 게시판 속성
    private final int CMD_GET_SCHOOL_LIST = 6;       // 학교 목록
    private final int CMD_GET_LTC_LIST = 7;       // LTC 목록
    private final int CMD_GET_TEACHER = 8;       // 지도강사 목록
    private final int CMD_GET_LTC_SUBJECT = 9;       // LTC 과목 목록

    private String _userType = "";
    private String _stName = "";
    private int _stuSeq = 0;
    private int _userGubun = 0;
    private int _stCode = 0;
    private String acaCode = "";
    private int teacherCnt = 0;
    private int _memberSeq = 0;
    private int stCodeParent = 0;
    private int _childCnt = -1;

    private PushMessage _pushMessage;

    private boolean isMain = true;

    private final String MR_STUDENT = "님";
    private final String MR_PARENT = "학부모님";
    private final String STR_NON_MEMBER = " (비회원)";

    private final int TWO_PEOPLE = 2;

    private BroadcastReceiver pushNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED.equals(intent.getAction())){
                    LogMgr.w(TAG, "broadcast onReceived ");
                    if(intent.hasExtra(IntentParams.PARAM_ATTENDANCE_INFO)) {
                        String type = intent.getStringExtra(IntentParams.PARAM_ATTENDANCE_INFO);
//                        if(type.equals(MSG_TYPE_ATTEND)) {
//                            new Thread(() -> {
//                                try {
//                                    List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_ATTEND);
//                                    if(pushMessages.isEmpty()) {
//                                        setNewAttendanceContent(false);
//                                    }else{
//                                        for (PushMessage data : pushMessages) {
//                                            if (data.stCode == _stCode) setNewAttendanceContent(true);
//                                        }
//                                    }
//                                }catch(Exception e){
//
//                                }
//                            }).start();
//                        }
                    }
                }
            }
        }
    };

    // 출석상태 업데이트되었을 때 New 표시
//    void setNewAttendanceContent(boolean isNew) {
//        runOnUiThread(()->{
//            if(ivAttendanceNew != null) {
//                if (isNew) {
//                    ivAttendanceNew.setVisibility(View.VISIBLE);
////                    layoutNotify.setBackground(getDrawable(R.drawable.selector_main_box_new));
//                } else {
//                    ivAttendanceNew.setVisibility(View.INVISIBLE);
////                    layoutNotify.setBackground(getDrawable(R.drawable.selector_main_box));
//                }
//            }
//        });
//    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACALIST:
                    requestACAList();
                    break;
                case CMD_GET_LOCAL_ACALIST:
                    requestLocalACAList();
                    break;
                case CMD_GET_MEMBER_INFO:
                    requestMemberInfo(_stuSeq, _stCode, true);
                    if (_userGubun == Constants.USER_TYPE_PARENTS) requestMemberInfo(_memberSeq, stCodeParent, false);
                    break;
                case CMD_GET_NOTIFY_INFO:
                    requestBoardList(PreferenceUtil.getAppAcaCode(mContext), "");
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
                case CMD_GET_LTC_SUBJECT:
                    HttpUtils.requestLTCSubjectList();
                    break;
            }
        }
    };

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent != null && result.getResultCode() != RESULT_CANCELED) {
            if(intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD)) { // 신규원생을 추가했을 경우
                intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD, true);
                intent.putExtra(IntentParams.PARAM_TEST_NEW_CHILD_FROM_MAIN, true);
                setResult(RESULT_OK, intent);
                finish();

            } else {
                mHandler.sendEmptyMessage(CMD_GET_MEMBER_INFO);
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initAppbar();
    }

    @Override
    public void onBackPressed() {
//        if (_userGubun == Constants.USER_TYPE_PARENTS) {
//            startActivity(new Intent(mContext, SelectStudentActivity.class));
//            finish();
//            return;
//        }
//        if (PreferenceUtil.getNumberOfChild(mContext) > 0){ // 자녀 인원수가 1명 이상인 경우
//            startActivity(new Intent(mContext, SelectStudentActivity.class));
//            finish();
//            return;
//        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            DataManager.getInstance().isSelectedChild = false;
            finishAffinity();
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

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle("");
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(false);
        setSupportActionBar(customAppbar.getToolbar());
        if (_userGubun == Constants.USER_TYPE_PARENTS) {
            customAppbar.setMainBtnLeftClickListener( v -> startActivity(new Intent(mContext, SelectStudentActivity.class)) );
        }
//        if (PreferenceUtil.getNumberOfChild(mContext) > 0){ // 자녀 인원수가 1명 이상인 경우 뒤로가기 버튼 활성화
//            customAppbar.setMainBtnLeftClickListener(v -> { startActivity(new Intent(mContext, SelectStudentActivity.class)); });
//        }

    }
    void initView() {
        findViewById(R.id.btn_teacher).setOnClickListener(this);
        findViewById(R.id.btn_attendance_state).setOnClickListener(this);
        //ivAttendanceNew = findViewById(R.id.img_attendance_new);
        btnTeacher = findViewById(R.id.btn_teacher);
        btnTuition = findViewById(R.id.btn_attendance_state);
        newAnnouncement = findViewById(R.id.layout_announcement);

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

    private void startBoardDetailActivity(AnnouncementData clickItem, TextView title, int position){
        if (clickItem != null){
            Intent targetIntent = new Intent(mContext, MenuAnnouncementDetailActivity.class);
            targetIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, clickItem);
            targetIntent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
            targetIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, position);
            resultLauncher.launch(targetIntent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);

        }else LogMgr.e("clickItem is null ");
    }

    private void startMenuActivity(MainMenuItemData clickItem){
        if(clickItem.getTargetClass() != null) {
            Intent targetIntent = new Intent(mContext, clickItem.getTargetClass());
            resultLauncher.launch(targetIntent);
            overridePendingTransition(R.anim.vertical_enter, R.anim.none);
        }else{
            Toast.makeText(mContext, "개발 중인 기능입니다.", Toast.LENGTH_SHORT).show();
            LogMgr.d("targetIntent is null at " + getString(clickItem.getTitleRes()));
        }
    }

    private void initData(){

        PreferenceUtil.setStuPhoneNum(mContext, "");
        PreferenceUtil.setParentPhoneNum(mContext, "");

        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                LogMgr.e(TAG, "push msg ");
                _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
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
        _userType = PreferenceUtil.getUserIsOriginal(mContext);
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        _stuSeq = PreferenceUtil.getStuSeq(mContext);
        _stName = PreferenceUtil.getStName(mContext);
        _stCode = PreferenceUtil.getUserSTCode(mContext);
        _childCnt = PreferenceUtil.getNumberOfChild(mContext);

        try {
            initMenusMember();

            if (_userType != null) {

                String str = "";

                if (_userType.equals(Constants.MEMBER)) { // 회원

                    imgStudentAttendance.setVisibility(View.GONE);
                    //mTvAttendance.setVisibility(View.VISIBLE);
                    mTvAttendanceDate.setVisibility(View.GONE);
                    mTvNonMember.setVisibility(View.GONE);

                    if (_userGubun == Constants.USER_TYPE_STUDENT) str = MR_STUDENT; // 원생
                    else str = MR_PARENT; // 부모

                }else{ // 비회원
                    mTvSchoolAndGradeName.setVisibility(View.GONE);
                    mTvStudentCampus.setVisibility(View.GONE);
                    mTvNonMemberNoti.setVisibility(View.VISIBLE);
                    imgStudentAttendance.setVisibility(View.GONE); // 출석 배지
                    mTvAttendance.setVisibility(View.GONE); // 출석 text
                    mTvAttendanceDate.setVisibility(View.GONE); // 출석날짜
                    mTvNonMember.setVisibility(View.GONE); // 비회원 배지

                    btnTeacher.setVisibility(View.GONE); // 화면 하단 지도강사 버튼 gone
                    btnTuition.setVisibility(View.GONE); // 화면 하단 수강료납부 버튼 gone
                    newAnnouncement.setVisibility(View.GONE); // 최근 공지사항 레이아웃 gone

                    if (_userGubun == Constants.USER_TYPE_STUDENT) str = MR_STUDENT +STR_NON_MEMBER; // 원생
                    else str = MR_PARENT+STR_NON_MEMBER; // 부모

                    LogMgr.e(TAG, "No Member");
                }
                mTvNameSub.setText(str);
            }

        }catch (Exception e){ LogMgr.e(TAG + "initData() Exception : ", e.getMessage()); }

        mHandler.sendEmptyMessage(CMD_GET_ACALIST);

        if(_pushMessage != null) {

            LogMgr.e("EVENT", _pushMessage.pushType);

            switch(_pushMessage.pushType) {
                case MSG_TYPE_NOTICE:   //공지사항의 경우 공지사항 상세페이지로 이동
                {
                    //if (intent != null) startBoardDetail(intent, getString(R.string.main_menu_announcement));
                    startDetailActivity(MenuAnnouncementDetailActivity.class);
                }
                break;

                case MSG_TYPE_ATTEND: // 출결알림
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    if (_childCnt < TWO_PEOPLE){
                        PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
                        pushPopupDialog.setOnOkButtonClickListener(view -> {
                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {
                                pushPopupDialog.getFCMManager().requestPushConfirmToServer(_pushMessage, _stCode);
                            }
                            pushPopupDialog.dismiss();
                        });
                        pushPopupDialog.show();
                    }
                }
                break;

                case MSG_TYPE_TEST_APPT: // 테스트예약
                {

                }
                break;

                case MSG_TYPE_PT: // 설명회예약
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    startDetailActivity(MenuBriefingDetailActivity.class);
                }
                break;

                case MSG_TYPE_SYSTEM: // 시스템알림
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    if (_pushMessage.stCode == _stCode) startDetailActivity(MenuNoticeDetailActivity.class);

                }
                break;

                case MSG_TYPE_ACA_SCHEDULE: // 캠퍼스일정
                {
                    startDetailActivity(MenuScheduleDetailActivity.class);
                }
                break;

                case MSG_TYPE_REPORT: // 성적표
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    if (_pushMessage.stCode == _stCode) startDetailActivity(ReportCardDetailActivity.class);
                }
                break;

                case MSG_TYPE_TUITION: // 미납
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    if (_pushMessage.stCode == _stCode) startActivity(new Intent(mContext, TuitionActivity.class));
                }
                break;

                case MSG_TYPE_QNA_ING: // Qna 접수 or 접수완료
                case MSG_TYPE_QNA_COMPLETE: // Qna 접수 or 접수완료
                {
                    if (_pushMessage.memberSeq != _memberSeq) {
                        _pushMessage = null;
                        return;
                    }
                    if (_pushMessage.stCode == _stCode) startDetailActivity(MenuQNADetailActivity.class);
                }
                break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED);
        registerReceiver(pushNotificationReceiver, intentFilter);

//        new Thread(() -> {
//            try {
//                List<PushMessage> pushMessages = JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().getMessageByReadFlagNType(false, MSG_TYPE_ATTEND);
//                if(pushMessages.isEmpty()) {
//                    setNewAttendanceContent(false);
//                }else{
//                    for (PushMessage data : pushMessages) {
//                        if (data.stCode == _stCode) setNewAttendanceContent(true);
//                    }
//                }
//            }catch(Exception e){
//
//            }
//        }).start();

        requestBoardList(PreferenceUtil.getAppAcaCode(mContext), "");
    }

    private void startDetailActivity(Class<?> targetActivity) {
        if (targetActivity != null) {
            Intent intent = new Intent(this, targetActivity);
            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
            intent.putExtras(intent);
            resultLauncher.launch(intent);
            _pushMessage = null;
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
                startActivityBottomMenu(TuitionActivity.class);
                break;

            case R.id.btn_teacher:
                if (teacherCnt > 0) startActivityBottomMenu(TeacherInfoActivity.class);
                else Toast.makeText(mContext, R.string.main_tv_teacher_empty, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startActivityBottomMenu(Class<?> cls){
        Intent targetIntent = new Intent(mContext, cls);

//        if (cls == MenuNoticeActivity.class){
//            targetIntent.putExtra(IntentParams.PARAM_TYPE_NOTICE, MSG_TYPE_ATTEND);
//            startActivity(targetIntent);
//
//        }else if (cls == TeacherInfoActivity.class){
//            startActivity(targetIntent);
//        }
        startActivity(targetIntent);
    }

    private void initMenusMember() {
        if(mList != null) mList.clear();

        if (_userType.equals(Constants.MEMBER)) { // 회원
            //원생정보
            //mList.add(new MainMenuItemData(R.drawable.icon_menu_student, R.string.main_menu_student_info, MenuStudentInfoActivity.class));
            //공지사항
            mList.add(new MainMenuItemData(DataManager.BOARD_NOTICE, R.drawable.icon_menu_attention, R.string.main_menu_announcement, MenuAnnouncementActivity.class));
            //캠퍼스일정
            mList.add(new MainMenuItemData(DataManager.BOARD_SCHEDULE, R.drawable.icon_menu_schedule, R.string.main_menu_campus_schedule, MenuScheduleActivity.class));
            //알림장
            mList.add(new MainMenuItemData(DataManager.BOARD_SYSTEM_NOTICE, R.drawable.icon_menu_notify, R.string.main_menu_notice, MenuNoticeActivity.class));
            //테스트예약
            mList.add(new MainMenuItemData(DataManager.BOARD_LEVELTEST, R.drawable.icon_menu_test_reserve, R.string.main_menu_test_reserve, MenuTestReserveActivity.class));
            //차량정보
            mList.add(new MainMenuItemData(DataManager.BOARD_BUS, R.drawable.icon_menu_bus, R.string.main_menu_bus_info, MenuBusActivity.class));
            //설명회예약
            mList.add(new MainMenuItemData(DataManager.BOARD_PT, R.drawable.icon_menu_briefing, R.string.main_menu_briefing_reserve, MenuBriefingActivity.class));
            //출석부
            mList.add(new MainMenuItemData(DataManager.BOARD_ATTENDANCE, R.drawable.icon_menu_rollbook, R.string.main_menu_attendance, MenuAttendanceActivity.class));
            //성적표
            mList.add(new MainMenuItemData(DataManager.BOARD_REPORT, R.drawable.icon_menu_notice, R.string.main_menu_report_card, MenuReportCardActivity.class));
            //QnA
            mList.add(new MainMenuItemData(DataManager.BOARD_QNA, R.drawable.icon_menu_question, R.string.main_menu_qna, MenuQNAActivity.class));
        }else{ // 비회원
            //공지사항
            mList.add(new MainMenuItemData(DataManager.BOARD_NOTICE, R.drawable.icon_menu_attention, R.string.main_menu_announcement, MenuAnnouncementActivity.class));
            //캠퍼스일정
            mList.add(new MainMenuItemData(DataManager.BOARD_SCHEDULE, R.drawable.icon_menu_schedule, R.string.main_menu_campus_schedule, MenuScheduleActivity.class));
            //테스트예약
            mList.add(new MainMenuItemData(DataManager.BOARD_LEVELTEST, R.drawable.icon_menu_test_reserve, R.string.main_menu_test_reserve, MenuTestReserveActivity.class));
            //설명회예약
            mList.add(new MainMenuItemData(DataManager.BOARD_PT, R.drawable.icon_menu_briefing, R.string.main_menu_briefing_reserve, MenuBriefingActivity.class));
            //QnA
            mList.add(new MainMenuItemData(DataManager.BOARD_QNA, R.drawable.icon_menu_question, R.string.main_menu_qna, MenuQNAActivity.class));
        }
    }
    private void updateMenus() {
        if(mList == null) return;
        mList.stream().forEach(menu -> {
            BoardAttributeData boardData = DataManager.getInstance().getBoardInfo(menu.getType());
            if(boardData != null) {
                if(!boardData.boardNm.equals(mContext.getString(menu.getTitleRes()))) {
                    menu.setTitle(boardData.boardNm);
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }
    // 캠퍼스 목록 조회
    private void requestACAList(){
//        showProgressDialog();
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

                    mHandler.sendEmptyMessage(CMD_GET_LOCAL_ACALIST);
                }

                @Override
                public void onFailure(Call<GetACAListResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestACAList() Exception : ", e.getMessage()); }

                    mHandler.sendEmptyMessage(CMD_GET_LOCAL_ACALIST);
                }
            });
        }
    }
    private void requestLocalACAList(){
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.getLocalACAList().enqueue(new Callback<GetACAListResponse>() {
                @Override
                public void onResponse(Call<GetACAListResponse> call, Response<GetACAListResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<ACAData> list = response.body().data;
                            DataManager.getInstance().initLocalACAListMap(list);
                        }
                    } else {

                        try {
                            LogMgr.e(TAG, "requestLocalACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }
                    mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
                    mHandler.sendEmptyMessage(CMD_GET_MEMBER_INFO);
                }

                @Override
                public void onFailure(Call<GetACAListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestLocalACAList() onFailure >> " + t.getMessage());
                    mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
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
    private void requestMemberInfo(int stuSeq, int stCode, boolean setAca){
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

                                if (stCode != 0){
                                    PreferenceUtil.setStuGender(mContext, getData.gender);
                                    PreferenceUtil.setStuBirth(mContext, getData.birth);

                                    if (setAca) {
                                        if (getData.acaName != null) { // 캠퍼스명
                                            PreferenceUtil.setAcaName(mContext, getData.acaName);
                                            mTvStudentCampus.setText(getData.acaName);
                                        }
                                        if (getData.acaCode != null) {
                                            acaCode = getData.acaCode;
                                            PreferenceUtil.setAcaCode(mContext, getData.acaCode);
                                        }
                                    }

    //                            if(!TextUtils.isEmpty(res.appAcaCode)) {
                                        PreferenceUtil.setAppAcaCode(mContext, getData.appAcaCode);
    //                            }
    //                            if(!TextUtils.isEmpty(res.appAcaName)) {
                                        PreferenceUtil.setAppAcaName(mContext, getData.appAcaName);
                                    List<ACAData> item = DataManager.getInstance().getACAList();
                                    for (ACAData data : item){
                                        if (acaCode.equals(data.acaCode)) {
                                            PreferenceUtil.setAcaTel(mContext, data.acaTel);
                                        }
                                    }

                                    if (getData.scName.equals("")) mTvSchoolAndGradeName.setText(getData.stGrade);
                                    else if (getData.stGrade.equals("")) mTvSchoolAndGradeName.setText(getData.scName);
                                    else mTvSchoolAndGradeName.setText(getData.scName + " " + getData.stGrade); // 학교, 학년
                                }

                                if (_userType.equals(Constants.MEMBER)){ // 회원

                                    if (_userGubun == Constants.USER_TYPE_PARENTS){
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
                                        PreferenceUtil.setParentPhoneNum(mContext, getData.parentPhoneNumber);
                                        PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                        PreferenceUtil.setStName(mContext, getData.name);
                                        PreferenceUtil.setParentName(mContext, "");

                                        mTvStudentName.setText(getData.name); // 원생 오리지널 이름
                                    }
                                }else{ // 비회원
                                    if (_userGubun == Constants.USER_TYPE_PARENTS){
                                        if (stCode == 0) {
                                            PreferenceUtil.setParentName(mContext, getData.name);
                                            PreferenceUtil.setParentPhoneNum(mContext, getData.phoneNumber);
                                            mTvStudentName.setText(Utils.getStr(getData.name));

                                        }else{
                                            PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                            PreferenceUtil.setStName(mContext, getData.name);

                                            if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용
                                                if (_stName != null) mTvStudentName.setText(_stName); // 자녀선택화면의 이름
                                            }else mTvStudentName.setText(Utils.getStr(getData.name)); // 원생 오리지널 이름
                                        }

                                    }else{
                                        PreferenceUtil.setParentPhoneNum(mContext, getData.parentPhoneNumber);
                                        PreferenceUtil.setStuPhoneNum(mContext, getData.phoneNumber);
                                        PreferenceUtil.setStName(mContext, getData.name);
                                        PreferenceUtil.setParentName(mContext, "");

                                        if (getData.name.equals("") || getData.name.equals("null") || getData.name == null){ // 이름이 없다면 자녀선택화면의 이름 사용
                                            if (_stName != null) mTvStudentName.setText(_stName); // 자녀선택화면의 이름
                                        }else mTvStudentName.setText(Utils.getStr(getData.name)); // 원생 오리지널 이름
                                    }
                                }

                                mTvAttendance.setText(R.string.main_null_attendance); // 출결 현황 text

                            }

                        }else{
                            LogMgr.e(TAG, "requestMemberInfo() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    //mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
                }

                @Override
                public void onFailure(Call<StudentInfoResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestMemberInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestMemberInfo() Exception : ", e.getMessage()); }

                    //mHandler.sendEmptyMessage(CMD_GET_NOTIFY_INFO);
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
                            updateMenus();
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
    private void requestBoardList(String acaCode, String appgubunCode) {
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getAnnouncementList(0, acaCode, appgubunCode).enqueue(new Callback<AnnouncementListResponse>() {
                @Override
                public void onResponse(Call<AnnouncementListResponse> call, Response<AnnouncementListResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            List<AnnouncementData> getData = new ArrayList<>();

                            if (announceList.size() > 0) announceList.clear();

                            if (response.body() != null) {
                                getData = response.body().data;
                                if (getData != null && !getData.isEmpty()) {
                                    announceList.addAll(getData.stream().limit(3).collect(Collectors.toList()));
                                } else {
                                    LogMgr.e(TAG, "ListData is null");
                                }
                            }
                        }

                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestBoardList() Exception: ", e.getMessage());
                    }
                    mTvListEmpty.setVisibility(announceList.isEmpty() ? View.VISIBLE : View.GONE);
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
                            if (getData != null && getData.data != null){
                                DataManager.getInstance().initClsListMap(getData.data);
                                String str = "";
                                if (getData.data.size() > 0) str = getData.data.get(0).sfName;
                                teacherCnt = getData.data.size();

                                if (teacherCnt <= 1) mTvTeacherName.setText(TextUtils.isEmpty(str) ? "" : str+" 선생님");
                                else mTvTeacherName.setText(TextUtils.isEmpty(str) ? "" : str+" 외 선생님");
                            }

                            LogMgr.e(TAG, "TeacherCnt: " + teacherCnt);

                        }else{
                            LogMgr.e(TAG, "requestTeacherCls() errBody : " + response.errorBody().string());
                        }

                    }catch (Exception e){ LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }
//                    hideProgressDialog();
                    mHandler.sendEmptyMessage(CMD_GET_LTC_SUBJECT);
                }

                @Override
                public void onFailure(Call<TeacherClsResponse> call, Throwable t) {
                    try { LogMgr.e(TAG, "requestTeacherCls() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestTeacherCls() Exception : ", e.getMessage()); }
//                    hideProgressDialog();
                    mHandler.sendEmptyMessage(CMD_GET_LTC_SUBJECT);
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