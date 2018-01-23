package com.haoyu.app.fragment

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.haoyu.app.activity.CmtsMovInfoActivity
import com.haoyu.app.adapter.CtmsMovementAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.entity.CmtsMovRegister
import com.haoyu.app.entity.CmtsMovement
import com.haoyu.app.entity.CmtsMovements
import com.haoyu.app.entity.Paginator
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
 * 描述:社区活动（全部、我的）
 * 作者:xiaoma
 */
class CmtsMovChildFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvEmpty: TextView
    private val mDatas = ArrayList<CmtsMovement>()
    private lateinit var adapter: CtmsMovementAdapter
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
        baseUrl = Constants.OUTRT_NET + "/m/movement?movementRelations[0].relation.id=cmts&movementRelations[0].relation.type=movement&orders=CREATE_TIME.DESC"
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
        adapter = CtmsMovementAdapter(context, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        tvEmpty.text = resources.getString(R.string.gen_class_emptylist)
        registRxBus()
    }

    override fun initData() {
        val url = baseUrl + "&page=" + page
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CmtsMovements>() {
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

            override fun onResponse(response: CmtsMovements?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null && response.responseData.getmMovements().size > 0) {
                    updateUI(response.responseData.getmMovements(), response.responseData.paginator)
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

    private fun updateUI(list: List<CmtsMovement>, paginator: Paginator?) {
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
                val movementId = mDatas[selected].id
                val intent = Intent(context, CmtsMovInfoActivity::class.java)
                intent.putExtra("movementId", movementId)
                startActivity(intent)
            }
        }
        adapter.setOnButtonClick(object : CtmsMovementAdapter.OnButtonClick {
            override fun onClick(position: Int, entity: CmtsMovement) {
                val movementId = entity.id
                val intent = Intent(context, CmtsMovInfoActivity::class.java)
                intent.putExtra("movementId", movementId)
                startActivity(intent)
            }

            override fun register(position: Int, activityId: String) {
                registerActivity(position, activityId)
            }

            override fun unregister(position: Int, registerId: String) {
                unRegisterActivity(position, registerId)
            }

        })
    }

    private fun registerActivity(position: Int, movementId: String) {
        val url = "${Constants.OUTRT_NET}/m/movement/register"
        val map = HashMap<String, String>()
        map["movement.id"] = movementId
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<CmtsMovRegister>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError()
            }

            override fun onResponse(response: CmtsMovRegister?) {
                hideTipDialog()
                response?.responseData?.let {
                    mDatas[position].getmMovementRegisters().add(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }, map))
    }

    private fun unRegisterActivity(position: Int, registerId: String) {
        val url = "${Constants.OUTRT_NET}/m/movement/register/$registerId"
        val map = HashMap<String, String>()
        map["_method"] = "delete"
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
                    mDatas[position].getmMovementRegisters().clear()
                    adapter.notifyDataSetChanged()
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

    override fun onEvent(event: MessageEvent) {
        if (event.getAction() == Action.DELETE_MOVEMENT) {   //删除活动
            if (event.obj != null && event.obj is CmtsMovement) {
                val entity = event.obj as CmtsMovement
                mDatas.remove(entity)
                adapter.notifyDataSetChanged()
            }
            if (mDatas.size == 0) {
                xRecyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
        } else if (event.getAction() == Action.REGIST_MOVEMENT) {   //活动报名
            if (event.obj != null && event.obj is CmtsMovement) {
                val entity = event.obj as CmtsMovement
                val selected = mDatas.indexOf(entity)
                if (selected != -1) {
                    mDatas[selected] = entity
                    adapter.notifyDataSetChanged()
                }
            }
        } else if (event.getAction() == Action.UNREGIST_MOVEMENT) {  //取消活动报名
            if (event.obj != null && event.obj is CmtsMovement) {
                val entity = event.obj as CmtsMovement
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