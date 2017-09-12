package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.LoadingDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.LoginResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.SharePreferenceHelper;
import com.haoyu.app.utils.Validator;
import com.haoyu.app.view.AppCheckBox;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/4/21 on 11:05
 * 描述:二师登录页面
 * 作者:马飞奔 Administrator
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private LoginActivity context = this;
    @BindView(R.id.rootView)
    ScrollView rootView;
    @BindView(R.id.et_userName)
    EditText et_userName;
    @BindView(R.id.et_passWord)
    EditText et_passWord;
    @BindView(R.id.ll_check)
    LinearLayout ll_check;
    @BindView(R.id.appCheckBox)
    AppCheckBox appCheckBox;
    @BindView(R.id.bt_login)
    Button bt_login;
    @BindView(R.id.forget_password)
    TextView forget_password;
    private boolean requestUN = true;

    /**
     * 用户名和密码过滤空格符号和回车符号
     */
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
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        et_userName.setFilters(new InputFilter[]{filter}); // 禁止输空格+换行键
        et_passWord.setFilters(new InputFilter[]{filter}); // 禁止输空格+换行键
        forget_password.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
        forget_password.getPaint().setAntiAlias(true);// 抗锯齿
        et_userName.setText(getAccount());
        et_passWord.setText(getPassWord());
        appCheckBox.setChecked(isRemember(), false);
        et_userName.setSelection(et_userName.getText().length());
        controlKeyboardLayout();
    }

    /**
     * 最外层布局，需要调整的布局
     * 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
     */
    private void controlKeyboardLayout() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                rootView.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = rootView.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > 100) {
                    rootView.fullScroll(ScrollView.FOCUS_DOWN); //滚动到底部
                } else {
                    //键盘隐藏
                    rootView.fullScroll(ScrollView.FOCUS_UP); //滚动到顶部
                }
                requestFocus();
            }
        });
    }

    private void requestFocus() {
        if (requestUN) {
            et_userName.requestFocus();
            et_passWord.clearFocus();
        } else {
            et_userName.clearFocus();
            et_passWord.requestFocus();
        }
    }

    @Override
    public void initData() {

    }

    @Override
    public void setListener() {
        bt_login.setOnClickListener(context);
        forget_password.setOnClickListener(context);
        et_userName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                requestUN = true;
                return false;
            }
        });
        et_passWord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                requestUN = false;
                return false;
            }
        });
        ll_check.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_check:
                appCheckBox.toggle();
                return;
            case R.id.bt_login:
                Common.hideSoftInput(context);
                String userName = et_userName.getText().toString().trim();
                String passWord = et_passWord.getText().toString().trim();
                if (userName.length() == 0) {
                    toast(context, "请输入账号");
                    return;
                }
//                if (!Validator.isUserName(userName)) {
//                    toast(context, "账号不存在");
//                    return;
//                }
                if (passWord.length() == 0) {
                    toast(context, "请输入密码");
                    return;
                }
                if (!Validator.isPassword(passWord)) {
                    toast(context, "无效密码");
                    return;
                }
                bt_login.setEnabled(false);
                login(userName, passWord);
                return;
            case R.id.forget_password:
                startActivity(new Intent(context, ForgetPassWordActivity.class));
                return;
        }
    }

    private void login(final String userName, final String passWord) {
        final LoadingDialog loading = new LoadingDialog(context, "正在登录");
        loading.setCanceledOnTouchOutside(false);
        loading.show();
        final String url = Constants.LOGIN_URL;
        addSubscription(Flowable.just(url).map(new Function<String, LoginResult>() {
            @Override
            public LoginResult apply(String url) throws Exception {
                return getUserInfo(url, userName, passWord);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LoginResult>() {
                    @Override
                    public void accept(LoginResult result) throws Exception {
                        loading.dismiss();
                        bt_login.setEnabled(true);
                        if (result != null && result.getResponseData() != null && result.getResponseData().getRole() != null
                                && result.getResponseData().getRole().contains(student)) {
                            saveUserInfo(result);
                            Intent intent = new Intent(context, AppHomePageActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showInfoDialog();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        loading.dismiss();
                        bt_login.setEnabled(true);
                        toast(context, "登录失败");
                    }
                }));
    }

    private LoginResult getUserInfo(String url, String userName, String passWord) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("username", userName);
        map.put("password", passWord);
        return OkHttpClientManager.getInstance().login(context, url, userName, passWord);
    }

    private void showInfoDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("请选择正确版本的App登录");
        dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 保存用户信息
     *
     * @param result
     */
    private void saveUserInfo(LoginResult result) {
        if (result == null) {
            return;
        }
        MobileUser user = result.getResponseData();
        if (user != null) {
            SharePreferenceHelper sharePreferenceHelper = new SharePreferenceHelper(context);
            Map<String, Object> map = new HashMap<>();
            map.put("avatar", user.getAvatar());
            map.put("id", user.getId());
            map.put("account", et_userName.getText().toString());
            if (appCheckBox.isChecked()) {
                map.put("password", et_passWord.getText().toString());
                map.put("firstLogin", false);
                map.put("remember", true);
            } else {
                map.put("password", null);
                map.put("firstLogin", true);
                map.put("remember", false);
            }
            if (user.getRealName() != null)
                map.put("realName", user.getRealName());
            else
                map.put("realName", user.getUserName());
            map.put("userName", user.getUserName());
            map.put("deptName", user.getDeptName());
            map.put("role", user.getRole());
            sharePreferenceHelper.saveSharePreference(map);
        }
    }
}
