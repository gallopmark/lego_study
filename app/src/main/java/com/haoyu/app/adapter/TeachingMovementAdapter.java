package com.haoyu.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.activity.TeachingResearchATActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.TeachingMovementEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/11 on 11:04
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingMovementAdapter extends BaseArrayRecyclerAdapter<TeachingMovementEntity> {
    private Context mContext;
    private int imageHeight;
    private RegisterCallBack registerCallBack;

    public TeachingMovementAdapter(Context context, List<TeachingMovementEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
        imageHeight = ScreenUtils.getScreenHeight(context) / 7 * 2;
    }

    public void setRegisterCallBack(RegisterCallBack registerCallBack) {
        this.registerCallBack = registerCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final TeachingMovementEntity entity, final int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageHeight);
        ImageView at_img = holder.obtainView(R.id.at_img);
        at_img.setLayoutParams(params);
        TextView tv_title = holder.obtainView(R.id.tv_title);  //活动标题
        TextView tv_type = holder.obtainView(R.id.tv_type);    //活动类型 报名中、进行中、已结束
        TextView tv_time = holder.obtainView(R.id.tv_time);    //活动时间
        TextView tv_address = holder.obtainView(R.id.tv_address);   //活动地点
        TextView tv_creator = holder.obtainView(R.id.tv_creator);   //活动发起人
        TextView tv_host = holder.obtainView(R.id.tv_host);   //主办单位
        TextView tv_viewNum = holder.obtainView(R.id.tv_viewNum);  //浏览人数
        TextView tv_joinNum = holder.obtainView(R.id.tv_joinNum);  //参与人数
        GlideImgManager.loadImage(mContext, entity.getImage(), R.drawable.app_default, R.drawable.app_default, at_img);
        Button bt_type = holder.obtainView(R.id.bt_type);
        tv_title.setText(entity.getTitle());
        if (entity.getState() != null && entity.getState().equals("no_begin")) {
            tv_type.setText("未开始");
            tv_type.setBackgroundResource(R.drawable.teaching_research_end);
            bt_type.setText("查看详情");
            bt_type.setBackgroundResource(R.drawable.round_label);
            bt_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
        } else if (entity.getState() != null && entity.getState().equals("register")) {
            tv_type.setText("报名中");
            tv_type.setBackgroundResource(R.drawable.teaching_research_apply);
            if (entity.getmMovementRegisters() != null && entity.getmMovementRegisters().size() > 0
                    && entity.getmMovementRegisters().get(0).getId() != null) {
                bt_type.setText("取消报名");
            } else {
                bt_type.setText("报名参与");
            }
            bt_type.setBackgroundResource(R.drawable.round_blue_label);
            bt_type.setTextColor(ContextCompat.getColor(mContext, R.color.qianlan));
        } else if (entity.getState() != null && entity.getState().equals("end")) {
            tv_type.setText("已结束");
            tv_type.setBackgroundResource(R.drawable.teaching_research_apply);
            bt_type.setText("查看详情");
            bt_type.setBackgroundResource(R.drawable.round_label);
            bt_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
        } else if (entity.getState() != null && entity.getState().equals("begin")) {
            tv_type.setText("进行中");
            tv_type.setBackgroundResource(R.drawable.teaching_research_ing);
            bt_type.setText("查看详情");
            bt_type.setBackgroundResource(R.drawable.round_label);
            bt_type.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
        } else {
            bt_type.setVisibility(View.GONE);
        }
        if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size() > 0) {
            TeachingMovementEntity.MovementRelation relation = entity.getmMovementRelations().get(0);
            tv_viewNum.setText(String.valueOf(relation.getBrowseNum()));
            tv_joinNum.setText(String.valueOf(relation.getParticipateNum()));
        } else {
            tv_viewNum.setText(String.valueOf(0));
            tv_joinNum.setText(String.valueOf(0));
        }
        if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size() > 0
                && entity.getmMovementRelations().get(0).getTimePeriod() != null) {
            TeachingMovementEntity.MovementRelation relation = entity.getmMovementRelations().get(0);
            tv_time.setText(TimeUtil.convertDayOfMinute(relation.getTimePeriod().getStartTime(),
                    relation.getTimePeriod().getEndTime()));
        } else {
            tv_time.setText("活动时间未确定");
        }
        tv_address.setText(entity.getLocation());
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_creator.setText(entity.getCreator().getRealName());
        } else {
            tv_creator.setText("");
        }
        tv_host.setText(entity.getSponsor());
        bt_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entity.getState() != null && entity.getState().equals("register")) {
                    if (entity.getmMovementRegisters() != null && entity.getmMovementRegisters().size() > 0
                            && entity.getmMovementRegisters().get(0).getId() != null) {
                        if (registerCallBack != null) {
                            registerCallBack.unregister(position, entity.getmMovementRegisters().get(0).getId());
                        }
                    } else {
                        if (registerCallBack != null) {
                            registerCallBack.register(position, entity.getId());
                        }
                    }
                } else {
                    String id = entity.getId();
                    Intent intent = new Intent(mContext, TeachingResearchATActivity.class);
                    intent.putExtra("id", id);
                    if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size() > 0) {
                        intent.putExtra("relationId", entity.getmMovementRelations().get(0).getId());
                    }
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_research_activity_item;
    }

    public interface RegisterCallBack {
        void register(int position, String activityId);

        void unregister(int position, String registerId);
    }
}
