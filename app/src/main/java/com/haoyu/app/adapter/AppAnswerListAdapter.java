package com.haoyu.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.FAQsAnswerEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.ExpandableTextView;

import java.util.List;

/**
 * 答案列表适配器
 */
public class AppAnswerListAdapter extends BaseArrayRecyclerAdapter<FAQsAnswerEntity> {
    private Context mContext;
    private OnItemLongClickListener onItemLongClickListener;

    public AppAnswerListAdapter(Context context, List<FAQsAnswerEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final FAQsAnswerEntity answer, final int position) {
        ImageView userIco = holder.obtainView(R.id.answer_userIco);
        TextView userName = holder.obtainView(R.id.tv_userName);
        TextView date = holder.obtainView(R.id.tv_date);
        ExpandableTextView content = holder.obtainView(R.id.tv_content);
        View line = holder.obtainView(R.id.line);
        if (answer.getCreator() != null && answer.getCreator().getAvatar() != null)
            GlideImgManager.loadCircleImage(mContext, answer.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, userIco);
        else
            userIco.setImageResource(R.drawable.user_default);
        if (answer.getCreator() != null && answer.getCreator().getRealName() != null)
            userName.setText(answer.getCreator().getRealName());
        else
            userName.setText("");
        date.setText(TimeUtil.converTime(answer.getCreateTime()));
        content.setText(answer.getContent());
        if (position == getItemCount() - 1)
            line.setVisibility(View.GONE);
        else
            line.setVisibility(View.VISIBLE);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onItemLongClickListener != null)
                    onItemLongClickListener.onItemLongClick(answer, position);
                return false;
            }
        });
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wenda_answer_item;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(FAQsAnswerEntity entity, int position);
    }
}
