package com.haoyu.app.fragment

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.entity.CourseProgressEntity
import com.haoyu.app.entity.CourseProgressResult
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.TimeUtil.computeTimeDiff
import com.haoyu.app.view.CircleProgressBar
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.view.RoundRectProgressBar
import okhttp3.Request

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习进度
 * 作者:xiaoma
 */
class PageProgressFragment : BaseFragment() {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var llContent: LinearLayout
    private lateinit var progressBar: CircleProgressBar
    private val textViews = arrayOfNulls<TextView>(10)
    private val layoutViews = arrayOfNulls<LinearLayout>(6)
    private val progressBars = arrayOfNulls<RoundRectProgressBar>(6)
    private var training = false
    private var courseId: String? = null
    private var courseType: String? = null
    private var onSelectCallBack: OnSelectCallBack? = null

    override fun createView(): Int {
        return R.layout.fragment_page_progress
    }

    override fun initView(view: View) {
        arguments?.let {
            training = it.getBoolean("training", false)
            courseId = it.getString("entityId")
            courseType = it.getString("courseType")
        }
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        llContent = view.findViewById(R.id.ll_content)
        progressBar = view.findViewById(R.id.progressBar)
        textViews[0] = view.findViewById(R.id.tv_time)
        textViews[1] = view.findViewById(R.id.tv_score)
        textViews[2] = view.findViewById(R.id.tv_text)
        textViews[3] = view.findViewById(R.id.tv_state)
        textViews[4] = view.findViewById(R.id.tv_video)
        textViews[5] = view.findViewById(R.id.tv_html)
        textViews[6] = view.findViewById(R.id.tv_assignment)
        textViews[7] = view.findViewById(R.id.tv_test)
        textViews[8] = view.findViewById(R.id.tv_discussion)
        textViews[9] = view.findViewById(R.id.tv_survey)
        layoutViews[0] = view.findViewById(R.id.ll_video)
        layoutViews[1] = view.findViewById(R.id.ll_html)
        layoutViews[2] = view.findViewById(R.id.ll_assignment)
        layoutViews[3] = view.findViewById(R.id.ll_test)
        layoutViews[4] = view.findViewById(R.id.ll_discussion)
        layoutViews[5] = view.findViewById(R.id.ll_survey)
        progressBars[0] = view.findViewById(R.id.videoProgress)
        progressBars[1] = view.findViewById(R.id.htmlProgress)
        progressBars[2] = view.findViewById(R.id.assignmentProgress)
        progressBars[3] = view.findViewById(R.id.testProgress)
        progressBars[4] = view.findViewById(R.id.discussionProgress)
        progressBars[5] = view.findViewById(R.id.surveyProgress)
        if (courseType != null && courseType == "微课") {
            layoutViews[1]?.visibility = View.GONE
            layoutViews[2]?.visibility = View.GONE
            layoutViews[4]?.visibility = View.GONE
            layoutViews[5]?.visibility = View.GONE
        }
        for (layout in layoutViews) {
            layout?.setOnClickListener({
                onSelectCallBack?.onClickCallBack()
            })
        }
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/" + courseId + "/study/m/course/" + courseId + "/study_progress"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CourseProgressResult>() {
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(response: CourseProgressResult?) {
                loadingView.visibility = View.VISIBLE
                if (response != null && response.responseData != null) {
                    llContent.visibility = View.VISIBLE
                    updateUI(response.responseData)
                } else {
                    loadFailView.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(entity: CourseProgressEntity) {
        if (training) {
            if (entity.getmTimePeriod() != null && entity.getmTimePeriod().minutes > 0) {
                textViews[0]?.text = "距离课程结束还有：" + computeTimeDiff(entity.getmTimePeriod().minutes)
            } else {
                if (entity.getmTimePeriod() != null && entity.getmTimePeriod().state != null) textViews[0]?.text = "课程" + entity.getmTimePeriod().state else {
                    textViews[0]?.text = "课程进行中"
                }
            }
        } else {
            textViews[0]?.text = "课程已结束"
        }
        val score = entity.score
        textViews[1]?.text = score.toInt().toString()
        textViews[2]?.text = "课程得分"
        if (entity.state != null && entity.state == "pass") {
            textViews[3]?.text = "合格"
        } else if (entity.state != null && entity.state == "nopass") {
            textViews[3]?.text = "不合格"
        } else {
            textViews[3]?.text = "未登记"
        }
        progressBar.maxProgress = 100f
        progressBar.setProgress(score.toFloat(), true)
        progressBars[0]?.max = entity.activityVideoNum
        progressBars[0]?.progress = entity.completeVideoNum
        progressBars[1]?.max = entity.activityHtmlNum
        progressBars[1]?.progress = entity.completeHtmlNum
        progressBars[2]?.max = entity.activityAssignmentNum
        progressBars[2]?.progress = entity.completeAssignmentNum
        progressBars[3]?.max = entity.activityTestNum
        progressBars[3]?.progress = entity.completeTestNum
        progressBars[4]?.max = entity.activityDiscussionNum
        progressBars[4]?.progress = entity.completeDiscussionNum
        progressBars[5]?.max = entity.activitySurveyNum
        progressBars[5]?.progress = entity.completeSurveyNum
        textViews[4]?.text = "已观看" + entity.completeVideoNum + "个/" + entity.activityVideoNum + "个"
        textViews[5]?.text = "已观看" + entity.completeHtmlNum + "个/" + entity.activityHtmlNum + "个"
        textViews[6]?.text = "已完成" + entity.completeAssignmentNum + "篇/" + entity.activityAssignmentNum + "篇"
        textViews[7]?.text = "已完成" + entity.completeTestNum + "个/" + entity.activityTestNum + "个"
        textViews[8]?.text = "已参与" + entity.completeDiscussionNum + "个/" + entity.activityDiscussionNum + "个"
        textViews[9]?.text = "已完成" + entity.completeSurveyNum + "个/" + entity.completeSurveyNum + "个"
    }

    interface OnSelectCallBack {
        fun onClickCallBack()
    }

    fun setOnSelectCallBack(onSelectCallBack: OnSelectCallBack) {
        this.onSelectCallBack = onSelectCallBack
    }

}