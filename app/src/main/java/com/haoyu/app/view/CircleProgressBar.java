package com.haoyu.app.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.haoyu.app.lego.student.R;


public class CircleProgressBar extends View {
    private int maxProgress = 100;
    private int progress;
    private int progressStrokeWidth;
    /**
     * 圆环的颜色
     */
    private int roundColor;

    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;
    /**
     * 圆环的宽度
     */
    private float roundWidth;
    /**
     * 进度圆环宽度
     */
    private float paintWidth;
    // 画圆所在的距形区域
    RectF oval;
    Paint paint;

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO 自动生成的构造函数存根
        oval = new RectF();
        paint = new Paint();
        progressStrokeWidth = Tools.dipToPx(context, 3);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CircleProgressBar);
        // 获取自定义属性和默认值
        roundColor = mTypedArray.getColor(
                R.styleable.CircleProgressBar_CircleColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(
                R.styleable.CircleProgressBar_CircleProgressColor, Color.GREEN);
        roundWidth = mTypedArray.getDimension(
                R.styleable.CircleProgressBar_CircleWidth, 2);
        paintWidth = mTypedArray.getDimension(
                R.styleable.CircleProgressBar_paintWidth, 4);
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO 自动生成的方法存根
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (centre - roundWidth / 2); // 圆环的半径
        paint.setAntiAlias(true); // 设置画笔为抗锯齿
        // paint.setColor(Color.TRANSPARENT); // 设置画笔颜色
        paint.setColor(roundColor); // 设置画笔颜色
        canvas.drawColor(Color.TRANSPARENT); // 白色背景
        // paint.setStrokeWidth(progressStrokeWidth); // 线宽
        paint.setStrokeWidth(roundWidth); // 线宽
        paint.setStyle(Style.STROKE);

//		oval.left = progressStrokeWidth / 2; // 左上角x
//		oval.top = progressStrokeWidth / 2; // 左上角y
//		oval.right = width - progressStrokeWidth / 2; // 左下角x
//		oval.bottom = height - progressStrokeWidth / 2; // 右下角y

        // canvas.drawArc(oval, -90, 360, true, paint); // 绘制白色圆圈，即进度条背景
        canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
        // paint.setColor(Color.RED);
        int paintRadius = (int) (centre - paintWidth / 2); // 圆环的半径
        paint.setColor(roundProgressColor);
        paint.setStrokeWidth(paintWidth);
        // paint.setColor(Color.rgb(0x57, 0x87, 0xb6));
        oval.left = centre - paintRadius; // 左上角x
        oval.top = centre - paintRadius; // 左上角y
        oval.right = centre + paintRadius; // 右下角x
        oval.bottom = centre + paintRadius; // 右下角y
        canvas.drawArc(oval, -90, ((float) progress / maxProgress) * 360,
                false, paint); // 绘制进度圆弧，这里是蓝色
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.invalidate();
    }

    /**
     * 非ＵＩ线程调用
     */
    public void setProgressNotInUiThread(int progress) {
        this.progress = progress;
        this.postInvalidate();
    }

    private static class Tools {
        public static int dipToPx(Context context, int dip) {
            return (int) (dip * context.getResources().getDisplayMetrics().density);
        }
    }
}
