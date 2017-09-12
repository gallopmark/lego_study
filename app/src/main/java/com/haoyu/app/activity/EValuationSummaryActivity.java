package com.haoyu.app.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.haoyu.app.adapter.TeachingStudyEvaluationAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.MEvaluateEntity;
import com.haoyu.app.entity.MEvaluateResult;
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
 * Created by acer1 on 2017/2/23.
 * 评课结果的评价总结及建议详情
 */
public class EValuationSummaryActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private EValuationSummaryActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    private int page = 1;
    private boolean isRefresh = false, isLoadMore = false;
    private String workshopId;
    private String lcecId;
    private List<MEvaluateEntity> evaluateEntityList = new ArrayList<>();//听课评课总结及建议集合
    private TeachingStudyEvaluationAdapter detailAdaper;//听课评课评价总结及建议

    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    @Override
    public int setLayoutResID() {
        return R.layout.teaching_study_evaluation_summary;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        lcecId = getIntent().getStringExtra("leceId");
        detailAdaper = new TeachingStudyEvaluationAdapter(context, evaluateEntityList);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(manager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        xRecyclerView.setAdapter(detailAdaper);
        xRecyclerView.setLoadingListener(context);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onRefresh() {
        page = 1;
        isRefresh = true;
        isLoadMore = false;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }

    //评价总结建议列表
    public void initData() {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/submissions?page=" + page;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<MEvaluateResult>() {

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(MEvaluateResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.isSuccess()) {
                    updateUI(response);
                    loadFailView.setVisibility(View.GONE);
                    xRecyclerView.setVisibility(View.VISIBLE);


                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(false);
                    } else if (isLoadMore) {
                        page -= 1;
                        xRecyclerView.loadMoreComplete(false);
                    } else {
                        onNetWorkError(context);
                    }
                }
            }
        });
    }

    private void updateUI(MEvaluateResult result) {
        if (result.getResponseData() != null && result.getResponseData() != null) {
            if (isRefresh) {
                evaluateEntityList.clear();
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
            evaluateEntityList.addAll(result.getResponseData().getmEvaluateSubmissions());
            detailAdaper.notifyDataSetChanged();
            if (result.getResponseData().getPaginator() != null && result.getResponseData().getPaginator().getHasNextPage()) {
                xRecyclerView.setLoadingMoreEnabled(true);
            } else {
                xRecyclerView.setLoadingMoreEnabled(false);
            }
        } else {
            if (isRefresh) {
                xRecyclerView.refreshComplete(true);
            } else if (isLoadMore) {
                xRecyclerView.loadMoreComplete(true);
            }
        }
    }
}
