package com.haoyu.app.activity

import android.app.AlertDialog
import android.content.Intent
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
import com.haoyu.app.adapter.AppCommentAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.FileUploadDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.pickerlib.MediaOption
import com.haoyu.app.pickerlib.MediaPicker
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.utils.*
import com.haoyu.app.view.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 创建日期：2018/1/19.
 * 描述:社区活动详情
 * 作者:xiaoma
 */
class CmtsMovInfoActivity : BaseActivity() {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var ssvContent: StickyScrollView
    private lateinit var videoRV: RecyclerView
    private lateinit var llEmptyFiles: LinearLayout
    private lateinit var commentRV: RecyclerView  //评论列表
    private lateinit var tvEmptyComment: TextView
    private lateinit var tvMoreReply: TextView
    private lateinit var btType: Button
    private lateinit var movementId: String
    private lateinit var movEntity: CmtsMovement
    private val fileInfos = ArrayList<MFileInfo>()
    private lateinit var fileAdapter: FileAdapter
    private var viewNum = 0
    private var participateNum = 0
    private var limit = 0  //活动参与数,限额数
    private val mDatas = ArrayList<CommentEntity>()
    private lateinit var adapter: AppCommentAdapter
    private var replyPosition = 0
    private var childPosition = 0
    private var register = false
    private var registerId: String? = null
    private var commentUrl: String? = null
    override fun setLayoutResID(): Int {
        return R.layout.activity_cmtsmovinfo
    }

