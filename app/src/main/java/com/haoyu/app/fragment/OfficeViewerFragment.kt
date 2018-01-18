package com.haoyu.app.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.LoadFailView

@Suppress("DEPRECATION")
/**
 * 创建日期：2018/1/18.
 * 描述:office文档查看
 * 作者:xiaoma
 */
class OfficeViewerFragment : Fragment() {
    private lateinit var flContent: FrameLayout
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadFailView: LoadFailView
    private var url: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getString("url")?.let {
            url = "https://view.officeapps.live.com/op/view.aspx?src=" + it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_webviewer, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        flContent = view.findViewById(R.id.fl_content)
        loadFailView = view.findViewById(R.id.loadFailView)
        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progressBar)
        loadFailView.setErrorMsg("网页加载失败，请点击重试！")
        url?.let {
            configWebview(it)
        }
    }

    private fun configWebview(url: String) {
        initWebViewSettings()
        webView.webViewClient = X5WebViewClient()
        webView.webChromeClient = X5WebChromeClient()
        webView.loadUrl(url)
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()// 返回前一个页面
                return@OnKeyListener true
            }
            false
        })
        loadFailView.setOnRetryListener {
            webView.reload()
            flContent.visibility = View.VISIBLE
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        webView.settings.setSupportZoom(true)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true//设置此属性，可任意比例缩放
        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.builtInZoomControls = true
        // 支持通过js打开新的窗口
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.pluginState = WebSettings.PluginState.ON
        //不显示webview缩放按钮
        webView.settings.displayZoomControls = false
        webView.settings.defaultZoom = WebSettings.ZoomDensity.FAR// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.domStorageEnabled = true
    }

    private inner class X5WebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            flContent.visibility = View.GONE
            loadFailView.visibility = View.VISIBLE
        }
    }

    private inner class X5WebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (loadFailView.visibility != View.GONE) loadFailView.visibility = View.GONE
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
            } else {
                if (progressBar.visibility != View.VISIBLE) progressBar.visibility = View.VISIBLE
                progressBar.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        webView.pauseTimers()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        webView.removeAllViews()
        webView.destroy()
    }
}