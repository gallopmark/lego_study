package com.haoyu.app.utils;

import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import org.xml.sax.XMLReader;

import java.util.Locale;

/**
 * 创建日期：2017/10/25 on 14:01
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class HtmlTagHandler implements Html.TagHandler {
    private OnImageClickListener onImageClickListener;

    public HtmlTagHandler() {

    }

    public HtmlTagHandler(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        // 处理标签<img>
        if (tag.toLowerCase(Locale.getDefault()).equals("img")) {
            // 获取长度
            int len = output.length();
            // 获取图片地址
            ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
            String imgURL = images[0].getSource();
            // 使图片可点击并监听点击事件
            output.setSpan(new ClickableImage(imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private class ClickableImage extends ClickableSpan {
        private String url;

        public ClickableImage(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View view) {
            if (onImageClickListener != null)
                onImageClickListener.onImageClick(view, url);
        }
    }

    public interface OnImageClickListener {
        void onImageClick(View view, String url);
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }
}
