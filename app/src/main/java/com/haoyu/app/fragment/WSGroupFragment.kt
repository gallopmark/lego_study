package com.haoyu.app.fragment

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.activity.WSHomePageActivity
import com.haoyu.app.adapter.WSGroupAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.lang.Exception

/**
 * 创建日期：2018/1/30.
 * 描述:工作坊群列表
 * 作者:xiaoma
 */
class WSGroupFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private var type = 1
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false
    private var baseUrl: String? = null
    private val mDatas = ArrayList<WorkShopMobileEntity>()
    private val wsUserMap = HashMap<String, WorkShopMobileUser>()
    private lateinit var adapter: WSGroupAdapter
    private var onResponseListener: OnResponseListener? = null

    override fun createView(): Int {
        return R.layout.fragment_wsgroup
    }

    override fun initView(view: View) {
        arguments?.let { type = it.getInt("type", 1) }
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        baseUrl = when (type) {
            1 -> "${Constants.OUTRT_NET}/m/workshop?type=my"
            else -> "${Constants.OUTRT_NET}/m/workshop?type=all"
        }
        adapter = WSGroupAdapter(context, mDatas)
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        tvEmpty.text = "没有工作坊~"
        loadFailView.setOnRetryListener { initData() }
    }

    override fun initData() {
        val url = "$baseUrl&page=$page"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<WorkShopListResult>() {
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

            override fun onResponse(response: WorkShopListResult?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.getmWorkshops().size > 0) {
                    updateUI(response.responseData.getmWorkshops(), response.responseData.getmWorkshopUserMap(), response.responseData.paginator)
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
            }
        }))
    }

    private fun updateUI(mDatas: List<WorkShopMobileEntity>, userMap: Map<String, WorkShopMobileUser>, paginator: Paginator?) {
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
        wsUserMap.putAll(userMap)
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
        paginator?.let { onResponseListener?.getTotalCount(it.totalCount) }
    }

    override fun setListener() {
        adapter.setOnItemClickListener { _, _, _, position ->
            val selected = position - 1
            if (selected in 0 until mDatas.size) {
                when (type) {
                    1 -> {
                        mDatas[selected].relation?.id?.let { getTrainInfo(it, mDatas[selected]) }
                    }
                    else -> {
                        val entity = mDatas[selected]
                        if (wsUserMap[entity.id]?.role != null) {
                            entity.relation?.id?.let { getTrainInfo(it, entity) }
                        } else {
                            showDialog()
                        }
                    }
                }
            }
        }
    }

    private fun getTrainInfo(id: String, entity: WorkShopMobileEntity) {
        val url = "${Constants.OUTRT_NET}/m/train/$id"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<MyTrainMobileEntity>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
            }

            override fun onResponse(response: BaseResponseResult<MyTrainMobileEntity>?) {
                hideTipDialog()
                val intent = Intent(context, WSHomePageActivity::class.java)
                intent.putExtra("workshopId", entity.id)
                intent.putExtra("workshopTitle", entity.title)
                response?.responseData?.getmTrainingTime()?.state?.let {
                    if (it == "进行中") intent.putExtra("training", true)
                }
                response?.responseData?.getmTrainingTime()?.minutes?.let {
                    if (it > 0) intent.putExtra("training", true)
                }
                startActivity(intent)
            }
        }))
    }

    private fun showDialog() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("您不是坊内成员无权查看内容，请查看与我相关的工作坊")
        dialog.setPositiveButton("我知道了", null)
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

    interface OnResponseListener {
        fun getTotalCount(totalCount: Int)
    }

    fun setOnResponseListener(onResponseListener: OnResponseListener) {
        this.onResponseListener = onResponseListener
    }
}