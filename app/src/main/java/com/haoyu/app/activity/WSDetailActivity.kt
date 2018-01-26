package com.haoyu.app.activity

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.adapter.BriefingAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.*
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.view.PullToLoadMoreLayout
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/26.
 * 描述:工作坊简介页面
 * 作者:xiaoma
 */
class WSDetailActivity : BaseActivity(), PullToLoadMoreLayout.OnPullToLeftListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var nsvContent: NestedScrollView
    private lateinit var pullGroup: PullToLoadMoreLayout
    private lateinit var recyclerView: RecyclerView
    private var workshopId: String? = null
    private val mDatas = ArrayList<MobileUser>()
    private lateinit var adapter: UsersAdapter
    private var isLoadMore = false
    private var page = 1
    override fun setLayoutResID(): Int {
        return R.layout.activity_wsdetail
    }

    override fun initView() {
        workshopId = intent.getStringExtra("workshopId")
        findViews()
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        adapter = UsersAdapter(mDatas)
        recyclerView.adapter = adapter
        pullGroup.setOnPullToLeftListener(context)
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        nsvContent = findViewById(R.id.nsv_content)
        pullGroup = findViewById(R.id.pull_group)
        recyclerView = findViewById(R.id.rv_student)
        toolBar.setOnLeftClickListener { finish() }
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/workshop/$workshopId/detail"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<WSDetailResult>() {
            val loadingView = findViewById<LoadingView>(R.id.loadingView)
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                val loadFailView = findViewById<LoadFailView>(R.id.loadFailView)
                loadFailView.visibility = View.VISIBLE
                loadFailView.setOnRetryListener { initData() }
            }

            override fun onResponse(response: WSDetailResult?) {
                loadingView.visibility = View.GONE
                if (response?.responseData?.getmWorkshop() != null) {
                    updateUI(response.responseData.getmWorkshop(), response.responseData.getmFileInfo())
                    getUsers()
                    getBriefList()
                } else {
                    val tvEmpty = findViewById<TextView>(R.id.tv_empty)
                    tvEmpty.text = "没有此工作坊信息"
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    @Suppress("DEPRECATION")
    private fun updateUI(entity: WorkShopMobileEntity, fileInfo: MFileInfo?) {
        workshopId = entity.id
        nsvContent.visibility = View.VISIBLE
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvTrain = findViewById<TextView>(R.id.tv_train)
        val tvContent = findViewById<TextView>(R.id.tv_content)
        val tvES = findViewById<TextView>(R.id.tv_emptySummary)
        tvTitle.text = entity.title
        entity.trainName?.let {
            val tvProject = findViewById<TextView>(R.id.tv_project)
            tvProject.text = "所属项目：$it"
            tvProject.visibility = View.VISIBLE
        }
        entity.timePeriod?.let {
            val tvTime = findViewById<TextView>(R.id.tv_time)
            tvTime.text = "研修时间：${TimeUtil.getSlashDate(it.startTime) + "~" +
                    TimeUtil.getSlashDate(it.endTime)}"
            tvTime.visibility = View.VISIBLE
        }
        entity.creator?.let {
            val tvCreator = findViewById<TextView>(R.id.tv_creator)
            tvCreator.text = "　创建人：${it.realName}"
            tvContent.visibility = View.VISIBLE
        }
        entity.type?.let {
            val tvType = findViewById<TextView>(R.id.tv_type)
            var type = "\u3000\u3000类型："
            when (it) {
                "personal" -> type += "个人工作坊"
                "train" -> type += "项目工作坊"
                "template" -> type += "示范性工作坊"
                else -> type += "--"
            }
            tvType.text = type
            tvType.visibility = View.VISIBLE
        }
        tvTrain.text = "培训时间：${entity.studyHours}学时"
        if (!TextUtils.isEmpty(entity.summary)) {
            val spanned = Html.fromHtml(entity.summary)
            tvContent.text = spanned
            tvContent.visibility = View.VISIBLE
            tvES.visibility = View.GONE
        } else {
            tvContent.visibility = View.GONE
            tvES.visibility = View.VISIBLE
        }
        setNumText(entity.studentNum, entity.activityNum, entity.faqQuestionNum, entity.resourceNum)
        val llFL = findViewById<LinearLayout>(R.id.ll_fileLayout)
        val tvEF = findViewById<TextView>(R.id.tv_emptyFile)
        if (fileInfo != null) {
            llFL.visibility = View.VISIBLE
            val ivType = llFL.findViewById<ImageView>(R.id.iv_fileType)
            val tvFileName = llFL.findViewById<TextView>(R.id.tv_mFileName)
            val tvFileSize = llFL.findViewById<TextView>(R.id.tv_mFileSize)
            Common.setFileType(fileInfo.url, ivType)
            tvFileName.text = fileInfo.fileName
            tvFileSize.text = Common.FormetFileSize(fileInfo.fileSize)
            tvEF.visibility = View.GONE
            llFL.setOnClickListener({
                val intent = Intent(context, MFileInfoActivity::class.java)
                intent.putExtra("fileInfo", fileInfo)
                startActivity(intent)
            })
        } else {
            llFL.visibility = View.GONE
            tvEF.visibility = View.VISIBLE
        }
    }

    private fun setNumText(studentNum: Int, activityNum: Int, faqQuestionNum: Int, resourceNum: Int) {
        val tvStudents = findViewById<TextView>(R.id.tv_students)
        val tvTasks = findViewById<TextView>(R.id.tv_tasks)
        val tvQuestions = findViewById<TextView>(R.id.tv_questions)
        val tvResources = findViewById<TextView>(R.id.tv_resources)
        var ssb: SpannableString
        val start = 0
        var end: Int
        val color = ContextCompat.getColor(context, R.color.darksalmon)
        val textStudy = studentNum.toString() + "\n参研学员"
        ssb = SpannableString(textStudy)
        end = textStudy.indexOf("参") - 1
        ssb.setSpan(AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvStudents.text = ssb
        val textActivity = activityNum.toString() + "\n研修任务"
        ssb = SpannableString(textActivity)
        end = textActivity.indexOf("研") - 1
        ssb.setSpan(AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvTasks.text = ssb
        val textQuestion = faqQuestionNum.toString() + "\n学员提问"
        ssb = SpannableString(textQuestion)
        end = textQuestion.indexOf("学") - 1
        ssb.setSpan(AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvQuestions.text = ssb
        val textResource = resourceNum.toString() + "\n学习资源"
        ssb = SpannableString(textResource)
        end = textResource.indexOf("学") - 1
        ssb.setSpan(AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvResources.text = ssb
    }

    /*获取优秀学员*/
    private fun getUsers() {
        val url = "${Constants.OUTRT_NET}/m/workshop_user/$workshopId/excellent_users?page=$page&limit=20"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<WSExecllentUsers>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: WSExecllentUsers?) {
                hideTipDialog()
                if (response?.responseData != null && response.responseData.getmWorkshopUsers().size > 0) {
                    updateUserPage(response.responseData.getmWorkshopUsers(), response.responseData.paginator)
                } else {
                    if (isLoadMore) {
                        pullGroup.completeToUpload()
                        pullGroup.setLoadingMoreEnabled(false)
                    } else {
                        pullGroup.visibility = View.GONE
                        val tvES = findViewById<TextView>(R.id.tv_emptyStudent)
                        tvES.visibility = View.VISIBLE
                    }
                }
            }
        }))
    }

    /*更新优秀学员page*/
    private fun updateUserPage(mDatas: List<WorkShopMobileUser>, paginator: Paginator?) {
        mDatas.indices.filter { mDatas[it].getmUser() != null }.mapTo(this.mDatas) { mDatas[it].getmUser() }
        adapter.notifyDataSetChanged()
        if (isLoadMore) {
            pullGroup.completeToUpload()
        }
        if (paginator != null && paginator.hasNextPage) {
            pullGroup.setLoadingMoreEnabled(true)
        } else {
            pullGroup.setLoadingMoreEnabled(false)
        }
    }

    private fun getBriefList() {
        val url = "${Constants.OUTRT_NET}/m/briefing?announcementRelations[0].relation.id=$workshopId&announcementRelations[0].relation.type=workshop&type=workshop_briefing&orders=CREATE_TIME.DESC&limit=5"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BriefingsResult>() {

            override fun onError(request: Request, e: Exception) {
                onNetWorkError(context)
            }

            override fun onResponse(response: BriefingsResult?) {
                if (response?.getResponseData() != null && response.getResponseData().announcements.size > 0) {
                    updateBriefList(response.getResponseData().announcements, response.getResponseData().paginator)
                } else {
                    val tvEmpty = findViewById<TextView>(R.id.tv_emptyBrief)
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    /*刷新研修简报列表*/
    private fun updateBriefList(mDatas: List<BriefingEntity>, paginator: Paginator?) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        val adapter = BriefingAdapter(mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener({ _, _, _, position ->
            if (position in 0..mDatas.size) {
                val intent = Intent(context, BriefingDetailActivity::class.java)
                intent.putExtra("relationId", mDatas[position].id)
                startActivity(intent)
            }
        })
        paginator?.hasNextPage.let {
            val btMore = findViewById<TextView>(R.id.bt_more)
            btMore.visibility = View.VISIBLE
            btMore.setOnClickListener {
                val intent = Intent(context, BriefingActivity::class.java).apply {
                    putExtra("relationId", workshopId)
                    putExtra("relationType", "workshop")
                    putExtra("type", "workshop_briefing")
                }
                startActivity(intent)
            }
        }
    }

    private inner class UsersAdapter(mDatas: List<MobileUser>) : BaseArrayRecyclerAdapter<MobileUser>(mDatas) {
        private val width = ScreenUtils.getScreenWidth(context) / 4

        override fun bindView(viewtype: Int): Int {
            return R.layout.peer_item
        }

        override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, user: MobileUser, position: Int) {
            val params = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.CENTER
            params.bottomMargin = PixelFormat.dp2px(context, 16f)
            holder.itemView.layoutParams = params
            val iv = holder.obtainView<ImageView>(R.id.person_img)
            val tv = holder.obtainView<TextView>(R.id.person_name)
            tv.text = user.realName
            GlideImgManager.loadCircleImage(context, user.avatar, R.drawable.user_default, R.drawable.user_default, iv)
        }
    }

    override fun onReleaseFingerToUpload() {
        isLoadMore = true
        page++
        getUsers()
    }

    override fun onStartToUpload() {
    }

}