package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.List;

/**
 * 创建日期：2017/1/13 on 14:27
 * 描述: 公共评论适配器
 * 作者:马飞奔 Administrator
 */
public class AppCommentAdapter extends BaseArrayRecyclerAdapter<CommentEntity> {
    private Context mContext;
    private String userId;
    private MoreReplyCallBack moreReplyCallBack;
    private CommentCallBack commnetCallBack;
    private SupportCallBack supportCallBack;
    private DeleteMainComment deleteMainComment;

    public AppCommentAdapter(Context mContext, List<CommentEntity> mDatas, String userId) {
        super(mDatas);
        this.mContext = mContext;
        this.userId = userId;
    }

    public void setMoreReplyCallBack(MoreReplyCallBack moreReplyCallBack) {
        this.moreReplyCallBack = moreReplyCallBack;
    }

    public void setCommentCallBack(CommentCallBack commnetCallBack) {
        this.commnetCallBack = commnetCallBack;
    }

    public void setSupportCallBack(SupportCallBack supportCallBack) {
        this.supportCallBack = supportCallBack;
    }

    public void setDeleteMainComment(DeleteMainComment deleteMainComment) {
        this.deleteMainComment = deleteMainComment;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final CommentEntity entity, final int position) {
        ImageView userIco = holder.obtainView(R.id.ic_user);
        TextView userName = holder.obtainView(R.id.tv_userName);
        TextView content = holder.obtainView(R.id.tv_content);
        TextView createDate = holder.obtainView(R.id.tv_createDate);
        View bodyDelete = holder.obtainView(R.id.bodyDelete);
        View bodyLike = holder.obtainView(R.id.bodyLike);
        final TextView like = holder.obtainView(R.id.tv_like);
        View bodyCommnet = holder.obtainView(R.id.bodyComment);
        final TextView comment = holder.obtainView(R.id.tv_comment);
        View replyLayout = holder.obtainView(R.id.replyLayout);
        RecyclerView recyclerView = holder.obtainView(R.id.recyclerView);
        TextView tv_more_reply = holder.obtainView(R.id.tv_more_reply);
        final View line = holder.obtainView(R.id.line);
        recyclerView.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(mContext);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        if (position == getItemCount() - 1) {
            line.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
        }
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            userName.setText(entity.getCreator().getRealName());
        } else {
            userName.setText("");
        }
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(userId)) {
            bodyDelete.setVisibility(View.VISIBLE);
        } else {
            bodyDelete.setVisibility(View.GONE);
        }
        content.setText(entity.getContent());
        createDate.setText(TimeUtil.converTime(entity.getCreateTime()));
        like.setText(String.valueOf(entity.getSupportNum()));
        comment.setText(String.valueOf(entity.getChildNum()));
        if (entity.getChildList().size() > 0) {
            replyLayout.setVisibility(View.VISIBLE);
        } else {
            replyLayout.setVisibility(View.GONE);
        }
        ChildCommentAdapter adapter = new ChildCommentAdapter(entity.getChildList());
        recyclerView.setAdapter(adapter);
        if (entity.getChildNum() > 10) {
            tv_more_reply.setVisibility(View.VISIBLE);
        } else {
            tv_more_reply.setVisibility(View.GONE);
        }
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bodyDelete:
                        if (deleteMainComment != null) {
                            deleteMainComment.deleteMainComment(entity.getId(), position);
                        }
                        break;
                    case R.id.bodyLike:
                        if (supportCallBack != null) {
                            supportCallBack.support(position, like);
                        }
                        break;
                    case R.id.bodyComment:
                        if (commnetCallBack != null) {
                            commnetCallBack.comment(position, entity);
                        }
                        break;
                    case R.id.tv_more_reply:
                        if (moreReplyCallBack != null) {
                            moreReplyCallBack.moreReply(position, entity);
                        }
                        break;
                }
            }
        };
        bodyDelete.setOnClickListener(listener);
        bodyLike.setOnClickListener(listener);
        bodyCommnet.setOnClickListener(listener);
        tv_more_reply.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_comment_item;
    }

    private class ChildCommentAdapter extends BaseArrayRecyclerAdapter<CommentEntity> {

        public ChildCommentAdapter(List<CommentEntity> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.app_comment_child_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, CommentEntity entity, int position) {
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
    }

    public interface MoreReplyCallBack {
        void moreReply(int position, CommentEntity entity);
    }

    public interface CommentCallBack {
        void comment(int position, CommentEntity entity);
    }

    public interface SupportCallBack {
        void support(int position, TextView tv_like);
    }

    public interface DeleteMainComment {
        void deleteMainComment(String id, int position);
    }

}
