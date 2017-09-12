package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.MessageAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.Message;
import com.haoyu.app.entity.Messages;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/8 on 14:50
 * 描述: 消息页面
 * 作者:马飞奔 Administrator
 */
public class MessageActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private MessageActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.empty_message)
    TextView empty_message;
    private List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private int page = 1;
    private boolean isRefresh, isLoadMore;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_message;
    }

    @Override
    public void initView() {
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(context, messages);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    public void initData() {
        if (!isRefresh && !isLoadMore) {
            showTipDialog();
        }
        String url = Constants.OUTRT_NET + "/m/message?page=" + page + "&limit=20";
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<Messages>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                }
            }

            @Override
            public void onResponse(Messages response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmMessages() != null
                        && response.getResponseData().getmMessages().size() > 0) {
                    updateUI(response.getResponseData().getmMessages(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    }
                    empty_message.setVisibility(View.VISIBLE);
                    xRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateUI(List<Message> messageList, Paginator paginator) {
        if (isRefresh) {
            xRecyclerView.refreshComplete(true);
            messages.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        messages.addAll(messageList);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                try {
                    String messageId = messages.get(position - 1).getId();
                    Intent intent = new Intent(context, MessageDetailActivity.class);
                    intent.putExtra("messageId", messageId);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        adapter.setReplyCallBack(new MessageAdapter.ReplyCallBack() {
            @Override
            public void onReply(MobileUser replyWho) {
                showDialog(replyWho);
            }
        });
    }

    private void showDialog(final MobileUser replyWho) {
        CommentDialog dialog = new CommentDialog(context, "输入回复内容");
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                createMessage(replyWho, content);
            }
        });
        dialog.show();
    }

    private void createMessage(MobileUser replyWho, String content) {  //发送消息
        String senderId = getUserId();
        String receiverId = replyWho.getId();
        Map<String, String> map = new HashMap<>();
        map.put("sender.id", senderId);
        map.put("receiver.id", receiverId);
        map.put("content", content);
        String url = Constants.OUTRT_NET + "/m/message";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                toast(context, "无法连接到服务器");
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    toastFullScreen("回复成功", true);
                } else {
                    toastFullScreen("回复失败", false);
                }
            }
        }, map));
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
}
