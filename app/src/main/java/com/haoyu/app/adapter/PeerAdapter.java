package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;

import java.util.List;

public class PeerAdapter extends BaseArrayRecyclerAdapter<MobileUser> {
    private Context context;

    public PeerAdapter(Context context, List<MobileUser> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MobileUser mUsers, int position) {
        ImageView iv = holder.obtainView(R.id.person_img);
        TextView tv = holder.obtainView(R.id.person_name);
        tv.setText(mUsers.getRealName());
        GlideImgManager.loadCircleImage(context, mUsers.getAvatar(), R.drawable.user_default, R.drawable.user_default, iv);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.peer_item;
    }
}
