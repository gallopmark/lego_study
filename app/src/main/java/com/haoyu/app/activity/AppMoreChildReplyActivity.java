package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    @BindView(R.id.tv_count)
    TextView tv_count;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_comment)
    TextView tv_comment;    //点评布局
    private List<ReplyEntity> mDatas = new ArrayList<>();
    private AppDiscussionReplyAdapter adapter;
    private ReplyEntity mainEntity;
    private String discussType, activityId, workshopId, relationId, mainId, baseUrl, postUrl;
    private int page = 1;
    private String orders = "CREATE_TIME.ASC";
    private boolean canEdit, isLoadMore;
    private int childPostNum;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_appchildreply;
    }

    @Override
    public void initView() {
        toolBar.setTitle_text("评论详情");
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
        showData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new AppDiscussionReplyAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.setLoadingListener(context);
    }

    private void showData() {
        ImageView ic_user = findViewById(R.id.ic_user);
        TextView tv_userName = findViewById(R.id.tv_userName);
        TextView tv_content = findViewById(R.id.tv_content);
        TextView tv_createDate = findViewById(R.id.tv_createDate);
        LinearLayout ll_delete = findViewById(R.id.ll_delete);
        LinearLayout ll_like = findViewById(R.id.ll_like);
        final TextView tv_like = findViewById(R.id.tv_like);
        LinearLayout ll_comment = findViewById(R.id.ll_comment);
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, mainEntity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getRealName() != null) {
            tv_userName.setText(mainEntity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        if (mainEntity.getCreator() != null && mainEntity.getCreator().getId() != null
                && mainEntity.getCreator().getId().equals(getUserId())) {
            ll_delete.setVisibility(View.VISIBLE);
        } else {
            ll_delete.setVisibility(View.GONE);
        }
        tv_content.setText(mainEntity.getContent());
        tv_createDate.setText(TimeUtil.converTime(mainEntity.getCreateTime()));
        tv_like.setText(String.valueOf(mainEntity.getSupportNum()));
        childPostNum = mainEntity.getChildPostCount();
        showCount();
        ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit)
                    deletePost();
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
        ll_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLike(tv_like);
            }
        });
        ll_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit)
                    showCommentDialog();
                else
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
            }
        });
    }

    private void showCount() {
        tv_count.setText(String.valueOf(childPostNum));
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
                if (!isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    onNetWorkError(context);
                }
            }

            @Override
            public void onResponse(ReplyListResult response) {
                hideTipDialog();
                if (tv_comment.getVisibility() != View.VISIBLE) {
                    tv_comment.setVisibility(View.VISIBLE);
                }
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData().getmDiscussionPosts(), response.getResponseData().getPaginator());
                } else {
                    if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    }
                }
            }
        }));
    }

    private void updateUI(List<ReplyEntity> list, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        this.mDatas.addAll(list);
        adapter.notifyDataSetChanged();
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
        tv_comment.setOnClickListener(new View.OnClickListener() {
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
                    showCount();
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                    } else {
                        toastFullScreen("回复成功", true);
                    }
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
    }

    @Override
    public void onLoadMore() {
        isLoadMore = true;
        page += 1;
        initData();
    }
}
