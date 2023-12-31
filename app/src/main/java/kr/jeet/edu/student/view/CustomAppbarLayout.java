package kr.jeet.edu.student.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.BaseActivity;
import kr.jeet.edu.student.activity.MainActivity;

public class CustomAppbarLayout extends AppBarLayout {
    private Context _context;
    private MaterialToolbar _toolbar;
    private ConstraintLayout _layoutBtnBack;
    private ImageView _ivLogo, _btnBack;
    private TextView _tvBack;
    Animation animSpin;
    private boolean clickEvent = false;
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
        _toolbar.setTitle("");
        _ivLogo = view.findViewById(R.id.iv_logo);
        _layoutBtnBack = view.findViewById(R.id.layout_btn_back);
        _btnBack = view.findViewById(R.id.btn_back);
        _tvBack = view.findViewById(R.id.tv_back_sub);
        animSpin = AnimationUtils.loadAnimation(_context, R.anim.anim_spin);
        animSpin.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(_context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                _context.startActivity(intent);
                ((BaseActivity)_context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                ((BaseActivity) _context).finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    public MaterialToolbar getToolbar() {
        return _toolbar;
    }
    public void setLogoClickable(boolean flag) {
        if(flag) {
            if(_ivLogo != null) {
                _ivLogo.setBackgroundResource(R.drawable.bt_go_home);
                _ivLogo.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!clickEvent) {
                            if(_ivLogo != null && animSpin != null) {
                                _ivLogo.startAnimation(animSpin);
                                clickEvent = true;
                            }
                        }
                    }
                });
            }
        }else{
            _ivLogo.setOnClickListener(null);
        }
    }

    public void setLogoVisible(boolean flag) {
        if(flag) {
//            _toolbar.setTitle("");
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
