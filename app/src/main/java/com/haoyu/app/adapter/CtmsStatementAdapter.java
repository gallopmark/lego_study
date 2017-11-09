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
 * 创建日期：2017/1/10 on 9:23
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CtmsStatementAdapter extends BaseArrayRecyclerAdapter<DiscussEntity> {
    private Context mContext;
    private RequestClickCallBack requestClickCallBack;

    public CtmsStatementAdapter(Context mContext, List<DiscussEntity> mDatas) {
        super(mDatas);
        this.mContext = mContext;
    }

    public void setRequestClickCallBack(RequestClickCallBack requestClickCallBack) {
        this.requestClickCallBack = requestClickCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final DiscussEntity entity, final int position) {
        ImageView iv_ico = holder.obtainView(R.id.iv_ico);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_createTime = holder.obtainView(R.id.tv_createTime);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        View rl_support = holder.obtainView(R.id.rl_support);
        final TextView tv_support = holder.obtainView(R.id.tv_support);
        View rl_comment = holder.obtainView(R.id.rl_comment);
        final TextView tv_commnet = holder.obtainView(R.id.tv_comment);
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, iv_ico);
        } else {
            iv_ico.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        tv_createTime.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_title.setText(entity.getTitle());
        if (entity.getContent() != null) {
            Spanned spanned = Html.fromHtml(entity.getContent());
            tv_content.setText(spanned);
        } else
            tv_content.setText(null);
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            tv_support.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getSupportNum()));
            tv_commnet.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getReplyNum()));
        } else {
            tv_support.setText(String.valueOf(0));
            tv_commnet.setText(String.valueOf(0));
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.rl_support:
                        if (requestClickCallBack != null)
                            requestClickCallBack.support(entity, position);
                        break;
                    case R.id.rl_comment:
                        if (requestClickCallBack != null)
                            requestClickCallBack.comment(entity, position);
                        break;
                }
            }
        };
        rl_support.setOnClickListener(listener);
        rl_comment.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_research_item;
    }

    public interface RequestClickCallBack {
        void support(DiscussEntity entity, int position);

        void comment(DiscussEntity entity, int position);
    }
}
