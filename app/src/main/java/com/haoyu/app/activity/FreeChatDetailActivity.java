package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.FreeChatDetailAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
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
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RippleView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

import static com.haoyu.app.lego.student.R.id.tv_discussion_content;

public class FreeChatDetailActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private FreeChatDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.xRecyclerView)
    XRecyclerView mXRecyclerView;
    @BindView(R.id.commentView)
    TextView commentView;
    private TextView tv_commentCount;
    private TextView tv_empty;
    private View headView;
    private CommentEntity commentEntity;
    private boolean isRefresh = false;
    private boolean isLoadMore = false;
    private String relationId, role;
    private int page = 1;
    private List<CommentEntity> mDatas = new ArrayList<>();
    private FreeChatDetailAdapter mAdaper;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_freechat_detail;
    }

    @Override
    public void initView() {
        role = getIntent().getStringExtra("role");
        relationId = getIntent().getStringExtra("relationId");
        commentEntity = (CommentEntity) getIntent().getSerializableExtra("entity");
        if (commentEntity == null)
            finish();
        if (commentEntity.getCreator() != null && commentEntity.getCreator().getId() != null
                && commentEntity.getCreator().getId().equals(getUserId()))
            toolBar.setShow_right_button(true);
        else
            toolBar.setShow_right_button(false);
        headView = getLayoutInflater().inflate(R.layout.freechat_header, null);
        initHead();
        mXRecyclerView.addHeaderView(headView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mXRecyclerView.setLayoutManager(layoutManager);
        mAdaper = new FreeChatDetailAdapter(context, mDatas, getUserId());
        mXRecyclerView.setAdapter(mAdaper);
        mXRecyclerView.setLoadingListener(context);
    }

    private void initHead() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headView.setLayoutParams(params);
        HtmlTextView tv_content = headView.findViewById(R.id.tv_discussion_content);
        TextView tv_userName = headView.findViewById(R.id.tv_userName);
        ImageView ic_user = headView.findViewById(R.id.ic_user);
        TextView tv_createTime = headView.findViewById(R.id.tv_createTime);
        tv_commentCount = headView.findViewById(R.id.tv_commentCount);
        tv_empty = headView.findViewById(R.id.tv_empty);
        tv_content.setHtml(commentEntity.getContent(), new HtmlHttpImageGetter(tv_content, Constants.REFERER));
        tv_commentCount.setText("评论(" + commentEntity.getChildNum() + ")");
        tv_createTime.setText("发表于" + TimeUtil.converTime(commentEntity.getCreateTime()));
        if (commentEntity.getCreator() != null && commentEntity.getCreator().getRealName() != null)
            tv_userName.setText(commentEntity.getCreator().getRealName());
        else
            tv_userName.setText("匿名用户");
        if (commentEntity.getCreator() != null && commentEntity.getCreator().getAvatar() != null)
            GlideImgManager.loadCircleImage(context, commentEntity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, ic_user);
        else
            ic_user.setImageResource(R.drawable.user_default);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/" + role + "_" + relationId + "/m/comment?relation.id=" + relationId + "&page=" + page + "&mainId=" + commentEntity.getId() + "&limit=20";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CommentListResult>() {

            @Override
            public void onBefore(Request request) {
                if (!isRefresh && !isLoadMore)
                    showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isRefresh) {
                    mXRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    mXRecyclerView.refreshComplete(false);
                }
            }

            @Override
            public void onResponse(CommentListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmComments() != null) {
                    updateUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        mXRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        mXRecyclerView.refreshComplete(true);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                    mXRecyclerView.setLoadingMoreEnabled(false);
                }
            }
        }));
    }

    private void updateUI(List<CommentEntity> mDatas, Paginator paginator) {
        if (isRefresh) {
            this.mDatas.clear();
            mXRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            mXRecyclerView.loadMoreComplete(true);
        }
        this.mDatas.addAll(mDatas);
        mAdaper.notifyDataSetChanged();
        if (mDatas.size() == 0)
            tv_empty.setVisibility(View.VISIBLE);
        if (paginator != null && paginator.getHasNextPage()) {
            mXRecyclerView.setLoadingMoreEnabled(true);
        } else {
            mXRecyclerView.setLoadingMoreEnabled(false);
        }
        if (paginator != null)
            tv_commentCount.setText("评论(" + paginator.getTotalCount() + ")");
    }

    @Override
    public void setListener() {
        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog();
            }
        });
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showDeleteDialog(commentEntity.getId());
            }
        });
        mAdaper.setOnDeleteListener(new FreeChatDetailAdapter.OnDeleteListener() {
            @Override
            public void onDelete(String id, int position) {
                deleteChild(id, position);
            }
        });
    }

    private void showDeleteDialog(final String id) {
        View view = getLayoutInflater().inflate(R.layout.dialog_delete, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(context).create();
        RippleView rv_delete = getView(view, R.id.rv_delete);
        RippleView rv_cancel = getView(view, R.id.rv_cancel);
        rv_delete.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                deleteDialog.dismiss();
                deleteParent(id);
            }
        });
        rv_cancel.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
        deleteDialog.getWindow().setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        deleteDialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        deleteDialog.getWindow().setContentView(view);
        deleteDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void deleteParent(String id) {
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
                    MessageEvent event = new MessageEvent();
                    event.setAction(Action.DELETE_COMMENT);
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("删除失败，请稍后尝试", false);
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
                createChild(content);
            }
        });
    }

    //创建子 回复
    private void createChild(String message) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new Hashtable<>();
        map.put("relation.type", "workshop_comment");
        map.put("content", message);
        map.put("mainId", commentEntity.getId());
        map.put("relation.id", relationId);
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
                    if (tv_empty.getVisibility() != View.GONE)
                        tv_empty.setVisibility(View.GONE);
                    if (!mXRecyclerView.isLoadingMoreEnabled()) {
                        mDatas.add(entity);
                        mAdaper.notifyDataSetChanged();
                    } else {
                        toastFullScreen("回复成功", true);
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_MAIN_COMMENT;
                    RxBus.getDefault().post(event);
                } else {
                    toastFullScreen("回复失败", false);
                }
            }
        }, map));
    }

    private void deleteChild(String id, final int position) {
        String url = Constants.OUTRT_NET + "/m/comment/" + id + "?_method=delete";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toastFullScreen("删除失败，请稍后尝试", false);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mDatas.remove(position);
                    mAdaper.notifyDataSetChanged();
                    if (mDatas.size() == 0)
                        tv_empty.setVisibility(View.VISIBLE);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MAIN_COMMENT;
                    RxBus.getDefault().post(event);
                } else {
                    toastFullScreen("删除失败，请稍后尝试", false);
                }
            }
        }));
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
