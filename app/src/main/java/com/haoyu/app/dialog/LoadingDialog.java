package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.CircularProgressView;

/**
 * 加载对话框
 *
 * @author xiaoma
 */
public class LoadingDialog extends AlertDialog {
    private Context context;
    private String text;
    private CircularProgressView circularProgressView;
    private View view;

    public LoadingDialog(Context context, String text) {
        super(context, R.style.dialog);
        this.context = context;
        this.text = text;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialog();
    }


    private void initView() {
        view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        circularProgressView = view.findViewById(R.id.progressView);
        TextView message = view.findViewById(R.id.detail_tv);
        message.setText(text);
    }

    private void initDialog() {
        setCanceledOnTouchOutside(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 3,
                ScreenUtils.getScreenWidth(context) / 3);
        setContentView(view, params);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        circularProgressView.clearAnimation();
    }
}
