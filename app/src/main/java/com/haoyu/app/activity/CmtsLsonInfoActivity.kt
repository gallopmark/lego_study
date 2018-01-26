package com.haoyu.app.activity

import android.app.Activity
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.haoyu.app.adapter.AppDiscussionAdapter
import com.haoyu.app.adapter.MFileInfoAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.FileUploadDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.filePicker.LFilePicker
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.utils.*
import com.haoyu.app.view.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import java.io.File
import java.util.*

/**
 * 创建日期：2018/1/23.
 * 描述:创课详情页
 * 作者:xiaoma
 */
class CmtsLsonInfoActivity : BaseActivity() {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView

    private lateinit var ssvContent: StickyScrollView

    private lateinit var tvAdviseCount: TextView
    private lateinit var rvAdvise: RecyclerView   //建议列表
    private lateinit var tvEmptyDatas: TextView
    private lateinit var tvMoreReply: TextView  //更多建议按钮
    private lateinit var lessonEntity: CmtsLessonEntity
    private lateinit var lessonId: String //创课id
    private lateinit var relationId: String //关联关系id
    private var supportNum = 0
    private var adviseNum = 0  //点赞数，提建议数
    private val mDatas = ArrayList<ReplyEntity>()
    private lateinit var adapter: AppDiscussionAdapter
    private var childPosition = 0
    private var replyPosition = 0

    override fun setLayoutResID(): Int {
        return R.layout.activity_cmtslsoninfo
    }

    override fun initView() {
        lessonId = intent.getStringExtra("lessonId")
        setToolBar()
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        tvEmpty = findViewById(R.id.tv_empty)
        ssvContent = findViewById(R.id.ssv_content)
        tvAdviseCount = findViewById(R.id.tv_adviseCount)
        rvAdvise = findViewById(R.id.rv_advise)
        tvEmptyDatas = findViewById(R.id.tv_emptyAdvise)
        tvMoreReply = findViewById(R.id.tv_more_reply)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        rvAdvise.layoutManager = llm
        adapter = AppDiscussionAdapter(context, mDatas, userId)
        rvAdvise.adapter = adapter
    }

