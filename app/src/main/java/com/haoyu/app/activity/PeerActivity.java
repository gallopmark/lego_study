package com.haoyu.app.activity;


import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.PeerAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.EducationConsultResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
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

/**
 * Created by acer1
 * 2017/1/10.
 * 同行
 */
public class PeerActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private PeerActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private PeerAdapter mAdapter;
    private List<MobileUser> usersList = new ArrayList<>();
    private boolean isRefresh, isLoadMore;
    private int page = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_peer;
    }

    @Override
    public void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        mAdapter = new PeerAdapter(context, usersList);
        xRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        xRecyclerView.setLoadingListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/user?page=" + page + "&limit=30";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<EducationConsultResult>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore)
                    loadingView.setVisibility(View.GONE);
                else
                    loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
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
            public void onResponse(EducationConsultResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getMUsers() != null && response.getResponseData().getMUsers().size() > 0) {
                    updateUI(response.getResponseData().getMUsers(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh)
                        xRecyclerView.refreshComplete(true);
                    else if (isLoadMore)
                        xRecyclerView.loadMoreComplete(true);
                    else {
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<MobileUser> mUsers, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            usersList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        usersList.addAll(mUsers);
        mAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
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
