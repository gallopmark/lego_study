package com.haoyu.app.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.haoyu.app.lego.student.BuildConfig;
import com.haoyu.app.lego.student.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Common {

    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    public static void hideSoftInput(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (context.getCurrentFocus() != null && context.getCurrentFocus().getWindowToken() != null) {
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }

    }

    public static String accuracy(double num, double total, int scale) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        //可以设置精确几位小数
        df.setMaximumFractionDigits(scale);
        //模式 例如四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (total == 0)
            return "0%";
        double accuracy_num = num / total * 100;
        if (accuracy_num > 100)
            accuracy_num = 100;
        return df.format(accuracy_num) + "%";
    }

    /***
     * 转换文件大小单位(b/kb/mb/gb)
     ***/
    public static String FormetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.0");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static String getFileType(String url) {
        String fileType = "";
        if (url == null) {
            return fileType;
        }
        int lastDot = url.lastIndexOf(".");
        if (lastDot < 0)
            return fileType;
        return url.substring(url.lastIndexOf("."), url.length()).toUpperCase();
    }

    public static String getFileName(String url) {
        String fileName = "";
        if (url != null && url.lastIndexOf("/") > 0) {
            fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        }
        return fileName;
    }

    /**
     * 格式化浮点数据，保留1位小数
     */
    public static String formatFloat(double value) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(value);
    }

    public static int getScale(float value) {
        if (value >= 1 && value < 10) {
            return 0;
        }

        if (value >= 10) {
            return 1 + getScale(value / 10);
        } else {
            return getScale(value * 10) - 1;
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    public static boolean openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            //判断是否是AndroidN以及更高的版本
            String type = getMIMEType(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                intent.setDataAndType(contentUri, type);
            } else {
                //设置intent的data和Type属性。
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "系统无法打开此文件", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    private static String getMIMEType(File file) {
        String type = "*/*";
        if (file != null && file.exists()) {
            String fName = file.getName();
            //获取后缀名前的分隔符"."在fName中的位置。
            int dotIndex = fName.lastIndexOf(".");
            if (dotIndex < 0) {
                return type;
            }
    /* 获取文件的后缀名 */
            String end = fName.substring(dotIndex + 1, fName.length()).toLowerCase();
            if (end.equals("")) return type;
            //在MIME和文件类型的匹配表中找到对应的MIME类型。
            for (int i = 0; i < Constants.MIME_MapTable.length; i++) {
                if (end.equals(Constants.MIME_MapTable[i][0]))
                    return Constants.MIME_MapTable[i][1];
            }
        }
        return type;
    }

    public static void setFileType(String url, ImageView iv_fileType) {
        if (url != null && url.length() > 0) {
            if (MediaFile.isPdfFileType(url)) {
                iv_fileType.setImageResource(R.drawable.resources_pdf);
            } else if (MediaFile.isImageFileType(url)) {
                iv_fileType.setImageResource(R.drawable.resources_jpg);
            } else if (MediaFile.isOfficeFileType(url)) {
                String type = getFileType(url);
                if (type.equals(".PPT") || type.equals(".PPTX")) {
                    iv_fileType.setImageResource(R.drawable.resources_ppt);
                } else if (type.equals(".DOC") || type.equals(".DOCX")) {
                    iv_fileType.setImageResource(R.drawable.resources_doc);
                } else if (type.equals(".XLS") || type.equals(".XLSX")) {
                    iv_fileType.setImageResource(R.drawable.resources_xls);
                } else {
                    iv_fileType.setImageResource(R.drawable.resources_unknown);
                }
            } else if (MediaFile.isTxtFileType(url)) {
                iv_fileType.setImageResource(R.drawable.resources_txt);
            } else if (getFileType(url).equals(".ZIP")) {
                iv_fileType.setImageResource(R.drawable.resources_zip);
            } else if (getFileType(url).equals(".APK")) {
                iv_fileType.setImageResource(R.drawable.ic_launcher);
            } else if (MediaFile.isVideoFileType(url)) {
                iv_fileType.setImageResource(R.drawable.resources_video);
            } else {
                iv_fileType.setImageResource(R.drawable.resources_unknown);
            }
        } else {
            iv_fileType.setImageResource(R.drawable.resources_unknown);
        }
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        int versionCode = 0;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /*通知是否已经关闭*/
    public static boolean isNotificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String CHECK_OP_NO_THROW = "checkOpNoThrow";
            String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                Class appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (Integer) opPostNotificationValue.get(Integer.class);
                return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*打开设置*/
    public static void openSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*是否注册了权限*/
    public static boolean checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String formatNum(int num) {
        if (num < 10 * 1000) {
            return String.valueOf(num);
        } else {
            double count;
            String nuit;
            if (num > 10 * 1000 && num < 10 * 1000 * 10000) {
                count = (double) num / (10 * 1000);
                nuit = "万";
            } else {
                count = (double) num / (10 * 1000 * 10000);
                nuit = "亿";
            }
            BigDecimal bd = new BigDecimal(count);
            return bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + nuit;
        }
    }
}
