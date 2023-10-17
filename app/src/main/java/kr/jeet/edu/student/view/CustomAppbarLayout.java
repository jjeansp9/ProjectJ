package kr.jeet.edu.student.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import kr.jeet.edu.student.R;

public class CustomAppbarLayout extends AppBarLayout {
    private Context _context;
    private MaterialToolbar _toolbar;
    private ConstraintLayout _layoutBtnBack;
    private ImageView _ivLogo, _btnBack;
    private TextView _tvBack;
    public CustomAppbarLayout(@NonNull Context context) {
        super(context);
        _context = context;
        initView();
    }

    public CustomAppbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        initView();
    }

    public CustomAppbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _context = context;
        initView();
    }
    private void initView() {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_custom_appbar, this, true);
        _toolbar = view.findViewById(R.id.toolbar);
        _ivLogo = view.findViewById(R.id.iv_logo);
        _layoutBtnBack = view.findViewById(R.id.layout_btn_back);
        _btnBack = view.findViewById(R.id.btn_back);
        _tvBack = view.findViewById(R.id.tv_back_sub);
    }
    public MaterialToolbar getToolbar() {
        return _toolbar;
    }

    public void setLogoVisible(boolean flag) {
        if(flag) {
            _toolbar.setTitle("");
            _ivLogo.setVisibility(View.VISIBLE);
        }else{
            _ivLogo.setVisibility(View.GONE);
        }
//        _toolbar.setLogo(R.drawable.img_jeet_logo);
//        _toolbar.setContentInsetStartWithNavigation(0);
//        _toolbar.setContentInsetsRelative(0, 0);
//        _toolbar.setLayoutParams(new Toolbar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));

//        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        layoutParams.gravity = Gravity.CENTER;
//        _toolbar.setLayoutParams(layoutParams);
    }



    public void setMainBtnLeftClickListener(View.OnClickListener listener) {
        _layoutBtnBack.setVisibility(View.VISIBLE);
        _layoutBtnBack.setOnClickListener(listener);
    }

    public void setTitle(int titleRes) {
        if(_toolbar != null) {
            _toolbar.setTitle(titleRes);
        }
    }
    public void setTitle(String title) {
        if(_toolbar != null) {
            _toolbar.setTitle(title);
        }
    }

}
