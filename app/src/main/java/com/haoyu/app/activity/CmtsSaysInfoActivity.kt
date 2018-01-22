package com.haoyu.app.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.haoyu.app.adapter.AppDiscussionAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.utils.*
import com.haoyu.app.view.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import java.util.*

/**
 * 创建日期：2018/1/18.
 * 描述:教研话题详情
 * 作者:xiaoma
 */
class CmtsSaysInfoActivity : BaseActivity() {
    private lateinit var context: CmtsSaysInfoActivity
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var ssvContent: StickyScrollView
    private lateinit var btSupport: Button
    private lateinit var replyRV: RecyclerView  //评论列表
    private lateinit var tvEmptyComment: TextView
    private lateinit var tvMoreReply: TextView
    private val mDatas = ArrayList<ReplyEntity>()
    private lateinit var adapter: AppDiscussionAdapter
    private var replyUrl: String? = null
    private lateinit var discussEntity: DiscussEntity
    private lateinit var discussionId: String
    private lateinit var relationId: String  //研说id,研说关系Id
    private var supportNum = 0
    private var replyNum = 0 //点赞数,评论数
    private var childPosition = 0
    private var replyPosition = 0

    override fun setLayoutResID(): Int {
        return R.layout.activity_cmtssaysinfo
    }

