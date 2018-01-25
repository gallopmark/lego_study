package com.haoyu.app.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.CourseMobileEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.ScreenUtils

/**
 * 创建日期：2018/1/25.
 * 描述:我的培训课程列表适配器
 * 作者:xiaoma
 */
class MyTrainCourseAdapter(private val context: Context, mDatas: List<CourseMobileEntity>)
    : BaseArrayRecyclerAdapter<CourseMobileEntity>(mDatas) {
    private var width: Int = 0
    private var height: Int = 0

    init {
        width = ScreenUtils.getScreenWidth(context) / 3 - 20
        height = width / 3 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.mytraincourse_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: CourseMobileEntity, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        val tvTitle = holder.obtainView<TextView>(R.id.tvTitle)
        val tvType = holder.obtainView<TextView>(R.id.tvType)
        val tvPeriod = holder.obtainView<TextView>(R.id.tvPeriod)
        val tvEnroll = holder.obtainView<TextView>(R.id.tvEnroll)
        val divider = holder.obtainView<View>(R.id.divider)
        ivImage.layoutParams = LinearLayout.LayoutParams(width, height)
        GlideImgManager.loadImage(context, entity.image, R.drawable.app_default, R.drawable.app_default, ivImage)
        tvTitle.text = entity.title
        tvType.text = entity.type
        tvPeriod.text = "${entity.studyHours}学时"
        tvEnroll.text = "${entity.registerNum}人报读"
        if (position != itemCount - 1) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }
}