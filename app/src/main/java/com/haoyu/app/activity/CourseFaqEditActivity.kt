package com.haoyu.app.activity

import android.text.TextUtils
import android.view.View
import android.widget.EditText
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.entity.FAQsEntity
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import okhttp3.Request
import java.lang.Exception
import java.util.*

/**
 * 创建日期：2018/1/31.
 * 描述:课程学习创建问答
 * 作者:xiaoma
 */
class CourseFaqEditActivity : BaseActivity(){
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var etContent: EditText
    private var relationId: String? = null
    override fun setLayoutResID(): Int {
        return R.layout.activity_coursefaqedit
    }

    override fun initView() {
        relationId = intent.getStringExtra("relationId")
        toolBar = findViewById(R.id.toolBar)
        etContent = findViewById(R.id.et_content)
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View?) {
                finish()
            }

            override fun onRightClick(view: View?) {
                val content = etContent.text.toString().trim()
                if (TextUtils.isEmpty(content)) {
                    showMaterialDialog("提示", "请输入问题内容")
                } else {
                    build(content)
                }
            }
        })
    }

    private fun build(content: String) {
        val url = "${Constants.OUTRT_NET}/m/faq_question"
        val map = HashMap<String, String>().apply {
            relationId?.let { put("relation.id", it) }
            put("relation.type", "course_study")
            put("content", content)
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<FAQsEntity>>() {
            override fun onBefore(request: Request?) {
                showTipDialog()
            }

            override fun onError(request: Request?, e: Exception?) {
                hideTipDialog()
            }

            override fun onResponse(response: BaseResponseResult<FAQsEntity>?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    val entity = response.responseData
                    entity.creator = MobileUser().apply {
                        id = userId
                        avatar = context.avatar
                        realName = context.realName
                    }
                    val event = MessageEvent().apply {
                        action = Action.CREATE_FAQ_QUESTION
                        obj = entity
                    }
                    RxBus.getDefault().post(event)
                    finish()
                }
            }
        }, map))
    }
}