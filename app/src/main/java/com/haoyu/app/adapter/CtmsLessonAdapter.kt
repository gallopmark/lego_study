package com.haoyu.app.adapter

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.CmtsLessonEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.RoundRectProgressBar

/**
 * 创建日期：2018/1/22.
 * 描述:社区创课列表适配器
 * 作者:xiaoma
 */
class CtmsLessonAdapter(private val context: Context, mDatas: List<CmtsLessonEntity>) : BaseArrayRecyclerAdapter<CmtsLessonEntity>(mDatas) {
    private var requestClickCallBack: RequestClickCallBack? = null
    override fun bindView(viewtype: Int): Int {
        return R.layout.cmtslesson_item
    }

    @Suppress("DEPRECATION")
    override fun onBindHoder(holder: RecyclerHolder, entity: CmtsLessonEntity, position: Int) {
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)
        val tvEnd = holder.obtainView<TextView>(R.id.tv_end)
        val tvContent = holder.obtainView<TextView>(R.id.tv_content)
        val icUser = holder.obtainView<ImageView>(R.id.ic_user)
        val tvUserName = holder.obtainView<TextView>(R.id.tv_userName)
        val tvCreateTime = holder.obtainView<TextView>(R.id.tv_createTime)
        val tvHeat = holder.obtainView<TextView>(R.id.tv_heat)
        val llSupport = holder.obtainView<LinearLayout>(R.id.ll_support)
        val tvSupport = holder.obtainView<TextView>(R.id.tv_support)
        val llAdvise = holder.obtainView<LinearLayout>(R.id.ll_advise)
        val tvAdvise = holder.obtainView<TextView>(R.id.tv_advise)
        val mRrogressBar = holder.obtainView<RoundRectProgressBar>(R.id.mRrogressBar)
        val tvDay = holder.obtainView<TextView>(R.id.tv_day)
        tvTitle.text = entity.title
        if (entity.content != null) {
            tvContent.text = Html.fromHtml(entity.content)
        } else {
            tvContent.text = null
        }
        if (entity.creator?.avatar != null) {
            GlideImgManager.loadCircleImage(context, entity.creator.avatar, R.drawable.user_default, R.drawable.user_default, icUser)
        } else {
            icUser.setImageResource(R.drawable.user_default)
        }
        if (entity.creator?.realName != null) {
            tvUserName.text = entity.creator.realName
        } else {
            tvUserName.text = ""
        }
        tvCreateTime.text = TimeUtil.converTime(entity.createTime)
        if (entity.getmDiscussionRelations().size > 0) {
            tvHeat.text = entity.getmDiscussionRelations()[0].browseNum.toString()
            tvSupport.text = entity.getmDiscussionRelations()[0].supportNum.toString()
            tvAdvise.text = entity.getmDiscussionRelations()[0].replyNum.toString()
        } else {
            tvHeat.text = 0.toString()
            tvSupport.text = 0.toString()
            tvAdvise.text = 0.toString()
        }
        mRrogressBar.max = 60
        mRrogressBar.progress = 60 - entity.remainDay
        if (entity.remainDay <= 0) {
            tvDay.text = "已结束"
        } else {
            tvDay.text = "还剩${entity.remainDay}天"
        }
        if (entity.remainDay <= 0) {
            tvEnd.visibility = View.VISIBLE
        } else {
            tvEnd.visibility = View.GONE
        }
        llSupport.setOnClickListener({ requestClickCallBack?.support(entity, position) })
        llAdvise.setOnClickListener({ requestClickCallBack?.giveAdvice(entity, position) })
    }

    interface RequestClickCallBack {
        fun support(entity: CmtsLessonEntity, position: Int)

        fun giveAdvice(entity: CmtsLessonEntity, position: Int)
    }

    fun setRequestClickCallBack(requestClickCallBack: RequestClickCallBack) {
        this.requestClickCallBack = requestClickCallBack
    }
}