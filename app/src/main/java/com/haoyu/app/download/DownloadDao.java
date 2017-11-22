package com.haoyu.app.download;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 创建日期：2017/11/21.
 * 描述:
 * 作者:xiaoma
 */

public class DownloadDao {
    DownloadDBHelper dbHelper;
    private static final String DBNAME = "download.db";
    private static final String TABLE_NAME = "download";

    public DownloadDao(Context context) {
        dbHelper = new DownloadDBHelper(context, DBNAME, TABLE_NAME, 1);
    }

    public void insertOrReplace(DownloadEntity entity) {
        String sql = "insert or replace into " + TABLE_NAME + "(url,savePath,fileName,totalSize,completeSize) values(?,?,?,?,?)";
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            Object[] bindArgs = new Object[]{entity.getUrl(), entity.getSaveDirPath(), entity.getFileName(), entity.getTotalSize(), entity.getCompletedSize()};
            db.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public DownloadEntity load(String url) {
        String sql = "SELECT url,savePath,fileName,totalSize,completeSize FROM " + TABLE_NAME + " WHERE url=?";
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, new String[]{url});
            if (cursor.moveToNext()) {
                DownloadEntity entity = new DownloadEntity();
                entity.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                entity.setSaveDirPath(cursor.getString(cursor.getColumnIndex("savePath")));
                entity.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
                entity.setTotalSize(cursor.getLong(cursor.getColumnIndex("totalSize")));
                entity.setCompletedSize(cursor.getLong(cursor.getColumnIndex("completeSize")));
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    public void delete(String url) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE url=?";
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.execSQL(sql, new Object[]{url});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
