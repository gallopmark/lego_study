package com.haoyu.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.haoyu.app.lego.student.R;


/**
 * 创建日期：2017/8/29 on 10:01
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class ProgressWebView extends WebView {

    private ProgressBar progressbar;
    private OnReceivedListener onReceivedListener;

    public ProgressWebView(Context context) {
        super(context);
        init(context);
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10, 0, 0));
        Drawable drawable = context.getResources().getDrawable(R.drawable.progress_loading);
        progressbar.setProgressDrawable(drawable);
        addView(progressbar);
        //是否可以缩放
        WebSettings settings = getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        settings.setJavaScriptEnabled(true);
        settings.setSupportMultipleWindows(true);
        settings.setBuiltInZoomControls(true);
        // 支持通过js打开新的窗口
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        //不显示webview缩放按钮
        settings.setDisplayZoomControls(false);
        settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        setWebViewClient(new AppWebViewClient());
        setWebChromeClient(new AppWebChromeClient());
    }

    public class AppWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView mWebView = new WebView(view.getContext());
            WebViewTransport transport = (WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);//注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        //处理alert弹出框，html 弹框的一种方式
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return true;
        }

        //处理confirm弹出框
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult
                result) {
            return true;
        }

        //处理prompt弹出框
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (onReceivedListener != null)
                onReceivedListener.onReceivedTitle(view, title);
        }
    }

    public class AppWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (onReceivedListener != null)
                onReceivedListener.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (onReceivedListener != null)
                onReceivedListener.onReceivedError(view, request, error);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public interface OnReceivedListener {
        void onReceivedTitle(WebView view, String title);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

        void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error);
    }

    public void setOnReceivedListener(OnReceivedListener onReceivedListener) {
        this.onReceivedListener = onReceivedListener;
    }
}
