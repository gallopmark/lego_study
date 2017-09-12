package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.ScreenUtils;


/**
 * 创建日期：2017/3/21 on 8:54
 * 描述:评论对话框
 * 作者:马飞奔 Administrator
 */
public class CommentDialog extends AlertDialog {
    private Context context;
    private View contentView;
    private String hint = "";
    private String btText;
    private OnSendCommentListener sendCommentListener;

    public CommentDialog(Context context) {
        super(context);
        this.context = context;
        initView();
    }


    public CommentDialog(Context context, String hint) {
        super(context);
        this.context = context;
        this.hint = hint;
        initView();
    }

    public CommentDialog(Context context, String hint, String btText) {
        super(context);
        this.context = context;
        this.hint = hint;
        this.btText = btText;
        initView();
    }

    public void setSendCommentListener(OnSendCommentListener sendCommentListener) {
        this.sendCommentListener = sendCommentListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void initView() {
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
        setView(contentView);
    }

    private void init() {
        Window window = getWindow();
        window.setContentView(R.layout.dialog_comment);
        final EditText et_imput = findViewById(R.id.et_content);
        et_imput.setHint(hint);
        et_imput.requestFocus();
        et_imput.setFocusable(true);
        final Button bt_send = findViewById(R.id.bt_send);
        bt_send.setEnabled(false);
        if (btText != null) {
            bt_send.setText(btText);
        }
        et_imput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    bt_send.setEnabled(true);
                } else {
                    bt_send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.hideSoftInput(context, et_imput);
                String content = et_imput.getText().toString();
                if (sendCommentListener != null) {
                    sendCommentListener.sendComment(content);
                }
                dismiss();
            }
        });
        window.setLayout(ScreenUtils.getScreenWidth(context), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setWindowAnimations(R.style.dialog_anim);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        window.setGravity(Gravity.BOTTOM);
    }

    public interface OnSendCommentListener {
        void sendComment(String content);
    }
}
