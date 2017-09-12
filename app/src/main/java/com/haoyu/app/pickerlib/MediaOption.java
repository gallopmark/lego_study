package com.haoyu.app.pickerlib;

import java.io.Serializable;

/**
 * 创建日期：2017/6/19 on 9:01
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MediaOption implements Serializable {
    public static final int REQUEST_CODE_TAKE = 1001;
    public static final int REQUEST_CODE_CROP = 1002;
    public static final int REQUEST_CODE_PREVIEW = 1003;
    public static final int RESULT_CODE_ITEMS = 1004;
    public static final int RESULT_CODE_BACK = 1005;
    public static final int RESULT_CODE_PATH = 1006;

    public static final String EXTRA_RESULT_ITEMS = "extra_result_items";
    public static final String EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position";
    public static final String EXTRA_IMAGE_ITEMS = "extra_image_items";
    public static final String EXTRA_IMAGE_CROP = "extra_image_path";

    public static final int TYPE_IMAGE = 1;     //选择图片
    public static final int TYPE_VIDEO = 2;     //选择视频
    private int selectType = TYPE_IMAGE;   //默认为选择图片
    private boolean multiMode = false;    //图片选择模式
    private int selectLimit = 9;         //最大选择图片数量
    private boolean crop = false;         //裁剪
    private boolean showCamera = true;   //显示相机
    private boolean isSaveRectangle = true;  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private int outPutX = 800;           //裁剪保存宽度
    private int outPutY = 800;           //裁剪保存高度
    private int focusWidth = 280;         //焦点框的宽度
    private int focusHeight = 280;        //焦点框的高度
    private CropImageView.Style style = CropImageView.Style.RECTANGLE; //裁剪框的形状

    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    public int getSelectType() {
        return selectType;
    }

    public boolean isMultiMode() {
        return multiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public boolean isCrop() {   //如果是多选则不允许剪裁
        if (isMultiMode())
            return false;
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isSaveRectangle() {
        return isSaveRectangle;
    }

    public void setSaveRectangle(boolean saveRectangle) {
        isSaveRectangle = saveRectangle;
    }

    public int getOutPutX() {
        return outPutX;
    }

    public void setOutPutX(int outPutX) {
        this.outPutX = outPutX;
    }

    public int getOutPutY() {
        return outPutY;
    }

    public void setOutPutY(int outPutY) {
        this.outPutY = outPutY;
    }

    public int getFocusWidth() {
        return focusWidth;
    }

    public void setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
    }

    public int getFocusHeight() {
        return focusHeight;
    }

    public void setFocusHeight(int focusHeight) {
        this.focusHeight = focusHeight;
    }

    public CropImageView.Style getStyle() {
        return style;
    }

    public void setStyle(CropImageView.Style style) {
        this.style = style;
    }

    public static class Builder {
        private MediaOption option;

        public Builder() {
            option = new MediaOption();
        }

        public Builder setSelectType(int selectType) {
            option.setSelectType(selectType);
            return this;
        }

        public Builder isMultiMode(boolean multiMode) {
            option.setMultiMode(multiMode);
            return this;
        }

        public Builder setSelectLimit(int selectLimit) {
            option.setSelectLimit(selectLimit);
            return this;
        }

        public Builder setCrop(boolean crop) {
            option.setCrop(crop);
            return this;
        }

        public Builder setShowCamera(boolean showCamera) {
            option.setShowCamera(showCamera);
            return this;
        }

        public Builder setSaveRectangle(boolean saveRectangle) {
            option.setSaveRectangle(saveRectangle);
            return this;
        }

        public Builder setOutPutX(int outPutX) {
            option.setOutPutX(outPutX);
            return this;
        }

        public Builder setOutPutY(int outPutY) {
            option.setOutPutY(outPutY);
            return this;
        }

        public Builder setFocusWidth(int focusWidth) {
            option.setFocusWidth(focusWidth);
            return this;
        }

        public Builder setFocusHeight(int focusHeight) {
            option.setFocusHeight(focusHeight);
            return this;
        }

        public Builder setStyle(CropImageView.Style style) {
            option.setStyle(style);
            return this;
        }

        public MediaOption build() {
            return option;
        }
    }
}
