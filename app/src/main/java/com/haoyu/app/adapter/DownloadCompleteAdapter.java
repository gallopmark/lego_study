package com.haoyu.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;

import org.wlf.filedownloader.DownloadFileInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2017/7/7 on 11:07
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DownloadCompleteAdapter extends BaseArrayRecyclerAdapter<DownloadFileInfo> {
    private Context context;
    private boolean edit = false;
    private List<DownloadFileInfo> mSelected = new ArrayList<>();
    private Map<Integer, Boolean> hashMap = new HashMap<>();
    private OnSelectedListener onSelectedListener;
    private OnItemClickListener onItemClickListener;

    public DownloadCompleteAdapter(Context context, List<DownloadFileInfo> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit, OnSelectedListener onSelectedListener) {
        this.edit = edit;
        this.onSelectedListener = onSelectedListener;
        notifyDataSetChanged();
    }

    public void selecetAll() {
        for (int i = 0; i < mDatas.size(); i++) {
            hashMap.put(i, true);
        }
        mSelected.clear();
        mSelected.addAll(mDatas);
        notifyDataSetChanged();
    }

    public void cancelAll() {
        for (int i = 0; i < mDatas.size(); i++) {
            hashMap.put(i, false);
        }
        notifyDataSetChanged();
    }

    public List<DownloadFileInfo> getmSelected() {
        return mSelected;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.download_complete_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final DownloadFileInfo fileInfo, final int position) {
        final CheckBox checkBox = holder.obtainView(R.id.checkBox);
        ImageView iv_fileType = holder.obtainView(R.id.iv_fileType);
        TextView tv_fileName = holder.obtainView(R.id.tv_fileName);
        iv_fileType.setImageResource(R.drawable.course_video_default);
        tv_fileName.setText(fileInfo.getFileName());
        if (edit) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        hashMap.put(position, false);
                    } else {
                        checkBox.setChecked(true);
                        hashMap.put(position, true);
                    }
                } else {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(fileInfo);
                    }
                }
            }
        });
        checkBox.setChecked(hashMap.get(position) == null ? false : hashMap.get(position));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                if (isCheck) {
                    if (!mSelected.contains(fileInfo)) {
                        mSelected.add(fileInfo);
                    }
                } else {
                    mSelected.remove(fileInfo);
                }
                if (onSelectedListener != null) {
                    onSelectedListener.onSelected(mSelected);
                }
            }
        });
    }

    public interface OnSelectedListener {
        void onSelected(List<DownloadFileInfo> mSelect);
    }

    public interface OnItemClickListener {
        void onItemClick(DownloadFileInfo fileInfo);
    }
}
