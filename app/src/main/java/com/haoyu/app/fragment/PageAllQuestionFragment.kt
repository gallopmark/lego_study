package com.haoyu.app.fragment

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.activity.AppQuestionDetailActivity
import com.haoyu.app.activity.AppQuestionEditActivity
import com.haoyu.app.adapter.PageQuestionAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.FAQsEntity
import com.haoyu.app.entity.FAQsListResult
import com.haoyu.app.entity.FollowMobileResult
import com.haoyu.app.entity.Paginator
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
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

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习、工作坊  所有问答列表
 * 作者:xiaoma
 */
class PageAllQuestionFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private var type: String? = null
    private var relationId: String? = null
    private var relationType: String? = null
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private val mDatas = ArrayList<FAQsEntity>()
    private lateinit var adapter: PageQuestionAdapter
    private var totalCount: Int = 0
    private var onResponseListener: OnResponseListener? = null

    fun setOnResponseListener(onResponseListener: OnResponseListener) {
        this.onResponseListener = onResponseListener
    }

    override fun createView(): Int {
        return R.layout.fragment_page_question_child
    }

    override fun initView(view: View) {
        type = arguments?.getString("type")
        relationId = arguments?.getString("relationId")
        relationType = arguments?.getString("relationType")
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        tvEmpty = view.findViewById(R.id.tv_empty)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        adapter = if (type != null && type == "course") {
            PageQuestionAdapter(context, mDatas, 1)
        } else {
            PageQuestionAdapter(context, mDatas, 2)
        }
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        tvEmpty.text = resources.getString(R.string.empty_questions)
        registRxBus()
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/m/faq_question" + "?relation.id=" + relationId + "&relation.type=" + relationType + "&page=" + page + "&orders=CREATE_TIME.DESC" + "&isLoadNewstAnswer=true"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<FAQsListResult>() {
            override fun onBefore(request: Request?) {
                if (isRefresh || isLoadMore) {
                    loadingView.visibility = View.GONE
                } else {
                    loadingView.visibility = View.VISIBLE
                }
            }

            override fun onError(request: Request?, e: Exception?) {
                loadingView.visibility = View.GONE
                when {
                    isRefresh -> xRecyclerView.refreshComplete(false)
                    isLoadMore -> {
                        page -= 1
                        xRecyclerView.loadMoreComplete(false)
                    }
                    else -> {
                        xRecyclerView.visibility = View.GONE
                        loadFailView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onResponse(response: FAQsListResult?) {
                loadingView.visibility = View.GONE
                if (response?.getResponseData() != null && response.getResponseData().questions.size > 0) {
                    updateUI(response.getResponseData().questions, response.getResponseData().paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> xRecyclerView.loadMoreComplete(true)
                        else -> {
                            xRecyclerView.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                    xRecyclerView.isLoadingMoreEnabled = false
                }
            }
        }))
    }

    private fun updateUI(list: List<FAQsEntity>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE)
            xRecyclerView.visibility = View.VISIBLE
        if (isRefresh) {
            mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(list)
        adapter.notifyDataSetChanged()
        if (paginator == null) {
            xRecyclerView.isLoadingMoreEnabled = false
        } else {
            totalCount = paginator.totalCount
            xRecyclerView.isLoadingMoreEnabled = paginator.hasNextPage
        }
        onResponseListener?.getTotalCount(totalCount)
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        adapter.setCollectCallBack(object : PageQuestionAdapter.CollectCallBack {
            override fun collect(position: Int, entity: FAQsEntity) {
                collection(position)
            }

            override fun cancelCollect(position: Int, entity: FAQsEntity) {
                cancelCollection(position)
            }
        })
        adapter.setAnswerCallBack { _, entity ->
            val intent = Intent()
            intent.setClass(context, AppQuestionEditActivity::class.java)
            intent.putExtra("isAnswer", true)
            intent.putExtra("entity", entity)
            startActivity(intent)
        }
        adapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
            if (position - 1 >= 0 && position - 1 < mDatas.size) {
                val intent = Intent()
                intent.setClass(context, AppQuestionDetailActivity::class.java)
                intent.putExtra("type", type)
                intent.putExtra("entity", mDatas[position - 1])
                startActivity(intent)
            }
        }
        adapter.setOnItemLongClickListener { entity, _ ->
            if (entity.creator != null && entity.creator.id != null && entity.creator.id == userId)
                bottomDialog(entity)
        }
    }

    /**
     * 取消收藏
     *
     * @param position
     */
    private fun cancelCollection(position: Int) {
        val follow = mDatas[position].follow
        if (follow != null) {
            val url = Constants.OUTRT_NET + "/m/follow/" + follow.id
            val map = HashMap<String, String>()
            map.put("_method", "delete")
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
                override fun onError(request: Request, exception: Exception) {
                    onNetWorkError()
                }

                override fun onResponse(response: BaseResponseResult<*>?) {
                    if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                        mDatas[position].follow = null
                        adapter.notifyDataSetChanged()
                        val event = MessageEvent()
                        event.action = Action.COLLECTION
                        event.obj = mDatas[position]
                        RxBus.getDefault().post(event)
                    }
                }
            }, map))
        }
    }

    /**
     * 创建收藏
     *
     * @param position
     */
    private fun collection(position: Int) {
        val url = Constants.OUTRT_NET + "/m/follow"
        val map = HashMap<String, String>()
        map.put("followEntity.id", mDatas[position].id)
        map.put("followEntity.type", "course_study_question")
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<FollowMobileResult>() {
            override fun onError(request: Request, exception: Exception) {
                onNetWorkError()
            }

            override fun onResponse(response: FollowMobileResult?) {
                if (response != null && response.responseCode != null && response.responseCode == "00") {
                    val entity = response.responseData
                    mDatas[position].follow = entity
                    adapter.notifyDataSetChanged()
                    val event = MessageEvent()
                    event.action = Action.COLLECTION
                    event.obj = mDatas[position]
                    RxBus.getDefault().post(event)
                } else {
                    response?.responseMsg?.let {
                        toast(it)
                    }
                }
            }
        }, map))
    }

    private fun bottomDialog(entity: FAQsEntity) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, LinearLayout(context), false)
        val dialog = AlertDialog.Builder(context).create()
        val rvDelete = view.findViewById<RippleView>(R.id.rv_delete)
        val rvCancel = view.findViewById<RippleView>(R.id.rv_cancel)
        val listener = RippleView.OnRippleCompleteListener {
            when (it.id) {
                R.id.rv_delete -> deleteQuestion(entity)
                R.id.rv_cancel -> {
                }
            }
            dialog.dismiss()
        }
        rvDelete.setOnRippleCompleteListener(listener)
        rvCancel.setOnRippleCompleteListener(listener)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
        dialog.window?.let {
            it.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            it.setWindowAnimations(R.style.dialog_anim)
            it.setContentView(view)
            it.setGravity(Gravity.BOTTOM)
        }
    }

    private fun deleteQuestion(entity: FAQsEntity) {
        val dialog = MaterialDialog(context)
        dialog.setTitle("温馨提示")
        dialog.setMessage("您确定删除此问答吗？")
        dialog.setPositiveButton("确定") { _, _ ->
            val url = Constants.OUTRT_NET + "/m/faq_question/" + entity.id
            val map = HashMap<String, String>()
            map.put("_method", "delete")
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
                        val event = MessageEvent()
                        event.action = Action.DELETE_FAQ_QUESTION
                        event.obj = entity
                        RxBus.getDefault().post(event)
                    }
                }
            }, map))
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.setNegativeButton("取消", null)
        dialog.show()
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
            if (!xRecyclerView.isLoadingMoreEnabled) {
                mDatas.add(0, entity)
                adapter.notifyDataSetChanged()
            }
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
}