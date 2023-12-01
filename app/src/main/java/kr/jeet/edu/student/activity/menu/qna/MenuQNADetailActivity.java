package kr.jeet.edu.student.activity.menu.qna;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.QnaData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class MenuQNADetailActivity extends BaseActivity {
    private final String TAG = "MenuQNADetailActivity";

    private TextView mTvTitle, mTvName, mTvDate, mTvQuestion, mTvAnswer, tvReadCount;
    private RetrofitApi mRetrofitApi;

    QnaData _currentData = null;
    int _currentDataPosition = 0;

    Menu _menu;
    int _memberSeq = -1;
    int _userGubun = 1;
    private boolean isEdited = false;
    private int _currentSeq = -1;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                if(intent != null) {
                    if (intent.hasExtra(IntentParams.PARAM_BOARD_EDITED)) {
                        isEdited = intent.getBooleanExtra(IntentParams.PARAM_BOARD_EDITED, false);
                        LogMgr.e("position = " + _currentDataPosition + "_currentData seq = " + _currentData.seq);
//                    if(edited && _currentDataPosition > 0) {
//                        intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//                        setResult(result.getResultCode(), intent);
//                    }
                        if (isEdited) {
                            int boardSeq = _currentData.seq;
                            //requestBoardDetail(boardSeq);
                        }
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

        if(intent.hasExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO)) {
            LogMgr.w("param is recived");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO, QnaData.class);
            }else{
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_ANNOUNCEMENT_INFO);
            }

        }else if(intent.hasExtra(IntentParams.PARAM_PUSH_MESSAGE)) {
            PushMessage message = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE, PushMessage.class);
            }else{
                message = intent.getParcelableExtra(IntentParams.PARAM_PUSH_MESSAGE);
            }
            _currentSeq = message.connSeq;
        }

        LogMgr.w("currentData = " + _currentData);
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initView() {
        initData();
        mTvTitle = findViewById(R.id.tv_qna_title);
        mTvName = findViewById(R.id.tv_writer_name);
        mTvDate = findViewById(R.id.tv_board_detail_write_date);
        tvReadCount = findViewById(R.id.tv_rd_cnt);
        mTvQuestion = findViewById(R.id.tv_question);
        mTvAnswer = findViewById(R.id.tv_answer);

//        initData();
        if (_currentData != null) {
            //requestBoardDetail(_currentData.seq);   //조회수 때문에 detail 을 호출해야만 함...
        }else if(_currentSeq != -1) {
            //requestBoardDetail(_currentSeq);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if(_currentData != null) {
//            if ((_userGubun == Constants.USER_TYPE_ADMIN && _currentData.memberResponseVO.seq == _seq)
//                    || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
//                getMenuInflater().inflate(R.menu.menu_edit_level_test, menu);
//                this._menu = menu;
//            }
//        }
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
                            hideMessageDialog();
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

    @Override
    public void onBackPressed() {
        LogMgr.d(TAG,"onBackPressed edit? " + isEdited);
//        if(isEdited) {
//            Intent intent = new Intent();
//            intent.putExtra(IntentParams.PARAM_BOARD_EDITED, isEdited);
//            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
//            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//            setResult(RESULT_OK, intent);
//            finish();
//        }else {
//            Intent intent = new Intent();
//            intent.putExtra(IntentParams.PARAM_BOARD_ITEM, _currentData);
//            intent.putExtra(IntentParams.PARAM_BOARD_POSITION, _currentDataPosition);
//            setResult(RESULT_CANCELED, intent);
//            finish();
//        }
        overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
    }
}