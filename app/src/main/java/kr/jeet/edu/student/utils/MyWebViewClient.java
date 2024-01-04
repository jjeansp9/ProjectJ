package kr.jeet.edu.student.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;

import kr.jeet.edu.student.R;

public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";

    private WebView wv;

    private AppCompatActivity activity;
    private AlertDialog mProgressDialog = null;
    private String accountNo = "";

    private static final String SHIN_HAN_LOGIN_URL = "https://www.shinhandamoa.com";
    private final String SHIN_HAN_LOGOUT_URL = "https://www.shinhandamoa.com/loggedOut#payer";
    private final String SHIN_HAN_JS_CODE = "javascript:window.location.replace('https://www.shinhandamoa.com/common/login#payer');";
    private final String SHIN_HAN_BTN_LOGIN_CLICK = "javascript:" +
            "   var btnId = document.getElementById('payer');" +
            "   const btnLogin = btnId.querySelector('.btn-sub-login');" +
            "   btnLogin.click();";
    private boolean btnLoginEvent = false;

    private final String BUS_ROUTE_URL = "http://m.jeet.kr/intro/table/index.jsp";
    private final String ADJUST_SCREEN_SIZE_JS_CODE = "javascript:var meta = document.createElement('meta'); meta.name = 'viewport'; meta.content = 'width=device-width, user-scalable = yes'; var header = document.getElementsByTagName('head')[0]; header.appendChild(meta)";
    private final String BUS_ROUTE_JS_CODE_MENU_CLEAR = "javascript:(function() {" +
            "   var element = document.querySelector('.mobile_quick');" +
            "   if (element) {" +
            "       element.style.display = 'none';" +
            "   }" +
            "   var headerDiv = document.getElementById('header');" +
            "   if (headerDiv) {" +
            "       headerDiv.parentNode.removeChild(headerDiv);" +
            "   }" +
            "   var sub_common2Div = document.querySelector('.sub_common2');" +
            "   if (sub_common2Div) {" +
            "       sub_common2Div.parentNode.removeChild(sub_common2Div);" +
            "   }" +
            "   var footerDiv = document.getElementById('footer');" +
            "   if (footerDiv) {" +
            "       footerDiv.parentNode.removeChild(footerDiv);" +
            "   }" +
            "   window.scrollTo(0, 0);" +
            "})()";



    public MyWebViewClient(AppCompatActivity mActivity, WebView webView) {
        this.activity = mActivity;
        this.wv = webView;
    }

    public MyWebViewClient(AppCompatActivity mActivity, WebView webView, String accountNo) {
        this.activity = mActivity;
        this.wv = webView;
        this.accountNo = accountNo;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://")) {
            if (request.getUrl().toString().startsWith("intent") && activity != null) {
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
                    if (activity != null) activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString())));
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
        showProgressDialog();

        if (url.equals(SHIN_HAN_LOGOUT_URL)) view.loadUrl(SHIN_HAN_JS_CODE);
        view.loadUrl(ADJUST_SCREEN_SIZE_JS_CODE);

        wv.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogMgr.e(TAG, "onLoadResource() : " + url);
        if (url.equals(SHIN_HAN_LOGOUT_URL)) view.loadUrl(SHIN_HAN_JS_CODE);
        view.loadUrl(ADJUST_SCREEN_SIZE_JS_CODE);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        LogMgr.e(TAG, "onPageCommitVisible() : " + url);

        if (url.contains(BUS_ROUTE_URL)) {
            view.loadUrl(BUS_ROUTE_JS_CODE_MENU_CLEAR);

        } else if (url.contains(SHIN_HAN_LOGIN_URL)) {
            view.loadUrl(shinhanJSCode());
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogMgr.e(TAG, "onPageFinished() : " + url);
        hideProgressDialog();
        wv.setVisibility(View.VISIBLE);

        view.loadUrl("javascript: window.scrollTo(0, 0);");
//        if (!TextUtils.isEmpty(accountNo)) { // 로그인 버튼 클릭 javascript 코드
//            if (!btnLoginEvent) {
//                view.loadUrl(SHIN_HAN_BTN_LOGIN_CLICK);
//                btnLoginEvent = true;
//            }
//        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (error != null) {
            int errorCode = error.getErrorCode();
            if (error.getDescription() != null) {
                CharSequence description = error.getDescription();

                if(!description.toString().equals("ERR_CLEARTEXT_NOT_PERMITTED")) {
                    // onLoad할 때 network_config_xml에 선언하지 않은 http url 주소가 load될 때가 있음
                    LogMgr.e(TAG, "onReceivedError() Code: " + errorCode + ", Description: " + description);

                    if (activity != null && !activity.isFinishing()) {
                        Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // 오류가 났을 때 대체 페이지 로드
        // wv.loadUrl("");
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.received_ssl_error_msg));
        builder.setPositiveButton(activity.getString(R.string.msg_continue), (dialog, which) -> handler.proceed());
        builder.setNegativeButton(activity.getString(R.string.cancel), (dialog, whitch) -> handler.cancel());
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null && activity != null && !activity.isFinishing()){
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
            if (mProgressDialog != null && activity != null && !activity.isFinishing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }

    private String shinhanJSCode(){
        if (activity != null && !activity.isFinishing()) {
            String inputValue = "";

            String clipboard = Utils.getClipData(activity).replaceAll("[^0-9]", "");
            //String clipboard = Utils.getClipData(activity);
            Utils.setClipData(activity, "");

            if (clipboard != null && accountNo != null) LogMgr.e(TAG, "clipboard: " + clipboard + ", accountNo: " + accountNo);

            if (clipboard.isEmpty()) {
                // 클립보드에 복사된 데이터를 불러오지 못한 경우 intent로 전달받은 데이터로 추가
                if (accountNo != null) inputValue = accountNo.replaceAll("[^0-9]", "");

            } else {
                inputValue = clipboard;
            }

            return "javascript:(function() {" +
                    "   var element = document.getElementById('btn-sitemap');" +
                    "   if (element) {" +
                    "       element.style.display = 'none';" +
                    "   }" +
                    "   var personalInfoCheckbox = document.querySelector('input#by-personal-info');" +
                    "   if (personalInfoCheckbox) {" +
                    "       var parentDiv = personalInfoCheckbox.parentElement;" +
                    "       parentDiv.remove();" +
                    "   }" +
                    "   var footer = document.querySelector('footer');" +
                    "   if (footer) {" +
                    "       footer.style.display = 'none';" +
                    "   }" +
                    "   var bankNum = document.getElementById('bankNum');" +
                    "   if (bankNum) {" +
                    "       bankNum.value = '" + inputValue + "';" +
                    "   }" +
                    "})()";
        } else {
            return "";
        }

    }
}
