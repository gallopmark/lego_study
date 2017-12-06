package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.WSTSEvaluateAdapter;
import com.haoyu.app.adapter.WSTSSuggestAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MEvaluateItem;
import com.haoyu.app.entity.MEvaluateSubmission;
import com.haoyu.app.entity.MEvaluateSubmissionData;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.VerticalScrollView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价结果
 * 作者:xiaoma
 */

public class WSTSInfoResultActivity extends BaseActivity {
    private WSTSInfoResultActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.sv_content)
    VerticalScrollView sv_content;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.rv_evaluate)
    RecyclerView rv_evaluate;
    @BindView(R.id.tv_emptyContent)
    TextView tv_emptyContent;
    @BindView(R.id.tv_adviseCount)
    TextView tv_adviseCount;
    @BindView(R.id.errorSuggest)
    LoadFailView errorSuggest;
    @BindView(R.id.rv_suggest)
    RecyclerView rv_suggest;
    @BindView(R.id.tv_emptySuggest)
    TextView tv_emptySuggest;
    @BindView(R.id.tv_expand)
    TextView tv_expand;
    private String workshopId, lcecId;
    private List<MEvaluateItem> evaluateItems = new ArrayList<>();
    private WSTSEvaluateAdapter evaluateAdapter;
    private List<MEvaluateSubmission> submissions = new ArrayList<>();
    private WSTSSuggestAdapter suggestAdapter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstsinforesult;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        workshopId = getIntent().getStringExtra("workshopId");
        lcecId = getIntent().getStringExtra("leceId");
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        rv_evaluate.setLayoutManager(layoutManager);
        evaluateAdapter = new WSTSEvaluateAdapter(evaluateItems);
        rv_evaluate.setAdapter(evaluateAdapter);
        FullyLinearLayoutManager mLayoutManager = new FullyLinearLayoutManager(context);
        mLayoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        rv_suggest.setLayoutManager(mLayoutManager);
        suggestAdapter = new WSTSSuggestAdapter(context, submissions);
        rv_suggest.setAdapter(suggestAdapter);
    }

    private void setToolBar() {
        toolBar.setTitle_text("查看评课结果");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        getEvaluates();
        getSuggests();
    }

    private void getEvaluates() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/result";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<List<MEvaluateItem>>>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<List<MEvaluateItem>> response) {
                loadingView.setVisibility(View.GONE);
                sv_content.setVisibility(View.VISIBLE);
                if (response != null && response.getResponseData() != null && response.getResponseData().size() > 0) {
                    updateUI(response.getResponseData());
                } else {
                    rv_evaluate.setVisibility(View.GONE);
                    tv_emptyContent.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(List<MEvaluateItem> mDatas) {
        evaluateItems.addAll(mDatas);
        evaluateAdapter.notifyDataSetChanged();
        int score = 0;
        for (int i = 0; i < evaluateItems.size(); i++) {
            score += evaluateItems.get(i).getAvgScore();
        }
        score = score / evaluateItems.size();
        String text = "您为本课的评分：" + score + "分";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("：") + 1;
        int end = text.length();
        ss.setSpan(new AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int color = ContextCompat.getColor(context, R.color.orange);
        ss.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_score.setText(ss);
    }

    private void getSuggests() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/submissions?limit=6";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MEvaluateSubmissionData>>() {
            @Override
            public void onError(Request request, Exception e) {
                errorSuggest.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<MEvaluateSubmissionData> response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().getmEvaluateSubmissions().size() > 0) {
                    updateSuggest(response.getResponseData().getmEvaluateSubmissions(), response.getResponseData().getPaginator());
                } else {
                    rv_suggest.setVisibility(View.GONE);
                    tv_emptySuggest.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateSuggest(List<MEvaluateSubmission> mDatas, Paginator paginator) {
        submissions.addAll(mDatas);
        suggestAdapter.notifyDataSetChanged();
        if (paginator != null) {
            tv_adviseCount.setText("共" + paginator.getTotalCount() + "条信息");
            if (paginator.getHasNextPage()) {
                tv_expand.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getEvaluates();
            }
        });
        errorSuggest.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getSuggests();
            }
        });
        evaluateAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, WSTSInfoScoreActivity.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("leceId", lcecId);
                if (position >= 0 && position < evaluateItems.size()) {
                    String itemId = evaluateItems.get(position).getId();
                    intent.putExtra("itemId", itemId);
                }
                startActivity(intent);
            }
        });
        tv_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WSTSSuggestActivity.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("leceId", lcecId);
                startActivity(intent);
            }
        });
    }
}
