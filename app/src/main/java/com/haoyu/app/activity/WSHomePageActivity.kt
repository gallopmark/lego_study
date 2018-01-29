package com.haoyu.app.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.gson.Gson
import com.haoyu.app.adapter.WSTaskAdapter
import com.haoyu.app.adapter.WSTaskEditAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.CommentDialog
import com.haoyu.app.dialog.DatePickerDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.student.R
import com.haoyu.app.swipe.OnActivityTouchListener
import com.haoyu.app.swipe.RecyclerTouchListener
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.ScreenUtils
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/25.
 * 描述:工作坊首页
 * 作者:xiaoma
 */
class WSHomePageActivity : BaseActivity(), View.OnClickListener, RecyclerTouchListener.RecyclerTouchListenerHelper {
    private var context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var ssvContent: StickyScrollView
    private lateinit var tvState: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyTask: TextView//没有阶段任务
    private lateinit var tvBottom: TextView
    private val mDatas = ArrayList<MultiItemEntity>()
    private lateinit var taskAdapter: WSTaskAdapter
    private lateinit var taskEditAdapter: WSTaskEditAdapter
    private var training: Boolean = false
    private lateinit var workshopId: String
    private var roleInws: String? = null
    private var canEdit: Boolean = false
    private var activityIndex: Int = 0
    private var touchListener: OnActivityTouchListener? = null
    override fun setLayoutResID(): Int {
        return R.layout.activity_wshomepage
    }

    override fun initView() {
        training = intent.getBooleanExtra("training", false)
        workshopId = intent.getStringExtra("workshopId")
        val title = intent.getStringExtra("workshopTitle")
        findViews()
        setToolBar()
        toolBar.setTitle_text(title)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        taskAdapter = WSTaskAdapter(context, mDatas)
        taskEditAdapter = WSTaskEditAdapter(context, mDatas)
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        ssvContent = findViewById(R.id.ssv_content)
        tvState = findViewById(R.id.tv_state)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmptyTask = findViewById(R.id.tv_emptyTask)
        tvBottom = findViewById(R.id.tv_bottom)
    }

