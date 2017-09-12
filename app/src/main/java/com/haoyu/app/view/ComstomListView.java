package com.haoyu.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * 创建日期：2016/11/24 on 10:59
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class ComstomListView extends ListView {
    public ComstomListView(Context context) {
        super(context);
    }

    public ComstomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComstomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true; // 禁止GridView滑动
        }
        return super.dispatchTouchEvent(ev);
    }
}
