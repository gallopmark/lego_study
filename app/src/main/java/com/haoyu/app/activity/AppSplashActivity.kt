package com.haoyu.app.activity

import android.content.Intent
import android.os.Handler
import android.view.KeyEvent
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.lego.student.R

/**
 * 创建日期：2018/1/12.
 * 描述:启动页
 * 作者:xiaoma
 */
class AppSplashActivity : BaseActivity() {
    override fun setLayoutResID(): Int {
        return R.layout.activity_app_splash
    }

    override fun initData() {
        val handler = Handler(mainLooper)
        handler.postDelayed({ enter() }, 2500)
    }

    private fun enter() {
        if (firstLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else super.onKeyDown(keyCode, event)
    }
}