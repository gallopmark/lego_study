package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.adapter.BriefingAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.entity.BriefingsResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WorkShopDetailResult;
import com.haoyu.app.entity.WorkShopExecllentUserResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.CustomViewPager;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2016/12/29 on 20:29
 * 描述: 工作坊简介页面
 * 作者:马飞奔 Administrator
 */
public class WorkShopDetailActivity extends BaseActivity {
    private WorkShopDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.contentView)
    ScrollView contentView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_workshopTitle)
    TextView tv_workshopTitle;   //工作坊标题
    @BindView(R.id.tv_project)
    TextView tv_project; //所属项目
    @BindView(R.id.tv_researchTime)
    TextView tv_researchTime;  //研修时间
    @BindView(R.id.tv_creator)
    TextView tv_creator; //创建人姓名
    @BindView(R.id.tv_workshopType)
    TextView tv_workshopType; //工作坊类型
    @BindView(R.id.tv_train)
    TextView tv_train; //培训时间
    @BindView(R.id.tv_workshop_detail)
    ExpandableTextView tv_workshop_detail; //工作坊简介
    @BindView(R.id.empty_summary)
    View empty_summary;
    @BindView(R.id.mFileContent)
    LinearLayout mFileContent;
    @BindView(R.id.tv_emptyFile)
    TextView tv_emptyFile;  //空文件
    @BindView(R.id.iv_fileType)
    ImageView iv_fileType;  //文件类型
    @BindView(R.id.tv_mFileName)
    TextView tv_mFileName; //文件名
    @BindView(R.id.tv_mFileSize)
    TextView tv_mFileSize;  //文件大小
    @BindView(R.id.research_students)
    TextView research_students; //参研学员数量
    @BindView(R.id.study_task)
    TextView study_task;  //研修任务数量
    @BindView(R.id.student_questions)
    TextView student_questions; //学员提问数量
    @BindView(R.id.study_resources)
    TextView study_resources;//学习资源数量
    @BindView(R.id.excellentPager)
    CustomViewPager excellentPager;
    private ExcellentPager excellentAdapter;
    @BindView(R.id.bodyErrorView)
    LoadFailView bodyErrorView;   //加载优秀学员失败
    @BindView(R.id.tv_emptyStudent)
    TextView tv_emptyStudent;  //空优秀学员
    @BindView(R.id.excellentIndicator)
    LinearLayout excellentIndicator;
    @BindView(R.id.briefRecyclerView)
    RecyclerView briefRecyclerView;  //简报列表
    private List<BriefingEntity> briefList = new ArrayList<>();
    private BriefingAdapter briefAdapter;
    @BindView(R.id.briefErrorView)
    LoadFailView briefErrorView;   //加载研修简报失败
    @BindView(R.id.empty_brief)
    View empty_brief;  //空简报内容
    @BindView(R.id.brief_pack_up)
    Button brief_pack_up;  //工作坊简介(研修简报)展开内容或者收起内容
    private ImageView[] excellentIndicatorViews;
    private ArrayMap<Integer, List<MobileUser>> dataMap = new ArrayMap<>();
    private String workshopId;
    private int userPage = 1, limit = 4;
    private int userTotalPage;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_workshop_detail;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        briefRecyclerView.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        briefRecyclerView.setLayoutManager(layoutManager);
        briefAdapter = new BriefingAdapter(briefList);
        briefRecyclerView.setAdapter(briefAdapter);
        briefRecyclerView.setFocusable(false);
        excellentAdapter = new ExcellentPager(dataMap);
        excellentPager.setAdapter(excellentAdapter);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/workshop/" + workshopId + "/detail";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkShopDetailResult>() {
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
            public void onResponse(WorkShopDetailResult response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshop() != null) {
                    updateUI(response.getResponseData().getmWorkshop(), response.getResponseData().getmFileInfo());
                    getExcellentUsers(true);
                    getBriefList();
                }
            }
        }));
    }

    private void updateUI(WorkShopMobileEntity mWSMEntity, final MFileInfo fileInfo) {
        contentView.setVisibility(View.VISIBLE);
        tv_workshopTitle.setText(mWSMEntity.getTitle());
        tv_project.setText(mWSMEntity.getTrainName());
        if (mWSMEntity.getCreator() != null) {
            tv_creator.setText(mWSMEntity.getCreator().getRealName());
        }
        if (mWSMEntity.getType() != null && mWSMEntity.getType().equals("personal")) {
            tv_workshopType.setText("个人工作坊");
        } else if (mWSMEntity.getType() != null && mWSMEntity.getType().equals("train")) {
            tv_workshopType.setText("项目工作坊");
        } else if (mWSMEntity.getType() != null && mWSMEntity.getType().equals("template")) {
            tv_workshopType.setText("示范性工作坊");
        } else {
            tv_workshopType.setText("未知类型");
        }
        tv_train.setText(String.valueOf(mWSMEntity.getStudyHours()) + "学时");
        if (mWSMEntity.getSummary() != null && mWSMEntity.getSummary().length() > 0) {
            Spanned spanned = Html.fromHtml(mWSMEntity.getSummary());
            tv_workshop_detail.setText(spanned);
        } else {
            tv_workshop_detail.setVisibility(View.GONE);
            empty_summary.setVisibility(View.VISIBLE);
        }
        if (mWSMEntity.getTimePeriod() != null) {
            tv_researchTime.setText(TimeUtil.getSlashDate(mWSMEntity.getTimePeriod().getStartTime()) + "~" +
                    TimeUtil.getSlashDate(mWSMEntity.getTimePeriod().getEndTime()));
        } else {
            tv_researchTime.setText("未设置");
        }
        research_students.setText(String.valueOf(mWSMEntity.getStudentNum()));
        study_task.setText(String.valueOf(mWSMEntity.getActivityNum()));
        student_questions.setText(String.valueOf(mWSMEntity.getFaqQuestionNum()));
        study_resources.setText(String.valueOf(mWSMEntity.getResourceNum()));
        if (fileInfo != null) {
            mFileContent.setVisibility(View.VISIBLE);
            tv_emptyFile.setVisibility(View.GONE);
            Common.setFileType(fileInfo.getUrl(), iv_fileType);
            tv_mFileName.setText(fileInfo.getFileName());
            tv_mFileSize.setText(Common.FormetFileSize(fileInfo.getFileSize()));
            mFileContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (fileInfo.getUrl() != null) {
                        Intent intent = new Intent(context, MFileInfoActivity.class);
                        intent.putExtra("fileInfo", fileInfo);
                        startActivity(intent);
                    } else {
                        toast(context, "文件链接不存在");
                    }
                }
            });
        } else {
            mFileContent.setVisibility(View.GONE);
            tv_emptyFile.setVisibility(View.VISIBLE);
        }
    }

    /*获取优秀学员*/
    private void getExcellentUsers(final boolean firstLoad) {
        String usersUrl = Constants.OUTRT_NET + "/m/workshop_user/" + workshopId + "/excellent_users?page=" + userPage + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, usersUrl, new OkHttpClientManager.ResultCallback<WorkShopExecllentUserResult>() {
            @Override
            public void onError(Request request, Exception e) {
                if (firstLoad) {
                    bodyErrorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(WorkShopExecllentUserResult response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshopUsers() != null
                        && response.getResponseData().getmWorkshopUsers().size() > 0) {
                    updateUserPage(response.getResponseData().getmWorkshopUsers(), response.getResponseData().getPaginator(), firstLoad);
                } else {
                    excellentPager.setVisibility(View.GONE);
                    tv_emptyStudent.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*更新优秀学员page*/
    private void updateUserPage(List<WorkShopMobileUser> workShopMobileUsers, Paginator paginator, boolean firstLoad) {
        if (paginator != null && firstLoad) {
            userTotalPage = paginator.getTotalPages();
            updateUserIndicator(userTotalPage);
        }
        List<MobileUser> users = new ArrayList<>();
        for (int i = 0; i < workShopMobileUsers.size(); i++) {
            if (workShopMobileUsers.get(i).getmUser() != null) {
                users.add(workShopMobileUsers.get(i).getmUser());
            }
        }
        if (users.size() > 0) {
            excellentPager.setVisibility(View.VISIBLE);
            tv_emptyStudent.setVisibility(View.GONE);
            dataMap.put(userPage - 1, users);
            excellentAdapter.notifyDataSetChanged();
        }
    }

    /*创建优秀学员分页提示*/
    private void updateUserIndicator(int totalPage) {
        if (totalPage > 0) {
            excellentIndicatorViews = new ImageView[totalPage];
            for (int i = 0; i < totalPage; i++) {   //位置从0开始 页数从1开始
                excellentIndicatorViews[i] = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_6);
                excellentIndicatorViews[i].setLayoutParams(params);
                excellentIndicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                excellentIndicator.addView(excellentIndicatorViews[i]);
                dataMap.put(i, null);
            }
            excellentIndicatorViews[0].setImageResource(R.drawable.course_yuandian_press);
            if (totalPage > 1) {
                excellentIndicator.setVisibility(View.VISIBLE);
            } else {
                excellentIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void getBriefList() {
        String url = Constants.OUTRT_NET + "/m/briefing?announcementRelations[0].relation.id=" + workshopId
                + "&announcementRelations[0].relation.type=workshop" + "&type=workshop_briefing" + "&orders=CREATE_TIME.DESC" + "&limit=5";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BriefingsResult>() {
            @Override
            public void onError(Request request, Exception e) {
                briefErrorView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BriefingsResult response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().getAnnouncements() != null
                        && response.getResponseData().getAnnouncements().size() > 0) {
                    updateBriefList(response.getResponseData().getAnnouncements(), response.getResponseData().getPaginator());
                } else {
                    brief_pack_up.setVisibility(View.GONE);
                    empty_brief.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*刷新研修简报列表*/
    private void updateBriefList(List<BriefingEntity> mDatas, Paginator paginator) {
        briefList.addAll(mDatas);
        briefAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            brief_pack_up.setVisibility(View.VISIBLE);
            empty_brief.setVisibility(View.GONE);
        }
        briefAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, BriefingDetailActivity.class);
                intent.putExtra("relationId", briefList.get(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        bodyErrorView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getExcellentUsers(true);
            }
        });

        briefErrorView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getBriefList();
            }
        });
        brief_pack_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BriefingActivity.class);
                intent.putExtra("relationId", workshopId);
                intent.putExtra("relationType", "workshop");
                intent.putExtra("type", "workshop_briefing");
                startActivity(intent);
            }
        });
        excellentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (excellentIndicatorViews != null && excellentIndicatorViews.length > 0) {
                    for (int i = 0; i < excellentIndicatorViews.length; i++) {
                        if (i == position)
                            excellentIndicatorViews[i].setImageResource(R.drawable.course_yuandian_press);
                        else
                            excellentIndicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                    }
                }
                if (dataMap.get(position) == null) {
                    userPage = position + 1;
                    if (userPage <= userTotalPage) {
                        getExcellentUsers(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class ExcellentPager extends PagerAdapter {
        private Map<Integer, List<MobileUser>> dataMap;

        public ExcellentPager(ArrayMap<Integer, List<MobileUser>> dataMap) {
            this.dataMap = dataMap;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return userTotalPage;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
            View view = getLayoutInflater().inflate(R.layout.workshop_excellent_user_item, null);
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(layoutManager);
            if (dataMap.get(position) != null) {
                ExcellentRecyclerAdapter adapter = new ExcellentRecyclerAdapter(dataMap.get(position));
                recyclerView.setAdapter(adapter);
            } else {
                recyclerView.setVisibility(View.GONE);
            }
            container.addView(view, 0);//添加页卡
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);//删除页卡
        }

        class ExcellentRecyclerAdapter extends BaseArrayRecyclerAdapter<MobileUser> {

            public ExcellentRecyclerAdapter(List<MobileUser> mDatas) {
                super(mDatas);
            }

            @Override
            public void onBindHoder(RecyclerHolder holder, MobileUser mobileUser, int position) {
                View contentView = holder.obtainView(R.id.contentView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtils.getScreenWidth(context) / 4, LinearLayout.LayoutParams.WRAP_CONTENT);
                contentView.setLayoutParams(params);
                ImageView userIco = holder.obtainView(R.id.userIco);
                TextView userName = holder.obtainView(R.id.userName);
                GlideImgManager.loadCircleImage(context, mobileUser.getAvatar(),
                        R.drawable.user_default, R.drawable.user_default, userIco);
                userName.setText(mobileUser.getRealName());
            }

            @Override
            public int bindView(int viewtype) {
                return R.layout.workshop_detail_excellent_item;
            }
        }
    }
}
