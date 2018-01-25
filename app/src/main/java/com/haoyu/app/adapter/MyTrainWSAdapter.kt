package com.haoyu.app.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.WorkShopMobileEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.ScreenUtils

/**
 * 创建日期：2018/1/25.
 * 描述:我的培训工作坊列表适配器
 * 作者:xiaoma
 */
class MyTrainWSAdapter(private val context: Context, mDatas: List<WorkShopMobileEntity>)
    : BaseArrayRecyclerAdapter<WorkShopMobileEntity>(mDatas) {
    private var width: Int = 0
    private var height: Int = 0

    init {
        width = ScreenUtils.getScreenWidth(context) / 3 - 20
        height = width / 3 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.mytrainws_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: WorkShopMobileEntity, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        val tvTitle = holder.obtainView<TextView>(R.id.tvTitle)
        val tvPeriod = holder.obtainView<TextView>(R.id.tvPeriod)
        val tvScore = holder.obtainView<TextView>(R.id.tvScore)
        val divider = holder.obtainView<View>(R.id.divider)
        ivImage.layoutParams = LinearLayout.LayoutParams(width, height)
        GlideImgManager.loadImage(context, entity.imageUrl, R.drawable.app_default, R.drawable.app_default, ivImage)
        tvTitle.text = entity.title
        tvPeriod.text = "${entity.studyHours}学时"
        tvScore.text = "获得${entity.point.toInt()}/${entity.qualifiedPoint}积分"
        if (position != itemCount - 1) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }
}