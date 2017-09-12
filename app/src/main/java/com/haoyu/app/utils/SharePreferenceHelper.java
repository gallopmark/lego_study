package com.haoyu.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * 创建日期：2016/12/26 on 11:10
 * 描述:
 * 作者:马飞奔 Administrator
 */
//封装SharedPreferences类

public class SharePreferenceHelper {
    private SharedPreferences preferences;

    public SharePreferenceHelper(Context context) {
        preferences = context.getSharedPreferences(Constants.Prefs_user, Context.MODE_PRIVATE);
    }

    public boolean saveSharePreference(Map<String, Object> map) {
        boolean flag = false;
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object == null) {
                editor.remove(key);
            }
            if (object instanceof Boolean) {
                boolean b = (boolean) object;
                editor.putBoolean(key, b);
            }
            if (object instanceof String) {
                String s = (String) object;
                editor.putString(key, s);
            }
            if (object instanceof Integer) {
                Integer i = (Integer) object;
                editor.putInt(key, i);
            }
            if (object instanceof Float) {
                Float f = (Float) object;
                editor.putFloat(key, f);
            }
            if (object instanceof Long) {
                Long l = (Long) object;
                editor.putLong(key, l);
            }
        }
        editor.commit();
        return flag;
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("userName", "");
    }

    public static String getAvatar(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("avatar", "");
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("id", "");
    }

    public static String getRealName(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("realName", "");
    }

    public static String getDeptName(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("deptName", "");
    }

    public static String getRole(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("role", "");
    }

    public static String getAccount(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("account", "");
    }

    public static String getPassWord(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("password", "");
    }

    public static String getStage(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("stage", "");
    }

    public static String getSubject(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString("subject", "");
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.Prefs_user, Context.MODE_PRIVATE);
    }
}
