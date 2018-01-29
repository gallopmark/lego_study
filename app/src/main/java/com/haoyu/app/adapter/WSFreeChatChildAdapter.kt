package com.haoyu.app.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.CommentEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/29.
 * 描述:工作坊自由交流自回复列表适配器
 * 作者:xiaoma
 */
class WSFreeChatChildAdapter(private val context: Context, private val userId: String, mDatas: List<CommentEntity>) : BaseArrayRecyclerAdapter<CommentEntity>(mDatas) {

    private var onDeleteListener: OnDeleteListener? = null
    override fun bindView(viewtype: Int): Int {
        return R.layout.wsfreechat_childitem
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: CommentEntity, position: Int) {
        val icUser = holder.obtainView<ImageView>(R.id.icUser)
        val tvName = holder.obtainView<TextView>(R.id.tvName)
        val tvContent = holder.obtainView<TextView>(R.id.tvContent)
        val tvTime = holder.obtainView<TextView>(R.id.tvTime)
        val llDelete = holder.obtainView<LinearLayout>(R.id.llDelete)
        if (entity.creator != null) {
            GlideImgManager.loadCircleImage(context, entity.creator.avatar, R.drawable.user_default, R.drawable.user_default, icUser)
            tvName.text = entity.creator.realName
        } else {
            icUser.setImageResource(R.drawable.user_default)
            tvName.text = null
        }
        tvContent.text = entity.content.trim()
        tvTime.text = "发表于${TimeUtil.converTime(entity.createTime)}"
        if (entity.creator?.id.equals(userId)) {
            llDelete.visibility = View.VISIBLE
        } else {
            llDelete.visibility = View.GONE
        }
        llDelete.setOnClickListener { onDeleteListener?.onDelete(position) }
    }

    interface OnDeleteListener {
        fun onDelete(position: Int)
    }

    fun setOnDeleteListener(onDeleteListener: OnDeleteListener) {
        this.onDeleteListener = onDeleteListener
    }
}