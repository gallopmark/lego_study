package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

public class FreeChatAdapter extends BaseArrayRecyclerAdapter<CommentEntity> {
    private Context context;

    public FreeChatAdapter(Context context, List<CommentEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.freechat_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final CommentEntity mComments, final int position) {
        TextView mContent = holder.obtainView(R.id.tv_discussion_text);
        ImageView personImg = holder.obtainView(R.id.ic_user);
        TextView userName = holder.obtainView(R.id.tv_userName);
        TextView createTime = holder.obtainView(R.id.tv_createTime);
        mContent.setText(mComments.getContent());
        if (mComments.getCreator() != null) {
            userName.setText(mComments.getCreator().getRealName());
        } else {
            userName.setText("");
        }
        createTime.setText("发表于" + TimeUtil.converTime(mComments.getCreateTime()));
        if (mComments.getCreator() != null && mComments.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, mComments.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, personImg);
        } else {
            personImg.setImageResource(R.drawable.user_default);
        }
    }
}
