package kr.jeet.edu.student.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.dialog.PopupDialog;
import kr.jeet.edu.student.utils.LogMgr;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public Context mContext;
    private AlertDialog mProgressDialog = null;
    private PopupDialog popupDialog = null;
    TextView txt;
    private boolean setBar = false;
    private int move = -1;
    protected long scrollToTopDelay = 0;

    /**
     *      1. 테스트예약 목록[부모앱,관리자앱] : 과목 추가 (관리자,사용자) - 현재 사용자앱은 추가 완료
     *      1-1. 테스트예약 목록[부모앱,관리자앱] : 날짜형식 ios에 맞게 수정
     *      2. 상담요청 상세[관리자앱] : ios랑 ui 맞추기
     *      3. 시스템알림 출결현황: 목록 item 클릭시 dialog 보여주기
     *      4. 시스템알림 미납: 월 변경시 목록 갱신 이슈 [api 이슈]
     *      5. 시스템알림 목록, 상세: 시간 단위 분까지
     *      6. 출석부: 캘린더, 범례 양 옆 마진주기
     *      7. 목록 갱신시 리스트 최상단으로 스크롤
     *      8. dialog 내용 길어지면 scroll
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initData();
        setStatusAndNavigatinBar(setBar);
    }
    private void initData() {
        scrollToTopDelay = getResources().getInteger(R.integer.scroll_to_top_delay);
    }
//    protected void initView();
//    protected void initAppbar();

    @Override
    public void onClick(View view) {

    }

    protected void showProgressDialog()
    {
        showProgressDialog(getString(R.string.requesting), null);
    }

    protected void showProgressDialog(String msg)
    {

        showProgressDialog(msg, null);
    }

    protected void showProgressDialog(DialogInterface.OnCancelListener listener)
    {
        showProgressDialog(getString(R.string.requesting), listener);
    }

    protected void showProgressDialog(String msg, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null){
            View view = getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(msg);

            mProgressDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
/*
        Window window = mProgressDialog.getWindow();
        if(window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(mProgressDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mProgressDialog.getWindow().setAttributes(layoutParams);
        }
        */
        runOnUiThread(() -> {
            if (txt != null && mProgressDialog != null) {
                txt.setText(msg);
                mProgressDialog.show();
            }
        });
    }

    protected void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
    protected void showMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener, boolean setEditText) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        popupDialog.setEdit(setEditText);
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }

        popupDialog.show();
    }
    protected void showMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener, boolean setEditText, boolean cancelable) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        popupDialog.setCanceledOnTouchOutside(cancelable);
        popupDialog.setEdit(setEditText);
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }

        popupDialog.show();
    }
    protected void showMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener, boolean setEditText, String setOkText, String setCancelText) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        popupDialog.setEdit(setEditText);
        popupDialog.setOkText(setOkText);
        popupDialog.setCancelText(setCancelText);
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }

        popupDialog.show();
    }
    protected void hideMessageDialog() {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switch (move){
            case Constants.MOVE_DEFAULT:
                LogMgr.e("EVENT_DETAIL");
                break;
            case Constants.MOVE_LEFT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_out);
                LogMgr.e("EVENT_LEFT");
                break;
            case Constants.MOVE_RIGHT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_exit);
                LogMgr.e("EVENT_RIGHT");
                break;
            case Constants.MOVE_UP:
                overridePendingTransition(R.anim.none, R.anim.vertical_out);
                LogMgr.e("EVENT_UP");
                break;
            case Constants.MOVE_DOWN:
                overridePendingTransition(R.anim.none, R.anim.vertical_exit);
                LogMgr.e("EVENT_DOWN");
                break;
            case Constants.MOVE_DETAIL_RIGHT:
                overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                LogMgr.e("EVENT_DETAIL_RIGHT");
                break;
        }
    }
    protected void setAnimMove(int setMove){
        move = setMove;
    }

    protected void setStatusAndNavigatinBar(boolean isNotSet){
        Window window = getWindow();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        int resColor = Color.BLACK;
        boolean lightStatusBar = false;
        boolean lightNavigationBar = false;

        if (!isNotSet){
            resColor = Color.WHITE;
            lightStatusBar = true; // 상태표시줄 [ true - black, false - white ]
            lightNavigationBar = true; // 네비게이션 [ true - black, false - white ]

            window.setNavigationBarColor(resColor);
            controller.setAppearanceLightNavigationBars(true); // navigation bar
        }

        window.setStatusBarColor(resColor);
        controller.setAppearanceLightStatusBars(lightStatusBar); // status bar
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    protected void showKeyboard(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}