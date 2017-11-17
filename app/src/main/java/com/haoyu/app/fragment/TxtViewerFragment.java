package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.dialog.PublicTipDialog;
import com.haoyu.app.utils.PixelFormat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/11/16.
 * 描述:txt文本查看
 * 作者:xiaoma
 */

public class TxtViewerFragment extends Fragment {
    private Context context;
    private String filePath;
    private PublicTipDialog dialog;
    private Disposable disposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        filePath = bundle.getString("filePath");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        int dp_12 = PixelFormat.dp2px(context, 12);
        tv.setPadding(dp_12, dp_12, dp_12, dp_12);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        openTxtFile(tv);
        return tv;
    }

    private void openTxtFile(final TextView tv_txt) {
        showTipDialog();
        disposable = Flowable.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                File file = new File(filePath);
                BufferedReader reader = null;
                String text = "";
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream in = new BufferedInputStream(fis);
                    in.mark(4);
                    byte[] first3bytes = new byte[3];
                    in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
                    in.reset();
                    if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                            && first3bytes[2] == (byte) 0xBF) {// utf-8
                        reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    } else if (first3bytes[0] == (byte) 0xFF
                            && first3bytes[1] == (byte) 0xFE) {
                        reader = new BufferedReader(
                                new InputStreamReader(in, "unicode"));
                    } else if (first3bytes[0] == (byte) 0xFE
                            && first3bytes[1] == (byte) 0xFF) {
                        reader = new BufferedReader(new InputStreamReader(in,
                                "utf-16be"));
                    } else if (first3bytes[0] == (byte) 0xFF
                            && first3bytes[1] == (byte) 0xFF) {
                        reader = new BufferedReader(new InputStreamReader(in,
                                "utf-16le"));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                    }
                    while (reader.readLine() != null) {
                        text += reader.readLine() + "\n";
                    }
                    reader.close();
                    fis.close();
                    in.close();
                } catch (Exception e) {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e1) {
                    }
                }
                return text;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String content) throws Exception {
                hideTipDialog();
                tv_txt.setText(content);
                tv_txt.setMovementMethod(ScrollingMovementMethod.getInstance());
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                tv_txt.setGravity(Gravity.CENTER);
                tv_txt.setText("无法预览此文件");
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
