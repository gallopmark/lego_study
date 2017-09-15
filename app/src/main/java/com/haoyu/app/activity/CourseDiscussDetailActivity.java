package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MobileUser;
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
import com.haoyu.app.utils.OkHttpClientManager.ResultCallback;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RippleView;
import com.haoyu.app.view.RippleView.OnRippleCompleteListener;
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
 * 课程学习讨论详情页面
 */
public class CourseDiscussDetailActivity extends BaseActivity implements
        View.OnClickListener, XRecyclerView.LoadingListener {
    private AppDiscussionAdapter adapter;
    private CourseDiscussDetailActivity context = this;
    private DiscussEntity discussEntity;
    private View headerView;
    private List<ReplyEntity> list = new ArrayList<>();
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.contentView)
    RelativeLayout contentView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    private String orders = "CREATE_TIME.ASC";
    private int page = 1;
    @BindView(R.id.rl_like)
    RelativeLayout rl_like;
    @BindView(R.id.rl_comment)
    RelativeLayout rl_comment;
    @BindView(R.id.tv_like)
    TextView tv_like;
    @BindView(R.id.tv_comment)
    TextView tv_comment;
    private TextView tv_commentCount;
    private String discussRelationId = "", text_comment = "评论";
    private int replyNum, supportNum;
    private int replyPosition = -1;
    private boolean isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_discussion_details;
    }

    @Override
    public void initView() {
        discussEntity = (DiscussEntity) getIntent().getSerializableExtra("entity");
        headerView = getLayoutInflater().inflate(R.layout.course_discuss_header, null);
        showHeader();
        xRecyclerView.addHeaderView(headerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new AppDiscussionAdapter(context, list, getUserId());
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
        registRxBus();
    }

    private void showHeader() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(params);
        TextView tv_discussion_title = headerView.findViewById(R.id.tv_discussion_title);
        ExpandableTextView tv_discussion_text = headerView.findViewById(R.id.tv_discussion_text);
        ImageView iv_userIco = headerView.findViewById(R.id.ic_user);
        TextView tv_userName = headerView.findViewById(R.id.tv_userName);
        TextView tv_createDate = headerView.findViewById(R.id.tv_createTime);
        tv_commentCount = headerView.findViewById(R.id.tv_commentCount);
        tv_discussion_title.setText(discussEntity.getTitle());
        tv_discussion_text.setHtmlText(discussEntity.getContent());
        if (discussEntity.getCreator() != null) {
            GlideImgManager.loadCircleImage(context, discussEntity.getCreator().getAvatar(), R.drawable.user_default, R.drawable.user_default, iv_userIco);
            tv_userName.setText(discussEntity.getCreator().getRealName());
        } else {
            iv_userIco.setImageResource(R.drawable.user_default);
            tv_userName.setText("匿名用户");
        }
        tv_createDate.setText("发表于" + TimeUtil.converTime(discussEntity.getCreateTime()));
        if (discussEntity.getmDiscussionRelations() != null
                && discussEntity.getmDiscussionRelations().size() > 0) {
            discussRelationId = discussEntity.getmDiscussionRelations().get(0).getId();
            replyNum = discussEntity.getmDiscussionRelations().get(0).getReplyNum();
            tv_commentCount.setText(text_comment + "(" + replyNum + ")");
            tv_comment.setText(String.valueOf(replyNum));
            supportNum = discussEntity.getmDiscussionRelations().get(0).getSupportNum();
            tv_like.setText(String.valueOf(supportNum));
        }
        if (discussEntity.getCreator() != null && discussEntity.getCreator().getId() != null
                && discussEntity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        } else {
            toolBar.setShow_right_button(false);
        }
    }

    /**
     * 获取评论和回复列表
     */
    public void initData() {
        if (isRefresh || isLoadMore)
            loadingView.setVisibility(View.GONE);
        else
            loadingView.setVisibility(View.VISIBLE);
        final String url = Constants.OUTRT_NET + "/m/discussion/post"
                + "?discussionUser.discussionRelation.id=" + discussRelationId
                + "&page=" + page + "&orders=" + orders;
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String s) throws Exception {
                return getMainReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                return getChildReply(url, result);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReplyListResult>() {
                    @Override
                    public void accept(ReplyListResult response) throws Exception {
                        if (loadingView.getVisibility() != View.GONE)
                            loadingView.setVisibility(View.GONE);
                        if (contentView.getVisibility() != View.VISIBLE)
                            contentView.setVisibility(View.VISIBLE);
                        updateUI(response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (loadingView.getVisibility() != View.GONE)
                            loadingView.setVisibility(View.GONE);
                        if (isRefresh) {
                            xRecyclerView.refreshComplete(false);
                        } else if (isLoadMore) {
                            page -= 1;
                            xRecyclerView.loadMoreComplete(false);
                        } else {
                            loadFailView.setVisibility(View.VISIBLE);
                        }
                    }
                }));
    }

    /*获取主回复*/
    private ReplyListResult getMainReply(String url) throws Exception {
        String listStr = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(listStr, ReplyListResult.class);
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
        if (response != null && response.getResponseData() != null && response.getResponseData().getmDiscussionPosts() != null && response.getResponseData().getmDiscussionPosts().size() > 0) {
            if (isRefresh) {
                list.clear();
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
            list.addAll(response.getResponseData().getmDiscussionPosts());
            adapter.notifyDataSetChanged();
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
            if (isRefresh) {
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
        }
        if (response != null && response.getResponseData() != null && response.getResponseData().getPaginator() != null) {
            replyNum = response.getResponseData().getPaginator().getTotalCount();
            if (response.getResponseData().getPaginator().getHasNextPage())
                xRecyclerView.setLoadingMoreEnabled(true);
            else
                xRecyclerView.setLoadingMoreEnabled(false);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
        tv_commentCount.setText(text_comment + "(" + replyNum + ")");
        tv_comment.setText(String.valueOf(replyNum));
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showBottomDialog();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        rl_like.setOnClickListener(context);
        rl_comment.setOnClickListener(context);
        adapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, final int position, final ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, final int position) {
                childPosition = position;
                showCommentDialog(true);
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
                intent.putExtra("relationId", discussRelationId);
                startActivity(intent);
            }
        });
        adapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                deleteReply(id, position);
            }
        });
    }

    private int childPosition;

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context);
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildReply(content);
                } else {
                    createComment(content);
                }
            }
        });
        dialog.show();
    }

    /**
     * 创建主回复
     */
    private void createComment(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("discussionUser.discussionRelation.id", discussRelationId);
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception exception) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    ReplyEntity entity = response.getResponseData();
                    entity.setCreator(getCreator(entity.getCreator()));
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        if (entity.getCreator() != null && entity.getCreator().getAvatar() == null) {
                            entity.getCreator().setAvatar(getAvatar());
                        }
                        list.add(entity);
                        adapter.notifyDataSetChanged();
                    }
                    replyNum++;
                    tv_commentCount.setText(text_comment + "(" + (replyNum) + ")");
                    tv_comment.setText(String.valueOf(replyNum));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_MAIN_REPLY;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toastFullScreen(response.getResponseMsg(), false);
                    }
                }
            }
        }, map));
    }

    /**
     * 发送子回复
     */
    private void sendChildReply(final String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", list.get(childPosition).getId());
        map.put("discussionUser.discussionRelation.id", discussRelationId);
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<ReplyResult>() {
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
                    entity.setCreator(getCreator(entity.getCreator()));
                    int childPostCount = list.get(childPosition).getChildPostCount() + 1;
                    list.get(childPosition).setChildPostCount(childPostCount);
                    if (list.get(childPosition).getChildReplyEntityList() != null && list.get(childPosition).getChildReplyEntityList().size() < 10) {
                        list.get(childPosition).getChildReplyEntityList().add(entity);
                    } else {
                        toast(context, "回复成功");
                    }
                    adapter.notifyDataSetChanged();
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
        String relationId = list.get(position).getId();
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
                    int count = list.get(position).getSupportNum() + 1;
                    list.get(position).setSupportNum(count);
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
        String url = Constants.OUTRT_NET + "/m/discussion/post/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", discussRelationId);
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
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                    replyNum--;
                    if (replyNum <= 0)
                        replyNum = 0;
                    tv_commentCount.setText(text_comment + "(" + (replyNum) + ")");
                    tv_comment.setText(String.valueOf(replyNum));
                }
            }
        }, map));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_like:
                createLike();
                break;
            case R.id.rl_comment:
                showCommentDialog(false);
                break;
        }
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_discussion_delete, null);
        final AlertDialog bottomDialog = new AlertDialog.Builder(context).create();
        RippleView rv_delete = view.findViewById(R.id.rv_delete);
        RippleView rv_cancel = view.findViewById(R.id.rv_cancel);
        rv_delete.setOnRippleCompleteListener(new OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                bottomDialog.dismiss();
                deleteDiscussion();
            }
        });
        rv_cancel.setOnRippleCompleteListener(new OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
        bottomDialog.getWindow().setLayout(
                ScreenUtils.getScreenWidth(context),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        bottomDialog.getWindow().setContentView(view);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    /**
     * 删除讨论
     */
    private void deleteDiscussion() {
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        String url = Constants.OUTRT_NET + "/m/discussion/" + discussEntity.getId();
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_COURSE_DISCUSSION;
                    RxBus.getDefault().post(event);
                    finish();
                }
            }
        }, map));
    }

    /* 创建观点（点赞） */
    private void createLike() {
        String url = Constants.OUTRT_NET + "/m/attitude";
        final String entityId = discussEntity.getId();
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", entityId);
        map.put("relation.type", "discussion");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<AttitudeMobileResult>() {
            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null
                        && response.getResponseCode().equals("00")) {
                    supportNum += 1;
                    tv_like.setText(String.valueOf(supportNum));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_LIKE;
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
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
            return creator;
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
        if (event.action.equals(Action.CREATE_CHILD_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            int childPostCount = list.get(replyPosition).getChildPostCount() + 1;
            list.get(replyPosition).setChildPostCount(childPostCount);
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (list.get(replyPosition).getChildReplyEntityList() != null && list.get(replyPosition).getChildReplyEntityList().size() < 10) {
                list.get(replyPosition).getChildReplyEntityList().add(entity);
            } else {
                toast(context, "回复成功");
            }
            adapter.notifyDataSetChanged();
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY)) {
            list.remove(replyPosition);
            adapter.notifyDataSetChanged();
            replyNum--;
            tv_commentCount.setText(text_comment + "(" + (replyNum) + ")");
            tv_comment.setText(String.valueOf(replyNum));
        }
    }

}

