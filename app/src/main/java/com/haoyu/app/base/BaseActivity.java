package com.haoyu.app.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.haoyu.app.dialog.LoadingDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.dialog.PublicTipDialog;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.AppToast;
import com.haoyu.app.utils.Constants;

import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class BaseActivity extends FragmentActivity {

    private BaseActivity context = this;
    private PublicTipDialog publicTipDialog;
    private LoadingDialog loadingDialog;
    public String student = "student";
    public String manager = "manager";
    public String teacher = "teacher";
    public String RESULT_INFO = "paths";
    public CompositeDisposable rxSubscriptions = new CompositeDisposable();
    private Disposable rxBusable;
    private Toast mToast;

    public void controlKeyboardLayout(final ScrollView scrollView,
                                      final View view) {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        scrollView.getWindowVisibleDisplayFrame(rect);
                        if (scrollView.getRootView().getHeight()
                                - rect.bottom > 100) {
                            int[] arrayOfInt = new int[2];
                            view.getLocationInWindow(arrayOfInt);
                            int i = arrayOfInt[1] + view.getHeight()
                                    - rect.bottom;
                            scrollView.scrollTo(0, i);
                            return;
                        }
                        scrollView.scrollTo(0, 0);
                    }
                });
    }

    /**
     * activity.findViewById()
     *
     * @param context
     * @param id
     * @return
     */
    public <T extends View> T getView(Activity context, int id) {
        return (T) context.findViewById(id);
    }

    /**
     * rootView.findViewById()
     *
     * @param rootView
     * @param id
     * @return
     */
    public <T extends View> T getView(View rootView, int id) {
        return (T) rootView.findViewById(id);
    }

    public void initView() {
    }

    public void initData() {
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ExitApplication.getInstance().addActivity(this);
        setContentView(setLayoutResID());
        ButterKnife.bind(this);
        initView();
        initData();
        setListener();
    }

    public abstract int setLayoutResID();

    public void registRxBus() {
        rxBusable = RxBus.getDefault().toObservable(MessageEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MessageEvent>() {
            @Override
            public void accept(MessageEvent event) throws Exception {
                obBusEvent(event);
            }
        });
    }


    public void unRegistRxBus() {
        if (rxBusable != null && !rxBusable.isDisposed()) {
            rxBusable.dispose();
        }
    }

    public void addSubscription(Disposable d) {
        if (d != null) {
            rxSubscriptions.add(d);
        }
    }

    public void removeSubscription(Disposable d) {
        if (d != null) {
            rxSubscriptions.remove(d);
        }
    }

    public void obBusEvent(MessageEvent event) {

    }

    public void unsubscribe() {
        rxSubscriptions.dispose();
    }

    public void onNetWorkError(Context context) {
        toast(context, getResources().getString(R.string.network_error));
    }

    public void setListener() {
    }

    public void toast(Context context, String text) {
        View v = LayoutInflater.from(context).inflate(R.layout.app_layout_toast, null);
        TextView textView = v.findViewById(R.id.tv_text);
        textView.setText(text);
        if (mToast == null) {
            mToast = new AppToast(context, R.style.AppToast);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setView(v);
        } else
            mToast.setView(v);
        mToast.show();
    }

    public void toastFullScreen(String content, boolean success) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast_publish_question, null);
        ImageView iv_result = view.findViewById(R.id.iv_result);
        TextView tv_result = view.findViewById(R.id.tv_result);
        if (success)
            iv_result.setImageResource(R.drawable.publish_success);
        else
            iv_result.setImageResource(R.drawable.publish_failure);
        tv_result.setText(content);
        if (mToast == null) {//只有mToast==null时才重新创建，否则只需更改提示文字
            mToast = new Toast(context);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.FILL, 0, 0);
            mToast.setView(view);
        } else
            mToast.setView(view);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null)
            mToast.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelToast();
        unRegistRxBus();
        unsubscribe();
        ExitApplication.getInstance().remove(this);
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(Constants.Prefs_user, Context.MODE_PRIVATE);
    }

    public String getUserName() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("userName", "");
        return "";
    }

    public String getAvatar() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("avatar", "");
        return "";
    }

    public String getUserId() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("id", "");
        return "";
    }

    public String getRealName() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("realName", "");
        return "";
    }

    public String getDeptName() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("deptName", "");
        return "";
    }

    public String getRole() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("role", "");
        return "";
    }

    public String getAccount() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("account", "");
        return "";
    }

    public String getPassWord() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("password", "");
        return "";
    }

    public String getStage() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("stage", "");
        return "";
    }

    public String getSubject() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getString("subject", "");
        return "";
    }

    public boolean firstLogin() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getBoolean("firstLogin", true);
        return true;
    }

    public boolean isRemember() {
        SharedPreferences preferences = getPreferences();
        if (preferences != null)
            return preferences.getBoolean("remember", false);
        return false;
    }

    public void showTipDialog() {
        hideTipDialog();
        publicTipDialog = new PublicTipDialog(context);
        publicTipDialog.show();
    }

    public void hideTipDialog() {
        if (publicTipDialog != null) {
            publicTipDialog.dismiss();
            publicTipDialog = null;
        }
    }

    public void showLoadingDialog(String loadingText) {
        hideLoadingDialog();
        loadingDialog = new LoadingDialog(context, loadingText);
        loadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public void showMaterialDialog(String title, String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("我知道了", null);
        dialog.show();
    }
}
