package com.haoyu.app.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.TouchImageView

/**
 * 创建日期：2018/1/17.
 * 描述:图片预览
 * 作者:xiaoma
 */
class PictureViewerFragment : Fragment() {
    private lateinit var activity: Context
    private var url: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context
        url = arguments?.getString("url")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val iv = TouchImageView(context)
        iv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iv.setBackgroundColor(ContextCompat.getColor(activity, R.color.black))
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        iv.layoutParams = params
        loadImage(iv)
        return iv
    }

    private fun loadImage(iv: TouchImageView) {
        Glide.with(activity).load(url).placeholder(R.drawable.ic_placeholder).fitCenter().dontAnimate().into(iv)
    }
}