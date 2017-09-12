package com.haoyu.app.activity;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.TeachStudyEvaluateAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.EvaluateResult;
import com.haoyu.app.entity.MEvaluateItemSubmissions;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * Created by acer1 on 2017/2/22.
 * 填写评课表
 */
public class TeachingStudyFillActivity extends BaseActivity implements View.OnClickListener {
    private TeachingStudyFillActivity context = this;
    private String workshopId;
    private String leceId;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_score)
    TextView tv_score;//分数
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;//评价的项
    @BindView(R.id.tv_warn)
    TextView tv_warn;//无评价项时提醒
    @BindView(R.id.et_advise)
    EditText et_advise;//评价总结及建议
    @BindView(R.id.ll_commit)
    LinearLayout ll_commit;//提交评价
    private List<MEvaluateItemSubmissions> submissionsList = new ArrayList<>();
    private String submissionId;
    private String submissionRelationId;
    private TeachStudyEvaluateAdapter evaluateAdapter;
    private List<String> idsList = new ArrayList<>();
    private List<Double> scoreList = new ArrayList<>();
    double d = 0;
    @BindView(R.id.scrollview)
    NestedScrollView scrollview;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_study_fill;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        leceId = getIntent().getStringExtra("leceId");
        evaluateAdapter = new TeachStudyEvaluateAdapter(submissionsList);

        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(evaluateAdapter);
    }

    @Override
    public void setListener() {
        ll_commit.setOnClickListener(context);
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
        evaluateAdapter.setEvaluateItem(new TeachStudyEvaluateAdapter.EvaluateItem() {
            @Override
            public void evaluateItem(String id, double score) {
                int indexId = idsList.indexOf(id);
                if (idsList.contains(id)) {
                    idsList.remove(indexId);
                    scoreList.remove(indexId);
                    idsList.add(id);
                    scoreList.add(score);
                } else {
                    idsList.add(id);
                    scoreList.add(score);
                }
                if (scoreList.size() > 0) {
                    d = 0;
                    for (int i = 0; i < scoreList.size(); i++) {
                        d += scoreList.get(i);
                    }
                    d = d / scoreList.size();
                    tv_score.setText(String.valueOf((int) d) + "分");
                }


            }
        });
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + leceId + "/edit_evaluate";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<EvaluateResult>() {
            @Override
            public void onError(Request request, Exception e) {
                scrollview.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(EvaluateResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.isSuccess()) {
                    if (response.getResponseData().getmEvaluateItems().size() == 0) {
                        tv_warn.setVisibility(View.VISIBLE);
                    } else {
                        tv_warn.setVisibility(View.GONE);
                    }
                    submissionId = response.getResponseData().getSubmissionId();
                    submissionRelationId = response.getResponseData().getSubmissionRelationId();
                    submissionsList.addAll(response.getResponseData().getmEvaluateItems());
                    evaluateAdapter.notifyDataSetChanged();
                    loadFailView.setVisibility(View.GONE);
                    ll_commit.setVisibility(View.VISIBLE);
                    scrollview.setVisibility(View.VISIBLE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }

            }
        }));
    }

    //提交听课评课列表
    private void commitContent(String content) {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/evaluate/submission/" + submissionId;
        Map<String, String> map = new HashMap<>();
        map.put("evaluateRelation.id", submissionRelationId);
        map.put("evaluateRelation.relation.id", leceId);
        map.put("comment", content);
        String userId = context.getUserId();
        for (int i = 0; i < idsList.size(); i++) {
            map.put("evaluateItemSubmissionMap[" + idsList.get(i) + "].score", String.valueOf(scoreList.get(i)));
            map.put("evaluateItemSubmissionMap[" + idsList.get(i) + "].creator.id ", userId);
        }
        map.put("_method", "put");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();

            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getSuccess()) {
                    toast(context, "提交成功");
                    MessageEvent event = new MessageEvent();
                    event.setAction(Action.SUBMIT_WORKSHOP_LECE);
                    event.setObj(String.valueOf((int) d));
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toast(context, "提交失败");
                }

            }
        }, map));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_commit:
                String advise = et_advise.getText().toString().trim();
                if (advise == null || advise.equals("")) {
                    toast(context, "请填写评价总结及建议");
                } else if (idsList.size() == 0) {
                    toast(context, "您还未评价打分");
                } else {
                    commitContent(advise);
                }
                break;

        }

    }
}
