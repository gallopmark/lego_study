package com.haoyu.app.pickerlib;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.view.AppToolBar;

import java.io.File;

import butterknife.BindView;

public class ImageCropActivity extends BaseActivity implements CropImageView.OnBitmapSaveCompleteListener {
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.cv_crop_image)
    CropImageView mCropImageView;
    private Bitmap mBitmap;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_pickerlib_imagecrop;
    }

    @Override
    public void initView() {
        String imagePath = getIntent().getStringExtra("imagePath");
        mCropImageView.setOnBitmapSaveCompleteListener(this);
        MediaOption option = MediaPicker.getInstance().getMediaOption();
        //获取需要的参数
        mOutputX = option.getOutPutX();
        mOutputY = option.getOutPutY();
        mIsSaveRectangle = option.isSaveRectangle();
        mCropImageView.setFocusStyle(option.getStyle());
        mCropImageView.setFocusWidth(option.getFocusWidth());
        mCropImageView.setFocusHeight(option.getFocusHeight());
        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(imagePath, options);
        mCropImageView.setImageBitmap(mBitmap);
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onRightClick(View view) {
                File cropFile = new File(Constants.mediaCache, "/crop/");
                mCropImageView.saveBitmapToFile(cropFile, mOutputX, mOutputY, mIsSaveRectangle);
            }
        });
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onBitmapSaveSuccess(File file) {
        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        Intent intent = new Intent();
        intent.putExtra(MediaOption.EXTRA_IMAGE_CROP, file.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBitmapSaveError(File file) {
        toastFullScreen("图片修剪失败", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
