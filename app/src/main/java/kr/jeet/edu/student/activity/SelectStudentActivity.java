package kr.jeet.edu.student.activity;

import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ACA_SCHEDULE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_ATTEND;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_NOTICE;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_PT;
import static kr.jeet.edu.student.fcm.FCMManager.MSG_TYPE_SYSTEM;

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
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.dialog.PushPopupDialog;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
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

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w(TAG, "result = " + result);
        Intent intent = result.getData();
        if(result.getResultCode() != RESULT_CANCELED) {
            if(intent != null && intent.hasExtra(IntentParams.PARAM_TEST_NEW_CHILD)) { // 신규원생을 추가했을 경우
                boolean added = intent.getBooleanExtra(IntentParams.PARAM_TEST_NEW_CHILD, false);
                if (added) {
                    requestChildStudentInfo(_parentSeq);
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);
        mContext = this;
        initData();
        initAppbar();
        initView();
    }

    private void initData(){
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

                    }

                    LogMgr.e("intent extra");
                } else {
                    LogMgr.e("No intent extra");
                    requestChildStudentInfo(_parentSeq);
                }

                if (intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
                    LogMgr.e(TAG, "push msg ");
                    _pushMessage = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
                    LogMgr.e(TAG, "msg = " + _pushMessage.body);
                } else {
                    LogMgr.e(TAG, "push msg is null");
                }
            }
        } catch (Exception e) {
            LogMgr.e(TAG + " Exception: ", e.getMessage());
        }

        if(_pushMessage != null) {


            switch(_pushMessage.pushType) {
                case MSG_TYPE_ATTEND:
                {
                    if (PreferenceUtil.getNumberOfChild(mContext) > 1){
                        PushPopupDialog pushPopupDialog = new PushPopupDialog(this, _pushMessage);
                        pushPopupDialog.setOnOkButtonClickListener(view -> {
                            if(!TextUtils.isEmpty(_pushMessage.pushId)) {

//                                PushConfirmRequest request = new PushConfirmRequest();
//
//                                List<String> list = new ArrayList<>();
//                                list.add(_pushMessage.pushId);

                                //pushPopupDialog.getFCMManager().requestPushConfirmToServer(_pushMessage);
                            }
                            pushPopupDialog.dismiss();
                        });
                        pushPopupDialog.show();
                    }
                }
                    break;

                case MSG_TYPE_NOTICE:
                {
                    if (_childCnt > 1) startPushActivity(MSG_TYPE_NOTICE);
                }
                    break;
                case MSG_TYPE_SYSTEM:
                {
                    if (_childCnt > 1) startPushActivity(MSG_TYPE_SYSTEM);
                }
                    break;
                case MSG_TYPE_PT:
                {
                    if (_childCnt > 1) startPushActivity(MSG_TYPE_PT);
                }
                    break;
                case MSG_TYPE_ACA_SCHEDULE:
                {
                    if (_childCnt > 1) startPushActivity(MSG_TYPE_ACA_SCHEDULE);
                }
                    break;

                default:
                    break;
            }
        }
    }

    private void startPushActivity(String pushType){
        Intent intent = new Intent(mContext, MainActivity.class);
        if (pushType.equals(MSG_TYPE_SYSTEM)){
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.push_type_system));

        } else if (pushType.equals(MSG_TYPE_NOTICE)){
            intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, getString(R.string.main_menu_announcement));
        }

        LogMgr.e(TAG, "EVENT pushActivity()");
        if(_pushMessage != null) {
            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, _pushMessage);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        _pushMessage = null;
    }


    @Override
    public void onBackPressed() {
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

    @Override
    void initAppbar(){
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setLogoVisible(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
    @Override
    void initView() {
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
                            if (data != null && data.size() > 0) {
                                mList.addAll(data);
                                mList.add(data.size(), new ChildStudentInfo());
                            } else {
                                mList.add(0, new ChildStudentInfo());
                            }
                        }else{
                            mList.add(0, new ChildStudentInfo());
                        }

                    }catch (Exception e) { LogMgr.e(TAG + "requestChildStudentInfo() Exception : ", e.getMessage()); }

                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();
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

    public void goMain(int position) {
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