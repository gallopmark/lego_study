package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MWSActivityCrease;
import com.haoyu.app.entity.MWSSectionCrease;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.entity.MultiItemEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/12/26.
 * 描述:工作坊阶段任务适配器(坊主身份可编辑任务阶段)
 * 作者:xiaoma
 */

public class WSTaskEditAdapter extends BaseArrayRecyclerAdapter<MultiItemEntity> {
    private Context context;
    private ArrayMap<Integer, Boolean> collapses = new ArrayMap<>();
    private int selected = -1, sectionSize;
    private OnSectionLongClickListener onSectionLongClickListener;
    private OnAddTaskListener addTaskListener;
    private OnTaskEditListener onTaskEditListener;

    public WSTaskEditAdapter(Context context, List<MultiItemEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void addItemEntities(List<MultiItemEntity> list) {
        this.mDatas.addAll(list);
        int index = 0;
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i) instanceof MWorkshopSection) {
                index++;
                if (i == 0) {
                    collapses.put(i, true);
                } else {
                    collapses.put(i, false);
                }
            }
        }
        this.sectionSize = index;
        notifyDataSetChanged();
    }

    @Override
    public boolean addItem(MultiItemEntity entity) {
        if (entity instanceof MWorkshopSection) {
            ((MWorkshopSection) entity).setPosition(sectionSize);
            sectionSize += 1;
        } else if (entity instanceof MWSSectionCrease) {
            sectionSize += 1;
        }
        mDatas.add(entity);
        notifyDataSetChanged();
        return true;
    }

    public void addActivity(int sectionIndex, MWorkshopActivity activity) {
        if (collapses.get(sectionIndex) != null && collapses.get(sectionIndex)) {
            activity.setVisible(true);
        } else {
            activity.setVisible(false);
        }
        MWorkshopSection section = (MWorkshopSection) mDatas.get(sectionIndex);
        activity.setTag(section);
        int index = sectionIndex + section.getActivities().size() + 1;
        section.getActivities().add(activity);
        mDatas.add(index, activity);
        notifyDataSetChanged();
    }

    public void removeActivity(int position) {
        MWorkshopActivity activity = (MWorkshopActivity) mDatas.remove(position);
        MWorkshopSection section = activity.getTag();
        section.getActivities().remove(activity);
        notifyDataSetChanged();
    }

    @Override
    public boolean removeItem(int position) {
        MultiItemEntity entity = mDatas.remove(position);
        if (entity instanceof MWorkshopSection) {
            sectionSize -= 1;
            MWorkshopSection section = (MWorkshopSection) entity;
            mDatas.removeAll(section.getActivities());
            mDatas.remove(section.getCrease());
            for (int i = position; i < mDatas.size(); i++) {
                if (mDatas.get(i) instanceof MWorkshopSection) {
                    int pos = ((MWorkshopSection) mDatas.get(i)).getPosition();
                    ((MWorkshopSection) mDatas.get(i)).setPosition(pos - 1);
                }
            }
        } else if (entity instanceof MWSSectionCrease) {
            sectionSize -= 1;
        }
        notifyDataSetChanged();
        return true;
    }

    public void setSelected(int position) {
        selected = position;
        notifyDataSetChanged();
    }

    public void setOnSectionLongClickListener(OnSectionLongClickListener onSectionLongClickListener) {
        this.onSectionLongClickListener = onSectionLongClickListener;
    }

    public void setAddTaskListener(OnAddTaskListener addTaskListener) {
        this.addTaskListener = addTaskListener;
    }

    public void setOnTaskEditListener(OnTaskEditListener onTaskEditListener) {
        this.onTaskEditListener = onTaskEditListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getItemType();
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == 1) {
            return R.layout.wssection_item;
        } else if (viewtype == 2) {
            return R.layout.wsactivity_item;
        } else if (viewtype == 3) {
            return R.layout.wssection_edititem;
        } else {
            return R.layout.workshop_add_task;
        }
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MultiItemEntity entity, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == 1) {
            setViewType_1(holder, entity, position);
        } else if (viewType == 2) {
            setViewType_2(holder, entity, position);
        } else if (viewType == 3) {
            setViewType_3(holder, entity);
        } else if (viewType == 4) {
            setViewType_4(holder);
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
            section.getCrease().setVisible(true);
            iv_expand.setImageResource(R.drawable.go_down);
        } else {
            for (int i = 0; i < section.getActivities().size(); i++) {
                section.getActivities().get(i).setVisible(false);
            }
            section.getCrease().setVisible(false);
            iv_expand.setImageResource(R.drawable.go_into);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (collapses.get(position) != null && collapses.get(position)) {
                    for (int i = 0; i < section.getActivities().size(); i++) {
                        section.getActivities().get(i).setVisible(false);
                    }
                    section.getCrease().setVisible(true);
                    collapses.put(position, false);
                    iv_expand.setImageResource(R.drawable.go_down);
                } else {
                    for (int i = 0; i < section.getActivities().size(); i++) {
                        section.getActivities().get(i).setVisible(true);
                    }
                    section.getCrease().setVisible(false);
                    collapses.put(position, true);
                    iv_expand.setImageResource(R.drawable.go_into);
                }
                notifyDataSetChanged();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onSectionLongClickListener != null) {
                    onSectionLongClickListener.onLongClickListener(section.getId(), position, section);
                }
                return false;
            }
        });
    }

    private void setViewType_2(RecyclerHolder holder, MultiItemEntity entity, int position) {
        ImageView ic_type = holder.obtainView(R.id.ic_type);
        TextView tv_typeName = holder.obtainView(R.id.tv_typeName);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        MWorkshopActivity activity = (MWorkshopActivity) entity;
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
    }

    private void setViewType_3(RecyclerHolder holder, MultiItemEntity entity) {
        TextView tv_addTask = holder.obtainView(R.id.tv_addTask);
        final LinearLayout ll_add_type = holder.obtainView(R.id.ll_add_type);
        TextView tv_discuss = holder.obtainView(R.id.tv_discuss);
        TextView tv_cc = holder.obtainView(R.id.tv_cc);
        TextView tv_ts = holder.obtainView(R.id.tv_ts);
        MWSActivityCrease crease = (MWSActivityCrease) entity;
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (crease.isVisible()) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        } else {
            params.height = 0;
        }
        holder.itemView.setLayoutParams(params);
        MWorkshopSection section = crease.getTag();
        final String stageId = section.getId();
        final int stageIndex = mDatas.indexOf(section);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_addTask:
                        if (ll_add_type.getVisibility() == View.VISIBLE) {
                            ll_add_type.setVisibility(View.GONE);
                        } else {
                            ll_add_type.setVisibility(View.VISIBLE);
                        }
                        break;
                    case R.id.tv_discuss:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onTaskEdit(1, stageId, stageIndex);
                        }
                        break;
                    case R.id.tv_cc:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onTaskEdit(2, stageId, stageIndex);
                        }
                        break;
                    case R.id.tv_ts:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onTaskEdit(3, stageId, stageIndex);
                        }
                        break;
                }
            }
        };
        tv_addTask.setOnClickListener(onClickListener);
        tv_discuss.setOnClickListener(onClickListener);
        tv_cc.setOnClickListener(onClickListener);
        tv_ts.setOnClickListener(onClickListener);
    }

    private void setViewType_4(RecyclerHolder holder) {
        TextView tv_position = holder.obtainView(R.id.tv_position);
        final TextView task_title = holder.obtainView(R.id.task_title);
        final TextView tv_researchTime = holder.obtainView(R.id.tv_researchTime);
        TextView task_confirm = holder.obtainView(R.id.task_confirm);
        TextView task_cancel = holder.obtainView(R.id.task_cancel);
        if (sectionSize < 10) {
            tv_position.setText("0" + sectionSize);
        } else if (sectionSize >= 10 && sectionSize <= 99) {
            tv_position.setText(String.valueOf(sectionSize));
        } else {
            tv_position.setText("..");
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.task_title:
                        if (addTaskListener != null) {
                            addTaskListener.inputTitle(task_title);
                        }
                        break;
                    case R.id.tv_researchTime:
                        if (addTaskListener != null) {
                            addTaskListener.inputTime(tv_researchTime);
                        }
                        break;
                    case R.id.task_confirm:
                        if (addTaskListener != null) {
                            addTaskListener.addTask(task_title, tv_researchTime, sectionSize);
                        }
                        break;
                    case R.id.task_cancel:
                        task_title.setText(null);
                        tv_researchTime.setText(null);
                        if (addTaskListener != null) {
                            addTaskListener.cancel();
                        }
                        break;
                }
            }
        };
        task_title.setOnClickListener(listener);
        tv_researchTime.setOnClickListener(listener);
        task_confirm.setOnClickListener(listener);
        task_cancel.setOnClickListener(listener);
    }

    public interface OnSectionLongClickListener {
        void onLongClickListener(String taskId, int position, MWorkshopSection entity);
    }

    public interface OnAddTaskListener {
        void inputTitle(TextView task_title);

        void inputTime(TextView tv_researchTime);

        void addTask(TextView task_title, TextView tv_researchTime, int sortNum);

        void cancel();
    }

    public interface OnTaskEditListener {
        void onTaskEdit(int type, String sectionId, int position);
    }
}
