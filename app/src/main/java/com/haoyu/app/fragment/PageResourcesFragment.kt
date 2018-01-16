package com.haoyu.app.fragment

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.activity.MFileInfoActivity
import com.haoyu.app.adapter.PageResourcesAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.entity.CourseResources
import com.haoyu.app.entity.Paginator
import com.haoyu.app.entity.ResourcesEntity
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习资源列表
 * 作者:xiaoma
 */
class PageResourcesFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var xRecyclerView: XRecyclerView
    private val mDatas = ArrayList<ResourcesEntity>()
    private lateinit var adapter: PageResourcesAdapter
    private var courseId: String? = null
    private var isLoadMore = false
    private var isRefresh = false
    private var page = 1
    override fun createView(): Int {
        return R.layout.fragment_page_resources
    }

    override fun initView(view: View) {
        courseId = arguments?.getString("entityId")
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        tvEmpty = view.findViewById(R.id.tv_empty)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        adapter = PageResourcesAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
    }

    override fun initData() {
        val url = (Constants.OUTRT_NET + "/m/resource/ncts?resourceRelations[0].relation.id=" + courseId + "&resourceRelations[0].relation.type=course" + "&page=" + page
                + "&limit=20" + "&orders=CREATE_TIME.DESC")
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CourseResources>() {
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

            override fun onResponse(response: CourseResources?) {
                loadingView.visibility = View.GONE
                if (response?.getResponseData() != null && response.getResponseData().resources.size > 0) {
                    updateUI(response.getResponseData().resources, response.getResponseData().paginator)
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

    private fun updateUI(resources: List<ResourcesEntity>, paginator: Paginator?) {
        if (xRecyclerView.visibility != View.VISIBLE)
            xRecyclerView.visibility = View.VISIBLE
        if (isRefresh) {
            mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(resources)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        adapter.setCallBack { resourcesName, mFileInfo ->
            mFileInfo.fileName = resourcesName
            val intent = Intent(context, MFileInfoActivity::class.java)
            intent.putExtra("fileInfo", mFileInfo)
            startActivity(intent)
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

}