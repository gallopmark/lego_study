package com.haoyu.app.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * viewpage 和listview 相互冲突 将父view 传递到viewpage 里面
 * <p>
 * 使用父类的方法 parent.requestDisallowInterceptTouchEvent(true);
 * <p>
 * 当 requestDisallowInterceptTouchEvent 如果为true的时候 表示:父view 不拦截子view的touch 事件
 * <p>
 * 这个方法只是改变flag
 *
 * @author baozi
 */

/**
 * viewpage 和listview 相互冲突 将父view 传递到viewpage 里面
 * <p>
 * 使用父类的方法 parent.requestDisallowInterceptTouchEvent(true);
 * <p>
 * 当 requestDisallowInterceptTouchEvent 如果为true的时候 表示:父view 不拦截子view的touch 事件
 * <p>
 * 这个方法只是改变flag
 *
 * @author baozi
 */
public class DecoratorViewPager extends ViewPager {
    private ViewGroup parent;

    public DecoratorViewPager(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public DecoratorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNestedpParent(ViewGroup parent) {
        this.parent = parent;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(arg0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
            mFirstLayout.setAccessible(true);
            mFirstLayout.set(this, false);
            if (getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
            setCurrentItem(getCurrentItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View childView = getChildAt(getCurrentItem());
        if (childView != null) {
            childView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(childView.getMeasuredHeight(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}