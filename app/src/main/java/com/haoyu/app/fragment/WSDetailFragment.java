package com.haoyu.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.activity.BriefingActivity;
import com.haoyu.app.activity.BriefingDetailActivity;
import com.haoyu.app.activity.MFileInfoActivity;
import com.haoyu.app.adapter.BriefingAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.entity.BriefingsResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WorkShopExecllentUserResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.PullToLoadMoreLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/11/13.
 * 描述:工作坊简介
 * 作者:xiaoma
 */

public class WSDetailFragment extends BaseFragment implements PullToLoadMoreLayout.OnPullToLeftListener {
    @BindView(R.id.tv_title)
    TextView tv_title;   //工作坊标题
    @BindView(R.id.tv_project)
    TextView tv_project; //所属项目
    @BindView(R.id.tv_time)
    TextView tv_time;  //研修时间
    @BindView(R.id.tv_creator)
    TextView tv_creator; //创建人姓名
    @BindView(R.id.tv_type)
    TextView tv_type; //工作坊类型
    @BindView(R.id.tv_train)
    TextView tv_train; //培训时间
    @BindView(R.id.tv_content)
    TextView tv_content; //工作坊简介
    @BindView(R.id.tv_emptySummary)
    TextView tv_emptySummary;
    @BindView(R.id.ll_fileLayout)
    LinearLayout ll_fileLayout;
    @BindView(R.id.tv_emptyFile)
    TextView tv_emptyFile;  //空文件
    @BindView(R.id.tv_students)
    TextView tv_students; //参研学员数量
    @BindView(R.id.tv_tasks)
    TextView tv_tasks;  //研修任务数量
    @BindView(R.id.tv_questions)
    TextView tv_questions; //学员提问数量
    @BindView(R.id.tv_resources)
    TextView tv_resources;//学习资源数量
    @BindView(R.id.pull_group)
    PullToLoadMoreLayout pull_group;
    @BindView(R.id.rv_student)
    RecyclerView rv_student;
    private List<MobileUser> users = new ArrayList<>();
    private UsersAdapter userAdapter;
    private boolean isLoadMore;
    @BindView(R.id.tv_emptyStudent)
    TextView tv_emptyStudent;  //空优秀学员
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;  //简报列表
    private List<BriefingEntity> mDatas = new ArrayList<>();
    private BriefingAdapter adapter;
    @BindView(R.id.tv_empty)
    TextView tv_empty;  //空简报内容
    @BindView(R.id.bt_more)
    Button bt_more;  //工作坊简介(研修简报)展开内容或者收起内容
    private String workshopId;
    private int userPage = 1, limit = 20;

    @Override
    public int createView() {
        return R.layout.fragment_wsdetail;
    }

    @Override
    public void initView(View view) {
        FullyLinearLayoutManager gridManager = new FullyLinearLayoutManager(context);
        gridManager.setOrientation(FullyLinearLayoutManager.HORIZONTAL);
        rv_student.setLayoutManager(gridManager);
        userAdapter = new UsersAdapter(users);
        rv_student.setAdapter(userAdapter);
        pull_group.setOnPullToLeftListener(this);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BriefingAdapter(mDatas);
        recyclerView.setAdapter(adapter);
        Bundle bundle = getArguments();
        WorkShopMobileEntity entity = (WorkShopMobileEntity) bundle.getSerializable("entity");
        MFileInfo fileInfo = (MFileInfo) bundle.getSerializable("fileInfo");
        updateUI(entity, fileInfo);
    }

