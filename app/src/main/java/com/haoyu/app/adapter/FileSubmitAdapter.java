package com.haoyu.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.utils.Common;

import java.util.List;

/**
 * Created by acer1 on 2017/2/13.
 * 作业上传文件
 */
public class FileSubmitAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {

    private Activity context;
    private onDisposeCallBack disposeCallBack;

    public FileSubmitAdapter(List<MFileInfo> mDatas) {
        super(mDatas);
    }

    public void setDisposeCallBack(onDisposeCallBack disposeCallBack) {
        this.disposeCallBack = disposeCallBack;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.resources_item3;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MFileInfo entity, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        View contentView = holder.obtainView(R.id.contentView);
        swipeLayout.setIos(true);
        ImageView iv_type = holder.obtainView(R.id.resourcesType);
        TextView tv_name = holder.obtainView(R.id.resourcesName);
        TextView tv_size = holder.obtainView(R.id.resourcesSize);
        Button bt_alter = holder.obtainView(R.id.bt_alter);
        Button bt_delete = holder.obtainView(R.id.bt_delete);
        Common.setFileType(entity.getUrl(),iv_type);
        tv_name.setText(entity.getFileName());
        tv_size.setText(Common.FormetFileSize(entity.getFileSize()));
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.contentView:
                        swipeLayout.smoothClose();
                        break;
                    case R.id.bt_delete:
                        swipeLayout.smoothClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onDelete(position);
                        }
                        break;
                }
            }
        };
        contentView.setOnClickListener(listener);
        bt_alter.setOnClickListener(listener);
        bt_delete.setOnClickListener(listener);
    }

    public interface onDisposeCallBack {
        void onDelete(int position);
    }
}
