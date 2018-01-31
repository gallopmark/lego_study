package com.haoyu.app.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.Message
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/30.
 * 描述:消息列表适配器
 * 作者:xiaoma
 */
class MessageAdapter(private val context: Context, mDatas: List<Message>) : BaseArrayRecyclerAdapter<Message>(mDatas) {
    override fun bindView(viewtype: Int): Int {
        return R.layout.message_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: Message, position: Int) {
        val tvType = holder.obtainView<TextView>(R.id.tvType)
        val tvTime = holder.obtainView<TextView>(R.id.tvTime)
        val tvContent = holder.obtainView<TextView>(R.id.tvContent)
        val btReply = holder.obtainView<Button>(R.id.btReply)
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
        tvContent.text = entity.content
        btReply.visibility = if (entity.type != null && entity.type == Message.TYPE_USER) View.VISIBLE else View.GONE
    }
}