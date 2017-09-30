package com.haoyu.app.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2016/11/15 on 10:24
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageDiscussionAdapter extends BaseArrayRecyclerAdapter<DiscussEntity> {
    private Context mContext;
    private OnPartCallBack onPartCallBack;

    public PageDiscussionAdapter(Context mContext, List<DiscussEntity> dataList) {
        super(dataList);
        this.mContext = mContext;
    }

    public void setOnPartCallBack(OnPartCallBack onPartCallBack) {
        this.onPartCallBack = onPartCallBack;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.page_discussion_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final DiscussEntity entity, final int position) {
        TextView tv_discussion_title = holder.obtainView(R.id.tv_discussion_title);
        TextView tv_discussion_text = holder.obtainView(R.id.tv_discussion_text);
        ImageView userIco = holder.obtainView(R.id.ic_user);
        TextView userName = holder.obtainView(R.id.tv_userName);
        TextView createDate = holder.obtainView(R.id.tv_createTime);
        final View bodyLike = holder.obtainView(R.id.bodyLike);
        View bodyComment = holder.obtainView(R.id.bodyComment);
        final TextView like = holder.obtainView(R.id.tv_dianzan);
        final TextView discuss = holder.obtainView(R.id.tv_pinglun);
        View bottomLine = holder.obtainView(R.id.bottomLine);
        if (position == getItemCount() - 1) {
            bottomLine.setVisibility(View.GONE);
        } else {
            bottomLine.setVisibility(View.VISIBLE);
        }
        tv_discussion_title.setText(entity.getTitle());
        if (entity.getContent() != null) {
            Spanned spanned = Html.fromHtml(entity.getContent());
            tv_discussion_text.setText(spanned);
        } else
            tv_discussion_text.setText(null);
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            userName.setText(entity.getCreator().getRealName());
        } else {
            userName.setText("");
        }
        createDate.setText("发表于" + TimeUtil.converTime(entity.getCreateTime()));
        if (entity.getmDiscussionRelations() != null
                && entity.getmDiscussionRelations().size() > 0) {
            int replyNum = entity.getmDiscussionRelations().get(0).getReplyNum();
            int supportNum = entity.getmDiscussionRelations().get(0).getSupportNum();
            discuss.setText(String.valueOf(replyNum));
            like.setText(String.valueOf(supportNum));
        } else {
            discuss.setText(String.valueOf(0));
            like.setText(String.valueOf(0));
        }
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bodyLike:
                        if (onPartCallBack != null)
                            onPartCallBack.createLink(position, like);
                        return;
                    case R.id.bodyComment:
                        if (onPartCallBack != null)
                            onPartCallBack.comment(position);
                        break;
                }
            }
        };
        bodyLike.setOnClickListener(listener);
        bodyComment.setOnClickListener(listener);
    }

    public interface OnPartCallBack {
        void createLink(int position, TextView tv_like);

        void comment(int position);
    }
}
