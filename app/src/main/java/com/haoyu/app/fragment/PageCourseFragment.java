package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.activity.AppSurveyHomeActivity;
import com.haoyu.app.activity.AppTestHomeActivity;
import com.haoyu.app.activity.AppTestResultActivity;
import com.haoyu.app.activity.CoursewareViewerActivity;
import com.haoyu.app.activity.IJKPlayerActivity;
import com.haoyu.app.activity.TeachingDiscussionActivity;
import com.haoyu.app.activity.TestAssignmentActivity;
import com.haoyu.app.adapter.CourseActivityAdapter;
import com.haoyu.app.adapter.CourseStudyAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseActivityListResult;
import com.haoyu.app.entity.CourseChildSectionEntity;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.CourseSectionEntity;
import com.haoyu.app.entity.CourseSectionResult;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MultiItemEntity;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;

import java.io.File;
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
    private CourseStudyAdapter sectionAdapter;
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
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        sectionAdapter = new CourseStudyAdapter(context, courseSections);
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

    private void updateUI(List<CourseSectionEntity> mDatas) {
        recyclerView.setVisibility(View.VISIBLE);
        for (int i = 0; i < mDatas.size(); i++) {
            CourseSectionEntity entity = mDatas.get(i);
            courseSections.add(entity);
            if (entity.getChildSections() != null && entity.getChildSections().size() > 0) {
                for (int j = 0; j < entity.getChildSections().size(); j++) {
                    CourseChildSectionEntity childEntity = entity.getChildSections().get(j);
                    courseSections.add(childEntity);
                    if (childEntity.getActivities() != null && childEntity.getActivities().size() > 0) {
                        for (int k = 0; k < childEntity.getActivities().size(); k++)
                            courseSections.add(childEntity.getActivities().get(k));
                    }
                }
            }
        }
        sectionAdapter.notifyDataSetChanged();
        sectionAdapter.setOnItemClickListener(new CourseStudyAdapter.OnItemClickListener() {
            @Override
            public void onChildSectionClick(int position) {
                recyclerView.smoothScrollToPosition(position);
            }

            @Override
            public void onActivityClick(CourseSectionActivity activity) {
                enterActivity(activity);
            }
        });
        sectionAdapter.setOnItemLongClickListener(new CourseStudyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(TextView tv, CharSequence charSequence) {
                if (overLine(tv)) {
                    MaterialDialog dialog = new MaterialDialog(context);
                    dialog.setTitle(null);
                    dialog.setMessage(charSequence);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("关闭", null);
                    dialog.show();
                }
            }
        });
    }

    private boolean overLine(TextView tv) {
        Layout layout = tv.getLayout();
        if (layout != null && layout.getLineCount() > 0) {
            int lines = layout.getLineCount();//获取textview行数
            if (layout.getEllipsisCount(lines - 1) > 0) {//获取最后一行省略掉的字符数，大于0就代表超过行数
                return true;
            }
        }
        return false;
    }

    private void updateAT(final List<CourseSectionActivity> mDatas) {
        recyclerView.setVisibility(View.VISIBLE);
        final CourseActivityAdapter mAdapter = new CourseActivityAdapter(context, mDatas);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                mAdapter.setSelected(mDatas.get(position).getId());
                CourseSectionActivity activity = mDatas.get(position);
                enterActivity(activity);
            }
        });
        mAdapter.setOnItemLongClickListener(new CourseActivityAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(TextView tv, CharSequence charSequence) {
                if (overLine(tv)) {
                    MaterialDialog dialog = new MaterialDialog(context);
                    dialog.setTitle(null);
                    dialog.setMessage(charSequence);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("关闭", null);
                    dialog.show();
                }
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
                playVideo(response, activity);
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
            toast("系统暂不支持浏览，请到网站完成。");
        }
    }

    /*播放视频*/
    private void playVideo(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmVideoUser() != null) {  //教学视频
            AppActivityViewEntity.VideoUserMobileEntity videoEntity = response.getResponseData().getmVideoUser();
            VideoMobileEntity video = videoEntity.getmVideo();
            if (video != null) {
                Intent intent = new Intent(context, IJKPlayerActivity.class);
                intent.putExtra("videoType", "course");
                if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                    intent.putExtra("running", true);
                else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                    intent.putExtra("running", true);
                else
                    intent.putExtra("running", false);
                intent.putExtra("activityId", activity.getId());
                intent.putExtra("videoTitle", activity.getTitle());
                intent.putExtra("videoId", videoEntity.getId());
                intent.putExtra("video", video);
                if (!TextUtils.isEmpty(video.getUrls())) {
                    DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(video.getUrls());
                    if (fileInfo != null && fileInfo.getFilePath() != null && new File(fileInfo.getFilePath()).exists()) {
                        intent.putExtra("videoUrl", fileInfo.getFilePath());
                    } else {
                        intent.putExtra("videoUrl", video.getUrls());
                    }
                    startActivityForResult(intent, STUDY_CODE);
                } else if (video.getVideoFiles().size() > 0) {
                    String url = video.getVideoFiles().get(0).getUrl();
                    DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
                    if (fileInfo != null && fileInfo.getFilePath() != null && new File(fileInfo.getFilePath()).exists()) {
                        intent.putExtra("videoUrl", fileInfo.getFilePath());
                    } else {
                        intent.putExtra("videoUrl", url);
                    }
                    startActivityForResult(intent, STUDY_CODE);
                } else {
                    toast("系统暂不支持浏览，请到网站完成。");
                }
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
            intent.putExtra("viewNum", mTextInfoUser.getViewNum());
            if (mTextInfoUser.getmTextInfo() != null) {
                intent.putExtra("interval", mTextInfoUser.getmTextInfo().getInterval());
                intent.putExtra("needViewNum", mTextInfoUser.getmTextInfo().getViewNum());
            }
            if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("file")) {  //课件类型为pdf文件
                String pdfUrl = mTextInfoUser.getmTextInfo().getPdfUrl();
                intent.putExtra("type", "file");
                intent.putExtra("url", pdfUrl);
                intent.setClass(context, CoursewareViewerActivity.class);
                startActivityForResult(intent, STUDY_CODE);
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("link")) {  //课件类型为外链
                String webUrl = mTextInfoUser.getmTextInfo().getContent();
                intent.setClass(context, CoursewareViewerActivity.class);
                intent.putExtra("type", "link");
                intent.putExtra("url", webUrl);
                startActivityForResult(intent, STUDY_CODE);
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().getType() != null
                    && mTextInfoUser.getmTextInfo().getType().equals("editor")) {  // 课件类型为文本
                String editor = mTextInfoUser.getmTextInfo().getContent();
                intent.setClass(context, CoursewareViewerActivity.class);
                intent.putExtra("type", "editor");
                intent.putExtra("editor", editor);
                startActivityForResult(intent, STUDY_CODE);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }
        } else {
            toast("系统暂不支持浏览，请到网站完成。");
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
                    if (courseSections.indexOf(activity) != -1) {
                        int index = courseSections.indexOf(activity);
                        CourseSectionActivity entity = (CourseSectionActivity) courseSections.get(index);
                        entity.setCompleteState(activity.getCompleteState());
                        courseSections.set(index, entity);
                        sectionAdapter.notifyDataSetChanged();
                    }
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
