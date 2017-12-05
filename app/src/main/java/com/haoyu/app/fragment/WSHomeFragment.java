package com.haoyu.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.activity.AppSurveyHomeActivity;
import com.haoyu.app.activity.AppTestHomeActivity;
import com.haoyu.app.activity.AppTestResultActivity;
import com.haoyu.app.activity.FreeChatActiviy;
import com.haoyu.app.activity.TeachingDiscussionActivity;
import com.haoyu.app.activity.WSTSInfoActivity;
import com.haoyu.app.activity.VideoPlayerActivity;
import com.haoyu.app.activity.WSCDEditActivity;
import com.haoyu.app.activity.WSCDInfoActivity;
import com.haoyu.app.activity.WSTDEditActivity;
import com.haoyu.app.activity.WSTSEditActivity;
import com.haoyu.app.activity.WorkShopEditTaskActivity;
import com.haoyu.app.activity.WorkshopQuestionActivity;
import com.haoyu.app.adapter.WorkShopSectionAdapter;
import com.haoyu.app.adapter.WorkShopTaskAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.DatePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.entity.WorkshopPhaseResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.ColorArcProgressBar;
import com.haoyu.app.view.StickyScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/11/13.
 * 描述:
 * 作者:xiaoma
 */

public class WSHomeFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.contentView)
    StickyScrollView contentView;
    @BindView(R.id.capBar1)
    ColorArcProgressBar capBar1;
    @BindView(R.id.capBar2)
    ColorArcProgressBar capBar2;
    @BindView(R.id.tv_day)
    TextView tv_day;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.ll_question)
    LinearLayout ll_question;
    @BindView(R.id.ll_exchange)
    LinearLayout ll_exchange;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<MWorkshopSection> mWorkshopSections = new ArrayList<>();
    private WorkShopSectionAdapter sectionAdapter;
    private WorkShopTaskAdapter taskAdapter;
    @BindView(R.id.tv_empty)
    TextView tv_empty;//没有数据时显示
    @BindView(R.id.tv_bottom)
    TextView tv_bottom;
    private boolean training;
    private String workshopId, role;
    private boolean canEdit;
    private int alterPosition, activityIndex;
    private int REQUEST_SECTION = 10, REQUEST_ACTIVITY = 11;

    @Override
    public int createView() {
        return R.layout.fragment_workshophome;
    }

    @Override
    public void initView(View view) {
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        sectionAdapter = new WorkShopSectionAdapter(context, mWorkshopSections);
        taskAdapter = new WorkShopTaskAdapter(context, mWorkshopSections);
        Bundle bundle = getArguments();
        training = bundle.getBoolean("training");
        WorkShopMobileEntity mWorkshop = (WorkShopMobileEntity) bundle.getSerializable("mWorkshop");
        if (mWorkshop != null) {
            workshopId = mWorkshop.getId();
        }
        WorkShopMobileUser mWorkshopUser = (WorkShopMobileUser) bundle.getSerializable("mWorkshopUser");
        if (mWorkshopUser != null) {
            role = mWorkshopUser.getRole();
            if (role != null && role.equals("master")) {
                canEdit = true;
                recyclerView.setAdapter(sectionAdapter);
            } else {
                recyclerView.setAdapter(taskAdapter);
            }
        } else {
            recyclerView.setAdapter(taskAdapter);
        }
        List<MWorkshopSection> mWorkshopSections = (List<MWorkshopSection>) bundle.getSerializable("mWorkshopSections");
        updateUI(mWorkshop, mWorkshopUser);
        updateUI(mWorkshopSections);
    }

    private void updateUI(WorkShopMobileEntity mWorkshop, WorkShopMobileUser mWorkshopUser) {
        if (mWorkshop != null && mWorkshop.getmTimePeriod() != null && mWorkshop.getmTimePeriod().getMinutes() > 0) {
            TimePeriod timePeriod = mWorkshop.getmTimePeriod();
            tv_day.setVisibility(View.VISIBLE);
            int remainDay = (int) (timePeriod.getMinutes() / 60 / 24);
            long startTime = timePeriod.getStartTime();
            long endTime = timePeriod.getEndTime();
            int allDay = getAllDay(startTime, endTime);
            int expandDay = allDay - remainDay;
            tv_day.setText(String.valueOf(expandDay));
            setTimePeriod(expandDay, allDay);
        } else {
            tv_day.setTextSize(14);
            if (mWorkshop.getmTimePeriod() != null && mWorkshop.getmTimePeriod().getState() != null) {
                tv_day.setText("工作坊研修\n" + mWorkshop.getmTimePeriod().getState());
            } else {
                tv_day.setTextSize(14);
                tv_day.setText("工作坊研修\n已结束");
            }
        }
        int qualityPoint = 0, point = 0;
        if (mWorkshop != null) {
            qualityPoint = mWorkshop.getQualifiedPoint();
        }
        if (mWorkshopUser != null) {
            point = (int) mWorkshopUser.getPoint();
        }
        setPoint(point, qualityPoint);
    }

    private void setTimePeriod(int expandDay, int allDay) {
        capBar1.setMaxValues(allDay);
        capBar1.setCurrentValues(expandDay);
        String text = expandDay + "\n已开展\n共" + allDay + "天";
        SpannableString ssb = new SpannableString(text);
        int start = 0;
        int end = text.indexOf("已") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = text.indexOf("已");
        end = text.indexOf("共") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = text.indexOf("共");
        end = text.length();
        ssb.setSpan(new AbsoluteSizeSpan(10, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_day.setText(ssb);
    }

    private void setPoint(int point, int qualityPoint) {
        capBar2.setMaxValues(qualityPoint);
        capBar2.setCurrentValues(point);
        String text = point + "\n研修积分\n（达标积分" + qualityPoint + "）";
        SpannableString ssb = new SpannableString(text);
        int start = 0;
        int end = text.indexOf("研") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = text.indexOf("研");
        end = text.indexOf("分") + 1;
        ssb.setSpan(new AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = text.indexOf("分") + 1;
        end = text.length();
        ssb.setSpan(new AbsoluteSizeSpan(10, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.course_progress)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_score.setText(ssb);
    }

    private int getAllDay(long startTime, long endTime) {
        long interval = (endTime - startTime) / 1000;
        int day = interval / 24 * 60 * 60 == 0 ? 1 : (int) (interval / (24 * 60 * 60));
        if (day <= 0) {
            return 0;
        }
        return day;
    }

    private void updateUI(List<MWorkshopSection> sections) {
        if (sections.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            if (canEdit) {
                sectionAdapter.addAll(sections);
                tv_bottom.setVisibility(View.VISIBLE);
            } else {
                taskAdapter.addAll(sections);
                taskAdapter.notifyDataSetChanged();
                tv_bottom.setVisibility(View.GONE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setListener() {
        ll_question.setOnClickListener(this);
        ll_exchange.setOnClickListener(this);
        tv_bottom.setOnClickListener(this);
        setAdapter(taskAdapter);
        setAdapter(sectionAdapter);
    }

    private void setAdapter(final WorkShopSectionAdapter adapter) {
        adapter.setOnSectionLongClickListener(new WorkShopSectionAdapter.OnSectionLongClickListener() {
            @Override
            public void onLongClickListener(String taskId, int position, MWorkshopSection entity) {
                showTaskEditDialog(taskId, position, entity);
            }
        });
        adapter.setItemCallBack(new WorkShopSectionAdapter.ActivityItemCallBack() {
            @Override
            public void itemCallBack(final MWorkshopActivity activity, final int position) {
                 /*进入活动*/
                enterActivity(activity.getId());
            }

            @Override
            public void onDelete(int mainIndex, int position) {
                deleteActivity(mainIndex, position);
            }
        });

        adapter.setAddTaskListener(new WorkShopSectionAdapter.OnAddTaskListener() {
            private String startTime, endTime;

            @Override
            public void inputTitle(final TextView task_title) {
                CommentDialog dialog = new CommentDialog(context, "输出阶段标题", "完成");
                dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
                    @Override
                    public void sendComment(String content) {
                        task_title.setText(content);
                    }
                });
                dialog.show();
            }

            @Override
            public void inputTime(final TextView tv_researchTime) {
                DatePickerDialog pickerDialog = new DatePickerDialog(context, true);
                pickerDialog.setDatePickerListener(new DatePickerDialog.OnDatePickerListener() {
                    @Override
                    public void datePicker(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
                        startTime = startYear + "-" + (startMonth < 10 ? "0" + startMonth : startMonth);
                        endTime = endYear + "-" + (endMonth < 10 ? "0" + endMonth : endMonth);
                        String mStartTime = startYear + "年" + (startMonth < 10 ? "0" + startMonth : startMonth) + "月";
                        String mEndTime = endYear + "年" + (endMonth < 10 ? "0" + endMonth : endMonth) + "月";
                        tv_researchTime.setText(mStartTime + "\u3000-\u3000" + mEndTime);
                    }
                });
                pickerDialog.show();
            }

            @Override
            public void addTask(TextView task_title, TextView tv_researchTime, int sortNum) {
                String title = task_title.getText().toString().trim();
                String time = tv_researchTime.getText().toString().trim();
                if (title.length() == 0) {
                    toast("请输入阶段标题");
                } else if (time.length() == 0) {
                    toast("请选择研修时间");
                } else {
                    addStage(title, startTime, endTime, sortNum);
                    task_title.setText(null);
                    tv_researchTime.setText(null);
                    adapter.setAddTask(false);
                    tv_bottom.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void cancel() {
                tv_bottom.setVisibility(View.VISIBLE);
            }
        });

        adapter.setAddActivityCallBack(new WorkShopSectionAdapter.AddActivityCallBack() {
            @Override
            public void addActivity(int type, String workSectionId, int position) {
                activityIndex = position;
                Intent intent = new Intent();
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("workSectionId", workSectionId);
                if (type == 1) {
                    intent.setClass(context, WSTDEditActivity.class);
                } else if (type == 2) {
                    intent.setClass(context, WSCDEditActivity.class);
                } else {
                    intent.setClass(context, WSTSEditActivity.class);
                }
                startActivityForResult(intent, REQUEST_ACTIVITY);
            }
        });
    }

    private void showTaskEditDialog(final String taskId, final int position, final MWorkshopSection entity) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_workshop_task, null);
        TextView tv_addTask = view.findViewById(R.id.tv_addTask);
        TextView tv_alterTask = view.findViewById(R.id.tv_alterTask);
        TextView tv_deleteTask = view.findViewById(R.id.tv_deleteTask);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_addTask:
                        smoothToBottom();
                        break;
                    case R.id.tv_alterTask:
                        alterPosition = position;
                        Intent intent = new Intent(context, WorkShopEditTaskActivity.class);
                        intent.putExtra("title", entity.getTitle());
                        if (entity.getTimePeriod() != null) {
                            intent.putExtra("startTime", entity.getTimePeriod().getStartTime());
                            intent.putExtra("endTime", entity.getTimePeriod().getEndTime());
                        }
                        intent.putExtra("workShopId", workshopId);
                        intent.putExtra("relationId", taskId);
                        startActivityForResult(intent, 200);
                        break;
                    case R.id.tv_deleteTask:
                        deleteTask(taskId, position);
                        break;
                    case R.id.tv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_addTask.setOnClickListener(listener);
        tv_alterTask.setOnClickListener(listener);
        tv_deleteTask.setOnClickListener(listener);
        tv_cancel.setOnClickListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //删除活动
    private void deleteActivity(final int mainIndex, final int position) {
        String activityId = mWorkshopSections.get(mainIndex).getActivities().get(position).getId();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mWorkshopSections.get(mainIndex).getActivities().remove(position);
                    sectionAdapter.setPressIndex(mainIndex, position);
                    sectionAdapter.notifyItemChanged(mainIndex);
                } else {
                    toast("删除失败，请稍后再试");
                }
            }
        }, map));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            String title = data.getStringExtra("title");
            TimePeriod timePeriod = (TimePeriod) data.getSerializableExtra("timePeriod");
            mWorkshopSections.get(alterPosition).setTitle(title);
            mWorkshopSections.get(alterPosition).setTimePeriod(timePeriod);
            sectionAdapter.notifyItemChanged(alterPosition);
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SECTION) {
            if (data != null && data.getSerializableExtra("section") != null
                    && data.getSerializableExtra("section") instanceof MWorkshopSection) {
                MWorkshopSection section = (MWorkshopSection) data.getSerializableExtra("section");
                mWorkshopSections.add(section);
                sectionAdapter.notifyItemInserted(mWorkshopSections.indexOf(section));
                recyclerView.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_ACTIVITY) {
            if (data != null && data.getSerializableExtra("activity") != null && data.getSerializableExtra("activity") instanceof MWorkshopActivity) {
                MWorkshopActivity mWorkshopActivity = (MWorkshopActivity) data.getSerializableExtra("activity");
                mWorkshopSections.get(activityIndex).getActivities().add(mWorkshopActivity);
                sectionAdapter.notifyItemChanged(activityIndex);
            }
        }
    }

    private void deleteTask(String id, final int position) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/workshop_section/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toast("删除失败，请稍后再试");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mWorkshopSections.remove(position);
                    sectionAdapter.notifyItemRangeRemoved(position, 1);
                    sectionAdapter.notifyItemRangeChanged(position, mWorkshopSections.size());
                    if (mWorkshopSections.size() == 0) {
                        tv_empty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    toast("删除失败，请稍后再试");
                }
            }
        }, map));
    }

    //添加新阶段
    private void addStage(String title, String startTime, String endTime, int sortNum) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/workshop_section";
        Map<String, String> map = new HashMap<>();
        map.put("workshopId", workshopId);
        map.put("title", title);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("sortNum", String.valueOf(sortNum));
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopPhaseResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(WorkshopPhaseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    tv_empty.setVisibility(View.GONE);
                    if (recyclerView.getVisibility() == View.GONE) {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    MWorkshopSection section = response.getResponseData();
                    mWorkshopSections.add(section);
                    sectionAdapter.notifyItemInserted(mWorkshopSections.indexOf(section));
                } else {
                    toastFullScreen("添加失败", false);
                }
            }
        }, map));
    }

    private void EnterActivity(AppActivityViewResult response) {
        if (response != null && response.getResponseData() != null
                && response.getResponseData().getmActivityResult() != null
                && response.getResponseData().getmActivityResult().getmActivity() != null) {
            CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
            if (activity.getType() != null && activity.getType().equals("lesson_plan")) {   //集体备课
                toast("系统暂不支持浏览，请到网站完成。");
            } else if (activity.getType() != null && activity.getType().equals("discussion")) {  //教学研讨
                openDiscussion(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("survey")) {  //问卷调查
                openSurvey(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("debate")) {  //在线辩论
                toast("系统暂不支持浏览，请到网站完成。");
            } else if (activity.getType() != null && activity.getType().equals("test")) {  //教学测验
                openTest(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("video")) {  //视频
                if (NetStatusUtil.isConnected(context)) {
                    if (NetStatusUtil.isWifi(context))
                        playVideo(response, activity);
                    else {
                        showNetDialog(response, activity);
                    }
                } else {
                    toast("当前网络不稳定，请检查网络设置！");
                }
            } else if (activity.getType() != null && activity.getType().equals("lcec")) {
                openLcec(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("discuss_class")) { //评课议课
                openDiscuss_class(response, activity);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }
        }
    }

    private void setAdapter(WorkShopTaskAdapter adapter) {
        adapter.setOnActivityClickCallBack(new WorkShopTaskAdapter.OnActivityClickCallBack() {
            @Override
            public void onActivityClick(final MWorkshopActivity activity) {
                 /*进入活动*/
                enterActivity(activity.getId());
            }
        });
    }

    private void enterActivity(final String activityId) {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/activity/wsts/" + activityId + "/view";
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
                EnterActivity(response);
            }
        }));
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
            intent.putExtra("summary", videoEntity.getmVideo().getSummary());
            intent.putExtra("attach", video);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("type", "workshop");
            intent.putExtra("videoId", videoEntity.getId());
            if (video != null && video.getUrls() != null && video.getUrls().length() > 0) {
                intent.putExtra("videoUrl", video.getUrls());
                startActivity(intent);
            } else if (video != null && video.getVideoFiles() != null && video.getVideoFiles().size() > 0) {
                intent.putExtra("videoUrl", video.getVideoFiles().get(0).getUrl());
                startActivity(intent);
            } else if (video != null && video.getAttchFiles() != null && video.getAttchFiles().size() > 0) {
                //教学观摩
                intent.putExtra("videoUrl", video.getAttchFiles().get(0).getUrl());
                startActivity(intent);
            } else {
                toast("系统暂不支持浏览，请到网站完成。");
            }

        }
    }

    /*打开听课评课*/
    private void openLcec(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmLcec() != null) {
            Intent intent = new Intent(context, WSTSInfoActivity.class);
            if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (training && activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("timePeriod", activity.getmTimePeriod());
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("mlcec", response.getResponseData().getmLcec());
            startActivity(intent);
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
            intent.putExtra("discussType", "workshop");
            intent.putExtra("workshopId", workshopId);
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
            startActivity(intent);
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
        intent.putExtra("relationId", workshopId);
        intent.putExtra("type", "workshop");
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        if (response.getResponseData() != null && response.getResponseData().getmSurveyUser() != null) {
            intent.putExtra("surveyUser", response.getResponseData().getmSurveyUser());
        }
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        startActivity(intent);
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
        intent.putExtra("relationId", workshopId);
        intent.putExtra("testType", "workshop");
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
        startActivity(intent);
    }

    private void openDiscuss_class(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmVideoDC() != null) {
            Intent intent = new Intent(context, WSCDInfoActivity.class);
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("timePeriod", activity.getmTimePeriod());
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("discussClass", response.getResponseData().getmVideoDC());
            startActivity(intent);
        } else {
            toast("系统暂不支持浏览，请到网站完成。");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_question:
                Intent intent = new Intent(context, WorkshopQuestionActivity.class);
                intent.putExtra("relationId", workshopId);
                startActivity(intent);
                break;
            case R.id.ll_exchange:
                Intent intent2 = new Intent(context, FreeChatActiviy.class);
                intent2.putExtra("relationId", workshopId);
                intent2.putExtra("role", role);
                startActivity(intent2);
                break;
            case R.id.tv_bottom:   //添加新阶段
                smoothToBottom();
                break;
        }
    }

    private void smoothToBottom() {
        if (mWorkshopSections.size() > 0) {
            sectionAdapter.setAddTask(true);
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    contentView.fullScroll(ScrollView.FOCUS_DOWN);
                    tv_bottom.setVisibility(View.GONE);
                }
            }, 10);
        } else {
            Intent intent = new Intent(context, WorkShopEditTaskActivity.class);
            intent.putExtra("workShopId", workshopId);
            intent.putExtra("create", true);
            startActivityForResult(intent, REQUEST_SECTION);
        }
    }
}
