package com.haoyu.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MyTrainCommunityResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/9 on 8:39
 * 描述: 研修社区列表适配器
 * 作者:马飞奔 Administrator
 */
public class MyTrainCommunityAdapter extends BaseArrayRecyclerAdapter<MyTrainCommunityResult> {
    private int imageWidth;
    private int imageHeight;

    public MyTrainCommunityAdapter(Context context, List<MyTrainCommunityResult> mDatas) {
        super(mDatas);
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - 20;
        imageHeight = imageWidth / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MyTrainCommunityResult entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView img = holder.obtainView(R.id.community_img);
        img.setLayoutParams(params);
        img.setImageResource(R.drawable.app_default);
        TextView tv_period = holder.obtainView(R.id.tv_period);
        TextView tv_studyHour = holder.obtainView(R.id.tv_studyHour);
        TextView tv_score = holder.obtainView(R.id.tv_score);
        View divider = holder.obtainView(R.id.divider);
        if (entity.getmCommunityRelation() != null && entity.getmCommunityRelation().getTimePeriod() != null) {
            tv_period.setText(TimeUtil.getSlashDate(entity.getmCommunityRelation().getTimePeriod().getStartTime())
                    + "至" + TimeUtil.getSlashDate(entity.getmCommunityRelation().getTimePeriod().getEndTime()));
        } else {
            tv_period.setText("时间未定");
        }
        if (entity.getmCommunityRelation() != null) {
            tv_studyHour.setText(entity.getmCommunityRelation().getStudyHours() + "学时");
            tv_score.setText("获得" + entity.getScore() + " / " + entity.getmCommunityRelation().getScore() + "积分");
        } else {
            tv_studyHour.setText("0学时");
            tv_score.setText("获得" + entity.getScore() + " / " + 0 + "积分");
        }
        if (position == getItemCount() - 1)
            divider.setVisibility(View.GONE);
        else
            divider.setVisibility(View.VISIBLE);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.community_list_item;
    }
}
