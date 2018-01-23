package com.haoyu.app.adapter

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.DiscussEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/22.
 * 描述:社区研说列表适配器
 * 作者:xiaoma
 */
class CtmsSaysAdapter(private val context: Context, mDatas: List<DiscussEntity>) : BaseArrayRecyclerAdapter<DiscussEntity>(mDatas) {
    private var requestClickCallBack: RequestClickCallBack? = null
    override fun bindView(viewType: Int): Int {
        return R.layout.cmtssays_item
    }

    @Suppress("DEPRECATION")
    override fun onBindHoder(holder: RecyclerHolder, entity: DiscussEntity, position: Int) {
        val ivIco = holder.obtainView<ImageView>(R.id.iv_ico)
        val tvUserName = holder.obtainView<TextView>(R.id.tv_userName)
        val tvCreateTime = holder.obtainView<TextView>(R.id.tv_createTime)
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)
        val tvContent = holder.obtainView<TextView>(R.id.tv_content)
        val rlSupport = holder.obtainView<View>(R.id.rl_support)
        val tvSupport = holder.obtainView<TextView>(R.id.tv_support)
        val rlComment = holder.obtainView<View>(R.id.rl_comment)
        val tvCommnet = holder.obtainView<TextView>(R.id.tv_comment)
        if (entity.creator != null) {
            GlideImgManager.loadCircleImage(context, entity.creator.avatar, R.drawable.user_default, R.drawable.user_default, ivIco)
        } else {
            ivIco.setImageResource(R.drawable.user_default)
        }
        if (entity.creator?.realName != null) {
            tvUserName.text = entity.creator.realName
        } else {
            tvUserName.text = ""
        }
        tvCreateTime.text = TimeUtil.converTime(entity.createTime)
        tvTitle.text = entity.title
        if (entity.content != null) {
            val spanned = Html.fromHtml(entity.content)
            tvContent.text = spanned
        } else {
            tvContent.text = null
        }
        if (entity.getmDiscussionRelations().size > 0) {
            tvSupport.text = entity.getmDiscussionRelations()[0].supportNum.toString()
            tvCommnet.text = entity.getmDiscussionRelations()[0].replyNum.toString()
        } else {
            tvSupport.text = 0.toString()
            tvCommnet.text = 0.toString()
        }
        rlSupport.setOnClickListener({ requestClickCallBack?.support(entity, position) })
        rlComment.setOnClickListener({ requestClickCallBack?.comment(entity, position) })
    }

    interface RequestClickCallBack {
        fun support(entity: DiscussEntity, position: Int)

        fun comment(entity: DiscussEntity, position: Int)
    }

    fun setRequestClickCallBack(requestClickCallBack: RequestClickCallBack) {
        this.requestClickCallBack = requestClickCallBack
    }
}