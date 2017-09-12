package com.haoyu.app.pickerlib;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/6/16 on 11:06
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MediaFolderAdapter extends BaseArrayRecyclerAdapter<MediaFolder> {
    private int imageWidth;
    private int imageHeight;

    public MediaFolderAdapter(Context context, List<MediaFolder> mDatas) {
        super(mDatas);
        imageWidth = ScreenUtils.getScreenWidth(context) / 4;
        imageHeight = imageWidth;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.pickerlib_folder_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MediaFolder folder, int position) {
        ImageView first_image = holder.obtainView(R.id.first_image);
        TextView tv_folder_name = holder.obtainView(R.id.tv_folder_name);
        TextView image_num = holder.obtainView(R.id.image_num);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                imageWidth, imageHeight);
        first_image.setLayoutParams(params);
        String name = folder.getName();
        int imageNum = folder.getMediaItems().size();
        String imagePath = folder.getFirstImagePath();
        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .crossFade()
                .override(180, 180)
                .into(first_image);
        image_num.setText("(" + imageNum + ")");
        tv_folder_name.setText(name);
    }
}
