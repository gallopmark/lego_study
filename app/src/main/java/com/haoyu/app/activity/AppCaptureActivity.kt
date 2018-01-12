package com.haoyu.app.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.zxing.CaptureFragment
import com.haoyu.app.zxing.CodeUtils

/**
 * 创建日期：2018/1/12.
 * 描述:定制化显示扫描界面
 * 作者:xiaoma
 */
class AppCaptureActivity : BaseActivity() {
    lateinit var context: AppCaptureActivity
    var requestCamera = false
    lateinit var ll_noCamera: LinearLayout
    lateinit var tv_tips: TextView
    lateinit var bt_settings: Button
    lateinit var fl_my_container: FrameLayout
    override fun setLayoutResID(): Int {
        return R.layout.activity_app_capture
    }

    override fun initView() {
        context = this
        setToolBar()
        ll_noCamera = findViewById(R.id.ll_noCamera)
        tv_tips = findViewById(R.id.tv_tips)
        bt_settings = findViewById(R.id.bt_settings)
        fl_my_container = findViewById(R.id.fl_my_container)
        if (hasCameraPermission()) {
            requestCamera()
        } else {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CAMERA), 1)
        }
        bt_settings.setOnClickListener({ openSettings() })
    }

    private fun setToolBar() {
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setTitle_text("扫一扫")
        toolBar.setOnLeftClickListener { finish() }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCamera() {
        requestCamera = true
        ll_noCamera.visibility = View.GONE
        fl_my_container.visibility = View.VISIBLE
        val fragment = CaptureFragment()
        // 为二维码扫描界面设置定制化界面
        fragment.analyzeCallback = analyzeCallback
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, fragment).commitAllowingStateLoss()
    }

    /**
     * 二维码解析回调函数
     */
    private var analyzeCallback: CodeUtils.AnalyzeCallback = object : CodeUtils.AnalyzeCallback {
        override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
            val intent = Intent()
            intent.putExtra(CodeUtils.RESULT_STRING, result)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        override fun onAnalyzeFailed() {
            toast(context, "无法识别的二维码")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestCamera()
        } else {
            ll_noCamera.visibility = View.VISIBLE
            tv_tips.text = "相机权限已被禁止，无法完成扫描，请重新打开相机权限。"
        }
    }

    private fun openSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + packageName)
            startActivity(intent)
        } catch (e: Exception) {
            toast(context, "无法跳转设置界面，请手动打开")
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (hasCameraPermission() && !requestCamera) {
            requestCamera()
        }
    }

}