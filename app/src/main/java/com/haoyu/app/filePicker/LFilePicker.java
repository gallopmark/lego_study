package com.haoyu.app.filePicker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class LFilePicker {
    private Activity mActivity;
    private Fragment mFragment;
    private android.support.v4.app.Fragment mSupportFragment;
    private int mRequestCode;
    private boolean mMutilyMode = true;
    private String[] mFileTypes;

    /**
     * 绑定Activity
     *
     * @param activity
     * @return
     */
    public LFilePicker withActivity(Activity activity) {
        this.mActivity = activity;
        return this;
    }

    /**
     * 绑定Fragment
     *
     * @param fragment
     * @return
     */
    public LFilePicker withFragment(Fragment fragment) {
        this.mFragment = fragment;
        return this;
    }

    /**
     * 绑定v4包Fragment
     *
     * @param supportFragment
     * @return
     */
    public LFilePicker withSupportFragment(android.support.v4.app.Fragment supportFragment) {
        this.mSupportFragment = supportFragment;
        return this;
    }


    /**
     * 请求码
     *
     * @param requestCode
     * @return
     */
    public LFilePicker withRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    /**
     * 设置选择模式，默认为true,多选；false为单选
     *
     * @param isMutily
     * @return
     */
    public LFilePicker withMutilyMode(boolean isMutily) {
        this.mMutilyMode = isMutily;
        return this;
    }

    public LFilePicker withFileFilter(String[] arrs) {
        this.mFileTypes = arrs;
        return this;
    }

    public void start() {
        if (mActivity == null && mFragment == null && mSupportFragment == null) {
            throw new RuntimeException("You must pass Activity or Fragment by withActivity or withFragment or withSupportFragment method");
        }
        Intent intent = initIntent();
        Bundle bundle = getBundle();
        intent.putExtras(bundle);

        if (mActivity != null) {
            mActivity.startActivityForResult(intent, mRequestCode);
        } else if (mFragment != null) {
            mFragment.startActivityForResult(intent, mRequestCode);
        } else {
            mSupportFragment.startActivityForResult(intent, mRequestCode);
        }
    }


    private Intent initIntent() {
        Intent intent;
        if (mActivity != null) {
            intent = new Intent(mActivity, LFilePickerActivity.class);
        } else if (mFragment != null) {
            intent = new Intent(mFragment.getActivity(), LFilePickerActivity.class);
        } else {
            intent = new Intent(mSupportFragment.getActivity(), LFilePickerActivity.class);
        }
        return intent;
    }

    @NonNull
    private Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putStringArray("mFileTypes", mFileTypes);
        bundle.putBoolean("mMutilyMode", mMutilyMode);
        return bundle;
    }
}
