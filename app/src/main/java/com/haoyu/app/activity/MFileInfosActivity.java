package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MFileInfoData;
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
 * 创建日期：2017/10/13 on 16:47
 * 描述:文件列表页面
 * 作者:马飞奔 Administrator
 */
public class MFileInfosActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private MFileInfosActivity context = this;
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
    private List<MFileInfo> fileInfos = new ArrayList<>();
    private MFileInfoAdapter adapter;
    private String relationId, relationType;
    private boolean isRefresh, isLoadMore;
    private int page = 1, limit = 20;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_fileinfos;
    }

    @Override
    public void initView() {
        setSupportToolbar();
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new MFileInfoAdapter(fileInfos);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    private void setSupportToolbar() {
        String title = getIntent().getStringExtra("title");
        if (title != null)
            toolBar.setTitle_text(title);
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/file?fileRelations[0].relation.id=" + relationId + "&fileRelations[0].relation.type=" + relationType + "&page=" + page + "&limit=" + limit + "&orders=CREATE_TIME.DESC";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MFileInfoData>>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore) {
                    if (loadingView.getVisibility() != View.GONE)
                        loadingView.setVisibility(View.GONE);
                } else
                    loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                if (loadingView.getVisibility() != View.GONE)
                    loadingView.setVisibility(View.GONE);
                if (isRefresh)
                    xRecyclerView.refreshComplete(false);
                else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else
                    loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<MFileInfoData> response) {
                if (loadingView.getVisibility() != View.GONE)
                    loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmFileInfos().size() > 0) {
                    updateUI(response.getResponseData().getmFileInfos(), response.getResponseData().getPaginator());
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

    private void updateUI(List<MFileInfo> mDatas, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            fileInfos.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        fileInfos.addAll(mDatas);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0 && position - 1 < fileInfos.size()) {
                    MFileInfo fileInfo = fileInfos.get(position - 1);
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
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
