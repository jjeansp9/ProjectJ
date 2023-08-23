package kr.jeet.edu.student.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class PopupDialog extends Dialog {

    private Context context;
    private TextView titleTv, contentTv, noteTv, notMatchTv;
    private EditText editText;
    private Button cancelBtn, okBtn;
    private ViewGroup titleLayout;

    public PopupDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public PopupDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    protected PopupDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        initView();
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_dialog);

        // 버튼 테두리 라운드 적용
        findViewById(R.id.dialog_ly).setClipToOutline(true);

        titleLayout = findViewById(R.id.title_ly);
        titleTv = (TextView) findViewById(R.id.title);
        contentTv = (TextView) findViewById(R.id.content);
        editText = (EditText) findViewById(R.id.edit);
        noteTv = (TextView) findViewById(R.id.note);
        notMatchTv = (TextView) findViewById(R.id.not_match);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        okBtn = (Button) findViewById(R.id.okBtn);

        titleLayout.setVisibility(View.GONE);
        noteTv.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
    }

    public void setTitle(String str) {
        if(!TextUtils.isEmpty(str)) {
            titleTv.setText(str);
            titleLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.GONE);
        }
    }

    public void setNotMatchTv(boolean notMatch) {
        if(notMatch) {
            notMatchTv.setText(R.string.msg_password_mismatch);
            notMatchTv.setVisibility(View.VISIBLE);
        } else {
            notMatchTv.setVisibility(View.GONE);
        }
    }

    public void setContent(String str) {
        contentTv.setText(str);
    }

    public void setNote(String str) {
        if(!TextUtils.isEmpty(str)) {
            noteTv.setVisibility(View.VISIBLE);
            noteTv.setText(str);
        } else {
            noteTv.setVisibility(View.GONE);
        }
    }

    public void setEdit(boolean isVisible){
        if (isVisible) editText.setVisibility(View.VISIBLE);
        else editText.setVisibility(View.GONE);
    }

    private String mInputText;

    public void setOkButtonText(String str) {
        okBtn.setText(str);
    }

    public void setOnOkButtonClickListener(View.OnClickListener listener) {
        okBtn.setOnClickListener(listener);
    }

    public void setOnCancelButtonClickListener(View.OnClickListener listener) {
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setOnClickListener(listener);
    }

    public String getInputText(){
        mInputText = editText.getText().toString().trim();
        return mInputText;
    }
}
