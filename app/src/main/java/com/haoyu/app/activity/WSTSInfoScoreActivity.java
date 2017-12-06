package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.WSTSInfoScoreData;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价项得分明细
 * 作者:xiaoma
 */

public class WSTSInfoScoreActivity extends BaseActivity {
    private WSTSInfoScoreActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.rl_content)
    RelativeLayout rl_content;
    @BindView(R.id.tv_total)
    TextView tv_total;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private String workshopId, leceId, itemId;
    private List<Double> mDatas = new ArrayList<>();
    private WSTSInfoScoreAdapter adapter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstsinfoscore;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        workshopId = getIntent().getStringExtra("workshopId");
        leceId = getIntent().getStringExtra("leceId");
        itemId = getIntent().getStringExtra("itemId");
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new WSTSInfoScoreAdapter(mDatas);
        recyclerView.setAdapter(adapter);
    }

    private void setToolBar() {
        toolBar.setTitle_text("得分明细");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/lcec/ " + leceId + "/" + itemId + "/score_detail";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<WSTSInfoScoreData>>() {
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
            public void onResponse(BaseResponseResult<WSTSInfoScoreData> response) {
                loadingView.setVisibility(View.GONE);
                rl_content.setVisibility(View.VISIBLE);
                updateUI(response);
            }
        }));
    }

    private void updateUI(BaseResponseResult<WSTSInfoScoreData> response) {
        if (response != null && response.getResponseData() != null) {
            setText_total(response.getResponseData().getTotalSubmission());
        }
        if (response != null && response.getResponseData().getScoreDetail().size() > 0) {
            updateDatas(response.getResponseData().getScoreDetail());
        } else {
            recyclerView.setVisibility(View.GONE);
            tv_empty.setVisibility(View.GONE);
        }
    }

    private void setText_total(int totalSubmission) {
        String text = "共" + totalSubmission + "人参与评分，评分如下：";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("共") + 1;
        int end = text.indexOf("人");
        int color = ContextCompat.getColor(context, R.color.defaultColor);
        ss.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_total.setText(ss);
    }

    private void updateDatas(List<Double> list) {
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private class WSTSInfoScoreAdapter extends BaseArrayRecyclerAdapter<Double> {

        private int width;
        private int dp_8;

        public WSTSInfoScoreAdapter(List<Double> mDatas) {
            super(mDatas);
            width = ScreenUtils.getScreenWidth(context) / 4;
            dp_8 = PixelFormat.dp2px(context, 8);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.wstsinfoscore_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final Double score, int position) {
            TextView tv_score = holder.obtainView(R.id.tv_score);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv_score.getLayoutParams();
            params.width = width;
            tv_score.setLayoutParams(params);
            tv_score.setPadding(0, dp_8, 0, dp_8);
            tv_score.setText((int) score.doubleValue() + "分");
        }
    }
}
