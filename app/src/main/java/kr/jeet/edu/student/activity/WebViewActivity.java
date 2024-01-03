package kr.jeet.edu.student.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.MyWebChromeClient;
import kr.jeet.edu.student.utils.MyWebViewClient;
import kr.jeet.edu.student.utils.Utils;
import kr.jeet.edu.student.view.CustomAppbarLayout;

public class WebViewActivity extends BaseActivity {

    private final static String TAG = "WebView Activity";
    private String title = "";
    private WebView wv;
    private String url;
    private String accountNo = "";

    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mActivity = this;
        mContext = this;
        initView();
        initAppbar();
    }

    private void initData(){
        try {
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.hasExtra(IntentParams.PARAM_APPBAR_TITLE)){
                    title = intent.getStringExtra(IntentParams.PARAM_APPBAR_TITLE);
                }
                if (intent.hasExtra(IntentParams.PARAM_WEB_VIEW_URL)){
                    url = intent.getStringExtra(IntentParams.PARAM_WEB_VIEW_URL);
                }
                if (intent.hasExtra(IntentParams.PARAM_ACCOUNT_NO)){
                    accountNo = intent.getStringExtra(IntentParams.PARAM_ACCOUNT_NO);
                }
            }

        }catch (Exception e){

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    void initView() {
        initData();

        findViewById(R.id.btn_confirm).setOnClickListener(this);

        wv = findViewById(R.id.webview);

        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);

        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);   //html 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정되도록 설정
        webSettings.setUseWideViewPort(true);        //html viewport 메타태그 지원
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        wv.setWebViewClient(new MyWebViewClient(mActivity, wv, accountNo));
        wv.setWebChromeClient(new MyWebChromeClient(mActivity));
        if (url != null) wv.loadUrl(url);
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
        switch (view.getId()) {
            case R.id.btn_confirm:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
}