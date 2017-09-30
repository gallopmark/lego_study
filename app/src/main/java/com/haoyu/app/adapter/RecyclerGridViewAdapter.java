package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * Created by acer1 on 2017/1/11.
 */
public class RecyclerGridViewAdapter extends BaseArrayRecyclerAdapter<MobileUser> {
    private Context mContext;

    public RecyclerGridViewAdapter(Context activity, List<MobileUser> mDatas) {
        super(mDatas);
        this.mContext = activity;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MobileUser mUsers, int position) {
        ImageView personImg = holder.obtainView(R.id.person_img);
        TextView personName = holder.obtainView(R.id.person_name);
        if (mUsers.getRealName() != null) {
            personName.setText(mUsers.getRealName());
        } else {
            personName.setText("");
        }
        Glide.with(mContext)
                .load(mUsers.getAvatar())
                .placeholder(R.drawable.user_default) //设置占位图
                .error(R.drawable.user_default) //设置错误图片
                .crossFade() //设置淡入淡出效果，默认300ms，可以传参
                //.dontAnimate() //不显示动画效果
                .into(personImg);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.peer_item;
    }


}
