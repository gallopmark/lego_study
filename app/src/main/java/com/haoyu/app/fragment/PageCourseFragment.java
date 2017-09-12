package com.haoyu.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.activity.AppSurveyHomeActivity;
import com.haoyu.app.activity.AppTestHomeActivity;
import com.haoyu.app.activity.AppTestResultActivity;
import com.haoyu.app.activity.CoursewareEditorActivity;
import com.haoyu.app.activity.CoursewareFileActivity;
import com.haoyu.app.activity.CoursewareLinkActivity;
import com.haoyu.app.activity.TeachingDiscussionActivity;
import com.haoyu.app.activity.TestAssignmentActivity;
import com.haoyu.app.activity.VideoPlayerActivity;
import com.haoyu.app.adapter.CourseActivityAdapter;
import com.haoyu.app.adapter.CourseSectionAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.ChildSectionMobileEntity;
import com.haoyu.app.entity.CourseActivityListResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.CourseSectionResult;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.SectionMobileEntity;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import org.wlf.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 章节fragment
 *
 * @author xiaoma
 */
public class PageCourseFragment extends BaseFragment {
    @BindView(R.id.loadView)
    LoadingView loadView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.empty_list)
    TextView empty_list;
    private boolean training;
    private String courseId;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    List<MultiItemEntity> courseSections = new ArrayList<>();
    private CourseSectionAdapter sectionAdapter;
    private final int STUDY_CODE = 1;

    @Override
    public int createView() {
        return R.layout.fragment_page_course;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            courseId = bundle.getString("entityId");
            training = bundle.getBoolean("training", false);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        sectionAdapter = new CourseSectionAdapter(context, courseSections);
        recyclerView.setAdapter(sectionAdapter);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    /**
     * 获取章节列表
     */
    @Override
    public void initData() {
        loadView.setVisibility(View.VISIBLE);
        String url = Constants.OUTRT_NET + "/" + courseId + "/study/m/course/" + courseId + "/study";
        addSubscription(Flowable.just(url).map(new Function<String, CourseSectionResult>() {
            @Override
            public CourseSectionResult apply(String url) throws Exception {
                return getData(url);
            }
        }).map(new Function<CourseSectionResult, CourseSectionResult>() {
            @Override
            public CourseSectionResult apply(CourseSectionResult response) throws Exception {
                return doWith(response);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<CourseSectionResult>() {
            @Override
            public void accept(CourseSectionResult response) throws Exception {
                loadView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmCourse() != null
                        && response.getResponseData().getmCourse().getmSections() != null
                        && response.getResponseData().getmCourse().getmSections().size() > 0) {
                    updateUI(response.getResponseData().getmCourse().getmSections());
                } else if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmActivities() != null
                        && response.getResponseData().getmActivities().size() > 0) {
                    updateAT(response.getResponseData().getmActivities());
                } else {
                    empty_list.setVisibility(View.VISIBLE);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                loadView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }
        }));
    }

    private CourseSectionResult getData(String url) throws Exception {
        String mDatas = OkHttpClientManager.getAsString(context, url);
        CourseSectionResult result = new GsonBuilder().create().fromJson(mDatas, CourseSectionResult.class);
        return result;
    }

    private CourseSectionResult doWith(CourseSectionResult response) throws Exception {
        if (response != null && response.getResponseData() != null
                && response.getResponseData().getmCourse() != null
                && response.getResponseData().getmCourse().getmSections() != null
                && response.getResponseData().getmCourse().getmSections().size() > 0) {
            for (int i = 0; i < response.getResponseData().getmCourse().getmSections().size(); i++) {
                if (response.getResponseData().getmCourse().getmSections().get(i).getChildSections() != null
                        && response.getResponseData().getmCourse().getmSections().get(i).getChildSections().size() > 0) {
                    for (int j = 0; j < response.getResponseData().getmCourse().getmSections().get(i).getChildSections().size(); j++) {
                        String sectionId = response.getResponseData().getmCourse().getmSections().get(i).getChildSections().get(j).getId();
                        CourseActivityListResult result = getActvityList(sectionId);
                        if (result != null && result.getResponseData() != null) {
                            response.getResponseData().getmCourse().getmSections().get(i).getChildSections().get(j).setActivities(result.getResponseData());
                        }
                    }
                }
            }
        }
        return response;
    }

    private CourseActivityListResult getActvityList(String sectionId) throws Exception {
        String url = Constants.OUTRT_NET + "/" + sectionId + "/study/m/activity/ncts" + "?id=" + sectionId;
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        CourseActivityListResult result = gson.fromJson(json, CourseActivityListResult.class);
        if (result != null && result.getResponseData() != null && result.getResponseData().size() > 0) {
            for (int i = 0; i < result.getResponseData().size(); i++) {
                CourseSectionActivity activity = result.getResponseData().get(i);
                if (activity.getType() != null && activity.getType().equals("video")) {      //如果是视频文件，则进入视频查询是否可以下载视频
                    url = Constants.OUTRT_NET + "/" + activity.getId() + "/study/m/activity/ncts/" + activity.getId() + "/view";
                    json = OkHttpClientManager.getAsString(context, url);
                    AppActivityViewResult viewResult = gson.fromJson(json, AppActivityViewResult.class);
                    if (viewResult != null && viewResult.getResponseData() != null && viewResult.getResponseData().getmVideoUser() != null) {
                        result.getResponseData().get(i).setmVideo(viewResult.getResponseData().getmVideoUser().getmVideo());
                    }
                }
            }
        }
        return result;
    }

    private void updateUI(List<SectionMobileEntity> mDatas) {
        recyclerView.setVisibility(View.VISIBLE);
        for (int i = 0; i < mDatas.size(); i++) {
            SectionMobileEntity entity = mDatas.get(i);
            courseSections.add(entity);
            if (entity.getChildSections() != null && entity.getChildSections().size() > 0) {
                for (int j = 0; j < entity.getChildSections().size(); j++) {
                    ChildSectionMobileEntity childEntity = entity.getChildSections().get(j);
                    entity.addSubItem(childEntity);
                }
            }
        }
        sectionAdapter.expandAll();
        for (int i = 0; i < courseSections.size(); i++) {
            if (courseSections.get(i) instanceof ChildSectionMobileEntity) {
                ChildSectionMobileEntity childEntity = (ChildSectionMobileEntity) courseSections.get(i);
                for (int k = 0; k < childEntity.getActivities().size(); k++) {
                    childEntity.addSubItem(childEntity.getActivities().get(k));
                }
            }
        }
        sectionAdapter.notifyDataSetChanged();
        sectionAdapter.setOnActivityClickCallBack(new CourseSectionAdapter.OnActivityClickCallBack() {
            @Override
            public void onActivityClick(CourseSectionActivity activity) {
                enterActivity(activity);
            }
        });
    }

    private void updateAT(final List<CourseSectionActivity> mDatas) {
        recyclerView.setVisibility(View.VISIBLE);
        final CourseActivityAdapter mAdapter = new CourseActivityAdapter(context, mDatas);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mAdapter.setSelected(mDatas.get(position).getId());
                CourseSectionActivity activity = mDatas.get(position);
                enterActivity(activity);
            }
        });
    }

    /**
     * 进入活动
     *
     * @param activity
     */
    private void enterActivity(final CourseSectionActivity activity) {
        String url = Constants.OUTRT_NET + "/" + activity.getId() + "/study/m/activity/ncts/" + activity.getId() + "/view";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(AppActivityViewResult response) {
                hideTipDialog();
                if (response != null) {
                    orientationActivity(response);
                }
            }
        }));
    }

    /**
     * 根据活动内容进入相关的activity
     *
     * @param response
     */
    private void orientationActivity(AppActivityViewResult response) {
        if (response.getResponseData() != null && response.getResponseData().getmActivityResult() != null
                && response.getResponseData().getmActivityResult().getmActivity() != null) {
            CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
            if (activity.getType() != null && activity.getType().equals("video")) {   //视频类型
                if (NetStatusUtil.isConnected(context)) {
                    if (NetStatusUtil.isWifi(context))
                        playVideo(response, activity);
                    else {
                        showNetDialog(response, activity);
                    }
                } else {
                    toast("当前网络不稳定，请检查网络设置！");
                }
            } else if (activity.getType() != null && activity.getType().equals("html")) {  //课件类型
                openHtml(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("discussion")) {  //课程研讨
                openDiscussion(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("survey")) {  //问卷调查
                openSurvey(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("test")) {  //测验类型
                openTest(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("assignment")) {  //作业类型
                openAssignMent(activity);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }
        } else {
            toast("无法进入此活动");
        }
    }

    private void showNetDialog(final AppActivityViewResult response, final CourseSectionActivity activity) {
        MaterialDialog mainDialog = new MaterialDialog(context);
        mainDialog.setTitle("网络提醒");
        mainDialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
        mainDialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                playVideo(response, activity);
                dialog.dismiss();
            }
        });
        mainDialog.setPositiveButton("取消", null);
        mainDialog.show();
    }

    /*播放视频*/
    private void playVideo(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmVideoUser() != null) {  //教学视频
            AppActivityViewEntity.VideoUserMobileEntity videoEntity = response.getResponseData().getmVideoUser();
            VideoMobileEntity video = videoEntity.getmVideo();
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("lastViewTime", videoEntity.getLastViewTime());
            if (video != null) {
                intent.putExtra("interval", video.getInterval());
                intent.putExtra("attach", video);
            }
            intent.putExtra("type", "course");
            intent.putExtra("courseId", courseId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("summary", videoEntity.getmVideo().getSummary());
            intent.putExtra("videoId", videoEntity.getId());
            if (video != null && video.getUrls() != null && video.getUrls().length() > 0) {
                intent.putExtra("videoUrl", video.getUrls());
                startActivity(intent);
            } else if (video != null && video.getVideoFiles() != null && video.getVideoFiles().size() > 0) {
                intent.putExtra("videoUrl", video.getVideoFiles().get(0).getUrl());
                intent.putExtra("fileName", video.getVideoFiles().get(0).getFileName());
                startActivity(intent);
            } else if (video != null && video.getAttchFiles() != null && video.getAttchFiles().size() > 0) {
                //教学观摩
                intent.putExtra("videoUrl", video.getAttchFiles().get(0).getUrl());
                intent.putExtra("fileName", video.getAttchFiles().get(0).getFileName());
                startActivity(intent);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }
        }
    }

    /*打开课件*/
    private void openHtml(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmTextInfoUser() != null) {
            AppActivityViewEntity.TextInfoUserMobileEntity mTextInfoUser = response.getResponseData().getmTextInfoUser();
            Intent intent = new Intent();
            if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("mTextInfoUserId", mTextInfoUser.getId());
            intent.putExtra("title", activity.getTitle());
            if (activity.getCompleteState() != null && activity.getCompleteState().equals("complete")) {
                intent.putExtra("needUpload", false);
            } else {
                intent.putExtra("needUpload", true);
            }
            intent.putExtra("viewNum", mTextInfoUser.getViewNum());
            if (mTextInfoUser.getmTextInfo() != null) {
                intent.putExtra("interval", mTextInfoUser.getmTextInfo().getInterval());
                intent.putExtra("needViewNum", mTextInfoUser.getmTextInfo().getViewNum());
            }
            if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("file")) {  //课件类型为pdf文件
                String pdfUrl = mTextInfoUser.getmTextInfo().getPdfUrl();
                intent.putExtra("file", pdfUrl);
                intent.setClass(context, CoursewareFileActivity.class);
                startActivityForResult(intent, STUDY_CODE);
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("link")) {  //课件类型为外链
                String webUrl = mTextInfoUser.getmTextInfo().getContent();
                intent.setClass(context, CoursewareLinkActivity.class);
                intent.putExtra("link", webUrl);
                startActivityForResult(intent, STUDY_CODE);
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("editor")) {  // 课件类型为文本
                String editor = mTextInfoUser.getmTextInfo().getContent();
                intent.setClass(context, CoursewareEditorActivity.class);
                intent.putExtra("editor", editor);
                startActivityForResult(intent, STUDY_CODE);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }
        }
    }

    /*打开课程研讨*/
    private void openDiscussion(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmDiscussionUser() != null) {
            Intent intent = new Intent(context, TeachingDiscussionActivity.class);
            if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("discussType", "course");
            intent.putExtra("relationId", courseId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("timePeriod", activity.getmTimePeriod());
            intent.putExtra("discussUser", response.getResponseData().getmDiscussionUser());
            intent.putExtra("mainNum", response.getResponseData().getmDiscussionUser().getMainPostNum());
            intent.putExtra("subNum", response.getResponseData().getmDiscussionUser().getSubPostNum());
            if (response.getResponseData().getmDiscussionUser().getmDiscussion() != null) {
                DiscussEntity entity = response.getResponseData().getmDiscussionUser().getmDiscussion();
                intent.putExtra("needMainNum", entity.getMainPostNum());
                intent.putExtra("needSubNum", entity.getSubPostNum());
            }
            startActivityForResult(intent, STUDY_CODE);
        } else
            toast("系统暂不支持浏览，请到网站完成。");
    }

    /*打开问卷调查*/
    private void openSurvey(AppActivityViewResult response, CourseSectionActivity activity) {
        Intent intent = new Intent(context, AppSurveyHomeActivity.class);
        if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
            intent.putExtra("running", true);
        else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
            intent.putExtra("running", true);
        else
            intent.putExtra("running", false);
        intent.putExtra("relationId", courseId);
        intent.putExtra("type", "course");
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        if (response.getResponseData() != null && response.getResponseData().getmSurveyUser() != null) {
            intent.putExtra("surveyUser", response.getResponseData().getmSurveyUser());
        }
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        startActivityForResult(intent, STUDY_CODE);
    }

    /*打开测验*/
    private void openTest(AppActivityViewResult response, CourseSectionActivity activity) {
        Intent intent = new Intent();
        if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
            intent.putExtra("running", true);
        else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
            intent.putExtra("running", true);
        else
            intent.putExtra("running", false);
        intent.putExtra("relationId", courseId);
        intent.putExtra("testType", "course");
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        if (response.getResponseData() != null && response.getResponseData().getmTestUser() != null) {
            intent.putExtra("testUser", response.getResponseData().getmTestUser());
        }
        if (response.getResponseData() != null && response.getResponseData().getmTestUser() != null
                && response.getResponseData().getmTestUser().getCompletionStatus() != null
                && response.getResponseData().getmTestUser().getCompletionStatus().equals("completed")) {
            if (response.getResponseData().getmActivityResult() != null) {
                intent.putExtra("score", response.getResponseData().getmActivityResult().getScore());
            }
            intent.setClass(context, AppTestResultActivity.class);
        } else {
            intent.setClass(context, AppTestHomeActivity.class);
        }
        startActivityForResult(intent, STUDY_CODE);
    }

    /*打开作业*/
    private void openAssignMent(CourseSectionActivity activity) {
        Intent intent = new Intent(context, TestAssignmentActivity.class);
        if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
            intent.putExtra("running", true);
        else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
            intent.putExtra("running", true);
        else
            intent.putExtra("running", false);
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        intent.putExtra("inCurrentDate", activity.isInCurrentDate());
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case STUDY_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    CourseSectionActivity activity = (CourseSectionActivity) data.getSerializableExtra("activity");
                    for (int i = 0; i < courseSections.size(); i++) {
                        if (courseSections.get(i) instanceof ChildSectionMobileEntity) {
                            ChildSectionMobileEntity childEntity = (ChildSectionMobileEntity) courseSections.get(i);
                            if (childEntity.contains(activity)) {
                                childEntity.removeSubItem(activity);
                                childEntity.addSubItem(childEntity.getSubItemPosition(activity), activity);
                                if (childEntity.getActivities() != null && childEntity.getActivities().size() > 0) {
                                    int completeCount = 0;
                                    for (int k = 0; k < childEntity.getActivities().size(); k++) {
                                        if (childEntity.getActivities().contains(activity))
                                            childEntity.getActivities().set(k, activity);
                                        CourseSectionActivity sectionActivity = childEntity.getActivities().get(k);
                                        if (sectionActivity.getCompleteState() != null && (sectionActivity.getCompleteState().equals("已完成") || sectionActivity.getCompleteState().equals("complete"))) {
                                            completeCount++;
                                        }
                                    }
                                    if (completeCount == childEntity.getActivities().size())
                                        childEntity.setCompleteState("complete");
                                    else if (0 < completeCount && completeCount < childEntity.getActivities().size())
                                        childEntity.setCompleteState("in_progress");
                                    else
                                        childEntity.setCompleteState("not_attempt");
                                    courseSections.set(i, childEntity);
                                }
                                break;
                            }
                        }
                    }
                    if (courseSections.indexOf(activity) != -1)
                        courseSections.set(courseSections.indexOf(activity), activity);
                    sectionAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sectionAdapter != null && sectionAdapter.getmOnFileDownloadStatusListener() != null) {
            FileDownloader.unregisterDownloadStatusListener(sectionAdapter.getmOnFileDownloadStatusListener());
        }
    }
}
