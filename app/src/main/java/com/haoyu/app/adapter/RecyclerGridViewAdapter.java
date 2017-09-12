package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;

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
            personName.setText("匿名用户");
        }
        if (mUsers.getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, mUsers.getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, personImg);
        } else {
            personImg.setImageResource(R.drawable.user_default);
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.peer_item;
    }


}
