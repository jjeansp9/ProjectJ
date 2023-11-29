package kr.jeet.edu.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.utils.MyWebChromeClient;
import kr.jeet.edu.student.utils.MyWebViewClient;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class PrivacySeeContentActivity extends BaseActivity {

    private final static String TAG = "privacySeeContent Activity";

    private NestedScrollView mScrollView;
    private TextView mTvContent;
    private String title = "";
    private String url = "";
    private WebView wv;

    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_see_content);
        mContext = this;
        mActivity = this;
        initData();
        initView();
        initAppbar();
    }

    private void initData(){
        try {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)){
                title = intent.getStringExtra(IntentParams.PARAM_APPBAR_TITLE);
            }
            if (intent.hasExtra(IntentParams.PARAM_WEB_VIEW_URL)){
                url = intent.getStringExtra(IntentParams.PARAM_WEB_VIEW_URL);
            }
        }catch (Exception e){

        }
    }

    void initView() {
        mScrollView = findViewById(R.id.scroll_privacy_content);
        mTvContent = findViewById(R.id.tv_privacy_content);
        findViewById(R.id.btn_pvy_consent_confirm).setOnClickListener(this);

        if (!TextUtils.isEmpty(url)){
            mScrollView.setVisibility(View.GONE);
            wv = findViewById(R.id.wv_privacy_content);

            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            wv.getSettings().setSupportZoom(true);
            wv.getSettings().setBuiltInZoomControls(true);
            wv.getSettings().setDisplayZoomControls(false);

            WebSettings webSettings = wv.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            wv.setWebViewClient(new MyWebViewClient(mActivity, wv));
            wv.setWebChromeClient(new MyWebChromeClient(mActivity));

            wv.loadUrl(url);
        }else{
            if (title.equals(getString(R.string.informed_consent_privacy_collection)) ||
            title.equals(getString(R.string.settings_private_info_agreement))){
                mTvContent.setText(R.string.privacy_collection_content);

            }else if (title.equals(getString(R.string.informed_consent_privacy_third_party))){
                mTvContent.setText(R.string.privacy_third_party_content);

            }else if (title.equals(getString(R.string.informed_consent_privacy_consignment))){
                mTvContent.setText(R.string.privacy_consigment_content);

            }else if (title.equals(getString(R.string.informed_consent_privacy_marketing))){
                mTvContent.setText(R.string.privacy_marketing_content);

            }else if (title.equals(getString(R.string.briefing_privacy))){
                mTvContent.setText(R.string.briefing_privacy_content);

            }else if (title.equals(getString(R.string.terms_agreement))){
                mTvContent.setText(R.string.pvy_terms_agree);

            }else if (title.equals(getString(R.string.terms_agreement_private_info))){
                mTvContent.setText(R.string.briefing_privacy_content);
            }
        }
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        //customAppbar.setTitle(title);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(false);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_pvy_consent_confirm:
                finish();
                break;
        }
    }
}