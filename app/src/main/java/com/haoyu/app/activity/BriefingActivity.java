package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.LinearLayout;

import com.haoyu.app.adapter.BriefingAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.entity.BriefingsResult;
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
 * 创建日期：2017/1/8 on 9:28
 * 描述:研修简报列表
 * 作者:马飞奔 Administrator
 */
public class BriefingActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private BriefingActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.emptyBrief)
    LinearLayout emptyBrief;
    private List<BriefingEntity> brirfList = new ArrayList<>();
    private BriefingAdapter adapter;
    private String relationId, relationType, type;
    private int page = 1;
    private boolean isRefresh, isLoadMore;
    private int index = -1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_briefing;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        type = getIntent().getStringExtra("type");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new BriefingAdapter(brirfList);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/briefing?announcementRelations[0].relation.id=" + relationId
                + "&page=" + page + "&limit=20" + "&orders=CREATE_TIME.DESC";
        if (relationType != null) {
            url += "&announcementRelations[0].relation.type" + relationType;
        }
        if (type != null) {
            url += "&type=" + type;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BriefingsResult>() {
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
                    xRecyclerView.loadMoreComplete(false);
                    page -= 1;
                } else
                    loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BriefingsResult researchResult) {
                loadingView.setVisibility(View.GONE);
                if (researchResult != null && researchResult.getResponseData() != null
                        && researchResult.getResponseData().getAnnouncements() != null
                        && researchResult.getResponseData().getAnnouncements().size() > 0) {
                    updateBriefList(researchResult.getResponseData().getAnnouncements(), researchResult.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        emptyBrief.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateBriefList(List<BriefingEntity> announcements, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            xRecyclerView.refreshComplete(true);
            brirfList.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        brirfList.addAll(announcements);
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
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0 && position - 1 < brirfList.size()) {
                    index = position - 1;
                    String id = brirfList.get(index).getId();
                    Intent intent = new Intent(context, BriefingDetailActivity.class);
                    intent.putExtra("relationId", id);
                    startActivity(intent);
                }
            }
        });

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
