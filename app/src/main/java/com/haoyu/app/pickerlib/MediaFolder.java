package com.haoyu.app.pickerlib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaFolder implements Serializable {

    private String name;  //当前文件夹的名字
    private String path;  //当前文件夹的路径
    private MediaItem cover;   //当前文件夹需要要显示的缩略图，默认为最近的一次图片
    private String firstImagePath;  //当前文件夹的第一张图片路径
    private List<MediaItem> mediaItems = new ArrayList<>();  //当前文件夹下所有图片的集合

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MediaItem getCover() {
        return cover;
    }

    public void setCover(MediaItem cover) {
        this.cover = cover;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public List<MediaItem> getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }

    /** 只要文件夹的路径和名字相同，就认为是相同的文件夹 */
    @Override
    public boolean equals(Object o) {
        try {
            MediaFolder other = (MediaFolder) o;
            return this.path.equalsIgnoreCase(other.path) && this.name.equalsIgnoreCase(other.name);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
