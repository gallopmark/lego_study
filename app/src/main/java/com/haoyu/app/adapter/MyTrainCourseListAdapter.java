package com.haoyu.app.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.activity.AppMultiImageShowActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MyTrainCourseResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/7 on 11:50
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MyTrainCourseListAdapter extends BaseArrayRecyclerAdapter<MyTrainCourseResult> {
    private Activity context;
    private int imageWidth;
    private int imageHeight;

    public MyTrainCourseListAdapter(Activity context, List<MyTrainCourseResult> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - 20;
        imageHeight = imageWidth / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MyTrainCourseResult entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView course_img = holder.obtainView(R.id.course_img);
        TextView course_title = holder.obtainView(R.id.course_title);
        TextView course_type = holder.obtainView(R.id.course_type);
        TextView course_period = holder.obtainView(R.id.course_period);
        TextView course_enroll = holder.obtainView(R.id.course_enroll);
        View divider = holder.obtainView(R.id.divider);
        course_img.setLayoutParams(params);
        if (entity.getmCourse() != null && entity.getmCourse().getImage() != null)
            GlideImgManager.loadImage(context, entity.getmCourse().getImage(), R.drawable.app_default, R.drawable.app_default, course_img);
        else
            course_img.setImageResource(R.drawable.app_default);
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
        course_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entity.getmCourse() != null) {
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(entity.getmCourse().getImage());
                    Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                    intent.putStringArrayListExtra("photos", imgList);
                    intent.putExtra("position", 0);
                    context.startActivity(intent);
                    context.overridePendingTransition(R.anim.zoom_in, 0);
                }
            }
        });
        if (position != getItemCount() - 1)
            divider.setVisibility(View.VISIBLE);
        else
            divider.setVisibility(View.GONE);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.mytrain_course_list_item;
    }
}
