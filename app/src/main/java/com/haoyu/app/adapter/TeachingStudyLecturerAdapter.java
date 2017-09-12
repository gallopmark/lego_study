package com.haoyu.app.adapter;

import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * Created by acer1 on 2017/2/16.
 */
public class TeachingStudyLecturerAdapter extends BaseArrayRecyclerAdapter<MobileUser> {

    public TeachingStudyLecturerAdapter(List<MobileUser> mDatas) {
        super(mDatas);
    }


    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_study_lecture_item;
    }


    @Override
    public void onBindHoder(RecyclerHolder holder, final MobileUser mobileUser, int position) {
        TextView tv_name = holder.obtainView(R.id.tv_name);
        if (mobileUser.getRealName() != null) {
            tv_name.setText(mobileUser.getRealName());
        } else {
            tv_name.setText("匿名用户");
        }

    }
}
