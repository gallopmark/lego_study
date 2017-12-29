package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
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
    private Context context;

    public AppMoreReplyAdapter(Context context, List<CommentEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, CommentEntity entity, int position) {
        TextView tv = holder.obtainView(R.id.tv_content);
        String content = "";
        int end;
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            content += entity.getCreator().getRealName() + "：";
        } else {
            content += "：";
        }
        end = content.length();
        if (!TextUtils.isEmpty(entity.getContent())) {
            content += entity.getContent();
        } else {
            content += "";
        }
        SpannableString ss = new SpannableString(content);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)), 0, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.childreply_item;
    }
}
