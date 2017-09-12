package com.haoyu.app.activity;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Validator;

import butterknife.BindView;

/**
 * 重置密码
 *
 * @author xiaoma
 *
 */
public class ResetPasswordActivity extends BaseActivity implements
        OnClickListener {
    private ResetPasswordActivity context = this;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.et_password1)
    EditText et_password1; // 新密码
    @BindView(R.id.et_password2)
    EditText et_password2; // 确认新密码
    @BindView(R.id.bt_finish)
    Button bt_finish; // 完成

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n"))
                return "";
            else
                return null;
        }
    };

    @Override
    public int setLayoutResID() {
        return R.layout.activity_reset_password;
    }

    @Override
    public void initView() {
        et_password1.setFilters(new InputFilter[] { filter });
        et_password2.setFilters(new InputFilter[] { filter });
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(context);
        bt_finish.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_finish:
                String password1 = et_password1.getText().toString().trim();
                String password2 = et_password2.getText().toString().trim();
                if (password1.length() == 0) {
                    et_password1.setHint(null);
                    // toast(context, "请输入新密码");
                    return;
                }
                if (!Validator.isPassword(password1)) {
                    et_password1.setHint(null);
                    // toast(context, "密码格式错误");
                    return;
                }
                if (password2.length() == 0) {
                    et_password2.setHint(null);
                    // toast(context, "请输入确认密码");
                    return;
                }
                if (!password1.equals(password2)) {
                    et_password2.setText("");
                    toast(context, "前后密码不一致，请重新输入");
                } else {
                    View view = context.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    setContentView(R.layout.activity_reset_finish);
                    findViewById();
                }
                break;
        }
    }

    private TextView tv_countdown;
    final Handler handler = new Handler();
    private int time = 4;

    private void findViewById() {
        tv_countdown = (TextView) findViewById(R.id.tv_countdown);
        handler.postDelayed(runnable, 1000);
    }

    /**
     * 倒计时3秒回到登录界面
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time--;
            if (time == 0) {
                finish();
            } else {
                tv_countdown.setText("（" + time + "s）");
                handler.postDelayed(this, 1000);
            }
        }
    };
}
