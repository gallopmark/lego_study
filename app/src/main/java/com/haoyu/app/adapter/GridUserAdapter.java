package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/12/7.
 * 描述:搜索成员适配器
 * 作者:xiaoma
 */

public class GridUserAdapter extends BaseArrayRecyclerAdapter<MobileUser> {
    private int width;
    private int dp_8;

    public GridUserAdapter(Context context, List<MobileUser> mDatas) {
        super(mDatas);
        width = ScreenUtils.getScreenWidth(context) / 4;
        dp_8 = PixelFormat.dp2px(context, 8);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.griduser_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MobileUser user, int position) {
        TextView tv_name = holder.obtainView(R.id.tv_name);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv_name.getLayoutParams();
        params.width = width;
        tv_name.setLayoutParams(params);
        tv_name.setPadding(0, dp_8, 0, dp_8);
        tv_name.setText(user.getRealName());
    }
}
