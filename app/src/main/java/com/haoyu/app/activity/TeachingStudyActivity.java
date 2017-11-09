package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppCommentAdapter;
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.StickyScrollView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

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
 * 创建日期：2017/8/30 on 10:29
 * 描述:听课评课
 * 作者:马飞奔 Administrator
 */
public class TeachingStudyActivity extends BaseActivity implements View.OnClickListener {
    private TeachingStudyActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    StickyScrollView scrollView;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_study_title)
    TextView tv_study_title; //听课评课标题
    @BindView(R.id.tv_activity_type)
    TextView tv_activity_type;  //活动类型
    @BindView(R.id.tv_subject)
    TextView tv_subject; //年级学科
    @BindView(R.id.tv_lecture)
    TextView tv_lecture; //授课人
    @BindView(R.id.tv_bookversion)
    TextView tv_bookversion; //选用教材
    @BindView(R.id.ll_fileLayout)
    LinearLayout ll_fileLayout;   //文档
    @BindView(R.id.tv_fileLayout)
    TextView tv_fileLayout;
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.ll_video)
    LinearLayout ll_video;  //视频文件
    @BindView(R.id.tv_videoName)
    TextView tv_videoName;  //视频名称
    @BindView(R.id.tv_evaluation)
    TextView tv_evaluation;
    @BindView(R.id.tv_content)
    HtmlTextView tv_content;
    @BindView(R.id.ll_discussion)
    LinearLayout ll_discussion;
    @BindView(R.id.tv_discussCount)
    TextView tv_discussCount;   //评论总数
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
    @BindView(R.id.ll_comment)
    LinearLayout ll_comment;
    @BindView(R.id.ll_insert)
    LinearLayout ll_insert;
    @BindView(R.id.ll_detail)
    LinearLayout ll_detail;
    private boolean running;   //是否在培训时间内、活动是否进行中
    private TimePeriod timePeriod;
    private String workshopId, activityId, activityTitle;
    private AppActivityViewEntity.MLcecMobileEntity lcecEntity;
    private int discussNum;//总回复数
    private AppCommentAdapter adapter;
    private List<CommentEntity> mComments = new ArrayList<>();
    private int replyPosition, childPosition;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_study;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        workshopId = getIntent().getStringExtra("workshopId");
        activityId = getIntent().getStringExtra("activityId");
        activityTitle = getIntent().getStringExtra("activityTitle");
        lcecEntity = (AppActivityViewEntity.MLcecMobileEntity) getIntent().getSerializableExtra("mlcec");
        setSupportToolbar();
        showData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AppCommentAdapter(context, mComments, getUserId());
        recyclerView.setAdapter(adapter);
        registRxBus();
    }

    private void setSupportToolbar() {
        toolBar.setTitle_text("听课评课");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    private void showData() {
        if (running) {
            if (timePeriod != null && timePeriod.getMinutes() > 0) {
                String time = "离活动结束还剩：" + TimeUtil.dateDiff(timePeriod.getMinutes());
                tv_time.setText(Html.fromHtml(time));
            } else {
                if (timePeriod != null && timePeriod.getState() != null) {
                    tv_time.setText("活动" + timePeriod.getState());
                } else {
                    tv_time.setText("活动进行中");
                }
            }
        } else {
            tv_time.setText("活动已结束");
        }
        if (lcecEntity.isHasSubmitEvaluate()) {
            ll_insert.setVisibility(View.GONE);
            ll_detail.setVisibility(View.VISIBLE);
        } else {
            ll_insert.setVisibility(View.VISIBLE);
            ll_detail.setVisibility(View.GONE);
        }
        if (lcecEntity.getTitle() != null)
            tv_study_title.setText(Html.fromHtml(lcecEntity.getTitle()));
        if (lcecEntity.getContent() != null && lcecEntity.getContent().trim().length() > 0) {
            final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
            final Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
            zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
            shouqi.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
            tv_content.setHtml(lcecEntity.getContent(), new HtmlHttpImageGetter(tv_content, Constants.REFERER));
            tv_content.setVisibility(View.VISIBLE);
            tv_evaluation.setCompoundDrawables(null, null, shouqi, null);
            tv_evaluation.setOnClickListener(new View.OnClickListener() {
                private boolean isExpand = false;

                @Override
                public void onClick(View view) {
                    if (isExpand) {
                        tv_content.setVisibility(View.VISIBLE);
                        tv_evaluation.setCompoundDrawables(null, null, shouqi, null);
                        isExpand = false;
                    } else {
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.smoothScrollTo(0, tv_evaluation.getTop());
                            }
                        }, 10);
                        tv_content.setVisibility(View.GONE);
                        tv_evaluation.setCompoundDrawables(null, null, zhankai, null);
                        isExpand = true;
                    }
                }
            });
        }
        String activityType = "活动类型：";
        if (lcecEntity.getType() != null && lcecEntity.getType().equals("offLine"))
            tv_activity_type.setText(activityType + "现场评课");
        else if (lcecEntity.getType() != null && lcecEntity.getType().equals("onLine"))
            tv_activity_type.setText(activityType + "实录评课");
        else
            tv_activity_type.setText(activityType + "未知");
        if (lcecEntity.getTextbook() != null)
            tv_bookversion.setText("选用教材：" + lcecEntity.getTextbook());
        else
            tv_bookversion.setText("选用教材：未知");
        if (lcecEntity.getTeacher() != null && lcecEntity.getTeacher().getRealName() != null)
            tv_lecture.setText("授课人：" + lcecEntity.getTeacher().getRealName());
        else
            tv_lecture.setText("授课人：未知");
        String ssubject = "年级学科：";
        if (lcecEntity.getStage() != null && lcecEntity.getSubject() != null)
            tv_subject.setText(ssubject + lcecEntity.getStage() + "\u3000" + lcecEntity.getSubject());
        else if (lcecEntity.getStage() != null)
            tv_subject.setText(ssubject + lcecEntity.getStage());
        else if (lcecEntity.getSubject() != null)
            tv_subject.setText(ssubject + lcecEntity.getSubject());
        else
            tv_subject.setText(ssubject + "未知");
        if (lcecEntity.getmFileInfos() != null && lcecEntity.getmFileInfos().size() > 0) {
            ll_fileLayout.setVisibility(View.VISIBLE);
            final List<MFileInfo> mDatas = lcecEntity.getmFileInfos();
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            rv_file.setLayoutManager(layoutManager);
            MFileInfoAdapter adapter = new MFileInfoAdapter(mDatas);
            rv_file.setAdapter(adapter);
            adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                    MFileInfo fileInfo = mDatas.get(position);
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
                    startActivity(intent);
                }
            });
            final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
            final Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
            zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
            shouqi.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
            tv_fileLayout.setCompoundDrawables(null, null, shouqi, null);
            tv_fileLayout.setOnClickListener(new View.OnClickListener() {
                private boolean isExpand = false;

                @Override
                public void onClick(View view) {
                    if (isExpand) {
                        rv_file.setVisibility(View.VISIBLE);
                        tv_fileLayout.setCompoundDrawables(null, null, shouqi, null);
                        isExpand = false;
                    } else {
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.smoothScrollTo(0, tv_fileLayout.getTop());
                            }
                        }, 10);
                        rv_file.setVisibility(View.GONE);
                        tv_fileLayout.setCompoundDrawables(null, null, zhankai, null);
                        isExpand = true;
                    }
                }
            });
        }
        if (lcecEntity.getmVideo() != null) {
            ll_video.setVisibility(View.VISIBLE);
            ll_video.setOnClickListener(context);
            tv_videoName.setText(lcecEntity.getmVideo().getFileName());
        }
    }

    @Override
    public void initData() {
        tv_more_reply.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        String url = Constants.OUTRT_NET + "/m/comment?relation.id=" + activityId + "&relation.type=lcec&orders=CREATE_TIME.ASC&limit=5";
        addSubscription(Flowable.just(url).map(new Function<String, CommentListResult>() {
            @Override
            public CommentListResult apply(String url) throws Exception {
                return get(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CommentListResult>() {
                    @Override
                    public void accept(CommentListResult response) throws Exception {
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

    private CommentListResult get(String url) throws Exception {
        Gson gson = new GsonBuilder().create();
        String listStr = OkHttpClientManager.getAsString(context, url);
        CommentListResult commentListResult = gson.fromJson(listStr, CommentListResult.class);
        if (commentListResult != null && commentListResult.getResponseData() != null &&
                commentListResult.getResponseData().getmComments() != null
                && commentListResult.getResponseData().getmComments().size() > 0) {
            for (int i = 0; i < commentListResult.getResponseData().getmComments().size(); i++) {
                String mainPostId = commentListResult.getResponseData().getmComments().get(i).getId();
                url = Constants.OUTRT_NET + "/m/comment?relation.id=" + activityId + "&relation.type=lcec"
                        + "&mainId=" + mainPostId + "&orders=CREATE_TIME.ASC";
                try {
                    String Jsonarr = OkHttpClientManager.getAsString(context, url);
                    CommentListResult childReplyListResult = gson.fromJson(Jsonarr, CommentListResult.class);
                    if (childReplyListResult.getResponseData() != null) {
                        commentListResult.getResponseData().getmComments().get(i).
                                setChildList(childReplyListResult.getResponseData().getmComments());
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return commentListResult;
    }

    private void updateUI(CommentListResult response) {
        mComments.clear();
        ll_discussion.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmComments() != null
                && response.getResponseData().getmComments().size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            mComments.addAll(response.getResponseData().getmComments());
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

    @Override
    public void setListener() {
        ll_insert.setOnClickListener(context);
        tv_comment.setOnClickListener(context);
        ll_comment.setOnClickListener(context);
        ll_detail.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        tv_more_reply.setOnClickListener(context);
        adapter.setCommentCallBack(new AppCommentAdapter.CommentCallBack() {
            @Override
            public void comment(int position, CommentEntity entity) {
                if (running) {
                    childPosition = position;
                    showCommentDialog(true);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
            }
        });

        adapter.setSupportCallBack(new AppCommentAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });
        adapter.setMoreReplyCallBack(new AppCommentAdapter.MoreReplyCallBack() {
            @Override
            public void moreReply(int position, CommentEntity entity) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("relationType", "lcec");
                intent.putExtra("relationId", activityId);
                startActivity(intent);
            }
        });
        adapter.setDeleteMainComment(new AppCommentAdapter.DeleteMainComment() {
            @Override
            public void deleteMainComment(String id, int position) {
                if (running) {
                    deleteComment(id, position);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_insert:
                //填写评课表
                if (running) {
                    intent.setClass(context, TeachingStudyFillActivity.class);
                    intent.putExtra("workshopId", workshopId);
                    intent.putExtra("leceId", lcecEntity.getId());
                    startActivity(intent);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
                return;
            case R.id.tv_comment:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
                return;
            case R.id.ll_comment:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
                return;
            case R.id.tv_more_reply:
                intent.setClass(context, AppMoreCommentActivity.class);
                intent.putExtra("relationId", activityId);
                intent.putExtra("relationType", "lcec");
                startActivity(intent);
                return;
            case R.id.ll_detail:
                //查看听课评课明细
                intent.setClass(context, TeachingStudyResultDetailActiivty.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("leceId", lcecEntity.getId());
                startActivity(intent);
                return;
            case R.id.ll_video:
                if (NetStatusUtil.isConnected(context)) {
                    if (NetStatusUtil.isWifi(context)) {
                        intent.setClass(context, VideoPlayerActivity.class);
                        intent.putExtra("activityTitle", activityTitle);
                        intent.putExtra("videoUrl", lcecEntity.getmVideo().getUrl());
                        startActivity(intent);
                    } else {
                        MaterialDialog dialog = new MaterialDialog(context);
                        dialog.setTitle("网络提醒");
                        dialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
                        dialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setPositiveButton("取消", null);
                        dialog.show();
                    }
                } else {
                    toast(context, "当前网络不稳定，请检查网络设置！");
                }
                return;
        }
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildComment(content);
                } else {
                    sendMainComment(content);
                }
            }
        });
    }

    /*发送主评论*/
    private void sendMainComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", activityId);
        map.put("relation.type", "lcec");
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
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
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    empty_comment.setVisibility(View.GONE);
                    CommentEntity entity = response.getResponseData();
                    entity.setCreator(getCreator(entity.getCreator()));
                    discussNum++;
                    tv_discussCount.setText(String.valueOf(discussNum));
                    if (mComments.size() < 5) {
                        mComments.add(response.getResponseData());
                        adapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /*发送子评论*/
    private void sendChildComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", activityId);
        map.put("relation.type", "lcec");
        map.put("content", content);
        map.put("mainId", mComments.get(childPosition).getId());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    CommentEntity entity = response.getResponseData();
                    entity.setCreator(getCreator(entity.getCreator()));
                    int childNum = mComments.get(childPosition).getChildNum() + 1;
                    mComments.get(childPosition).setChildNum(childNum);
                    adapter.notifyDataSetChanged();
                    if (mComments.get(childPosition).getChildList().size() >= 10) {
                        toastFullScreen("评论成功", true);
                    } else {
                        mComments.get(childPosition).getChildList().add(response.getResponseData());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    private MobileUser getCreator(MobileUser creator) {
        if (creator == null) {
            creator = new MobileUser();
            creator.setId(getUserId());
            creator.setAvatar(getAvatar());
            creator.setRealName(getRealName());
        } else {
            if (creator.getId() == null || (creator.getId() != null && creator.getId().toLowerCase().equals("null")))
                creator.setId(getUserId());
            if (creator.getAvatar() == null || (creator.getAvatar() != null && creator.getAvatar().toLowerCase().equals("null")))
                creator.setAvatar(getAvatar());
            if (creator.getRealName() == null || (creator.getRealName() != null && creator.getRealName().toLowerCase().equals("null")))
                creator.setRealName(getRealName());
        }
        return creator;
    }

    /**
     * 创建观点(点赞)
     */
    private void createLike(final int position, final TextView tvLike) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = mComments.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                toast(context, "点赞失败");
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tvLike);
                    int count = mComments.get(position).getSupportNum() + 1;
                    mComments.get(position).setSupportNum(count);
                    adapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map);
    }

    /**
     * 删除评论
     *
     * @param id
     * @param position
     */
    private void deleteComment(String id, final int position) {
        String url = Constants.OUTRT_NET + "/m/comment/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mComments.remove(position);
                    adapter.notifyDataSetChanged();
                    if (mComments.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        empty_comment.setVisibility(View.VISIBLE);
                    }
                    initData();
                }
            }
        }, map);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_CHILD_COMMENT)) {
            if (event.getBundle() != null && event.getBundle().getSerializable("mainComment") != null
                    && event.getBundle().getSerializable("mainComment") instanceof CommentEntity) {
                CommentEntity mainComment = (CommentEntity) event.getBundle().getSerializable("mainComment");
                int position = mComments.indexOf(mainComment);
                if (position != -1 && event.getBundle().getSerializable("childComment") != null
                        && event.getBundle().getSerializable("childComment") instanceof CommentEntity) {
                    int childPostNum = mComments.get(position).getChildNum();
                    mComments.get(position).setChildNum(childPostNum + 1);
                    CommentEntity childComment = (CommentEntity) event.getBundle().getSerializable("childComment");
                    if (mComments.get(position).getChildList() != null && mComments.get(position).getChildList().size() < 10) {
                        mComments.get(position).getChildList().add(childComment);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                int childPostNum = mComments.get(replyPosition).getChildNum();
                mComments.get(replyPosition).setChildNum(childPostNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof CommentEntity) {
                CommentEntity entity = (CommentEntity) event.obj;
                if (mComments.indexOf(entity) != -1) {
                    mComments.set(mComments.indexOf(entity), entity);
                    adapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = mComments.get(replyPosition).getSupportNum();
                mComments.get(replyPosition).setSupportNum(supportNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_COMMENT) && event.obj != null && event.obj instanceof CommentEntity) {       //在更多评论列表删除评论
            CommentEntity entity = (CommentEntity) event.getObj();
            if (mComments.contains(entity)) {
                mComments.remove(entity);
                adapter.notifyDataSetChanged();
                initData();
            }
        }
    }
}
