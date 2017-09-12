package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.RoundRectProgressBar;


/**
 * 创建日期：2017/3/27 on 16:02
 * 描述:文件上传或下载dialog
 * 作者:马飞奔 Administrator
 */
public class FileUploadDialog extends AlertDialog {
    private Context context;
    private View contentView;
    private RoundRectProgressBar mProgressBar;
    private TextView tv_fileName;
    private TextView tv_progress;
    private Button bt_close;
    private String fileName;
    private String tipText;
    private CancelListener cancelListener;

    public FileUploadDialog(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public FileUploadDialog(Context context, String fileName) {
        super(context);
        this.context = context;
        this.fileName = fileName;
        initView();
    }

    public FileUploadDialog(Context context, String fileName, String tipText) {
        super(context);
        this.context = context;
        this.fileName = fileName;
        this.tipText = tipText;
        initView();
    }

    private void initView() {
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_upload, null);
        tv_fileName = contentView.findViewById(R.id.tv_fileName);
        mProgressBar = contentView.findViewById(R.id.mRrogressBar);
        tv_progress = contentView.findViewById(R.id.tv_progress);
        bt_close = contentView.findViewById(R.id.bt_close);
        tv_fileName.setText(fileName);
        tv_progress.setText(tipText + "\u2000" + "0%");
        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (cancelListener != null) {
                    cancelListener.cancel();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (ScreenUtils.getScreenWidth(context) / 6 * 5),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(contentView, params);
        setCanceledOnTouchOutside(false);
    }

    public void setFileName(String fileName) {
        tv_fileName.setText(fileName);
    }

    public void setUploadProgressBar(long totalBytes, long remainingBytes) {
        mProgressBar.setMax((int) totalBytes);
        mProgressBar.setProgress((int) (totalBytes - remainingBytes));
    }

    public void setUploadText(long totalBytes, long remainingBytes) {
        tv_progress.setText(tipText + "\u2000" + (totalBytes - remainingBytes) * 100 / totalBytes + "%");
    }

    public interface CancelListener {
        void cancel();
    }

    public void setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }
}
