package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haoyu.app.adapter.FreeChatAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

public class FreeChatActiviy extends BaseActivity implements XRecyclerView.LoadingListener {
    private FreeChatActiviy context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.bt_create)
    Button bt_create;
    private int page = 1;
    private boolean isRefresh = false;
    private boolean isLoadMore = false;
    private FreeChatAdapter mAdapter;
    private List<CommentEntity> mCommentsList = new ArrayList<>();
    private String role, relationId;
    private int clickIndex;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_freechat;
    }

    @Override
    public void initView() {
        role = getIntent().getStringExtra("role");
        relationId = getIntent().getStringExtra("relationId");
        mAdapter = new FreeChatAdapter(context, mCommentsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setAdapter(mAdapter);
        xRecyclerView.setLoadingListener(this);
        registRxBus();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/" + role + "_" + relationId + "/m/comment?relation.id=" + relationId + "&page=" + page + "&orders=CREATE_TIME.DESC&limit=20";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CommentListResult>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore)
                    loadingView.setVisibility(View.GONE);
                else
                    loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
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

            @Override
            public void onResponse(CommentListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmComments() != null && response.getResponseData().getmComments().size() > 0) {
                    updateUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                    xRecyclerView.setLoadingMoreEnabled(false);
                }
            }
        }));
    }

    private void updateUI(List<CommentEntity> mDatas, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            mCommentsList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mCommentsList.addAll(mDatas);
        mAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_empty:
                        createFreeChat();
                        return;
                    case R.id.bt_create:
                        createFreeChat();
                        return;
                }
            }
        };
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        bt_create.setOnClickListener(listener);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0 && position - 1 < mCommentsList.size()) {
                    clickIndex = position - 1;
                    Intent intent = new Intent(context, FreeChatDetailActivity.class);
                    intent.putExtra("relationId", relationId);
                    intent.putExtra("role", role);
                    intent.putExtra("entity", mCommentsList.get(clickIndex));
                    startActivity(intent);
                }
            }
        });
    }

    private void createFreeChat() {
        Intent intent = new Intent(context, FreeChatEditActivity.class);
        intent.putExtra("relationId", relationId);
        startActivity(intent);
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
        String action = event.getAction();
        if (action.equals(Action.CREATE_COMMENT) && event.obj != null && event.obj instanceof CommentEntity) {
            CommentEntity entity = (CommentEntity) event.obj;
            if (entity.getCreator() != null && entity.getCreator().getAvatar() == null) {
                entity.getCreator().setAvatar(getAvatar());
            }
            if (!xRecyclerView.isLoadingMoreEnabled()) {
                mCommentsList.add(entity);
                mAdapter.notifyDataSetChanged();
            }
        } else if (action.equals(Action.DELETE_COMMENT)) {
            mCommentsList.remove(clickIndex);
            mAdapter.notifyDataSetChanged();
        } else if (action.equals(Action.CREATE_MAIN_COMMENT)) {
            int childNum = mCommentsList.get(clickIndex).getChildNum() + 1;
            mCommentsList.get(clickIndex).setChildNum(childNum);
            mAdapter.notifyDataSetChanged();
        } else if (action.equals(Action.DELETE_MAIN_COMMENT)) {
            int childNum = mCommentsList.get(clickIndex).getChildNum() - 1;
            mCommentsList.get(clickIndex).setChildNum(childNum);
            mAdapter.notifyDataSetChanged();
        }
    }

}
