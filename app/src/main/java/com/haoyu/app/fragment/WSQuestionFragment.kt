package com.haoyu.app.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.activity.QuestionDetaiActivity
import com.haoyu.app.adapter.QuestionAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.swipe.RecyclerTouchListener
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.view.RippleView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

/**
 * 创建日期：2018/1/31.
 * 描述:工作坊问答列表
 * 作者:xiaoma
 */
class WSQuestionFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var activity: Activity
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var xRecyclerView: XRecyclerView
    private var type = 1
    private var relationId: String? = null
    private var relationType: String? = null
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private val mDatas = ArrayList<FAQsEntity>()
    private lateinit var adapter: QuestionAdapter
    private var baseUrl: String? = null
    private var totalCount = 0
    private var onResponseListener: OnResponseListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as Activity
        arguments?.let {
            type = it.getInt("type", 1)
            relationId = it.getString("relationId")
            relationType = it.getString("relationType")
        }
        baseUrl = "${Constants.OUTRT_NET}/m/faq_question?relation.id=$relationId&relation.type=$relationType&isLoadNewstAnswer=true&orders=CREATE_TIME.DESC"
        if (type == 2) baseUrl += "&creator.id=$userId"
    }

    override fun createView(): Int {
        return R.layout.fragment_question
    }

    override fun initView(view: View) {
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        tvEmpty = view.findViewById(R.id.tv_empty)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = QuestionAdapter(context, mDatas, 2)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        tvEmpty.text = if (type == 1) resources.getString(R.string.allQuestion) else resources.getString(R.string.myAskQuestion)
        registRxBus()
    }

    override fun initData() {
        val url = "$baseUrl&page=$page"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<FAQsListResult>() {
            override fun onBefore(request: Request?) {
                if (isRefresh || isLoadMore) loadingView.visibility = View.GONE
                else loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request?, e: Exception?) {
                loadingView.visibility = View.GONE
                when {
                    isRefresh -> xRecyclerView.refreshComplete(false)
                    isLoadMore -> {
                        page -= 1
                        xRecyclerView.loadMoreComplete(false)
                    }
                    else -> loadFailView.visibility = View.VISIBLE
                }
            }

            override fun onResponse(response: FAQsListResult?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.questions.size > 0) {
                    updateUI(response.responseData.questions, response.responseData.paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> {
                            xRecyclerView.loadMoreComplete(true)
                            xRecyclerView.isLoadingMoreEnabled = false
                        }
                        else -> {
                            xRecyclerView.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
                response?.responseData?.paginator?.let { totalCount = it.totalCount }
                onResponseListener?.getTotalCount(totalCount)
            }
        }))
    }

    private fun updateUI(mDatas: List<FAQsEntity>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE) xRecyclerView.visibility = View.VISIBLE
        when {
            isRefresh -> {
                this.mDatas.clear()
                xRecyclerView.refreshComplete(true)
            }
            isLoadMore -> xRecyclerView.loadMoreComplete(true)
        }
        this.mDatas.addAll(mDatas)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        val onTouchListener = RecyclerTouchListener(activity, xRecyclerView)
        onTouchListener.setIndependentViews(R.id.ivCollection, R.id.tvBottom).setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                val selected = position - 1
                if (selected in 0 until mDatas.size) {
                    val intent = Intent()
                    intent.setClass(context, QuestionDetaiActivity::class.java)
                    intent.putExtra("entity", mDatas[selected])
                    intent.putExtra("from", "workshop")
                    startActivity(intent)
                }
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
                val selected = position - 1
                when (independentViewID) {
                    R.id.ivCollection -> {
                        if (selected in 0 until mDatas.size) {
                            if (mDatas[selected].follow == null) {
                                onFollow(selected, 1)
                            } else {
                                onFollow(selected, 2)
                            }
                        }
                    }
                    R.id.tvBottom -> {
                        if (selected in 0 until mDatas.size) {
                            val dialog = CommentDialog(context, "输入答案", "提交")
                            dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
                                override fun sendComment(content: String) {
                                    onAnswer(content, selected)
                                }
                            })
                            dialog.show()
                        }
                    }
                }
            }
        }).setLongClickable(true) { position ->
            val selected = position - 1
            if (selected in 0 until mDatas.size) {
                mDatas[selected].creator?.id?.let {
                    if (it == userId) {
                        onDialog(selected)
                    }
                }
            }
        }
        xRecyclerView.addOnItemTouchListener(onTouchListener)
    }

    private fun onFollow(position: Int, type: Int) {
        val url = if (type == 1) "${Constants.OUTRT_NET}/m/follow" else "${Constants.OUTRT_NET}/m/follow/${mDatas[position].follow?.id}"
        val map = if (type == 1) HashMap<String, String>().apply {
            put("followEntity.id", mDatas[position].id)
            put("followEntity.type", "workshop_question")
        } else HashMap<String, String>().apply { put("_method", "delete") }
        if (type == 1) {     //创建关注
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<FollowMobileEntity>>() {
                override fun onError(request: Request?, e: Exception?) {
                    onNetWorkError()
                }

                override fun onResponse(response: BaseResponseResult<FollowMobileEntity>?) {
                    response?.responseData?.let {
                        mDatas[position].follow = it
                        adapter.notifyDataSetChanged()
                    }
                }
            }, map))
        } else {       //取消关注
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
                override fun onError(request: Request, exception: Exception) {
                    onNetWorkError()
                }

                override fun onResponse(response: BaseResponseResult<*>?) {
                    response?.responseCode?.let {
                        if (it == "00") {
                            mDatas[position].follow = null
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }, map))
        }
    }

    private fun onDialog(position: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, LinearLayout(context), false)
        val alertDialog = AlertDialog.Builder(context).create()
        val rvDelete = view.findViewById<RippleView>(R.id.rv_delete)
        val rvCancel = view.findViewById<RippleView>(R.id.rv_cancel)
        rvDelete.setOnRippleCompleteListener {
            val dialog = MaterialDialog(context)
            dialog.setTitle("提示")
            dialog.setMessage("确定删除此问答吗？")
            dialog.setNegativeButton("取消", null)
            dialog.setPositiveButton("确定", { _, _ -> delete(position) })
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

    private fun delete(position: Int) {
        val url = "${Constants.OUTRT_NET}/m/faq_question/${mDatas[position].id}"
        val map = HashMap<String, String>().apply { put("_method", "delete") }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError()
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    mDatas.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }
        }, map))
    }

    private fun onAnswer(content: String, position: Int) {
        val url = "${Constants.OUTRT_NET}/m/faq_answer"
        val map = HashMap<String, String>().apply {
            put("questionId", mDatas[position].id)
            put("content", content)
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<FAQsAnswerEntity>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError()
            }

            override fun onResponse(response: BaseResponseResult<FAQsAnswerEntity>?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    val entity = response.responseData
                    entity.creator?.let {
                        entity.creator = getCreator(it)
                    }
                    val count = mDatas[position].faqAnswerCount + 1
                    mDatas[position].faqAnswerCount = count
                    adapter.notifyDataSetChanged()
                }
            }
        }, map))
    }

    private fun getCreator(creator: MobileUser): MobileUser {
        creator.id = userId
        creator.avatar = avatar
        creator.realName = realName
        return creator
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

    override fun onEvent(event: MessageEvent) {
        if (event.getAction() == Action.CREATE_FAQ_QUESTION && event.obj != null && event.obj is FAQsEntity) {
            val entity = event.obj as FAQsEntity
            mDatas.add(0, entity)
            adapter.notifyDataSetChanged()
            if (xRecyclerView.visibility != View.VISIBLE) {
                xRecyclerView.visibility = View.VISIBLE
            }
            if (tvEmpty.visibility != View.GONE) {
                tvEmpty.visibility = View.GONE
            }
            onResponseListener?.getTotalCount(totalCount + 1)
        } else if (event.getAction() == Action.COLLECTION && event.obj != null && event.obj is FAQsEntity) {
            val entity = event.obj as FAQsEntity
            if (mDatas.indexOf(entity) >= 0) {
                val index = mDatas.indexOf(entity)
                mDatas[index] = entity
                adapter.notifyDataSetChanged()
            }
        } else if (event.getAction() == Action.DELETE_FAQ_QUESTION && event.obj != null && event.obj is FAQsEntity) {
            val entity = event.obj as FAQsEntity
            mDatas.remove(entity)
            adapter.notifyDataSetChanged()
            if (mDatas.size == 0) {
                xRecyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
            onResponseListener?.getTotalCount(totalCount - 1)
        } else if (event.getAction() == Action.CREATE_FAQ_ANSWER && event.obj != null && event.obj is FAQsEntity) {
            val entity = event.obj as FAQsEntity
            val index = mDatas.indexOf(entity)
            if (index >= 0) {
                val faqAnswerCount = mDatas[index].faqAnswerCount + 1
                mDatas[index].faqAnswerCount = faqAnswerCount
                adapter.notifyDataSetChanged()
            }
        }
    }

    interface OnResponseListener {
        fun getTotalCount(totalCount: Int)
    }

    fun setOnResponseListener(onResponseListener: OnResponseListener) {
        this.onResponseListener = onResponseListener
    }
}