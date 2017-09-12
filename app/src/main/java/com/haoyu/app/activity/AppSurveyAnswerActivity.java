package com.haoyu.app.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.haoyu.app.adapter.MoreSurveAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.SurveyAnswerSubmission;
import com.haoyu.app.entity.SurveyQuestionResult;
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
 * Created by acer1 on 2017/3/21.
 * 获取调查问卷更多的详情
 */
public class AppSurveyAnswerActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private AppSurveyAnswerActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.recyclerView)
    XRecyclerView recyclerView;
    private String acid;
    private String type;
    private String questionId;
    private String workShopId;
    private boolean isRefresh = false, isLoadMore = false;
    private int page = 1;
    private List<SurveyAnswerSubmission> mDatas = new ArrayList<>();
    private MoreSurveAdapter adapter;


    @Override
    public int setLayoutResID() {
        return R.layout.activity_survey_answer;
    }

    @Override
    public void initView() {
        type = getIntent().getStringExtra("type");
        questionId = getIntent().getStringExtra("questionId");
        workShopId = getIntent().getStringExtra("workShopId");
        acid = getIntent().getStringExtra("activityId");
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setArrowImageView(R.drawable.refresh_arrow);
        adapter = new MoreSurveAdapter(context, mDatas);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.setLoadingListener(this);
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    public void initData() {
        String url;
        if (type != null && type.equals("workshop")) {
            url = Constants.OUTRT_NET + "/student_" + workShopId + "/m/survey_result /" + questionId + "/submissions?page=" + page;
        } else {
            url = Constants.OUTRT_NET + "/" + acid + "/study/m/survey_result /" + questionId + "/submissions?page=" + page;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<SurveyQuestionResult>() {
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
                if (isRefresh)
                    recyclerView.refreshComplete(false);
                else if (isLoadMore) {
                    page -= 1;
                    recyclerView.loadMoreComplete(false);
                } else
                    loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(SurveyQuestionResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmSubmissions() != null && response.getResponseData().getmSubmissions().size() > 0) {
                    updateUI(response.getResponseData().getmSubmissions(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh)
                        recyclerView.refreshComplete(true);
                    else if (isLoadMore)
                        recyclerView.loadMoreComplete(true);
                }

            }
        }));
    }

    private void updateUI(List<SurveyAnswerSubmission> list, Paginator paginator) {
        if (recyclerView.getVisibility() != View.VISIBLE)
            recyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            recyclerView.refreshComplete(true);
            mDatas.clear();
        } else if (isLoadMore)
            recyclerView.loadMoreComplete(true);
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage())
            recyclerView.setLoadingMoreEnabled(true);
        else
            recyclerView.setLoadingMoreEnabled(false);
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
