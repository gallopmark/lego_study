package com.haoyu.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

public class FreeChatDetailAdapter extends BaseArrayRecyclerAdapter<CommentEntity> {
    private Context context;
    private String userId;

    public FreeChatDetailAdapter(Activity context, List<CommentEntity> mDatas, String userId) {
        super(mDatas);
        this.context = context;
        this.userId = userId;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.freechat_detil_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final CommentEntity entity, final int position) {
        ImageView ic_user = holder.obtainView(R.id.ic_user);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        TextView tv_createDate = holder.obtainView(R.id.tv_createDate);
        LinearLayout bodyDelete = holder.obtainView(R.id.bodyDelete);
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(userId))
            bodyDelete.setVisibility(View.VISIBLE);
        else
            bodyDelete.setVisibility(View.GONE);
        tv_createDate.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_content.setText(entity.getContent());
        bodyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (OnDeleteListener != null) {
                    OnDeleteListener.onDelete(entity.getId(), position);
                }
            }
        });
        if (entity != null && entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
    }


    public interface OnDeleteListener {
        void onDelete(String id, int position);
    }

    public OnDeleteListener OnDeleteListener;

    public void setOnDeleteListener(OnDeleteListener deletelistener) {
        this.OnDeleteListener = deletelistener;
    }
}
