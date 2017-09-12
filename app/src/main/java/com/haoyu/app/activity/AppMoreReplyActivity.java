package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.adapter.AppMoreReplyAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/16 on 16:05
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppMoreReplyActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private AppMoreReplyActivity context = this;
    private List<CommentEntity> commentList = new ArrayList<>();
    private AppMoreReplyAdapter commentAdapter;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private View headerView;
    private TextView tv_comment;
    private TextView tv_content;
    private TextView tv_createDate;
    private TextView tv_like;
    private TextView tv_userName;
    private ImageView userIco;
    private View bodyDelete;
    private View bodyLike;
    private View bodyComment;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.commentView)
    View commentView;
    private CommentEntity commentEntity;
    private String relationId, relationType, mainId;
    private int page = 1;
    private String orders = "CREATE_TIME.DESC";
    private int limit = 20;
    private boolean isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_more_reply;
    }

    @Override
    public void initView() {
        commentEntity = (CommentEntity) getIntent().getSerializableExtra("entity");
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        headerView = getLayoutInflater().inflate(
                R.layout.app_more_reply_headview, null);
        userIco = getView(headerView, R.id.ic_user);
        tv_userName = getView(headerView, R.id.tv_userName);
        tv_content = getView(headerView, R.id.tv_content);
        tv_createDate = getView(headerView, R.id.tv_createDate);
        tv_like = getView(headerView, R.id.tv_like);
        tv_comment = getView(headerView, R.id.tv_comment);
        bodyDelete = getView(headerView, R.id.bodyDelete);
        bodyLike = getView(headerView, R.id.bodyLike);
        bodyComment = getView(headerView, R.id.bodyComment);
        xRecyclerView.addHeaderView(headerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        commentAdapter = new AppMoreReplyAdapter(context, commentList);
        xRecyclerView.setAdapter(commentAdapter);
        xRecyclerView.setLoadingListener(context);
        showData();
    }

    private void showData() {
        if (commentEntity != null) {
            mainId = commentEntity.getId();
            if (commentEntity.getCreator() != null && commentEntity.getCreator().getAvatar() != null) {
                GlideImgManager.loadCircleImage(context, commentEntity.getCreator().getAvatar(),
                        R.drawable.user_default, R.drawable.user_default, userIco);
            } else {
                userIco.setImageResource(R.drawable.user_default);
            }
            if (commentEntity.getCreator() != null && commentEntity.getCreator().getRealName() != null) {
                tv_userName.setText(commentEntity.getCreator().getRealName());
            } else {
                tv_userName.setText("匿名用户");
            }
            if (commentEntity.getCreator() != null && commentEntity.getCreator().getId() != null
                    && commentEntity.getCreator().getId().equals(getUserId())) {
                bodyDelete.setVisibility(View.VISIBLE);
            }
            tv_content.setText(commentEntity.getContent());
            tv_createDate.setText(TimeUtil.converTime(commentEntity.getCreateTime()));
            tv_like.setText(String.valueOf(commentEntity.getSupportNum()));
            tv_comment.setText(String.valueOf(commentEntity.getChildNum()));
        }
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/comment?relation.id=" + relationId + "&relation.type=" + relationType
                + "&mainId=" + mainId + "&orders=" + orders + "&limit=" + limit + "&page=" + page;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CommentListResult>() {
            @Override
            public void onBefore(Request request) {
                if (!isRefresh && !isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                }
            }

            @Override
            public void onResponse(CommentListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmComments() != null
                        && response.getResponseData().getmComments().size() > 0) {
                    updateUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    }
                }
            }
        });
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

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.commentView:
                        showCommentDialog();
                        break;
                    case R.id.bodyDelete:
                        deleteComment();
                        break;
                    case R.id.bodyLike:
                        createLike(tv_like);
                        break;
                    case R.id.bodyComment:
                        showCommentDialog();
                        break;
                }
            }
        };
        commentView.setOnClickListener(listener);
        bodyDelete.setOnClickListener(listener);
        bodyLike.setOnClickListener(listener);
        bodyComment.setOnClickListener(listener);
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

    /**
     * 创建观点(点赞)
     */
    private void createLike(final TextView tvLike) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = mainId;
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
                    int supportNum = commentEntity.getSupportNum() + 1;
                    commentEntity.setSupportNum(supportNum);
                    tvLike.setText(String.valueOf(supportNum));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    event.obj = commentEntity;
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map);
    }

    private void showCommentDialog() {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                sendChildComment(content);
            }
        });
    }

    private void sendChildComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", relationId);
        map.put("relation.type", relationType);
        map.put("content", content);
        map.put("mainId", mainId);
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            @Override
            public void onError(Request request, Exception e) {

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
                        toast(context, "回复成功");
                    } else {
                        commentList.add(entity);
                        commentAdapter.notifyDataSetChanged();
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_CHILD_COMMENT;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                }
            }
        }, map);
    }

    /**
     * 删除评论
     */
    private void deleteComment() {
        String url = Constants.OUTRT_NET + "/m/comment/" + mainId;
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
                toast(context, "与服务器连接失败");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MAIN_COMMENT;
                    event.obj = commentEntity;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map);
    }
}
