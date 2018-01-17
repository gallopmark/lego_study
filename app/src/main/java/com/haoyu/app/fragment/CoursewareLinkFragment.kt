package com.haoyu.app.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.view.LoadFailView
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*


@Suppress("DEPRECATION")
/**
 * 创建日期：2018/1/17.
 * 描述:课程学习教学课件外链查看
 * 作者:xiaoma
 */
class CoursewareLinkFragment : Fragment() {
    private lateinit var flContent: FrameLayout
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadFailView: LoadFailView
    private var url: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        url = arguments?.getString("url")
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
        val map = HashMap<String, String>()
        map.put("Referer", Constants.REFERER)
        webView.loadUrl(url, map)
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
        val webSetting = webView.settings
        webSetting.setSupportZoom(true)
        webSetting.loadWithOverviewMode = true
        webSetting.useWideViewPort = true//设置此属性，可任意比例缩放
        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        webSetting.javaScriptEnabled = true
        webSetting.setSupportMultipleWindows(true)
        webSetting.builtInZoomControls = true
        // 支持通过js打开新的窗口
        webSetting.javaScriptCanOpenWindowsAutomatically = true
        webSetting.pluginState = WebSettings.PluginState.ON
        //不显示webview缩放按钮
        webSetting.displayZoomControls = false
        webSetting.defaultZoom = WebSettings.ZoomDensity.FAR// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        webSetting.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSetting.domStorageEnabled = true
    }

    private inner class X5WebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            return try {
                handleHttp(url)
            } catch (e: Exception) {
                super.shouldInterceptRequest(view, url)
            }
        }

        @Throws(Exception::class)
        private fun handleHttp(url: String): WebResourceResponse {
            val client = OkHttpClient()
            val request = Request.Builder().addHeader("Referer", Constants.REFERER).url(url).build()
            val response = client.newCall(request).execute()
            var mimeType = ""
            response?.header("content-type", response.body()?.contentType()?.type())?.let {
                mimeType = it
                if (it.contains(";")) {
                    mimeType = it.substring(0, it.indexOf(";"))
                }
            }
            var encoding = ""
            response?.body()?.contentType()?.charset()?.name()?.let {
                encoding = it
            }
            val inputStream = response?.body()?.byteStream()
            return WebResourceResponse(mimeType, encoding, inputStream)
        }

        override fun onReceivedSslError(webView: WebView, handler: SslErrorHandler, sslError: SslError) {
            handler.proceed()
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