package com.haoyu.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 创建日期：2016/12/24 on 9:14
 * 描述: Android关于实现EditText中加多行下划线的的一种方法
 * 作者:马飞奔 Administrator
 */
public class LinedEditText extends EditText {
//    private Paint mPaint = new Paint();


    public LinedEditText(Context context) {
        super(context);
        initPaint();
    }


    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }


    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }


    private void initPaint() {
//        mPaint.setStyle(Paint.Style.STROKE);
////        mPaint.setColor(0x80000000);
//      mPaint.setStyle(Paint.Style.STROKE);
//      mPaint.setColor(R.color.dashed_line_color);
//        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
//        mPaint.setPathEffect(effects);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDraw(Canvas canvas) {
        Paint mPaint = new Paint();
//       mPaint.setColor(0x80000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.LTGRAY);
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 5);
        mPaint.setPathEffect(effects);

        int left = getLeft();
        int right = getRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int height = getHeight();
        int lineHeight = getLineHeight();
        int spcingHeight = (int) getLineSpacingExtra();
        int count = (height - paddingTop - paddingBottom) / lineHeight;


        for (int i = 0; i < count; i++) {
            int baseline = lineHeight * (i + 1) + paddingTop - spcingHeight / 2;
            canvas.drawLine(0, baseline, right - paddingRight, baseline, mPaint);
        }


        super.onDraw(canvas);
    }
}