    override fun initView() {
        movementId = intent.getStringExtra("movementId")
        findViews()
        val emptyText = resources.getString(R.string.teach_active_emptylist)
        tvEmpty.text = emptyText
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.HORIZONTAL
        videoRV.layoutManager = llm
        fileAdapter = FileAdapter(fileInfos)
        videoRV.adapter = fileAdapter
        adapter = AppCommentAdapter(context, mDatas, userId)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        commentRV.isNestedScrollingEnabled = false
        commentRV.layoutManager = layoutManager
        commentRV.adapter = adapter
        registRxBus()
        commentUrl = "${Constants.OUTRT_NET}/m/comment?relation.id=$movementId&relation.type=movement&orders=CREATE_TIME.DESC"
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        tvEmpty = findViewById(R.id.tv_empty)
        ssvContent = findViewById(R.id.ssv_content)
        videoRV = findViewById(R.id.videoRV)
        llEmptyFiles = findViewById(R.id.llEmptyFiles)
        commentRV = findViewById(R.id.commentRV)
        tvEmptyComment = findViewById(R.id.tv_emptyComment)
        tvMoreReply = findViewById(R.id.tv_more_reply)
        btType = findViewById(R.id.bt_type)
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/movement/view/$movementId"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<CmtsMovement>>() {
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(result: BaseResponseResult<CmtsMovement>?) {
                loadingView.visibility = View.GONE
                if (result?.getResponseData() != null) {
                    updateUI(result.getResponseData())
                } else {
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(entity: CmtsMovement) {
        movEntity = entity
        ssvContent.visibility = View.VISIBLE
        if (movEntity.creator?.id != null && movEntity.creator.id == userId) {
            toolBar.setShow_right_button(true)
        }
        setImage(movEntity.image)
        setTopText(movEntity)
        setContentText(movEntity.content)
        setFileInfos(movEntity.getmFileInfos())
        setBottomText(movEntity)
        getComment()
    }

    private fun setImage(url: String?) {
        val imageView = findViewById<ImageView>(R.id.iv_image)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = ScreenUtils.getScreenHeight(context) / 7 * 2
        val layoutParams = LinearLayout.LayoutParams(width, height)
        imageView.layoutParams = layoutParams
        GlideImgManager.loadImage(context, url, R.drawable.app_default, R.drawable.app_default, imageView)
    }

    private fun setTopText(entity: CmtsMovement) {
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = entity.title
        val tvTime = findViewById<TextView>(R.id.tv_time)
        val tvApply = findViewById<TextView>(R.id.tv_apply)
        if (entity.getmMovementRelations().size > 0) {
            val relation = entity.getmMovementRelations()[0]
            relation.timePeriod?.let {
                val dayMinute = TimeUtil.convertDayOfMinute(it.startTime, it.endTime)
                tvTime.text = "时间：$dayMinute"
                val endTime = TimeUtil.convertDayOfMinute(it.endTime)
                tvApply.text = "报名：截止 $endTime"
            }
            viewNum = relation.browseNum
            participateNum = relation.participateNum
            limit = relation.ticketNum
        } else {
            tvTime.visibility = View.GONE
            tvApply.visibility = View.GONE
        }
        val tvAddress = findViewById<TextView>(R.id.tv_address)
        tvAddress.text = "地点：${entity.location}"
        val tvHost = findViewById<TextView>(R.id.tv_host)
        tvHost.text = "主办：${entity.sponsor}"
        val tvType = findViewById<TextView>(R.id.tv_type)
        if (entity.type != null && entity.type == "communicationMeeting") {
            tvType.text = "类型：跨校交流会"
        } else if (entity.type != null && entity.type == "expertInteraction") {
            tvType.text = "类型：专家互动"
        } else if (entity.type != null && entity.type == "lessonViewing") {
            tvType.text = "类型：创课观摩"
        } else {
            tvType.visibility = View.GONE
        }
        val tvPart = findViewById<TextView>(R.id.tv_participation)
        val tvLimit = findViewById<TextView>(R.id.tv_limit)
        if (entity.participationType != null && entity.participationType == "ticket") {
            tvPart.text = "参与：须报名预约，凭电子票入场"
            tvLimit.visibility = View.VISIBLE
            tvLimit.text = "限额：$participateNum /$limit 人"
        } else if (entity.participationType != null && entity.participationType == "free") {
            tvPart.text = "参与：在线报名，免费入场"
        } else if (entity.participationType != null && entity.participationType == "chair") {
            tvPart.text = "参与：讲座视频录像+在线问答交流"
        } else {
            tvLimit.visibility = View.GONE
            tvPart.visibility = View.GONE
        }
    }

    @Suppress("DEPRECATION")
    private fun setContentText(content: String?) {
        val llContent = findViewById<LinearLayout>(R.id.ll_content)
        if (content != null) {
            llContent.visibility = View.VISIBLE
            val tvContent = findViewById<TextView>(R.id.tv_content)
            val ivExpand = findViewById<ImageView>(R.id.iv_expand)
            val imageGetter = HtmlHttpImageGetter(tvContent, Constants.REFERER, true)
            val spanned = Html.fromHtml(content, imageGetter, null)
            tvContent.movementMethod = LinkMovementMethod.getInstance()
            tvContent.text = spanned
            tvContent.visibility = View.VISIBLE
            ivExpand.setImageResource(R.drawable.course_dictionary_shouqi)
            llContent.setOnClickListener(object : View.OnClickListener {
                private var isExpand = false

                override fun onClick(view: View) {
                    if (isExpand) {
                        tvContent.visibility = View.VISIBLE
                        ivExpand.setImageResource(R.drawable.course_dictionary_shouqi)
                        isExpand = false
                    } else {
                        ssvContent.postDelayed({ ssvContent.smoothScrollTo(0, llContent.top) }, 10)
                        tvContent.visibility = View.GONE
                        ivExpand.setImageResource(R.drawable.course_dictionary_xiala)
                        isExpand = true
                    }
                }
            })
        } else {
            llContent.visibility = View.GONE
        }
    }

    private fun setFileInfos(list: List<MFileInfo>) {
        if (list.isNotEmpty()) {
            fileInfos.addAll(list)
            fileAdapter.notifyDataSetChanged()
            if (videoRV.visibility != View.VISIBLE) videoRV.visibility = View.VISIBLE
            if (llEmptyFiles.visibility != View.GONE) llEmptyFiles.visibility = View.GONE
            fileAdapter.setOnItemClickListener({ _, _, _, position ->
                val photos = ArrayList<String>()
                fileInfos.mapTo(photos) { it.url }
                val intent = Intent(context, AppMultiImageShowActivity::class.java)
                intent.putStringArrayListExtra("photos", photos)
                intent.putExtra("position", position)
                startActivity(intent)
            })
        } else {
            videoRV.visibility = View.GONE
            llEmptyFiles.visibility = View.VISIBLE
        }
    }

    private fun setBottomText(entity: CmtsMovement) {
        val llBottom = findViewById<LinearLayout>(R.id.ll_bottom)
        llBottom.visibility = View.VISIBLE
        setNumText()
        if (entity.state != null && entity.state == "register") {
            register = true
            if (entity.getmMovementRegisters().size > 0) {
                entity.getmMovementRegisters()[0].id?.let {
                    registerId = it
                    register = false
                    btType.text = "取消报名"
                    btType.isEnabled = true
                }
            } else {
                btType.isEnabled = true
                btType.text = "报名参与"
            }
        } else {
            btType.text = "报名参与"
            btType.isEnabled = false
        }
        btType.setOnClickListener({
            if (register) {   //报名参与
                registerActivity()
            } else {           //取消报名
                unRegisterActivity()
            }
        })
    }

    private fun setNumText() {
        val tvBottomText = findViewById<TextView>(R.id.tv_bottomText)
        val text = "$viewNum 次浏览，$participateNum 人参加"
        val ssb = SpannableString(text)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)),
                0, text.indexOf("次"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        val start = text.indexOf("，") + 1
        val end = text.indexOf("人")
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvBottomText.text = ssb
    }

    private inner class FileAdapter(mDatas: List<MFileInfo>) : BaseArrayRecyclerAdapter<MFileInfo>(mDatas) {
        private var width = 0
        private var height = 0

        init {
            val screenWidth = resources.displayMetrics.widthPixels
            val densityDpi = resources.displayMetrics.densityDpi
            var cols = screenWidth / densityDpi
            cols = if (cols < 3) 3 else cols
            val columnSpace = 4 * context.resources.displayMetrics.density
            width = ((screenWidth - columnSpace * (cols - 1)) / cols).toInt()
            height = (width * 4) / 5
        }

        override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, mFileInfo: MFileInfo, position: Int) {
            holder.itemView.layoutParams = LinearLayout.LayoutParams(width, height)
            val ivImg = holder.obtainView<ImageView>(R.id.iv_img)
            val ivVideo = holder.obtainView<ImageView>(R.id.iv_video)
            if (mFileInfo.url != null && MediaFile.isVideoFileType(mFileInfo.url)) {
                ivVideo.visibility = View.VISIBLE
            } else {
                ivVideo.visibility = View.GONE
            }
            GlideImgManager.loadImage(context, mFileInfo.url, R.drawable.app_default, R.drawable.app_default, ivImg)
        }

        override fun bindView(viewtype: Int): Int {
            return R.layout.cmtsmovres_item
        }
    }

    private fun getComment() {
        tvMoreReply.visibility = View.GONE
        val url = "$commentUrl&limit=5"
        addSubscription(Flowable.fromCallable({
            var json = OkHttpClientManager.getAsString(context, url)
            val gson = GsonBuilder().create()
            val result = gson.fromJson(json, CommentListResult::class.java)
            result?.getResponseData()?.getmComments()?.let {
                for (i in 0 until it.size) {
                    try {
                        val url2 = "$commentUrl&mainId=${it[i].id}"
                        json = OkHttpClientManager.getAsString(context, url2)
                        val mResult = gson.fromJson(json, CommentListResult::class.java)
                        if (mResult?.getResponseData()?.getmComments() != null) {
                            it[i].childList = mResult.responseData.getmComments()
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
            result
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            if (it?.responseData?.getmComments() != null) {
                updateListUI(it.responseData.getmComments(), it.responseData.paginator)
            } else {
                lfv()
            }
        }, { lfv() }))
    }


    /*更新评论列表*/
    private fun updateListUI(list: List<CommentEntity>, paginator: Paginator?) {
        mDatas.clear()
        if (list.isNotEmpty()) {
            mDatas.addAll(list)
            adapter.notifyDataSetChanged()
            if (paginator != null && paginator.hasNextPage) {
                tvMoreReply.visibility = View.VISIBLE
            }
            commentRV.visibility = View.VISIBLE
        } else {
            commentRV.visibility = View.GONE
            setEmptyText()
        }
    }

    private fun setEmptyText() {
        val tvEmptyComment = findViewById<TextView>(R.id.tv_emptyComment)
        tvEmptyComment.visibility = View.VISIBLE
        val text = "目前还没人发起评论，\n赶紧去发表您的评论吧！"
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

    private fun lfv() {
        val lfv = findViewById<LoadFailView>(R.id.lfv)
        lfv.visibility = View.VISIBLE
        lfv.setOnRetryListener { getComment() }
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
        findViewById<TextView>(R.id.tv_comment).setOnClickListener({ showCommentDialog(false) })
        tvMoreReply.setOnClickListener({
            val intent = Intent(context, AppMoreCommentActivity::class.java)
            intent.putExtra("relationId", movementId)
            intent.putExtra("relationType", "movement")
            startActivity(intent)
        })
        adapter.setCommentCallBack({ position, _ ->
            childPosition = position
            showCommentDialog(true)
        })
        adapter.setSupportCallBack({ position, tv_like -> createLike(position, tv_like) })
        adapter.setMoreReplyCallBack({ position, entity ->
            replyPosition = position
            val intent = Intent(context, AppMoreReplyActivity::class.java)
            intent.putExtra("entity", entity)
            intent.putExtra("relationType", "movement")
            intent.putExtra("relationId", movementId)
            startActivity(intent)
        })
        adapter.setDeleteMainComment({ id, position -> deleteComment(id, position) })
    }

    private fun showCommentDialog(sendChild: Boolean) {
        val dialog = CommentDialog(context, "请输入评论内容")
        dialog.show()
        dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
            override fun sendComment(content: String) {
                if (sendChild) {
                    sendChildComment(content)
                } else {
                    sendMainComment(content)
                }
            }
        })
    }

    /*发送主评论*/
    private fun sendMainComment(content: String) {
        val url = "${Constants.OUTRT_NET}/m/comment"
        val map = HashMap<String, String>()
        map["relation.id"] = movementId
        map["relation.type"] = "movement"
        map["content"] = content
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
                if (response?.getResponseData() != null) {
                    commentRV.visibility = View.VISIBLE
                    tvEmptyComment.visibility = View.GONE
                    val entity = response.getResponseData()
                    entity.creator?.let {
                        entity.creator = getCreator(it)
                    }
                    if (mDatas.size < 5) {
                        mDatas.add(entity)
                        adapter.notifyDataSetChanged()
                    } else {
                        tvMoreReply.visibility = View.VISIBLE
                        toastFullScreen("评论成功", true)
                    }
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

    /*发送子评论*/
    private fun sendChildComment(content: String) {
        val url = "${Constants.OUTRT_NET}/m/comment"
        val map = HashMap<String, String>()
        map["relation.id"] = movementId
        map["relation.type"] = "movement"
        map["content"] = content
        map["mainId"] = mDatas[childPosition].id
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
            }

            override fun onResponse(response: BaseResponseResult<CommentEntity>?) {
                hideTipDialog()
                if (response?.getResponseData() != null) {
                    val childNum = mDatas[childPosition].childNum + 1
                    mDatas[childPosition].childNum = childNum
                    adapter.notifyDataSetChanged()
                    if (mDatas[childPosition].childList.size >= 10) {
                        toastFullScreen("评论成功", true)
                    } else {
                        val entity = response.getResponseData()
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas[childPosition].childList.add(entity)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    toastFullScreen("评论失败", false)
                }
            }
        }, map))
    }

    /**
     * 创建观点(点赞)
     */
    private fun createLike(position: Int, tvLike: TextView) {
        val url = "${Constants.OUTRT_NET}/m/attitude"
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
                    val goodView = GoodView(context)
                    val defaultColor = ContextCompat.getColor(context, R.color.defaultColor)
                    goodView.setTextInfo("+1", defaultColor, 15f)
                    goodView.show(tvLike)
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

    /**
     * 删除评论
     *
     * @param id
     * @param position
     */
    private fun deleteComment(id: String, position: Int) {
        val url = "${Constants.OUTRT_NET}/m/comment/$id"
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
                    mDatas.removeAt(position)
                    adapter.notifyDataSetChanged()
                    if (mDatas.size == 0) {
                        commentRV.visibility = View.GONE
                        tvEmptyComment.visibility = View.VISIBLE
                    }
                    getComment()
                }
            }
        }, map))
    }

    private fun registerActivity() {
        val url = "${Constants.OUTRT_NET}/m/movement/register"
        val map = HashMap<String, String>()
        map["movement.id"] = movementId
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<CmtsMovRegister>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: CmtsMovRegister?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    registerId = response.responseData.id
                    register = false
                    btType.text = "取消报名"
                    participateNum += 1
                    setNumText()
                    val event = MessageEvent()
                    event.action = Action.REGIST_MOVEMENT
                    movEntity.getmMovementRegisters().add(response.responseData)
                    if (movEntity.getmMovementRelations().size > 0) {
                        movEntity.getmMovementRelations()[0].participateNum = participateNum
                    }
                    event.obj = movEntity
                    RxBus.getDefault().post(event)
                } else {
                    toast(context, "报名失败")
                }
            }
        }, map))
    }

    private fun unRegisterActivity() {
        val url = "${Constants.OUTRT_NET}/m/movement/register/$registerId"
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
                    register = true
                    btType.text = "报名参与"
                    participateNum -= 1
                    if (participateNum <= 0) {
                        participateNum = 0
                    }
                    setNumText()
                    val event = MessageEvent()
                    event.action = Action.UNREGIST_MOVEMENT
                    movEntity.getmMovementRegisters().clear()
                    if (movEntity.getmMovementRelations().size > 0) {
                        movEntity.getmMovementRelations()[0].participateNum = participateNum
                    }
                    event.obj = movEntity
                    RxBus.getDefault().post(event)
                } else {
                    toast(context, "取消报名失败")
                }
            }
        }, map))
    }

    override fun obBusEvent(event: MessageEvent) {
        if (event.action == Action.CREATE_CHILD_COMMENT) {
            if (event.bundle != null && event.bundle.getSerializable("mainComment") != null
                    && event.bundle.getSerializable("mainComment") is CommentEntity) {
                val mainComment = event.bundle.getSerializable("mainComment") as CommentEntity
                val position = mDatas.indexOf(mainComment)
                if (position >= 0 && event.bundle.getSerializable("childComment") != null
                        && event.bundle.getSerializable("childComment") is CommentEntity) {
                    val childPostNum = mDatas[position].childNum
                    mDatas[position].childNum = childPostNum + 1
                    val childComment = event.bundle.getSerializable("childComment") as CommentEntity
                    if (mDatas[position].childList.size < 10) {
                        mDatas[position].childList.add(childComment)
                    }
                    adapter.notifyDataSetChanged()
                }
            } else {
                val childPostNum = mDatas[replyPosition].childNum
                mDatas[replyPosition].childNum = childPostNum + 1
                adapter.notifyDataSetChanged()
            }
        } else if (event.action == Action.CREATE_LIKE) {
            if (event.obj != null && event.obj is CommentEntity) {
                val entity = event.obj as CommentEntity
                if (mDatas.indexOf(entity) != -1) {
                    mDatas[mDatas.indexOf(entity)] = entity
                    adapter.notifyDataSetChanged()
                }
            } else {
                val supportNum = mDatas[replyPosition].supportNum
                mDatas[replyPosition].supportNum = supportNum + 1
                adapter.notifyDataSetChanged()
            }
        } else if (event.action == Action.DELETE_MAIN_COMMENT && event.obj != null && event.obj is CommentEntity) {       //在更多评论列表删除评论
            val entity = event.getObj() as CommentEntity
            if (mDatas.contains(entity)) {
                mDatas.remove(entity)
                adapter.notifyDataSetChanged()
                getComment()
            }
        }
    }

    private fun showBottomDialog() {
        val contentView = layoutInflater.inflate(R.layout.dialog_teaching_at, LinearLayout(context), false)
        val dialog = AlertDialog.Builder(context).create()
        val tvVideo = contentView.findViewById<TextView>(R.id.tv_video)
        val tvPhoto = contentView.findViewById<TextView>(R.id.tv_photo)
        val tvDelete = contentView.findViewById<TextView>(R.id.tv_delete)
        val tvCancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        val listener = View.OnClickListener { view ->
            when (view.id) {
                R.id.tv_video -> pickerMedia(2)
                R.id.tv_photo -> pickerMedia(1)
                R.id.tv_delete -> showTipsDialog()
            }
            dialog.dismiss()
        }
        tvVideo.setOnClickListener(listener)
        tvPhoto.setOnClickListener(listener)
        tvDelete.setOnClickListener(listener)
        tvCancel.setOnClickListener(listener)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
        dialog.window?.let {
            it.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT)
            it.setWindowAnimations(R.style.dialog_anim)
            it.setContentView(contentView)
            it.setGravity(Gravity.BOTTOM)
        }
    }

    private fun pickerMedia(mediaType: Int) {
        val builder = MediaOption.Builder()
        if (mediaType == 1) {
            builder.setSelectType(MediaOption.TYPE_IMAGE)
        } else {
            builder.setSelectType(MediaOption.TYPE_VIDEO)
        }
        builder.setShowCamera(true)
        val option = builder.build()
        MediaPicker.getInstance().init(option).selectMedia(context, object : MediaPicker.onSelectMediaCallBack() {
            override fun onSelected(path: String) {
                if (File(path).exists()) {
                    if (mediaType == 2 && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                        val dialog = MaterialDialog(context)
                        dialog.setTitle("网络提醒")
                        dialog.setMessage("使用2G/3G/4G网络上传视频会消耗较多流量。确定要开启吗？")
                        dialog.setPositiveButton("确定") { _, _ -> upload(File(path)) }
                        dialog.setNegativeButton("取消", null)
                        dialog.show()
                    } else {
                        upload(File(path))
                    }
                } else {
                    toast(context, "文件不存在")
                }
            }
        })
    }

    private fun upload(file: File) {
        val url1 = "${Constants.OUTRT_NET}/m/file/uploadTemp"
        val dialog = FileUploadDialog(context, file.name, "正在上传")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val disposable = Flowable.fromCallable({
            val gson = GsonBuilder().create()
            val json = OkHttpClientManager.post(context, url1, file, file.name) { totalBytes, remainingBytes, _, _ ->
                Flowable.just(longArrayOf(totalBytes, remainingBytes)).observeOn(AndroidSchedulers.mainThread()).subscribe { params ->
                    dialog.setUploadProgressBar(params[0], params[1])
                    dialog.setUploadText(params[0], params[1])
                }
            }
            val result = gson.fromJson(json, FileUploadResult::class.java)
            result?.responseData?.let {
                val url2 = "${Constants.OUTRT_NET}/m/movement/$movementId/upload"
                val map = HashMap<String, String>()
                map["fileInfos[0].id"] = result.responseData.id
                map["fileInfos[0].url"] = result.responseData.url
                map["fileInfos[0].fileName"] = result.responseData.fileName
                val responseStr = OkHttpClientManager.postAsString(context, url2, map)
                gson.fromJson(responseStr, FileUploadDataResult::class.java)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            dialog.dismiss()
            it?.responseData?.getmFileInfos()?.let {
                setFileInfos(it)
            }
        }, {
            dialog.dismiss()
            toastFullScreen("上传失败", false)
        })
        dialog.setCancelListener {
            val mDialog = MaterialDialog(context)
            mDialog.setTitle("提示")
            mDialog.setMessage("你确定取消本次上传吗？")
            mDialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            mDialog.setPositiveButton("确定") { _, _ ->
                mDialog.dismiss()
                disposable.dispose()
            }
            mDialog.setNegativeButton("关闭", { _, dialog ->
                dialog.dismiss()
                if (dialog != null && !dialog.isShowing) {
                    dialog.show()
                }
            })
            dialog.show()
        }
    }

    private fun showTipsDialog() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("你确定删除吗？")
        dialog.setNegativeButton("确定") { _, _ -> delete() }
        dialog.setPositiveButton("取消", null)
        dialog.show()
    }

    /*删除活动*/
    private fun delete() {
        val url = "${Constants.OUTRT_NET}/m/movement/$movementId"
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
                    toastFullScreen("已成功删除，返回首页", true)
                    val event = MessageEvent()
                    event.action = Action.DELETE_MOVEMENT
                    event.obj = movEntity
                    RxBus.getDefault().post(event)
                    finish()
                } else {
                    toastFullScreen("删除失败", false)
                }
            }
        }, map))
    }
}