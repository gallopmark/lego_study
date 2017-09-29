package com.haoyu.app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.List;

/**
 * 创建日期：2017/1/3 on 15:09
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class WorkShopSectionAdapter extends BaseArrayRecyclerAdapter<MWorkshopSection> {
    private Context mContext;
    private ActivityItemCallBack itemCallBack;
    private List<MWorkshopSection> sectionList;
    private ArrayMap<Integer, Boolean> arrayMap = new ArrayMap<>();
    private int viewType;
    private boolean addTask;
    private OnAddTaskListener addTaskListener;
    private OnTaskEditListener onTaskEditListener;
    private AddActivityCallBack addActivityCallBack;
    private int mainPosition = -1, childPosition = -1;

    public WorkShopSectionAdapter(Context context, List<MWorkshopSection> mDatas) {
        super(mDatas);
        this.sectionList = mDatas;
        this.mContext = context;
    }

    public void setItemCallBack(ActivityItemCallBack itemCallBack) {
        this.itemCallBack = itemCallBack;
    }

    public void setAddTask(boolean addTask) {
        this.addTask = addTask;
        notifyDataSetChanged();
    }

    public void setAddTaskListener(OnAddTaskListener addTaskListener) {
        this.addTaskListener = addTaskListener;
    }

    public void setOnTaskEditListener(OnTaskEditListener onTaskEditListener) {
        this.onTaskEditListener = onTaskEditListener;
    }

    public void addAll(List<MWorkshopSection> mDatas) {
        sectionList.addAll(mDatas);
        for (int i = 0; i < sectionList.size(); i++) {
            if (i == 0) {
                arrayMap.put(i, true);
            } else {
                arrayMap.put(i, false);
            }
        }
        notifyDataSetChanged();
    }

    public void setAddActivityCallBack(AddActivityCallBack addActivityCallBack) {
        this.addActivityCallBack = addActivityCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MWorkshopSection entity, final int position) {
        if (viewType == 0) {
            View ll_content = holder.obtainView(R.id.ll_content);
            final LinearLayout ll_add_type = holder.obtainView(R.id.ll_add_type);
            ll_add_type.setVisibility(View.GONE);
            TextView tv_position = holder.obtainView(R.id.tv_position);
            TextView tv_title = holder.obtainView(R.id.tv_title);
            final ImageView iv_isExpand = holder.obtainView(R.id.iv_isExpand);
            TextView tv_researchTime = holder.obtainView(R.id.tv_researchTime);
            final RecyclerView atRecyclerView = holder.obtainView(R.id.atRecyclerView);
            atRecyclerView.setNestedScrollingEnabled(false);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(mContext);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            atRecyclerView.setLayoutManager(layoutManager);
            atRecyclerView.setFocusable(false);
            WorkShopAtAdapter adapter = new WorkShopAtAdapter(entity.getActivities(), position);
            atRecyclerView.setAdapter(adapter);
            if (entity.getActivities().size() > 0)
                iv_isExpand.setVisibility(View.VISIBLE);
            else
                iv_isExpand.setVisibility(View.GONE);
            if (arrayMap.get(position) != null && arrayMap.get(position)) {
                atRecyclerView.setVisibility(View.VISIBLE);
                iv_isExpand.setImageResource(R.drawable.course_dictionary_xiala);
            } else {
                atRecyclerView.setVisibility(View.GONE);
                iv_isExpand.setImageResource(R.drawable.progress_goto);
            }
            //添加任务
            TextView task_add = holder.obtainView(R.id.tv_addTask);
            TextView ll_discuss = holder.obtainView(R.id.tv_discuss);
            TextView ll_query = holder.obtainView(R.id.tv_query);
            TextView ll_course = holder.obtainView(R.id.tv_course);
            if (position + 1 < 10) {
                tv_position.setText("0" + (position + 1));
            } else if (position + 1 >= 10 && position + 1 <= 99) {
                tv_position.setText(String.valueOf(position + 1));
            } else {
                tv_position.setText("..");
            }
            tv_position.setText(position + 1 < 10 ? "0" + (position + 1) : String.valueOf(position + 1));
            tv_title.setText(entity.getTitle());
            if (entity.getTimePeriod() != null) {
                tv_researchTime.setText("研修时间：" +
                        TimeUtil.getDateYM(entity.getTimePeriod().getStartTime()) + "-"
                        + TimeUtil.getDateYM(entity.getTimePeriod().getEndTime()));
            } else {
                tv_researchTime.setText("研修时间：未知");
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.ll_content:
                            if (atRecyclerView.getVisibility() == View.VISIBLE) {
                                arrayMap.put(position, false);
                                atRecyclerView.setVisibility(View.GONE);
                                iv_isExpand.setImageResource(R.drawable.progress_goto);
                            } else {
                                arrayMap.put(position, true);
                                atRecyclerView.setVisibility(View.VISIBLE);
                                iv_isExpand.setImageResource(R.drawable.course_dictionary_xiala);
                            }
                            break;
                        case R.id.tv_addTask:
                            ll_add_type.setFocusable(true);
                            if (ll_add_type.getVisibility() == View.GONE) {
                                ll_add_type.setVisibility(View.VISIBLE);
                            } else {
                                ll_add_type.setVisibility(View.GONE);
                            }
                            break;
                        case R.id.tv_discuss:
                        /*添加研讨*/
                            if (addActivityCallBack != null) {
                                addActivityCallBack.addActivity(1, entity.getId(), position);
                            }
                            break;
                        case R.id.tv_query:
                        /*添加教学观摩*/
                            if (addActivityCallBack != null) {
                                addActivityCallBack.addActivity(2, entity.getId(), position);
                            }
                            break;
                        case R.id.tv_course:
                        /*添加听课评课*/
                            if (addActivityCallBack != null) {
                                addActivityCallBack.addActivity(3, entity.getId(), position);
                            }
                            break;
                    }
                }
            };
            ll_content.setOnClickListener(listener);
            task_add.setOnClickListener(listener);
            ll_discuss.setOnClickListener(listener);
            ll_query.setOnClickListener(listener);
            ll_course.setOnClickListener(listener);
            ll_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showTaskEditDialog(entity.getId(), position, entity);
                    return true;
                }
            });
        } else {
            TextView tv_position = holder.obtainView(R.id.tv_position);
            if (position + 1 < 10) {
                tv_position.setText("0" + (position + 1));
            } else if (position + 1 >= 10 && position + 1 <= 99) {
                tv_position.setText(String.valueOf(position + 1));
            } else {
                tv_position.setText("..");
            }
            final TextView task_title = holder.obtainView(R.id.task_title);
            final TextView tv_researchTime = holder.obtainView(R.id.tv_researchTime);
            TextView task_confirm = holder.obtainView(R.id.task_confirm);
            TextView task_cancel = holder.obtainView(R.id.task_cancel);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.task_title:
                            if (addTaskListener != null)
                                addTaskListener.inputTitle(task_title);
                            break;
                        case R.id.tv_researchTime:
                            if (addTaskListener != null)
                                addTaskListener.inputTime(tv_researchTime);
                            break;
                        case R.id.task_confirm:
                            if (addTaskListener != null) {
                                addTaskListener.addTask(task_title, tv_researchTime, position + 1);
                            }
                            break;
                        case R.id.task_cancel:
                            task_title.setText(null);
                            tv_researchTime.setText(null);
                            setAddTask(false);
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
    }

    private void showTaskEditDialog(final String taskId, final int position, final MWorkshopSection entity) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_workshop_task, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (ScreenUtils.getScreenWidth(mContext) / 4 * 3),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv_addTask = view.findViewById(R.id.tv_addTask);
        TextView tv_alterTask = view.findViewById(R.id.tv_alterTask);
        TextView tv_deleteTask = view.findViewById(R.id.tv_deleteTask);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_addTask:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onAdd();
                        }
                        dialog.dismiss();
                        break;
                    case R.id.tv_alterTask:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onAlter(taskId, position, entity);
                        }
                        dialog.dismiss();
                        break;
                    case R.id.tv_deleteTask:
                        if (onTaskEditListener != null) {
                            onTaskEditListener.onDelete(taskId, position);
                        }
                        dialog.dismiss();
                        break;
                }
            }
        };
        tv_addTask.setOnClickListener(listener);
        tv_alterTask.setOnClickListener(listener);
        tv_deleteTask.setOnClickListener(listener);
        dialog.show();
        dialog.setContentView(view, params);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == 1)
            return R.layout.workshop_add_task;
        else
            return R.layout.workshop_section_item;
    }

    @Override
    public int getItemCount() {
        if (addTask)
            return mDatas.size() + 1;
        else
            return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDatas.size()) {
            viewType = 1;
        } else {
            viewType = 0;
        }
        return viewType;
    }

    class WorkShopAtAdapter extends BaseArrayRecyclerAdapter<MWorkshopActivity> {

        private int mainIndex;

        public WorkShopAtAdapter(List<MWorkshopActivity> mDatas, int mainIndex) {
            super(mDatas);
            this.mainIndex = mainIndex;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final MWorkshopActivity entity, final int position) {
            SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
            swipeLayout.setIos(true);
            View contentView = holder.obtainView(R.id.contentView);
            ImageView ic_type = holder.obtainView(R.id.ic_type);
            TextView tv_typeName = holder.obtainView(R.id.tv_typeName);
            TextView tv_title = holder.obtainView(R.id.tv_title);
            Button bt_delete = holder.obtainView(R.id.bt_delete);
            if (entity.getType() != null && entity.getType().equals("discussion")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.ws_discuss_press);
                } else
                    ic_type.setImageResource(R.drawable.ws_discuss_default);
                tv_typeName.setText("教学研讨");
            } else if (entity.getType() != null && entity.getType().equals("survey")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.ws_questionnaire_press);
                } else
                    ic_type.setImageResource(R.drawable.ws_questionnaire_default);
                tv_typeName.setText("调查问卷");
            } else if (entity.getType().equals("debate")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.ws_bianlun_press);
                } else
                    ic_type.setImageResource(R.drawable.ws_bianlun_default);
                tv_typeName.setText("在线辩论");
            } else if (entity.getType().equals("lcec")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.ws_tingke_press);
                } else
                    ic_type.setImageResource(R.drawable.ws_tingke_default);
                tv_typeName.setText("听课评课");
            } else if (entity.getType().equals("lesson_plan")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.ws_beike_press);
                } else
                    ic_type.setImageResource(R.drawable.ws_beike_default);
                tv_typeName.setText("集体备课");
            } else if (entity.getType().equals("test")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.progress_test_press);
                } else
                    ic_type.setImageResource(R.drawable.progress_test_default);
                tv_typeName.setText("在线测验");
            } else if (entity.getType().equals("video")) {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.progress_video_press);
                } else
                    ic_type.setImageResource(R.drawable.progress_video_default);
                tv_typeName.setText("教学观摩");
            } else {
                if (mainPosition == mainIndex && childPosition == position) {
                    ic_type.setImageResource(R.drawable.course_word_selected);
                } else
                    ic_type.setImageResource(R.drawable.course_word_default);
                tv_typeName.setText("类型未知");
            }
            if (mainPosition == mainIndex && childPosition == position) {
                tv_typeName.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
                tv_title.setTextColor(ContextCompat.getColor(mContext, R.color.defaultColor));
            } else {
                tv_typeName.setTextColor(ContextCompat.getColor(mContext, R.color.blow_gray));
                tv_title.setTextColor(ContextCompat.getColor(mContext, R.color.line_bottom));
            }
            if (entity.getTitle() != null && entity.getTitle().trim().length() > 0) {
                tv_title.setText(entity.getTitle());
            } else
                tv_title.setText("无标题");
            tv_title.setText(entity.getTitle());
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.contentView:
                            setPress(mainIndex, position);
                            if (itemCallBack != null) {
                                itemCallBack.itemCallBack(entity, mainIndex);
                            }
                            break;
                        case R.id.bt_delete:
                            if (itemCallBack != null) {
                                itemCallBack.onDelete(mainIndex, position);
                            }
                            break;
                    }
                }
            };
            contentView.setOnClickListener(listener);
            bt_delete.setOnClickListener(listener);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.workshop_activity_edit_item;
        }

    }

    private void setPress(int mainIndex, int position) {
        mainPosition = mainIndex;
        childPosition = position;
        notifyDataSetChanged();
    }

    public void setPressIndex(int mainIndex, int position) {
        mainPosition = mainIndex;
        if (position == childPosition) {
            childPosition = -1;
        } else if (position < childPosition) {
            childPosition -= 1;
        }
        notifyDataSetChanged();
    }

    public interface ActivityItemCallBack {
        void itemCallBack(MWorkshopActivity activity, int mainPosition);

        void onDelete(int mainIndex, int position);
    }

    public interface OnAddTaskListener {
        void inputTitle(TextView task_title);

        void inputTime(TextView tv_researchTime);

        void addTask(TextView task_title, TextView tv_researchTime, int sortNum);

        void cancel();
    }

    public interface OnTaskEditListener {
        void onAdd();

        void onAlter(String taskId, int position, MWorkshopSection entity);

        void onDelete(String taskId, int position);
    }

    public interface AddActivityCallBack {
        void addActivity(int type, String workSectionId, int position);
    }
}
