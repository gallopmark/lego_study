package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.List;

/**
 * 讨论列表适配器（包括主回复和子回复）
 *
 * @author xiaoma
 */
public class AppDiscussionAdapter extends BaseArrayRecyclerAdapter<ReplyEntity> {
    private OnPostClickListener onPostClickListener;
    private Context context;
    private MoreReplyCallBack moreReplyCallBack;
    private SupportCallBack supportCallBack;
    private DeleteMainReply deleteMainReply;
    private String userId;

    public AppDiscussionAdapter(Context context, List<ReplyEntity> mDatas, String userId) {
        super(mDatas);
        this.context = context;
        this.userId = userId;
    }

    public void setSupportCallBack(SupportCallBack supportCallBack) {
        this.supportCallBack = supportCallBack;
    }

    public void setMoreReplyCallBack(MoreReplyCallBack moreReplyCallBack) {
        this.moreReplyCallBack = moreReplyCallBack;
    }

    public void setDeleteMainReply(DeleteMainReply deleteMainReply) {
        this.deleteMainReply = deleteMainReply;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.mainreply_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ReplyEntity entity, final int position) {
        ImageView ic_user = holder.obtainView(R.id.ic_user);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        TextView createDate = holder.obtainView(R.id.tv_createDate);
        LinearLayout ll_delete = holder.obtainView(R.id.ll_delete);
        LinearLayout ll_like = holder.obtainView(R.id.ll_like);
        final TextView like = holder.obtainView(R.id.tv_like);
        LinearLayout ll_comment = holder.obtainView(R.id.ll_comment);
        final TextView comment = holder.obtainView(R.id.tv_comment);
        LinearLayout replyLayout = holder.obtainView(R.id.replyLayout);
        RecyclerView recyclerView = holder.obtainView(R.id.recyclerView);
        TextView tv_more_reply = holder.obtainView(R.id.tv_more_reply);
        final View line = holder.obtainView(R.id.line);
        if (position == getItemCount() - 1) {
            line.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(userId)) {
            ll_delete.setVisibility(View.VISIBLE);
        } else {
            ll_delete.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(entity.getContent())) {
            tv_content.setText(entity.getContent().trim());
        } else {
            tv_content.setText("");
        }
        createDate.setText(TimeUtil.converTime(entity.getCreateTime()));
        like.setText(String.valueOf(entity.getSupportNum()));
        comment.setText(String.valueOf(entity.getChildPostCount()));
        if (entity.getChildReplyEntityList().size() > 0) {
            replyLayout.setVisibility(View.VISIBLE);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            AppDiscussionReplyAdapter adapter = new AppDiscussionReplyAdapter(context, entity.getChildReplyEntityList());
            recyclerView.setAdapter(adapter);
        } else {
            replyLayout.setVisibility(View.GONE);
        }
        if (entity.getChildPostCount() > 10) {
            tv_more_reply.setVisibility(View.VISIBLE);
        } else {
            tv_more_reply.setVisibility(View.GONE);
        }
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ll_delete:
                        if (deleteMainReply != null) {
                            deleteMainReply.deleteMainReply(entity.getId(), position);
                        }
                        break;
                    case R.id.ll_like:
                        if (supportCallBack != null) {
                            supportCallBack.support(position, like);
                        }
                        break;
                    case R.id.ll_comment:
                        if (onPostClickListener != null) {
                            onPostClickListener.onChildClick(v, position);
                        }
                        break;
                    case R.id.tv_more_reply:
                        if (moreReplyCallBack != null) {
                            moreReplyCallBack.callBack(entity, position);
                        }
                        break;
                }
            }
        };
        ll_delete.setOnClickListener(listener);
        ll_like.setOnClickListener(listener);
        ll_comment.setOnClickListener(listener);
        tv_more_reply.setOnClickListener(listener);
    }

    public interface OnPostClickListener {
        void onTargetClick(View view, int position, ReplyEntity entity);

        void onChildClick(View view, int position);
    }

    public void setOnPostClickListener(OnPostClickListener onPostClickListener) {
        this.onPostClickListener = onPostClickListener;
    }

    public interface MoreReplyCallBack {
        void callBack(ReplyEntity entity, int position);
    }

    public interface SupportCallBack {
        void support(int position, TextView tv_like);
    }

    public interface DeleteMainReply {
        void deleteMainReply(String id, int position);
    }
}
