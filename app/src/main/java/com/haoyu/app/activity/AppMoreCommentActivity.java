package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppCommentAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
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
 * 创建日期：2017/1/16 on 10:14
 * 描述:更多评论
 * 作者:马飞奔 Administrator
 */
public class AppMoreCommentActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private AppMoreCommentActivity context = this;
    private List<CommentEntity> commentList = new ArrayList<>();
    private AppCommentAdapter commentAdapter;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_comment)
    TextView tv_comment;
    private String relationId, relationType;
    private int page = 1;
    private String mainUrl, orders = "CREATE_TIME.ASC";
    private boolean isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_appmainreply;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        commentAdapter = new AppCommentAdapter(context, commentList, getUserId());
        xRecyclerView.setAdapter(commentAdapter);
        xRecyclerView.setLoadingListener(context);
        mainUrl = Constants.OUTRT_NET + "/m/comment?relation.id=" + relationId + "&relation.type=" + relationType + "&orders=" + orders;
    }

    public void initData() {
        final String url = mainUrl + "&page=" + page;
        if (isRefresh || isLoadMore)
            loadingView.setVisibility(View.GONE);
        else
            loadingView.setVisibility(View.VISIBLE);
        addSubscription(Flowable.just(url).map(new Function<String, CommentListResult>() {
            @Override
            public CommentListResult apply(String url) throws Exception {
                return getComment(url);
            }
        }).map(new Function<CommentListResult, CommentListResult>() {
            @Override
            public CommentListResult apply(CommentListResult result) throws Exception {
                if (result != null && result.getResponseData() != null && result.getResponseData().getmComments().size() > 0) {
                    return getChildComment(result, result.getResponseData().getmComments());
                }
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<CommentListResult>() {
            @Override
            public void accept(CommentListResult response) throws Exception {
                onResponse(response);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                onError();
            }
        }));
    }

    /*获取主评论列表*/
    private CommentListResult getComment(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, CommentListResult.class);
    }

    /*通过主评论id获取子评论*/
    private CommentListResult getChildComment(CommentListResult result, List<CommentEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            String mainPostId = list.get(i).getId();
            String url = mainUrl + "&mainPostId=" + mainPostId;
            try {
                CommentListResult mResult = getComment(url);
                if (mResult != null && mResult.getResponseData() != null) {
                    List<CommentEntity> childList = mResult.getResponseData().getmComments();
                    result.getResponseData().getmComments().get(i).setChildList(childList);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    /*加载完成，更新页面*/
    private void onResponse(CommentListResult response) {
        loadingView.setVisibility(View.GONE);
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (tv_comment.getVisibility() != View.VISIBLE)
            tv_comment.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmComments().size() > 0) {
            updateUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
            if (isRefresh) {
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
        }
    }

    private void updateUI(List<CommentEntity> mDatas, Paginator paginator) {
        if (isRefresh) {
            commentList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        commentList.addAll(mDatas);
        commentAdapter.notifyDataSetChanged();
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

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(false);
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        commentAdapter.setCommentCallBack(new AppCommentAdapter.CommentCallBack() {
            @Override
            public void comment(int position, CommentEntity entity) {
                childPosition = position;
                showCommentDialog(true);
            }
        });
        commentAdapter.setSupportCallBack(new AppCommentAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });

        commentAdapter.setMoreReplyCallBack(new AppCommentAdapter.MoreReplyCallBack() {
            @Override
            public void moreReply(int position, CommentEntity entity) {
                registRxBus();
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreReplyActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("entity", entity);
                intent.putExtra("relationId", relationId);
                intent.putExtra("relationType", relationType);
                startActivity(intent);
            }
        });
        commentAdapter.setDeleteMainComment(new AppCommentAdapter.DeleteMainComment() {
            @Override
            public void deleteMainComment(String id, int position) {
                deleteComment(id, position);
            }
        });
    }

    /**
     * 创建观点(点赞)
     */
    private void createLike(final int position, final TextView tvLike) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = commentList.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                toast(context, "点赞失败");
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tvLike);
                    int count = commentList.get(position).getSupportNum() + 1;
                    commentList.get(position).setSupportNum(count);
                    tvLike.setText(String.valueOf(count));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    event.obj = commentList.get(position);
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
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
                    CommentEntity entity = commentList.remove(position);
                    commentAdapter.notifyDataSetChanged();
                    MessageEvent ev = new MessageEvent();
                    ev.action = Action.DELETE_MAIN_COMMENT;
                    ev.obj = entity;
                    RxBus.getDefault().post(ev);
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

    private int childPosition, replyPosition;

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

    private void sendMainComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", relationId);
        map.put("relation.type", "movement");
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            @Override
            public void onError(Request request, Exception e) {
                toastFullScreen("点评失败", false);
            }

            @Override
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                if (response != null && response.getResponseData() != null) {
                    CommentEntity entity = response.getResponseData();
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
                    if (xRecyclerView.isLoadingMoreEnabled()) {
                        toastFullScreen("点评成功", true);
                    } else {
                        commentList.add(entity);
                        commentAdapter.notifyDataSetChanged();
                    }
                }
            }
        }, map));
    }

    private void sendChildComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", relationId);
        map.put("relation.type", "movement");
        map.put("content", content);
        map.put("mainId", commentList.get(childPosition).getId());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            @Override
            public void onError(Request request, Exception e) {
                toastFullScreen("回复失败", false);
            }

            @Override
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                if (response != null && response.getResponseData() != null) {
                    CommentEntity entity = response.getResponseData();
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
                    int childNum = commentList.get(childPosition).getChildNum() + 1;
                    commentList.get(childPosition).setChildNum(childNum);
                    commentAdapter.notifyDataSetChanged();
                    if (commentList.get(childPosition).getChildList().size() >= 10) {
                        toastFullScreen("回复成功", true);
                    } else {
                        commentList.get(childPosition).getChildList().add(entity);
                        commentAdapter.notifyDataSetChanged();
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_CHILD_COMMENT;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mainComment", commentList.get(childPosition));
                    bundle.putSerializable("childComment", entity);
                    event.setBundle(bundle);
                    RxBus.getDefault().post(event);
                }
            }
        }, map));
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_CHILD_COMMENT) && event.obj != null && event.obj instanceof CommentEntity) {
            int childPostNum = commentList.get(replyPosition).getChildNum();
            commentList.get(replyPosition).setChildNum(childPostNum + 1);
            commentAdapter.notifyDataSetChanged();
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            int supportNum = commentList.get(replyPosition).getSupportNum();
            commentList.get(replyPosition).setSupportNum(supportNum + 1);
            commentAdapter.notifyDataSetChanged();
        } else if (event.action.equals(Action.DELETE_MAIN_COMMENT)) {
            commentList.remove(replyPosition);
            commentAdapter.notifyDataSetChanged();
        }
    }
}
