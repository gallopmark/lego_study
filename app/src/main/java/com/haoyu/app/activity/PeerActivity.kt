package com.haoyu.app.activity


import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.adapter.PeerAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.MobileUserData
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
    private lateinit var tvEmpty: TextView
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
        tvEmpty = findViewById(R.id.tv_empty)
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
        val url = "${Constants.OUTRT_NET}/m/user?page=$page&limit=30"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUserData>>() {
            override fun onBefore(request: Request) {
                if (isRefresh || isLoadMore)
                    loadingView.visibility = View.GONE
                else
                    loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
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

            override fun onResponse(response: BaseResponseResult<MobileUserData>?) {
                loadingView.visibility = View.GONE
                if (response?.responseData?.getmUsers() != null && response.responseData.getmUsers().size > 0) {
                    updateUI(response.responseData.getmUsers(), response.responseData.paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> xRecyclerView.loadMoreComplete(true)
                        else -> tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }))
    }

    private fun updateUI(mUsers: List<MobileUser>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE)
            xRecyclerView.visibility = View.VISIBLE
        when {
            isRefresh -> {
                mDatas.clear()
                xRecyclerView.refreshComplete(true)
            }
            isLoadMore -> {
                xRecyclerView.loadMoreComplete(true)
            }
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
