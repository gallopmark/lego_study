package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/1/3 on 13:38
 * 描述:侧滑菜单工作坊群——工作坊列表适配器
 * 作者:马飞奔 Administrator
 */
public class WorkShopGroupAdapter extends BaseArrayRecyclerAdapter<WorkShopMobileEntity> {
    private Context context;
    private int imageWidth;
    private int imageHeight;

    public WorkShopGroupAdapter(Context context, List<WorkShopMobileEntity> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - 20;
        imageHeight = imageWidth / 3 * 2;
    }


    @Override
    public void onBindHoder(RecyclerHolder holder, WorkShopMobileEntity entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView img = holder.obtainView(R.id.workshop_img);
        img.setLayoutParams(params);
        TextView workshop_title = holder.obtainView(R.id.workshop_title);
        TextView workshop_time = holder.obtainView(R.id.workshop_time);
        TextView tv_workshopType = holder.obtainView(R.id.tv_workshopType);
        if (entity.getImageUrl() != null) {
            GlideImgManager.loadImage(context, entity.getImageUrl(), R.drawable.app_default, R.drawable.app_default, img);
        } else {
            img.setImageResource(R.drawable.app_default);
        }
        workshop_title.setText(entity.getTitle());
        workshop_time.setText(entity.getStudyHours() + "学时");
        if (entity.getType().equals("personal")) {
            tv_workshopType.setText("个人工作坊");
        } else if (entity.getType().equals("train")) {
            tv_workshopType.setText("项目工作坊");
        } else if (entity.getType().equals("template")) {
            tv_workshopType.setText("示范性工作坊");
        } else {
            tv_workshopType.setText("未知类型");
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.workshop_list_item;
    }
}
