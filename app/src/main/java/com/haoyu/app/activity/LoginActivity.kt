package com.haoyu.app.activity

import android.content.Intent
import android.graphics.Rect
import android.text.TextUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ScrollView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.dialog.LoadingDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.LoginResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.SharePreferenceHelper
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * 创建日期：2018/1/12.
 * 描述:登录页面
 * 作者:xiaoma
 */
class LoginActivity : BaseActivity() {
    private lateinit var context: LoginActivity
    private lateinit var et_userName: EditText
    private lateinit var et_passWord: EditText
    private lateinit var cb_remember: CheckBox
    private lateinit var bt_login: Button
    private var requestUN = true
    private var remember: Boolean = false
    override fun setLayoutResID(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
        context = this
        et_userName = findViewById(R.id.et_userName)
        et_passWord = findViewById(R.id.et_passWord)
        cb_remember = findViewById(R.id.cb_remember)
        bt_login = findViewById(R.id.bt_login)
        et_userName.setText(account)
        et_userName.setSelection(et_userName.text.length)
        remember = isRemember
        if (remember) {
            et_passWord.setText(passWord)
        }
        cb_remember.isChecked = remember
        controlKeyboardLayout()
    }

    /**
     * 最外层布局，需要调整的布局
     * 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
     */
    private fun controlKeyboardLayout() {
        val rootView = findViewById<ScrollView>(R.id.rootView)
        rootView.viewTreeObserver.addOnGlobalLayoutListener({
            val rect = Rect()
            //获取root在窗体的可视区域
            rootView.getWindowVisibleDisplayFrame(rect)
            //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
            val rootInvisibleHeight = rootView.rootView.height - rect.bottom
            //若不可视区域高度大于100，则键盘显示
            if (rootInvisibleHeight > 100) {
                rootView.fullScroll(ScrollView.FOCUS_DOWN) //滚动到底部
            } else {
                //键盘隐藏
                rootView.fullScroll(ScrollView.FOCUS_UP) //滚动到顶部
            }
            requestFocus()
        })
    }

    private fun requestFocus() {
        if (requestUN) {
            et_userName.requestFocus()
            et_passWord.clearFocus()
        } else {
            et_userName.clearFocus()
            et_passWord.requestFocus()
        }
    }

    override fun setListener() {
        bt_login.setOnClickListener({
            Common.hideSoftInput(context)
            val userName = et_userName.text.toString().trim()
            val passWord = et_passWord.text.toString().trim()
            if (TextUtils.isEmpty(userName)) {
                toast(context, "请输入账号")
            }
            if (TextUtils.isEmpty(passWord)) {
                toast(context, "请输入密码")
            }
            bt_login.isEnabled = false
            login(userName, passWord)
        })
        et_userName.setOnTouchListener { _, _ ->
            requestUN = true
            false
        }
        et_passWord.setOnTouchListener { _, _ ->
            requestUN = false
            false
        }
        cb_remember.setOnCheckedChangeListener { _, isChecked -> remember = isChecked }
    }

    private fun login(userName: String, passWord: String) {
        val loading = LoadingDialog(context, "正在登录")
        loading.setCanceledOnTouchOutside(false)
        loading.show()
        val url = Constants.LOGIN_URL
        addSubscription(Flowable.just(url).map(@Throws(Exception::class) { _ -> getUserInfo(url, userName, passWord) })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    loading.dismiss()
                    bt_login.isEnabled = true
                    val role = result?.responseData?.role
                    if (role != null && role.contains(student)) {
                        result.responseData?.let { saveUserInfo(it) }
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showInfoDialog()
                    }
                }) {
                    loading.dismiss()
                    bt_login.isEnabled = true
                    toast(context, "登录失败，请稍后再试")
                })
    }

    @Throws(Exception::class)
    private fun getUserInfo(url: String, userName: String, passWord: String): LoginResult? {
        val map = HashMap<String, String>()
        map.put("username", userName)
        map.put("password", passWord)
        return OkHttpClientManager.getInstance().login(context, url, userName, passWord)
    }

    private fun showInfoDialog() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("您不是学员身份，请选择正确版本登录")
        dialog.setPositiveButton("我知道了") { _, _ -> dialog.dismiss() }
        dialog.show()
    }

    /**
     * 保存用户信息
     */
    private fun saveUserInfo(user: MobileUser) {
        val sharePreferenceHelper = SharePreferenceHelper(context)
        val map = HashMap<String, Any>()
        map.put("avatar", user.avatar)
        map.put("id", user.id)
        map.put("account", et_userName.text.toString().trim())
        if (remember) {
            map.put("firstLogin", false)
            map.put("remember", true)
        } else {
            map.put("firstLogin", true)
            map.put("remember", false)
        }
        if (user.realName != null)
            map.put("realName", user.realName)
        else
            map.put("realName", user.userName)
        map.put("password", et_passWord.text.toString().trim())
        map.put("userName", user.userName)
        map.put("deptName", user.deptName)
        map.put("role", user.role)
        sharePreferenceHelper.saveSharePreference(map)
    }
}

