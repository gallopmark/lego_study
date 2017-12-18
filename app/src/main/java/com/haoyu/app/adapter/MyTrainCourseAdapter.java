package com.haoyu.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MyTrainCourseResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/1/7 on 11:50
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MyTrainCourseAdapter extends BaseArrayRecyclerAdapter<MyTrainCourseResult> {
    private Activity context;
    private int width;
    private int height;

    public MyTrainCourseAdapter(Activity context, List<MyTrainCourseResult> mDatas) {
        super(mDatas);
        this.context = context;
        width = ScreenUtils.getScreenWidth(context) / 3 - 20;
        height = width / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MyTrainCourseResult entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        ImageView course_img = holder.obtainView(R.id.course_img);
        TextView course_title = holder.obtainView(R.id.course_title);
        TextView course_type = holder.obtainView(R.id.course_type);
        TextView course_period = holder.obtainView(R.id.course_period);
        TextView course_enroll = holder.obtainView(R.id.course_enroll);
        View divider = holder.obtainView(R.id.divider);
        course_img.setLayoutParams(params);
        if (entity.getmCourse() != null && entity.getmCourse().getImage() != null) {
            GlideImgManager.loadImage(context, entity.getmCourse().getImage(), R.drawable.app_default, R.drawable.app_default, course_img);
        } else {
            course_img.setImageResource(R.drawable.app_default);
        }
        if (entity.getmCourse() != null) {
            course_title.setText(entity.getmCourse().getTitle());
            course_type.setText(entity.getmCourse().getType());
            course_period.setText(String.valueOf(entity.getmCourse().getStudyHours()) + "学时");
            course_enroll.setText(entity.getmCourse().getRegisterNum() + "人报读");
        } else {
            course_title.setText(null);
            course_type.setText(null);
            course_period.setText("0学时");
            course_enroll.setText("0人报读");
        }
        if (position != getItemCount() - 1) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }

    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.mytraincourse_item;
    }
}
