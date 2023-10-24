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
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.MyWebChromeClient;
import kr.jeet.edu.student.utils.MyWebViewClient;

public class WebViewActivity extends BaseActivity {

    private final static String TAG = "WebView Activity";

    private WebView wv;
    private String url;

    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mActivity = this;
        mContext = this;
        initData();
        initView();
    }

    private void initData(){
        try {
            Intent intent = getIntent();
            if (intent != null){
                if (intent.hasExtra(IntentParams.PARAM_WEB_VIEW_URL)){
                    url = intent.getStringExtra(IntentParams.PARAM_WEB_VIEW_URL);
                }
            }
        }catch (Exception e){

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    void initView() {

        wv = findViewById(R.id.web_view);

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

        wv.setWebViewClient(new MyWebViewClient(mActivity, wv));
        wv.setWebChromeClient(new MyWebChromeClient(mActivity));

        if (url != null) wv.loadUrl(url);
    }

    @Override
    void initAppbar() {}
}