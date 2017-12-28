package com.haoyu.app.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.haoyu.app.lego.student.R;


public class CircleProgressBar extends View {
    private float maxProgress = 100;
    private float currentValue;
    private int arcColor; //圆环的颜色
    private int progressColor;  //圆环进度的颜色
    private float arcWidth;  //圆环的宽度
    private float progressWidth; //进度圆环宽度
    // 画圆所在的距形区域
    private RectF bgRect;
    private Paint allArcPaint;
    private Paint progressPaint;
    private float startAngle = -90;
    private float sweepAngle = 360;
    private int aniSpeed = 1000;

    public CircleProgressBar(Context context) {
        super(context, null);
        initView();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCofig(context, attrs);
        initView();
    }


    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCofig(context, attrs);
        initView();
    }

    private void initCofig(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        // 获取自定义属性和默认值
        arcColor = mTypedArray.getColor(R.styleable.CircleProgressBar_CircleColor, Color.YELLOW);
        progressColor = mTypedArray.getColor(R.styleable.CircleProgressBar_CircleProgressColor, Color.GREEN);
        arcWidth = mTypedArray.getDimension(R.styleable.CircleProgressBar_CircleArcWidth, dipToPx(context, 2));
        progressWidth = mTypedArray.getDimension(R.styleable.CircleProgressBar_CircleProgressWidth, dipToPx(context, 4));
        maxProgress = mTypedArray.getFloat(R.styleable.CircleProgressBar_maxValue, 100);
        float progress = mTypedArray.getFloat(R.styleable.CircleProgressBar_currentValue, 0);
        startAngle = mTypedArray.getFloat(R.styleable.CircleProgressBar_startAngle, -90);
        sweepAngle = mTypedArray.getFloat(R.styleable.CircleProgressBar_sweepAngle, 360);
        aniSpeed = mTypedArray.getInt(R.styleable.CircleProgressBar_aniSpeed, 1000);
        setMaxProgress(maxProgress);
        setProgress(progress);
        mTypedArray.recycle();
    }

    private void initView() {
        bgRect = new RectF();
        //整个弧形
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeWidth(arcWidth);
        allArcPaint.setColor(arcColor);
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);
        //当前进度的弧形
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setColor(progressColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerX = getWidth() / 2;
        int paintRadius = (int) (centerX - progressWidth / 2); // 圆环的半径
        bgRect.left = centerX - paintRadius; // 左上角x
        bgRect.top = centerX - paintRadius; // 左上角y
        bgRect.right = centerX + paintRadius; // 右下角x
        bgRect.bottom = centerX + paintRadius; // 右下角y
//        //整个弧
        canvas.drawCircle(centerX, centerX, paintRadius, allArcPaint); // 画出圆环
        canvas.drawArc(bgRect, startAngle, currentValue, false, progressPaint); // 绘制进度圆弧，这里是蓝色
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgress(float progress) {
        setProgress(progress, false);
    }

    public void setProgress(float progress, boolean animate) {
        if (progress > maxProgress) {
            progress = maxProgress;
        }
        if (progress < 0) {
            progress = 0;
        }
        currentValue = (progress / maxProgress) * sweepAngle;
        if (animate) {
            setAnimation();
        } else {
            invalidate();
        }
    }

    private void setAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, currentValue);
        animator.setDuration(aniSpeed);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentValue = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }

    /**
     * 非ＵＩ线程调用
     */
    public void setProgressNotInUiThread(float progress) {
        if (progress > maxProgress) {
            progress = maxProgress;
        }
        if (progress < 0) {
            progress = 0;
        }
        currentValue = (progress / maxProgress) * sweepAngle;
        postInvalidate();
    }

    private int dipToPx(Context context, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }
}
