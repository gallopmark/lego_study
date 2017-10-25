package com.haoyu.app.pickerlib;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/6/16 on 14:24
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MediaGridAdapter extends BaseArrayRecyclerAdapter<MediaItem> {
    private int TYPE_CAMERA = 1;
    private int TYPE_ITEM = 2;
    private int selectType;
    private boolean showCamera;
    private Context context;
    private int mImageSize;               //每个条目的大小
    private int limit;
    private List<MediaItem> mSelects = new ArrayList<>();
    private boolean isMultiMode;
    private int selected = -1;
    private OnItemClickListener onItemClickListener;

    public MediaGridAdapter(Context context, List<MediaItem> mDatas, boolean showCamera) {
        super(mDatas);
        this.context = context;
        this.showCamera = showCamera;
        mImageSize = getImageItemWidth(context);
        isMultiMode = MediaPicker.getInstance().getMediaOption().isMultiMode();
        selectType = MediaPicker.getInstance().getMediaOption().getSelectType();
        limit = MediaPicker.getInstance().getMediaOption().getSelectLimit();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列，并获取Item宽度
     */
    private int getImageItemWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (4 * context.getResources().getDisplayMetrics().density);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == TYPE_CAMERA)
            return R.layout.pickerlib_camera_item;
        else
            return R.layout.pickerlib_media_item;
    }

    @Override
    public int getItemCount() {
        if (showCamera)
            return mDatas.size() + 1;
        return mDatas.size();
    }

    @Override
    public void onBindHoder(final RecyclerHolder holder, final MediaItem imageItem, final int position) {
        holder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(mImageSize, mImageSize));
        if (holder.getItemViewType() == TYPE_CAMERA) {
            if (selectType == MediaOption.TYPE_VIDEO)
                holder.setText(R.id.tv_take, "拍摄视频");
            else
                holder.setText(R.id.tv_take, "拍摄照片");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onCamera();
                    }
                }
            });
        } else {
            ImageView picture = holder.obtainView(R.id.picture);
            final CheckBox checkBox = holder.obtainView(R.id.checkBox);
            TextView tv_duration = holder.obtainView(R.id.tv_duration);
            final FrameLayout fl_selected = holder.obtainView(R.id.fl_selected);
            final int index = showCamera ? position - 1 : position;
            final MediaItem item = mDatas.get(index);
            String path = item.getPath();
            Glide.with(context)
                    .load(path)
                    .placeholder(R.drawable.ic_placeholder)
                    .crossFade()
                    .centerCrop()
                    .override(mImageSize, mImageSize)
                    .into(picture);
            if (isMultiMode)
                checkBox.setVisibility(View.VISIBLE);
            else
                checkBox.setVisibility(View.GONE);
            if (selectType == MediaOption.TYPE_VIDEO) {
                tv_duration.setVisibility(View.VISIBLE);
                tv_duration.setText(item.getDuration());
            } else {
                tv_duration.setVisibility(View.GONE);
            }
            if (selected == index)
                fl_selected.setVisibility(View.VISIBLE);
            else
                fl_selected.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isMultiMode) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                            mSelects.remove(item);
                        } else {
                            if (mSelects.contains(item))
                                mSelects.remove(item);
                            if (mSelects.size() < limit) {
                                mSelects.add(item);
                                checkBox.setChecked(true);
                            } else {
                                if (onItemClickListener != null) {
                                    onItemClickListener.onOverChoice(limit);
                                }
                            }
                        }
                        if (onItemClickListener != null) {
                            onItemClickListener.onMultipleChoice(mSelects);
                        }
                    } else {
                        setSelected(index);
                        if (onItemClickListener != null) {
                            onItemClickListener.onSingleChoice(item);
                        }
                    }
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelects.contains(item)) {
                        checkBox.setChecked(false);
                        mSelects.remove(item);
                    } else {
                        if (mSelects.size() < limit) {
                            mSelects.add(item);
                            checkBox.setChecked(true);
                        } else {
                            checkBox.setChecked(false);
                            if (onItemClickListener != null) {
                                onItemClickListener.onOverChoice(limit);
                            }
                        }
                    }
                    if (onItemClickListener != null) {
                        onItemClickListener.onMultipleChoice(mSelects);
                    }
                }
            });
            checkBox.setChecked(mSelects.contains(item));
        }
    }

    public void setSelected(int selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onCamera();

        void onSingleChoice(MediaItem item);

        void onOverChoice(int limit);

        void onMultipleChoice(List<MediaItem> mSelects);
    }
}
