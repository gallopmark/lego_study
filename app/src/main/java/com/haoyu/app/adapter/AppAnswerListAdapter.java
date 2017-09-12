package com.haoyu.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.FAQsAnswerEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 答案列表适配器
 */
public class AppAnswerListAdapter extends BaseArrayRecyclerAdapter<FAQsAnswerEntity> {
    private Context mContext;
    private String userId;
    private ItemDisposeCallBack disposeCallBack;

    public AppAnswerListAdapter(Context context, List<FAQsAnswerEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    public AppAnswerListAdapter(Context context, List<FAQsAnswerEntity> mDatas, String userId) {
        super(mDatas);
        this.mContext = context;
        this.userId = userId;
    }

    public void setDisposeCallBack(ItemDisposeCallBack disposeCallBack) {
        this.disposeCallBack = disposeCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final FAQsAnswerEntity answer, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        ImageView userIco = holder.obtainView(R.id.answer_userIco);
        TextView userName = holder.obtainView(R.id.tv_userName);
        TextView date = holder.obtainView(R.id.tv_date);
        TextView content = holder.obtainView(R.id.tv_content);
        View line = holder.obtainView(R.id.line);
        final Button bt_alter = holder.obtainView(R.id.bt_alter);
        Button bt_delete = holder.obtainView(R.id.bt_delete);
        swipeLayout.setIos(true);
        if (answer.getCreator() != null && answer.getCreator().getId() != null
                && answer.getCreator().getId().equals(userId)) {
            swipeLayout.setSwipeEnable(true);
        } else {
            swipeLayout.setSwipeEnable(false);
        }
        if (answer.getCreator() != null && answer.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext,answer.getCreator().getAvatar()
                    ,R.drawable.user_default,R.drawable.user_default,userIco);
//            Glide.with(mContext)
//                    .load(answer.getCreator().getAvatar())
//                    .placeholder(R.drawable.user_default)
//                    .error(R.drawable.user_default)
//                    .dontAnimate().into(userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
        }
        if (answer.getCreator() != null && answer.getCreator().getRealName() != null) {
            userName.setText(answer.getCreator().getRealName());
        } else {
            userName.setText("匿名用户");
        }
        if (answer.getCreateTime() != null) {
            date.setText(TimeUtil.converTime(answer.getCreateTime()));
        } else {
            date.setText(TimeUtil.converTime(System.currentTimeMillis()));
        }
        content.setText(answer.getContent());
        if (position == getItemCount() - 1) {
            line.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.smContentView:
//                        Toast.makeText(mContext, "点击了:" + position, Toast.LENGTH_LONG).show();
                        break;
                    case R.id.bt_alter:
                        swipeLayout.smoothClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onAlter(answer, position);
                        }
                        break;
                    case R.id.bt_delete:
                        swipeLayout.smoothClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onDelete(answer, position);
                        }
                        break;
                }
            }
        };
        bt_alter.setOnClickListener(listener);
        bt_delete.setOnClickListener(listener);
//        smContentView.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wenda_answer_item;
    }

    public interface ItemDisposeCallBack {
        void onAlter(FAQsAnswerEntity entity, int position);

        void onDelete(FAQsAnswerEntity entity, int position);
    }
}
