package com.haoyu.app.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/25.
 * 描述:工作坊阶段任务适配器(坊主身份可编辑任务阶段)
 * 作者:xiaoma
 */
class WSTaskEditAdapter(private val context: Context, mDatas: List<MultiItemEntity>) : BaseArrayRecyclerAdapter<MultiItemEntity>(mDatas) {
    private val collapses = ArrayMap<Int, Boolean>()
    private var selected = -1
    private var sectionSize: Int = 0
    private var addTaskListener: OnEditTaskListener? = null

    fun addItemEntities(list: List<MultiItemEntity>) {
        this.mDatas.addAll(list)
        var index = 0
        for (i in mDatas.indices) {
            if (mDatas[i] is MWorkshopSection) {
                index++
                collapses[i] = i == 0
            }
        }
        this.sectionSize = index
        notifyDataSetChanged()
    }

    override fun addItem(entity: MultiItemEntity): Boolean {
        if (entity is MWorkshopSection) {
            entity.position = sectionSize
            sectionSize += 1
        } else if (entity is MWSSectionCrease) {
            sectionSize += 1
        }
        mDatas.add(entity)
        notifyDataSetChanged()
        return true
    }

    override fun addItem(position: Int, entity: MultiItemEntity): Boolean {
        mDatas.add(position, entity)
        notifyDataSetChanged()
        return true
    }

    fun addActivity(sectionIndex: Int, activity: MWorkshopActivity) {
        activity.isVisible = collapses[sectionIndex] != null && collapses[sectionIndex] == true
        val section = mDatas[sectionIndex] as MWorkshopSection
        activity.tag = section
        val index = sectionIndex + section.activities.size + 1
        section.activities.add(activity)
        mDatas.add(index, activity)
        notifyDataSetChanged()
    }

    fun removeActivity(position: Int) {
        val activity = mDatas.removeAt(position) as MWorkshopActivity
        val section = activity.tag
        section.activities.remove(activity)
        notifyDataSetChanged()
    }

    override fun removeItem(position: Int): Boolean {
        val entity = mDatas.removeAt(position)
        if (entity is MWorkshopSection) {
            sectionSize -= 1
            mDatas.removeAll(entity.activities)
            mDatas.remove(entity.crease)
            for (i in position until mDatas.size) {
                if (mDatas[i] is MWorkshopSection) {
                    val pos = (mDatas[i] as MWorkshopSection).position
                    (mDatas[i] as MWorkshopSection).position = pos - 1
                }
            }
        } else if (entity is MWSSectionCrease) {
            sectionSize -= 1
        }
        notifyDataSetChanged()
        return true
    }

    fun setOnEditTaskListener(addTaskListener: OnEditTaskListener) {
        this.addTaskListener = addTaskListener
    }

    override fun getItemViewType(position: Int): Int {
        return mDatas[position].itemType
    }

