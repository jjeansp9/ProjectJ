package kr.jeet.edu.student.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;

import kr.jeet.edu.student.R;

public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";

    private WebView wv;

    private AppCompatActivity activity;
    private AlertDialog mProgressDialog = null;

    // 자동로그아웃 화면이 떴을 때
//    onLoadResource() : https://www.shinhandamoa.com/common/login#payer,
//    onPageFinished() : https://www.shinhandamoa.com/common/login#payer,
//    onLoadResource() : https://www.shinhandamoa.com/loggedOut#payer,
//    onPageStarted() : https://www.shinhandamoa.com/loggedOut#payer,
//    onLoadResource() : https://www.shinhandamoa.com/assets/bootstrap/bootstrap.css,
//    onPageFinished() : https://www.shinhandamoa.com/loggedOut#payer

    // 정상 이동하였을 때
//    onLoadResource() : https://www.shinhandamoa.com/common/login#payer,
//    onPageStarted() : https://www.shinhandamoa.com/common/login#payer,
//    onLoadResource() : https://www.shinhandamoa,
//    onPageFinished() : https://www.shinhandamoa.com/common/login#payer

    public MyWebViewClient(AppCompatActivity mActivity, WebView webView) {
        this.activity = mActivity;
        this.wv = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://")) {
            if (request.getUrl().toString().startsWith("intent")) {
                Intent schemeIntent;
                try {
                    schemeIntent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    activity.startActivity(schemeIntent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    String pkgName = schemeIntent.getPackage();
                    if (pkgName != null) {
                        activity.startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + pkgName)
                                )
                        );
                        return true;
                    }
                }
            } else {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString())));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            view.clearCache(true);
            view.clearHistory();
            view.loadUrl(request.getUrl().toString());
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogMgr.e(TAG, "onPageStarted() : " + url);
        if (url.equals("https://www.shinhandamoa.com/loggedOut#payer")) {
            view.loadUrl("javascript:window.location.replace('https://www.shinhandamoa.com/common/login#payer');");
        }
        showProgressDialog();
        wv.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogMgr.e(TAG, "onLoadResource() : " + url);
        if (url.equals("https://www.shinhandamoa.com/loggedOut#payer")) {
            view.loadUrl("javascript:window.location.replace('https://www.shinhandamoa.com/common/login#payer');");
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogMgr.e(TAG, "onPageFinished() : " + url);

        String javascriptCode = "";

        // 예제 1: #payer-login-method 요소의 change 이벤트를 트리거하여 원하는 동작 수행
        javascriptCode += "$('#payer-login-method').val('bank-number').trigger('change');";

        // 예제 2: #by-account 요소의 click 이벤트를 트리거하여 원하는 동작 수행
        javascriptCode += "$('#by-account').prop('checked', true).trigger('click');";

        // 예제 3: .second-row-input 요소에 값을 추가
        javascriptCode += "$('.second-row-input').val('1234');";


        view.evaluateJavascript(javascriptCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // JavaScript 코드 실행 결과 처리
                if ("null".equals(value)) {
                    // JavaScript 코드가 아무런 결과를 반환하지 않은 경우
                    LogMgr.e(TAG, "JavaScript execution result is null");
                } else {
                    // JavaScript 코드가 결과를 반환한 경우
                    LogMgr.e(TAG, "JavaScript execution result: " + value);
                }

                // 이후에 필요한 작업을 수행할 수 있습니다.
                hideProgressDialog();
                wv.setVisibility(View.VISIBLE);
            }
        });

        hideProgressDialog();
        wv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();
        LogMgr.e(TAG, "onReceivedError() : " + error);
        // 오류가 났을 때 대체 페이지 로드
        //wv.loadUrl("");
    }

    private void showProgressDialog() {
        if (mProgressDialog == null){
            View view = activity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(activity.getString(R.string.requesting));

            mProgressDialog = new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
}
