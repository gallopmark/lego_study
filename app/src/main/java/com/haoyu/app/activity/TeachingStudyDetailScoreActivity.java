package com.haoyu.app.activity;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.TeachingStudyScoreDetailAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.ScoreDetailResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by acer1 on 2017/2/21.
 * 听课评课的得分明细情况
 */
public class TeachingStudyDetailScoreActivity extends BaseActivity {
    private TeachingStudyDetailScoreActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_count)
    TextView tv_count;
    private GridLayoutManager manager;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private TeachingStudyScoreDetailAdapter adapter;
    private String workshopId;
    private String leceId;
    private String itemId;
    private List<Double> detailList = new ArrayList<>();
    @BindView(R.id.tv_warn)
    TextView tv_warn;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.ll_show)
    LinearLayout ll_show;

    @Override
    public int setLayoutResID() {
        return R.layout.teaching_study_result_detail;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        leceId = getIntent().getStringExtra("leceId");
        itemId = getIntent().getStringExtra("itemId");
        adapter = new TeachingStudyScoreDetailAdapter(detailList);
        manager = new GridLayoutManager(context, 3);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
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

    //得分明细情况
    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/ " + leceId + "/" + itemId + "/score_detail";
        addSubscription(Flowable.just(url).map(new Function<String, ScoreDetailResult>() {
            @Override
            public ScoreDetailResult apply(String s) throws Exception {
                return getScoreData(s);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ScoreDetailResult>() {
            @Override
            public void accept(ScoreDetailResult response) throws Exception {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getSuccess() && response.getResponseData() != null) {
                    StringBuilder actionText = new StringBuilder();
                    actionText.append("<font color='#181818'>"
                            + "共：" + "</font>");
                    actionText.append("<font color='#62B542'>"
                            + response.getResponseData().getTotalSubmission() + "</font>");
                    actionText.append("<font color='#181818'>"
                            + "人参与评分，评分如下：" + " " + "</font>");
                    tv_count.setText(Html.fromHtml(actionText.toString()));
                    detailList.addAll(response.getResponseData().getScoreDetail());
                    adapter.notifyDataSetChanged();
                    if (detailList.size() == 0) {
                        tv_warn.setVisibility(View.VISIBLE);
                    } else {
                        tv_warn.setVisibility(View.GONE);
                    }
                    loadFailView.setVisibility(View.GONE);
                    ll_show.setVisibility(View.VISIBLE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    ll_show.setVisibility(View.GONE);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                loadFailView.setVisibility(View.VISIBLE);
                ll_show.setVisibility(View.GONE);
            }
        }));


    }

    private ScoreDetailResult getScoreData(String url) throws Exception {
        Gson gson = new GsonBuilder().create();
        String strJson = OkHttpClientManager.getAsString(context, url);
        ScoreDetailResult response = gson.fromJson(strJson, ScoreDetailResult.class);
        return response;

     /*   OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<ScoreDetailResult>() {
            @Override
            public void onError(Request request, Exception e) {
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(ScoreDetailResult response) {

                loadingView.setVisibility(View.GONE);
                if (response != null && response.getSuccess() && response.getResponseData() != null) {
                    StringBuilder actionText = new StringBuilder();
                    actionText.append("<font color='#181818'>"
                            + "共：" + "</font>");
                    actionText.append("<font color='#62B542'>"
                            + response.getResponseData().getTotalSubmission() + "</font>");
                    actionText.append("<font color='#181818'>"
                            + "人参与评分，评分如下：" + " " + "</font>");
                    tv_count.setText(Html.fromHtml(actionText.toString()));
                    detailList.addAll(response.getResponseData().getScoreDetail());
                    adapter.notifyDataSetChanged();
                    if (detailList.size() == 0) {
                        tv_warn.setVisibility(View.VISIBLE);
                    } else {
                        tv_warn.setVisibility(View.GONE);
                    }
                    loadFailView.setVisibility(View.GONE);
                    ll_show.setVisibility(View.VISIBLE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    ll_show.setVisibility(View.GONE);
                }

            }
        });*/
    }
}
