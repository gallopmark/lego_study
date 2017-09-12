package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.TeachingStudyLecturerAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * Created by acer1 on 2017/2/16.
 * 授课人
 */
public class TeachingStudyLeacturerActivity extends BaseActivity implements View.OnClickListener, XRecyclerView.LoadingListener {
    private TeachingStudyLeacturerActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.rl_search)
    RelativeLayout rl_search;
    @BindView(R.id.recyclerView)
    XRecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private TeachingStudyLecturerAdapter lecturerAdapter;
    private boolean isRefresh, isLoadMore, needDialog = true;
    private int page = 1;
    @BindView(R.id.tv_warn)
    TextView tv_warn;
    private List<MobileUser> mobileUserList = new ArrayList<>();
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    private String userName;

    @Override
    public int setLayoutResID() {
        return R.layout.teaching_study_lecturer;
    }

    @Override
    public void initView() {
        lecturerAdapter = new TeachingStudyLecturerAdapter(mobileUserList);
        gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(lecturerAdapter);
        recyclerView.setArrowImageView(R.drawable.refresh_arrow);
        recyclerView.setLoadingListener(context);
        tv_name.setText(getRealName());
    }

    @Override
    public void setListener() {
        rl_search.setOnClickListener(context);
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });

        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getUser();
            }
        });
        lecturerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0) {
                    MobileUser user = mobileUserList.get(position - 1);
                    Intent intent = new Intent();
                    intent.putExtra("user", user);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_search:
                userName = et_name.getText().toString().trim();
                if (userName.length() == 0) {
                    showMaterialDialog("提示", "请输入人名");
                } else {
                    mobileUserList.clear();
                    needDialog = true;
                    getUser();
                }
                break;
        }
    }

    private void getUser() {
        String url = Constants.OUTRT_NET + "/m/user?id=" + getUserId() + "&paramMap[realName]=" + userName + "&page=" + page + "&limit=30";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<MobileUserResult>() {
            @Override
            public void onBefore(Request request) {
                if (needDialog) {
                    showTipDialog();
                }
                tv_warn.setVisibility(View.GONE);
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(MobileUserResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmUsers() != null
                        && response.getResponseData().getmUsers().size() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData().getmUsers(), response.getResponseData().getPaginator());
                } else {
                    tv_warn.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(List<MobileUser> mDatas, Paginator paginator) {
        if (isRefresh) {
            mobileUserList.clear();
            recyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            recyclerView.loadMoreComplete(true);
        }
        mobileUserList.addAll(mDatas);
        lecturerAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            recyclerView.setLoadingMoreEnabled(true);
        } else {
            recyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void onRefresh() {
        page = 1;
        isRefresh = true;
        isLoadMore = false;
        needDialog = false;
        getUser();
    }

    @Override
    public void onLoadMore() {
        page += 1;
        isRefresh = false;
        isLoadMore = true;
        needDialog = false;
        getUser();
    }

}
