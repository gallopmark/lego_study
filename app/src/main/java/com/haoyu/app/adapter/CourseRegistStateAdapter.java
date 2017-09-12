package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.MCourseRegister;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/5/23 on 16:52
 * 描述: 选课列表适配器
 * 作者:马飞奔 Administrator
 */
public class CourseRegistStateAdapter extends BaseArrayRecyclerAdapter<MCourseRegister> {

    private Context context;
    private int imageWidth;
    private int imageHeight;
    private boolean visiable;
    private boolean swipeEnable = true;
    private ArrayMap<Integer, Boolean> isSelected = new ArrayMap<>();
    private CancelCallBack callBack;

    public CourseRegistStateAdapter(Context context, List<MCourseRegister> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 5 * 2 - 20;
        imageHeight = imageWidth / 3 * 2;
        init();
    }

    private void init() {
        for (int i = 0; i < mDatas.size(); i++) {
            isSelected.put(i, false);
        }
    }

    public void addAll(List<MCourseRegister> mDatas) {
        this.mDatas.addAll(mDatas);
        notifyDataSetChanged();
        init();
    }

    public void selectAll() {
        for (Integer i : isSelected.keySet()) {
            isSelected.put(i, true);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        for (Integer i : isSelected.keySet()) {
            isSelected.put(i, false);
        }
        swipeEnable = true;
        visiable = true;
        notifyDataSetChanged();
    }

    public void cancel(){
        for (Integer i : isSelected.keySet()) {
            isSelected.put(i, false);
        }
        swipeEnable = true;
        visiable = false;
        notifyDataSetChanged();
    }

    public void setCancelCallBack(CancelCallBack callBack) {
        this.callBack = callBack;
    }

    public void setEdit() {
        visiable = true;
        swipeEnable = false;
        notifyDataSetChanged();
    }

    public ArrayMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.course_regist_state_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MCourseRegister mCourseRegister, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        View contentView = holder.obtainView(R.id.contentView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        final CheckBox cb_select = holder.obtainView(R.id.cb_select);
        ImageView course_img = holder.obtainView(R.id.course_img);
        TextView course_title = holder.obtainView(R.id.course_title);
        TextView course_type = holder.obtainView(R.id.course_type);
        TextView course_period = holder.obtainView(R.id.course_period);
        TextView course_enroll = holder.obtainView(R.id.course_enroll);
        Button bt_cancel = holder.obtainView(R.id.bt_cancel);
        course_img.setLayoutParams(params);
        swipeLayout.setIos(true);
        swipeLayout.setSwipeEnable(swipeEnable);
        if (visiable) {
            cb_select.setVisibility(View.VISIBLE);
        } else {
            cb_select.setVisibility(View.GONE);
        }
        cb_select.setChecked(isSelected.get(position));
        CourseMobileEntity entity = mCourseRegister.getmCourse();
        if (entity != null && entity.getImage() != null && entity.getImage().length() > 0) {
            GlideImgManager.loadImage(context, entity.getImage(), R.drawable.app_default, R.drawable.app_default, course_img);
        } else {
            course_img.setImageResource(R.drawable.app_default);
        }
        if (entity != null && entity.getTitle() != null) {
            course_title.setText(entity.getTitle());
        } else {
            course_title.setText("无标题");
        }
        if (entity != null && entity.getType() != null) {
            course_type.setText(entity.getType());
        } else {
            course_type.setText("未知类型");
        }
        if (entity != null) {
            course_period.setText(String.valueOf(entity.getStudyHours()) + "学时");
        } else {
            course_period.setText("未知学时");
        }
        if (entity != null) {
            course_enroll.setText(entity.getRegisterNum() + "人报读");
        } else {
            course_enroll.setText("0人报读");
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.contentView:
                        swipeLayout.smoothClose();
                        if (cb_select.isChecked())
                            cb_select.setChecked(false);
                        else
                            cb_select.setChecked(true);
                        setChecked(position, cb_select.isChecked());
                        break;
                    case R.id.cb_select:
                        setChecked(position, cb_select.isChecked());
                        break;
                    case R.id.bt_cancel:
                        swipeLayout.smoothClose();
                        if (callBack != null) {
                            callBack.cancel(mCourseRegister, position);
                        }
                        break;
                }
            }
        };
        contentView.setOnClickListener(listener);
        cb_select.setOnClickListener(listener);
        bt_cancel.setOnClickListener(listener);
    }

    private void setChecked(int position, boolean isChecked) {
        isSelected.put(position, isChecked);
        if (selectListener != null) {
            selectListener.onSelect(isSelected);
        }
        notifyDataSetChanged();
    }

    private OnSelectListener selectListener;

    public void setOnSelectListener(OnSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public interface OnSelectListener {
        void onSelect(ArrayMap<Integer, Boolean> isSelected);
    }

    public interface CancelCallBack {
        void cancel(MCourseRegister mCourseRegister, int position);
    }
}
