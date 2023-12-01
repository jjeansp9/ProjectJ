package kr.jeet.edu.student.activity.menu.qna;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.QnaData;
import kr.jeet.edu.student.model.data.QnaDetailData;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.QnaDetailResponse;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuQNADetailActivity extends BaseActivity {
    private final String TAG = "MenuQNADetailActivity";

    private LinearLayoutCompat mLayoutTag, mLayoutAnswer;
    private TextView mTvTitle, mTvName, mTvDate, mTvQuestion, mTvAnswer, tvReadCount;

    private QnaData _currentData = null;
    private QnaDetailData _detailData = null;
    private int _currentDataPosition = 0;

    private Menu _menu;
    private int _memberSeq = -1;
    private int _userGubun = 1;
    private boolean isEdited = false;
    private int _currentSeq = -1;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent != null) {
                    if(intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                        isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                        if (isEdited) {
                            requestQnaDetail(_currentSeq);
                        }

                    } else if(intent.hasExtra(IntentParams.PARAM_BOARD_DELETED)) {

                    }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_qna_detail);
        mContext = this;
        initAppbar();
        initView();
    }
    private void initData(){

        _userGubun = PreferenceUtil.getUserGubun(this);
        _memberSeq = PreferenceUtil.getUserSeq(this);

        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_BOARD_POSITION)) {
            _currentDataPosition = intent.getIntExtra(IntentParams.PARAM_BOARD_POSITION, -1);
        }

        if(intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) { // 목록 -> 상세
            LogMgr.w("param is recived");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, QnaData.class);
            }else{
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO);
            }
            if (_currentData != null) _currentSeq = _currentData.seq;

        }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) { // 푸쉬 -> 상세
            PushMessage message = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }else{
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
            }
            if (message != null) _currentSeq = message.connSeq;
        }

        LogMgr.w("currentData = " + _currentData);
    }

    private void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {
        initData();
        mLayoutTag = findViewById(R.id.layout_tag);
        mLayoutAnswer = findViewById(R.id.layout_answer);
        mTvTitle = findViewById(R.id.tv_qna_title);
        mTvName = findViewById(R.id.tv_writer_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvQuestion = findViewById(R.id.tv_question);
        mTvAnswer = findViewById(R.id.tv_answer);

//        initData();
        if (_currentData != null) {
            requestQnaDetail(_currentData.seq);   //조회수 때문에 detail 을 호출해야만 함...
        }else if(_currentSeq != -1) {
            requestQnaDetail(_currentSeq);
        }
    }

    private void setView() {
        if (!TextUtils.isEmpty(_detailData.isMain)) {
            if (_detailData.isMain.equals(Constants.QNA_STATE_NOTICE)) { // 공지 글
                addTag(R.color.color_notice, "공지");

                if (_detailData.userGubun <= Constants.USER_TYPE_TEACHER) { // 강사, 관리자의 경우
                    if (!_detailData.isOpen.equals(Constants.QNA_STATE_OPEN)) {
                        addTag(R.color.color_private, "비공개");
                    }
                    if (_detailData.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                        addTag(R.color.color_me, "본인");
                    }
                }
            } else {
                if (_detailData.userGubun >= Constants.USER_TYPE_STUDENT) {
                    if (!TextUtils.isEmpty(_detailData.state)) {
                        if (_detailData.state.equals(Constants.QNA_STATE_SUBSCRIPTION)) {
                            addTag(R.color.color_subscription, "신청");

                        } else if(_detailData.state.equals(Constants.QNA_STATE_RECEPTION)) {
                            addTag(R.color.color_receiption, "접수");

                        } else if(_detailData.state.equals(Constants.QNA_STATE_COMPLETE)) {
                            addTag(R.color.color_complete, "완료");

                        }
                    }
                }
                if (!TextUtils.isEmpty(_detailData.isOpen)) {
                    if (!_detailData.isOpen.equals(Constants.QNA_STATE_OPEN)) {
                        addTag(R.color.color_private, "비공개");
                    }
                }
                if (_detailData.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                    addTag(R.color.color_me, "본인");
                }
            }
        }

        mTvTitle.setText(Utils.getStr(_detailData.title));
        //mTvName.setText(Utils.getStr(_currentDetailData.title)); // 부모,원생의 경우 이름 안보이게
        mTvDate.setText(Utils.getStr(_detailData.insertDate));
        tvReadCount.setText(Utils.getStr(Utils.decimalFormat(_detailData.rdcnt)));
        mTvQuestion.setText(Utils.getStr(_detailData.content));

        if (TextUtils.isEmpty(_detailData.reply)) mLayoutAnswer.setVisibility(View.GONE);
        else mTvAnswer.setText(_detailData.reply);
    }

    private void addTag(int bgColor, String state) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginEndInPixels = Utils.fromDpToPx(4);

        TextView tvStatus = new TextView(mContext);
        tvStatus.setText(state);
        tvStatus.setHint("가나다라");
        tvStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_badge_default));
        ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(ContextCompat.getColor(mContext, bgColor)));
        tvStatus.setTextAppearance(R.style.QnaTagTextAppearance);
        tvStatus.setGravity(Gravity.CENTER);

        tvStatus.setPadding(6,2,6,2);

        layoutParams.rightMargin = marginEndInPixels;
        tvStatus.setLayoutParams(layoutParams);

        mLayoutTag.addView(tvStatus);
    }

    // QnA 상세조회
    private void requestQnaDetail(int boardSeq){
        if (RetrofitClient.getInstance() != null){

            showProgressDialog();

            if (RetrofitClient.getApiInterface() != null) {
                RetrofitClient.getApiInterface().getQnaDetail(boardSeq, _userGubun).enqueue(new Callback<QnaDetailResponse>() {
                    @Override
                    public void onResponse(Call<QnaDetailResponse> call, Response<QnaDetailResponse> response) {
                        try {
                            if (response.isSuccessful()){

                                if (response.body() != null) {

                                    QnaDetailData data = response.body().data;
                                    if (data != null){
                                        _detailData = data;
                                        setView();

                                    }else LogMgr.e(TAG+" requestNoticeDetail is null");
                                }
                            }else{
                                finishActivity();
                                Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            LogMgr.e(TAG + "requestNoticeDetail() Exception : ", e.getMessage());
                        }

                        hideProgressDialog();
                    }

                    @Override
                    public void onFailure(Call<QnaDetailResponse> call, Throwable t) {
                        try {
                            LogMgr.e(TAG, "requestNoticeDetail() onFailure >> " + t.getMessage());
                        }catch (Exception e){
                        }
                        hideProgressDialog();
                        finishActivity();
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void requestDeleteQna() {

        int boardSeq = 0;

        if (_currentData != null) boardSeq = _currentData.seq;
        else if(_currentSeq != -1) boardSeq = _currentSeq;

        if (RetrofitClient.getInstance() != null){

            showProgressDialog();

            if (RetrofitClient.getApiInterface() != null) {
                RetrofitClient.getApiInterface().deleteQna(boardSeq).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        try {
                            if (response.isSuccessful()){

                                // TODO : QNA Delete
                            }else{
                                //Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            LogMgr.e(TAG + "requestNoticeDetail() Exception : ", e.getMessage());
                        }

                        hideProgressDialog();
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        try {
                            LogMgr.e(TAG, "requestNoticeDetail() onFailure >> " + t.getMessage());
                        }catch (Exception e){
                        }
                        hideProgressDialog();
                        Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(_currentData != null) {
            if (_currentData.writerSeq == _memberSeq) {
                if (_currentData.state.equals(Constants.QNA_STATE_SUBSCRIPTION)) {
                    getMenuInflater().inflate(R.menu.menu_edit, menu);
                    this._menu = menu;
                }
            }
        }
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_edit:
                navigate2EditQNAActivity();
                return true;
            case R.id.action_delete:
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.board_item_confirm_delete)
                        , v -> {
                            requestDeleteQna();
                        },
                        v -> hideMessageDialog(), false);

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigate2EditQNAActivity() {
        Intent editIntent = new Intent(mContext, EditQNAActivity.class);
        editIntent.putExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, _currentData);
        editIntent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
        resultLauncher.launch(editIntent);

    }

    private void setEditData() {
        // TODO : _currentData = _detailData (수정되었을 때 글 목록 해당 position의 data만 갱신하기 위함)
        if (isEdited) {

        }
    }

    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed edit? " + isEdited);
        if(isEdited) {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            setResult(RESULT_OK, intent);

        }else {
            Intent intent = new Intent();
            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
            setResult(RESULT_CANCELED, intent);
        }
        finish();
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }

    private void finishActivity() {
        new Handler().postDelayed(() -> {
            finish();
            overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
        }, 500);
    }
}