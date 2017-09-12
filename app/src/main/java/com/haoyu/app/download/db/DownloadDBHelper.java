package com.haoyu.app.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建日期：2017/8/8 on 15:47
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DownloadDBHelper extends SQLiteOpenHelper {
    private static int DATABASE_VERSION = 1;
    private String TABLE_NAME;

    public static final String _id = "_id";
    public static final String URL = "URL";
    public static final String FILE_PATH = "FILE_PATH";
    public static final String SO_FAR_BYTES = "SO_FAR_BYTES";
    public static final String TOTAL_BYTES = "TOTAL_BYTES";
    public static final String FILE_NAME = "FILE_NAME";

    public DownloadDBHelper(Context context, String DBName, String tableName) {
        super(context, DBName, null, DATABASE_VERSION);
        this.TABLE_NAME = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + _id + " INTEGER PRIMARY KEY ,"// 0: id
                + URL + " TEXT," // 1: url
                + FILE_PATH + " TEXT,"  // 2: filePath
                + SO_FAR_BYTES + " INTEGER ,"  // 3: soFarBytes
                + TOTAL_BYTES + " INTEGER ,"  // 4: totalBytes
                + FILE_NAME + " TEXT);"); // 5: fileName
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
