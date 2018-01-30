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
 * 创建日期：2018/1/30.
 * 描述:工作坊群列表适配器
 * 作者:xiaoma
 */
class WSGroupAdapter(private val context: Context, mDatas: List<WorkShopMobileEntity>) : BaseArrayRecyclerAdapter<WorkShopMobileEntity>(mDatas) {

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    init {
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - 20
        imageHeight = imageWidth / 3 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.wsgroup_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: WorkShopMobileEntity, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        val tvTitle = holder.obtainView<TextView>(R.id.tvTitle)
        val tvTime = holder.obtainView<TextView>(R.id.tvTime)
        val tvType = holder.obtainView<TextView>(R.id.tvType)
        ivImage.layoutParams = LinearLayout.LayoutParams(imageWidth, imageHeight)
        GlideImgManager.loadImage(context, entity.imageUrl, R.drawable.app_default, R.drawable.app_default, ivImage)
        tvTitle.text = entity.title
        tvTime.text = "${entity.studyHours}学时"
        if (entity.type == null) {
            tvType.visibility = View.GONE
        } else {
            tvType.visibility = View.VISIBLE
            when (entity.type) {
                "personal" -> tvType.text = "个人工作坊"
                "train" -> tvType.text = "项目工作坊"
                "template" -> tvType.text = "示范性工作坊"
                else -> tvType.text = "项目工作坊"
            }
        }
    }
}