package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.ResourcesEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.List;

/**
 * 创建日期：2016/11/29 on 17:46
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageResourcesAdapter extends BaseArrayRecyclerAdapter<ResourcesEntity> {
    private Context context;
    private OpenResourceCallBack callBack;

    public PageResourcesAdapter(Context context, List<ResourcesEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void setCallBack(OpenResourceCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final ResourcesEntity entity, int position) {
        RecyclerView fileLV = holder.obtainView(R.id.fileLV);
        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
        manager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        fileLV.setLayoutManager(manager);
        ResourcesListAdapter adapter = new ResourcesListAdapter(entity.getFileInfos(), entity.getTitle());
        fileLV.setAdapter(adapter);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.resources_list_item;
    }

    class ResourcesListAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {

        private String resourcesName;

        public ResourcesListAdapter(List<MFileInfo> mDatas, String resourcesName) {
            super(mDatas);
            this.resourcesName = resourcesName;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final MFileInfo entity, final int position) {
            ImageView iv_type = holder.obtainView(R.id.iv_fileType);
            TextView tv_name = holder.obtainView(R.id.tv_mFileName);
            TextView tv_size = holder.obtainView(R.id.tv_mFileSize);
            Common.setFileType(entity.getUrl(), iv_type);
            tv_name.setText(resourcesName);
            tv_size.setText(Common.FormetFileSize(entity.getFileSize()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.open(resourcesName, entity);
                    }
                }
            });
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.fileinfo_item;
        }
    }

    public interface OpenResourceCallBack {
        void open(String resourcesName, MFileInfo mFileInfo);
    }
}
