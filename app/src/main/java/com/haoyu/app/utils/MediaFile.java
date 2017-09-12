package com.haoyu.app.utils;

/**
 * 创建日期：2017/2/13 on 16:03
 * 描述:
 * 作者:马飞奔 Administrator
 */

import java.util.HashMap;

/**
 * 判断文件类型
 * MediaScanner helper class.
 */
public class MediaFile {
    // comma separated list of all file extensions supported by the media scanner

    // Audio file types
    private static final int FILE_TYPE_MP3 = 1;
    private static final int FILE_TYPE_M4A = 2;
    private static final int FILE_TYPE_WAV = 3;
    private static final int FILE_TYPE_AMR = 4;
    private static final int FILE_TYPE_AWB = 5;
    private static final int FILE_TYPE_WMA = 6;
    private static final int FILE_TYPE_OGG = 7;
    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_OGG;

    // MIDI file types
    private static final int FILE_TYPE_MID = 8;
    private static final int FILE_TYPE_SMF = 9;
    private static final int FILE_TYPE_IMY = 10;
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;

    // Video file types
    /**
     * 微软视频 ：wmv、asf、asx
     * Real Player ：rm、 rmvb
     * MPEG视频 ：mpg、mpeg、mpe
     * 手机视频 ：3gp
     * Apple视频 ：mov
     * Sony视频 ：mp4、m4v
     * 其他常见视频：avi、dat、mkv、flv、vob
     */
    private static final int FILE_TYPE_MP4 = 11;
    private static final int FILE_TYPE_M4V = 12;
    private static final int FILE_TYPE_3GPP = 13;
    private static final int FILE_TYPE_3GPP2 = 14;
    private static final int FILE_TYPE_WMV = 15;
    private static final int FILE_TYPE_ASF = 16;
    private static final int FILE_TYPE_ASX = 17;
    private static final int FILE_TYPE_RM = 18;
    private static final int FILE_TYPE_RMVB = 19;
    private static final int FILE_TYPE_MPG = 20;
    private static final int FILE_TYPE_MPEG = 21;
    private static final int FILE_TYPE_MPE = 22;
    private static final int FILE_TYPE_AVI = 23;
    private static final int FILE_TYPE_DAT = 24;
    private static final int FILE_TYPE_MKV = 25;
    private static final int FILE_TYPE_FLV = 26;
    private static final int FILE_TYPE_VOB = 27;
    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_VOB;

    // Image file types
    /**
     * JPEG、TIFF、RAW、BMP、GIF、PNG
     */
    private static final int FILE_TYPE_JPEG = 28;
    private static final int FILE_TYPE_GIF = 29;
    private static final int FILE_TYPE_PNG = 30;
    private static final int FILE_TYPE_BMP = 31;
    private static final int FILE_TYPE_TIFF = 32;
    private static final int FILE_TYPE_RAW = 33;
    private static final int FILE_TYPE_WBMP = 34;
    private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE = FILE_TYPE_WBMP;

    // Playlist file types
    private static final int FILE_TYPE_M3U = 35;
    private static final int FILE_TYPE_PLS = 36;
    private static final int FILE_TYPE_WPL = 37;
    private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE = FILE_TYPE_WPL;
    // office file types
    /**
     * doc,docx,xls,xlsx,ppt,pptx
     */
    private static final int FILE_TYPE_DOC = 38;
    private static final int FILE_TYPE_DOCX = 39;
    private static final int FILE_TYPE_XLS = 40;
    private static final int FILE_TYPE_XLSX = 41;
    private static final int FILE_TYPE_PPT = 42;
    private static final int FILE_TYPE_PPTX = 42;
    private static final int FILE_TYPE_PPS = 44;
    private static final int FIRST_OFFICE_FILE_TYPE = FILE_TYPE_DOC;
    private static final int LAST_OFFICE_FILE_TYPE = FILE_TYPE_PPS;

    private static final int FILE_TYPE_PDF = 45;

    private static final int FILE_TYPE_TXT = 46;

    private static HashMap<String, Integer> sFileTypeMap = new HashMap<>();

    static void addFileType(String extension, int fileType) {
        sFileTypeMap.put(extension, new Integer(fileType));
    }

