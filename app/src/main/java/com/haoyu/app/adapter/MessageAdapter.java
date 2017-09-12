package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.Message;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/8 on 15:03
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MessageAdapter extends BaseArrayRecyclerAdapter<Message> {
    private Context mContext;
    private ReplyCallBack replyCallBack;

    public MessageAdapter(Context mContext, List mDatas) {
        super(mDatas);
        this.mContext = mContext;
    }

    public void setReplyCallBack(ReplyCallBack replyCallBack) {
        this.replyCallBack = replyCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final Message entity, int position) {
        TextView message_type = holder.obtainView(R.id.message_type);
        TextView createTime = holder.obtainView(R.id.createTime);
        TextView message_content = holder.obtainView(R.id.message_content);
        Button message_reply = holder.obtainView(R.id.message_reply);
        message_content.setText(entity.getContent());
        if (entity.getType() != null && entity.getType().equals(Message.TYPE_USER)) {
            message_reply.setVisibility(View.VISIBLE);
        } else {
            message_reply.setVisibility(View.GONE);
        }
        if (entity.getType() != null && entity.getType().equals(Message.TYPE_SYSTEM)) {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
            message_type.setText("系统通知");
        } else if (entity.getType() != null && entity.getType().equals(Message.TYPE_USER)) {
            message_type.setBackgroundResource(R.drawable.message_user);
            message_type.setTextColor(ContextCompat.getColor(mContext, R.color.message_user));
            if (entity.getSender() != null) {
                message_type.setText(entity.getSender().getRealName());
            } else {
                message_type.setText("匿名用户");
            }
        } else if (entity.getType() != null && entity.getType().equals(Message.TYPE_DAILY_WARN)) {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
            message_type.setText("每日提醒");
        } else {
            message_type.setBackgroundResource(R.drawable.message_system);
            message_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
            message_type.setText(entity.getType());
        }
        createTime.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
        message_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replyCallBack != null) {
                    replyCallBack.onReply(entity.getSender());
                }
            }
        });
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.message_list_item;
    }

    public interface ReplyCallBack {
        void onReply(MobileUser replyWho);
    }
}
