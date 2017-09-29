package com.haoyu.app.utils;

/**
 * 创建日期：2016/12/21 on 8:50
 * 描述:
 * 作者:马飞奔 Administrator
 */

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间转换
 *
 * @author vendor
 */
public class TimeUtil {

    private static final String TAG = "TimeUtil";

    /**
     * 时间格式：yyyy-MM-dd
     */
    public static final String DATE_FORMAT = "yyyy年MM月dd日";

    public static final String DATE_SLASH = "yyyy/MM/dd";

    public static final String DATE_Y_M = "yyyy年MM月";

    public static final String DATE_H_R = "yyyy-MM-dd";
    /**
     * 时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间格式：yyyy-MM-dd HH:mm
     */
    public static final String TIME_FORMAT2 = "yyyy-MM-dd HH:mm";

    /**
     * 在之前
     */
    public static final int TIME_BEFORE = 1;

    /**
     * 在中间
     */
    public static final int TIME_ING = 2;

    /**
     * 在之后
     */
    public static final int TIME_AFTER = 3;

    /**
     * 异常
     */
    public static final int TIME_ERROR = -1;

    /**
     * string型时间转换
     *
     * @param timeFormat 时间格式
     * @param timestamp  时间
     * @return 刚刚  x分钟  小时前  ...
     */
    public static String convertTime(String timeFormat, String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        try {
            return converTime(sdf.parse(timestamp).getTime());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return timestamp;
    }

    /**
     * string型时间转换
     *
     * @param timeFormat 时间格式
     * @param timestamp  时间
     * @return 刚刚  x分钟  小时前  ...
     */
    public static String convertTime(String timeFormat, long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        try {
            Date date = new Date();
            date.setTime(timestamp);
            return sdf.format(date);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    public static String getSlashDate(long time) {
        String timeStr = null;
        try {
            Date date = new Date(time);
            timeStr = new SimpleDateFormat(DATE_SLASH, Locale.getDefault()).format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeStr;
    }

    /**
     * int型时间转换
     *
     * @param timestamp 时间
     * @return 刚刚  x分钟  一天内  ...
     */
    public static String converTime(long timestamp) {
        String timeStr;

        long interval = (System.currentTimeMillis() - timestamp) / 1000;
        if (interval <= 60) { //1分钟内 服务端的时间 可能和本地的有区别 所以小于0的 对于这个情况全部都显示刚刚
            timeStr = "刚刚";
        } else if (interval < 60 * 60) { // 1小时内
            timeStr = (interval / 60 == 0 ? 1 : interval / 60) + "分钟前";
        } else if (interval < 24 * 60 * 60) { // 一天内
            timeStr = (interval / 60 * 60 == 0 ? 1 : interval / (60 * 60)) + "小时前";
        } else if (interval < 30 * 24 * 60 * 60) { // 天前
            timeStr = (interval / 24 * 60 * 60 == 0 ? 1 : interval / (24 * 60 * 60)) + "天前";
        } else if (interval < 12 * 30 * 24 * 60 * 60) { // 月前
            timeStr = (interval / 30 * 24 * 60 * 60 == 0 ? 1 : interval / (30 * 24 * 60 * 60)) + "个月前";
        } else if (interval < 12 * 30 * 24 * 60 * 60) { // 年前
            timeStr = (interval / 12 * 30 * 24 * 60 * 60 == 0 ? 1 : interval / (12 * 30 * 24 * 60 * 60)) + "年前";
        } else {
            Date date = new Date();
            date.setTime(timestamp);
            timeStr = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
        }
        return timeStr;
    }

    /**
     * int型时间转换 比较距离结束
     *
     * @param timestamp 时间
     * @return 刚刚  x分钟  一天后  ...
     */
    public static String convertEndTime(long timestamp) {
        String timeStr;

        long interval = (timestamp - System.currentTimeMillis()) / 1000;
        if (interval <= 60) { //1分钟内 服务端的时间 可能和本地的有区别 所以小于0的 对于这个情况全部都显示刚刚
            timeStr = "1分钟";
        } else if (interval < 60 * 60) { // 1小时内
            timeStr = (interval / 60 == 0 ? 1 : interval / 60) + "分钟";
        } else if (interval < 24 * 60 * 60) { // 一天内
            timeStr = (interval / 60 * 60 == 0 ? 1 : interval / (60 * 60)) + "小时";
        } else if (interval < 30 * 24 * 60 * 60) { // 天前
            timeStr = (interval / 24 * 60 * 60 == 0 ? 1 : interval / (24 * 60 * 60)) + "天";
        } else if (interval < 12 * 30 * 24 * 60 * 60) { // 月前
            timeStr = (interval / 30 * 24 * 60 * 60 == 0 ? 1 : interval / (30 * 24 * 60 * 60)) + "个月";
        } else if (interval < 12 * 30 * 24 * 60 * 60) { // 年前
            timeStr = (interval / 12 * 30 * 24 * 60 * 60 == 0 ? 1 : interval / (12 * 30 * 24 * 60 * 60)) + "年";
        } else {
            timeStr = getDateFormat(timestamp);
        }
        return timeStr;
    }

    public static String getDateFormat(long timestamp) {
        String timeStr;
        try {
            Date date = new Date();
            date.setTime(timestamp);
            timeStr = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
        } catch (Exception e) {
            e.printStackTrace();
            timeStr = "时间格式未知";
        }
        return timeStr;
    }

    public static String getDateYM(long timestamp) {
        String timeStr;
        try {
            Date date = new Date();
            date.setTime(timestamp);
            timeStr = new SimpleDateFormat(DATE_Y_M, Locale.getDefault()).format(date);
        } catch (Exception e) {
            e.printStackTrace();
            timeStr = "时间格式未知";
        }
        return timeStr;
    }

    /**
     * 将long型时间转为固定格式的时间字符串
     *
     * @param longTime 时间
     * @return {@link TimeUtil#TIME_FORMAT}
     */
    public static String convertToTime(long longTime) {
        return convertToTime(TIME_FORMAT, longTime);
    }

    /**
     * 将long型时间转为固定格式的时间字符串
     *
     * @param timeformat 时间格式
     * @param longTime   时间
     * @return timeformat
     */
    public static String convertToTime(String timeformat, long longTime) {
        Date date = new Date(longTime);
        return convertToTime(timeformat, date);
    }

    /**
     * 将Date型时间转为固定格式的时间字符串
     *
     * @param timeformat 时间格式
     * @param date       时间
     * @return timeformat
     */
    public static String convertToTime(String timeformat, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeformat, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 将Calendar型时间转为固定格式的时间字符串
     *
     * @param timeformat 时间格式
     * @param calendar   时间
     * @return timeformat
     */
    public static String convertToTime(String timeformat, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeformat, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * 将long型时间转为固定格式的日期字符串
     *
     * @param longTime 时间
     * @return {@link TimeUtil#DATE_FORMAT}
     */
    public static String convertToDate(long longTime) {
        return convertToTime(DATE_FORMAT, longTime);
    }

    /**
     * 将String类型时间转为long类型时间
     *
     * @param timeFormat 解析格式
     * @param timestamp  yyyy-MM-dd HH:mm:ss
     * @return 时间
     */
    public static long covertToLong(String timeFormat, String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        try {
            Date date = sdf.parse(timestamp);
            return date.getTime();
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            return -1;
        }
    }

    /**
     * long型时间转换
     *
     * @param longTime 长整型时间
     * @return 2013年7月3日 18:05(星期三)
     */
    public static String convertDayOfWeek(long longTime) {
        final String format = "%d年%d月%d日 %s:%s(%s)";

        Calendar c = Calendar.getInstance(); // 日历实例
        c.setTime(new Date(longTime));

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String h = hour > 9 ? String.valueOf(hour) : "0" + hour;
        int minute = c.get(Calendar.MINUTE);
        String m = minute > 9 ? String.valueOf(minute) : "0" + minute;
        return String.format(Locale.getDefault(), format, year, month + 1, date, h, m, converToWeek(c.get(Calendar.DAY_OF_WEEK)));
    }

    /**
     * 转换数字的星期为字符串的
     *
     * @param w
     * @return 星期x
     */
    private static String converToWeek(int w) {
        String week = null;

        switch (w) {
            case 1:
                week = "星期日";
                break;
            case 2:
                week = "星期一";
                break;
            case 3:
                week = "星期二";
                break;
            case 4:
                week = "星期三";
                break;
            case 5:
                week = "星期四";
                break;
            case 6:
                week = "星期五";
                break;
            case 7:
                week = "星期六";
                break;
        }

        return week;
    }

    /**
     * 计算时间是否在区间内
     *
     * @param time  time
     * @param time1 time
     * @param time2 time
     * @return {@link TimeUtil#TIME_BEFORE}{@link TimeUtil#TIME_ING}{@link TimeUtil#TIME_AFTER}
     */
    public static int betweenTime(long time, long time1, long time2) {
        if (time1 > time2) {  //时间1大
            long testTime = time1;
            time1 = time2;
            time2 = testTime;
        }

        //已经过去
        if (time1 > time) {
            return TIME_BEFORE;
        } else if (time2 < time) {
            return TIME_AFTER;
        } else {
            return TIME_ING;
        }
    }

    public static String computeTimeDifference(String time) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = df.parse(time);
            return computeTimeDifference(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String computeTimeDifference(long time) {
//        long l = time - System.currentTimeMillis();
//        if (l <= 0) {
//            return "" + 0 + "天" + 0 + "小时" + 0 + "分" + 0 + "秒";
//        }
//
//        long day = l / (24 * 60 * 60 * 1000);
//        long hour = (l / (60 * 60 * 1000) - day * 24);
//        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
//        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
//        return "" + day + "天" + hour + "小时" + min + "分" + s + "秒";
//    }

    public static String computeTimeDifference(long time) {
        long l = time - System.currentTimeMillis();
        if (l <= 0) {
            return "" + 0 + "天" + 0 + "时" + 0 + "分";
        }

        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
//        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return "" + day + "天" + hour + "时" + min + "分";
    }

    public static String timeFormat(long time) {
        long l = time - System.currentTimeMillis();
        if (l <= 0) {
            return "" + 0 + "天" + 0 + "小时";
        }

        long diffHour = l / (1000 * 60 * 60);
        long day = l / (1000 * 60 * 60 * 24);
        if (diffHour < 24) {
            // 显示为小时
            return "" + diffHour + "小时";
        } else {
            // 显示天
            return "" + day + "天";
        }
    }

    public static String getDate(long time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(time);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTime(long time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = new Date(time);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 得到几天后的时间
     *
     * @param d
     * @param day
     * @return
     */
    public static long getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTimeInMillis();
    }

    public static String getDateHR(long timestamp) {
        String timeStr;
        try {
            Date date = new Date();
            date.setTime(timestamp);
            timeStr = new SimpleDateFormat(DATE_H_R, Locale.getDefault()).format(date);
        } catch (Exception e) {
            e.printStackTrace();
            timeStr = "时间格式未知";
        }
        return timeStr;
    }

    public static int getSurplusDay(long createTime) {
        createTime += 1000 * 60 * 60 * 24 * 60;
        long l = createTime - System.currentTimeMillis();
        if (l <= 0) {
            return 0;
        }
        int day = (int) (l / (1000 * 60 * 60 * 24));
        return day;
    }

    public static int getCCTotalDay(long createTime) {
        createTime += getDateAfter(new Date(), 60);
        int day = (int) (createTime / (1000 * 60 * 60 * 24));
        return day;
    }

    public static int differentDays(Date date1, Date date2) {
        Date date = new Date();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) //同一年
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) //闰年
                {
                    timeDistance += 366;
                } else //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else //不同年
        {
            return day2 - day1;
        }
    }

    public static String convertDayOfMinute(long startTime, long endTime) {
        final String format1 = "%d年%d月%d日 %s:%s";
        final String format2 = "%d月%d日 %s:%s";
        Calendar c1 = Calendar.getInstance(); // 日历实例
        c1.setTime(new Date(startTime));
        Calendar c2 = Calendar.getInstance(); // 日历实例
        c2.setTime(new Date(endTime));
        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int date1 = c1.get(Calendar.DATE);
        int hour1 = c1.get(Calendar.HOUR_OF_DAY);
        String h1 = hour1 > 9 ? String.valueOf(hour1) : "0" + hour1;
        int minute1 = c1.get(Calendar.MINUTE);
        String m1 = minute1 > 9 ? String.valueOf(minute1) : "0" + minute1;
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH);
        int date2 = c2.get(Calendar.DATE);
        int hour2 = c1.get(Calendar.HOUR_OF_DAY);
        String h2 = hour1 > 9 ? String.valueOf(hour2) : "0" + hour2;
        int minute2 = c1.get(Calendar.MINUTE);
        String m2 = minute1 > 9 ? String.valueOf(minute2) : "0" + minute2;
        if (year1 == year2) {
            return String.format(Locale.getDefault(), format1, year1, month1 + 1, date1, h1, m1) + "至"
                    + String.format(Locale.getDefault(), format2, month2 + 1, date2, h2, m2);
        }
        return String.format(Locale.getDefault(), format1, year1, month1 + 1, date1, h1, m1) + "至"
                + String.format(Locale.getDefault(), format1, year2, month2 + 1, date2, h2, m2);
    }

    public static String convertTimeOfDay(long startTime, long endTime) {
        final String format1 = "%d/%d/%d";
        final String format2 = "%d/%d %s:%s";
        Calendar c1 = Calendar.getInstance(); // 日历实例
        c1.setTime(new Date(startTime));
        Calendar c2 = Calendar.getInstance(); // 日历实例
        c2.setTime(new Date(endTime));
        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int date1 = c1.get(Calendar.DATE);
        int hour1 = c1.get(Calendar.HOUR_OF_DAY);
        String h1 = hour1 > 9 ? String.valueOf(hour1) : "0" + hour1;
        int minute1 = c1.get(Calendar.MINUTE);
        String m1 = minute1 > 9 ? String.valueOf(minute1) : "0" + minute1;
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH);
        int date2 = c2.get(Calendar.DATE);
        int hour2 = c1.get(Calendar.HOUR_OF_DAY);
        String h2 = hour1 > 9 ? String.valueOf(hour2) : "0" + hour2;
        int minute2 = c1.get(Calendar.MINUTE);
        String m2 = minute1 > 9 ? String.valueOf(minute2) : "0" + minute2;
        if (year1 == year2) {
            return String.format(Locale.getDefault(), format1, year1, month1 + 1, date1, h1, m1) + "-"
                    + String.format(Locale.getDefault(), format2, month2 + 1, date2, h2, m2);
        }
        return String.format(Locale.getDefault(), format1, year1, month1 + 1, date1, h1, m1) + "至"
                + String.format(Locale.getDefault(), format1, year2, month2 + 1, date2, h2, m2);
    }

    public static String convertDayOfMinute(long time) {
        final String format = "%d年%d月%d日 %s:%s";
        Calendar c = Calendar.getInstance(); // 日历实例
        c.setTime(new Date(time));
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String h = hour > 9 ? String.valueOf(hour) : "0" + hour;
        int minute = c.get(Calendar.MINUTE);
        String m1 = minute > 9 ? String.valueOf(minute) : "0" + minute;
        return String.format(Locale.getDefault(), format, year, month + 1, date, h, m1);
    }

    public static int getYear(long longTime) {
        Calendar c = Calendar.getInstance(); // 日历实例
        c.setTime(new Date(longTime));
        return c.get(Calendar.YEAR);
    }

    /*
    * 将时间转换为时间戳
    */
    public static long dateToLong(String stamp, String dateFormat) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            Date date = simpleDateFormat.parse(stamp);
            long ts = date.getTime();
            return ts;
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public static String dateDiff(long startTime, long endTime) {
        StringBuilder actionText = new StringBuilder();
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long diff;
        long day;
        long hour;
        long min;
        // 获得两个时间的毫秒时间差异
        diff = endTime - startTime;
        day = diff / nd;// 计算差多少天
        hour = diff % nd / nh + day * 24;// 计算差多少小时
        min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
        // 输出结果

        System.out.println("hour=" + hour + ",min=" + min);
        actionText.append("<font color='#181818'>"
                + "离活动结束还剩：" + " " + "</font>");
        actionText.append("<font color='#ff9900'>"
                + day + "</font>");
        actionText.append("<font color='#181818'>"
                + "天" + " " + "</font>");
        actionText.append("<font color='#ff9900'>"
                + (hour - day * 24) + "</font>");
        actionText.append("<font color='#181818'>"
                + "时" + " " + "</font>");
        actionText.append("<font color='#ff9900'>"
                + (min - day * 24 * 60) + "</font>");
        actionText.append("<font color='#181818'>"
                + "分" + " " + "</font>");

        return actionText.toString();
    }

    public static String computeTimeDiff(long minutes) {
        String timeStr = "";
        if (minutes <= 0) { //1分钟内 服务端的时间 可能和本地的有区别 所以小于0的 对于这个情况全部都显示刚刚
            timeStr = "1分钟";
        } else if (minutes < 60) { // 1小时内
            timeStr = minutes + "分钟";
        } else if (minutes < 24 * 60) { // 一天内
            timeStr = (minutes / 60 == 0 ? 1 : minutes / 60) + "小时";
        } else if (minutes < 30 * 24 * 60) { // 天前
            long day = minutes / 60 / 24;
            long hour = (minutes - day * 24 * 60) / 60;
            long min = minutes - (day * 24 * 60) - (hour * 60);
            return "" + day + "天" + hour + "时" + min + "分";
        } else if (minutes < 12 * 30 * 24 * 60) { // 月前
            timeStr = (minutes / 30 * 24 * 60 == 0 ? 1 : minutes / (30 * 24 * 60)) + "个月";
        } else if (minutes < 12 * 30 * 24 * 60) { // 年前
            timeStr = (minutes / 12 * 30 * 24 * 60 == 0 ? 1 : minutes / (12 * 30 * 24 * 60)) + "年";
        }
        return timeStr;
    }

    public static String dateDiff(long minutes) {
        String timeStr;
        StringBuilder actionText = new StringBuilder();
        if (minutes <= 0) { //1分钟内 服务端的时间 可能和本地的有区别 所以小于0的 对于这个情况全部都显示刚刚
            timeStr = minutes * 60 + "秒";
        } else if (minutes > 0 && minutes < 60) { // 1小时内
            timeStr = minutes + "分钟";
        } else if (minutes > 60 && minutes < 24 * 60) { // 天前
            timeStr = (minutes / 60 == 0 ? 1 : minutes / 60) + "小时";
        } else if (minutes > 24 * 60 && minutes < 30 * 24 * 60) { // 月前
            long day = minutes / 60 / 24;
            long hour = (minutes - day * 24 * 60) / 60;
            long min = minutes - (day * 24 * 60) - (hour * 60);
            // 输出结果
            actionText.append("<font color='#ff9900'>"
                    + day + "</font>");
            actionText.append("<font color='#181818'>"
                    + "天" + " " + "</font>");
            actionText.append("<font color='#ff9900'>"
                    + hour + "</font>");
            actionText.append("<font color='#181818'>"
                    + "时" + " " + "</font>");
            actionText.append("<font color='#ff9900'>"
                    + min + "</font>");
            actionText.append("<font color='#181818'>"
                    + "分" + " " + "</font>");
            return actionText.toString();
        } else if (minutes > 30 * 24 * 60 && minutes < 12 * 30 * 24 * 60) { // 年前
            timeStr = (minutes / 30 * 24 * 60 == 0 ? 1 : minutes / (30 * 24 * 60)) + "个月";
        } else { // 年前
            timeStr = (minutes / 12 * 30 * 24 * 60 == 0 ? 1 : minutes / (12 * 30 * 24 * 60)) + "年";
        }
        return timeStr;
    }
}