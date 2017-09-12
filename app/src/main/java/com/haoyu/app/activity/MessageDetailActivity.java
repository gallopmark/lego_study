package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.Message;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/8 on 16:09
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MessageDetailActivity extends BaseActivity {
    private MessageDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.message_type)
    TextView message_type;
    @BindView(R.id.createTime)
    TextView createTime;
    @BindView(R.id.messageTitle)
    TextView messageTitle;
    @BindView(R.id.messageContent)
    TextView messageContent;
    private String messageId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_message_detail;
    }

    @Override
    public void initView() {
        messageId = getIntent().getStringExtra("messageId");
    }

    public void initData() {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/message/" + messageId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<Message>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<Message> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }

        }));
    }

    private void updateUI(Message entity) {
        if (entity.getTitle() == null || entity.getTitle().length() == 0)
            messageTitle.setText("无标题");
        else
            messageTitle.setText(entity.getTitle());
        messageContent.setText(entity.getContent());
        if (entity.getType() != null && entity.getType().equals(Message.TYPE_SYSTEM)) {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            message_type.setText("系统通知");
        } else if (entity.getType() != null && entity.getType().equals(Message.TYPE_USER)) {
            message_type.setBackgroundResource(R.drawable.message_user);
            message_type.setTextColor(ContextCompat.getColor(context, R.color.message_user));
            if (entity.getSender() != null) {
                message_type.setText(entity.getSender().getRealName());
            } else {
                message_type.setText("匿名用户");
            }
        } else if (entity.getType() != null && entity.getType().equals(Message.TYPE_DAILY_WARN)) {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            message_type.setText("每日提醒");
        } else {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setText("系统通知");
            message_type.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            message_type.setText(entity.getType());
        }
        createTime.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

}
