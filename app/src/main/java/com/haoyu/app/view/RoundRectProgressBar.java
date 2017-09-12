package com.haoyu.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.haoyu.app.lego.student.R;


/**
 * 水平进度条边部室圆角
 */
public class RoundRectProgressBar extends ProgressBar {

    private int mProgressColor = getResources().getColor(R.color.defaultColor);//default value
    private int mProgressBackgroundColor = getResources().getColor(R.color.spaceColor);//default value
    private Paint mPaint;

    public int getmProgressColor() {
        return mProgressColor;
    }

    public void setmProgressColor(int mProgressColor) {
        this.mProgressColor = mProgressColor;
    }

    public int getmProgressBackgroundColor() {
        return mProgressBackgroundColor;
    }

    public void setmProgressBackgroundColor(int mProgressBackgroundColor) {
        this.mProgressBackgroundColor = mProgressBackgroundColor;
    }

    public RoundRectProgressBar(Context context) {
        this(context, null);
    }

    public RoundRectProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrsValues(attrs);
        //init paint
        mPaint = new Paint();
    }

    /**
     * get attrs values
     *
     * @param attrs
     */
    private void getAttrsValues(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RoundRectProgressBar);
        mProgressColor = ta.getColor(R.styleable.RoundRectProgressBar_progress_color, mProgressColor);
        mProgressBackgroundColor = ta.getColor(R.styleable.RoundRectProgressBar_progress_background_color, mProgressBackgroundColor);
        ta.recycle();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();

        // circle radius
        int radius = getHeight() / 2;

        //draw background
        mPaint.setColor(mProgressBackgroundColor);
        canvas.drawCircle(getPaddingLeft() + radius, getPaddingTop() + radius, radius, mPaint);
        canvas.drawRect(getPaddingLeft() + radius, getPaddingTop(), getWidth() - radius, getHeight(), mPaint);
        canvas.drawCircle(getWidth() - radius, getPaddingTop() + radius, radius, mPaint);

        //draw progress
        float progressLength = getProgress() * 1.0f / getMax() * getWidth();
        mPaint.setColor(mProgressColor);
        if (progressLength >= radius) {
            canvas.drawCircle(getPaddingLeft() + radius, getPaddingTop() + radius, radius, mPaint);
            canvas.drawRect(getPaddingLeft() + radius, getPaddingTop(), progressLength - radius, getHeight(), mPaint);
            if (progressLength - radius > radius) {
                canvas.drawCircle(progressLength - radius, getPaddingTop() + radius, radius, mPaint);
            }
        }

        canvas.restore();
    }
}
