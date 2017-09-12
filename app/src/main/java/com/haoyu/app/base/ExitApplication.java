package com.haoyu.app.base;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

public class ExitApplication extends Application {
    private static ExitApplication instance;
    private List<Activity> activityList = new LinkedList<Activity>();
    private boolean isExit = false;

    public static ExitApplication getInstance() {
        if (instance == null) {
            instance = new ExitApplication();
        }
        return instance;
    }

    public void addActivity(Activity paramActivity) {
        this.activityList.add(paramActivity);
    }

    public void exit() {
        for (Activity activity : activityList) {
            // Log.d("ExitAppLication.getInstance().exit()",
            // "关闭的Activity："+activity.getClass().getName());
            activity.finish();
        }
        System.exit(0);
    }

    public void logout(){
        for (Activity activity : activityList) {
            activity.finish();
        }
    }

    public void remove(Activity activity) {
        activityList.remove(activity);
    }

    public boolean isExit() {
        return this.isExit;
    }

    public void setExit(boolean isExit) {
        this.isExit = isExit;
    }
}
