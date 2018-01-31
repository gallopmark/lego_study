package com.haoyu.app.activity

import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.entity.Message
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import okhttp3.Request

/**
 * 创建日期：2018/1/30.
 * 描述:消息详情
 * 作者:xiaoma
 */
class MessageDetailActivity : BaseActivity() {
    private val context = this
    private var messageId: String? = null

    override fun setLayoutResID(): Int {
        return R.layout.activity_message_detail
    }

    override fun initView() {
        messageId = intent.getStringExtra("messageId")
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setOnLeftClickListener { finish() }
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/message/$messageId"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<Message>>() {
            private val loadingView = findViewById<LoadingView>(R.id.loadingView)
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                val loadFailView = findViewById<LoadFailView>(R.id.loadFailView)
                loadFailView.setOnRetryListener { initData() }
            }

            override fun onResponse(response: BaseResponseResult<Message>?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null) {
                    updateUI(response.responseData)
                } else {
                    val tvEmpty = findViewById<TextView>(R.id.tv_empty)
                    tvEmpty.text = "暂无数据~"
                    tvEmpty.visibility = View.VISIBLE
                }
            }

        }))
    }

    private fun updateUI(entity: Message) {
        val llContent = findViewById<LinearLayout>(R.id.llContent)
        llContent.visibility = View.VISIBLE
        val tvType = findViewById<TextView>(R.id.tvType)
        val tvTime = findViewById<TextView>(R.id.tvTime)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvContent = findViewById<TextView>(R.id.tvContent)
        if (entity.type != null && entity.type == Message.TYPE_SYSTEM) {
            tvType.setBackgroundResource(R.drawable.shape_message_system)
            tvType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            tvType.text = "系统通知"
        } else if (entity.type != null && entity.type == Message.TYPE_USER) {
            tvType.setBackgroundResource(R.drawable.shape_message_user)
            tvType.setTextColor(ContextCompat.getColor(context, R.color.message_user))
            tvType.text = if (entity.sender?.realName != null) entity.sender.realName else ""
        } else if (entity.type != null && entity.type == Message.TYPE_DAILY_WARN) {
            tvType.setBackgroundResource(R.drawable.shape_message_system)
            tvType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            tvType.text = "每日提醒"
        } else {
            tvType.setBackgroundResource(R.drawable.shape_message_system)
            tvType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            tvType.text = entity.type
        }
        tvTime.text = TimeUtil.getSlashDate(entity.createTime)
        if (TextUtils.isEmpty(entity.title)) {
            tvTitle.visibility = View.GONE
        } else {
            tvTitle.text = entity.title
        }
        tvContent.text = entity.content
    }
}