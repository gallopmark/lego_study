package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * 创建日期：2017/3/6 on 9:08
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppTestResultGridAdapter extends BaseArrayRecyclerAdapter<Boolean> {
    private Context context;

    public AppTestResultGridAdapter(Context context, List<Boolean> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_test_result_grid_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, Boolean correct, int position) {
        TextView tv_result = holder.obtainView(R.id.tv_result);
        tv_result.setText(String.valueOf(position + 1));
        if (correct) {
            tv_result.setTextColor(ContextCompat.getColor(context, R.color.black));
            tv_result.setBackgroundResource(R.drawable.test_answer_true);
        } else {
            tv_result.setTextColor(ContextCompat.getColor(context, R.color.white));
            tv_result.setBackgroundResource(R.drawable.test_answer_false);
        }
    }
}
