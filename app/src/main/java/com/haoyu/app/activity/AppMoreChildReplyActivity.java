package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.adapter.AppDiscussionReplyAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
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
 * 创建日期：2017/3/1 on 16:16
 * 描述: 评论回复列表
 * 作者:马飞奔 Administrator
 */
public class AppMoreChildReplyActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private AppMoreChildReplyActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private View headView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.commentView)
    View commentView;    //点评布局
    private List<ReplyEntity> replyList = new ArrayList<>();
    private AppDiscussionReplyAdapter replyAdapter;
    private ReplyEntity mainEntity;
    private String discussType, activityId, workshopId, relationId, mainId, baseUrl, postUrl;
    private int page = 1;
    private String orders = "CREATE_TIME.ASC";
    private boolean canEdit, isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_more_child_reply;
    }

    @Override
    public void initView() {
        mainEntity = (ReplyEntity) getIntent().getSerializableExtra("entity");
        discussType = getIntent().getStringExtra("discussType");
        activityId = getIntent().getStringExtra("activityId");
        workshopId = getIntent().getStringExtra("workshopId");
        relationId = getIntent().getStringExtra("relationId");
        mainId = mainEntity.getId();
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
        headView = getLayoutInflater().inflate(R.layout.dis_reply_list_item, null);
        showData(headView);
        xRecyclerView.addHeaderView(headView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        replyAdapter = new AppDiscussionReplyAdapter(context, replyList);
        xRecyclerView.setAdapter(replyAdapter);
        xRecyclerView.setLoadingListener(context);
    }

    private TextView tv_comment;
    private int childPostNum;

    private void showData(View headView) {
        ImageView ic_user = getView(headView, R.id.ic_user);
        TextView tv_userName = getView(headView, R.id.tv_userName);
        TextView tv_content = getView(headView, R.id.tv_content);
        TextView tv_createDate = getView(headView, R.id.tv_createDate);
        View bodyDelete = getView(headView, R.id.bodyDelete);
        View bodyLike = getView(headView, R.id.bodyLike);
        final TextView tv_like = getView(headView, R.id.tv_like);
        View bodyCommnet = getView(headView, R.id.bodyComment);
        tv_comment = getView(headView, R.id.tv_comment);
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, mainEntity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getRealName() != null) {
            tv_userName.setText(mainEntity.getCreator().getRealName());
        } else {
            tv_userName.setText("匿名用户");
        }
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getId() != null
                && mainEntity.getCreator().getId().equals(getUserId())) {
            bodyDelete.setVisibility(View.VISIBLE);
        } else {
            bodyDelete.setVisibility(View.GONE);
        }
        tv_content.setText(mainEntity.getContent());
        tv_createDate.setText(TimeUtil.converTime(mainEntity.getCreateTime()));
        tv_like.setText(String.valueOf(mainEntity.getSupportNum()));
        childPostNum = mainEntity.getChildPostCount();
        tv_comment.setText(String.valueOf(mainEntity.getChildPostCount()));
        bodyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit)
                    deletePost();
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
        bodyLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLike(tv_like);
            }
        });
        bodyCommnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit)
                    showCommentDialog();
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
    }

    private void deletePost() {
        String url = postUrl + "/" + mainId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", relationId);
        map.put("mainPostId", mainId);
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
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MAIN_REPLY;
                    event.obj = mainEntity;
                    RxBus.getDefault().post(event);
                    finish();
                    toastFullScreen("删除成功", true);
                } else {
                    toastFullScreen("删除失败", false);
                }
            }
        }, map));
    }


    /* 创建观点（点赞） */

    private void createLike(final TextView tv_like) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", mainId);
        map.put("relation.type", "discussion");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null &&
                        response.getResponseCode().equals("00")) {
                    int supportNum = mainEntity.getSupportNum() + 1;
                    tv_like.setText(String.valueOf(supportNum));
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 16);
                    goodView.show(tv_like);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    event.arg1 = supportNum;
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    public void initData() {
        String url = baseUrl + "?discussionUser.discussionRelation.id=" + relationId
                + "&mainPostId=" + mainId + "&page=" + page + "&limit=20" + "&orders=" + orders;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyListResult>() {
            @Override
            public void onBefore(Request request) {
                if (!isRefresh && !isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(ReplyListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmDiscussionPosts() != null) {
                    updateUI(response.getResponseData().getmDiscussionPosts(), response.getResponseData().getPaginator());
                }
            }
        }));
    }

    private void updateUI(List<ReplyEntity> mDatas, Paginator paginator) {
        if (isRefresh) {
            replyList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        replyList.addAll(mDatas);
        replyAdapter.notifyDataSetChanged();
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
        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canEdit)
                    showCommentDialog();
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
    }

    private void showCommentDialog() {
        CommentDialog dialog = new CommentDialog(context, "请输入回复内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                sendChildReply(content);
            }
        });
    }

    /*创建子回复*/
    private void sendChildReply(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", mainId);
        map.put("discussionUser.discussionRelation.id", relationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {

            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
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
                    childPostNum++;
                    tv_comment.setText(String.valueOf(childPostNum));
                    replyList.add(entity);
                    replyAdapter.notifyDataSetChanged();
                    toastFullScreen("回复成功", true);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_CHILD_REPLY;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                } else {
                    toastFullScreen("回复失败", false);
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
}
