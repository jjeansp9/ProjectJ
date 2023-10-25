package kr.jeet.edu.student.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
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

    private static final String SHIN_HAN_LOGIN_URL = "https://www.shinhandamoa.com/common/login#payer";
    private final String SHIN_HAN_LOGOUT_URL = "https://www.shinhandamoa.com/loggedOut#payer";
    private final String SHIN_HAN_JS_CODE = "javascript:window.location.replace('https://www.shinhandamoa.com/common/login#payer');";

    private final String BUS_ROUTE_URL = "http://m.jeet.kr/intro/table/index.jsp?route_type=1&campus_fk=";
    private final String BUS_ROUTE_JS_CODE = "javascript:var meta = document.createElement('meta'); meta.name = 'viewport'; meta.content = 'width=device-width, user-scalable = yes'; var header = document.getElementsByTagName('head')[0]; header.appendChild(meta)";
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
        showProgressDialog();

        if (url.equals(SHIN_HAN_LOGOUT_URL)) view.loadUrl(SHIN_HAN_JS_CODE);
        view.loadUrl(BUS_ROUTE_JS_CODE);

        wv.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogMgr.e(TAG, "onLoadResource() : " + url);
        if (url.equals(SHIN_HAN_LOGOUT_URL)) view.loadUrl(SHIN_HAN_JS_CODE);
        view.loadUrl(BUS_ROUTE_JS_CODE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogMgr.e(TAG, "onPageFinished() : " + url);

        if (url.contains(BUS_ROUTE_URL)) view.loadUrl(BUS_ROUTE_JS_CODE_MENU_CLEAR);
        else if (url.contains(SHIN_HAN_LOGIN_URL)) view.loadUrl(shinhanJSCode());

        hideProgressDialog();
        wv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();

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

    private String shinhanJSCode(){
        String getClipboard = Utils.getClipData(activity).replaceAll("[^0-9]", "");
        LogMgr.e(TAG, "clipboard: " + getClipboard);
        int clipboard = 0;
        Utils.setClipData(activity, "");
        try{ clipboard = Integer.parseInt(getClipboard); }
        catch (Exception e) {}

        return "javascript:(function() {" +
                "   var bankNumElement = document.getElementById('bankNum');" +
                "   if (bankNumElement) {" +
                "       bankNumElement.value = '" + clipboard + "';" +
                "   }" +
                "   window.scrollTo(0, 0);" +
                "})()";
    }
}
