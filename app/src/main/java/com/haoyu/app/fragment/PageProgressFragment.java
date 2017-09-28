package com.haoyu.app.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.CourseProgressEntity;
import com.haoyu.app.entity.CourseProgressResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.ColorArcProgressBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RoundRectProgressBar;

import butterknife.BindView;
import butterknife.BindViews;
import okhttp3.Request;

/**
 * 创建日期：2016/11/30 on 9:49
 * 描述:  进度
 * 作者:马飞奔 Administrator
 */
public class PageProgressFragment extends BaseFragment {
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.contentView)
    View contentView;
    @BindView(R.id.progressBar)
    ColorArcProgressBar progressBar;
    @BindViews({R.id.tv_time, R.id.tv_score, R.id.tv_state, R.id.tv_video, R.id.tv_html, R.id.tv_assignment, R.id.tv_test, R.id.tv_discussion, R.id.tv_survey})
    TextView[] textViews;
    @BindViews({R.id.ll_video, R.id.ll_html, R.id.ll_assignment, R.id.ll_test, R.id.ll_discussion, R.id.ll_survey})
    LinearLayout[] layoutViews;
    @BindViews({R.id.videoProgress, R.id.htmlProgress, R.id.assignmentProgress, R.id.testProgress, R.id.discussionProgress, R.id.surveyProgress})
    RoundRectProgressBar[] progressBars;
    private boolean training;
    private String courseId, courseType;
    private OnSelectCallBack onSelectCallBack;

    @Override
    public int createView() {
        return R.layout.fragment_page_progress;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            training = bundle.getBoolean("training", false);
            courseId = bundle.getString("entityId");
            courseType = bundle.getString("courseType");
        }
        if (courseType != null && courseType.equals("微课")) {
            layoutViews[1].setVisibility(View.GONE);
            layoutViews[2].setVisibility(View.GONE);
            layoutViews[4].setVisibility(View.GONE);
            layoutViews[5].setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/study/m/course/" + courseId + "/study_progress";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseProgressResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(CourseProgressResult response) {
                loadingView.setVisibility(View.VISIBLE);
                if (response != null && response.getResponseData() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    showData(response.getResponseData());
                }
            }
        }));
    }

    private void showData(CourseProgressEntity entity) {
        if (training) {
            if (entity.getmTimePeriod() != null && entity.getmTimePeriod().getMinutes() > 0) {
                textViews[0].setText(computeTimeDiff(entity.getmTimePeriod().getMinutes()));
            } else {
                if (entity.getmTimePeriod() != null && entity.getmTimePeriod().getState() != null) {
                    textViews[0].setText("课程" + entity.getmTimePeriod().getState());
                } else {
                    textViews[0].setText("课程进行中");
                }
            }
        } else {
            textViews[0].setText("课程已结束");
        }
        double score = entity.getScore();
        textViews[1].setText(String.valueOf((int) score));
        if (entity.getState() != null && entity.getState().equals("pass")) {
            textViews[2].setText("合格");
        } else if (entity.getState() != null && entity.getState().equals("nopass")) {
            textViews[2].setText("不合格");
        } else {
            textViews[2].setText("未登记");
        }
        progressBar.setMaxValues(100);
        progressBar.setCurrentValues((float) score);
        progressBars[0].setMax(entity.getActivityVideoNum());
        progressBars[0].setProgress(entity.getCompleteVideoNum());
        progressBars[1].setMax(entity.getActivityHtmlNum());
        progressBars[1].setProgress(entity.getCompleteHtmlNum());
        progressBars[2].setMax(entity.getActivityAssignmentNum());
        progressBars[2].setProgress(entity.getCompleteAssignmentNum());
        progressBars[3].setMax(entity.getActivityTestNum());
        progressBars[3].setProgress(entity.getCompleteTestNum());
        progressBars[4].setMax(entity.getActivityDiscussionNum());
        progressBars[4].setProgress(entity.getCompleteDiscussionNum());
        progressBars[5].setMax(entity.getActivitySurveyNum());
        progressBars[5].setProgress(entity.getCompleteSurveyNum());
        textViews[3].setText("已观看" + entity.getCompleteVideoNum() + "个/" + entity.getActivityVideoNum() + "个");
        textViews[4].setText("已观看" + entity.getCompleteHtmlNum() + "个/" + entity.getActivityHtmlNum() + "个");
        textViews[5].setText("已完成" + entity.getCompleteAssignmentNum() + "篇/" + entity.getActivityAssignmentNum() + "篇");
        textViews[6].setText("已完成" + entity.getCompleteTestNum() + "个/" + entity.getActivityTestNum() + "个");
        textViews[7].setText("已参与" + entity.getCompleteDiscussionNum() + "个/" + entity.getActivityDiscussionNum() + "个");
        textViews[8].setText("已完成" + entity.getCompleteSurveyNum() + "个/" + entity.getCompleteSurveyNum() + "个");
    }

    private CharSequence computeTimeDiff(long minutes) {
        String timeStr = "距离课程结束还有";
        StringBuilder actionText = new StringBuilder();
        if (minutes <= 0) { //1分钟内 服务端的时间 可能和本地的有区别 所以小于0的 对于这个情况全部都显示刚刚
            timeStr += "1分钟";
        } else if (minutes < 60) { // 1小时内
            timeStr += minutes + "分钟";
        } else if (minutes < 24 * 60) { // 一天内
            timeStr += (minutes / 60 == 0 ? 1 : minutes / 60) + "小时";
        } else if (minutes < 30 * 24 * 60) { // 天前
            long day = minutes / 60 / 24;
            long hour = (minutes - day * 24 * 60) / 60;
            long min = minutes - (day * 24 * 60) - (hour * 60);
            // 输出结果
            actionText.append("距离课程结束还有");
            actionText.append("<font color='#FEE42D'>"
                    + day + "</font>天");
            actionText.append("<font color='#FEE42D'>"
                    + hour + "</font>时");
            actionText.append("<font color='#FEE42D'>"
                    + min + "</font>分");
            timeStr = actionText.toString();
            return Html.fromHtml(timeStr);
        } else if (minutes < 12 * 30 * 24 * 60) { // 月
            timeStr += (minutes / 30 * 24 * 60 == 0 ? 1 : minutes / (30 * 24 * 60)) + "个月";
        } else {
            timeStr += (minutes / 12 * 30 * 24 * 60 == 0 ? 1 : minutes / (12 * 30 * 24 * 60)) + "年";
        }
        return timeStr;
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSelectCallBack != null)
                    onSelectCallBack.onClickCallBack();
            }
        };
        for (LinearLayout layout : layoutViews) {
            layout.setOnClickListener(listener);
        }
    }

    public void setOnSelectCallBack(OnSelectCallBack onSelectCallBack) {
        this.onSelectCallBack = onSelectCallBack;
    }

    public interface OnSelectCallBack {
        void onClickCallBack();
    }
}
