package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ACA_SCHEDULE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_PT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_REPORT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_SYSTEM;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_TUITION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.adapter.SelectStudentListAdapter;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.dialog.PushPopupDialog;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.HttpUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectStudentActivity extends BaseActivity {

    private String TAG = SelectStudentActivity.class.getSimpleName();

    private TextView tvEmpty;

    private RetrofitApi mRetrofitApi;

    private RecyclerView mListView;
    private SelectStudentListAdapter mAdapter;

    private int _parentSeq = 0;
    private PushMessage _pushMessage;
    private ArrayList<ChildStudentInfo> mList = new ArrayList<>();
    boolean doubleBackToExitPressedOnce = false;

    private int _childCnt = 0;
    boolean fromMain = false;
    private final int TWO_PEOPLE = 2;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w(TAG, "result = " + result);
        Intent intent = result.getData();
        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent != null && intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD)) { // 신규원생을 추가했을 경우
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_TEST_NEW_CHILD, false);

                if (intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD_FROM_MAIN)) {
                    fromMain = intent.getBooleanExtra(IntentParams.PARAM_TEST_NEW_CHILD_FROM_MAIN, false);
                }

                if (added) requestChildStudentInfo(_parentSeq);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);
        mContext = this;
        initView();
    }

    private void initData(){
        initAppbar();
        try {
            _parentSeq = PreferenceUtil.getUserSeq(mContext);
            _childCnt = PreferenceUtil.getNumberOfChild(mContext);

            Intent intent = getIntent();
            if (intent != null) {

                if (intent.hasExtra(IntentParams.PARAM_CHILD_STUDENT_INFO)) {

                    ArrayList<ChildStudentInfo> childStudentInfoList;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        childStudentInfoList = intent.getParcelableArrayListExtra(IntentParams.PARAM_CHILD_STUDENT_INFO, ChildStudentInfo.class);
                    } else {
                        childStudentInfoList = intent.getParcelableArrayListExtra(IntentParams.PARAM_CHILD_STUDENT_INFO);
                    }

                    if (childStudentInfoList != null) {
                        mList = childStudentInfoList;
                        if (mList.size() == 0) mList.add(0, new ChildStudentInfo());
                        else mList.add(mList.size(), new ChildStudentInfo());

                    } else {
                        if (mList.size() == 0) mList.add(0, new ChildStudentInfo());
                    }

                    setBackBtn();

                    LogMgr.e("intent extra");
                } else {
                    LogMgr.e("No intent extra");
                    requestChildStudentInfo(_parentSeq);
                }

                if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                    LogMgr.e(TAG, "push msg ");
                    _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                    if (_pushMessage != null) LogMgr.e(TAG, "msg = " + _pushMessage.body);


                } else {
                    LogMgr.e(TAG, "push msg is null");
                }
            }
        } catch (Exception e) {
            LogMgr.e(TAG + " Exception: ", e.getMessage());
        }

        if(_pushMessage != null) {

            // TODO : seq , stCode에 따라 다른계정의 push는 보여주지 않게 하기
            if (_pushMessage.memberSeq != _parentSeq) return;

            switch(_pushMessage.pushType) {
                case MSG_TYPE_ATTEND: // 출결상태
                {
                    if (_childCnt >= TWO_PEOPLE){
                        PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
                        pushPopupDialog.setOnOkButtonClickListener(view -> {
                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {
                                pushPopupDialog.getFCMManager().requestPushConfirmToServer(_pushMessage, _pushMessage.stCode);
                            }
                            pushPopupDialog.dismiss();
                        });
                        pushPopupDialog.show();
                    }
                }
                    break;
                case MSG_TYPE_NOTICE: // 공지사항
                {
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(MenuBoardDetailActivity.class);
                }
                    break;
                case MSG_TYPE_SYSTEM: // 시스템알림
                {
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(MenuBoardDetailActivity.class);
                }
                    break;
                case MSG_TYPE_PT: // 설명회예약
                {
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(MenuBriefingDetailActivity.class);
                }
                    break;
                case MSG_TYPE_ACA_SCHEDULE: // 캠퍼스일정
                {
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(MenuScheduleDetailActivity.class);
                }
                    break;
                case MSG_TYPE_REPORT: // 성적표
                {
                    LogMgr.e(TAG, "Event sel stu");
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(ReportCardDetailActivity.class);
                }
                break;
                case MSG_TYPE_TUITION: // 미납알림
                {
                    if (_childCnt >= TWO_PEOPLE) startPushActivity(TuitionActivity.class);
                }
                break;

                default:
                    break;
            }
        }
        HttpUtils.requestSchoolList();
        HttpUtils.requestLTCList();
        HttpUtils.requestLTCSubjectList();
    }

    private void startPushActivity(Class<?> targetActivity){
        Intent intent = new Intent(mContext, targetActivity);
        if(_pushMessage != null) intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);

        startActivity(intent);
        _pushMessage = null;
    }


    @Override
    public void onBackPressed() {
        LogMgr.e(TAG, "childCnt: " + _childCnt);
        if (_childCnt >= 0) {

            super.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.msg_backbutton_to_exit, Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
    CustomAppbarLayout customAppbar;
    @Override
    void initAppbar(){
        customAppbar = findViewById(R.id.customAppbar);
        if (customAppbar != null) {
            customAppbar.setLogoVisible(true);
            setSupportActionBar(customAppbar.getToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
    @Override
    void initView() {
        initData();
        tvEmpty = findViewById(R.id.tv_sel_stu_empty);

        mListView = (RecyclerView) findViewById(R.id.listView);
        mAdapter = new SelectStudentListAdapter(mContext, mList);
        mListView.setAdapter(mAdapter);
    }

    private void requestChildStudentInfo(int parentMemberSeq) {
        showProgressDialog();
        if(RetrofitClient.getInstance() != null) {
            mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.searchChildStudents(parentMemberSeq).enqueue(new Callback<SearchChildStudentsResponse>() {
                @Override
                public void onResponse(Call<SearchChildStudentsResponse> call, Response<SearchChildStudentsResponse> response) {
                    mList.clear();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            ArrayList<ChildStudentInfo> data = response.body().data;
                            if (data != null) {
                                if (data.size() > 0) {
                                    mList.addAll(data);
                                    mList.add(data.size(), new ChildStudentInfo());

                                    PreferenceUtil.setNumberOfChild(mContext, data.size());
                                } else {
                                    mList.add(0, new ChildStudentInfo());
                                    PreferenceUtil.setNumberOfChild(mContext, 0);
                                }
                            }

                        }else{
                            mList.add(0, new ChildStudentInfo());
                        }

                        setBackBtn();

                    }catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();

                    if (fromMain) {
                        if (mList.size() <= TWO_PEOPLE) goMain(0);
                    }
                }

                @Override
                public void onFailure(Call<SearchChildStudentsResponse> call, Throwable t) {
                    mList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);

                    try { LogMgr.e(TAG, "requestStudentInfo() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }

    private void setBackBtn() {
        if (mList.size() > 1) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void goMain(int position) {
        DataManager.getInstance().isSelectedChild = true;
        PreferenceUtil.setStuSeq(mContext, mList.get(position).seq);
        PreferenceUtil.setStName(mContext, mList.get(position).stName);
        PreferenceUtil.setUserSTCode(mContext, mList.get(position).stCode);
        PreferenceUtil.setStuGender(mContext, mList.get(position).gender);

        Intent intent = new Intent(mContext, MainActivity.class);
        LogMgr.e(TAG, "EVENT goMain()");
        if(_pushMessage != null) {
            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goTestForNewStu() {
        Intent intent = new Intent(mContext, InformedConsentActivity.class);
        intent.putExtra(IntentParams.PARAM_TEST_TYPE, Constants.LEVEL_TEST_TYPE_NEW_CHILD);
        resultLauncher.launch(intent);
    }
}