package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * 创建日期：2017/3/1 on 16:31
 * 描述:讨论子回复是配资
 * 作者:马飞奔 Administrator
 */
public class AppDiscussionReplyAdapter extends BaseArrayRecyclerAdapter<ReplyEntity> {
    private Context mContext;

    public AppDiscussionReplyAdapter(Context context, List<ReplyEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, ReplyEntity entity, int position) {
        TextView tv = holder.obtainView(R.id.tv_content);
        tv.setText(null);
        SpannableString ss, ss1;
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            ss = new SpannableString(entity.getCreator().getRealName() + ": ");
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.defaultColor)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        } else {
            ss = new SpannableString("匿名用户" + ": ");
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.defaultColor)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        }
        if (entity.getContent() != null) {
            ss1 = new SpannableString(entity.getContent());
            ss1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.black)), 0, ss1.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss1);
        } else {
            ss1 = new SpannableString("");
            ss1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.black)), 0, ss1.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss1);
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_more_reply_item;
    }
}
