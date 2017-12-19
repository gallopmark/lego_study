package com.haoyu.app.filePicker;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * 文件列表适配器
 */
public class FileFilterAdapter extends BaseArrayRecyclerAdapter<File> {

    public interface OnItemClickListener {
        void click(int position);
    }

    public OnItemClickListener onItemClickListener;
    private FileFilter mFileFilter;
    private boolean[] mCheckedFlags;
    private boolean mMutilyMode;

    public FileFilterAdapter(List<File> mDatas, FileFilter mFileFilter, boolean mMutilyMode) {
        super(mDatas);
        this.mFileFilter = mFileFilter;
        this.mMutilyMode = mMutilyMode;
        mCheckedFlags = new boolean[mDatas.size()];
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.filefilter_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final File file, final int position) {
        LinearLayout layoutRoot = holder.obtainView(R.id.layout_item_root);
        ImageView ivType = holder.obtainView(R.id.iv_type);
        TextView tvName = holder.obtainView(R.id.tv_name);
        TextView tvDetail = holder.obtainView(R.id.tv_detail);
        final CheckBox cbChoose = holder.obtainView(R.id.cb_choose);
        if (file.isFile()) {
            updateFileIconStyle(file.getAbsolutePath(), ivType);
            tvName.setText(file.getName());
            tvDetail.setText("文件大小：" + " " + FileUtils.getReadableFileSize(file.length()));
            cbChoose.setVisibility(View.VISIBLE);
        } else {
            ivType.setImageResource(R.drawable.folder_style_yellow);
            tvName.setText(file.getName());
            File[] files = file.listFiles(mFileFilter);
            if (files == null) {
                tvDetail.setText("0 " + "项");
            } else {
                tvDetail.setText(files.length + " " + "项");
            }
            cbChoose.setVisibility(View.GONE);
        }
        if (!mMutilyMode) {
            cbChoose.setVisibility(View.GONE);
        }
        layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file.isFile()) {
                    cbChoose.setChecked(!cbChoose.isChecked());
                }
                onItemClickListener.click(position);
            }
        });
        cbChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同步复选框和外部布局点击的处理
                onItemClickListener.click(position);
            }
        });
        cbChoose.setOnCheckedChangeListener(null);//先设置一次CheckBox的选中监听器，传入参数null
        cbChoose.setChecked(mCheckedFlags[position]);//用数组中的值设置CheckBox的选中状态
        //再设置一次CheckBox的选中监听器，当CheckBox的选中状态发生改变时，把改变后的状态储存在数组中
        cbChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCheckedFlags[position] = b;
            }
        });
    }

    private void updateFileIconStyle(String filePath, ImageView imageView) {
        Common.setFileType(filePath, imageView);
    }

    /**
     * 设置监听器
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置数据源
     *
     * @param mListData
     */
    public void setmListData(List<File> mListData) {
        this.mDatas = mListData;
        mCheckedFlags = new boolean[mListData.size()];
    }
}


