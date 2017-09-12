package com.haoyu.app.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.activity.AppMultiImageShowActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MyTrainWorkShopResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/7 on 12:48
 * 描述: 我的培训工作坊列表适配器
 * 作者:马飞奔 Administrator
 */
public class MyTrainWorkShopListAdapter extends BaseArrayRecyclerAdapter<MyTrainWorkShopResult> {
    private Activity context;
    private int imageWidth;
    private int imageHeight;

    public MyTrainWorkShopListAdapter(Activity context, List<MyTrainWorkShopResult> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - 20;
        imageHeight = imageWidth / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MyTrainWorkShopResult result, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView img = holder.obtainView(R.id.workshop_img);
        img.setLayoutParams(params);
        TextView workshop_title = holder.obtainView(R.id.workshop_title);
        TextView workshop_time = holder.obtainView(R.id.workshop_time);
        TextView tv_score = holder.obtainView(R.id.tv_score);
        View divider = holder.obtainView(R.id.divider);
        if (result.getmWorkshop() != null && result.getmWorkshop().getImageUrl() != null) {
            GlideImgManager.loadImage(context, result.getmWorkshop().getImageUrl(), R.drawable.app_default, R.drawable.app_default, img);
        } else {
            img.setImageResource(R.drawable.app_default);
        }
        if (result.getmWorkshop() != null)
            workshop_title.setText(result.getmWorkshop().getTitle());
        else
            workshop_title.setText(null);
        if (result.getmWorkshop() != null) {
            workshop_time.setText(result.getmWorkshop().getStudyHours() + "学时");
        } else {
            workshop_time.setText("0学时");
        }
        if (result.getmWorkshop() != null) {
            tv_score.setText("获得" + result.getPoint() + " / " + result.getmWorkshop().getQualifiedPoint() + "积分");
        } else {
            tv_score.setText("获得" + result.getPoint() + "/" + "0积分");
        }
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.getmWorkshop() != null) {
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(result.getmWorkshop().getImageUrl());
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
        return R.layout.mytrain_workshop_list_item;
    }
}
