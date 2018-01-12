package com.haoyu.app.activity

import com.haoyu.app.base.BaseActivity
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/12.
 * 描述:关于我们
 * 作者:xiaoma
 */
class AboutUsActivity : BaseActivity() {
    override fun setLayoutResID(): Int {
        return R.layout.activity_about_us
    }

    override fun initView() {
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setOnLeftClickListener {
            finish()
        }
    }

}