package com.haoyu.app.activity


import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.adapter.PeerAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.entity.EducationConsultResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.Paginator
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.util.*

/*同行*/
class PeerActivity : BaseActivity(), XRecyclerView.LoadingListener {
    private val context = this
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tv_empty: TextView
    private lateinit var adapter: PeerAdapter
    private val mDatas = ArrayList<MobileUser>()
    private var isRefresh: Boolean = false
    private var isLoadMore: Boolean = false
    private var page = 1

    override fun setLayoutResID(): Int {
        return R.layout.activity_peer
    }

    override fun initView() {
        setToolBar()
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        xRecyclerView = findViewById(R.id.xRecyclerView)
        tv_empty = findViewById(R.id.tv_empty)
        val layoutManager = GridLayoutManager(this, 4)
        layoutManager.orientation = GridLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        adapter = PeerAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(context)
        loadFailView.setOnRetryListener { initData() }
    }

    private fun setToolBar() {
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setTitle_text("同行")
        toolBar.setOnLeftClickListener { finish() }
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/m/user?page=" + page + "&limit=30"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<EducationConsultResult>() {
            override fun onBefore(request: Request) {
                if (isRefresh || isLoadMore)
                    loadingView.visibility = View.GONE
                else
                    loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false)
                } else if (isLoadMore) {
                    page -= 1
                    xRecyclerView.loadMoreComplete(false)
                } else {
                    loadFailView.visibility = View.VISIBLE
                }
            }

            override fun onResponse(response: EducationConsultResult?) {
                loadingView.visibility = View.GONE
                if (response != null && response.responseData != null
                        && response.responseData.mUsers != null && response.responseData.mUsers.size > 0) {
                    updateUI(response.responseData.mUsers, response.responseData.paginator)
                } else {
                    if (isRefresh)
                        xRecyclerView.refreshComplete(true)
                    else if (isLoadMore)
                        xRecyclerView.loadMoreComplete(true)
                    else {
                        tv_empty.visibility = View.VISIBLE
                    }
                }
            }
        }))
    }

    private fun updateUI(mUsers: List<MobileUser>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE)
            xRecyclerView.visibility = View.VISIBLE
        if (isRefresh) {
            mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(mUsers)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
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
