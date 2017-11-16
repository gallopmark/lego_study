package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.TouchImageView;

/**
 * 创建日期：2017/11/16.
 * 描述:图片预览
 * 作者:xiaoma
 */

public class PictureViewerFragment extends Fragment {

    private Context context;
    private String url;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        url = bundle.getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TouchImageView imageView = new TouchImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        loadImage(imageView);
        return imageView;
    }

    private void loadImage(TouchImageView imageView) {
        Glide.with(context).load(url).placeholder(R.drawable.ic_placeholder).fitCenter().dontAnimate().into(imageView);
    }
}
