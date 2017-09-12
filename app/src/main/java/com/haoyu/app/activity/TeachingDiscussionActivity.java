package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.StickyScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/8/17 on 10:05
 * 描述:教学研讨（包括课程学习教学研讨活动和工作坊研修教学研讨活动）
 * 作者:马飞奔 Administrator
 */
public class TeachingDiscussionActivity extends BaseActivity implements View.OnClickListener {
    private TeachingDiscussionActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.scrollView)
    StickyScrollView scrollView;
    @BindView(R.id.tv_discussTime)
    TextView tv_discussTime;   //教学研讨时间
    @BindView(R.id.tv_discussTitle)
    TextView tv_discussTitle;  //教学研讨标题
    @BindView(R.id.tv_partNum)
    TextView tv_partNum;   //参与人数
    @BindView(R.id.tv_browseNum)
    TextView tv_browseNum;   //浏览人数
    @BindView(R.id.tv_discussContent)
    ExpandableTextView tv_discussContent;   //教学研讨内容
    @BindView(R.id.ll_fileLayout)
    LinearLayout ll_fileLayout;   //文档
    @BindView(R.id.fileIndicator)
    LinearLayout fileIndicator;  //指示器
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.ll_discussion)
    LinearLayout ll_discussion;
    @BindView(R.id.tv_discussCount)
    TextView tv_discussCount;   //研讨回复数量
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.empty_comment)
    LinearLayout empty_comment;
    @BindView(R.id.tv_comment)
    TextView tv_comment;
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;   //更多回复内容
    @BindView(R.id.tv_bottomView)
    TextView tv_bottomView;
    private boolean running;   //是否在培训时间内、活动是否进行中
    private TimePeriod timePeriod;
    private String discussType, activityId, workshopId, activityTitle, discussionRelationId, baseUrl, postUrl;
    private int needMainNum, needSubNum, mainNum, subNum;  //要求完成主回复，子回复；已完成主回复，子回复。
    private AppActivityViewEntity.DiscussionUserMobileEntity discussEntity;
    private ImageView[] fileIndicatorViews;
    private int discussNum;//总回复数
    private List<ReplyEntity> mDatas = new ArrayList<>();
    private AppDiscussionAdapter adapter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_discussion;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        discussType = getIntent().getStringExtra("discussType");
        workshopId = getIntent().getStringExtra("workshopId");
        activityId = getIntent().getStringExtra("activityId");
        activityTitle = getIntent().getStringExtra("activityTitle");
        needMainNum = getIntent().getIntExtra("needMainNum", 0);
        needSubNum = getIntent().getIntExtra("needSubNum", 0);
        mainNum = getIntent().getIntExtra("mainNum", 0);
        subNum = getIntent().getIntExtra("subNum", 0);
        discussEntity = (AppActivityViewEntity.DiscussionUserMobileEntity) getIntent().getSerializableExtra("discussUser");
        if (discussType != null && discussType.equals("course")) {
            baseUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/" + activityId + "unique_uid_" + getUserId() + "/m/discussion/post";
        } else {
            baseUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/student_" + workshopId + "unique_uid_" + getUserId() + "/m/discussion/post";
        }
        setSupportToolbar();
        showTips();
        if (discussEntity != null && discussEntity.getmDiscussion() != null && discussEntity.getmDiscussion().getmDiscussionRelations() != null && discussEntity.getmDiscussion().getmDiscussionRelations().size() > 0)
            discussionRelationId = discussEntity.getmDiscussion().getmDiscussionRelations().get(0).getId();
        if (discussEntity != null && discussEntity.getmDiscussion() != null)
            showData(discussEntity.getmDiscussion());
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AppDiscussionAdapter(context, mDatas, getUserId());
        recyclerView.setAdapter(adapter);
        registRxBus();
    }

    private void setSupportToolbar() {
        if (activityTitle == null)
            toolBar.setTitle_text("教学研讨");
        else
            toolBar.setTitle_text(activityTitle);
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showTopView();
            }
        });
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "要求完成 <font color='#ffa500'>" + needMainNum + "</font> 个回复，<font color='#ffa500'>" + needSubNum + "</font> 个子回复 / 您已完成 <font color='#ffa500'>" + mainNum + "</font> 个回复，<font color='#ffa500'>" + subNum + "</font> 个子回复。";
        tv_tips.setText(Html.fromHtml(message));
    }

    private void showTopView() {
        ll_tips.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_show);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolBar.setShow_right_button(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showData(DiscussEntity discussEntity) {
        if (running) {
            if (timePeriod != null && timePeriod.getMinutes() > 0) {
                String time = "离活动结束还剩：" + TimeUtil.dateDiff(timePeriod.getMinutes());
                tv_discussTime.setText(Html.fromHtml(time));
            } else {
                if (timePeriod != null && timePeriod.getState() != null) {
                    tv_discussTime.setText("活动" + timePeriod.getState());
                } else {
                    tv_discussTime.setText("活动进行中");
                }
            }
        } else {
            tv_discussTime.setText("活动已结束");
        }
        if (discussEntity.getmDiscussionRelations() != null && discussEntity.getmDiscussionRelations().size() > 0) {
            int browseNum = discussEntity.getmDiscussionRelations().get(0).getBrowseNum();
            tv_browseNum.setText("浏览人数：" + browseNum);
            int followNum = discussEntity.getmDiscussionRelations().get(0).getParticipateNum();
            tv_partNum.setText("参与人数：" + followNum);
        }
        tv_discussTitle.setText(discussEntity.getTitle());
        tv_discussContent.setHtmlText(discussEntity.getContent());
        if (discussEntity.getmFileInfos() != null && discussEntity.getmFileInfos().size() > 0) {
            ll_fileLayout.setVisibility(View.VISIBLE);
            FilePageAdapter filePageAdapter = new FilePageAdapter(discussEntity.getmFileInfos());
            viewPager.setAdapter(filePageAdapter);
            if (discussEntity.getmFileInfos().size() > 1) {
                fileIndicatorViews = new ImageView[discussEntity.getmFileInfos().size()];
                for (int i = 0; i < discussEntity.getmFileInfos().size(); i++) {   //位置从0开始 页数从1开始
                    fileIndicatorViews[i] = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_5);
                    fileIndicatorViews[i].setLayoutParams(params);
                    fileIndicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                    fileIndicator.addView(fileIndicatorViews[i]);
                }
                fileIndicatorViews[0].setImageResource(R.drawable.course_yuandian_press);
            }

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (fileIndicatorViews != null && fileIndicatorViews.length > 0) {
                        for (int i = 0; i < fileIndicatorViews.length; i++) {
                            if (i == position)
                                fileIndicatorViews[i].setImageResource(R.drawable.course_yuandian_press);
                            else
                                fileIndicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            ll_fileLayout.setVisibility(View.GONE);
        }
    }

    class FilePageAdapter extends PagerAdapter {
        private List<MFileInfo> fileInfos;

        public FilePageAdapter(List<MFileInfo> fileInfos) {
            this.fileInfos = fileInfos;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return fileInfos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.page_file_item, null);
            ImageView iv_fileType = view.findViewById(R.id.iv_fileType);
            TextView tv_mFileName = view.findViewById(R.id.tv_mFileName);
            TextView tv_mFileSize = view.findViewById(R.id.tv_mFileSize);
            final MFileInfo fileInfo = fileInfos.get(position);
            Common.setFileType(fileInfo.getUrl(), iv_fileType);
            tv_mFileName.setText(fileInfo.getFileName());
            tv_mFileSize.setText(Common.FormetFileSize(fileInfo.getFileSize()));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fileInfo.getUrl() == null)
                        toast(context, "下载的链接不存在");
                    else {
                        Intent intent = new Intent(context, MFileInfoActivity.class);
                        intent.putExtra("fileInfo", fileInfo);
                        startActivity(intent);
                    }
                }
            });
            container.addView(view, 0);//添加页卡
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);//删除页卡
        }
    }

    @Override
    public void initData() {
        String url = baseUrl + "?discussionUser.discussionRelation.id=" + discussionRelationId + "&orders=CREATE_TIME.ASC&limit=5";
        loadingView.setVisibility(View.VISIBLE);
        tv_more_reply.setVisibility(View.GONE);
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String url) throws Exception {
                return getMainReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                String url = baseUrl + "?discussionUser.discussionRelation.id=" + discussionRelationId + "&orders=CREATE_TIME.ASC&limit=10";
                return getChildReply(url, result);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReplyListResult>() {
                    @Override
                    public void accept(ReplyListResult response) throws Exception {
                        loadingView.setVisibility(View.GONE);
                        updateUI(response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        loadingView.setVisibility(View.GONE);
                        loadFailView.setVisibility(View.VISIBLE);
                    }
                }));
    }

    /*获取主回复*/
    private ReplyListResult getMainReply(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, ReplyListResult.class);
    }

    /*通过主回复id获取子回复*/
    private ReplyListResult getChildReply(String url, ReplyListResult result) {
        if (result != null && result.getResponseData() !=
                null && result.getResponseData().getmDiscussionPosts() != null) {
            for (int i = 0; i < result.getResponseData().getmDiscussionPosts().size(); i++) {
                String mainPostId = result.getResponseData().getmDiscussionPosts().get(i).getId();
                String _url = url + "&mainPostId=" + mainPostId;
                try {
                    ReplyListResult mResult = getMainReply(_url);
                    if (mResult.getResponseData() != null) {
                        result.getResponseData().getmDiscussionPosts().get(i).setChildReplyEntityList(mResult.getResponseData().getmDiscussionPosts());
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return result;
    }

    private void updateUI(ReplyListResult response) {
        mDatas.clear();
        ll_discussion.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmDiscussionPosts() != null
                && response.getResponseData().getmDiscussionPosts().size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            mDatas.addAll(response.getResponseData().getmDiscussionPosts());
            adapter.notifyDataSetChanged();
            if (response.getResponseData().getPaginator() != null && response.getResponseData().getPaginator().getHasNextPage())
                tv_more_reply.setVisibility(View.VISIBLE);
            if (response.getResponseData().getPaginator() != null)
                discussNum = response.getResponseData().getPaginator().getTotalCount();
        } else {
            empty_comment.setVisibility(View.VISIBLE);
        }
        tv_discussCount.setText(String.valueOf(discussNum));
    }

    private int childPosition, replyPosition;

    @Override
    public void setListener() {
        tv_close.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        ll_discussion.setOnClickListener(context);
        tv_comment.setOnClickListener(context);
        tv_bottomView.setOnClickListener(context);
        tv_more_reply.setOnClickListener(context);
        adapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, int position, ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, int position) {
                childPosition = position;
                if (running) {
                    showCommentDialog(true);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
            }
        });
        adapter.setSupportCallBack(new AppDiscussionAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position);
            }
        });
        adapter.setMoreReplyCallBack(new AppDiscussionAdapter.MoreReplyCallBack() {
            @Override
            public void callBack(ReplyEntity entity, int position) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreChildReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("entity", entity);
                intent.putExtra("discussType", discussType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("relationId", discussionRelationId);
                if (running)
                    intent.putExtra("canEdit", true);
                else
                    intent.putExtra("canEdit", false);
                startActivity(intent);
            }
        });
        adapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                if (running) {
                    deleteReply(id, position);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
            }
        });
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildReply(childPosition, content);
                } else {
                    sendMainReply(content);
                }
            }
        });
    }

    /*创建子回复*/
    private void sendChildReply(final int position, final String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", mDatas.get(position).getId());
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            public void onError(Request request, Exception exception) {
                hideTipDialog();
                onNetWorkError(context);
            }

            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    ReplyEntity entity = response.getResponseData();
                    if (entity.getCreator() == null) {
                        MobileUser creator = new MobileUser();
                        creator.setId(getUserId());
                        creator.setAvatar(getAvatar());
                        creator.setRealName(getRealName());
                        entity.setCreator(creator);
                    } else {
                        if (entity.getCreator().getId() == null || (entity.getCreator().getId() != null && entity.getCreator().getId().toLowerCase().equals("null")))
                            entity.getCreator().setId(getUserId());
                        if (entity.getCreator().getAvatar() == null || (entity.getCreator().getAvatar() != null && entity.getCreator().getAvatar().toLowerCase().equals("null")))
                            entity.getCreator().setAvatar(getAvatar());
                        if (entity.getCreator().getRealName() == null || (entity.getCreator().getRealName() != null && entity.getCreator().getRealName().toLowerCase().equals("null")))
                            entity.getCreator().setRealName(getRealName());
                    }
                    int childPostCount = mDatas.get(position).getChildPostCount() + 1;
                    mDatas.get(position).setChildPostCount(childPostCount);
                    if (mDatas.get(position).getChildReplyEntityList() != null && mDatas.get(position).getChildReplyEntityList().size() < 10) {
                        mDatas.get(position).getChildReplyEntityList().add(entity);
                    } else {
                        toastFullScreen("评论成功", true);
                    }
                    adapter.notifyDataSetChanged();
                    getActivityInfo();
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /*创建主回复*/
    private void sendMainReply(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    if (recyclerView.getVisibility() != View.VISIBLE)
                        recyclerView.setVisibility(View.VISIBLE);
                    if (empty_comment.getVisibility() != View.GONE)
                        empty_comment.setVisibility(View.GONE);
                    if (mDatas.size() < 5) {
                        ReplyEntity entity = response.getResponseData();
                        if (entity.getCreator() == null) {
                            MobileUser creator = new MobileUser();
                            creator.setId(getUserId());
                            creator.setAvatar(getAvatar());
                            creator.setRealName(getRealName());
                            entity.setCreator(creator);
                        } else {
                            if (entity.getCreator().getId() == null || (entity.getCreator().getId() != null && entity.getCreator().getId().toLowerCase().equals("null")))
                                entity.getCreator().setId(getUserId());
                            if (entity.getCreator().getAvatar() == null || (entity.getCreator().getAvatar() != null && entity.getCreator().getAvatar().toLowerCase().equals("null")))
                                entity.getCreator().setAvatar(getAvatar());
                            if (entity.getCreator().getRealName() == null || (entity.getCreator().getRealName() != null && entity.getCreator().getRealName().toLowerCase().equals("null")))
                                entity.getCreator().setRealName(getRealName());
                        }
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                    getActivityInfo();
                    discussNum++;
                    tv_discussCount.setText(String.valueOf(discussNum));
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /**
     * 创建评论列表点赞观点(点赞)
     *
     * @param position
     */
    private void createLike(final int position) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = mDatas.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    int count = mDatas.get(position).getSupportNum() + 1;
                    mDatas.get(position).setSupportNum(count);
                    adapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    private void deleteReply(String id, final int position) {
        String url = postUrl + "/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        map.put("mainPostId", id);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null
                        && response.getResponseCode().equals("00")) {
                    mDatas.remove(position);
                    adapter.notifyDataSetChanged();
                    if (mDatas.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        empty_comment.setVisibility(View.VISIBLE);
                    }
                    discussNum--;
                    tv_discussCount.setText(String.valueOf(discussNum));
                    getActivityInfo();
                    initData();
                }
            }
        }, map));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_close:
                hideTopView();
                return;
            case R.id.ll_discussion:
                scrollView.scrollTo(0, recyclerView.getTop());
                return;
            case R.id.tv_comment:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
                return;
            case R.id.tv_bottomView:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
                return;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreMainReplyActivity.class);
                intent.putExtra("type", "comment");
                intent.putExtra("discussType", discussType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("relationId", discussionRelationId);
                if (running)
                    intent.putExtra("canEdit", true);
                else
                    intent.putExtra("canEdit", false);
                startActivity(intent);
                return;
        }
    }

    private void hideTopView() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_hide);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_tips.setVisibility(View.GONE);
                    toolBar.setShow_right_button(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            ll_tips.setVisibility(View.GONE);
            toolBar.setShow_right_button(true);
        }
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            discussNum++;
            tv_discussCount.setText(String.valueOf(discussNum));
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (mDatas.size() < 5) {
                mDatas.add(entity);
                adapter.notifyDataSetChanged();
            }
            getActivityInfo();
        } else if (event.action.equals(Action.CREATE_CHILD_REPLY)) {
            /*来自更多评论列表界面创建子回复*/
            if (event.getBundle() != null && event.getBundle().getSerializable("mainReply") != null
                    && event.getBundle().getSerializable("mainReply") instanceof ReplyEntity) {
                ReplyEntity mainReply = (ReplyEntity) event.getBundle().getSerializable("mainReply");
                int position = mDatas.indexOf(mainReply);
                if (position != -1 && event.getBundle().getSerializable("childReply") != null
                        && event.getBundle().getSerializable("childReply") instanceof ReplyEntity) {
                    ReplyEntity childReply = (ReplyEntity) event.getBundle().getSerializable("childReply");
                    int childPostNum = mDatas.get(position).getChildPostCount() + 1;
                    mDatas.get(position).setChildPostCount(childPostNum);
                    if (mDatas.get(position).getChildReplyEntityList() != null && mDatas.get(position).getChildReplyEntityList().size() < 10) {
                        mDatas.get(position).getChildReplyEntityList().add(childReply);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {   //来自更多回复列表创建回复
                int childPostNum = mDatas.get(replyPosition).getChildPostCount() + 1;
                mDatas.get(replyPosition).setChildPostCount(childPostNum);
                adapter.notifyDataSetChanged();
            }
            getActivityInfo();
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof ReplyEntity) {
                ReplyEntity entity = (ReplyEntity) event.obj;
                if (mDatas.indexOf(entity) != -1) {
                    mDatas.set(mDatas.indexOf(entity), entity);
                    adapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = mDatas.get(replyPosition).getSupportNum();
                mDatas.get(replyPosition).setSupportNum(supportNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY) && event.obj != null
                && event.obj instanceof ReplyEntity) {
            discussNum--;
            tv_discussCount.setText(String.valueOf(discussNum));
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (mDatas.contains(entity)) {
                mDatas.remove(entity);
                adapter.notifyDataSetChanged();
                initData();
            }
            getActivityInfo();
        }
    }

    private void getActivityInfo() {
        String url;
        if (discussType != null && discussType.equals("course"))
            url = Constants.OUTRT_NET + "/" + activityId + "/study/m/activity/ncts/" + activityId + "/view";
        else
            url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/activity/wsts/" + activityId + "/view";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(AppActivityViewResult response) {
                if (response != null && response.getResponseData() != null) {
                    if (response.getResponseData().getmDiscussionUser() != null) {
                        mainNum = response.getResponseData().getmDiscussionUser().getMainPostNum();
                        subNum = response.getResponseData().getmDiscussionUser().getSubPostNum();
                    }
                    showTips();
                    showTopView();
                    if (response.getResponseData().getmActivityResult() != null && response.getResponseData().getmActivityResult().getmActivity() != null) {
                        CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                        Intent intent = new Intent();
                        intent.putExtra("activity", activity);
                        setResult(RESULT_OK, intent);
                    }
                }
            }
        }));
    }
}
