package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

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
 * 创建日期：2017/3/1 on 15:19
 * 描述: 教研研说（话题）评论列表页面
 * 作者:马飞奔 Administrator
 */
public class AppMoreMainReplyActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private AppMoreMainReplyActivity context = this;
    private List<ReplyEntity> replyList = new ArrayList<>();
    private AppDiscussionAdapter replyAdapter;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_comment)
    TextView tv_comment;    //点评布局
    private String discussType, activityId, workshopId, relationId, baseUrl, postUrl, mainUrl;
    private int page = 1;
    private String orders = "CREATE_TIME.ASC";
    private boolean canEdit, isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_appmainreply;
    }

    @Override
    public void initView() {
        discussType = getIntent().getStringExtra("discussType");
        activityId = getIntent().getStringExtra("activityId");
        workshopId = getIntent().getStringExtra("workshopId");
        String type = getIntent().getStringExtra("type");
        relationId = getIntent().getStringExtra("relationId");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        replyAdapter = new AppDiscussionAdapter(context, replyList, getUserId());
        xRecyclerView.setAdapter(replyAdapter);
        xRecyclerView.setLoadingListener(context);
        if (type.equals("comment")) {
            toolBar.setTitle_text("评论详情");
            tv_comment.setHint("输入评论内容");
        } else if (type.equals("advise")) {
            toolBar.setTitle_text("所有建议");
            tv_comment.setHint("提一些建议，帮助完善创课");
        }
        if (discussType != null && discussType.equals("course")) {
            canEdit = getIntent().getBooleanExtra("canEdit", false);
            baseUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/" + activityId + "unique_uid_" + getUserId() + "/m/discussion/post";
        } else if (discussType != null && discussType.equals("workshop")) {
            canEdit = getIntent().getBooleanExtra("canEdit", false);
            baseUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/student_" + workshopId + "unique_uid_" + getUserId() + "/m/discussion/post";
        } else {
            canEdit = true;
            baseUrl = postUrl = Constants.OUTRT_NET + "/m/discussion/post";
        }
        mainUrl = baseUrl + "?discussionUser.discussionRelation.id=" + relationId + "&orders=" + orders;
        registRxBus();
    }

    public void initData() {
        if (isRefresh || isLoadMore)
            loadingView.setVisibility(View.GONE);
        else
            loadingView.setVisibility(View.VISIBLE);
        final String url = mainUrl + "&page=" + page;
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String url) throws Exception {
                return getReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                if (result != null && result.getResponseData() != null && result.getResponseData().getmDiscussionPosts().size() > 0) {
                    return getChildReply(result, result.getResponseData().getmDiscussionPosts());
                }
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ReplyListResult>() {
            @Override
            public void accept(ReplyListResult result) throws Exception {
                onResponse(result);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                onError();
            }
        }));
    }

    /*获取主回复*/
    private ReplyListResult getReply(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, ReplyListResult.class);
    }

    /*通过主回复id获取子回复*/
    private ReplyListResult getChildReply(ReplyListResult result, List<ReplyEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            String mainPostId = list.get(i).getId();
            String url = mainUrl + "&mainPostId=" + mainPostId;
            try {
                ReplyListResult mResult = getReply(url);
                if (mResult != null && mResult.getResponseData() != null) {
                    List<ReplyEntity> childList = mResult.getResponseData().getmDiscussionPosts();
                    result.getResponseData().getmDiscussionPosts().get(i).setChildReplyEntityList(childList);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    /*加载完成，更新页面*/
    private void onResponse(ReplyListResult response) {
        loadingView.setVisibility(View.GONE);
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (tv_comment.getVisibility() != View.VISIBLE)
            tv_comment.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmDiscussionPosts().size() > 0) {
            updateUI(response.getResponseData().getmDiscussionPosts(), response.getResponseData().getPaginator());
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
            if (isRefresh) {
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
        }
    }

    /*显示评论列表*/
    private void updateUI(List<ReplyEntity> mainList, Paginator paginator) {
        if (isRefresh) {
            replyList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        replyList.addAll(mainList);
        replyAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    /*加载失败*/
    private void onError() {
        loadingView.setVisibility(View.GONE);
        if (isRefresh)
            xRecyclerView.refreshComplete(false);
        else if (isLoadMore) {
            page -= 1;
            xRecyclerView.loadMoreComplete(false);
        } else
            loadFailView.setVisibility(View.VISIBLE);
    }

    private int childPosition, replyPosition;

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
        tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canEdit)
                    showCommentDialog(false);
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
        replyAdapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, final int position, final ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, final int position) {
                childPosition = position;
                if (canEdit)
                    showCommentDialog(true);
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });

        replyAdapter.setSupportCallBack(new AppDiscussionAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });

        replyAdapter.setMoreReplyCallBack(new AppDiscussionAdapter.MoreReplyCallBack() {   //查看更多回复
            @Override
            public void callBack(ReplyEntity entity, int position) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreChildReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("discussType", discussType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("relationId", relationId);
                intent.putExtra("canEdit", canEdit);
                startActivity(intent);
            }
        });

        replyAdapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                if (canEdit)
                    deleteReply(id, position);
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
    }

    /**
     * 创建评论列表点赞观点(点赞)
     *
     * @param position
     * @param tv_like
     */
    private void createLike(final int position, final TextView tv_like) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = replyList.get(position).getId();
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
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tv_like);
                    int count = replyList.get(position).getSupportNum() + 1;
                    replyList.get(position).setSupportNum(count);
                    replyAdapter.notifyDataSetChanged();
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    event.obj = replyList.get(position);
                    RxBus.getDefault().post(event);
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
        map.put("discussionUser.discussionRelation.id", relationId);
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent ev = new MessageEvent();
                    ev.action = Action.DELETE_MAIN_REPLY;
                    ev.obj = replyList.get(position);
                    RxBus.getDefault().post(ev);
                }
            }
        }, map));
    }

    private void showCommentDialog(final boolean sendChild) {
        String hint;
        if (sendChild) {
            hint = "输入回复内容";
        } else {
            hint = tv_comment.getHint().toString();
        }
        CommentDialog dialog = new CommentDialog(context, hint);
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
    private void sendChildReply(final int position, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", replyList.get(position).getId());
        map.put("discussionUser.discussionRelation.id", relationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            public void onError(Request request, Exception exception) {
            }

            public void onResponse(ReplyResult response) {
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
                    int childPostCount = replyList.get(position).getChildPostCount() + 1;
                    replyList.get(position).setChildPostCount(childPostCount);
                    if (replyList.get(position).getChildReplyEntityList() != null && replyList.get(position).getChildReplyEntityList().size() < 10) {
                        replyList.get(position).getChildReplyEntityList().add(entity);
                    } else {
                        toastFullScreen("回复成功", true);
                    }
                    replyAdapter.notifyDataSetChanged();
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_CHILD_REPLY;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mainReply", replyList.get(position));
                    bundle.putSerializable("childReply", entity);
                    event.setBundle(bundle);
                    RxBus.getDefault().post(event);
                }
            }
        }, map));
    }

    /*创建主回复*/
    private void sendMainReply(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("discussionUser.discussionRelation.id", relationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(ReplyResult response) {
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
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        replyList.add(entity);
                        replyAdapter.notifyDataSetChanged();
                    } else {
                        toastFullScreen("评论成功", true);
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_MAIN_REPLY;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                }
            }
        }, map));
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_CHILD_REPLY)) {
            int childPostNum = replyList.get(replyPosition).getChildPostCount();
            replyList.get(replyPosition).setChildPostCount(childPostNum + 1);
            replyAdapter.notifyDataSetChanged();
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            int supportNum = replyList.get(replyPosition).getSupportNum();
            replyList.get(replyPosition).setSupportNum(supportNum + 1);
            replyAdapter.notifyDataSetChanged();
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            ReplyEntity entity = (ReplyEntity) event.obj;
            replyList.remove(entity);
            replyAdapter.notifyDataSetChanged();
        }
    }
}
