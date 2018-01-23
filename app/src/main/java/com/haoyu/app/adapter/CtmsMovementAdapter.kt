package com.haoyu.app.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.CmtsMovement
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.ScreenUtils
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/22.
 * 描述:社区研修活动列表适配器
 * 作者:xiaoma
 */
class CtmsMovementAdapter(private var context: Context, mDatas: List<CmtsMovement>) : BaseArrayRecyclerAdapter<CmtsMovement>(mDatas) {
    private var imageHeight = 0
    private var onButtonClick: OnButtonClick? = null

    init {
        imageHeight = ScreenUtils.getScreenHeight(context) / 7 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.cmtsmov_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: CmtsMovement, position: Int) {
        val imageView = holder.obtainView<ImageView>(R.id.iv_image)
        imageView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageHeight)
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)  //活动标题
        val tvType = holder.obtainView<TextView>(R.id.tv_type)    //活动类型 报名中、进行中、已结束
        val tvTime = holder.obtainView<TextView>(R.id.tv_time)    //活动时间
        val tvAddress = holder.obtainView<TextView>(R.id.tv_address)   //活动地点
        val tvCreator = holder.obtainView<TextView>(R.id.tv_creator)   //活动发起人
        val tvHost = holder.obtainView<TextView>(R.id.tv_host)   //主办单位
        val tvBottom = holder.obtainView<TextView>(R.id.tvBottom)  //浏览人数
        GlideImgManager.loadImage(context, entity.image, R.drawable.app_default, R.drawable.app_default, imageView)
        val btType = holder.obtainView<Button>(R.id.bt_type)
        tvTitle.text = entity.title
        if (entity.state != null && entity.state == "no_begin") {
            tvType.text = "未开始"
            tvType.setBackgroundResource(R.drawable.teaching_research_end)
            btType.text = "查看详情"
            btType.setBackgroundResource(R.drawable.round_label)
            btType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        } else if (entity.state != null && entity.state == "register") {
            tvType.text = "报名中"
            tvType.setBackgroundResource(R.drawable.teaching_research_apply)
            if (entity.getmMovementRegisters().size > 0 && entity.getmMovementRegisters()[0].id != null) {
                btType.text = "取消报名"
            } else {
                btType.text = "报名参与"
            }
            btType.setBackgroundResource(R.drawable.round_blue_label)
            btType.setTextColor(ContextCompat.getColor(context, R.color.qianlan))
        } else if (entity.state != null && entity.state == "end") {
            tvType.text = "已结束"
            tvType.setBackgroundResource(R.drawable.teaching_research_apply)
            btType.text = "查看详情"
            btType.setBackgroundResource(R.drawable.round_blue_label)
            btType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        } else if (entity.state != null && entity.state == "begin") {
            tvType.text = "进行中"
            tvType.setBackgroundResource(R.drawable.teaching_research_ing)
            btType.text = "查看详情"
            btType.setBackgroundResource(R.drawable.round_blue_label)
            btType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        } else {
            tvType.text = "已结束"
            tvType.setBackgroundResource(R.drawable.teaching_research_apply)
            btType.text = "查看详情"
            btType.setBackgroundResource(R.drawable.round_blue_label)
            btType.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        }
        var browseNum = 0
        var partNum = 0
        if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size > 0) {
            browseNum = entity.getmMovementRelations()[0].browseNum
            partNum = entity.getmMovementRelations()[0].participateNum
        }
        val text = "$browseNum 次浏览，$partNum 人参与"
        val ss = SpannableString(text)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.darkorange)),
                0, text.indexOf("次") - 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.darkorange)),
                text.indexOf("，") + 1, text.indexOf("人") - 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvBottom.text = ss
        if (entity.getmMovementRelations().size > 0 && entity.getmMovementRelations()[0].timePeriod != null) {
            val relation = entity.getmMovementRelations()[0]
            tvTime.text = "时间：${TimeUtil.convertDayOfMinute(relation.timePeriod.startTime, relation.timePeriod.endTime)}"
        } else {
            tvTime.text = "时间："
        }
        tvAddress.text = "地点：${entity.location}"
        if (entity.creator?.realName != null) {
            tvCreator.text = "发起：${entity.creator.realName}"
        } else {
            tvCreator.text = ""
        }
        tvHost.text = "主办：${entity.sponsor}"
        btType.setOnClickListener {
            if (entity.state != null && entity.state == "register") {
                if (entity.getmMovementRegisters().size > 0 && entity.getmMovementRegisters()[0].id != null) {
                    onButtonClick?.unregister(position, entity.getmMovementRegisters()[0].id)
                } else {
                    onButtonClick?.register(position, entity.id)
                }
            } else {
                onButtonClick?.onClick(position, entity)
            }
        }
    }

    interface OnButtonClick {
        fun onClick(position: Int, entity: CmtsMovement)

        fun register(position: Int, activityId: String)

        fun unregister(position: Int, registerId: String)
    }

    fun setOnButtonClick(onButtonClick: OnButtonClick) {
        this.onButtonClick = onButtonClick
    }
}