package com.haoyu.app.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

/**
 * 创建日期：2017/11/28.
 * 描述:兼容性popupwindow 显示的位置在6.0 7.0 7.1.1的兼容
 * 作者:xiaoma
 */

public class SupportPopupWindow extends PopupWindow {
    public SupportPopupWindow(Context context) {
        super(context);
    }

    public SupportPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SupportPopupWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SupportPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SupportPopupWindow(View contentView) {
        super(contentView);
    }

    public SupportPopupWindow() {
        super();
    }

    public SupportPopupWindow(int width, int height) {
        super(width, height);
    }

    public SupportPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    public SupportPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
    }

    @Override
    public void showAsDropDown(View anchor) {
        setSupportDrawDown(anchor);
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        setSupportDrawDown(anchor);
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        setSupportDrawDown(anchor);
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    private void setSupportDrawDown(View anchor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
    }
}
