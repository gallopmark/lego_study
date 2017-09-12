package com.haoyu.app.activity;

import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Validator;
import com.haoyu.app.view.TimeButton;

import butterknife.BindView;

/**
 * 忘记密码，通过姓名，手机号，验证码进行找回
 *
 * @author xiaoma
 */
public class ForgetPassWordActivity extends BaseActivity implements
        OnClickListener {
    private ForgetPassWordActivity context = this;
    @BindView(R.id.iv_back)
    ImageView iv_back; // 返回
    @BindView(R.id.et_name)
    EditText et_name; // 姓名
    @BindView(R.id.tv_emptyPhone)
    TextView tv_emptyPhone; // 空手机号
    @BindView(R.id.et_phone)
    EditText et_phone; // 登记的手机号
    @BindView(R.id.tv_emptyCode)
    TextView tv_emptyCode; // 空验证码
    @BindView(R.id.et_security_code)
    EditText et_security_code; // 验证码
    @BindView(R.id.tb_getCode)
    TimeButton tb_getCode; // 点击获取验证码
    @BindView(R.id.bt_next)
    Button bt_next; // 下一步

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
        return R.layout.activity_forget_password;
    }

    @Override
    public void initView() {
        // tb_getCode.onCreate(savedInstanceState);
        et_name.setFilters(new InputFilter[] { filter });
        et_phone.setFilters(new InputFilter[] { filter });
        et_security_code.setFilters(new InputFilter[] { filter });
        tb_getCode.setTextAfter("秒后重新获取").setTextBefore("获取验证码")
                .setLenght(60 * 1000);
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(context);
        tb_getCode.setOnClickListener(context);
        bt_next.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tb_getCode:
                getCode();
                break;
            case R.id.bt_next:
                String name = et_name.getText().toString().trim();
                String phone = et_phone.getText().toString().trim();
                String code = et_security_code.getText().toString();
                if (name.length() == 0) {
                    et_name.setHint(null);
                    // toast(context, "请输入姓名");
                    return;
                }
                if (phone.length() == 0) {
                    et_phone.setHint(null);
                    tv_emptyPhone.setText("请输入手机号");
                    tv_emptyPhone.setVisibility(View.VISIBLE);
                    // toast(context, "请输入登记手机号");
                    return;
                }
                if (!Validator.isMobile(phone)) {
                    et_phone.setHint(null);
                    tv_emptyPhone.setText("无效的手机号");
                    tv_emptyPhone.setVisibility(View.VISIBLE);
                    // toast(context, "手机号格式错误");
                    return;
                }
                if (code.length() == 0) {
                    tv_emptyCode.setHint(null);
                    tv_emptyCode.setText("请输入验证码");
                    tv_emptyCode.setVisibility(View.VISIBLE);
                    return;
                }
                startActivity(new Intent(context, ResetPasswordActivity.class));
                break;
        }
    }

    /**
     * 获取验证码
     */
    private void getCode() {
        et_security_code.setText("A2B3D");
    }
}
