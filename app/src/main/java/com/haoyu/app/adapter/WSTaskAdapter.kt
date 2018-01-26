package com.haoyu.app.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.entity.MWorkshopActivity
import com.haoyu.app.entity.MWorkshopSection
import com.haoyu.app.entity.MultiItemEntity
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/25.
 * 描述:工作坊阶段任务适配器
 * 作者:xiaoma
 */
class WSTaskAdapter(private val context: Context, mDatas: List<MultiItemEntity>) : BaseArrayRecyclerAdapter<MultiItemEntity>(mDatas) {
    private val collapses = ArrayMap<Int, Boolean>()
    private var selected = -1

    fun addItemEntities(list: List<MultiItemEntity>) {
        this.mDatas.addAll(list)
        mDatas.indices.filter { mDatas[it] is MWorkshopSection }.forEach { collapses[it] = it == 0 }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return mDatas[position].itemType
    }

    override fun bindView(viewtype: Int): Int {
        return if (viewtype == 1) {
            R.layout.wssection_item
        } else {
            R.layout.wsactivity_item
        }
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity, position: Int) {
        val viewType = holder.itemViewType
        if (viewType == 1) {
            setViewType1(holder, entity, position)
        } else {
            setViewType2(holder, entity, position)
        }
    }

    private fun setViewType1(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity, position: Int) {
        val tvPosition = holder.obtainView<TextView>(R.id.tv_position)
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)
        val tvTime = holder.obtainView<TextView>(R.id.tv_time)
        val ivExpand = holder.obtainView<AppCompatImageView>(R.id.iv_expand)
        val section = entity as MWorkshopSection
        val stageIndex = section.position + 1
        when {
            stageIndex < 10 -> tvPosition.text = "0$stageIndex"
            stageIndex in 10..99 -> tvPosition.text = stageIndex.toString()
            else -> tvPosition.text = ".."
        }
        tvTitle.text = section.title
        var textTime = "研修时间："
        section.timePeriod?.let {
            textTime += TimeUtil.getDateYM(it.startTime) + "-" + TimeUtil.getDateYM(it.endTime)
        }
        tvTime.text = textTime
        if (collapses[position] != null && collapses[position] == true) {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = true
            }
            ivExpand.setImageResource(R.drawable.ic_expand_more_black_24dp)
        } else {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = false
            }
            ivExpand.setImageResource(R.drawable.ic_arrow_right_black_24dp)
        }
    }

    private fun setViewType2(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity, position: Int) {
        val icType = holder.obtainView<ImageView>(R.id.ic_type)
        val tvTypeName = holder.obtainView<TextView>(R.id.tv_typeName)
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)
        val activity = entity as MWorkshopActivity
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (activity.isVisible) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        } else {
            params.height = 0
        }
        holder.itemView.layoutParams = params
        val type = activity.type
        var typeName = "阶段任务"
        var resId = R.drawable.ic_ws_def_selector
        type?.let {
            when (it) {
                "discussion" -> {
                    resId = R.drawable.ic_ws_discuss_selector
                    typeName = "教学研讨"
                }
                "survey" -> {
                    resId = R.drawable.ic_ws_survey_selector
                    typeName = "调查问卷"
                }
                "debate" -> {
                    resId = R.drawable.ic_ws_debate_selector
                    typeName = "在线辩论"
                }
                "lcec" -> {
                    resId = R.drawable.ic_ws_lcec_selector
                    typeName = "听课评课"
                }
                "lesson_plan" -> {
                    resId = R.drawable.ic_ws_lessonplan_selector
                    typeName = "集体备课"
                }
                "test" -> {
                    typeName = "在线测验"
                }
                "video" -> {
                    resId = R.drawable.ic_ws_video_selector
                    typeName = "教学观摩"
                }
                "discuss_class" -> {
                    resId = R.drawable.ic_ws_video_selector
                    typeName = "教学观摩"
                }
                else -> {
                    resId = R.drawable.ic_ws_def_selector
                    typeName = "阶段任务"
                }
            }
        }
        icType.setImageResource(resId)
        tvTypeName.text = typeName
        tvTitle.text = activity.title
        if (selected == position) {
            tvTypeName.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            icType.isSelected = true
        } else {
            tvTypeName.setTextColor(ContextCompat.getColor(context, R.color.blow_gray))
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.line_bottom))
            icType.isSelected = false
        }
    }

    fun setSelected(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    fun collapse(position: Int) {
        val section = mDatas[position] as MWorkshopSection
        if (collapses[position] != null && collapses[position] == true) {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = false
            }
            collapses[position] = false
        } else {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = true
            }
            collapses[position] = true
        }
        notifyDataSetChanged()
    }
}