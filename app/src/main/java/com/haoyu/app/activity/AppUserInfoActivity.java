package com.haoyu.app.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.fragment.UserInfoFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/2/27 on 13:42
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppUserInfoActivity extends BaseActivity {
    private AppUserInfoActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;       //返回按钮
    @BindView(R.id.loadingView)
    LoadingView loadingView;   //正在加载视图
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;  //加载失败视图
    @BindView(R.id.tv_empty)
    TextView tv_empty;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        toolBar.setTitle_text("个人信息");
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
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {
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
            public void onResponse(UserInfoResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                } else {
                    tv_empty.setText("没有查询到个人信息");
                }
            }

        }));
    }

    private void updateUI(MobileUser user) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }
}
