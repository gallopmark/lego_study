package com.haoyu.app.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建日期：2017/11/21.
 * 描述:
 * 作者:xiaoma
 */

public class DownloadDBHelper extends SQLiteOpenHelper {
    private String TABLE_NAME;

    public DownloadDBHelper(Context context, String name, String tableName, int version) {
        super(context, name, null, version);
        TABLE_NAME = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (download_id INTEGER PRIMARY KEY AUTOINCREMENT," + // 0: downloadId
                "totalSize INTEGER," + // 1: toolSize
                "completeSize INTEGER," + // 2: completedSize
                "url TEXT," + // 3: url
                "savePath TEXT," + // 4: saveDirPath
                "fileName TEXT,UNIQUE(url));"); // 6: downloadStatus
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
