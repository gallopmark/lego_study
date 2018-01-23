package com.haoyu.app.fragment

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.activity.CmtsLsonInfoActivity
import com.haoyu.app.adapter.CtmsLessonAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/18.
 * 描述:社区创课（全部、我的）
 * 作者:xiaoma
 */
class CmtsLsonChildFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private val mDatas = ArrayList<CmtsLessonEntity>()
    private lateinit var adapter: CtmsLessonAdapter
    private var isRefresh = false
    private var isLoadMore = false
    private var page = 1
    private var baseUrl: String? = null
    private var type = 1
    private var onResponseListener: OnResponseListener? = null
    override fun createView(): Int {
        return R.layout.fragment_cmtschild
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.getInt("type")?.let {
            type = it
        }
        baseUrl = Constants.OUTRT_NET + "/m/lesson/cmts/new?discussionRelations[0].relation.id=cmts&discussionRelations[0].relation.type=lesson&orders=CREATE_TIME.DESC"
        if (type == 2) {
            baseUrl += "&creator.id=" + userId
        }
    }

    override fun initView(view: View) {
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        adapter = CtmsLessonAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        tvEmpty.text = resources.getString(R.string.gen_class_emptylist)
        registRxBus()
    }

    override fun initData() {
        val url = baseUrl + "&page=" + page
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CmtsLessonEntities>() {
            override fun onBefore(request: Request) {
                if (isRefresh || isLoadMore) {
                    loadingView.visibility = View.GONE
                } else {
                    loadingView.visibility = View.VISIBLE
                }
            }

            override fun onError(request: Request, e: Exception) {
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

            override fun onResponse(response: CmtsLessonEntities?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.getmLessons().size > 0) {
                    updateUI(response.responseData.getmLessons(), response.responseData.paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> xRecyclerView.loadMoreComplete(true)
                        else -> {
                            xRecyclerView.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }))
    }

    private fun updateUI(list: List<CmtsLessonEntity>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE) xRecyclerView.visibility = View.VISIBLE
        if (isRefresh) {
            mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(list)
        adapter.notifyDataSetChanged()
        var totalCount = 0
        paginator?.let {
            totalCount = it.totalCount
        }
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
        onResponseListener?.getTotalCount(totalCount)
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        adapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
            val selected = position - 1
            if (selected >= 0 && selected < mDatas.size) {
                val lessonId = mDatas[selected].id
                val intent = Intent(context, CmtsLsonInfoActivity::class.java)
                intent.putExtra("lessonId", lessonId)
                startActivity(intent)
            }
        }
        adapter.setRequestClickCallBack(object : CtmsLessonAdapter.RequestClickCallBack {
            override fun support(entity: CmtsLessonEntity, position: Int) {
                if (entity.isSupport) toast("您已点赞过")
                else createLike(position)
            }

            override fun giveAdvice(entity: CmtsLessonEntity, position: Int) {
                showInputDialog(position)
            }
        })
    }

    private fun createLike(position: Int) {
        val url = Constants.OUTRT_NET + "/m/attitude"
        val entityId = mDatas[position].id
        val map = HashMap<String, String>()
        map["attitude"] = "support"
        map["relation.id"] = entityId
        map["relation.type"] = "discussion"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            override fun onError(request: Request, exception: Exception) {
                onNetWorkError()
            }

            override fun onResponse(response: AttitudeMobileResult?) {
                if (response?.responseCode != null && response.responseCode == "00") {
                    if (mDatas[position].getmDiscussionRelations().size > 0) {
                        val supportNum = mDatas[position].getmDiscussionRelations()[0].supportNum + 1
                        mDatas[position].getmDiscussionRelations()[0].supportNum = supportNum
                        adapter.notifyDataSetChanged()
                    }
                    mDatas[position].isSupport = true
                } else if (response?.responseMsg != null) {
                    mDatas[position].isSupport = true
                    toast("您已点赞过")
                } else {
                    toast("点赞失败")
                }
            }
        }, map))
    }

    private fun showInputDialog(position: Int) {
        val dialog = CommentDialog(context)
        dialog.show()
        dialog.setSendCommentListener { content -> giveAdvice(content, position) }
    }

    private fun giveAdvice(content: String, position: Int) {
        if (mDatas[position].getmDiscussionRelations().size > 0) {
            val relationId = mDatas[position].getmDiscussionRelations()[0].id
            val url = Constants.OUTRT_NET + "/m/discussion/post"
            val map = HashMap<String, String>()
            map["discussionUser.discussionRelation.id"] = relationId
            map["content"] = content
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<ReplyResult>() {
                override fun onBefore(request: Request) {
                    showTipDialog()
                }


                override fun onError(request: Request, e: Exception) {
                    hideTipDialog()
                    onNetWorkError()
                }

                override fun onResponse(response: ReplyResult?) {
                    hideTipDialog()
                    response?.responseData?.let {
                        if (mDatas[position].getmDiscussionRelations().size > 0) {
                            val replyNum = mDatas[position].getmDiscussionRelations()[0].replyNum + 1
                            mDatas[position].getmDiscussionRelations()[0].replyNum = replyNum
                            adapter.notifyDataSetChanged()
                            toastFullScreen("发表成功", true)
                        }
                    }
                }
            }, map))
        }
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
        if (event.getAction() == Action.CREATE_GEN_CLASS && event.obj != null && event.obj is CmtsLessonEntity) {  //创建创课
            if (xRecyclerView.visibility == View.GONE) {
                xRecyclerView.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
            }
            val entity = event.obj as CmtsLessonEntity
            mDatas.add(0, entity)
            adapter.notifyDataSetChanged()
        } else if (event.getAction() == Action.DELETE_GEN_CLASS) {   //删除创课
            if (event.obj != null && event.obj is CmtsLessonEntity) {
                val entity = event.obj as CmtsLessonEntity
                mDatas.remove(entity)
                adapter.notifyDataSetChanged()
            }
            if (mDatas.size == 0) {
                xRecyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
        } else if (event.getAction() == Action.SUPPORT_STUDY_CLASS) {    //创课点赞
            if (event.obj != null && event.obj is CmtsLessonEntity) {
                val entity = event.obj as CmtsLessonEntity
                val selected = mDatas.indexOf(entity)
                if (selected != -1) {
                    mDatas[selected] = entity
                    adapter.notifyDataSetChanged()
                }
            }
        } else if (event.getAction() == Action.GIVE_STUDY_ADVICE) {   //创课提建议
            if (event.obj != null && event.obj is CmtsLessonEntity) {
                val entity = event.obj as CmtsLessonEntity
                val selected = mDatas.indexOf(entity)
                if (selected != -1) {
                    mDatas[selected] = entity
                    adapter.notifyDataSetChanged()
                }
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