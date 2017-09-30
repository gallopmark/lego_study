package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
    private Context mContext;
    private MoreReplyCallBack moreReplyCallBack;
    private SupportCallBack supportCallBack;
    private DeleteMainReply deleteMainReply;
    private String userId;

    public AppDiscussionAdapter(Context mContext, List<ReplyEntity> mDatas, String userId) {
        super(mDatas);
        this.mContext = mContext;
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
        return R.layout.dis_reply_list_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ReplyEntity entity, final int position) {
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
        recyclerView.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(mContext);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        final View line = holder.obtainView(R.id.line);
        if (position == getItemCount() - 1) {
            line.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            userName.setText(entity.getCreator().getRealName());
        } else {
            userName.setText("");
        }
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
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
        comment.setText(String.valueOf(entity.getChildPostCount()));
        if (entity.getChildReplyEntityList().size() > 0) {
            replyLayout.setVisibility(View.VISIBLE);
        } else {
            replyLayout.setVisibility(View.GONE);
        }
        if (entity.getChildPostCount() > 10) {
            tv_more_reply.setVisibility(View.VISIBLE);
        } else {
            tv_more_reply.setVisibility(View.GONE);
        }
        MoreReplyAdapter adapter = new MoreReplyAdapter(mContext, entity.getChildReplyEntityList());
        recyclerView.setAdapter(adapter);
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bodyDelete:
                        if (deleteMainReply != null) {
                            deleteMainReply.deleteMainReply(entity.getId(), position);
                        }
                        break;
                    case R.id.bodyLike:
                        if (supportCallBack != null) {
                            supportCallBack.support(position, like);
                        }
                        break;
                    case R.id.bodyComment:
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
        bodyDelete.setOnClickListener(listener);
        bodyLike.setOnClickListener(listener);
        bodyCommnet.setOnClickListener(listener);
        tv_more_reply.setOnClickListener(listener);
    }

    public interface OnPostClickListener {
        void onTargetClick(View view, int position, ReplyEntity entity);

        void onChildClick(View view, int position);
    }

    public void setOnPostClickListener(OnPostClickListener onPostClickListener) {
        this.onPostClickListener = onPostClickListener;
    }

    static class MoreReplyAdapter extends BaseArrayRecyclerAdapter<ReplyEntity> {
        private Context context;

        public MoreReplyAdapter(Context context, List<ReplyEntity> mDatas) {
            super(mDatas);
            this.context = context;
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.discussion_child_reply_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, ReplyEntity entity, int position) {
            TextView tv = holder.obtainView(R.id.tv_content);
            tv.setText(null);
            if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
                SpannableString ss = new SpannableString(entity.getCreator().getRealName() + ": ");
                ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)), 0, ss.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.append(ss);
            } else {
                SpannableString ss = new SpannableString("" + ": ");
                ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)), 0, ss.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.append(ss);
            }
            if (entity.getContent() != null) {
                SpannableString ss = new SpannableString(entity.getContent());
                ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, ss.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.append(ss);
            } else {
                SpannableString ss = new SpannableString("");
                ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, ss.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.append(ss);
            }
        }
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
