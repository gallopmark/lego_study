package com.haoyu.app.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.WSTSSuggestAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.MEvaluateSubmission;
import com.haoyu.app.entity.MEvaluateSubmissionData;
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
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价和建议
 * 作者:xiaoma
 */

public class WSTSSuggestActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private WSTSSuggestActivity context;
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
    private String workshopId, lcecId;
    private List<MEvaluateSubmission> mDatas = new ArrayList<>();
    private WSTSSuggestAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private int page = 1, limit = 20;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstssuggest;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        workshopId = getIntent().getStringExtra("workshopId");
        lcecId = getIntent().getStringExtra("leceId");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new WSTSSuggestAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    private void setToolBar() {
        toolBar.setTitle_text("评价总结及建议");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/submissions?page=" + page + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MEvaluateSubmissionData>>() {
            @Override
            public void onBefore(Request request) {
                if (!isRefresh && isLoadMore) {
                    loadingView.setVisibility(View.VISIBLE);
                }
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
            public void onResponse(BaseResponseResult<MEvaluateSubmissionData> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmEvaluateSubmissions().size() > 0) {
                    UpdateUI(response.getResponseData().getmEvaluateSubmissions(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                        xRecyclerView.setVisibility(View.GONE);
                    }
                }
            }
        }));
    }

    private void UpdateUI(List<MEvaluateSubmission> list, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (isRefresh) {
            xRecyclerView.refreshComplete(true);
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
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
