package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.SurveyResultAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.AppSurveyResult;
import com.haoyu.app.entity.SurveyAnswer;
import com.haoyu.app.entity.SurveyAnswerSubmissionResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2016/12/28 on 9:59
 * 描述:  1.3	查看问卷调查统计信息
 * 作者:马飞奔 Administrator
 */
public class AppSurveyResultActivity extends BaseActivity {
    private AppSurveyResultActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_survey_title)
    TextView tv_survey_title;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private SurveyResultAdapter adapter;
    private List<SurveyAnswer> mDatas = new ArrayList<>();
    private Map<String, Map<String, Integer>> dateMap = new HashMap<>();
    private String surveyType, relationId, activityId, surveyId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_survey_result;
    }

    @Override
    public void initView() {
        surveyType = getIntent().getStringExtra("type");
        relationId = getIntent().getStringExtra("relationId");
        activityId = getIntent().getStringExtra("activityId");
        surveyId = getIntent().getStringExtra("surveyId");
        String surveyTitle = getIntent().getStringExtra("surveyTitle");
        tv_survey_title.setText(surveyTitle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SurveyResultAdapter(context, mDatas, dateMap);
        recyclerView.setAdapter(adapter);
    }

    /*查看问卷调查统计信息*/
    public void initData() {
        String url;
        if (surveyType != null && surveyType.equals("course")) {
            url = Constants.OUTRT_NET + "/" + activityId + "/study/m/survey_result/" + relationId + "/" + surveyId;
        } else {
            url = Constants.OUTRT_NET + "/student_" + relationId + "/m/survey_result/" + relationId + "/" + surveyId;
        }
        loadingView.setVisibility(View.VISIBLE);
        addSubscription(Flowable.just(url).map(new Function<String, AppSurveyResult>() {
            @Override
            public AppSurveyResult apply(String url) throws Exception {
                return getResult(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<AppSurveyResult>() {
            @Override
            public void accept(AppSurveyResult appSurveyResult) throws Exception {
                loadingView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                if (appSurveyResult != null && appSurveyResult.getResponseData() != null && appSurveyResult.getResponseData().getChoiceInteractionResults() != null) {
                    adapter.setParticipateNum(appSurveyResult.getResponseData().getParticipateNum());
                    updateUI(appSurveyResult.getResponseData().getmSurveyQuestions(), appSurveyResult.getResponseData().getChoiceInteractionResults());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }
        }));
    }

    private AppSurveyResult getResult(String url) throws Exception {
        Gson gson = new Gson();
        String resultStr = OkHttpClientManager.getAsString(context, url);
        AppSurveyResult appSurveyResult = gson.fromJson(resultStr, AppSurveyResult.class);
        if (appSurveyResult != null && appSurveyResult.getResponseData() != null
                && appSurveyResult.getResponseData().getmSurveyQuestions() != null) {
            for (int i = 0; i < appSurveyResult.getResponseData().getmSurveyQuestions().size(); i++) {   //如果是问答题则获取问答题答案列表
                try {
                    if (appSurveyResult.getResponseData().getmSurveyQuestions().get(i).getType().equals(SurveyAnswer.textEntry)) {
                        String questionId = appSurveyResult.getResponseData().getmSurveyQuestions().get(i).getId();
                        String mUrl;
                        if (surveyType != null && surveyType.equals("course"))
                            mUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/survey_result/" + questionId + "/submissions" + "?page=1&limit=2";
                        else
                            mUrl = Constants.OUTRT_NET + "/student_" + relationId + "/m/survey_result/" + questionId + "/submissions" + "?page=1&limit=2";
                        String mSubmissionStr = OkHttpClientManager.getAsString(context, mUrl);
                        SurveyAnswerSubmissionResult submissionResult = gson.fromJson(mSubmissionStr, SurveyAnswerSubmissionResult.class);
                        if (submissionResult != null && submissionResult.getResponseData() != null) {
                            appSurveyResult.getResponseData().getmSurveyQuestions().get(i).setAnswerSubmissionData(submissionResult.getResponseData());
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return appSurveyResult;
    }

    private void updateUI(List<SurveyAnswer> surveyAnswers, Map<String, Map<String, Integer>> dateMap) {
        adapter.addAll(surveyAnswers, dateMap);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setListener() {
        toolBar.setOnRightClickListener(new AppToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                finish();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        adapter.setAnswerCallBack(new SurveyResultAdapter.ExpandAnswerCallBack() {
            @Override
            public void expand(String questionId, int position) {
                Intent intent = new Intent(context, AppSurveyAnswerActivity.class);
                intent.putExtra("type", surveyType);
                intent.putExtra("questionId", questionId);
                intent.putExtra("workShopId", relationId);
                intent.putExtra("activityId", activityId);
                startActivity(intent);
            }
        });
    }

}
