package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.haoyu.app.adapter.PeerAdapter;
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

public class SearchUsersActivity extends BaseActivity implements XRecyclerView.LoadingListener, View.OnClickListener {
    private SearchUsersActivity context;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_clear)
    TextView tv_clear;
    @BindView(R.id.tv_search)
    TextView tv_search;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private List<MobileUser> mDatas = new ArrayList<>();
    private PeerAdapter adapter;
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
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new PeerAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.setLoadingListener(context);
    }

    @Override
    public void setListener() {
        tv_cancel.setOnClickListener(context);
        tv_search.setOnClickListener(context);
        tv_clear.setOnClickListener(context);
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    tv_clear.setVisibility(View.GONE);
                } else {
                    tv_clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_clear:
                et_name.setText(null);
                break;
            case R.id.tv_search:
                init();
                userName = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    showMaterialDialog("提示", "请输入姓名");
                } else {
                    searchUsers();
                }
                break;
        }
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);//用于屏蔽 activity 默认的转场动画效果
    }
}
