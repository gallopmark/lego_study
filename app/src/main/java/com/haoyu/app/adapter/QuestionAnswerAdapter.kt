package com.haoyu.app.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.FAQsAnswerEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.ExpandableTextView

/**
 * 创建日期：2018/1/31.
 * 描述:问答答案列表适配器
 * 作者:xiaoma
 */
class QuestionAnswerAdapter(private val context: Context, mDatas: List<FAQsAnswerEntity>) : BaseArrayRecyclerAdapter<FAQsAnswerEntity>(mDatas) {
    override fun bindView(viewtype: Int): Int {
        return R.layout.questionanswer_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: FAQsAnswerEntity, position: Int) {
        val ivIco = holder.obtainView<ImageView>(R.id.ivIco)
        val tvName = holder.obtainView<TextView>(R.id.tvName)
        val tvDate = holder.obtainView<TextView>(R.id.tvDate)
        val tvContent = holder.obtainView<ExpandableTextView>(R.id.tvContent)
        GlideImgManager.loadCircleImage(context, entity.creator?.avatar, R.drawable.user_default, R.drawable.user_default, ivIco)
        tvName.text = entity.creator?.realName
        tvDate.text = TimeUtil.converTime(entity.createTime)
        tvContent.text = entity.content
    }
}