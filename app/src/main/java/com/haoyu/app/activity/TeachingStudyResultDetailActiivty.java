package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.TeachingStudyDetailAdaper;
import com.haoyu.app.adapter.TeachingStudyResultDetial;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.EvaluateItemResult;
import com.haoyu.app.entity.MEvaluateEntity;
import com.haoyu.app.entity.MEvaluateResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
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
 * 查看听课评课的结果
 */
public class TeachingStudyResultDetailActiivty extends BaseActivity {
    private TeachingStudyResultDetailActiivty context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.recyclerView2)
    RecyclerView recyclerView2;
    @BindView(R.id.tv_count)
    TextView tv_count;
    private String workshopId;
    private String lcecId;
    private TeachingStudyDetailAdaper evaluateAdapter;//听课评课评价内容列表
    private TeachingStudyResultDetial detailAdaper;//听课评课评价总结及建议
    private List<EvaluateItemResult.EvaluateItemResponse> submissionsList = new ArrayList<>();//评价列表集合
    private List<MEvaluateEntity> evaluateEntityList = new ArrayList<>();//听课评课总结及建议集合
    @BindView(R.id.tv_warn)
    TextView tv_warn;
    @BindView(R.id.tv_warn2)
    TextView tv_warn2;
    @BindView(R.id.tv_more)
    TextView tv_more;
    @BindView(R.id.scrollview)
    ScrollView scrollview;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    @Override
    public int setLayoutResID() {
        return R.layout.teaching_study_result;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        lcecId = getIntent().getStringExtra("leceId");
        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
        manager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        FullyLinearLayoutManager manager2 = new FullyLinearLayoutManager(context);
        manager2.setOrientation(FullyLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView2.setLayoutManager(manager2);
        evaluateAdapter = new TeachingStudyDetailAdaper(submissionsList);
        detailAdaper = new TeachingStudyResultDetial(context, evaluateEntityList);
        recyclerView.setAdapter(evaluateAdapter);
        recyclerView2.setAdapter(detailAdaper);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/result";
        final String url2 = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/" + lcecId + "/submissions";
        addSubscription(Flowable.just(url).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return init(s) != null && initData2(url2) != null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override

            public void accept(Boolean response) {
                loadingView.setVisibility(View.GONE);
                if (response) {
                    loadFailView.setVisibility(View.GONE);
                    scrollview.setVisibility(View.VISIBLE);
                    evaluateAdapter.notifyDataSetChanged();
                    detailAdaper.notifyDataSetChanged();
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
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

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        tv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EValuationSummaryActivity.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("leceId", lcecId);
                startActivity(intent);
            }
        });
        evaluateAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, TeachingStudyDetailScoreActivity.class);
                EvaluateItemResult.EvaluateItemResponse evaluateEntity = submissionsList.get(position);
                if (evaluateEntity != null && evaluateEntity.getId() != null) {
                    intent.putExtra("itemId", evaluateEntity.getId());
                }
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("leceId", lcecId);
                startActivity(intent);

            }
        });

        loadFailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.setVisibility(View.VISIBLE);
                initData();
            }
        });
    }


    //查看评价结果
    public EvaluateItemResult init(String url) throws Exception {
        String strJson = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        EvaluateItemResult response = gson.fromJson(strJson, EvaluateItemResult.class);
        if (response != null && response.getResponseData() != null && response.isSuccess()) {
            submissionsList.addAll(response.getResponseData());
            double d = 0;
            for (int i = 0; i < submissionsList.size(); i++) {
                d += submissionsList.get(i).getAvgScore();
            }

            if (submissionsList.size() > 0) {
                tv_score.setText(String.valueOf((int) (d / submissionsList.size())) + "分");
            } else {
                tv_score.setText("0分");
                tv_warn.setVisibility(View.VISIBLE);
            }

        }
        return response;

    }

    //评价总结建议列表
    private MEvaluateResult initData2(String url) throws Exception {
        Gson gson = new GsonBuilder().create();
        String strJson = OkHttpClientManager.getAsString(context, url);
        MEvaluateResult response = gson.fromJson(strJson, MEvaluateResult.class);
        if (response != null && response.getResponseData() != null && response.isSuccess()) {
            tv_warn2.setVisibility(View.GONE);
            if (response.getResponseData().getPaginator() != null) {
                tv_count.setText("（共有" + response.getResponseData().getPaginator().getTotalCount() + "条信息）");
            }
            evaluateEntityList.addAll(response.getResponseData().getmEvaluateSubmissions());
            if (response.getResponseData().getPaginator() != null && response.getResponseData().getPaginator().getHasNextPage()) {
                tv_more.setVisibility(View.VISIBLE);
            }
        } else {
            tv_warn2.setVisibility(View.VISIBLE);
        }
        return response;

    }

}