    private fun setToolBar() {
        toolBar = findViewById(R.id.toolBar)
        val title = resources.getString(R.string.gen_class_detail)
        toolBar.setTitle_text(title)
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                showBottomDialog()
            }
        })
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/m/lesson/cmts/view/" + lessonId
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<CmtsLessonData>>() {
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(result: BaseResponseResult<CmtsLessonData>?) {
                loadingView.visibility = View.GONE
                if (result?.getResponseData()?.getmLesson() != null) {
                    updateUI(result.getResponseData().getmLesson(), result.getResponseData().getmLessonAttribute())
                    getFiles()
                    getAdvise()
                } else {
                    tvEmpty.text = resources.getString(R.string.gen_class_emptylist)
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(mLesson: CmtsLessonEntity, attribute: CmtsLessonAttribute?) {
        ssvContent.visibility = View.VISIBLE
        lessonEntity = mLesson
        if (lessonEntity.creator?.id != null && lessonEntity.creator.id == userId) {
            toolBar.setShow_right_button(true)
        }
        if (lessonEntity.getmDiscussionRelations().size > 0) {
            relationId = lessonEntity.getmDiscussionRelations()[0].id
        }
        val remainDay = lessonEntity.remainDay
        setRemainDay(remainDay)
        setLesson(lessonEntity)
        attribute?.let {
            setLessonAttribute(attribute)
        }
    }

    private fun setRemainDay(remainDay: Int) {
        val mRrogressBar = findViewById<RoundRectProgressBar>(R.id.mRrogressBar)
        val tvDay = findViewById<TextView>(R.id.tv_day)
        mRrogressBar.max = 60
        mRrogressBar.progress = 60 - remainDay
        if (remainDay > 0) {
            val text = "还剩 $remainDay 天"
            tvDay.text = text
        } else {
            tvDay.text = "已结束"
        }
    }

    private fun setLesson(mLesson: CmtsLessonEntity) {
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = mLesson.title
        val ivUser = findViewById<ImageView>(R.id.iv_userIco)
        if (mLesson.creator?.avatar != null) {
            GlideImgManager.loadCircleImage(context, mLesson.creator.avatar, R.drawable.user_default, R.drawable.user_default, ivUser)
        } else {
            ivUser.setImageResource(R.drawable.user_default)
        }
        mLesson.creator?.let {
            val tvUserName = findViewById<TextView>(R.id.tv_userName)
            tvUserName.text = it.realName
        }
        val tvDate = findViewById<TextView>(R.id.tv_createTime)
        val dateText = "发布于${TimeUtil.getSlashDate(mLesson.createTime)}"
        tvDate.text = dateText
        var browseNum = 0
        var adviseNum = 0
        if (mLesson.getmDiscussionRelations().size > 0) {
            browseNum = mLesson.getmDiscussionRelations()[0].browseNum
            supportNum = mLesson.getmDiscussionRelations()[0].supportNum
            adviseNum = mLesson.getmDiscussionRelations()[0].replyNum
        }
        val tvHeatNum = findViewById<TextView>(R.id.tv_heatNum)
        val tvSupportNum = findViewById<TextView>(R.id.tv_supportNum)
        val tvAdviseNum = findViewById<TextView>(R.id.tv_adviseNum)
        val heatText = "热度（$browseNum）"
        tvHeatNum.text = heatText
        val supportText = "赞（$supportNum）"
        tvSupportNum.text = supportText
        tvSupportNum.setOnClickListener { createLike(tvSupportNum) }
        val adviseText = "提建议（$adviseNum）"
        tvAdviseNum.text = adviseText
        if (!isEmpty(mLesson.content)) {
            val title = resources.getString(R.string.gen_class_topic)
            val llIntroduce = findViewById<LinearLayout>(R.id.ll_introduce)
            addView(title, mLesson.content, llIntroduce)
        }
    }

    private fun createLike(tvSupportNum: TextView) {
        val url = "${Constants.OUTRT_NET}/m/attitude"
        val map = HashMap<String, String>()
        map.put("attitude", "support")
        map.put("relation.id", lessonId)
        map.put("relation.type", "discussion")
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            override fun onError(request: Request, exception: Exception) {
                onNetWorkError(context)
            }

            override fun onResponse(response: AttitudeMobileResult?) {
                if (response?.responseCode != null && response.responseCode == "00") {
                    val goodView = GoodView(context)
                    val defaultColor = ContextCompat.getColor(context, R.color.defaultColor)
                    goodView.setTextInfo("+1", defaultColor, 16f)
                    goodView.show(tvSupportNum)
                    supportNum++
                    val supportText = "赞（$supportNum）"
                    tvSupportNum.text = supportText
                    val event = MessageEvent()
                    event.action = Action.SUPPORT_STUDY_CLASS
                    if (lessonEntity.getmDiscussionRelations().size > 0) {
                        lessonEntity.getmDiscussionRelations()[0].supportNum = supportNum
                    }
                    event.obj = lessonEntity
                    RxBus.getDefault().post(event)
                } else if (response?.responseMsg != null) {
                    toast(context, "您已点赞过")
                } else {
                    toast(context, "点赞失败")
                }
            }
        }, map))
    }

    private fun setLessonAttribute(att: CmtsLessonAttribute) {
        val llIntroduce = findViewById<LinearLayout>(R.id.ll_introduce)  //创课介绍详细布局
        if (!isEmpty(att.realia)) {
            val realia = resources.getString(R.string.gen_class_realia)
            addView(realia, att.realia, llIntroduce)
        }
        if (!isEmpty(att.realiaSummary)) {
            val realiaSummary = resources.getString(R.string.gen_class_realiaSummary)
            addView(realiaSummary, att.realiaSummary, llIntroduce)
        }
        if (!isEmpty(att.realiaUseReason)) {
            val userReason = resources.getString(R.string.gen_class_realiaUseReason)
            addView(userReason, att.realiaUseReason, llIntroduce)
        }
        val llFrame = findViewById<LinearLayout>(R.id.ll_frame)
        if (isEmpty(att.topicBase) && isEmpty(att.learnDetail) && isEmpty(att.designPrinciple)) {
            val layoutFrame = findViewById<LinearLayout>(R.id.layout_frame)
            layoutFrame.visibility = View.GONE
        } else {
            if (!isEmpty(att.topicBase)) {
                val topicBase = resources.getString(R.string.gen_class_topicBase)
                addView(topicBase, att.topic, llFrame)
            }
            if (!isEmpty(att.learnDetail)) {
                val learnDetail = resources.getString(R.string.gen_class_learnDetail)
                addView(learnDetail, att.learnDetail, llFrame)
            }
            if (!isEmpty(att.designPrinciple)) {
                val designPrinciple = resources.getString(R.string.gen_class_designPrinciple)
                addView(designPrinciple, att.designPrinciple, llFrame)
            }
        }
        val llActivity = findViewById<LinearLayout>(R.id.ll_activity)
        if (isEmpty(att.stemElement) && isEmpty(att.examples) && isEmpty(att.models) && isEmpty(att.rethink) && isEmpty(att.expand)) {
            val layoutActivity = findViewById<LinearLayout>(R.id.layout_activity)
            layoutActivity.visibility = View.GONE
        } else {
            if (!isEmpty(att.stemElement)) {
                val stem = resources.getString(R.string.gen_class_stem_element)
                addView(stem, att.stemElement, llActivity)
            }
            if (!isEmpty(att.examples)) {
                val examples = resources.getString(R.string.gen_class_examples)
                addView(examples, att.examples, llActivity)
            }
            if (!isEmpty(att.models)) {
                val models = resources.getString(R.string.gen_class_models)
                addView(models, att.models, llActivity)
            }
            if (!isEmpty(att.rethink)) {
                val rethink = resources.getString(R.string.gen_class_rethink)
                addView(rethink, att.rethink, llActivity)
            }
            if (!isEmpty(att.expand)) {
                val expand = resources.getString(R.string.gen_class_expand)
                addView(expand, att.expand, llActivity)
            }
        }
        val tagIntroduce = findViewById<LinearLayout>(R.id.tag_introduce)
        val ivIntroduce = findViewById<ImageView>(R.id.iv_introduce)
        tagIntroduce.setOnClickListener(object : View.OnClickListener {  // 创课介绍展开或收起
            internal var isExpand = false

            override fun onClick(view: View) {
                if (isExpand) {
                    llIntroduce.visibility = View.VISIBLE
                    ivIntroduce.setImageResource(R.drawable.course_dictionary_shouqi)
                    isExpand = false
                } else {
                    ssvContent.postDelayed({ ssvContent.smoothScrollBy(0, tagIntroduce.top) }, 10)
                    llIntroduce.visibility = View.GONE
                    ivIntroduce.setImageResource(R.drawable.course_dictionary_xiala)
                    isExpand = true
                }
            }
        })
        val tagFrame = findViewById<LinearLayout>(R.id.tag_frame)
        val ivFrame = findViewById<ImageView>(R.id.iv_frame)
        tagFrame.setOnClickListener(object : View.OnClickListener {
            private var isExpand = true

            override fun onClick(view: View) {
                if (isExpand) {
                    llFrame.visibility = View.VISIBLE
                    ivFrame.setImageResource(R.drawable.course_dictionary_shouqi)
                    isExpand = false
                } else {
                    ssvContent.postDelayed({ ssvContent.smoothScrollBy(0, tagFrame.top) }, 10)
                    llFrame.visibility = View.GONE
                    ivFrame.setImageResource(R.drawable.course_dictionary_xiala)
                    isExpand = true
                }
            }
        })
        val tagActivity = findViewById<LinearLayout>(R.id.tag_activity)
        val ivActivity = findViewById<ImageView>(R.id.iv_activity)
        tagActivity.setOnClickListener(object : View.OnClickListener {
            private var isExpand = true

            override fun onClick(view: View) {
                if (isExpand) {
                    llActivity.visibility = View.VISIBLE
                    ivActivity.setImageResource(R.drawable.course_dictionary_shouqi)
                    isExpand = false
                } else {
                    ssvContent.postDelayed({ ssvContent.smoothScrollBy(0, tagActivity.top) }, 10)
                    llActivity.visibility = View.GONE
                    ivActivity.setImageResource(R.drawable.course_dictionary_xiala)
                    isExpand = true
                }
            }
        })
    }

    private fun isEmpty(text: String?): Boolean {
        return text?.trim()?.isEmpty() != false
    }

    private fun addView(title: String, content: String, parent: LinearLayout) {
        if (parent.visibility != View.VISIBLE) {
            parent.visibility = View.VISIBLE
        }
        val tvTitle = createTitleView()
        tvTitle.text = title
        val tvContent = createContentView()
        setSpannedText(tvContent, content)
        parent.addView(tvTitle)
        parent.addView(tvContent)
    }

    private fun createTitleView(): TextView {
        val tv = TextView(context)
        tv.textSize = 16f
        tv.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        return tv
    }

    private fun createContentView(): TextView {
        val tv = TextView(context)
        tv.textSize = 16f
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = PixelFormat.dp2px(context, 12f)
        params.bottomMargin = PixelFormat.dp2px(context, 12f)
        tv.layoutParams = params
        return tv
    }

    @Suppress("DEPRECATION")
    private fun setSpannedText(tv: TextView, text: String?) {
        val imageGetter = HtmlHttpImageGetter(tv, Constants.REFERER, true)
        if (text != null) {
            val spanned = Html.fromHtml(text, imageGetter, null)
            tv.movementMethod = LinkMovementMethod.getInstance()
            tv.text = spanned
        } else {
            tv.text = null
        }
    }

    private fun getFiles() {
        val url = "${Constants.OUTRT_NET}/m/file?fileRelations[0].relation.id=$lessonId&fileRelations[0].relation.type=discussion&limit=2&orders=CREATE_TIME.DESC"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<MFileInfoData>>() {

            override fun onError(request: Request, e: Exception) {
                onFileError()
            }

            override fun onResponse(response: BaseResponseResult<MFileInfoData>?) {
                if (response?.getResponseData() != null) {
                    updateFiles(response.getResponseData())
                } else {
                    onFileEmpty()
                }
            }
        }))
    }

    private fun onFileError() {
        val lfv1 = findViewById<LoadFailView>(R.id.lfv1)
        lfv1.visibility = View.VISIBLE
        lfv1.setOnRetryListener { getFiles() }
    }

    private fun updateFiles(responseData: MFileInfoData) {
        if (responseData.getmFileInfos().size > 0) {
            val mDatas = responseData.getmFileInfos()
            val rvFile = findViewById<RecyclerView>(R.id.rv_file)
            rvFile.visibility = View.VISIBLE
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            rvFile.layoutManager = layoutManager
            val adapter = MFileInfoAdapter(mDatas)
            rvFile.adapter = adapter
            adapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
                val fileInfo = mDatas[position]
                val intent = Intent(context, MFileInfoActivity::class.java)
                intent.putExtra("fileInfo", fileInfo)
                startActivity(intent)
            }
            if (responseData.paginator != null && responseData.paginator.hasNextPage) {
                val tvCheckAll = findViewById<TextView>(R.id.tv_checkAll)
                tvCheckAll.visibility = View.VISIBLE
                tvCheckAll.setOnClickListener {
                    val intent = Intent(context, MFileInfosActivity::class.java)
                    intent.putExtra("title", "课程资源")
                    intent.putExtra("relationId", lessonId)
                    intent.putExtra("relationType", "discussion")
                    startActivity(intent)
                }
            }
        } else {
            onFileEmpty()
        }
    }

    private fun onFileEmpty() {
        val llEmptyFiles = findViewById<LinearLayout>(R.id.llEmptyFiles)
        llEmptyFiles.visibility = View.VISIBLE
    }

    private fun getAdvise() {
        tvMoreReply.visibility = View.GONE
        val replyUrl = "${Constants.OUTRT_NET}/m/discussion/post?discussionUser.discussionRelation.id=$relationId&orders=CREATE_TIME.ASC"
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
                updateAdvise(it.responseData.getmDiscussionPosts(), it.responseData.paginator)
            } else {
                setEmptyText()
            }
        }, { e ->
            e.printStackTrace()
            onAdviseError()
        }))
    }

    private fun updateAdvise(list: List<ReplyEntity>, paginator: Paginator?) {
        mDatas.clear()
        if (list.isNotEmpty()) {
            mDatas.addAll(list)
            adapter.notifyDataSetChanged()
            paginator?.let {
                adviseNum = it.totalCount
                setAdvise()
                if (it.hasNextPage) {
                    tvMoreReply.visibility = View.VISIBLE
                    tvMoreReply.setOnClickListener {
                        val intent = Intent(context, AppMoreMainReplyActivity::class.java)
                        intent.putExtra("type", "advise")
                        intent.putExtra("relationId", relationId)
                        startActivity(intent)
                    }
                }
            }
            rvAdvise.visibility = View.VISIBLE
        } else {
            setEmptyText()
            rvAdvise.visibility = View.GONE
        }
        setBottomView()
    }

    private fun setEmptyText() {
        tvEmptyDatas.visibility = View.VISIBLE
        val text = "目前还没人提建议，\n赶紧去发表您的建议吧！"
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
        tvEmptyDatas.movementMethod = LinkMovementMethod.getInstance()
        tvEmptyDatas.text = ssb
    }

    private fun setBottomView() {
        val bottomView = findViewById<TextView>(R.id.bottomView)
        bottomView.visibility = View.VISIBLE
        bottomView.setOnClickListener { showCommentDialog(false) }
    }

    private fun onAdviseError() {
        val lfv2 = findViewById<LoadFailView>(R.id.lfv2)
        lfv2.visibility = View.VISIBLE
        lfv2.setOnRetryListener { getAdvise() }
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        adapter.setOnPostClickListener(object : AppDiscussionAdapter.OnPostClickListener {
            override fun onTargetClick(view: View, position: Int, entity: ReplyEntity) {

            }

            override fun onChildClick(view: View, position: Int) {
                childPosition = position
                showCommentDialog(true)
            }
        })

        adapter.setSupportCallBack({ position, tv_like -> createLike(position, tv_like) })
        adapter.setMoreReplyCallBack({ entity, position ->
            replyPosition = position
            val intent = Intent(context, AppMoreChildReplyActivity::class.java)
            intent.putExtra("entity", entity)
            intent.putExtra("relationId", relationId)
            startActivity(intent)
        })
        adapter.setDeleteMainReply({ id, position -> deleteReply(id, position) })
    }

    /**
     * 创建观点(点赞)
     *
     * @param position
     */
    private fun createLike(position: Int, tvLike: TextView) {
        val url = "${Constants.OUTRT_NET}/m/attitude"
        val relationId = mDatas[position].id
        val map = HashMap<String, String>().apply {
            put("attitude", "support")
            put("relation.id", relationId)
            put("relation.type", "discussion_post")
        }
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
                } else if (response != null && response.responseMsg != null) {
                    toast(context, "您已点赞过")
                } else {
                    toast(context, "点赞失败")
                }
            }
        }, map))
    }

    private fun sendChildReply(position: Int, content: String) {
        val map = HashMap<String, String>()
        map.put("content", content)
        map.put("mainPostId", mDatas[position].id)
        map.put("discussionUser.discussionRelation.id", relationId)
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
                    if (mDatas[position].childReplyEntityList.size < 10) {
                        val entity = response.responseData
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas[position].childReplyEntityList.add(entity)
                    } else {
                        toastFullScreen("回复成功", true)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }, map))
    }

    private fun deleteReply(id: String, position: Int) {
        val url = "${Constants.OUTRT_NET}/m/discussion/post/$id"
        val map = HashMap<String, String>().apply {
            put("_method", "delete")
            put("discussionUser.discussionRelation.id", relationId)
            put("mainPostId", id)
        }
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
                        tvEmptyDatas.visibility = View.VISIBLE
                        rvAdvise.visibility = View.GONE
                    }
                    adviseNum--
                    setAdvise()
                    getAdvise()
                }
            }
        }, map))
    }

    private fun showCommentDialog(sendChild: Boolean) {
        val dialog = CommentDialog(context, "请输入您的建议")
        dialog.show()
        dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
            override fun sendComment(content: String) {
                if (sendChild) {
                    sendChildReply(childPosition, content)
                } else {
                    giveAdvice(content)
                }
            }
        })
    }

    private fun giveAdvice(content: String) {
        val url = "${Constants.OUTRT_NET}/m/discussion/post"
        val map = HashMap<String, String>().apply {
            put("discussionUser.discussionRelation.id", relationId)
            put("content", content)
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<ReplyResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: ReplyResult?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    rvAdvise.visibility = View.VISIBLE
                    tvEmptyDatas.visibility = View.GONE
                    if (mDatas.size < 5) {
                        val entity = response.responseData
                        entity.creator?.let {
                            entity.creator = getCreator(it)
                        }
                        mDatas.add(entity)
                        adapter.notifyDataSetChanged()
                    } else {
                        tvMoreReply.visibility = View.VISIBLE
                        toastFullScreen("发送成功", true)
                    }
                    adviseNum++
                    setAdvise()
                    val event = MessageEvent()
                    event.action = Action.GIVE_STUDY_ADVICE
                    if (lessonEntity.getmDiscussionRelations().size > 0) {
                        lessonEntity.getmDiscussionRelations()[0].replyNum = adviseNum
                    }
                    event.obj = lessonEntity
                    RxBus.getDefault().post(event)
                } else {
                    toastFullScreen("发送失败", true)
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

    private fun setAdvise() {
        val text = "收到 $adviseNum 条建议"
        tvAdviseCount.text = text
    }

    private fun showBottomDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_teaching_cc, LinearLayout(context), false)
        val dialog = AlertDialog.Builder(context).create()
        val tvUpload = view.findViewById<TextView>(R.id.tv_upload)
        val tvDelete = view.findViewById<TextView>(R.id.tv_delete)
        val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)
        tvUpload.setOnClickListener({
            openFilePicker()
            dialog.dismiss()
        })
        tvDelete.setOnClickListener({
            showTipsDialog()
            dialog.dismiss()
        })
        tvCancel.setOnClickListener({ dialog.dismiss() })
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

    private fun openFilePicker() {
        LFilePicker().withActivity(context).withRequestCode(1).withMutilyMode(false).start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val list = data.getStringArrayListExtra(RESULT_INFO)
            if (list != null && list.size > 0) {
                val filePath = list[0]
                val file = File(filePath)
                if (file.exists()) {
                    uploadFile(file)
                } else {
                    showMaterialDialog("提示", "上传的文件不存在，请重新选择文件")
                }
            }
        }
    }

    private fun uploadFile(file: File) {
        val url = "${Constants.OUTRT_NET}/m/file/uploadTemp"
        val dialog = FileUploadDialog(context, file.name, "正在上传")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val gson = Gson()
        val mSubscription = Flowable.fromCallable({
            val json = OkHttpClientManager.post(context, url, file, file.name) { totalBytes, remainingBytes, _, _ ->
                Flowable.just(longArrayOf(totalBytes, remainingBytes)).observeOn(AndroidSchedulers.mainThread())
                        .subscribe { params ->
                            dialog.setUploadProgressBar(params[0], params[1])
                            dialog.setUploadText(params[0], params[1])
                        }
            }
            gson.fromJson(json, FileUploadResult::class.java)
        }).map { mResult ->
            commitContent(mResult)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
            dialog.dismiss()
            if (response?.responseCode != null && response.responseCode == "00") {
                getFiles()
            } else {
                showErrorDialog(file)
            }
        }, {
            dialog.dismiss()
            showErrorDialog(file)
        })
        dialog.setCancelListener { showCancelDialog(mSubscription, dialog) }
    }

    /*拿到上传临时文件返回的结果再次提交到创课表*/
    @Throws(Exception::class)
    private fun commitContent(mResult: FileUploadResult?): FileUploadDataResult? {
        if (mResult?.responseData != null) {
            val url = "${Constants.OUTRT_NET}/m/lesson/cmts/$lessonId/upload"
            val gson = GsonBuilder().create()
            val map = HashMap<String, String>().apply {
                put("fileInfos[0].id", mResult.responseData.id)
                put("fileInfos[0].url", mResult.responseData.url)
                put("fileInfos[0].fileName", mResult.responseData.fileName)
            }
            val responseStr = OkHttpClientManager.postAsString(context, url, map)
            return gson.fromJson(responseStr, FileUploadDataResult::class.java)
        }
        return null
    }

    /*上传失败显示dialog*/
    private fun showErrorDialog(file: File) {
        val dialog = MaterialDialog(context)
        dialog.setTitle("上传结果")
        dialog.setMessage("由于网络问题上传资源失败，您可以点击重新上传再次上传")
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray))
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        dialog.setNegativeButton("取消", null)
        dialog.setPositiveButton("重新上传") { _, _ -> uploadFile(file) }
        dialog.show()
    }

    /*取消上传显示dialog*/
    private fun showCancelDialog(mSubscription: Disposable, dialog: FileUploadDialog) {
        val mDialog = MaterialDialog(context)
        mDialog.setTitle("提示")
        mDialog.setMessage("你确定取消本次上传吗？")
        mDialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        mDialog.setPositiveButton("确定") { _, _ ->
            dialog.dismiss()
            mSubscription.dispose()
        }
        mDialog.setNegativeButton("关闭") { _, _ ->
            if (!dialog.isShowing) {
                dialog.show()
            }
        }
        mDialog.show()
    }

    private fun showTipsDialog() {
        val materialDialog = MaterialDialog(context)
        materialDialog.setTitle("提示")
        materialDialog.setMessage("你确定删除吗？")
        materialDialog.setNegativeButton("确定") { _, _ -> deleteCc() }
        materialDialog.setPositiveButton("取消", null)
        materialDialog.show()
    }

    /*删除创课*/
    private fun deleteCc() {
        val url = Constants.OUTRT_NET + "/m/lesson/cmts/" + lessonId
        val map = HashMap<String, String>()
        map.put("_method", "delete")
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                onNetWorkError(context)
                hideTipDialog()
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    val event = MessageEvent()
                    event.action = Action.DELETE_GEN_CLASS
                    event.obj = lessonEntity
                    finish()
                } else {
                    toast(context, "删除失败")
                }
            }
        }, map))
    }

    override fun obBusEvent(event: MessageEvent) {
        if (event.action == Action.CREATE_MAIN_REPLY && event.obj != null && event.obj is ReplyEntity) {
            adviseNum++
            setAdvise()
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
                if (position >= 0 && event.bundle.getSerializable("childReply") != null
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
        } else if (event.action == Action.DELETE_MAIN_REPLY && event.obj != null
                && event.obj is ReplyEntity) {
            val entity = event.obj as ReplyEntity
            if (mDatas.contains(entity)) {
                mDatas.remove(entity)
                adapter.notifyDataSetChanged()
                getAdvise()
            }
            adviseNum--
            setAdvise()
        }
    }
}