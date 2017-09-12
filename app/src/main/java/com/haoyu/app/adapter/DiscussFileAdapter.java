package com.haoyu.app.adapter;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;

import java.util.List;

/**
 * Created by acer1 on 2017/2/6.
 * 研讨文件列表
 */
public class DiscussFileAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {
    private Activity mContext;

    public DiscussFileAdapter(Activity context, List<MFileInfo> mDatas) {
        super(mDatas);
        this.mContext = context;

    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MFileInfo entity, int position) {
        ImageView iv_type = holder.obtainView(R.id.resourcesType);
        TextView resourcesName = holder.obtainView(R.id.resourcesName);
        TextView resourcesSize = holder.obtainView(R.id.resourcesSize);
        resourcesName.setText(entity.getFileName());
        Common.setFileType(entity.getUrl(),iv_type);
        resourcesSize.setText(Common.FormetFileSize(entity.getFileSize()));
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.resources_item;
    }

    public interface OpenResourceCallBack {
        void open(MFileInfo mFileInfo);

    }
}
