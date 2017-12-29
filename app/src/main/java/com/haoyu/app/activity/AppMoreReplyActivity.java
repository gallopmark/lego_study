package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_count)
    TextView tv_count;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_comment)
    TextView tv_comment;    //点评布局
    private List<CommentEntity> mDatas = new ArrayList<>();
    private AppMoreReplyAdapter adapter;
    private CommentEntity entity;
    private String relationId, relationType, mainId;
    private int page = 1;
    private String orders = "CREATE_TIME.DESC";
    private int limit = 20;
    private boolean isLoadMore;
    private int childPostNum;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_appchildreply;
    }

    @Override
    public void initView() {
        toolBar.setTitle_text("评论详情");
        entity = (CommentEntity) getIntent().getSerializableExtra("entity");
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        mainId = entity.getId();
        showData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        adapter = new AppMoreReplyAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
        xRecyclerView.setPullRefreshEnabled(false);
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
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(getUserId())) {
            ll_delete.setVisibility(View.VISIBLE);
        } else {
            ll_delete.setVisibility(View.GONE);
        }
        tv_content.setText(entity.getContent());
        tv_createDate.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_like.setText(String.valueOf(entity.getSupportNum()));
        childPostNum = entity.getChildNum();
        showCount();
        ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment();
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
                showCommentDialog();
            }
        });
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(getUserId())) {
            ll_delete.setVisibility(View.VISIBLE);
        }
        tv_content.setText(entity.getContent());
        tv_createDate.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_like.setText(String.valueOf(entity.getSupportNum()));
    }

    private void showCount() {
        tv_count.setText(String.valueOf(childPostNum));
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/comment?relation.id=" + relationId + "&relation.type=" + relationType
                + "&mainId=" + mainId + "&orders=" + orders + "&limit=" + limit + "&page=" + page;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CommentListResult>() {
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
            public void onResponse(CommentListResult response) {
                hideTipDialog();
                if (tv_comment.getVisibility() != View.VISIBLE) {
                    tv_comment.setVisibility(View.VISIBLE);
                }
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
                } else {
                    if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    }
                }
            }
        }));
    }

    private void updateUI(List<CommentEntity> mDatas, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        this.mDatas.addAll(mDatas);
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
                showCommentDialog();
            }
        });
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
                    int supportNum = entity.getSupportNum() + 1;
                    entity.setSupportNum(supportNum);
                    tvLike.setText(String.valueOf(supportNum));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
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
                    childPostNum++;
                    showCount();
                    if (xRecyclerView.isLoadingMoreEnabled()) {
                        toast(context, "回复成功");
                    } else {
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
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
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }
}
