package com.haoyu.app.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;

import java.util.List;

/**
 * 创建日期：2017/10/13 on 16:30
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfoAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {
    private boolean hideDivider;

    public MFileInfoAdapter(List<MFileInfo> mDatas) {
        super(mDatas);
    }

    public MFileInfoAdapter(List<MFileInfo> mDatas, boolean hideDivider) {
        super(mDatas);
        this.hideDivider = hideDivider;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.fileinfo_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MFileInfo fileInfo, int position) {
        ImageView iv_fileType = holder.obtainView(R.id.iv_fileType);
        TextView tv_mFileName = holder.obtainView(R.id.tv_mFileName);
        TextView tv_mFileSize = holder.obtainView(R.id.tv_mFileSize);
        View divider = holder.obtainView(R.id.divider);
        Common.setFileType(fileInfo.getUrl(), iv_fileType);
        tv_mFileName.setText(fileInfo.getFileName());
        tv_mFileSize.setText(Common.FormetFileSize(fileInfo.getFileSize()));
        if (hideDivider) {
            divider.setVisibility(View.GONE);
        } else {
            if (position == getItemCount() - 1) {
                divider.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }
        }
    }
}
