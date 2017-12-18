package com.haoyu.app.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.activity.AppQuestionDetailActivity;
import com.haoyu.app.activity.AppQuestionEditActivity;
import com.haoyu.app.adapter.PageQuestionAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FAQsEntity;
import com.haoyu.app.entity.FAQsListResult;
import com.haoyu.app.entity.FollowMobileEntity;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RippleView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * 创建日期：2017/8/16 on 10:34
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageNoticeQuestionFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    private LoadingView loadingView;
    private LoadFailView loadFailView;
    private XRecyclerView xRecyclerView;
    private TextView emptyView;
    private List<FAQsEntity> mDatas = new ArrayList<>();
    private PageQuestionAdapter adapter;
    private String relationId;
    private int page = 1;
    private boolean isRefresh, isLoadMore;

    @Override
    public int createView() {
        return R.layout.fragment_page_question_child;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) relationId = bundle.getString("relationId");
        loadingView = view.findViewById(R.id.loadingView);
        loadFailView = view.findViewById(R.id.loadFailView);
        xRecyclerView = view.findViewById(R.id.xRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new PageQuestionAdapter(context, mDatas, 1);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        emptyView.setText(getResources().getString(R.string.empty_follow));
        registRxBus();
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/faq_question" + "?relation.id=" + relationId + "&relation.type=course_study" + "&page=" + page + "&orders=CREATE_TIME.DESC" + "&follow.creator.id=" + getUserId() + "&isLoadNewstAnswer=true";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<FAQsListResult>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore) {
                    loadingView.setVisibility(View.GONE);
                } else {
                    loadingView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    xRecyclerView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(FAQsListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getQuestions() != null && response.getResponseData().getQuestions().size() > 0) {
                    updateUI(response.getResponseData().getQuestions(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setLoadingMoreEnabled(false);
                        xRecyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<FAQsEntity> list, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        adapter.setCollectCallBack(new PageQuestionAdapter.CollectCallBack() {
            @Override
            public void collect(int position, FAQsEntity entity) {
            }

            @Override
            public void cancelCollect(int position, FAQsEntity entity) {
                cancelCollection(position);
            }
        });
        adapter.setAnswerCallBack(new PageQuestionAdapter.AnswerCallBack() {
            @Override
            public void answer(int position, FAQsEntity entity) {
                Intent intent = new Intent();
                intent.setClass(context, AppQuestionEditActivity.class);
                intent.putExtra("isAnswer", true);
                intent.putExtra("questionId", entity.getId());
                startActivity(intent);
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                if (position - 1 >= 0 && position - 1 < mDatas.size()) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), AppQuestionDetailActivity.class);
                    intent.putExtra("type", "course");
                    intent.putExtra("entity", mDatas.get(position - 1));
                    startActivity(intent);
                }
            }
        });
        adapter.setOnItemLongClickListener(new PageQuestionAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(FAQsEntity entity, int position) {
                if (entity.getCreator() != null && entity.getCreator().getId() != null && entity.getCreator().getId().equals(getUserId()))
                    bottomDialog(entity);
            }
        });
    }

    /**
     * 取消收藏
     *
     * @param position
     */
    private void cancelCollection(final int position) {
        FollowMobileEntity follow = mDatas.get(position).getFollow();
        if (follow != null) {
            String url = Constants.OUTRT_NET + "/m/follow/" + follow.getId();
            Map<String, String> map = new HashMap<>();
            map.put("_method", "delete");
            addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
                @Override
                public void onError(Request request, Exception exception) {
                    onNetWorkError();
                }

                @Override
                public void onResponse(BaseResponseResult response) {
                    if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                        mDatas.get(position).setFollow(null);
                        MessageEvent event = new MessageEvent();
                        event.action = Action.COLLECTION;
                        event.obj = mDatas.get(position);
                        RxBus.getDefault().post(event);
                    }
                }
            }, map));
        }
    }

    private void bottomDialog(final FAQsEntity entity) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        RippleView rv_delete = view.findViewById(R.id.rv_delete);
        RippleView rv_cancel = view.findViewById(R.id.rv_cancel);
        RippleView.OnRippleCompleteListener listener = new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView view) {
                switch (view.getId()) {
                    case R.id.rv_delete:
                        deleteQuestion(entity);
                        break;
                    case R.id.rv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        rv_delete.setOnRippleCompleteListener(listener);
        rv_cancel.setOnRippleCompleteListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void deleteQuestion(final FAQsEntity entity) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("温馨提示");
        dialog.setMessage("您确定删除此问答吗？");
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                String url = Constants.OUTRT_NET + "/m/faq_question/" + entity.getId();
                Map<String, String> map = new HashMap<>();
                map.put("_method", "delete");
                addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
                    @Override
                    public void onBefore(Request request) {
                        showTipDialog();
                    }

                    @Override
                    public void onError(Request request, Exception e) {
                        hideTipDialog();
                        onNetWorkError();
                    }

                    @Override
                    public void onResponse(BaseResponseResult response) {
                        hideTipDialog();
                        if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                            MessageEvent event = new MessageEvent();
                            event.action = Action.DELETE_FAQ_QUESTION;
                            event.obj = entity;
                            RxBus.getDefault().post(event);
                        }
                    }
                }, map));
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }


    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }

    @Override
    public void onEvent(MessageEvent event) {
        if (event.getAction().equals(Action.COLLECTION) && event.obj != null && event.obj instanceof FAQsEntity) {
            FAQsEntity entity = (FAQsEntity) event.obj;
            if (entity.getFollow() == null) {  //取消收藏
                if (mDatas.contains(entity)) {
                    mDatas.remove(entity);
                    adapter.notifyDataSetChanged();
                }
                if (mDatas.size() == 0) {
                    xRecyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            } else {
                if (!mDatas.contains(entity)) {
                    mDatas.add(0, entity);
                    adapter.notifyDataSetChanged();
                }
                if (xRecyclerView.getVisibility() == View.GONE) {
                    xRecyclerView.setVisibility(View.VISIBLE);
                }
                if (emptyView.getVisibility() == View.VISIBLE) {
                    emptyView.setVisibility(View.GONE);
                }
            }
        } else if (event.getAction().equals(Action.DELETE_FAQ_QUESTION) && event.obj != null && event.obj instanceof FAQsEntity) {
            FAQsEntity entity = (FAQsEntity) event.obj;
            mDatas.remove(entity);
            adapter.notifyDataSetChanged();
            if (mDatas.size() == 0) {
                xRecyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        } else if (event.getAction().equals(Action.CREATE_FAQ_ANSWER) && event.obj != null && event.obj instanceof FAQsEntity) {
            FAQsEntity entity = (FAQsEntity) event.obj;
            int index = mDatas.indexOf(entity);
            if (index >= 0) {
                int faqAnswerCount = mDatas.get(index).getFaqAnswerCount() + 1;
                mDatas.get(index).setFaqAnswerCount(faqAnswerCount);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
