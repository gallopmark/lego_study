package com.haoyu.app.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.PixelFormat
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter


/**
 * 创建日期：2018/1/17.
 * 描述:课程学习教学课件文本编辑器
 * 作者:xiaoma
 */
class CoursewareEditorFragment : Fragment() {

    private var activity: Context? = null
    private var editor: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context
        editor = arguments?.getString("editor")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val tv = TextView(activity)
        tv.textSize = 16f
        val dp12 = PixelFormat.dp2px(context, 12f)
        tv.setPadding(dp12, dp12, dp12, dp12)
        tv.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        openEditor(tv)
        return tv
    }

    @Suppress("DEPRECATION")
    private fun openEditor(tv: TextView) {
        editor.isNullOrEmpty().let {
            val imageGetter = HtmlHttpImageGetter(tv, Constants.REFERER, true)
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(editor, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
            } else {
                Html.fromHtml(editor, imageGetter, null)
            }
            tv.movementMethod = LinkMovementMethod.getInstance()
            tv.text = spanned
        }
    }
}