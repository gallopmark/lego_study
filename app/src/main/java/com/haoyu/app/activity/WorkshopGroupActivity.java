package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.haoyu.app.adapter.WorkShopGroupAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.MyTrainMobileEntity;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WorkShopListResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/8 on 11:38
 * 描述: 工作坊列表界面
 * 作者:马飞奔 Administrator
 */
public class WorkshopGroupActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private WorkshopGroupActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rb_related)
    RadioButton rb_related;
    @BindView(R.id.rb_all)
    RadioButton rb_all;   //与我相关
    @BindView(R.id.xRelatedView)
    XRecyclerView xRelatedView;
    @BindView(R.id.xAllView)
    XRecyclerView xAllView;
    private List<WorkShopMobileEntity> relatedList = new ArrayList<>();
    private List<WorkShopMobileEntity> allList = new ArrayList<>();
    private WorkShopGroupAdapter relatedAdapter, allAdapter;
    private boolean isLoadRelated, isLoadAll;
    private int page0 = 1, page1 = 1;
    private int checkIndex = 0;
    private boolean isRefresh, isLoadMore;
    private Map<String, WorkShopMobileUser> allUserMap = new HashMap<>();

    @Override
    public int setLayoutResID() {
        return R.layout.activity_workshop_group;
    }

    @Override
    public void initView() {
        LinearLayoutManager relatedManager = new LinearLayoutManager(context);
        relatedManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRelatedView.setArrowImageView(R.drawable.refresh_arrow);
        xRelatedView.setLayoutManager(relatedManager);
        LinearLayoutManager allManager = new LinearLayoutManager(context);
        allManager.setOrientation(LinearLayoutManager.VERTICAL);
        xAllView.setArrowImageView(R.drawable.refresh_arrow);
        xAllView.setLayoutManager(allManager);
        relatedAdapter = new WorkShopGroupAdapter(context, relatedList);
        xRelatedView.setAdapter(relatedAdapter);
        xRelatedView.setLoadingListener(context);
        allAdapter = new WorkShopGroupAdapter(context, allList);
        xAllView.setAdapter(allAdapter);
        xAllView.setLoadingListener(context);
        setTabSelection(checkIndex);
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_related:
                        checkIndex = 0;
                        break;
                    case R.id.rb_all:
                        checkIndex = 1;
                        break;
                }
                setTabSelection(checkIndex);
            }
        });

        relatedAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0) {
                    WorkShopMobileEntity entity = relatedList.get(position - 1);
                    getTrainInfo(entity);
                }
            }
        });

        allAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0) {
                    WorkShopMobileEntity entity = allList.get(position - 1);
                    String workshopId = entity.getId();
                    if (allUserMap.get(workshopId) != null && allUserMap.get(workshopId).getRole() != null) {
                        getTrainInfo(entity);
                    } else {
                        showDialog();
                    }
                }
            }
        });
    }

    private void getTrainInfo(final WorkShopMobileEntity entity) {
        if (entity.getRelation() != null && entity.getRelation().getId() != null) {
            String url = Constants.OUTRT_NET + "/m/train/" + entity.getRelation().getId();
            addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MyTrainMobileEntity>>() {
                @Override
                public void onBefore(Request request) {
                    showTipDialog();
                }

                @Override
                public void onError(Request request, Exception e) {
                    hideTipDialog();
                }

                @Override
                public void onResponse(BaseResponseResult<MyTrainMobileEntity> response) {
                    hideTipDialog();
                    Intent intent = new Intent(context, WorkshopHomeActivity.class);
                    intent.putExtra("workshopId", entity.getId());
                    intent.putExtra("workshopTitle", entity.getTitle());
                    if (response != null && response.getResponseData() != null && response.getResponseData().getmTrainingTime() != null
                            && response.getResponseData().getmTrainingTime().getState() != null
                            && response.getResponseData().getmTrainingTime().getState().equals("进行中"))
                        intent.putExtra("training", true);
                    else
                        intent.putExtra("training", false);
                    startActivity(intent);
                }
            }));
        } else {
            showMaterialDialog("提示", "找不到培训相关信息，无法进入工作坊学习！");
        }
    }

    private void showDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("您不是坊内成员无权查看内容，请查看与我相关的工作坊");
        dialog.setPositiveButton("我知道了", null);
        dialog.show();
    }

    private void setTabSelection(int checkIndex) {
        isRefresh = false;
        isLoadMore = false;
        xRelatedView.setVisibility(View.GONE);
        xAllView.setVisibility(View.GONE);
        switch (checkIndex) {
            case 0:
                xRelatedView.setVisibility(View.VISIBLE);
                if (!isLoadRelated) {
                    loadData();
                }
                break;
            case 1:
                xAllView.setVisibility(View.VISIBLE);
                if (!isLoadAll) {
                    loadData();
                }
                break;
        }
    }

    public void loadData() {
        if (!isRefresh && !isLoadMore) {
            showTipDialog();
        }
        String url;
        if (checkIndex == 0) {
            url = Constants.OUTRT_NET + "/m/workshop?type=my" + "&page=" + page0;
        } else {
            url = Constants.OUTRT_NET + "/m/workshop?type=all" + "&page=" + page1;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkShopListResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                response(false);
            }

            @Override
            public void onResponse(WorkShopListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    rb_related.setText("与我相关" + "（" + getCount(response.getResponseData().getMyRelativeNum()) + "）");
                    rb_all.setText("全部" + "（" + getCount(response.getResponseData().getNotTemplateNum()) + "）");
                }
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshops() != null
                        && response.getResponseData().getmWorkshops().size() > 0) {
                    updateUI(response.getResponseData().getmWorkshops(), response.getResponseData().getmWorkshopUserMap(), response.getResponseData().getPaginator());
                } else {
                    response(true);
                }
            }
        }));
    }

    private String getCount(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        }
        double num = (double) count / 10000;
        BigDecimal bd = new BigDecimal(num);
        num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (num < 10000) {
            if (num / 1000 > 1) {
                bd = new BigDecimal(num / 1000);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "千万";
            } else if (num / 100 > 1) {
                bd = new BigDecimal(num / 100);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "百万";
            } else if (num / 10 > 1) {
                bd = new BigDecimal(num / 10);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "十万";
            }
            return num + "万";
        }
        return "大于1亿";
    }

    private void response(boolean success) {
        if (success) {
            switch (checkIndex) {
                case 0:
                    isLoadRelated = true;
                    if (isRefresh) {
                        xRelatedView.refreshComplete(success);
                    } else if (isLoadMore) {
                        xRelatedView.loadMoreComplete(success);
                    }
                    break;
                case 1:
                    isLoadAll = true;
                    if (isRefresh) {
                        xAllView.refreshComplete(success);
                    } else if (isLoadMore) {
                        xAllView.loadMoreComplete(success);
                    }
                    break;
            }
        } else {
            switch (checkIndex) {
                case 0:
                    isLoadRelated = false;
                    if (isRefresh) {
                        xRelatedView.refreshComplete(success);
                    } else if (isLoadMore && !success) {
                        page0 -= 1;
                        xRelatedView.loadMoreComplete(success);
                    }
                    break;
                case 1:
                    isLoadAll = false;
                    if (isRefresh) {
                        xAllView.refreshComplete(success);
                    } else if (isLoadMore) {
                        page1 -= 1;
                        xAllView.loadMoreComplete(success);
                    }
                    break;
            }
        }
    }

    private void updateUI(List<WorkShopMobileEntity> workshops, Map<String, WorkShopMobileUser> userMap, Paginator paginator) {
        switch (checkIndex) {
            case 0:
                isLoadRelated = true;
                if (isRefresh) {
                    xRelatedView.refreshComplete(true);
                    relatedList.clear();
                } else if (isLoadMore) {
                    xRelatedView.loadMoreComplete(true);
                }
                relatedList.addAll(workshops);
                relatedAdapter.notifyDataSetChanged();
                if (paginator != null && paginator.getHasNextPage()) {
                    xRelatedView.setLoadingMoreEnabled(true);
                } else {
                    xRelatedView.setLoadingMoreEnabled(false);
                }
                break;
            case 1:
                isLoadAll = true;
                if (isRefresh) {
                    xAllView.refreshComplete(true);
                    allUserMap.clear();
                    allList.clear();
                } else if (isLoadMore) {
                    xAllView.loadMoreComplete(true);
                }
                allUserMap.putAll(userMap);
                allList.addAll(workshops);
                allAdapter.notifyDataSetChanged();
                if (paginator != null && paginator.getHasNextPage()) {
                    xAllView.setLoadingMoreEnabled(true);
                } else {
                    xAllView.setLoadingMoreEnabled(false);
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        if (checkIndex == 0) {
            page0 = 1;
        } else {
            page1 = 1;
        }
        loadData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        if (checkIndex == 0) {
            page0 += 1;
        } else {
            page1 += 1;
        }
        loadData();
    }
}
