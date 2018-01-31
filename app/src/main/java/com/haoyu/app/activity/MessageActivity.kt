package com.haoyu.app.activity

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.adapter.MessageAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.entity.Message
import com.haoyu.app.entity.Messages
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.Paginator
import com.haoyu.app.lego.student.R
import com.haoyu.app.swipe.RecyclerTouchListener
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.lang.Exception
import java.util.*

/**
 * 创建日期：2018/1/30.
 * 描述:消息
 * 作者:xiaoma
 */
class MessageActivity : BaseActivity(), XRecyclerView.LoadingListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private val mDatas = ArrayList<Message>()
    private lateinit var adapter: MessageAdapter

    override fun setLayoutResID(): Int {
        return R.layout.activity_message
    }

    override fun initView() {
        toolBar = findViewById(R.id.toolBar)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        xRecyclerView = findViewById(R.id.xRecyclerView)
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = MessageAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(context)
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/message?page=$page&limit=20"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<Messages>() {
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
                    else -> {
                        loadFailView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onResponse(response: Messages?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.getmMessages().size > 0) {
                    updateUI(response.responseData.getmMessages(), response.responseData.paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> {
                            xRecyclerView.loadMoreComplete(true)
                            xRecyclerView.isLoadingMoreEnabled = false
                        }
                        else -> {
                            val tvEmpty = findViewById<TextView>(R.id.tv_empty)
                            tvEmpty.text = "暂无消息~"
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }))
    }

    private fun updateUI(mDatas: List<Message>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE) xRecyclerView.visibility = View.VISIBLE
        when {
            isRefresh -> {
                this.mDatas.clear()
                xRecyclerView.refreshComplete(true)
            }
            isLoadMore -> {
                xRecyclerView.loadMoreComplete(true)
            }
        }
        this.mDatas.addAll(mDatas)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        toolBar.setOnLeftClickListener { finish() }
        loadFailView.setOnRetryListener { initData() }
        val onTouchListener = RecyclerTouchListener(context, xRecyclerView)
        onTouchListener.setIndependentViews(R.id.btReply).setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                val selected = position - 1
                if (selected in 0 until mDatas.size) {
                    val messageId = mDatas[selected].id
                    val intent = Intent(context, MessageDetailActivity::class.java).apply { putExtra("messageId", messageId) }
                    startActivity(intent)
                }
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
                val selected = position - 1
                if (selected in 0 until mDatas.size) {
                    val dialog = CommentDialog(context, "输入回复内容")
                    dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
                        override fun sendComment(content: String) {
                            val sender = mDatas[selected].sender
                            sendMessage(sender, content)
                        }
                    })
                    dialog.show()
                }
            }
        })
        xRecyclerView.addOnItemTouchListener(onTouchListener)
    }

    private fun sendMessage(replyWho: MobileUser, content: String) {  //发送消息
        val map = HashMap<String, String>().apply {
            put("sender.id", userId)
            put("receiver.id", replyWho.id)
            put("content", content)
        }
        val url = "${Constants.OUTRT_NET}/m/message"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request?) {
                showTipDialog()
            }

            override fun onError(request: Request?, e: Exception?) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.responseCode != null && response.responseCode == "00") {
                    toastFullScreen("回复成功", true)
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