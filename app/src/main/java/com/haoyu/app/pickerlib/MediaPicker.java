package com.haoyu.app.pickerlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.haoyu.app.lego.student.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaPicker {

    private static MediaPicker mInstance;
    private MediaOption imageOption;
    private onSelectMediaCallBack selectMediaCallBack;

    private MediaPicker() {
    }

    public static MediaPicker getInstance() {
        if (mInstance == null) {
            synchronized (MediaPicker.class) {
                if (mInstance == null) {
                    mInstance = new MediaPicker();
                }
            }
        }
        return mInstance;
    }

    public MediaPicker init(MediaOption option) {
        imageOption = option;
        return mInstance;
    }

    public MediaOption getMediaOption() {
        if (imageOption == null)
            imageOption = getDefaultOption();
        return imageOption;
    }

    public MediaOption getDefaultOption() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .isMultiMode(false)
                .setCrop(false)
                .setShowCamera(true)
                .build();
        return option;
    }

    public onSelectMediaCallBack getSelectMediaCallBack() {
        return selectMediaCallBack;
    }

    public void setSelectMediaCallBack(onSelectMediaCallBack selectMediaCallBack) {
        this.selectMediaCallBack = selectMediaCallBack;
    }

    public void selectMedia(Activity context, onSelectMediaCallBack onSelectImageListener) {
        this.selectMediaCallBack = onSelectImageListener;
        Intent intent = new Intent(context, MediaGridActivity.class);
        context.startActivityForResult(intent, MediaOption.RESULT_CODE_PATH);
        context.overridePendingTransition(R.anim.slide_bottom_in, 0);
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 扫描图片
     */
    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + file.getAbsolutePath());
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static class onSelectMediaCallBack {
       public void onSelected(String path) {
        }

       public void onSelected(List<MediaItem> mSelects) {
        }
    }

}