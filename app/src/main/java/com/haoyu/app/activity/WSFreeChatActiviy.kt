package com.haoyu.app.activity

import android.app.Activity
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.haoyu.app.adapter.WSFreeChatAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.CommentEntity
import com.haoyu.app.entity.CommentListResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.Paginator
import com.haoyu.app.lego.student.R
import com.haoyu.app.swipe.OnActivityTouchListener
import com.haoyu.app.swipe.RecyclerTouchListener
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import java.lang.Exception
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * 创建日期：2018/1/29.
 * 描述:工作坊自由交流
 * 作者:xiaoma
 */
class WSFreeChatActiviy : BaseActivity(), XRecyclerView.LoadingListener, RecyclerTouchListener.RecyclerTouchListenerHelper {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvBottom: TextView
    private var relationId: String? = null
    private var roleInws: String? = null
    private var baseUrl: String? = null
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private var mDatas = ArrayList<CommentEntity>()
    private lateinit var adapter: WSFreeChatAdapter
    private var onTouchListener: RecyclerTouchListener? = null
    private var touchListener: OnActivityTouchListener? = null

    override fun setLayoutResID(): Int {
        return R.layout.activity_wsfreechat
    }

    override fun initView() {
        roleInws = intent.getStringExtra("role")
        relationId = intent.getStringExtra("relationId")
        toolBar = findViewById(R.id.toolBar)
        loadingView = findViewById(R.id.loadingView)
        xRecyclerView = findViewById(R.id.xRecyclerView)
        tvEmpty = findViewById(R.id.tv_empty)
        tvBottom = findViewById(R.id.tvBottom)
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = WSFreeChatAdapter(context, userId, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(context)
        onTouchListener = RecyclerTouchListener(context, xRecyclerView)
        xRecyclerView.addOnItemTouchListener(onTouchListener)
        baseUrl = "${Constants.OUTRT_NET}/${roleInws}_$relationId/m/comment?relation.id=$relationId"
    }

    override fun initData() {
        if (isRefresh || isLoadMore) loadingView.visibility = View.GONE
        else loadingView.visibility = View.VISIBLE
        val url = "$baseUrl&page=$page&orders=CREATE_TIME.DESC"
        Flowable.fromCallable {
            val gson = Gson()
            val json = OkHttpClientManager.getAsString(context, url)
            val result = gson.fromJson(json, CommentListResult::class.java)
            result?.responseData?.getmComments()?.let {
                for (i in 0 until it.size) {
                    val mainId = it[i].id
                    val url2 = "$baseUrl&mainId=$mainId&limit=5"
                    val json2 = OkHttpClientManager.getAsString(context, url2)
                    val result2 = gson.fromJson(json2, CommentListResult::class.java)
                    result2?.responseData?.getmComments()?.let {
                        result.responseData.getmComments()[i].childList = it
                    }
                }
            }
            result
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            loadingView.visibility = View.GONE
            if (it?.getResponseData()?.getmComments() != null && it.getResponseData().getmComments().size > 0) {
                updateUI(it.getResponseData().getmComments(), it.getResponseData().paginator)
            } else {
                when {
                    isRefresh -> xRecyclerView.refreshComplete(true)
                    isLoadMore -> xRecyclerView.loadMoreComplete(true)
                    else -> onEmptyData()
                }
                xRecyclerView.isLoadingMoreEnabled = false
            }
        }, {
            when {
                isRefresh -> xRecyclerView.refreshComplete(false)
                isLoadMore -> {
                    page -= 1
                    xRecyclerView.loadMoreComplete(false)
                }
                else -> {
                    val loadFailView = findViewById<LoadFailView>(R.id.loadFailView)
                    loadFailView.setOnRetryListener { initData() }
                }
            }
        })
    }

