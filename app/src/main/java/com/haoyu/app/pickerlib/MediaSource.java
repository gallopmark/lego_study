package com.haoyu.app.pickerlib;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/6/16 on 10:12
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MediaSource {

    public static List<MediaFolder> getImageFolders(Context context) {
        List<MediaFolder> imageFolders = new ArrayList<>();
        List<MediaItem> imageItems = getImages(context, null);
        if (imageItems.size() > 0) {
            for (int i = 0; i < imageItems.size(); i++) {
                MediaItem imageItem = imageItems.get(i);
                String imagePath = imageItem.getPath();
                //根据父路径分类存放图片
                File imageFile = new File(imagePath);
                File imageParentFile = imageFile.getParentFile();
                MediaFolder imageFolder = new MediaFolder();
                imageFolder.setName(imageParentFile.getName());
                imageFolder.setPath(imageParentFile.getAbsolutePath());
                if (!imageFolders.contains(imageFolder)) {
                    ArrayList<MediaItem> images = new ArrayList<>();
                    images.add(imageItem);
                    imageFolder.setFirstImagePath(imagePath);
                    imageFolder.setMediaItems(images);
                    imageFolders.add(imageFolder);
                } else {
                    imageFolders.get(imageFolders.indexOf(imageFolder)).getMediaItems().add(imageItem);
                }
            }
        }
        return imageFolders;
    }

    public static List<MediaItem> getImages(Context context, String path) {
        String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
                MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
                MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
                MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
                MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608
        ContentResolver mContentResolver = context.getContentResolver();
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor mCursor;
        if (path == null)
            mCursor = mContentResolver.query(mImageUri, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC");
        else
            mCursor = mContentResolver.query(mImageUri, IMAGE_PROJECTION, IMAGE_PROJECTION[1] + " like '%" + path + "%'", null, IMAGE_PROJECTION[6] + " DESC");
        ArrayList<MediaItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
        while (mCursor.moveToNext()) {
            //查询数据
            String imageName = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            String imagePath = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
            long imageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            int imageWidth = mCursor.getInt(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
            int imageHeight = mCursor.getInt(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
            String imageMimeType = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
            long imageAddTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
            //封装实体
            MediaItem imageItem = new MediaItem();
            imageItem.setName(imageName);
            imageItem.setPath(imagePath);
            imageItem.setSize(imageSize);
            imageItem.setWidth(imageWidth);
            imageItem.setHeight(imageHeight);
            imageItem.setMimeType(imageMimeType);
            imageItem.setAddTime(imageAddTime);
            allImages.add(imageItem);
        }
        return allImages;
    }

    /*获取最近照片 100条记录*/
    public static List<MediaItem> getLatelyImages(Context context) {
        String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
                MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
                MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
                MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
                MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608
        ContentResolver mContentResolver = context.getContentResolver();
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor mCursor = mContentResolver.query(mImageUri, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC");
        ArrayList<MediaItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
        int latelyNum = 0;
        while (mCursor.moveToNext() && latelyNum < 100) {
            //查询数据
            String imageName = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            String imagePath = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
            long imageSize = mCursor.getLong(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            int imageWidth = mCursor.getInt(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
            int imageHeight = mCursor.getInt(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
            String imageMimeType = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
            long imageAddTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
            //封装实体
            MediaItem imageItem = new MediaItem();
            imageItem.setName(imageName);
            imageItem.setPath(imagePath);
            imageItem.setSize(imageSize);
            imageItem.setWidth(imageWidth);
            imageItem.setHeight(imageHeight);
            imageItem.setMimeType(imageMimeType);
            imageItem.setAddTime(imageAddTime);
            allImages.add(imageItem);
            latelyNum++;
        }
        return allImages;
    }

    public static List<MediaFolder> getVideoFolders(Context context) {
        List<MediaFolder> imageFolders = new ArrayList<>();
        List<MediaItem> videoItems = getVideos(context, null);
        if (videoItems.size() > 0) {
            for (int i = 0; i < videoItems.size(); i++) {
                MediaItem item = videoItems.get(i);
                String videoPath = item.getPath();
                //根据父路径分类存放图片
                File videoFile = new File(videoPath);
                File videoParentFile = videoFile.getParentFile();
                MediaFolder imageFolder = new MediaFolder();
                imageFolder.setName(videoParentFile.getName());
                imageFolder.setPath(videoParentFile.getAbsolutePath());
                if (!imageFolders.contains(imageFolder)) {
                    ArrayList<MediaItem> images = new ArrayList<>();
                    images.add(item);
                    imageFolder.setFirstImagePath(videoPath);
                    imageFolder.setMediaItems(images);
                    imageFolders.add(imageFolder);
                } else {
                    imageFolders.get(imageFolders.indexOf(imageFolder)).getMediaItems().add(item);
                }
            }
        }
        return imageFolders;
    }

    public static List<MediaItem> getVideos(Context context, String path) {
        String[] VIDEO_PROJECTION = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
        };
        ContentResolver mContentResolver = context.getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor mCursor;
        if (path == null)
            mCursor = mContentResolver.query(videoUri, VIDEO_PROJECTION, null, null, VIDEO_PROJECTION[2] + " DESC");
        else
            mCursor = mContentResolver.query(videoUri, VIDEO_PROJECTION, VIDEO_PROJECTION[0] + " like '%" + path + "%'", null, VIDEO_PROJECTION[2] + " DESC");
        List<MediaItem> videos = new ArrayList<>();
        while (mCursor.moveToNext()) {
            //查询数据
            String videoPath = mCursor.getString(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
            String videoName = mCursor.getString(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
            long imageAddTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
            long duration = mCursor.getLong(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
            //封装实体
            MediaItem item = new MediaItem();
            item.setName(videoName);
            item.setPath(videoPath);
            item.setAddTime(imageAddTime);
            item.setDuration(duration);
            videos.add(item);
        }
        return videos;
    }

    public static List<MediaItem> getLatelyVideos(Context context) {
        String[] VIDEO_PROJECTION = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
        };
        ContentResolver mContentResolver = context.getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor mCursor;
        mCursor = mContentResolver.query(videoUri, VIDEO_PROJECTION, null, null, VIDEO_PROJECTION[2] + " DESC");
        List<MediaItem> videos = new ArrayList<>();
        int latelyNum = 0;
        while (mCursor.moveToNext() && latelyNum < 100) {
            //查询数据
            String videoPath = mCursor.getString(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
            String videoName = mCursor.getString(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
            long imageAddTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
            long duration = mCursor.getLong(mCursor.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
            //封装实体
            MediaItem item = new MediaItem();
            item.setName(videoName);
            item.setPath(videoPath);
            item.setAddTime(imageAddTime);
            item.setDuration(duration);
            videos.add(item);
            latelyNum++;
        }
        return videos;
    }
}
