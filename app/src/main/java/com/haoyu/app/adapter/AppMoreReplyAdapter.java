package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * 创建日期：2017/1/16 on 11:44
 * 描述: 更多回复是适配器
 * 作者:马飞奔 Administrator
 */
public class AppMoreReplyAdapter extends BaseArrayRecyclerAdapter<CommentEntity> {
    private Context mContext;

    public AppMoreReplyAdapter(Context context, List<CommentEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, CommentEntity entity, int position) {
        TextView tv = holder.obtainView(R.id.tv_content);
        tv.setText(null);
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            SpannableString ss = new SpannableString(entity.getCreator().getRealName() + ": ");
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.defaultColor)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        } else {
            SpannableString ss = new SpannableString("" + ": ");
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.defaultColor)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        }
        if (entity.getContent() != null) {
            SpannableString ss = new SpannableString(entity.getContent());
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.black)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        } else {
            SpannableString ss = new SpannableString("");
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,
                    R.color.black)), 0, ss.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(ss);
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_more_reply_item;
    }
}