    private fun updateUI(mDatas: List<CommentEntity>, paginator: Paginator?) {
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

    private fun onEmptyData() {
        xRecyclerView.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        val text = "目前还没人参与交流，\n赶紧去发起您的疑问吧！"
        val ssb = SpannableString(text)
        val start = text.indexOf("去") + 1
        val end = text.indexOf("吧")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                showInputDialog()
            }
        }
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvEmpty.movementMethod = LinkMovementMethod.getInstance()
        tvEmpty.text = ssb
    }

    private fun showInputDialog() {
        val dialog = CommentDialog(context, "输入您的疑问", "发起")
        dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
            override fun sendComment(content: String) {
                createfc(content)
            }
        })
        dialog.show()
    }

    /*创建主自由交流*/
    private fun createfc(content: String) {
        val url = "${Constants.OUTRT_NET}/m/comment"
        val map = HashMap<String, String>().apply {
            put("content", content)
            relationId?.let { put("relation.id", it) }
            put("relation.type", "workshop_comment")
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<CommentEntity>?) {
                hideTipDialog()
                response?.responseData?.let {
                    if (tvEmpty.visibility != View.GONE) tvEmpty.visibility = View.GONE
                    if (xRecyclerView.visibility != View.VISIBLE) xRecyclerView.visibility = View.VISIBLE
                    val entity = it
                    entity.creator?.let {
                        entity.creator = getCreator(it)
                    }
                    mDatas.add(0, entity)
                    adapter.notifyDataSetChanged()
                }
            }
        }, map))
    }

    override fun setListener() {
        toolBar.setOnLeftClickListener { finish() }
        tvBottom.setOnClickListener { showInputDialog() }
        onTouchListener?.setIndependentViews(R.id.llDelete, R.id.llComment)?.setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                val selected = position - 1
                if (selected in 0 until mDatas.size) {
                    val intent = Intent(context, WSFreeChatDetailActivity::class.java)
                    intent.putExtra("relationId", relationId)
                    intent.putExtra("role", roleInws)
                    intent.putExtra("entity", mDatas[selected])
                    startActivityForResult(intent, 1)
                }
            }

            override fun onIndependentViewClicked(viewID: Int, position: Int) {
                when (viewID) {
                    R.id.llDelete -> {
                        val selected = position - 1
                        if (selected in 0 until mDatas.size) {
                            val dialog = MaterialDialog(context)
                            dialog.setTitle("提示")
                            dialog.setMessage("确定删除此回复吗？")
                            dialog.setNegativeButton("取消", null)
                            dialog.setPositiveButton("确定", { _, _ -> deletefc(selected) })
                            dialog.show()
                        }
                    }
                    R.id.llComment -> {
                        val selected = position - 1
                        if (selected in 0 until mDatas.size) {
                            val dialog = CommentDialog(context, "输入回复内容", "发送")
                            dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
                                override fun sendComment(content: String) {
                                    createcFc(content, selected)
                                }
                            })
                            dialog.show()
                        }
                    }
                }
            }
        })
    }

    /*删除主自由交流*/
    private fun deletefc(position: Int) {
        val url = "${Constants.OUTRT_NET}/m/comment/${mDatas[position].id}"
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
                    mDatas.removeAt(position)
                    adapter.notifyDataSetChanged()
                    if (mDatas.size == 0) {
                        onEmptyData()
                    }
                }
            }
        }, OkHttpClientManager.Param("_method", "delete")))
    }

    /*创建子自由交流*/
    private fun createcFc(content: String, position: Int) {
        val url = "${Constants.OUTRT_NET}/m/comment"
        val map = HashMap<String, String>().apply {
            put("content", content)
            relationId?.let { put("relation.id", it) }
            put("relation.type", "workshop_comment")
            put("mainId", mDatas[position].id)
            put("parentId", mDatas[position].id)
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            override fun onBefore(request: Request?) {
                showTipDialog()
            }

            override fun onError(request: Request?, e: Exception?) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<CommentEntity>?) {
                hideTipDialog()
                response?.responseData?.let {
                    val childNum = mDatas[position].childNum + 1
                    mDatas[position].childNum = childNum
                    if (mDatas[position].childList.size < 10) {
                        val entity = it
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas[position].childList.add(entity)
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val entity = data.getSerializableExtra("entity") as CommentEntity
            mDatas.remove(entity)
            adapter.notifyDataSetChanged()
            if (mDatas.size == 0) {
                onEmptyData()
            }
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

    override fun setOnActivityTouchListener(listener: OnActivityTouchListener) {
        this.touchListener = listener
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        touchListener?.getTouchCoordinates(ev)
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }
}