package com.haoyu.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.activity.CourseDiscussEditActivity;
import com.haoyu.app.activity.CourseDiscussDetailActivity;
import com.haoyu.app.adapter.PageDiscussionAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussListResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 讨论fragment
 *
 * @author xiaoma
 */
public class PageDiscussionFragment extends BaseFragment implements XRecyclerView.LoadingListener, OnClickListener {
    private PageDiscussionAdapter adapter; // 讨论适配器
    @BindView(R.id.bt_createDiscussion)
    Button createDiscuss; // 创建讨论按钮
    @BindView(R.id.loadView)
    LoadingView loadView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    private boolean isLoadMore = false;
    private boolean isRefresh = false;
    @BindView(R.id.li_emptyDiscussion)
    LinearLayout li_emptyDiscussion; // 空讨论
    private List<DiscussEntity> list = new ArrayList<>(); // 讨论集合
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    private String orders = "CREATE_TIME.DESC";
    private int page = 1, oldPage;
    private String courseId;
    private String discussionRelationType = "courseStudy";
    private int itemIndex;

    /**
     * activity与fragment的数据交互
     *
     * @param event
     */
    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals(Action.CREATE_COURSE_DISCUSSION) && event.obj != null && event.obj instanceof DiscussEntity) {
            DiscussEntity entity = (DiscussEntity) event.obj;
            list.add(0, entity);
            adapter.notifyDataSetChanged();
            if (xRecyclerView.getVisibility() == View.GONE) {
                xRecyclerView.setVisibility(View.VISIBLE);
            }
            if (li_emptyDiscussion.getVisibility() == View.VISIBLE) {
                li_emptyDiscussion.setVisibility(View.GONE);
            }
        } else if (action.equals(Action.ALTER_COURSE_DISCUSSION) && event.obj != null && event.obj instanceof DiscussEntity) {
            DiscussEntity entity = (DiscussEntity) event.obj;
            list.set(itemIndex, entity);
            adapter.notifyDataSetChanged();
        } else if (action.equals(Action.DELETE_COURSE_DISCUSSION)) {
            list.remove(itemIndex);
            adapter.notifyDataSetChanged();
            if (list.size() <= 0) {
                li_emptyDiscussion.setVisibility(View.VISIBLE);
                xRecyclerView.setVisibility(View.GONE);
            }
        } else if (action.equals(Action.CREATE_MAIN_REPLY)) {
            if (list.get(itemIndex).getmDiscussionRelations() != null
                    && list.get(itemIndex).getmDiscussionRelations().size() > 0) {
                int replyNum = list.get(itemIndex)
                        .getmDiscussionRelations().get(0)
                        .getReplyNum();
                list.get(itemIndex).getmDiscussionRelations().get(0)
                        .setReplyNum(1 + replyNum);
                adapter.notifyDataSetChanged();
            }
        } else if (action.equals(Action.CREATE_LIKE)) {
            if (list.get(itemIndex).getmDiscussionRelations() != null
                    && list.get(itemIndex).getmDiscussionRelations().size() > 0) {
                int supportNum = list.get(itemIndex).getmDiscussionRelations().get(0).getSupportNum() + 1;
                list.get(itemIndex).getmDiscussionRelations().get(0).setSupportNum(supportNum);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public int createView() {
        return R.layout.fragment_page_discussion;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            courseId = bundle.getString("entityId");
        }
        view.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        adapter = new PageDiscussionAdapter(getActivity(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/discussion" + "?discussionRelations[0].relation.id=" + courseId
                + "&discussionRelations[0].relation.type=" + discussionRelationType + "&page=" + page
                + "&orders=" + orders;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DiscussListResult>() {
            @Override
            public void onBefore(Request request) {
                if (!isRefresh && !isLoadMore) {
                    loadView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                page = oldPage;
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    loadView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(DiscussListResult response) {
                loadView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null &&
                        response.getResponseData().getmDiscussions() != null
                        && response.getResponseData().getmDiscussions().size() > 0) {
                    updateUI(response.getResponseData().getmDiscussions(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        li_emptyDiscussion.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<DiscussEntity> discussions, Paginator paginator) {
        loadView.setVisibility(View.GONE);
        xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            list.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        list.addAll(discussions);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        adapter.setOnPartCallBack(new PageDiscussionAdapter.OnPartCallBack() {
            @Override
            public void createLink(int position, TextView tv_like) {
                if (!list.get(position).isSupport())
                    createLike(tv_like, position);
                else
                    toast("您已经点赞过");
            }

            @Override
            public void comment(final int position) {
                CommentDialog dialog = new CommentDialog(context, "输入评论内容");
                dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
                    @Override
                    public void sendComment(String content) {
                        createComment(content, position);
                    }
                });
                dialog.show();
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter baseRecyclerAdapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0 && position - 1 < list.size()) {
                    itemIndex = position - 1;
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), CourseDiscussDetailActivity.class);
                    intent.putExtra("entity", list.get(itemIndex));
                    startActivity(intent);
                }
            }
        });
        createDiscuss.setOnClickListener(this);
    }

    /**
     * 创建观点（点赞）
     *
     * @param position
     */
    private void createLike(final TextView tv_like, final int position) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        final String entityId = list.get(position).getId();
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", entityId);
        map.put("relation.type", "discussion");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            @Override
            public void onError(Request request, Exception exception) {
                onNetWorkError();
            }

            @Override
            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    if (list.get(position).getmDiscussionRelations() != null && list.get(position).getmDiscussionRelations().size() > 0) {
                        int supportNum = list.get(position).getmDiscussionRelations().get(0).getSupportNum() + 1;
                        list.get(position).getmDiscussionRelations().get(0).setSupportNum(supportNum);
                        adapter.notifyDataSetChanged();
                    }
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tv_like);
                    list.get(position).setSupport(true);
                } else if (response != null && response.getResponseMsg() != null) {
                    list.get(position).setSupport(true);
                    toast("您已点赞过");
                } else {
                    toast("点赞失败");
                }
            }
        }, map));
    }

    /**
     * 创建评论
     */
    private void createComment(String content, final int position) {
        DiscussEntity entity = list.get(position);
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("content", content);
            map.put("discussionUser.discussionRelation.id", entity
                    .getmDiscussionRelations().get(0).getId());
            String url = Constants.OUTRT_NET + "/m/discussion/post";
            addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
                @Override
                public void onError(Request request, Exception exception) {
                    onNetWorkError();
                }

                @Override
                public void onResponse(ReplyResult response) {
                    if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                        if (list.get(position).getmDiscussionRelations() != null
                                && list.get(position).getmDiscussionRelations().size() > 0) {
                            int replyNum = list.get(position).getmDiscussionRelations().get(0).getReplyNum() + 1;
                            list.get(position).getmDiscussionRelations().get(0).setReplyNum(replyNum);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        if (response != null && response.getResponseMsg() != null) {
                            toast(response.getResponseMsg());
                        }
                    }
                }
            }, map));
        }
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        oldPage = page;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        oldPage = page;
        page += 1;
        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_createDiscussion:
                Intent intent = new Intent(getActivity(), CourseDiscussEditActivity.class);
                intent.putExtra("courseId", courseId);
                getActivity().startActivity(intent);
                break;
        }
    }

}
