package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.GridUserAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserData;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/7.
 * 描述:搜索用户
 * 作者:xiaoma
 */

public class SearchUsersActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private SearchUsersActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.iv_search)
    ImageView iv_search;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_user)
    TextView tv_user;
    @BindView(R.id.rl_result)
    RelativeLayout rl_result;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private List<MobileUser> mDatas = new ArrayList<>();
    private GridUserAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private String userName;
    private int page = 1, limit = 30;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_searchuser;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        toolBar.setTitle_text("选择授课人");
        tv_current.setText("当前授课人：");
        tv_user.setText(getRealName());
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new GridUserAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.setLoadingListener(context);
    }

    private void setToolBar() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void setListener() {
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
                userName = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    showMaterialDialog("提示", "请输入姓名");
                } else {
                    searchUsers();
                }
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                int selected = position - 1;
                if (selected >= 0 && selected < mDatas.size()) {
                    MobileUser user = mDatas.get(selected);
                    Intent intent = new Intent();
                    intent.putExtra("user", user);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void init() {
        Common.hideSoftInput(context, et_name);
        isRefresh = true;
        isLoadMore = false;
        page = 1;
    }

    private void searchUsers() {
        String url = Constants.OUTRT_NET + "/m/user?paramMap[realName]=" + userName + "&page=" + page + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUserData>>() {
            @Override
            public void onBefore(Request request) {
                if (!isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult<MobileUserData> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmUsers().size() > 0) {
                    updateUI(response.getResponseData().getmUsers(), response.getResponseData().getPaginator());
                } else {
                    if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                        xRecyclerView.setLoadingMoreEnabled(false);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<MobileUser> users, Paginator paginator) {
        if (rl_result.getVisibility() != View.VISIBLE) {
            rl_result.setVisibility(View.VISIBLE);
        }
        if (isRefresh) {
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(users);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        searchUsers();
    }
}
