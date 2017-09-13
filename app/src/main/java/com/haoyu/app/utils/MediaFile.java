package com.haoyu.app.utils;

/**
 * 创建日期：2017/2/13 on 16:03
 * 描述:判断文件类型
 * 作者:马飞奔 Administrator
 */
public class MediaFile {
    public static boolean isImageFileType(String path) {
        String[] fileTypes = new String[]{"bmp", "jpg", "png", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "WMF"};
        return contains(path, fileTypes);
    }

    //根据视频文件路径判断文件类型
    public static boolean isVideoFileType(String path) {
        String[] fileTypes = new String[]{"mp4", "m4v", "wmv", "wma", "mid", "asf", "asx", "rm", "rmvb", "mpg", "mpeg", "mpe", "3gp", "mov", "avi", "dat", "mkv", "flv", "flv"};
        return contains(path, fileTypes);
    }

    //根据文档路径判断文件类型
    public static boolean isOfficeFileType(String path) {
        /**
         * doc,docx,xls,xlsx,ppt,pptx
         */
        String[] fileTypes = new String[]{"doc", "docx", "xls", "xlsx", "ppt", "pptx"};
        return contains(path, fileTypes);
    }

    public static boolean isPdfFileType(String path) {
        String[] fileTypes = new String[]{"pdf"};
        return contains(path, fileTypes);
    }

    public static boolean isTxtFileType(String path) {
        String[] fileTypes = new String[]{"txt"};
        return contains(path, fileTypes);
    }

    private static boolean contains(String url, String[] fileTypes) {
        int lastDot = url.lastIndexOf(".");
        if (lastDot < 0)
            return false;
        String fileType = url.substring(lastDot + 1).toLowerCase();
        for (String type : fileTypes) {
            if (type.equals(fileType))
                return true;
        }
        return false;
    }
}
