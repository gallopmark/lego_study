package com.haoyu.app.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haoyu.app.dialog.PublicTipDialog;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.AppToast;
import com.haoyu.app.utils.Constants;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class BaseFragment extends Fragment {
    public Context context;
    private PublicTipDialog dialog;
    protected Unbinder unbinder;
    public CompositeDisposable rxSubscriptions = new CompositeDisposable();
    private Disposable rxBusable;
    private Toast mToast, fullToast;
    private SharedPreferences preferences;

    public abstract int createView();

    public void initData() {
    }

    public void initView(View view) {
    }

    public void registRxBus() {
        rxBusable = RxBus.getDefault().toObservable(MessageEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MessageEvent>() {
            @Override
            public void accept(MessageEvent event) throws Exception {
                onEvent(event);
            }
        });
    }

    public void unRegistRxBus() {
        if (rxBusable != null && !rxBusable.isDisposed()) {
            rxBusable.dispose();
        }
    }

    public void onEvent(MessageEvent event) {

    }

    public void addSubscription(Disposable d) {
        if (d != null) {
            rxSubscriptions.add(d);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        preferences = context.getSharedPreferences(Constants.Prefs_user, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(createView(), container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initView(rootView);
        setListener();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    public void onNetWorkError() {
        toast(getResources().getString(R.string.network_error));
    }

    public void setListener() {
    }

    public void toast(String text) {
        View v = LayoutInflater.from(context).inflate(R.layout.app_layout_toast, null);
        TextView textView = v.findViewById(R.id.tv_text);
        textView.setText(text);
        if (mToast == null) {
            mToast = new AppToast(context, R.style.AppToast);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setView(v);
        } else {
            mToast.setView(v);
        }
        mToast.show();
    }

    public void toastFullScreen(String content, boolean success) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.toast_publish_question, null);
        ImageView iv_result = view.findViewById(R.id.iv_result);
        TextView tv_result = view.findViewById(R.id.tv_result);
        if (success)
            iv_result.setImageResource(R.drawable.publish_success);
        else
            iv_result.setImageResource(R.drawable.publish_failure);
        tv_result.setText(content);
        if (fullToast == null) {//只有mToast==null时才重新创建，否则只需更改提示文字
            fullToast = new Toast(context);
            fullToast.setDuration(Toast.LENGTH_LONG);
            fullToast.setGravity(Gravity.FILL, 0, 0);
            fullToast.setView(view);
        } else {
            fullToast.setView(view);
        }
        fullToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        cancelToast();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegistRxBus();
        rxSubscriptions.dispose();
    }

    public String getUserName() {
        return preferences.getString("userName", "");
    }

    public String getAvatar() {
        return preferences.getString("avatar", "");
    }

    public String getUserId() {
        return preferences.getString("id", "");
    }

    public String getRealName() {
        return preferences.getString("realName", "");
    }

    public String getDeptName() {
        return preferences.getString("deptName", "");
    }


    public void showTipDialog() {
        dialog = new PublicTipDialog(context);
        dialog.show();
    }

    public void hideTipDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public <T extends View> T getView(View rootView, int id) {
        return (T) rootView.findViewById(id);
    }
}
