package com.haoyu.app.activity;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ProgressWebView;

import butterknife.BindView;

/**
 * 创建日期：2017/9/5 on 16:04
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfoPreViewActivity extends BaseActivity {
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.fl_content)
    FrameLayout fl_content;
    @BindView(R.id.ll_failure)
    LinearLayout ll_failure;
    private ProgressWebView webView;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_fileinfo_preview;
    }

    @Override
    public void initView() {
        String url = getIntent().getStringExtra("url");
        String fileName = getIntent().getStringExtra("fileName");
        toolBar.setTitle_text(fileName);
        configWebview(url);
    }

    private void configWebview(String url) {
        fl_content.setVisibility(View.VISIBLE);
        webView = new ProgressWebView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        fl_content.addView(webView);
        webView.setOnReceivedListener(new ProgressWebView.OnReceivedListener() {
            @Override
            public void onReceivedTitle(WebView view, String title) {

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                fl_content.setVisibility(View.GONE);
                ll_failure.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                fl_content.setVisibility(View.GONE);
                ll_failure.setVisibility(View.VISIBLE);
            }
        });
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl(url);
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键
                        webView.goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        ll_failure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_content.setVisibility(View.VISIBLE);
                ll_failure.setVisibility(View.GONE);
                webView.reload();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers(); //小心这个！！！暂停整个 WebView 所有布局、解析、JS。
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fl_content.removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
}