    static {
        addFileType("MP3", FILE_TYPE_MP3);
        addFileType("M4A", FILE_TYPE_M4A);
        addFileType("WAV", FILE_TYPE_WAV);
        addFileType("AMR", FILE_TYPE_AMR);
        addFileType("AWB", FILE_TYPE_AWB);
        addFileType("WMA", FILE_TYPE_WMA);
        addFileType("OGG", FILE_TYPE_OGG);

        addFileType("MID", FILE_TYPE_MID);
        addFileType("XMF", FILE_TYPE_MID);
        addFileType("RTTTL", FILE_TYPE_MID);
        addFileType("SMF", FILE_TYPE_SMF);
        addFileType("IMY", FILE_TYPE_IMY);

        addFileType("MP4", FILE_TYPE_MP4);
        addFileType("M4V", FILE_TYPE_M4V);
        addFileType("3GP", FILE_TYPE_3GPP);
        addFileType("3GPP", FILE_TYPE_3GPP);
        addFileType("3G2", FILE_TYPE_3GPP2);
        addFileType("3GPP2", FILE_TYPE_3GPP2);
        addFileType("WMV", FILE_TYPE_WMV);
        addFileType("ASF", FILE_TYPE_ASF);
        addFileType("ASX", FILE_TYPE_ASX);
        addFileType("RM", FILE_TYPE_RM);
        addFileType("RMVB", FILE_TYPE_RMVB);
        addFileType("MPG", FILE_TYPE_MPG);
        addFileType("MPEG", FILE_TYPE_MPEG);
        addFileType("MPE", FILE_TYPE_MPE);
        addFileType("AVI", FILE_TYPE_AVI);
        addFileType("DAT", FILE_TYPE_DAT);
        addFileType("MKV", FILE_TYPE_MKV);
        addFileType("FLV", FILE_TYPE_FLV);
        addFileType("VOB", FILE_TYPE_VOB);

        addFileType("JPG", FILE_TYPE_JPEG);
        addFileType("JPEG", FILE_TYPE_JPEG);
        addFileType("GIF", FILE_TYPE_GIF);
        addFileType("PNG", FILE_TYPE_PNG);
        addFileType("BMP", FILE_TYPE_BMP);
        addFileType("TIFF", FILE_TYPE_TIFF);
        addFileType("RAW", FILE_TYPE_RAW);
        addFileType("WBMP", FILE_TYPE_WBMP);

        addFileType("M3U", FILE_TYPE_M3U);
        addFileType("PLS", FILE_TYPE_PLS);
        addFileType("WPL", FILE_TYPE_WPL);

        addFileType("DOC", FILE_TYPE_DOC);
        addFileType("DOCX", FILE_TYPE_DOCX);
        addFileType("XLS", FILE_TYPE_XLS);
        addFileType("XLSX", FILE_TYPE_XLSX);
        addFileType("PPT", FILE_TYPE_PPT);
        addFileType("PPTX", FILE_TYPE_PPTX);
        addFileType("PPS", FILE_TYPE_PPS);

        addFileType("PDF", FILE_TYPE_PDF);

        addFileType("TXT", FILE_TYPE_TXT);
    }

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE) ||
                (fileType >= FIRST_MIDI_FILE_TYPE &&
                        fileType <= LAST_MIDI_FILE_TYPE));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE &&
                fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isImageFileType(String path) {
        int type = getFileType(path);
        if (type != -1) {
            return isImageFileType(type);
        }
        return false;
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE &&
                fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static int getFileType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0)
            return -1;
        if (sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase()) != null)
            return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
        else
            return -1;
    }

    //根据视频文件路径判断文件类型
    public static boolean isVideoFileType(String path) {  //自己增加
        int type = getFileType(path);
        if (type != -1) {
            return isVideoFileType(type);
        }
        return false;
    }

    //根据音频文件路径判断文件类型
    public static boolean isAudioFileType(String path) {  //自己增加
        int type = getFileType(path);
        if (type != -1) {
            return isAudioFileType(type);
        }
        return false;
    }

    //根据文档路径判断文件类型
    public static boolean isOfficeFileType(String path) {
        int type = getFileType(path);
        if (type != -1) {
            return isOfficeFileType(type);
        }
        return false;
    }

    public static boolean isOfficeFileType(int fileType) {
        return (fileType >= FIRST_OFFICE_FILE_TYPE &&
                fileType <= LAST_OFFICE_FILE_TYPE);
    }

    public static boolean isPdfFileType(String path) {
        int type = getFileType(path);
        if (type != -1) {
            return isPdfFileType(type);
        }
        return false;
    }

    public static boolean isPdfFileType(int fileType) {
        return fileType == FILE_TYPE_PDF;
    }

    public static boolean isTxtFileType(String filePath) {
        int type = getFileType(filePath);
        if (type != -1) {
            return isTxtFileType(type);
        }
        return false;
    }

    public static boolean isTxtFileType(int fileType) {
        return fileType == FILE_TYPE_TXT;
    }
}
