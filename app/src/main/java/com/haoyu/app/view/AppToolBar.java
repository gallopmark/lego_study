package com.haoyu.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;


/**
 * 创建日期：2017/8/31 on 11:19
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppToolBar extends LinearLayout {
    /**
     * 标题栏的根布局
     */
    private LinearLayout ll_layout;
    /**
     * 标题栏的左边按返回按钮
     */
    private ImageView iv_leftImage;
    private TextView tv_leftView;

    /**
     * 标题栏的右边按保存按钮
     */
    private ImageView iv_rightImage;
    private TextView tv_rightView;
    /**
     * 标题栏的中间的文字
     */
    private TextView tv_title;
    /**
     * 标题栏的背景颜色
     */
    private int title_background_color;
    /**
     * 标题栏的显示的标题文字
     */
    private String title_text;
    /**
     * 标题栏的显示的标题文字颜色
     */
    private int title_textColor;
    /**
     * 返回按钮的资源图片
     */
    private int left_button_imageId;
    /**
     * 返回按钮上显示的文字
     */
    private String left_button_text;
    /**
     * 返回按钮上显示的文字颜色
     */
    private int left_button_textColor;
    /**
     * 是否显示返回按钮
     */
    private boolean show_left_button;

    /**
     * 右边保存按钮的资源图片
     */
    private int right_button_imageId;
    /**
     * 右边保存按钮的文字
     */
    private String right_button_text;
    /**
     * 右边保存按钮的文字颜色
     */
    private int right_button_textColor;
    /**
     * 是否显示右边保存按钮
     */
    private boolean show_right_button;

    private int left_background;
    private int right_background;

    private boolean showLeftView, showRightView;
    /**
     * 标题的点击事件
     */
    private OnLeftClickListener onLeftClickListener;
    private OnRightClickListener onRightClickListener;
    private TitleOnClickListener titleOnClickListener;

    public AppToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**加载布局文件*/
        LayoutInflater.from(context).inflate(R.layout.app_toolbar, this, true);
        ll_layout = findViewById(R.id.ll_layout);
        iv_leftImage = findViewById(R.id.iv_leftImage);
        tv_leftView = findViewById(R.id.tv_leftView);
        iv_rightImage = findViewById(R.id.iv_rightImage);
        tv_rightView = findViewById(R.id.tv_rightView);
        tv_title = findViewById(R.id.tv_title);

        /**获取属性值*/
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppToolBar);
        /**标题相关*/
        title_background_color = typedArray.getColor(R.styleable.AppToolBar_title_background, ContextCompat.getColor(context, R.color.defaultColor));
        title_text = typedArray.getString(R.styleable.AppToolBar_title_text);
        title_textColor = typedArray.getColor(R.styleable.AppToolBar_title_textColor, ContextCompat.getColor(context, R.color.white));
        /**返回按钮相关*/
        left_button_imageId = typedArray.getResourceId(R.styleable.AppToolBar_left_button_image, -1);
        left_button_text = typedArray.getString(R.styleable.AppToolBar_left_button_text);
        left_button_textColor = typedArray.getColor(R.styleable.AppToolBar_left_button_textColor, Color.WHITE);
        left_background = typedArray.getResourceId(R.styleable.AppToolBar_left_background, R.drawable.app_toolbar_selector);
        show_left_button = typedArray.getBoolean(R.styleable.AppToolBar_show_left_button, true);
        /**右边保存按钮相关*/
        right_button_imageId = typedArray.getResourceId(R.styleable.AppToolBar_right_button_image, -1);
        right_button_text = typedArray.getString(R.styleable.AppToolBar_right_button_text);
        right_button_textColor = typedArray.getColor(R.styleable.AppToolBar_right_button_textColor, Color.WHITE);
        right_background = typedArray.getResourceId(R.styleable.AppToolBar_right_background, R.drawable.app_toolbar_selector);
        show_right_button = typedArray.getBoolean(R.styleable.AppToolBar_show_right_button, true);
        /**设置值*/
        setTitle_background_color(title_background_color);
        setTitle_text(title_text);
        setTitle_textColor(title_textColor);
        if (!TextUtils.isEmpty(left_button_text)) {//返回按钮显示为文字
            setLeft_button_text(left_button_text);
            setLeft_button_textColor(left_button_textColor);
            showLeftView = true;
        } else {
            if (left_button_imageId != -1)
                setLeft_button_imageId(left_button_imageId);
            showLeftView = false;
        }
        iv_leftImage.setBackgroundResource(left_background);
        tv_leftView.setBackgroundResource(left_background);
        if (!TextUtils.isEmpty(right_button_text)) {//返回按钮显示为文字
            setRight_button_text(right_button_text);
            setRight_button_textColor(right_button_textColor);
            showRightView = true;
            iv_rightImage.setVisibility(GONE);
            tv_rightView.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
        } else {
            if (right_button_imageId != -1)
                setRight_button_imageId(right_button_imageId);
            showRightView = false;
            iv_rightImage.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
            tv_rightView.setVisibility(GONE);
        }
        iv_rightImage.setBackgroundResource(right_background);
        tv_rightView.setBackgroundResource(right_background);
        setShow_left_button(show_left_button);
        setShow_right_button(show_right_button);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.iv_leftImage:
                        if (onLeftClickListener != null)
                            onLeftClickListener.onLeftClick(view);
                        if (titleOnClickListener != null)
                            titleOnClickListener.onLeftClick(view);
                        break;
                    case R.id.tv_leftView:
                        if (onLeftClickListener != null)
                            onLeftClickListener.onLeftClick(view);
                        if (titleOnClickListener != null)
                            titleOnClickListener.onLeftClick(view);
                        break;
                    case R.id.iv_rightImage:
                        if (onRightClickListener != null)
                            onRightClickListener.onRightClick(view);
                        if (titleOnClickListener != null)
                            titleOnClickListener.onRightClick(view);
                        break;
                    case R.id.tv_rightView:
                        if (onRightClickListener != null)
                            onRightClickListener.onRightClick(view);
                        if (titleOnClickListener != null)
                            titleOnClickListener.onRightClick(view);
                        break;
                }
            }
        };
        iv_leftImage.setOnClickListener(listener);
        tv_leftView.setOnClickListener(listener);
        iv_rightImage.setOnClickListener(listener);
        tv_rightView.setOnClickListener(listener);
        typedArray.recycle();
    }

    /**
     * 设置返回按钮的资源图片id
     *
     * @param left_button_imageId 资源图片id
     */
    public void setLeft_button_imageId(int left_button_imageId) {
        iv_leftImage.setImageResource(left_button_imageId);
    }

    /**
     * 设置返回按钮的文字
     *
     * @param left_button_text
     */
    public void setLeft_button_text(CharSequence left_button_text) {
        tv_leftView.setText(left_button_text);
    }

    /**
     * 设置返回按钮的文字颜色
     *
     * @param left_button_textColor
     */
    public void setLeft_button_textColor(int left_button_textColor) {
        tv_leftView.setTextColor(left_button_textColor);
    }

    /**
     * 设置返回按钮的文字大小
     *
     * @param left_button_textSize
     */
    public void setLeft_button_textSize(float left_button_textSize) {
        tv_leftView.setTextSize(left_button_textSize);
    }

    /**
     * 设置是否显示返回按钮
     *
     * @param show_left_button
     */
    public void setShow_left_button(boolean show_left_button) {
        if (showLeftView) {
            iv_leftImage.setVisibility(GONE);
            tv_leftView.setVisibility(show_left_button ? VISIBLE : INVISIBLE);
        } else {
            iv_leftImage.setVisibility(show_left_button ? VISIBLE : INVISIBLE);
            tv_leftView.setVisibility(GONE);
        }
    }


    /**
     * 设置右边保存按钮的资源图片
     *
     * @param right_button_imageId
     */
    public void setRight_button_imageId(int right_button_imageId) {
        iv_rightImage.setImageResource(right_button_imageId);
    }

    /**
     * 设置右边的保存按钮的文字
     *
     * @param right_button_text
     */
    public void setRight_button_text(CharSequence right_button_text) {
        tv_rightView.setText(right_button_text);
    }

    /**
     * 设置右边保存按钮的文字颜色
     *
     * @param right_button_textColor
     */
    public void setRight_button_textColor(int right_button_textColor) {
        tv_rightView.setTextColor(right_button_textColor);
    }

    /**
     * 设置右边保存按钮的文字大小
     *
     * @param right_button_textSize
     */
    public void setRight_button_textSize(float right_button_textSize) {
        tv_rightView.setTextSize(right_button_textSize);
    }

    /**
     * 设置是显示右边保存按钮
     *
     * @param show_right_button
     */
    public void setShow_right_button(boolean show_right_button) {
        if (showRightView) {
            iv_rightImage.setVisibility(GONE);
            tv_rightView.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
        } else {
            iv_rightImage.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
            tv_rightView.setVisibility(GONE);
        }
    }


    /**
     * 设置标题背景的颜色
     *
     * @param title_background_color
     */
    public void setTitle_background_color(int title_background_color) {
        ll_layout.setBackgroundColor(title_background_color);
    }

    /**
     * 设置标题的文字
     *
     * @param title_text
     */
    public void setTitle_text(CharSequence title_text) {
        tv_title.setText(title_text);
    }

    /**
     * 设置标题的文字颜色
     *
     * @param title_textColor
     */
    public void setTitle_textColor(int title_textColor) {
        tv_title.setTextColor(title_textColor);
    }

    /**
     * 设置标题的文字大小
     *
     * @param title_textSize
     */
    public void setTitle_textSize(float title_textSize) {
        tv_title.setTextSize(title_textSize);
    }

    public LinearLayout getLl_layout() {
        return ll_layout;
    }

    public ImageView getIv_leftImage() {
        return iv_leftImage;
    }

    public TextView getTv_leftView() {
        return tv_leftView;
    }

    public ImageView getIv_rightImage() {
        return iv_rightImage;
    }

    public TextView getTv_rightView() {
        return tv_rightView;
    }

    public TextView getTv_title() {
        return tv_title;
    }

    public void setOnLeftClickListener(OnLeftClickListener onLeftClickListener) {
        this.onLeftClickListener = onLeftClickListener;
    }

    public void setOnRightClickListener(OnRightClickListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }

    /**
     * 设置标题的点击监听
     *
     * @param titleOnClickListener
     */
    public void setOnTitleClickListener(TitleOnClickListener titleOnClickListener) {
        this.titleOnClickListener = titleOnClickListener;
    }

    /**
     * 监听标题点击接口
     */
    public interface TitleOnClickListener {
        /**
         * 返回按钮的点击事件
         */
        void onLeftClick(View view);

        /**
         * 保存按钮的点击事件
         */
        void onRightClick(View view);
    }

    public interface OnLeftClickListener {
        /**
         * 返回按钮的点击事件
         */
        void onLeftClick(View view);
    }

    public interface OnRightClickListener {
        /**
         * 保存按钮的点击事件
         */
        void onRightClick(View view);
    }
}
