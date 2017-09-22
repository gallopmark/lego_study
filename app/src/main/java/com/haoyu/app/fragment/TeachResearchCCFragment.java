package com.haoyu.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.activity.TeachingResearchCCActivity;
import com.haoyu.app.adapter.TeachingCCAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.entity.TeachingLessonListResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/8/15 on 10:35
 * 描述:教研创课
 * 作者:马飞奔 Administrator
 */
public class TeachResearchCCFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.emptyView)
    TextView emptyView;
    private List<TeachingLessonEntity> mDatas = new ArrayList<>();
    private TeachingCCAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private int page = 1;
    private int selected = -1;

    @Override
    public int createView() {
        return R.layout.fragment_teach_research;
    }

    @Override
    public void initView(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new TeachingCCAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        emptyView.setText(getResources().getString(R.string.gen_class_emptylist));
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/lesson/cmts?discussionRelations[0].relation.id=cmts"
                + "&discussionRelations[0].relation.type=lesson&page=" + page + "&orders=CREATE_TIME.DESC";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TeachingLessonListResult>() {
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
            public void onResponse(TeachingLessonListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmLessons() != null
                        && response.getResponseData().getmLessons().size() > 0) {
                    updateUI(response.getResponseData().getmLessons(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<TeachingLessonEntity> list, Paginator paginator) {
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
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                selected = position - 1;
                if (selected >= 0 && selected < mDatas.size()) {
                    String id = mDatas.get(selected).getId();
                    Intent intent = new Intent(context, TeachingResearchCCActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("remainDay", mDatas.get(selected).getRemainDay());
                    if (mDatas.get(position - 1).getmDiscussionRelations() != null && mDatas.get(position - 1).getmDiscussionRelations().size() > 0) {
                        intent.putExtra("relationId", mDatas.get(position - 1).getmDiscussionRelations().get(0).getId());
                    }
                    startActivity(intent);
                }
            }
        });
        adapter.setRequestClickCallBack(new TeachingCCAdapter.RequestClickCallBack() {
            @Override
            public void support(TeachingLessonEntity entity, int position) {
                if (entity.isSupport())
                    toast("您已点赞过");
                else
                    createLike(position);
            }

            @Override
            public void giveAdvice(TeachingLessonEntity entity, int position) {
                showInputDialog(position);
            }
        });
    }

    private void createLike(final int position) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        final String entityId = mDatas.get(position).getId();
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", entityId);
        map.put("relation.type", "discussion");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            @Override
            public void onError(Request request, Exception exception) {
                onNetWorkError();
            }

            @Override
            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    if (mDatas.get(position).getmDiscussionRelations() != null && mDatas.get(position).getmDiscussionRelations().size() > 0) {
                        int supportNum = mDatas.get(position).getmDiscussionRelations().get(0).getSupportNum() + 1;
                        mDatas.get(position).getmDiscussionRelations().get(0).setSupportNum(supportNum);
                        adapter.notifyDataSetChanged();
                    }
                    mDatas.get(position).setSupport(true);
                } else if (response != null && response.getResponseMsg() != null) {
                    mDatas.get(position).setSupport(true);
                    toast("您已点赞过");
                } else {
                    toast("点赞失败");
                }
            }
        }, map));
    }

    private void showInputDialog(final int position) {
        CommentDialog dialog = new CommentDialog(context);
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                giveAdvice(content, position);
            }
        });
    }

    private void giveAdvice(String content, final int position) {
        if (mDatas.get(position).getmDiscussionRelations() != null
                && mDatas.get(position).getmDiscussionRelations().size() > 0) {
            String relationId = mDatas.get(position).getmDiscussionRelations().get(0).getId();
            String url = Constants.OUTRT_NET + "/m/discussion/post";
            Map<String, String> map = new HashMap<>();
            map.put("discussionUser.discussionRelation.id", relationId);
            map.put("content", content);
            addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
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
                public void onResponse(ReplyResult response) {
                    hideTipDialog();
                    if (response != null && response.getResponseData() != null) {
                        if (mDatas.get(position).getmDiscussionRelations() != null
                                && mDatas.get(position).getmDiscussionRelations().size() > 0) {
                            int replyNum = mDatas.get(position).getmDiscussionRelations().get(0).getReplyNum() + 1;
                            mDatas.get(position).getmDiscussionRelations().get(0).setReplyNum(replyNum);
                            adapter.notifyDataSetChanged();
                            toastFullScreen("发表成功", true);
                        }
                    }
                }
            }, map));
        }
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
    public void obBusEvent(MessageEvent event) {
        if (event.getAction().equals(Action.CREATE_GEN_CLASS) && event.obj != null && event.obj instanceof TeachingLessonEntity) {  //创建创课
            if (xRecyclerView.getVisibility() == View.GONE) {
                xRecyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
            TeachingLessonEntity entity = (TeachingLessonEntity) event.obj;
            mDatas.add(0, entity);
            adapter.notifyDataSetChanged();
        } else if (event.getAction().equals(Action.DELETE_GEN_CLASS)) {   //删除创课
            mDatas.remove(selected);
            adapter.notifyDataSetChanged();
            if (mDatas.size() == 0) {
                xRecyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        } else if (event.getAction().equals(Action.ALTER_GEN_CLASS)) {   //修改创课
            Bundle bundle = event.getBundle();
            if (bundle != null) {
                String title = bundle.getString("title");
                String content = bundle.getString("content");
                mDatas.get(selected).setTitle(title);
                mDatas.get(selected).setContent(content);
                adapter.notifyDataSetChanged();
            }
        } else if (event.getAction().equals(Action.SUPPORT_STUDY_CLASS)) {    //创课点赞
            if (mDatas.get(selected).getmDiscussionRelations() != null && mDatas.get(selected).getmDiscussionRelations().size() > 0) {
                int supportNum = mDatas.get(selected).getmDiscussionRelations().get(0).getSupportNum() + 1;
                mDatas.get(selected).getmDiscussionRelations().get(0).setSupportNum(supportNum);
                adapter.notifyDataSetChanged();
            }
        } else if (event.getAction().equals(Action.GIVE_STUDY_ADVICE)) {   //创课提建议
            if (mDatas.get(selected).getmDiscussionRelations() != null && mDatas.get(selected).getmDiscussionRelations().size() > 0) {
                int replyNum = mDatas.get(selected).getmDiscussionRelations().get(0).getReplyNum() + 1;
                mDatas.get(selected).getmDiscussionRelations().get(0).setReplyNum(replyNum);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