    override fun bindView(viewtype: Int): Int {
        return when (viewtype) {
            1 -> R.layout.wssection_item
            2 -> R.layout.wsactivity_item
            3 -> R.layout.wssection_edititem
            else -> R.layout.wstask_edititem
        }
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity, position: Int) {
        val viewType = holder.itemViewType
        when (viewType) {
            1 -> setViewType1(holder, entity, position)
            2 -> setViewType2(holder, entity, position)
            3 -> setViewType3(holder, entity)
            else -> setViewType4(holder, entity, position)
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
        tvTime.text = "研修时间：${if (section.timePeriod != null) {
            TimeUtil.getDateYM(section.timePeriod.startTime) + "-" + TimeUtil.getDateYM(section.timePeriod.endTime)
        } else ""}"
        if (collapses[position] != null && collapses[position] == true) {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = true
            }
            section.crease.isVisible = true
            ivExpand.setImageResource(R.drawable.ic_expand_more_black_24dp)
        } else {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = false
            }
            section.crease.isVisible = false
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
        if (TextUtils.isEmpty(activity.type)) {
            tvTypeName.text = "阶段任务"
            icType.setImageResource(R.drawable.ic_ws_def_selector)
        } else {
            when (activity.type) {
                "discussion" -> {
                    icType.setImageResource(R.drawable.ic_ws_discuss_selector)
                    tvTypeName.text = "教学研讨"
                }
                "survey" -> {
                    icType.setImageResource(R.drawable.ic_ws_survey_selector)
                    tvTypeName.text = "调查问卷"
                }
                "debate" -> {
                    icType.setImageResource(R.drawable.ic_ws_debate_selector)
                    tvTypeName.text = "在线辩论"
                }
                "lcec" -> {
                    icType.setImageResource(R.drawable.ic_ws_lcec_selector)
                    tvTypeName.text = "听课评课"
                }
                "lesson_plan" -> {
                    icType.setImageResource(R.drawable.ic_ws_lessonplan_selector)
                    tvTypeName.text = "集体备课"
                }
                "test" -> {
                    tvTypeName.text = "在线测验"
                }
                "video" -> {
                    icType.setImageResource(R.drawable.ic_ws_video_selector)
                    tvTypeName.text = "教学观摩"
                }
                "discuss_class" -> {
                    icType.setImageResource(R.drawable.ic_ws_video_selector)
                    tvTypeName.text = "教学观摩"
                }
                else -> {
                    icType.setImageResource(R.drawable.ic_ws_def_selector)
                    tvTypeName.text = "阶段任务"
                }
            }
        }
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

    private fun setViewType3(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity) {
        val tvAddTask = holder.obtainView<TextView>(R.id.tv_addTask)
        val llAddType = holder.obtainView<LinearLayout>(R.id.ll_add_type)
        val crease = entity as MWSActivityCrease
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (crease.isVisible) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        } else {
            params.height = 0
        }
        holder.itemView.layoutParams = params
        tvAddTask.setOnClickListener({
            if (llAddType.visibility == View.VISIBLE) {
                llAddType.visibility = View.GONE
            } else {
                llAddType.visibility = View.VISIBLE
            }
        })
    }

    private fun setViewType4(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MultiItemEntity, position: Int) {
        val tvPosition = holder.obtainView<TextView>(R.id.tv_position)
        val tvTitle = holder.obtainView<TextView>(R.id.tv_title)
        val tvTime = holder.obtainView<TextView>(R.id.tv_time)
        val tvConfirm = holder.obtainView<TextView>(R.id.tv_confirm)
        val tvCancel = holder.obtainView<TextView>(R.id.tv_cancel)
        val section = (entity as MWSSectionCrease).tag
        val stageIndex = if (section != null) section.position + 1 else sectionSize
        val isAdd = section == null
        tvTitle.text = section?.title
        tvTime.text = if (section != null) {
            "研修时间：${section.timePeriod?.let { TimeUtil.getDateYM(it.startTime) + "-" + TimeUtil.getDateYM(it.endTime) }}"
        } else null
        when {
            stageIndex < 10 -> tvPosition.text = "0$stageIndex"
            stageIndex in 10..99 -> tvPosition.text = stageIndex.toString()
            else -> tvPosition.text = ".."
        }
        tvTitle.setOnClickListener { addTaskListener?.inputTitle(tvTitle) }
        tvTime.setOnClickListener { addTaskListener?.inputTime(tvTime) }
        tvConfirm.setOnClickListener {
            if (isAdd) {
                addTaskListener?.addTask(tvTitle, tvTime, sectionSize + 1)
            } else {
                addTaskListener?.alertTask(tvTitle, tvTime, section, position)
            }
        }
        tvCancel.setOnClickListener {
            if (isAdd) {
                addTaskListener?.cancelAdd()
            } else {
                addTaskListener?.cancelAlert(section, position)
            }
        }
    }

    interface OnEditTaskListener {
        fun inputTitle(tvTitle: TextView)

        fun inputTime(tvTime: TextView)

        fun addTask(tvTitle: TextView, tvTime: TextView, sortNum: Int)

        fun alertTask(tvTitle: TextView, tvTime: TextView, tag: MWorkshopSection, position: Int)

        fun cancelAdd()

        fun cancelAlert(section: MWorkshopSection, position: Int)
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
            section.crease.isVisible = true
            collapses[position] = false
        } else {
            for (i in 0 until section.activities.size) {
                section.activities[i].isVisible = true
            }
            section.crease.isVisible = false
            collapses[position] = true
        }
        notifyDataSetChanged()
    }
}