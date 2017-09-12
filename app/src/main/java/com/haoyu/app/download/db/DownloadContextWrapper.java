package com.haoyu.app.download.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.SDCardUtil;

import java.io.File;
import java.io.IOException;

/**
 * 创建日期：2017/8/8 on 15:44
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DownloadContextWrapper extends ContextWrapper {
    public DownloadContextWrapper(Context base) {
        super(base);
    }

    /**
     * 获得数据库路径，如果不存在，则创建对象对象
     *
     * @param name
     */
    @Override
    public File getDatabasePath(String name) {
        // 判断是否存在sd卡
        if (!SDCardUtil.isSDCardEnable()) {// 如果不存在,
            return null;
        } else {// 如果存在
            // 获取sd卡路径
            String dbDir = Constants.dbPath;
            String dbPath = dbDir + "/" + name;// 数据库路径
            // 判断目录是否存在，不存在则创建该目录
            File dirFile = new File(dbDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            // 数据库文件是否创建成功
            boolean isFileCreateSuccess = false;
            // 判断文件是否存在，不存在则创建该文件
            File dbFile = new File(dbPath);
            if (!dbFile.exists()) {
                try {
                    isFileCreateSuccess = dbFile.createNewFile();// 创建文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                isFileCreateSuccess = true;
            }
            // 返回数据库文件对象
            if (isFileCreateSuccess) {
                return dbFile;
            } else {
                return null;
            }
        }
    }

    /**
     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
     *
     * @param name
     * @param mode
     * @param factory
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(
                getDatabasePath(name), null);
        return result;
    }

    /**
     * Android 4.0会调用此方法获取数据库。
     *
     * @param name
     * @param mode
     * @param factory
     * @param errorHandler
     * @see android.content.ContextWrapper#openOrCreateDatabase(java.lang.String,
     * int, android.database.sqlite.SQLiteDatabase.CursorFactory,
     * android.database.DatabaseErrorHandler)
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(
                getDatabasePath(name), null);
        return result;
    }
}
