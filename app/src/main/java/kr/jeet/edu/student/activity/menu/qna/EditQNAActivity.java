package kr.jeet.edu.student.activity.menu.qna;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.QnaData;
import kr.jeet.edu.student.model.data.QnaDetailData;
import kr.jeet.edu.student.model.data.StudentGradeData;
import kr.jeet.edu.student.model.request.QnaAddRequest;
import kr.jeet.edu.student.model.request.QnaEditRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.StudentGradeListResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditQNAActivity extends BaseActivity {

    private static final String TAG = "editQnA";
    private static final int CMD_GET_ACA_LIST = 0;
    private static final int CMD_GET_GRADE_LIST = 1;
    //views
    PowerSpinnerView spinnerCampus, spinnerGrade;
    NestedScrollView scrollView;
    EditText etSubject, etContent;
    LinearLayout layoutPrivate;
    CheckBox cbIsPrivate;
    LinearLayoutCompat layoutBottom;

    List<ACAData> _ACAList = new ArrayList<>();
    List<String> _ACANameList = new ArrayList<>();
    ACAData selectedACA = null;
    List<StudentGradeData> _GradeList = new ArrayList<>();
    StudentGradeData selectedGrade = null;

    QnaDetailData _currentData = new QnaDetailData();

    String _acaCode = "";
    String _gubunCode = "";
    int _userGubun = 1;
    int _memberSeq = 0;
    String _stName = "";
    int _stCode = 0;

    int _boardSeq = 0;

    private final String IS_OPEN = "Y";
    private final String IS_PRIVATE = "N";

    Constants.BoardEditMode boardEditMode = Constants.BoardEditMode.New;

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case CMD_GET_ACA_LIST:
                    break;
                case CMD_GET_GRADE_LIST:
                    if (_GradeList != null) {
                        if(boardEditMode == Constants.BoardEditMode.New) {
                            if (spinnerGrade != null) spinnerGrade.setEnabled(true);
                        }
                        Utils.updateSpinnerList(spinnerGrade, _GradeList.stream().map(t -> t.gubunName).collect(Collectors.toList()));
                    }
                    if(boardEditMode == Constants.BoardEditMode.Edit){
                        LogMgr.w(TAG, "boardEditMode is Edit");
//                        if (selectedGrade == null) {
                        try {
                            LogMgr.w(TAG, "_currentData.acaGubunCode = " + _currentData.acaGubunName);
                            Optional option = _GradeList.stream().filter(t -> String.valueOf(t.gubunCode).equals(_currentData.acaGubunCode)).findFirst();
                            if (option.isPresent()) {
                                selectedGrade = (StudentGradeData) option.get();
                            }
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        if (selectedGrade != null) {
                            LogMgr.w(TAG, "selectedGrade is not null");
                            int selectedIndex = _GradeList.indexOf(selectedGrade);
                            LogMgr.w(TAG, "selectedGrade = " + selectedGrade.gubunName + " / index  = " + selectedIndex);
                            spinnerGrade.selectItemByIndex(selectedIndex);
                        }else{
                            LogMgr.w(TAG, "selectedGrade is null");
                        }

//                        }
                    }else{
                        if(!TextUtils.isEmpty(_gubunCode)) {
                            try {
                                LogMgr.w(TAG, "_currentData.acaGubunCode = " + _gubunCode);
                                Optional option = _GradeList.stream().filter(t -> String.valueOf(t.gubunCode).equals(_gubunCode)).findFirst();
                                if (option.isPresent()) {
                                    selectedGrade = (StudentGradeData) option.get();
                                }
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }

                            if (selectedGrade != null) {
                                LogMgr.w(TAG, "selectedGrade is not null");
                                int selectedIndex = _GradeList.indexOf(selectedGrade);
                                LogMgr.w(TAG, "selectedGrade = " + selectedGrade.gubunName + " / index  = " + selectedIndex);
                                spinnerGrade.selectItemByIndex(selectedIndex);
                                _gubunCode = "";
                            }else{
                                LogMgr.w(TAG, "selectedGrade is null");
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_qna);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(this);
        _memberSeq = PreferenceUtil.getUserSeq(this);
        _stName = PreferenceUtil.getStName(this);
        _stCode = PreferenceUtil.getUserSTCode(this);

        initIntentData();
        initView();
        initAppbar();
        initData();
    }

    private void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        if(boardEditMode == Constants.BoardEditMode.New) {
            customAppbar.setTitle(R.string.menu_item_add);
        }else {
            customAppbar.setTitle(R.string.menu_item_edit);
        }
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if (intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
                boardEditMode = Constants.BoardEditMode.Edit;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, QnaDetailData.class);
                } else {
                    _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
                }
                if (_currentData != null) {
                    _boardSeq = _currentData.seq;
                }

            } else {
                boardEditMode = Constants.BoardEditMode.New;
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_ACACODE)) {
                _acaCode = intent.getStringExtra(IntentParams.PARAM_STU_ACACODE);
            }
            if(intent.hasExtra(IntentParams.PARAM_STU_GRADECODE)) {
                _gubunCode = intent.getStringExtra(IntentParams.PARAM_STU_GRADECODE);
            }
        }
    }

    private void initView() {

        layoutBottom = findViewById(R.id.layout_bottom);
        layoutPrivate = findViewById(R.id.layout_allow_private);
        etSubject = findViewById(R.id.et_subject);
        etContent = findViewById(R.id.et_content);
        scrollView = findViewById(R.id.scrollview);
        spinnerCampus = findViewById(R.id.spinner_campus);
        spinnerGrade = findViewById(R.id.spinner_grade);
        cbIsPrivate = findViewById(R.id.check_private);

        layoutPrivate.setOnClickListener(this);

        setSpinnerCampus(); // 캠퍼스
        setSpinnerGrade(); // 캠퍼스 구분
    }

    private void setSpinnerCampus() {
        _ACAList.addAll(DataManager.getInstance().getLocalACAListMap().values());
        if (_ACAList != null) _ACANameList = _ACAList.stream().map(t -> t.acaName).collect(Collectors.toList());

        Utils.updateSpinnerList(spinnerCampus, _ACANameList);

        spinnerCampus.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            ACAData selectedData = null;
            Optional<ACAData> optional = _ACAList.stream().filter(t -> t.acaName.equals(newItem)).findFirst();
            if (optional.isPresent()) {
                selectedData = (ACAData) optional.get();
            }
            selectedACA = selectedData;
            LogMgr.w("selectedACA = " + selectedACA.acaCode + " / " + selectedACA.acaName);
            if (boardEditMode == Constants.BoardEditMode.New) {
                requestGradeList(selectedACA.acaCode);
                if (selectedGrade != null) selectedGrade = null;
                if (spinnerGrade != null) spinnerGrade.clearSelectedItem();
            } else {
                requestGradeList(selectedACA.acaCode);
            }
        });
        spinnerCampus.setSpinnerOutsideTouchListener((view, motionEvent) -> spinnerGrade.dismiss());
        spinnerCampus.setLifecycleOwner(this);
    }

    private void setSpinnerGrade() {
        spinnerGrade.setOnSpinnerItemSelectedListener((oldIndex, oldItem, newIndex, newItem) -> {
            LogMgr.e(newItem + " selected");
            StudentGradeData selectedData = null;
            Optional<StudentGradeData> optional = _GradeList.stream().filter(t -> t.gubunName.equals(newItem)).findFirst();
            if (optional.isPresent()) selectedData = (StudentGradeData) optional.get();

            selectedGrade = selectedData;
        });
        spinnerGrade.setSpinnerOutsideTouchListener((view, motionEvent) -> spinnerGrade.dismiss());
        spinnerGrade.setLifecycleOwner(this);
        spinnerGrade.setEnabled(false);
    }

    private void initData() {
        if(boardEditMode == Constants.BoardEditMode.New){
            layoutBottom.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(_acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_acaCode)).findFirst();
                if (option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }
                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            }
        }else {
            layoutBottom.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(_currentData.acaCode)) {

                Optional option = _ACAList.stream().filter(t -> t.acaCode.equals(_currentData.acaCode)).findFirst();
                if (option.isPresent()) {
                    selectedACA = (ACAData) option.get();
                }

                if (selectedACA != null) {
                    int selectedIndex = _ACAList.indexOf(selectedACA);
                    spinnerCampus.selectItemByIndex(selectedIndex);
                }

            } else {
                //전체선택
//            spinner.selectItemByIndex(0);
            }
        }
        if(boardEditMode == Constants.BoardEditMode.New) { // 작성
            spinnerCampus.setEnabled(true);

        }else if(boardEditMode == Constants.BoardEditMode.Edit){ // 수정
            // 제목
            if(!TextUtils.isEmpty(_currentData.title)) etSubject.setText(_currentData.title);
            // 내용
            if(!TextUtils.isEmpty(_currentData.content)) etContent.setText(_currentData.content);
            // 공개여부
            if (!TextUtils.isEmpty(_currentData.isOpen)){
                if (_currentData.isOpen.equals(IS_PRIVATE)) cbIsPrivate.setChecked(true);
                else cbIsPrivate.setChecked(false);
            }
            spinnerCampus.setEnabled(false);
            spinnerGrade.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.layout_allow_private:
                cbIsPrivate.setChecked(!cbIsPrivate.isChecked());
                break;
        }
    }
    private boolean checkForUpdate() {
        if(selectedACA == null) {   //캠퍼스 선택
            Toast.makeText(mContext, R.string.error_message_unselected_campus, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(selectedGrade == null) {   //등급 선택
            Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(etSubject.getText())) {   //제목
            showKeyboard(etSubject);
            Toast.makeText(mContext, R.string.error_message_empty_subject, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(etContent.getText())) {   //내용
            showKeyboard(etContent);
            Toast.makeText(mContext, R.string.error_message_empty_content, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 캠퍼스 구분
    private void requestGradeList(String acaCode){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getStudentGradeList(acaCode).enqueue(new Callback<StudentGradeListResponse>() {
                @Override
                public void onResponse(Call<StudentGradeListResponse> call, Response<StudentGradeListResponse> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {
                            List<StudentGradeData> list = response.body().data;
                            if(_GradeList != null) _GradeList.clear();
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
                            LogMgr.e(TAG, "requestACAList() errBody : " + response.errorBody().string());
                        } catch (IOException e) {
                        }

                    }

                }

                @Override
                public void onFailure(Call<StudentGradeListResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestACAList() onFailure >> " + t.getMessage());
//                    _handler.sendEmptyMessage(CMD_GET_GRADE_LIST);
                }
            });
        }
    }

    // 글 작성 or 수정
    private void requestQna() {
        Call<BaseResponse> call = null;

        showProgressDialog();

        if(boardEditMode == Constants.BoardEditMode.New) call = setRequestAddData();
        else if(boardEditMode == Constants.BoardEditMode.Edit) call = setRequestEditData();

        if (call != null) {
            call.enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {

                            Intent intent = new Intent();

                            if(boardEditMode == Constants.BoardEditMode.New) {
                                Utils.createNotification(mContext, "[지트에듀케이션]", getString(R.string.qna_insert_success));
                                intent.putExtra(IntentParams.PARAM_BOARD_ADDED, true);

                            }
                            else if(boardEditMode == Constants.BoardEditMode.Edit) {
                                //Utils.createNotification(mContext, "[지트에듀케이션]", getString(R.string.qna_update_success));
                                intent.putExtra(IntentParams.PARAM_BOARD_EDITED, true);
                            }

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                    hideProgressDialog();
                }
                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                    try { LogMgr.e(TAG, "requestQna() onFailure >> " + t.getMessage()); }
                    catch (Exception e) { LogMgr.e(TAG + "requestQna() Exception : ", e.getMessage()); }

                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }
    }

    // Q&A 글 작성
    private Call<BaseResponse> setRequestAddData() {

        QnaAddRequest qnaInsert = new QnaAddRequest();

        String isOpen = "";

        if (cbIsPrivate.isChecked()) isOpen = IS_PRIVATE;
        else isOpen = IS_OPEN;

        qnaInsert.writerSeq = _memberSeq;
        qnaInsert.writerNm = _stName; // 작성자는 부모여도 원생명으로
        qnaInsert.userGubun = _userGubun;
        qnaInsert.stCode = _stCode;

        // 캠퍼스
        qnaInsert.acaCode = selectedACA.acaCode;
        qnaInsert.acaName = selectedACA.acaName;

        // 캠퍼스구분
        qnaInsert.acaGubunCode = selectedGrade.gubunCode;
        qnaInsert.acaGubunName = selectedGrade.gubunName;

        qnaInsert.title = etSubject.getText().toString(); // 제목
        qnaInsert.content = etContent.getText().toString(); // 내용
        qnaInsert.isOpen = isOpen; // 공개여부 [Y/N]

        LogMgr.e(TAG, "== requestQna ADD Data ==" +
                "\nwriterSeq: " + qnaInsert.writerSeq +
                "\nwriterNm: " + qnaInsert.writerNm +
                "\nuserGubun: " + qnaInsert.userGubun +
                "\nstCode: " + qnaInsert.stCode +
                "\nacaCode: " + qnaInsert.acaCode +
                "\nacaName: " + qnaInsert.acaName +
                "\nacaGubunCode: " + qnaInsert.acaGubunCode +
                "\nacaGubunName: " + qnaInsert.acaGubunName +
                "\ntitle: " + qnaInsert.title +
                "\ncontent: " + qnaInsert.content +
                "\nisOpen: " + qnaInsert.isOpen
        );

        return RetrofitClient.getApiInterface().insertQna(qnaInsert);
    }

    // Q&A 글 수정
    private Call<BaseResponse> setRequestEditData() {

        QnaEditRequest qnaUpdate = new QnaEditRequest();

        String isOpen = "";

        if (cbIsPrivate.isChecked()) isOpen = IS_PRIVATE;
        else isOpen = IS_OPEN;

        qnaUpdate.seq = _boardSeq;
        qnaUpdate.userGubun = _userGubun;
        qnaUpdate.title = etSubject.getText().toString();
        qnaUpdate.content = etContent.getText().toString();
        qnaUpdate.isOpen = isOpen;

        LogMgr.e(TAG, "== requestQna Edit Data  ==" +
                "\nseq: " + qnaUpdate.seq +
                "\nuserGubun: " + qnaUpdate.userGubun +
                "\ntitle: " + qnaUpdate.title +
                "\ncontent: " + qnaUpdate.content +
                "\nisOpen: " + qnaUpdate.isOpen
        );

        return RetrofitClient.getApiInterface().updateQna(qnaUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        int positionOfMenuItem = 0;
        try {
            MenuItem item = menu.getItem(positionOfMenuItem);
            SpannableString span = new SpannableString(item.getTitle());
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), 0, span.length(), 0);
            item.setTitle(span);
        }catch(Exception ex){}
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_complete:
                if(checkForUpdate()){
                    requestQna();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}