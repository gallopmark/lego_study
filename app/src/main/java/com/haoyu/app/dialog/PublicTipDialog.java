package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.haoyu.app.lego.student.R;


/**
 * 创建日期：2016/11/14 on 10:21
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PublicTipDialog extends AlertDialog implements DialogInterface.OnDismissListener {
    private Context context;
    private ImageView iv_loading;
    private AnimationDrawable animationDrawable;

    public PublicTipDialog(Context context) {
        super(context, R.style.PublicTipDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setOnDismissListener(this);
    }

    private void init() {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_loading_tips, null);
        iv_loading = contentView.findViewById(R.id.iv_loading);
        animationDrawable = (AnimationDrawable) iv_loading.getDrawable();
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            animationDrawable.start();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(contentView, params);
        setCanceledOnTouchOutside(false);
        getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (animationDrawable != null && animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
    }
}