    override fun initView() {
        context = this
        discussionId = intent.getStringExtra("discussionId")
        val title = resources.getString(R.string.study_says_detail)
        val emptyText = resources.getString(R.string.study_says_emptylist)
        findViews()
        toolBar.setTitle_text(title)
        tvEmpty.text = emptyText
        adapter = AppDiscussionAdapter(context, mDatas, userId)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        replyRV.isNestedScrollingEnabled = false
        replyRV.layoutManager = layoutManager
        replyRV.adapter = adapter
        registRxBus()
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        tvEmpty = findViewById(R.id.tv_empty)
        ssvContent = findViewById(R.id.ssv_content)
        btSupport = findViewById(R.id.bt_support)
        replyRV = findViewById(R.id.replyRV)
        tvEmptyComment = findViewById(R.id.tv_emptyComment)
        tvMoreReply = findViewById(R.id.tv_more_reply)
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/m/discussion/cmts/view/" + discussionId
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<DiscussResult>() {
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(result: DiscussResult?) {
                loadingView.visibility = View.GONE
                if (result?.responseData != null) {
                    updateUI(result.responseData)
                } else {
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(entity: DiscussEntity) {
        ssvContent.visibility = View.VISIBLE
        discussEntity = entity
        showData(discussEntity)
        if (discussEntity.creator != null && discussEntity.creator.id != null && discussEntity.creator.id == userId) {
            toolBar.setShow_right_button(true)
        }
        if (discussEntity.getmDiscussionRelations().size > 0) {
            relationId = discussEntity.getmDiscussionRelations()[0].id
        }
        replyUrl = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + relationId + "&orders=CREATE_TIME.ASC"
        getReply()
    }

    @Suppress("DEPRECATION")
    private fun showData(entity: DiscussEntity) {
        entity.creator?.let {
            val ivUserIco = findViewById<ImageView>(R.id.iv_userIco)
            GlideImgManager.loadCircleImage(context, it.avatar, R.drawable.user_default, R.drawable.user_default, ivUserIco)
            val tvUserName = findViewById<TextView>(R.id.tv_userName)
            tvUserName.text = it.realName
        }
        val tvCreateTime = findViewById<TextView>(R.id.tv_createTime)
        val slashDate = TimeUtil.getSlashDate(entity.createTime)
        tvCreateTime.text = "发布于$slashDate"
        if (entity.getmDiscussionRelations().size > 0) {
            supportNum = entity.getmDiscussionRelations()[0].supportNum
            val tvViewNum = findViewById<TextView>(R.id.tv_viewNum)
            tvViewNum.text = entity.getmDiscussionRelations()[0].browseNum.toString()
            btSupport.text = "赞($supportNum)"
        }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = entity.title
        entity.content?.let {
            val tvContent = findViewById<TextView>(R.id.tv_content)
            val imageGetter = HtmlHttpImageGetter(tvContent, Constants.REFERER, true)
            val spanned = Html.fromHtml(entity.content, imageGetter, null)
            tvContent.movementMethod = LinkMovementMethod.getInstance()
            tvContent.text = spanned
        }
    }

    private fun getReply() {
        tvMoreReply.visibility = View.GONE
        val url = "$replyUrl&limit=5"
        addSubscription(Flowable.fromCallable {
            var json = OkHttpClientManager.getAsString(context, url)
            val gson = GsonBuilder().create()
            val result = gson.fromJson(json, ReplyListResult::class.java)
            result?.getResponseData()?.getmDiscussionPosts()?.let {
                for (i in 0 until it.size) {
                    try {
                        val url2 = "$replyUrl&mainPostId=${it[i].id}"
                        json = OkHttpClientManager.getAsString(context, url2)
                        val mResult = gson.fromJson(json, ReplyListResult::class.java)
                        if (mResult?.getResponseData()?.getmDiscussionPosts() != null) {
                            it[i].childReplyEntityList = mResult.responseData.getmDiscussionPosts()
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
            result
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            if (it?.responseData?.getmDiscussionPosts() != null) {
                updateUI(it.responseData.getmDiscussionPosts(), it.responseData.paginator)
            } else {
                lfv()
            }
        }, { lfv() }))
    }

    /*显示评论列表*/
    private fun updateUI(list: List<ReplyEntity>, paginator: Paginator?) {
        mDatas.clear()
        if (list.isNotEmpty()) {
            mDatas.addAll(list)
            adapter.notifyDataSetChanged()
            if (paginator != null) {
                if (paginator.hasNextPage) {
                    tvMoreReply.visibility = View.VISIBLE
                }
                replyNum = paginator.totalCount
            }
            replyRV.visibility = View.VISIBLE
        } else {
            replyRV.visibility = View.GONE
            setEmptyText()
        }
        setReplyText(replyNum)
        setBottomView()
    }

    private fun setReplyText(replyNum: Int) {
        val tvCommentNum = findViewById<TextView>(R.id.tv_commentNum)
        tvCommentNum.text = "共有 $replyNum 条评论"
    }

    private fun setEmptyText() {
        tvEmptyComment.visibility = View.VISIBLE
        val text = "目前还没人参与评论，\n赶紧去发表您的评论吧！"
        val ssb = SpannableString(text)
        val start = text.indexOf("去") + 1
        val end = text.indexOf("吧")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                showCommentDialog(false)
            }
        }
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvEmptyComment.movementMethod = LinkMovementMethod.getInstance()
        tvEmptyComment.text = ssb
    }

    private fun setBottomView() {
        val bottomView = findViewById<TextView>(R.id.bottomView)
        bottomView.visibility = View.VISIBLE
        bottomView.setOnClickListener({ showCommentDialog(false) })
    }

    private fun lfv() {
        val lfv = findViewById<LoadFailView>(R.id.lfv)
        lfv.visibility = View.VISIBLE
        lfv.setOnRetryListener { getReply() }
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                showBottomDialog()
            }
        })
        loadFailView.setOnRetryListener { initData() }
        btSupport.setOnClickListener({ createLike() })
        tvMoreReply.setOnClickListener({
            val intent = Intent(context, AppMoreMainReplyActivity::class.java)
            intent.putExtra("type", "comment")
            intent.putExtra("relationId", relationId)
            startActivity(intent)
        })
        adapter.setOnPostClickListener(object : AppDiscussionAdapter.OnPostClickListener {
            override fun onTargetClick(view: View, position: Int, entity: ReplyEntity) {

            }

            override fun onChildClick(view: View, position: Int) {
                childPosition = position
                showCommentDialog(true)
            }
        })

        adapter.setMoreReplyCallBack({ entity, position ->
            replyPosition = position
            val intent = Intent(context, AppMoreChildReplyActivity::class.java)
            intent.putExtra("entity", entity)
            intent.putExtra("relationId", relationId)
            startActivity(intent)
        })

        adapter.setSupportCallBack({ position, _ -> createLike(position) })

        adapter.setDeleteMainReply({ id, position -> deleteReply(id, position) })
    }

    private fun showCommentDialog(sendChild: Boolean) {
        val dialog = CommentDialog(context, "请输入评论内容")
        dialog.show()
        dialog.setSendCommentListener { content ->
            if (sendChild) {
                sendChildReply(childPosition, content)
            } else {
                sendMainReply(content)
            }
        }
    }

    /* 创建观点（点赞） */
    private fun createLike() {
        val url = Constants.OUTRT_NET + "/m/attitude"
        val map = HashMap<String, String>()
        map["attitude"] = "support"
        map["relation.id"] = discussionId
        map["relation.type"] = "discussion"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: AttitudeMobileResult?) {
                hideTipDialog()
                if (response != null && response.responseCode == "00") {
                    supportNum++
                    btSupport.text = "赞($supportNum)"
                    val goodView = GoodView(context)
                    val defaultColor = ContextCompat.getColor(context, R.color.defaultColor)
                    goodView.setTextInfo("+1", defaultColor, 16f)
                    goodView.show(btSupport)
                    val event = MessageEvent()
                    event.action = Action.SUPPORT_STUDY_SAYS
                    if (discussEntity.getmDiscussionRelations().size > 0) {
                        discussEntity.getmDiscussionRelations()[0].supportNum = supportNum
                    }
                    event.obj = discussEntity
                    RxBus.getDefault().post(event)
                } else {
                    if (response?.responseMsg != null) {
                        toast(context, "您已点赞过")
                    }
                }
            }
        }, map))
    }

    /*创建子回复*/
    private fun sendChildReply(position: Int, content: String) {
        val map = HashMap<String, String>()
        map["content"] = content
        map["mainPostId"] = mDatas[position].id
        map["discussionUser.discussionRelation.id"] = relationId
        val url = Constants.OUTRT_NET + "/m/discussion/post"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<ReplyResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, exception: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: ReplyResult?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    val childPostCount = mDatas[position].childPostCount + 1
                    mDatas[position].childPostCount = childPostCount
                    if (mDatas[position].childReplyEntityList != null && mDatas[position].childReplyEntityList.size < 10) {
                        val entity = response.responseData
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas[position].childReplyEntityList.add(entity)
                    } else {
                        toastFullScreen("评论成功", true)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    toastFullScreen("评论失败", false)
                }
            }
        }, map))
    }

    /*创建主回复*/
    private fun sendMainReply(content: String) {
        val map = HashMap<String, String>()
        map["content"] = content
        map["discussionUser.discussionRelation.id"] = relationId
        val url = Constants.OUTRT_NET + "/m/discussion/post"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<ReplyResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
            }

            override fun onResponse(response: ReplyResult?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    replyRV.visibility = View.VISIBLE
                    tvEmptyComment.visibility = View.GONE
                    if (mDatas.size < 5) {
                        val entity = response.responseData
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas.add(entity)
                        adapter.notifyDataSetChanged()
                    } else {
                        tvMoreReply.visibility = View.VISIBLE
                        toastFullScreen("评论成功", true)
                    }
                    val event = MessageEvent()
                    event.action = Action.CREATE_MAIN_REPLY
                    if (discussEntity.getmDiscussionRelations().size > 0) {
                        val replyNum = discussEntity.getmDiscussionRelations()[0].replyNum + 1
                        discussEntity.getmDiscussionRelations()[0].replyNum = replyNum
                    }
                    event.obj = discussEntity
                    RxBus.getDefault().post(event)
                    replyNum++
                    setReplyText(replyNum)
                } else {
                    toastFullScreen("评论失败", false)
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

    /* 创建评论列表点赞观点(点赞)**/
    private fun createLike(position: Int) {
        val url = Constants.OUTRT_NET + "/m/attitude"
        val relationId = mDatas[position].id
        val map = HashMap<String, String>()
        map["attitude"] = "support"
        map["relation.id"] = relationId
        map["relation.type"] = "discussion_post"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            override fun onError(request: Request, exception: Exception) {
                onNetWorkError(context)
            }

            override fun onResponse(response: AttitudeMobileResult?) {
                if (response?.responseCode != null && response.responseCode == "00") {
                    val count = mDatas[position].supportNum + 1
                    mDatas[position].supportNum = count
                    adapter.notifyDataSetChanged()
                } else if (response?.responseMsg != null) {
                    toast(context, "您已点赞过")
                } else {
                    toast(context, "点赞失败")
                }
            }
        }, map))
    }

    private fun deleteReply(id: String, position: Int) {
        val url = Constants.OUTRT_NET + "/m/discussion/post/" + id
        val map = HashMap<String, String>()
        map["_method"] = "delete"
        map["discussionUser.discussionRelation.id"] = relationId
        map["mainPostId"] = id
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    mDatas.removeAt(position)
                    adapter.notifyDataSetChanged()
                    if (mDatas.size == 0) {
                        replyRV.visibility = View.GONE
                        tvEmptyComment.visibility = View.VISIBLE
                    }
                    replyNum--
                    setReplyText(replyNum)
                    getReply()
                }
            }
        }, map))
    }

    override fun obBusEvent(event: MessageEvent) {
        if (event.action == Action.CREATE_MAIN_REPLY && event.obj != null && event.obj is ReplyEntity) {
            replyNum++
            setReplyText(replyNum)
            val entity = event.obj as ReplyEntity
            if (mDatas.size < 5) {
                mDatas.add(entity)
                adapter.notifyDataSetChanged()
            }
        } else if (event.action == Action.CREATE_CHILD_REPLY) {
            /*来自更多评论列表界面创建子回复*/
            if (event.bundle != null && event.bundle.getSerializable("mainReply") != null
                    && event.bundle.getSerializable("mainReply") is ReplyEntity) {
                val mainReply = event.bundle.getSerializable("mainReply") as ReplyEntity
                val position = mDatas.indexOf(mainReply)
                if (position != -1 && event.bundle.getSerializable("childReply") != null
                        && event.bundle.getSerializable("childReply") is ReplyEntity) {
                    val childReply = event.bundle.getSerializable("childReply") as ReplyEntity
                    val childPostNum = mDatas[position].childPostCount + 1
                    mDatas[position].childPostCount = childPostNum
                    if (mDatas[position].childReplyEntityList != null && mDatas[position].childReplyEntityList.size < 10) {
                        mDatas[position].childReplyEntityList.add(childReply)
                    }
                    adapter.notifyDataSetChanged()
                }
            } else {   //来自更多回复列表创建回复
                val childPostNum = mDatas[replyPosition].childPostCount + 1
                mDatas[replyPosition].childPostCount = childPostNum
                adapter.notifyDataSetChanged()
            }
        } else if (event.action == Action.CREATE_LIKE) {
            if (event.obj != null && event.obj is ReplyEntity) {
                val entity = event.obj as ReplyEntity
                if (mDatas.indexOf(entity) != -1) {
                    mDatas[mDatas.indexOf(entity)] = entity
                    adapter.notifyDataSetChanged()
                }
            } else {
                val supportNum = mDatas[replyPosition].supportNum
                mDatas[replyPosition].supportNum = supportNum + 1
                adapter.notifyDataSetChanged()
            }
        } else if (event.action == Action.DELETE_MAIN_REPLY && event.obj != null && event.obj is ReplyEntity) {
            replyNum--
            setReplyText(replyNum)
            val entity = event.obj as ReplyEntity
            if (mDatas.contains(entity)) {
                mDatas.remove(entity)
                adapter.notifyDataSetChanged()
                getReply()
            }
        }
    }

    private fun showBottomDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_delete, LinearLayout(context), false)
        val dialog = AlertDialog.Builder(context).create()
        val rvDelete = view.findViewById<RippleView>(R.id.rv_delete)
        val rvCancel = view.findViewById<RippleView>(R.id.rv_cancel)
        rvDelete.setOnRippleCompleteListener({
            showTipsDialog()
            dialog.dismiss()
        })
        rvCancel.setOnRippleCompleteListener({ dialog.dismiss() })
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
        dialog.window?.let {
            it.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT)
            it.setWindowAnimations(R.style.dialog_anim)
            it.setContentView(view)
            it.setGravity(Gravity.BOTTOM)
        }
    }

    private fun showTipsDialog() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("你确定删除吗？")
        dialog.setNegativeButton("确定") { _, _ -> deleteSay() }
        dialog.setPositiveButton("取消", null)
        dialog.show()
    }

    /**
     * 删除研说
     */
    private fun deleteSay() {
        val url = Constants.OUTRT_NET + "/m/discussion/cmts/" + discussionId
        val map = HashMap<String, String>()
        map["_method"] = "delete"
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    val event = MessageEvent()
                    event.action = Action.DELETE_STUDY_SAYS
                    event.obj = discussEntity
                    RxBus.getDefault().post(event)
                    toastFullScreen("已成功删除，返回首页", true)
                    Handler().postDelayed({ finish() }, 3000)
                } else {
                    toast(context, "删除失败")
                }
            }
        }, map))

    }

}