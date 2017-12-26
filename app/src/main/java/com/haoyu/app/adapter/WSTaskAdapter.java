package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.entity.MultiItemEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/12/26.
 * 描述:工作坊阶段任务适配器
 * 作者:xiaoma
 */

public class WSTaskAdapter extends BaseArrayRecyclerAdapter<MultiItemEntity> {
    private Context context;
    private ArrayMap<Integer, Boolean> collapses = new ArrayMap<>();
    private int selected = -1;
    private OnActivityClickListener onActivityClickListener;

    public WSTaskAdapter(Context context, List<MultiItemEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void setOnActivityClickListener(OnActivityClickListener onActivityClickListener) {
        this.onActivityClickListener = onActivityClickListener;
    }

    public void addItemEntities(List<MultiItemEntity> list) {
        this.mDatas.addAll(list);
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i) instanceof MWorkshopSection) {
                if (i == 0) {
                    collapses.put(i, true);
                } else {
                    collapses.put(i, false);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getItemType();
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == 1) {
            return R.layout.wssection_item;
        } else {
            return R.layout.wsactivity_item;
        }
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MultiItemEntity entity, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == 1) {
            setViewType_1(holder, entity, position);
        } else {
            setViewType_2(holder, entity, position);
        }
    }

    private void setViewType_1(RecyclerHolder holder, MultiItemEntity entity, final int position) {
        TextView tv_position = holder.obtainView(R.id.tv_position);
        final TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_time = holder.obtainView(R.id.tv_time);
        final ImageView iv_expand = holder.obtainView(R.id.iv_expand);
        final MWorkshopSection section = (MWorkshopSection) entity;
        int stageIndex = section.getPosition() + 1;
        if (stageIndex < 10) {
            tv_position.setText("0" + stageIndex);
        } else if (stageIndex >= 10 && stageIndex <= 99) {
            tv_position.setText(String.valueOf(stageIndex));
        } else {
            tv_position.setText("..");
        }
        tv_title.setText(section.getTitle());
        String text_time = "研修时间：";
        if (section.getTimePeriod() != null) {
            text_time += TimeUtil.getDateYM(section.getTimePeriod().getStartTime()) + "-"
                    + TimeUtil.getDateYM(section.getTimePeriod().getEndTime());
        }
        tv_time.setText(text_time);
        if (collapses.get(position) != null && collapses.get(position) == true) {
            for (int i = 0; i < section.getActivities().size(); i++) {
                section.getActivities().get(i).setVisible(true);
            }
            iv_expand.setImageResource(R.drawable.go_down);
        } else {
            for (int i = 0; i < section.getActivities().size(); i++) {
                section.getActivities().get(i).setVisible(false);
            }
            iv_expand.setImageResource(R.drawable.go_into);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (collapses.get(position) != null && collapses.get(position)) {
                    for (int i = 0; i < section.getActivities().size(); i++) {
                        section.getActivities().get(i).setVisible(false);
                    }
                    collapses.put(position, false);
                    iv_expand.setImageResource(R.drawable.go_down);
                } else {
                    for (int i = 0; i < section.getActivities().size(); i++) {
                        section.getActivities().get(i).setVisible(true);
                    }
                    collapses.put(position, true);
                    iv_expand.setImageResource(R.drawable.go_into);
                }
                notifyDataSetChanged();
            }
        });
    }

    private void setViewType_2(RecyclerHolder holder, MultiItemEntity entity, final int position) {
        ImageView ic_type = holder.obtainView(R.id.ic_type);
        TextView tv_typeName = holder.obtainView(R.id.tv_typeName);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        final MWorkshopActivity activity = (MWorkshopActivity) entity;
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (activity.isVisible()) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        } else {
            params.height = 0;
        }
        holder.itemView.setLayoutParams(params);
        String type = activity.getType();
        if (type != null && type.equals("discussion")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.ws_discuss_press);
            } else {
                ic_type.setImageResource(R.drawable.ws_discuss_default);
            }
            tv_typeName.setText("教学研讨");
        } else if (type != null && type.equals("survey")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.ws_questionnaire_press);
            } else {
                ic_type.setImageResource(R.drawable.ws_questionnaire_default);
            }
            tv_typeName.setText("调查问卷");
        } else if (type != null && type.equals("debate")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.ws_bianlun_press);
            } else {
                ic_type.setImageResource(R.drawable.ws_bianlun_default);
            }
            tv_typeName.setText("在线辩论");
        } else if (type != null && type.equals("lcec")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.ws_tingke_press);
            } else {
                ic_type.setImageResource(R.drawable.ws_tingke_default);
            }
            tv_typeName.setText("听课评课");
        } else if (type != null && type.equals("lesson_plan")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.ws_beike_press);
            } else {
                ic_type.setImageResource(R.drawable.ws_beike_default);
            }
            tv_typeName.setText("集体备课");
        } else if (type != null && type.equals("test")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.progress_test_press);
            } else {
                ic_type.setImageResource(R.drawable.progress_test_default);
            }
            tv_typeName.setText("在线测验");
        } else if (type != null && type.equals("video")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.progress_video_press);
            } else {
                ic_type.setImageResource(R.drawable.progress_video_default);
            }
            tv_typeName.setText("教学观摩");
        } else if (type != null && type.equals("discuss_class")) {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.progress_video_press);
            } else {
                ic_type.setImageResource(R.drawable.progress_video_default);
            }
            tv_typeName.setText("教学观摩");
        } else {
            if (selected == position) {
                ic_type.setImageResource(R.drawable.course_word_selected);
            } else {
                ic_type.setImageResource(R.drawable.course_word_default);
            }
            tv_typeName.setText("类型未知");
        }
        if (selected == position) {
            tv_typeName.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            tv_title.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        } else {
            tv_typeName.setTextColor(ContextCompat.getColor(context, R.color.blow_gray));
            tv_title.setTextColor(ContextCompat.getColor(context, R.color.line_bottom));
        }
        if (activity.getTitle() != null && activity.getTitle().trim().length() > 0) {
            tv_title.setText(activity.getTitle());
        } else {
            tv_title.setText("无标题");
        }
        tv_title.setText(activity.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelected(position);
                if (onActivityClickListener != null) {
                    onActivityClickListener.onActivityClick(activity.getId());
                }
            }
        });
    }

    private void setSelected(int position) {
        selected = position;
        notifyDataSetChanged();
    }

    public interface OnActivityClickListener {
        void onActivityClick(String activityId);
    }
}
