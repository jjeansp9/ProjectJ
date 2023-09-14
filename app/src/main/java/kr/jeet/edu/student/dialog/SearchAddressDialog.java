package kr.jeet.edu.student.dialog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.LogMgr;

public class SearchAddressDialog extends DialogFragment {

    static public SearchAddressDialog newInstance() {
        return new SearchAddressDialog();
    }

    public interface OnSearchAddressResult {
        void onConfirm(String address);
    }

    public void showDialog(AppCompatActivity activity, OnSearchAddressResult result) {
        this.mResult = result;
        show(activity.getSupportFragmentManager(), "searchAddressDialog");
    }

    private WebView mWebView;
    private ProgressBar progressBar;
    public OnSearchAddressResult mResult;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowsDialog(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_search_address_dialog, container, false);

        v.findViewById(R.id.ly_dialog_bg).setClipToOutline(true);
        mWebView = (WebView) v.findViewById(R.id.webview);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar);

        v.findViewById(R.id.btn_close).setOnClickListener(view -> dismiss());

        initWebView();

        return v;
    }

    private void initWebView() {
        // JavaScript 허용
        mWebView.getSettings().setJavaScriptEnabled(true);

        // JavaScript의 window.open 허용
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        mWebView.addJavascriptInterface(new SearchJavaScriptInterface(mResult), "Android"); // HTML 문서에도 동일하게 작성 window.Android....

        //DOMStorage 허용
        mWebView.getSettings().setDomStorageEnabled(true);

        //ssl 인증이 없는 경우 해결을 위한 부분
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // SSL 에러가 발생해도 계속 진행
                handler.proceed();
            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }

            // 페이지 로딩 시작시 호출
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogMgr.e("페이지 시작", url);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);

                LogMgr.e("페이지 로딩", url);
                mWebView.loadUrl("javascript:sample2_execDaumPostcode();");
            }
        });

        // webview url load. php or html 파일 주소
//        mWebView.loadUrl("http://sososhopping.com:8080/roadSearch.html");
        mWebView.loadUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX + "main/searchAddress/android");

//        mWebView.loadUrl("file:///android_asset/searchAddress.html");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private class SearchJavaScriptInterface {

        public OnSearchAddressResult mResult;

        public SearchJavaScriptInterface(OnSearchAddressResult result) {
            mResult = result;
        }

        @JavascriptInterface
        public void processDATA(String str) {
            mResult.onConfirm(str);

        }
    }

}
