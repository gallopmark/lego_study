package com.haoyu.app.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/12.
 * 描述:教务咨询
 * 作者:xiaoma
 */
class EducationConsultActivity : BaseActivity() {
    lateinit var context: EducationConsultActivity
    lateinit var tv_phone: TextView
    override fun setLayoutResID(): Int {
        return R.layout.activity_educational_consulting
    }

    override fun initView() {
        context = this
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setTitle_text("教务咨询")
        toolBar.setOnLeftClickListener { finish() }
        tv_phone = findViewById(R.id.tv_phone)
        tv_phone.setOnClickListener({
            if (hasPhone()) {
                openPhone()
            } else {    //申请拨打电话权限
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        })
    }

    /*判断是否已经打开拨打电话权限*/
    private fun hasPhone(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    /*拨打电话*/
    private fun openPhone() {
        try {
            val phone = tv_phone.text.toString().replace("-", "")
            val intent = Intent()
            intent.action = Intent.ACTION_CALL
            intent.data = Uri.parse("tel:" + phone)
            startActivity(intent)
        } catch (e: Exception) {
            toast(context, "通话失败，请稍后再试")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openPhone()
        }
    }
}