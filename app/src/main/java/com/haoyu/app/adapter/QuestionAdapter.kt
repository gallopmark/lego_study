package com.haoyu.app.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.FAQsEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/31.
 * 描述:工作坊互助问答列表适配器
 * 作者:xiaoma
 */
class QuestionAdapter(private val context: Context, mDatas: List<FAQsEntity>, private val type: Int) : BaseArrayRecyclerAdapter<FAQsEntity>(mDatas) {

    override fun bindView(viewtype: Int): Int {
        return R.layout.question_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: FAQsEntity, position: Int) {
        val ivIco = holder.obtainView<ImageView>(R.id.ivIco)
        val tvName = holder.obtainView<TextView>(R.id.tvName)
        val tvDate = holder.obtainView<TextView>(R.id.tvDate)
        val tvQuestion = holder.obtainView<TextView>(R.id.tvQuestion)
        val tvAnswer = holder.obtainView<TextView>(R.id.tvAnswer)
        val tvCount = holder.obtainView<TextView>(R.id.tvCount)
        GlideImgManager.loadCircleImage(context, entity.creator?.avatar, R.drawable.user_default, R.drawable.user_default, ivIco)
        tvName.text = entity.creator?.realName
        tvDate.text = TimeUtil.converTime(entity.createTime)
        tvQuestion.text = entity.content
        tvAnswer.text = if (entity.newstAnswer != null) entity.newstAnswer.content else "暂时没有人回答"
        if (type == 1) {
            val tvCollection = holder.obtainView<TextView>(R.id.tvCollection)
            tvCollection.visibility = View.VISIBLE
            tvCollection.text = if (entity.follow != null) "取消收藏" else "收藏"
        } else {
            val ivCollection = holder.obtainView<ImageView>(R.id.ivCollection)
            ivCollection.visibility = View.VISIBLE
            if (entity.follow != null) {
                ivCollection.setImageResource(R.drawable.workshop_collect_press)
            } else {
                ivCollection.setImageResource(R.drawable.workshop_collect_default)
            }
        }
        tvCount.text = "全部答案（${entity.faqAnswerCount}）"
    }
}