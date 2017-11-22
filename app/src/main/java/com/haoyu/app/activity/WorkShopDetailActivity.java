package com.haoyu.app.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.WorkShopDetailResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.fragment.WSDetailFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2016/12/29 on 20:29
 * 描述: 工作坊简介页面
 * 作者:马飞奔 Administrator
 */
public class WorkShopDetailActivity extends BaseActivity {
    private WorkShopDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private String workshopId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        toolBar.setTitle_text("工作坊简介");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/workshop/" + workshopId + "/detail";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkShopDetailResult>() {
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
            public void onResponse(WorkShopDetailResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshop() != null) {
                    updateUI(response.getResponseData().getmWorkshop(), response.getResponseData().getmFileInfo());
                } else {
                    tv_empty.setText("暂无简介");
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(WorkShopMobileEntity entity, MFileInfo mFileInfo) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        WSDetailFragment fragment = new WSDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        bundle.putSerializable("fileInfo", mFileInfo);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
    }
}