    private void updateUI(WorkShopMobileEntity entity, final MFileInfo fileInfo) {
        workshopId = entity.getId();
        tv_title.setText(entity.getTitle());
        String trainName = "所属项目：";
        if (entity.getTrainName() != null) {
            trainName += entity.getTrainName();
        } else {
            trainName += "--";
        }
        tv_project.setText(trainName);
        String timeP = "研修时间：";
        if (entity.getTimePeriod() != null) {
            timeP += TimeUtil.getSlashDate(entity.getTimePeriod().getStartTime()) + "~" +
                    TimeUtil.getSlashDate(entity.getTimePeriod().getEndTime());
        } else {
            timeP += "--";
        }
        tv_time.setText(timeP);
        String creator = "\u3000创建人：";
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            creator += entity.getCreator().getRealName();
        } else {
            creator += "--";
        }
        tv_creator.setText(creator);
        String type = "\u3000\u3000类型：";
        if (entity.getType() != null && entity.getType().equals("personal")) {
            type += "个人工作坊";
        } else if (entity.getType() != null && entity.getType().equals("train")) {
            type += "项目工作坊";
        } else if (entity.getType() != null && entity.getType().equals("template")) {
            type += "示范性工作坊";
        } else {
            type += "--";
        }
        tv_type.setText(type);
        tv_train.setText("培训时间：" + entity.getStudyHours() + "学时");
        if (entity.getSummary() != null && entity.getSummary().length() > 0) {
            Spanned spanned = Html.fromHtml(entity.getSummary());
            tv_content.setText(spanned);
            tv_content.setVisibility(View.VISIBLE);
            tv_emptySummary.setVisibility(View.GONE);
        } else {
            tv_content.setVisibility(View.GONE);
            tv_emptySummary.setVisibility(View.VISIBLE);
        }
        setNum_text(entity.getStudentNum(), entity.getActivityNum(), entity.getFaqQuestionNum(), entity.getResourceNum());
        if (fileInfo != null) {
            ll_fileLayout.setVisibility(View.VISIBLE);
            ImageView iv_fileType = ll_fileLayout.findViewById(R.id.iv_fileType);
            TextView tv_mFileName = ll_fileLayout.findViewById(R.id.tv_mFileName);
            TextView tv_mFileSize = ll_fileLayout.findViewById(R.id.tv_mFileSize);
            Common.setFileType(fileInfo.getUrl(), iv_fileType);
            tv_mFileName.setText(fileInfo.getFileName());
            tv_mFileSize.setText(Common.FormetFileSize(fileInfo.getFileSize()));
            tv_emptyFile.setVisibility(View.GONE);
            ll_fileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
                    startActivity(intent);
                }
            });
        } else {
            ll_fileLayout.setVisibility(View.GONE);
            tv_emptyFile.setVisibility(View.VISIBLE);
        }
    }

    private void setNum_text(int studentNum, int activityNum, int faqQuestionNum, int resourceNum) {
        SpannableString ssb;
        int start = 0, end;
        int color = ContextCompat.getColor(context, R.color.darksalmon);
        String text_study = studentNum + "\n参研学员";
        ssb = new SpannableString(text_study);
        end = text_study.indexOf("参") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_students.setText(ssb);
        String text_activity = activityNum + "\n研修任务";
        ssb = new SpannableString(text_activity);
        end = text_activity.indexOf("研") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_tasks.setText(ssb);
        String text_question = faqQuestionNum + "\n学员提问";
        ssb = new SpannableString(text_question);
        end = text_question.indexOf("学") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_questions.setText(ssb);
        String text_resource = resourceNum + "\n学习资源";
        ssb = new SpannableString(text_resource);
        end = text_resource.indexOf("学") - 1;
        ssb.setSpan(new AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_resources.setText(ssb);
    }

    @Override
    public void initData() {
        getUsers();
        getBriefList();
    }

    /*获取优秀学员*/
    private void getUsers() {
        String url = Constants.OUTRT_NET + "/m/workshop_user/" + workshopId + "/excellent_users?page=" + userPage + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkShopExecllentUserResult>() {
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
            public void onResponse(WorkShopExecllentUserResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshopUsers() != null
                        && response.getResponseData().getmWorkshopUsers().size() > 0) {
                    updateUserPage(response.getResponseData().getmWorkshopUsers(), response.getResponseData().getPaginator());
                } else {
                    if (isLoadMore) {
                        pull_group.completeToUpload();
                        pull_group.setLoadingMoreEnabled(false);
                    } else {
                        pull_group.setVisibility(View.GONE);
                        tv_emptyStudent.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    /*更新优秀学员page*/
    private void updateUserPage(List<WorkShopMobileUser> workShopMobileUsers, Paginator paginator) {
        for (int i = 0; i < workShopMobileUsers.size(); i++) {
            if (workShopMobileUsers.get(i).getmUser() != null) {
                users.add(workShopMobileUsers.get(i).getmUser());
            }
        }
        userAdapter.notifyDataSetChanged();
        if (isLoadMore) {
            pull_group.completeToUpload();
        }
        if (paginator != null && paginator.getHasNextPage()) {
            pull_group.setLoadingMoreEnabled(true);
        } else {
            pull_group.setLoadingMoreEnabled(false);
        }
    }

    private void getBriefList() {
        String url = Constants.OUTRT_NET + "/m/briefing?announcementRelations[0].relation.id=" + workshopId
                + "&announcementRelations[0].relation.type=workshop" + "&type=workshop_briefing" + "&orders=CREATE_TIME.DESC" + "&limit";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BriefingsResult>() {
            @Override
            public void onBefore(Request request) {
                bt_more.setVisibility(View.GONE);
                tv_empty.setVisibility(View.GONE);
            }

            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError();
            }

            @Override
            public void onResponse(BriefingsResult response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().getAnnouncements() != null && response.getResponseData().getAnnouncements().size() > 0) {
                    updateBriefList(response.getResponseData().getAnnouncements(), response.getResponseData().getPaginator());
                } else {
                    bt_more.setVisibility(View.GONE);
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*刷新研修简报列表*/
    private void updateBriefList(List<BriefingEntity> list, Paginator paginator) {
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            bt_more.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        }
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, BriefingDetailActivity.class);
                intent.putExtra("relationId", mDatas.get(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void setListener() {
        bt_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BriefingActivity.class);
                intent.putExtra("relationId", workshopId);
                intent.putExtra("relationType", "workshop");
                intent.putExtra("type", "workshop_briefing");
                startActivity(intent);
            }
        });
    }

    private class UsersAdapter extends BaseArrayRecyclerAdapter<MobileUser> {
        private int width;

        public UsersAdapter(List<MobileUser> mDatas) {
            super(mDatas);
            width = ScreenUtils.getScreenWidth(context) / 4;
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.peer_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MobileUser user, int position) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.bottomMargin = PixelFormat.dp2px(context, 16);
            holder.itemView.setLayoutParams(params);
            ImageView iv = holder.obtainView(R.id.person_img);
            TextView tv = holder.obtainView(R.id.person_name);
            tv.setText(user.getRealName());
            GlideImgManager.loadCircleImage(context, user.getAvatar(), R.drawable.user_default, R.drawable.user_default, iv);
        }
    }

    @Override
    public void onReleaseFingerToUpload() {
        isLoadMore = true;
        userPage++;
        getUsers();
    }

    @Override
    public void onStartToUpload() {

    }

}
