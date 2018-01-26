package com.haoyu.app.fragment

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.haoyu.app.activity.CourseDiscussDetailActivity
import com.haoyu.app.activity.CourseDiscussEditActivity
import com.haoyu.app.adapter.PageDiscussionAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.GoodView
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习讨论列表
 * 作者:xiaoma
 */
class PageDiscussionFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var btCreate: Button
    private var courseId: String? = null
    private var page = 1
    private var isLoadMore = false
    private var isRefresh = false
    private var mDatas = ArrayList<DiscussEntity>() // 讨论集合
    private lateinit var adapter: PageDiscussionAdapter
    private var itemIndex = -1

    override fun createView(): Int {
        return R.layout.fragment_page_discussion
    }

    override fun initView(view: View) {
        courseId = arguments?.getString("entityId")
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        tvEmpty = view.findViewById(R.id.tv_empty)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        btCreate = view.findViewById(R.id.bt_create)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        adapter = PageDiscussionAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        registRxBus()
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/m/discussion" + "?discussionRelations[0].relation.id=" + courseId + "&discussionRelations[0].relation.type=courseStudy" + "&page=" + page + "&orders=CREATE_TIME.DESC"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<DiscussListResult>() {
            override fun onBefore(request: Request) {
                if (isRefresh || isLoadMore) {
                    loadingView.visibility = View.GONE
                } else {
                    loadingView.visibility = View.VISIBLE
                }
            }

            override fun onError(request: Request, e: Exception) {
                when {
                    isRefresh -> xRecyclerView.refreshComplete(false)
                    isLoadMore -> {
                        page--
                        xRecyclerView.loadMoreComplete(false)
                    }
                    else -> {
                        loadingView.visibility = View.GONE
                        loadFailView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onResponse(response: DiscussListResult?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.getmDiscussions().size > 0) {
                    updateUI(response.responseData.getmDiscussions(), response.responseData.paginator)
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

    private fun updateUI(list: List<DiscussEntity>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE) {
            xRecyclerView.visibility = View.VISIBLE
        }
        if (isRefresh) {
            xRecyclerView.refreshComplete(true)
            mDatas.clear()
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(list)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        btCreate.setOnClickListener({
            val intent = Intent(context, CourseDiscussEditActivity::class.java)
            intent.putExtra("courseId", courseId)
            startActivity(intent)
        })
        adapter.setOnPartCallBack(object : PageDiscussionAdapter.OnPartCallBack {
            override fun createLink(position: Int, tv_like: TextView) {
                if (!mDatas[position].isSupport)
                    createLike(tv_like, position)
                else
                    toast("您已经点赞过")
            }

            override fun comment(position: Int) {
                val dialog = CommentDialog(context, "输入评论内容")
                dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
                    override fun sendComment(content: String) {
                        createComment(content, position)
                    }
                })
                dialog.show()
            }
        })
        adapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
            val selected = position - 1
            if (selected >= 0 && selected < mDatas.size) {
                itemIndex = selected
                val intent = Intent()
                intent.setClass(context, CourseDiscussDetailActivity::class.java)
                intent.putExtra("entity", mDatas[itemIndex])
                startActivity(intent)
            }
        }
    }

    /**
     * 创建观点（点赞）
     *
     * @param position
     */
    private fun createLike(tv_like: TextView, position: Int) {
        val url = Constants.OUTRT_NET + "/m/attitude"
        val entityId = mDatas[position].id
        val map = HashMap<String, String>()
        map.put("attitude", "support")
        map.put("relation.id", entityId)
        map.put("relation.type", "discussion")
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            override fun onError(request: Request, exception: Exception) {
                onNetWorkError()
            }

            override fun onResponse(response: AttitudeMobileResult?) {
                if (response != null && response.responseCode != null && response.responseCode == "00") {
                    if (mDatas[position].getmDiscussionRelations().size > 0) {
                        val supportNum = mDatas[position].getmDiscussionRelations()[0].supportNum + 1
                        mDatas[position].getmDiscussionRelations()[0].supportNum = supportNum
                        adapter.notifyDataSetChanged()
                    }
                    val goodView = GoodView(context)
                    val defaultColor = ContextCompat.getColor(context, R.color.defaultColor)
                    goodView.setTextInfo("+1", defaultColor, 15f)
                    goodView.show(tv_like)
                    mDatas[position].isSupport = true
                } else {
                    if (response?.responseMsg != null) {
                        mDatas[position].isSupport = true
                        toast("您已点赞过")
                    } else {
                        toast("点赞失败")
                    }
                }
            }
        }, map))
    }

    /**
     * 创建评论
     */
    private fun createComment(content: String, position: Int) {
        val entity = mDatas[position]
        if (entity.getmDiscussionRelations().size > 0) {
            val map = HashMap<String, String>()
            map.put("content", content)
            map.put("discussionUser.discussionRelation.id", entity.getmDiscussionRelations()[0].id)
            val url = Constants.OUTRT_NET + "/m/discussion/post"
            addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<ReplyResult>() {
                override fun onError(request: Request, exception: Exception) {
                    onNetWorkError()
                }

                override fun onResponse(response: ReplyResult?) {
                    if (response != null && response.responseCode != null && response.responseCode == "00") {
                        if (mDatas[position].getmDiscussionRelations().size > 0) {
                            val replyNum = mDatas[position].getmDiscussionRelations()[0].replyNum + 1
                            mDatas[position].getmDiscussionRelations()[0].replyNum = replyNum
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        response?.responseMsg?.let {
                            toast(it)
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
        val action = event.getAction()
        if (action == Action.CREATE_COURSE_DISCUSSION && event.obj != null && event.obj is DiscussEntity) {
            val entity = event.obj as DiscussEntity
            mDatas.add(0, entity)
            adapter.notifyDataSetChanged()
            if (xRecyclerView.visibility != View.VISIBLE) {
                xRecyclerView.visibility = View.VISIBLE
            }
            if (tvEmpty.visibility != View.GONE) {
                tvEmpty.visibility = View.GONE
            }
        } else if (action == Action.ALTER_COURSE_DISCUSSION && event.obj != null && event.obj is DiscussEntity) {
            val entity = event.obj as DiscussEntity
            mDatas[itemIndex] = entity
            adapter.notifyDataSetChanged()
        } else if (action == Action.DELETE_COURSE_DISCUSSION) {
            mDatas.removeAt(itemIndex)
            adapter.notifyDataSetChanged()
            if (mDatas.size <= 0) {
                tvEmpty.visibility = View.VISIBLE
                xRecyclerView.visibility = View.GONE
            }
        } else if (action == Action.CREATE_MAIN_REPLY) {
            if (mDatas[itemIndex].getmDiscussionRelations().size > 0) {
                val replyNum = mDatas[itemIndex].getmDiscussionRelations()[0].replyNum
                mDatas[itemIndex].getmDiscussionRelations()[0].replyNum = 1 + replyNum
                adapter.notifyDataSetChanged()
            }
        } else if (action == Action.CREATE_LIKE) {
            if (mDatas[itemIndex].getmDiscussionRelations().size > 0) {
                val supportNum = mDatas[itemIndex].getmDiscussionRelations()[0].supportNum + 1
                mDatas[itemIndex].getmDiscussionRelations()[0].supportNum = supportNum
                adapter.notifyDataSetChanged()
            }
        }
    }
}