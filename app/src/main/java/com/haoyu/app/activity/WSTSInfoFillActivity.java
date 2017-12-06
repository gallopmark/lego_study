package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.adapter.WSTSEvaluateFillAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.MEvaluateFillData;
import com.haoyu.app.entity.MEvaluateItem;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.VerticalScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊填写听课评课表
 * 作者:xiaoma
 */

public class WSTSInfoFillActivity extends BaseActivity {
    private WSTSInfoFillActivity context;
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
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.bt_commit)
    Button bt_commit;
    private String workshopId, leceId, submissionId, submissionRelationId;
    private List<MEvaluateItem> evaluateItems = new ArrayList<>();
    private WSTSEvaluateFillAdapter adapter;
    private Map<String, Integer> hashMap = new HashMap<>();

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstsinfofill;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        workshopId = getIntent().getStringExtra("workshopId");
        leceId = getIntent().getStringExtra("leceId");
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        rv_evaluate.setLayoutManager(layoutManager);
        adapter = new WSTSEvaluateFillAdapter(evaluateItems);
        rv_evaluate.setAdapter(adapter);
        String hint = "点击输入评价内容...\n输入内容请从本节课的不足之处成功之处与需改的意见出发简要点评课";
        et_content.setHint(hint);
    }

    private void setToolBar() {
        toolBar.setTitle_text("填写评课表");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + leceId + "/edit_evaluate";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MEvaluateFillData>>() {
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
            public void onResponse(BaseResponseResult<MEvaluateFillData> response) {
                loadingView.setVisibility(View.GONE);
                sv_content.setVisibility(View.VISIBLE);
                bt_commit.setVisibility(View.VISIBLE);
                if (response != null && response.getResponseData() != null) {
                    submissionId = response.getResponseData().getSubmissionId();
                    submissionRelationId = response.getResponseData().getSubmissionRelationId();
                    updateUI(response.getResponseData().getmEvaluateItems());
                }
            }
        }));
    }

    private void updateUI(List<MEvaluateItem> mDatas) {
        if (mDatas.size() > 0) {
            evaluateItems.addAll(mDatas);
            adapter.notifyDataSetChanged();
            adapter.setOnItemStarChangeListener(new WSTSEvaluateFillAdapter.OnItemStarChangeListener() {
                @Override
                public void onItemStarChange(String id, int score) {
                    hashMap.put(id, score);
                    setScore();
                }
            });
        } else {
            rv_evaluate.setVisibility(View.GONE);
            tv_emptyContent.setVisibility(View.VISIBLE);
        }
    }

    private void setScore() {
        int score = 0;
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            score += entry.getValue();
        }
        score = score / evaluateItems.size();
        String text = "您为本课的评分：" + score + "分";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("：") + 1;
        int end = text.length();
        ss.setSpan(new AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int color = ContextCompat.getColor(context, R.color.orange);
        ss.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_score.setText(null);
        tv_score.setText(ss);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        et_content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sv_content.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sv_content.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 10);
                return false;
            }
        });
        bt_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkOut()) {
                    commit();
                }
            }
        });
    }

    private boolean checkOut() {
        if (hashMap.size() == 0) {
            showMaterialDialog("提示", "您还未完成评分");
            return false;
        }
        String content = et_content.getText().toString().trim();
        if (content.length() == 0) {
            showMaterialDialog("提示", "请填写评价内容");
            return false;
        }
        return true;
    }

    private void commit() {
        String content = et_content.getText().toString();
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/evaluate/submission/" + submissionId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("evaluateRelation.id", submissionRelationId);
        map.put("evaluateRelation.relation.id", leceId);
        map.put("comment", content);
        String userId = getUserId();
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            String scoreId = "evaluateItemSubmissionMap[" + entry.getKey() + "].score";
            String creatorId = "evaluateItemSubmissionMap[" + entry.getKey() + "].creator.id";
            map.put(scoreId, String.valueOf(entry.getValue()));
            map.put(creatorId, userId);
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    toast(context, "提交失败");
                }
            }
        }, map));
    }
}