    private fun setToolBar() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                showPopupWindow()
            }
        })
    }

    override fun initData() {
        loadingView.visibility = View.VISIBLE
        addSubscription(Flowable.fromCallable { onCall() }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    loadingView.visibility = View.GONE
                    updateUI(response)
                }, {
                    loadingView.visibility = View.GONE
                    loadFailView.visibility = View.GONE
                }))
    }

    @Throws(Exception::class)
    private fun onCall(): WorkShopResult? {
        var url = "${Constants.OUTRT_NET}/m/workshop/$workshopId"
        var json = OkHttpClientManager.getAsString(context, url)
        val gson = Gson()
        val response = gson.fromJson(json, WorkShopResult::class.java)
        response?.responseData?.getmWorkshopSections()?.let {
            for (i in 0 until it.size) {
                url = "${Constants.OUTRT_NET}/m/activity/wsts/${it[i].id}"
                json = OkHttpClientManager.getAsString(context, url)
                val result = gson.fromJson(json, WSActivities::class.java)
                result?.responseData?.let { response.responseData.getmWorkshopSections()[i].activities = it }
            }
        }
        return response
    }

    private fun updateUI(response: WorkShopResult?) {
        if (response?.getResponseData() != null) {
            showContent()
            val mWorkshop = response.getResponseData().getmWorkshop()
            val mWorkshopUser = response.getResponseData().getmWorkshopUser()
            val mWorkshopSections = response.getResponseData().getmWorkshopSections()
            mWorkshopUser?.role?.let {
                roleInws = it
                if (roleInws == "master") {
                    canEdit = true
                }
            }
            updateUI(mWorkshop, mWorkshopUser)
            updateUI(mWorkshopSections)
        } else {
            val tvEmpty = findViewById<TextView>(R.id.tv_empty)
            tvEmpty.text = "暂无工作坊信息~"
            tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun showContent() {
        toolBar.setShow_right_button(true)
        setProgressLayout()
        ssvContent.visibility = View.VISIBLE
        val llQuestion = findViewById<LinearLayout>(R.id.ll_question)
        val llExchange = findViewById<LinearLayout>(R.id.ll_exchange)
        llQuestion.setOnClickListener(context)
        llExchange.setOnClickListener(context)
    }

    private fun setProgressLayout() {
        val width = ScreenUtils.getScreenWidth(context) / 3
        val rlCpb1 = findViewById<RelativeLayout>(R.id.rl_cpb1)
        val cpb1Params = rlCpb1.layoutParams as LinearLayout.LayoutParams
        cpb1Params.width = width
        cpb1Params.height = width
        rlCpb1.layoutParams = cpb1Params
        val rlCpb2 = findViewById<RelativeLayout>(R.id.rl_cpb2)
        val cpb2Params = rlCpb2.layoutParams as LinearLayout.LayoutParams
        cpb2Params.width = width
        cpb2Params.height = width
        rlCpb2.layoutParams = cpb2Params
        val params = tvState.layoutParams as LinearLayout.LayoutParams
        params.leftMargin = -width / 8
        tvState.layoutParams = params
    }

    private fun updateUI(mWorkshop: WorkShopMobileEntity?, mWorkshopUser: WorkShopMobileUser?) {
        val cpb1 = findViewById<CircleProgressBar>(R.id.cpb1)
        val tvDay = findViewById<TextView>(R.id.tv_day)
        if (mWorkshop?.getmTimePeriod() != null) {
            val timePeriod = mWorkshop.getmTimePeriod()
            val remainDay = (timePeriod.minutes / 60 / 24).toInt()
            val allDay = getAllDay(timePeriod.startTime, timePeriod.endTime)
            val expandDay = allDay - remainDay
            setTimePeriod(expandDay, allDay, tvDay)
            if (timePeriod.minutes > 0) {
                tvState.visibility = View.INVISIBLE
            } else {
                tvState.visibility = View.VISIBLE
                if (TextUtils.isEmpty(timePeriod.state)) {
                    tvState.text = "已结束"
                } else {
                    tvState.text = timePeriod.state
                }
            }
        } else {
            tvState.visibility = View.INVISIBLE
            tvDay.textSize = 14f
            tvDay.text = "工作坊研修\n进行中"
            cpb1.maxProgress = 100f
            cpb1.setProgress(0f)
        }
        var qualityPoint = 0
        var point = 0
        mWorkshop?.let { qualityPoint = it.qualifiedPoint }
        mWorkshopUser?.let { point = it.point.toInt() }
        setPoint(point, qualityPoint)
    }

    private fun getAllDay(startTime: Long, endTime: Long): Int {
        val interval = (endTime - startTime) / 1000
        val day = if (interval / 24 * 60 * 60 == 0L) 1 else (interval / (24 * 60 * 60)).toInt()
        return if (day <= 0) {
            0
        } else day
    }

    private fun setTimePeriod(expandDay: Int, allDay: Int, tvDay: TextView) {
        val cpb1 = findViewById<CircleProgressBar>(R.id.cpb1)
        cpb1.maxProgress = allDay.toFloat()
        cpb1.setProgress(expandDay.toFloat(), true)
        val text = expandDay.toString() + "\n已开展\n共" + allDay + "天"
        val ssb = SpannableString(text)
        var start = 0
        var end = text.indexOf("已") - 1
        ssb.setSpan(AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        start = text.indexOf("已")
        end = text.indexOf("共") - 1
        ssb.setSpan(AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        start = text.indexOf("共")
        end = text.length
        ssb.setSpan(AbsoluteSizeSpan(10, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvDay.text = ssb
    }

    private fun setPoint(point: Int, qualityPoint: Int) {
        val cpb2 = findViewById<CircleProgressBar>(R.id.cpb2)
        val tvScore = findViewById<TextView>(R.id.tv_score)
        cpb2.maxProgress = qualityPoint.toFloat()
        cpb2.setProgress(point.toFloat(), true)
        val text = point.toString() + "\n研修积分\n（达标积分" + qualityPoint + "）"
        val ssb = SpannableString(text)
        var start = 0
        var end = text.indexOf("研") - 1
        ssb.setSpan(AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        start = text.indexOf("研")
        end = text.indexOf("分") + 1
        ssb.setSpan(AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        start = text.indexOf("分") + 1
        end = text.length
        ssb.setSpan(AbsoluteSizeSpan(10, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvScore.text = ssb
    }

    private fun updateUI(sections: List<MWorkshopSection>) {
        if (sections.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            if (canEdit) {
                recyclerView.adapter = taskEditAdapter
                addTypeEdit(sections)
                tvBottom.visibility = View.VISIBLE
            } else {
                recyclerView.adapter = taskAdapter
                addTypeDef(sections)
                tvBottom.visibility = View.GONE
            }
        } else {
            recyclerView.visibility = View.GONE
            tvEmptyTask.visibility = View.VISIBLE
        }
    }

    private fun addTypeEdit(sections: List<MWorkshopSection>) {
        val list = ArrayList<MultiItemEntity>()
        for (i in sections.indices) {
            val section = sections[i]
            section.position = i
            val crease = MWSActivityCrease()
            crease.tag = section
            section.crease = crease
            list.add(section)
            for (j in 0 until section.activities.size) {
                val activity = section.activities[j]
                activity.tag = section
                list.add(activity)
            }
            list.add(crease)
        }
        taskEditAdapter.addItemEntities(list)
        setTaskEditAdapter(taskEditAdapter)
    }

    private fun addTypeDef(sections: List<MWorkshopSection>) {
        val list = ArrayList<MultiItemEntity>()
        for (i in sections.indices) {
            val section = sections[i]
            section.position = i
            list.add(section)
            (0 until section.activities.size).mapTo(list) { section.activities[it] }
        }
        taskAdapter.addItemEntities(list)
        setTaskAdapter(taskAdapter)
    }

    private fun setTaskAdapter(mAdapter: WSTaskAdapter) {
        val onTouchListener = RecyclerTouchListener(context, recyclerView)
        onTouchListener.setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                val itemType = mDatas[position].itemType
                if (itemType == 1) {
                    mAdapter.collapse(position)
                } else {
                    mAdapter.setSelected(position)
                    val activity = mDatas[position] as MWorkshopActivity
                    getActivityInfo(activity.id)
                }
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {

            }
        })
        recyclerView.addOnItemTouchListener(onTouchListener)
    }

    private fun setTaskEditAdapter(mAdapter: WSTaskEditAdapter) {
        val onTouchListener = RecyclerTouchListener(context, recyclerView)
        onTouchListener.setIgnoredViewTypes(3, 4).setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                val itemType = mDatas[position].itemType
                if (itemType == 1) {
                    mAdapter.collapse(position)
                } else {
                    mAdapter.setSelected(position)
                    val activity = mDatas[position] as MWorkshopActivity
                    getActivityInfo(activity.id)
                }
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {

            }
        }).setSwipeOptionViews(R.id.ll_alert, R.id.ll_delete)
                .setSwipeable(R.id.ll_rowFG, R.id.ll_rowBG) { viewID, position ->
                    val itemType = mDatas[position].itemType
                    if (viewID == R.id.ll_alert) {
                        if (itemType == 1) {
                            val section = mDatas.removeAt(position) as MWorkshopSection
                            val crease = MWSSectionCrease()
                            crease.tag = section
                            mAdapter.addItem(position, crease)
                        }
                    } else {
                        if (itemType == 1) {
                            val section = mDatas[position] as MWorkshopSection
                            val taskId = section.id
                            val dialog = MaterialDialog(context)
                            dialog.setTitle("温馨提示")
                            dialog.setMessage("确定删除此阶段吗？")
                            dialog.setPositiveButton("确定") { _, _ -> deleteTask(taskId, position) }
                            dialog.setNegativeButton("取消", null)
                            dialog.show()
                        } else {
                            val dialog = MaterialDialog(context)
                            dialog.setTitle("温馨提示")
                            dialog.setMessage("确定删除此任务吗？")
                            dialog.setPositiveButton("确定") { _, _ -> deleteActivity(position) }
                            dialog.setNegativeButton("取消", null)
                            dialog.show()
                        }
                    }
                }
        recyclerView.addOnItemTouchListener(onTouchListener)
        val independentListener = RecyclerTouchListener(context, recyclerView)
        independentListener.setIgnoredViewTypes(1, 2, 4).setIndependentViews(R.id.tv_discuss, R.id.tv_cc, R.id.tv_ts).setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {

            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
                val crease = mDatas[position] as MWSActivityCrease
                val section = crease.tag
                val sectionId = section.id
                val intent = Intent().apply {
                    putExtra("workshopId", workshopId)
                    putExtra("workSectionId", sectionId)
                }
                activityIndex = mDatas.indexOf(section)
                when (independentViewID) {
                    R.id.tv_discuss -> intent.setClass(context, WSTDEditActivity::class.java)
                    R.id.tv_cc -> intent.setClass(context, WSCDEditActivity::class.java)
                    else -> intent.setClass(context, WSTSEditActivity::class.java)
                }
                startActivityForResult(intent, 1)
            }
        })
        recyclerView.addOnItemTouchListener(independentListener)
        mAdapter.setOnEditTaskListener(object : WSTaskEditAdapter.OnEditTaskListener {
            private var startTime: String? = null
            private var endTime: String? = null

            override fun inputTitle(tvTitle: TextView) {
                showInputDialog(tvTitle)
            }

            override fun inputTime(tvTime: TextView) {
                val dialog = DatePickerDialog(context, true)
                dialog.setDatePickerListener { startYear, startMonth, _, endYear, endMonth, _ ->
                    startTime = "$startYear-" + if (startMonth < 10) "0$startMonth" else startMonth
                    endTime = "$endYear-" + if (endMonth < 10) "0$endMonth" else endMonth
                    val mStartTime = "${startYear}年" + (if (startMonth < 10) "0$startMonth" else startMonth) + "月"
                    val mEndTime = "${endYear}年" + (if (endMonth < 10) "0$endMonth" else endMonth) + "月"
                    tvTime.text = "研修时间：$mStartTime-$mEndTime"
                }
                dialog.show()
            }

            override fun addTask(tvTitle: TextView, tvTime: TextView, sortNum: Int) {
                val title = tvTitle.text.toString().trim()
                val time = tvTime.text.toString().trim()
                if (checkText(title, time)) {
                    removeFromBottom()
                    addStage(title, startTime, endTime, sortNum)
                    tvTitle.text = null
                    tvTime.text = null
                }
            }

            override fun alertTask(tvTitle: TextView, tvTime: TextView, tag: MWorkshopSection, position: Int) {
                val title = tvTitle.text.toString().trim()
                val time = tvTime.text.toString().trim()
                if (checkText(title, time)) {
                    alterTask(tag, title, startTime, endTime, position)
                }
            }

            private fun checkText(title: String, time: String): Boolean {
                if (TextUtils.isEmpty(title)) {
                    toast(context, "请输入阶段标题")
                    return false
                } else if (TextUtils.isEmpty(time)) {
                    toast(context, "请选择研修时间")
                    return false
                }
                return true
            }

            override fun cancelAdd() {
                removeFromBottom()
            }

            override fun cancelAlert(section: MWorkshopSection, position: Int) {
                mDatas.removeAt(position)
                mAdapter.addItem(position, section)
            }
        })
    }

    /*获取活动相关信息*/
    private fun getActivityInfo(activityId: String) {
        val url = "${Constants.OUTRT_NET}/student_$workshopId/m/activity/wsts/$activityId/view"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: AppActivityViewResult) {
                hideTipDialog()
                getIntoActivity(response)
            }
        }))
    }

    private fun getIntoActivity(response: AppActivityViewResult?) {
        response?.responseData?.getmActivityResult()?.getmActivity()?.let {
            val activity = it
            if (TextUtils.isEmpty(activity.type)) {
                toast(context, "系统暂不支持浏览，请到网站完成。")
            } else {
                when (activity.type) {
                    "lesson_plan" -> toast(context, "系统暂不支持浏览，请到网站完成。") //集体备课
                    "discussion" -> openDiscussion(response, activity) //教学研讨
                    "survey" -> openSurvey(response, activity) //问卷调查
                    "debate" -> toast(context, "系统暂不支持浏览，请到网站完成。")  //在线辩论
                    "test" -> openTest(response, activity) //教学测验
                    "video" -> playVideo(response, activity) //教学观摩
                    "lcec" -> openLcec(response, activity) //听课评课
                    "discuss_class" -> openDiscussClass(response, activity) //评课议课
                    else -> toast(context, "系统暂不支持浏览，请到网站完成。")
                }
            }
        }
    }

    /*播放视频*/
    private fun playVideo(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData.getmVideoUser() != null) {  //教学视频
            val videoEntity = response.responseData.getmVideoUser()
            videoEntity.getmVideo()?.let {
                val intent = Intent(context, IJKPlayerActivity::class.java)
                val timePeriod = activity.getmTimePeriod()
                if (training && timePeriod?.state != null && timePeriod.state == "进行中") {
                    intent.putExtra("running", true)
                } else {
                    if (training && timePeriod != null && timePeriod.minutes > 0) {
                        intent.putExtra("running", true)
                    } else {
                        intent.putExtra("running", false)
                    }
                }
                intent.putExtra("workshopId", workshopId)
                intent.putExtra("activityId", activity.id)
                intent.putExtra("videoTitle", activity.title)
                intent.putExtra("videoId", videoEntity.id)
                intent.putExtra("video", it)
                if (!TextUtils.isEmpty(it.urls)) {
                    intent.putExtra("videoUrl", it.urls)
                    startActivity(intent)
                } else if (it.videoFiles.size > 0) {
                    intent.putExtra("videoUrl", it.videoFiles[0].url)
                    startActivity(intent)
                }
            }
        } else {
            toast(context, "系统暂不支持浏览，请到网站完成。")
        }
    }

    /*打开听课评课*/
    private fun openLcec(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData.getmLcec() != null) {
            val intent = Intent(context, WSTSInfoActivity::class.java)
            val timePeriod = activity.getmTimePeriod()
            if (training && timePeriod?.state != null && timePeriod.state == "进行中") {
                intent.putExtra("running", true)
            } else {
                if (training && timePeriod != null && timePeriod.minutes > 0) {
                    intent.putExtra("running", true)
                } else {
                    intent.putExtra("running", false)
                }
            }
            intent.putExtra("timePeriod", timePeriod)
            intent.putExtra("workshopId", workshopId)
            intent.putExtra("activityId", activity.id)
            intent.putExtra("activityTitle", activity.title)
            intent.putExtra("mlcec", response.responseData.getmLcec())
            startActivity(intent)
        } else {
            toast(context, "系统暂不支持浏览，请到网站完成。")
        }
    }

    /*打开课程研讨*/
    private fun openDiscussion(response: AppActivityViewResult, activity: CourseSectionActivity) {
        val discussUser = response.responseData.getmDiscussionUser()
        if (discussUser != null) {
            val intent = Intent(context, TeachingDiscussionActivity::class.java)
            val timePeriod = activity.getmTimePeriod()
            if (training && timePeriod?.state != null && timePeriod.state == "进行中") {
                intent.putExtra("running", true)
            } else {
                if (training && timePeriod != null && timePeriod.minutes > 0) {
                    intent.putExtra("running", true)
                } else {
                    intent.putExtra("running", false)
                }
            }
            intent.putExtra("discussType", "workshop")
            intent.putExtra("workshopId", workshopId)
            intent.putExtra("activityId", activity.id)
            intent.putExtra("activityTitle", activity.title)
            intent.putExtra("timePeriod", activity.getmTimePeriod())
            intent.putExtra("discussUser", discussUser)
            intent.putExtra("mainNum", discussUser.mainPostNum)
            intent.putExtra("subNum", discussUser.subPostNum)
            if (discussUser.getmDiscussion() != null) {
                val entity = discussUser.getmDiscussion()
                intent.putExtra("needMainNum", entity.mainPostNum)
                intent.putExtra("needSubNum", entity.subPostNum)
            }
            startActivity(intent)
        } else {
            toast(context, "系统暂不支持浏览，请到网站完成。")
        }
    }

    /*打开问卷调查*/
    private fun openSurvey(response: AppActivityViewResult, activity: CourseSectionActivity) {
        val intent = Intent(context, AppSurveyHomeActivity::class.java)
        val timePeriod = activity.getmTimePeriod()
        if (training && timePeriod?.state != null && timePeriod.state == "进行中") {
            intent.putExtra("running", true)
        } else {
            if (training && timePeriod != null && timePeriod.minutes > 0) {
                intent.putExtra("running", true)
            } else {
                intent.putExtra("running", false)
            }
        }
        intent.putExtra("relationId", workshopId)
        intent.putExtra("type", "workshop")
        intent.putExtra("timePeriod", activity.getmTimePeriod())
        if (response.responseData.getmSurveyUser() != null) {
            intent.putExtra("surveyUser", response.responseData.getmSurveyUser())
        }
        intent.putExtra("activityId", activity.id)
        intent.putExtra("activityTitle", activity.title)
        startActivity(intent)
    }

    /*打开测验*/
    private fun openTest(response: AppActivityViewResult, activity: CourseSectionActivity) {
        val intent = Intent()
        val timePeriod = activity.getmTimePeriod()
        if (training && timePeriod?.state != null && timePeriod.state == "进行中") {
            intent.putExtra("running", true)
        } else {
            if (training && timePeriod != null && timePeriod.minutes > 0) {
                intent.putExtra("running", true)
            } else {
                intent.putExtra("running", false)
            }
        }
        intent.putExtra("relationId", workshopId)
        intent.putExtra("testType", "workshop")
        intent.putExtra("timePeriod", activity.getmTimePeriod())
        intent.putExtra("activityId", activity.id)
        intent.putExtra("activityTitle", activity.title)
        if (response.responseData.getmTestUser() != null) {
            intent.putExtra("testUser", response.responseData.getmTestUser())
        }
        response.responseData.getmTestUser()?.completionStatus?.let {
            if (it == "completed") {
                intent.putExtra("score", response.responseData.getmActivityResult().score)
                intent.setClass(context, AppTestResultActivity::class.java)
                startActivity(intent)
                return
            }
        }
        intent.setClass(context, AppTestHomeActivity::class.java)
        startActivity(intent)
    }

    private fun openDiscussClass(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData.getmVideoDC() != null) {
            val intent = Intent(context, WSCDInfoActivity::class.java)
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
                intent.putExtra("running", true)
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
                intent.putExtra("running", true)
            else
                intent.putExtra("running", false)
            intent.putExtra("timePeriod", activity.getmTimePeriod())
            intent.putExtra("workshopId", workshopId)
            intent.putExtra("activityId", activity.id)
            intent.putExtra("activityTitle", activity.title)
            intent.putExtra("discussClass", response.responseData.getmVideoDC())
            startActivity(intent)
        } else {
            toast(context, "系统暂不支持浏览，请到网站完成。")
        }
    }

    //删除活动
    private fun deleteActivity(position: Int) {
        val activity = mDatas[position] as MWorkshopActivity
        val activityId = activity.id
        val url = "${Constants.OUTRT_NET}/master_$workshopId/unique_uid_$userId/m/activity/wsts/$activityId"
        val map = HashMap<String, String>().apply { put("_method", "delete") }
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
                    taskEditAdapter.removeActivity(position)
                } else {
                    toast(context, "删除失败，请稍后再试")
                }
            }
        }, map))
    }

    private fun showInputDialog(tvTitle: TextView) {
        val hint = "输出阶段标题"
        val etText = tvTitle.text.toString()
        val dialog = CommentDialog(context, hint, etText, "完成")
        dialog.setSendCommentListener(object : CommentDialog.OnSendCommentListener {
            override fun sendComment(content: String) {
                tvTitle.text = content
            }
        })
        dialog.show()
    }

    //添加新阶段
    private fun addStage(title: String, startTime: String?, endTime: String?, sortNum: Int) {
        val url = "${Constants.OUTRT_NET}/master_$workshopId/unique_uid_$userId/m/workshop_section"
        val map = HashMap<String, String>().apply {
            put("workshopId", workshopId)
            put("title", title)
            startTime?.let {
                put("startTime", it)
            }
            endTime?.let {
                put("endTime", it)
            }
            put("sortNum", sortNum.toString())
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<WorkshopPhaseResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: WorkshopPhaseResult?) {
                hideTipDialog()
                if (response?.responseData != null) {
                    if (tvEmptyTask.visibility != View.GONE) tvEmptyTask.visibility = View.GONE
                    if (recyclerView.visibility != View.VISIBLE) recyclerView.visibility = View.VISIBLE
                    val section = response.responseData
                    val crease = MWSActivityCrease()
                    crease.tag = section
                    section.crease = crease
                    taskEditAdapter.addItem(section)
                    taskEditAdapter.addItem(crease)
                } else {
                    toastFullScreen("添加失败", false)
                }
            }
        }, map))
    }

    /*修改阶段*/
    private fun alterTask(section: MWorkshopSection, title: String, startTime: String?, endTime: String?, position: Int) {
        val taskId = section.id
        val url = "${Constants.OUTRT_NET}/master_$workshopId/unique_uid_$userId/m/workshop_section/$taskId"
        val map = HashMap<String, String>().apply {
            put("_method", "put")
            put("title", title)
            startTime?.let {
                put("startTime", it)
            }
            endTime?.let {
                put("endTime", it)
            }
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                e.printStackTrace()
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    mDatas.removeAt(position)
                    section.title = title
                    if (startTime != null || endTime != null) {
                        section.timePeriod = TimePeriod().apply {
                            this.startTime = TimeUtil.dateToLong(startTime, "yyyy-MM")
                            this.endTime = TimeUtil.dateToLong(endTime, "yyyy-MM")
                        }
                    }
                    taskEditAdapter.addItem(position, section)
                } else {
                    toastFullScreen("修改失败", false)
                }
            }
        }, map))
    }

    /*删除阶段*/
    private fun deleteTask(id: String, position: Int) {
        val url = "${Constants.OUTRT_NET}/master_$workshopId/unique_uid_$userId/m/workshop_section/$id"
        val map = HashMap<String, String>().apply { put("_method", "delete") }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                super.onBefore(request)
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                toast(context, "删除失败，请稍后再试")
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    taskEditAdapter.removeItem(position)
                    if (mDatas.size == 0) {
                        tvEmptyTask.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                } else {
                    toast(context, "删除失败，请稍后再试")
                }
            }
        }, map))
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val activity = data.getSerializableExtra("activity") as MWorkshopActivity
            taskEditAdapter.addActivity(activityIndex, activity)
        }
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        tvBottom.setOnClickListener(context)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_question -> {
                val intent = Intent(context, WorkshopQuestionActivity::class.java)
                intent.putExtra("relationId", workshopId)
                startActivity(intent)
            }
            R.id.ll_exchange -> {
                val intent = Intent(context, WSFreeChatActiviy::class.java)
                intent.putExtra("relationId", workshopId)
                intent.putExtra("role", role)
                startActivity(intent)
            }
            R.id.tv_bottom   //添加新阶段
            -> smoothToBottom()
        }
    }

    private fun smoothToBottom() {
        if (mDatas.size == 0) {
            recyclerView.visibility = View.VISIBLE
            tvEmptyTask.visibility = View.GONE
        }
        val crease = MWSSectionCrease()
        taskEditAdapter.addItem(crease)
        tvBottom.visibility = View.GONE
        ssvContent.postDelayed({ ssvContent.fullScroll(ScrollView.FOCUS_DOWN) }, 10)
    }

    private fun removeFromBottom() {
        if (mDatas.size == 0) {
            recyclerView.visibility = View.GONE
            tvEmptyTask.visibility = View.VISIBLE
        }
        taskEditAdapter.removeItem(mDatas.size - 1)
        tvBottom.visibility = View.VISIBLE
    }

    private fun showPopupWindow() {
        val popupView = layoutInflater.inflate(R.layout.popwindow_workshop_menu, LinearLayout(context), false)
        val pw = SupportPopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
        val llNotice = popupView.findViewById<View>(R.id.ll_notice)
        val llIntroduct = popupView.findViewById<View>(R.id.ll_introduct)
        llNotice.setOnClickListener {
            pw.dismiss()
            val intent = Intent(context, AnnouncementActivity::class.java)
            intent.putExtra("relationId", workshopId)
            intent.putExtra("relationType", "workshop")
            intent.putExtra("type", "workshop_announcement")
            startActivity(intent)
        }
        llIntroduct.setOnClickListener {
            pw.dismiss()
            val intent = Intent(context, WSDetailActivity::class.java)
            intent.putExtra("workshopId", workshopId)
            startActivity(intent)
        }
        popupView.setOnClickListener { pw.dismiss() }
        pw.isTouchable = true
        pw.setBackgroundDrawable(ColorDrawable())
        pw.isOutsideTouchable = true
        val view = toolBar.iv_rightImage
        pw.showAsDropDown(view, 0, -10)
    }

    override fun setOnActivityTouchListener(listener: OnActivityTouchListener) {
        this.touchListener = listener
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        touchListener?.getTouchCoordinates(ev)
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }
}