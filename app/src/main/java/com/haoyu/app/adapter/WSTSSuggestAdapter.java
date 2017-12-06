package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MEvaluateSubmission;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价和建议
 * 作者:xiaoma
 */

public class WSTSSuggestAdapter extends BaseArrayRecyclerAdapter<MEvaluateSubmission> {
    private Context context;

    public WSTSSuggestAdapter(Context context, List<MEvaluateSubmission> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wsts_suggestitem;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MEvaluateSubmission item, int position) {
        ImageView iv_ico = holder.obtainView(R.id.iv_ico);
        TextView tv_name = holder.obtainView(R.id.tv_name);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        TextView tv_time = holder.obtainView(R.id.tv_time);
        if (item.getCreator() != null && item.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, item.getCreator().getAvatar(), R.drawable.user_default,
                    R.drawable.user_default, iv_ico);
        } else {
            iv_ico.setImageResource(R.drawable.user_default);
        }
        if (item.getCreator() != null) {
            tv_name.setText(item.getCreator().getRealName());
        } else {
            tv_name.setText("");
        }
        tv_content.setText(item.getComment());
        tv_time.setText(TimeUtil.getDateHR(item.getCreateTime()));
    }
}
