package kr.jeet.edu.student.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.jeet.edu.student.R;

public class Confirm2LeaveDialog extends Dialog {
    private Context context;
    private TextView tvTitle, tvDescription;
    private EditText editTextContent;
    private Button cancelBtn, okBtn;
    //checkbox

    private ViewGroup titleLayout;
    public Confirm2LeaveDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public Confirm2LeaveDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    protected Confirm2LeaveDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        initView();
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_confirm_2_leave);

        // 버튼 테두리 라운드 적용
        findViewById(R.id.dialog_ly).setClipToOutline(true);

        titleLayout = findViewById(R.id.title_ly);
        tvTitle = (TextView) findViewById(R.id.title);
        tvDescription = findViewById(R.id.tv_description);
        String appName = context.getString(R.string.app_name);
        tvDescription.setText(context.getString(R.string.description_leave, appName));
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        okBtn = (Button) findViewById(R.id.okBtn);
        editTextContent = (EditText) findViewById(R.id.edit_agree);
        editTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence)) {
                    String str = charSequence.toString();
                    if(str.equals(context.getString(R.string.msg_agree))) {
                        okBtn.setEnabled(true);
                        return;
                    }
                }
                okBtn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setTitle(String str) {
        if(!TextUtils.isEmpty(str)) {
            tvTitle.setText(str);
            titleLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.GONE);
        }
    }

    public void setEditText(String content) {
//        if(editText.getVisibility() == View.VISIBLE) {
        editTextContent.setText(content);
        editTextContent.requestFocus();
//        }
    }

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


    @Override
    public void dismiss() {
        super.dismiss();
    }
}