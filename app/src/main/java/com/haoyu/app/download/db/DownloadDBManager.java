package com.haoyu.app.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * 创建日期：2017/8/8 on 15:55
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DownloadDBManager {
    private SQLiteDatabase db;
    private DownloadDBHelper helper;
    private static String DBName = "filedownload.db";
    private static String TABLE_NAME = "file_download";

    public DownloadDBManager(Context context) {
        DownloadContextWrapper dbContext = new DownloadContextWrapper(context);
        helper = new DownloadDBHelper(dbContext, DBName, TABLE_NAME);
    }

    public void save(DownloadFileInfo fileInfo) {
        try {
            db = helper.getWritableDatabase();
            String sql = "select url from " + TABLE_NAME + " where url='" + fileInfo.getUrl() + "'";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToNext()) {
                db.execSQL("insert into " + TABLE_NAME + " ("
                                + DownloadDBHelper.URL + ","
                                + DownloadDBHelper.FILE_PATH + ","
                                + DownloadDBHelper.FILE_NAME + ","
                                + DownloadDBHelper.TOTAL_BYTES + ","
                                + DownloadDBHelper.SO_FAR_BYTES + ""
                                + ") values(?,?,?,?,?)",
                        new Object[]{fileInfo.getUrl(), fileInfo.getFilePath(), fileInfo.getFileName(), fileInfo.getTotalBytes(), fileInfo.getSoFarBytes()});
            }
            cursor.close();
            close();
        } catch (Exception e) {
            close();
        }
    }

    public String search(String url) {
        if (url == null)
            return null;
        try {
            db = helper.getWritableDatabase();
            String sql = "select " + DownloadDBHelper.FILE_PATH + " from " + TABLE_NAME + " where url='" + url + "'";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                String savePath = cursor.getString(cursor.getColumnIndex(DownloadDBHelper.FILE_PATH));
                if (savePath != null && new File(savePath).exists()) {
                    return savePath;
                } else {
                    delete(url);
                }
            }
            cursor.close();
            close();
        } catch (Exception e) {
            close();
        }
        return null;
    }

    public void delete(String url) {
        if (url == null)
            return;
        try {
            db = helper.getWritableDatabase();
            String sql = "delete from " + TABLE_NAME + " where " + DownloadDBHelper.URL + "=" + url;
            db.execSQL(sql);
            close();
        } catch (Exception e) {
            close();
        }
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
