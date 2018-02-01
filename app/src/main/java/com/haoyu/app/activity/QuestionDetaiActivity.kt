package com.haoyu.app.activity

import android.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.adapter.QuestionAnswerAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.swipe.RecyclerTouchListener
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.RippleView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.lang.Exception

/**
 * 创建日期：2018/1/31.
 * 描述:工作坊互助问答详情
 * 作者:xiaoma
 */
class QuestionDetaiActivity : BaseActivity(), XRecyclerView.LoadingListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvBottom: TextView
    private lateinit var entity: FAQsEntity
    private var from: String? = null
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private val mDatas = ArrayList<FAQsAnswerEntity>()
    private lateinit var adapter: QuestionAnswerAdapter

    override fun setLayoutResID(): Int {
        return R.layout.activity_questiondetail
    }

    override fun initView() {
        entity = intent.getSerializableExtra("entity") as FAQsEntity
        from = intent.getStringExtra("from")
        toolBar = findViewById(R.id.toolBar)
        xRecyclerView = findViewById(R.id.xRecyclerView)
        tvBottom = findViewById(R.id.tvBottom)
        entity.creator?.id?.let {
            if (it == userId) {
                toolBar.setShow_right_button(true)
            }
        }
        addHeadView()
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = QuestionAnswerAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(context)
    }

    private fun addHeadView() {
        val headView = layoutInflater.inflate(R.layout.question_header, LinearLayout(context), false)
        headView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val ivIco = headView.findViewById<ImageView>(R.id.ivIco)
        val tvName = headView.findViewById<TextView>(R.id.tvName)
        val tvQuestion = headView.findViewById<TextView>(R.id.tvQuestion)
        tvEmpty = headView.findViewById(R.id.tvEmpty)
        GlideImgManager.loadCircleImage(context, entity.creator?.avatar, R.drawable.user_default, R.drawable.user_default, ivIco)
        tvName.text = entity.creator?.realName
        from?.let {
            if (it == "course") {
                val tvCollection = headView.findViewById<TextView>(R.id.tvCollection)
                tvCollection.visibility = View.VISIBLE
                tvCollection.text = if (entity.follow != null) "取消收藏" else "收藏"
                tvCollection.setOnClickListener {
                    if (entity.follow == null) {
                        onFollow(1, tvCollection, 1)
                    } else {
                        onFollow(2, tvCollection, 1)
                    }
                }
            } else {
                val ivCollection = headView.findViewById<ImageView>(R.id.ivCollection)
                ivCollection.visibility = View.VISIBLE
                if (entity.follow != null) {
                    ivCollection.setImageResource(R.drawable.workshop_collect_press)
                } else {
                    ivCollection.setImageResource(R.drawable.workshop_collect_default)
                }
                ivCollection.setOnClickListener {
                    if (entity.follow == null) {
                        onFollow(1, ivCollection, 2)
                    } else {
                        onFollow(2, ivCollection, 2)
                    }
                }
            }
        }
        tvQuestion.text = entity.content
        xRecyclerView.addHeaderView(headView)
    }

    private fun onFollow(type: Int, view: View, viewType: Int) {
        val url = if (type == 1) "${Constants.OUTRT_NET}/m/follow" else "${Constants.OUTRT_NET}/m/follow/${entity.follow?.id}"
        val map = if (type == 1) HashMap<String, String>().apply {
            put("followEntity.id", entity.id)
            put("followEntity.type", "workshop_question")
        } else HashMap<String, String>().apply { put("_method", "delete") }
        if (type == 1) {     //创建关注
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<FollowMobileEntity>>() {
                override fun onError(request: Request?, e: Exception?) {
                    onNetWorkError(context)
                }

                override fun onResponse(response: BaseResponseResult<FollowMobileEntity>?) {
                    response?.responseData?.let {
                        entity.follow = it
                        if (viewType == 1) {
                            (view as TextView).text = "取消收藏"
                        } else {
                            (view as ImageView).setImageResource(R.drawable.workshop_collect_press)
                        }
                        sendMessage()
                    }
                }
            }, map))
        } else {       //取消关注
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
                override fun onError(request: Request, exception: Exception) {
                    onNetWorkError(context)
                }

                override fun onResponse(response: BaseResponseResult<*>?) {
                    response?.responseCode?.let {
                        if (it == "00") {
                            entity.follow = null
                            if (viewType == 1) {
                                (view as TextView).text = "收藏"
                            } else {
                                (view as ImageView).setImageResource(R.drawable.workshop_collect_default)
                            }
                            sendMessage()
                        }
                    }
                }
            }, map))
        }
    }

    private fun sendMessage() {
        val event = MessageEvent()
        event.action = Action.COLLECTION
        event.obj = entity
        RxBus.getDefault().post(event)
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/faq_answer?questionId=${entity.id}&page=$page&orders=CREATE_TIME.AS"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<FAQsAnswers>() {
            override fun onBefore(request: Request) {
                super.onBefore(request)
                if (!isRefresh && !isLoadMore) {
                    showTipDialog()
                }
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false)
                } else if (isLoadMore) {
                    page -= 1
                    xRecyclerView.loadMoreComplete(false)
                }
            }

            override fun onResponse(response: FAQsAnswers?) {
                hideTipDialog()
                if (response?.getResponseData() != null && response.getResponseData().answers.size > 0) {
                    updateUI(response.getResponseData().answers, response.getResponseData().paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> xRecyclerView.loadMoreComplete(true)
                        else -> {
                            xRecyclerView.isLoadingMoreEnabled = false
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }))
    }

    private fun updateUI(mDatas: List<FAQsAnswerEntity>, paginator: Paginator?) {
        if (isRefresh) {
            this.mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        this.mDatas.addAll(mDatas)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View?) {
                finish()
            }

            override fun onRightClick(view: View?) {
                onDialog()
            }
        })
        val onTouchListener = RecyclerTouchListener(context, xRecyclerView)
        onTouchListener.setLongClickable(true) { position ->
            val selected = position - 2
            if (selected in 0 until mDatas.size) {
                mDatas[selected].creator?.id?.let {
                    if (it == userId) {
                        onDialog(selected)
                    }
                }
            }
        }
        xRecyclerView.addOnItemTouchListener(onTouchListener)
        tvBottom.setOnClickListener {
            val dialog = CommentDialog(context, "输入答案", "提交")
            dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
                override fun sendComment(content: String) {
                    onAnswer(content)
                }
            })
            dialog.show()
        }
    }

    private fun onDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, LinearLayout(context), false)
        val alertDialog = AlertDialog.Builder(context).create()
        val rvDelete = view.findViewById<RippleView>(R.id.rv_delete)
        val rvCancel = view.findViewById<RippleView>(R.id.rv_cancel)
        rvDelete.setOnRippleCompleteListener {
            val dialog = MaterialDialog(context)
            dialog.setTitle("提示")
            dialog.setMessage("确定删除此问答吗？")
            dialog.setNegativeButton("取消", null)
            dialog.setPositiveButton("确定", { _, _ -> delete() })
            dialog.show()
        }
        rvCancel.setOnRippleCompleteListener { alertDialog.dismiss() }
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.show()
        alertDialog.window?.let {
            it.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            it.setWindowAnimations(R.style.dialog_anim)
            it.setContentView(view)
            it.setGravity(Gravity.BOTTOM)
        }
    }

    private fun delete() {
        val url = "${Constants.OUTRT_NET}/m/faq_question/${entity.id}"
        val map = HashMap<String, String>().apply { put("_method", "delete") }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    val event = MessageEvent().apply {
                        action = Action.DELETE_FAQ_QUESTION
                        obj = entity
                    }
                    RxBus.getDefault().post(event)
                    finish()
                }
            }
        }, map))
    }

    private fun onDialog(position: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, LinearLayout(context), false)
        val alertDialog = AlertDialog.Builder(context).create()
        val rvDelete = view.findViewById<RippleView>(R.id.rv_delete)
        val rvCancel = view.findViewById<RippleView>(R.id.rv_cancel)
        rvDelete.setOnRippleCompleteListener {
            val dialog = MaterialDialog(context)
            dialog.setTitle("提示")
            dialog.setMessage("确定删除此答案吗？")
            dialog.setNegativeButton("取消", null)
            dialog.setPositiveButton("确定", { _, _ -> deleteAnswer(position) })
            dialog.show()
        }
        rvCancel.setOnRippleCompleteListener { alertDialog.dismiss() }
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.show()
        alertDialog.window?.let {
            it.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            it.setWindowAnimations(R.style.dialog_anim)
            it.setContentView(view)
            it.setGravity(Gravity.BOTTOM)
        }
    }

    private fun deleteAnswer(position: Int) {
        val url = "${Constants.OUTRT_NET}/m/faq_answer/${mDatas[position].id}"
        val map = HashMap<String, String>().apply { put("_method", "delete") }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, exception: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    mDatas.removeAt(position)
                    adapter.notifyDataSetChanged()
                    if (mDatas.size == 0) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }, map))
    }

    private fun onAnswer(content: String) {
        val url = "${Constants.OUTRT_NET}/m/faq_answer"
        val map = HashMap<String, String>().apply {
            put("questionId", entity.id)
            put("content", content)
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<FAQsAnswerEntity>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<FAQsAnswerEntity>?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    val entity = response.responseData
                    entity.creator = MobileUser().apply {
                        id = userId
                        avatar = context.avatar
                        realName = context.realName
                    }
                    mDatas.add(0, entity)
                    adapter.notifyDataSetChanged()
                    if (tvEmpty.visibility != View.GONE) tvEmpty.visibility = View.GONE
                }
            }
        }, map))
    }

    override fun onRefresh() {
        isRefresh = true
        isLoadMore = false
        page = 1
        initData()
    }

    override fun onLoadMore() {
        isRefresh = false
        isLoadMore = true
        page += 1
        initData()
    }
}