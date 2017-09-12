package com.haoyu.app.imageloader;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.haoyu.app.lego.student.R;

import java.io.File;

/**
 * 创建日期：2017/3/7 on 11:46
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class GlideImgManager {
    /**
     * load normal  for img
     *
     * @param url
     * @param erroImg
     * @param emptyImg
     * @param iv
     */
    public static void loadCircleImage(Context context, String url, int emptyImg, int erroImg, ImageView iv) {
        //原生 API
        Glide.with(context).load(url).placeholder(emptyImg).error(erroImg).dontAnimate()
                .centerCrop().transform(new GlideCircleTransform(context)).into(iv);
    }

    public static void loadCircleImage(Context context, File file, int emptyImg, int erroImg, ImageView iv) {
        //原生 API
        Glide.with(context).load(file).placeholder(emptyImg).error(erroImg).dontAnimate()
                .centerCrop().transform(new GlideCircleTransform(context)).into(iv);
    }

    public static void loadImage(Context context, String url, int emptyImg, int erroImg, ImageView iv) {
        //原生 API
        Glide.with(context).load(url).placeholder(emptyImg).error(erroImg).centerCrop().dontAnimate().into(iv);
    }

    public static void loadGifImage(Context context, String url, ImageView iv) {
        Glide.with(context).load(url).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.app_default).error(R.drawable.app_default).into(iv);
    }

    public static void loadImage(Context context, final File file, final ImageView imageView) {
        Glide.with(context)
                .load(file)
                .into(imageView);
    }

    public static void loadImage(Context context, final int resourceId, final ImageView imageView) {
        Glide.with(context)
                .load(resourceId)
                .into(imageView);
    }
}
