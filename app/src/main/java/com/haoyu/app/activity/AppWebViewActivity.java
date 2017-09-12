package com.haoyu.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import butterknife.BindView;

import static android.view.View.GONE;

/**
 * 创建日期：2017/6/22 on 9:37
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppWebViewActivity extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_result)
    HtmlTextView tv_result;
    @BindView(R.id.ll_webContent)
    LinearLayout ll_webContent;
    @BindView(R.id.ll_loadFail)
    LinearLayout ll_loadFail;
    @BindView(R.id.tv_failTip)
    TextView tv_failTip;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_webview;
    }

    @Override
    public void initView() {
        String result = getIntent().getStringExtra("text");
        String webUrl = getIntent().getStringExtra("webUrl");
        if (result != null) {
            tv_result.setVisibility(View.VISIBLE);
            tv_result.setHtml(result, new HtmlHttpImageGetter(tv_result));
        } else if (webUrl != null) {
            ll_webContent.setVisibility(View.VISIBLE);
        }
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        // 支持通过js打开新的窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            webSettings.setMediaPlaybackRequiresUserGesture(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        //不显示webview缩放按钮
        webSettings.setDisplayZoomControls(false);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // 接受所有证书
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.setVisibility(View.GONE);
                ll_loadFail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webView.setVisibility(View.GONE);
                ll_loadFail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    toastFullScreen("无法创建该下载任务",false);
                }
            }
        });
        webView.loadUrl(webUrl);
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
    }
}
