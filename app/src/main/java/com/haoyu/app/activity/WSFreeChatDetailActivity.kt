package com.haoyu.app.activity

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.adapter.WSFreeChatChildAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.CommentEntity
import com.haoyu.app.entity.CommentListResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.Paginator
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request
import java.lang.Exception
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * 创建日期：2018/1/29.
 * 描述:工作坊自由交流评论详细
 * 作者:xiaoma
 */
class WSFreeChatDetailActivity : BaseActivity(), XRecyclerView.LoadingListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tvBottom: TextView
    private val mDatas = ArrayList<CommentEntity>()
    private lateinit var adapter: WSFreeChatChildAdapter
    private var roleInws: String? = null
    private var relationId: String? = null
    private lateinit var entity: CommentEntity
    private var count = 0
    private var page = 1
    private var isRefresh = false
    private var isLoadMore = false

    override fun setLayoutResID(): Int {
        return R.layout.activity_wsfreechatdetail
    }

    override fun initView() {
        roleInws = intent.getStringExtra("role")
        relationId = intent.getStringExtra("relationId")
        entity = intent.getSerializableExtra("entity") as CommentEntity
        toolBar = findViewById(R.id.toolBar)
        xRecyclerView = findViewById(R.id.xRecyclerView)
        tvBottom = findViewById(R.id.tvBottom)
        if (entity.creator?.id.equals(userId)) toolBar.setShow_right_button(true)
        val header = layoutInflater.inflate(R.layout.wsfreechat_header, LinearLayout(context), false)
        initHeader(header)
        xRecyclerView.addHeaderView(header)
        xRecyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = WSFreeChatChildAdapter(context, userId, mDatas)
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(context)
    }

    private fun initHeader(header: View) {
        header.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val tvContent = header.findViewById<TextView>(R.id.tvContent)
        val icUser = header.findViewById<ImageView>(R.id.icUser)
        val tvName = header.findViewById<TextView>(R.id.tvName)
        val tvTime = header.findViewById<TextView>(R.id.tvTime)
        tvCount = header.findViewById(R.id.tvCount)
        tvEmpty = header.findViewById(R.id.tvEmpty)
        tvContent.text = entity.content
        if (entity.creator != null) {
            GlideImgManager.loadCircleImage(context, entity.creator.avatar, R.drawable.user_default, R.drawable.user_default, icUser)
            tvName.text = entity.creator.realName
        } else {
            icUser.setImageResource(R.drawable.user_default)
            tvName.text = null
        }
        tvTime.text = "发表于${TimeUtil.converTime(entity.createTime)}"
        count = entity.childNum
        setCountText()
    }

    private fun setCountText() {
        tvCount.text = "评论($count)"
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/${roleInws}_$relationId/m/comment?relation.id=$relationId&page=$page&mainId=${entity.id}&limit=20"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CommentListResult>() {
            override fun onBefore(request: Request?) {
                if (!isRefresh && !isLoadMore) {
                    showTipDialog()
                }
            }

            override fun onError(request: Request?, e: Exception?) {
                hideTipDialog()
                when {
                    isRefresh -> xRecyclerView.refreshComplete(false)
                    isLoadMore -> {
                        page -= 1
                        xRecyclerView.loadMoreComplete(false)
                    }
                    else -> {
                        onNetWorkError(context)
                    }
                }
            }

            override fun onResponse(response: CommentListResult?) {
                hideTipDialog()
                if (response?.responseData != null && response.responseData.getmComments().size > 0) {
                    updateUI(response.responseData.getmComments(), response.responseData.paginator)
                } else {
                    when {
                        isRefresh -> xRecyclerView.refreshComplete(true)
                        isLoadMore -> xRecyclerView.loadMoreComplete(true)
                        else -> tvEmpty.visibility = View.VISIBLE
                    }
                    xRecyclerView.isLoadingMoreEnabled = false
                }
            }
        }))
    }

    private fun updateUI(mDatas: List<CommentEntity>, paginator: Paginator?) {
        when {
            isRefresh -> {
                this.mDatas.clear()
                xRecyclerView.refreshComplete(true)
            }
            isLoadMore -> xRecyclerView.loadMoreComplete(true)
        }
        this.mDatas.addAll(mDatas)
        adapter.notifyDataSetChanged()
        paginator?.let {
            count = it.totalCount
            setCountText()
        }
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View?) {
                finish()
            }

            override fun onRightClick(view: View?) {
                val dialog = MaterialDialog(context)
                dialog.setTitle("提示")
                dialog.setMessage("确定删除此回复吗？")
                dialog.setNegativeButton("取消", null)
                dialog.setPositiveButton("确定", { _, _ ->
                    deletefc()
                })
                dialog.show()
            }
        })
        adapter.setOnDeleteListener(object : WSFreeChatChildAdapter.OnDeleteListener {
            override fun onDelete(position: Int) {
                deleteCFc(position)
            }
        })
        tvBottom.setOnClickListener { showInputDialog() }
    }

    /*删除主自由交流*/
    private fun deletefc() {
        val url = "${Constants.OUTRT_NET}/m/comment/${entity.id}"
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
                    val intent = Intent().apply { putExtra("entity", entity) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }, OkHttpClientManager.Param("_method", "delete")))
    }

    private fun deleteCFc(position: Int) {
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
                    count -= 1
                    setCountText()
                    if (mDatas.size == 0) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }, OkHttpClientManager.Param("_method", "delete")))
    }

    private fun showInputDialog() {
        val dialog = CommentDialog(context, "输入回复内容", "发送")
        dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
            override fun sendComment(content: String) {
                createcFc(content)
            }
        })
        dialog.show()
    }

    /*创建子自由交流*/
    private fun createcFc(content: String) {
        val url = "${Constants.OUTRT_NET}/m/comment"
        val map = HashMap<String, String>().apply {
            put("content", content)
            relationId?.let { put("relation.id", it) }
            put("relation.type", "workshop_comment")
            put("mainId", entity.id)
            put("parentId", entity.id)
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
                    count += 1
                    setCountText()
                    if (tvEmpty.visibility != View.GONE) tvEmpty.visibility = View.GONE
                    if (xRecyclerView.isLoadingMoreEnabled) {
                        toastFullScreen("发送成功", true)
                    } else {
                        val entity = it
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas.add(entity)
                        adapter.notifyDataSetChanged()
                    }
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