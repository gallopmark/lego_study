package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/1/6 on 16:27
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class RegistCourseAdapter extends BaseArrayRecyclerAdapter<CourseMobileEntity> {
    private Context context;
    private int imageWidth;
    private int imageHeight;

    public RegistCourseAdapter(Context context, List<CourseMobileEntity> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 5 * 2 - 20;
        imageHeight = imageWidth / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, CourseMobileEntity entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView course_img = holder.obtainView(R.id.course_img);
        TextView course_title = holder.obtainView(R.id.course_title);
        TextView course_type = holder.obtainView(R.id.course_type);
        TextView course_period = holder.obtainView(R.id.course_period);
        TextView course_enroll = holder.obtainView(R.id.course_enroll);
        course_img.setLayoutParams(params);
        if (entity.getImage() != null && entity.getImage().length() > 0) {
            GlideImgManager.loadImage(context, entity.getImage(), R.drawable.app_default, R.drawable.app_default, course_img);
        } else {
            course_img.setImageResource(R.drawable.app_default);
        }
        if (entity.getTitle() != null) {
            course_title.setText(entity.getTitle());
        } else {
            course_title.setText("无标题");
        }
        if (entity.getType() != null) {
            course_type.setText(entity.getType());
        } else {
            course_type.setText("未知类型");
        }
        course_period.setText(String.valueOf(entity.getStudyHours()) + "学时");
        course_enroll.setText(entity.getRegisterNum() + "人报读");
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.regist_course_item;
    }
}
