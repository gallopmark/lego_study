package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.utils.PixelFormat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 创建日期：2017/11/16.
 * 描述:txt文本查看
 * 作者:xiaoma
 */

public class TxtViewerFragment extends Fragment {
    private Context context;
    private String filePath;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        filePath = bundle.getString("filePath");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        int dp_12 = PixelFormat.dp2px(context, 12);
        tv.setPadding(dp_12, dp_12, dp_12, dp_12);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        openTxtFile(tv);
        return tv;
    }

    private void openTxtFile(TextView tv_txt) {
        File file = new File(filePath);
        BufferedReader reader;
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
            String str = reader.readLine();
            while (str != null) {
                text = text + str + "\n";
                str = reader.readLine();
            }
            reader.close();
            tv_txt.setVisibility(View.VISIBLE);
            tv_txt.setText(text);
            tv_txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        } catch (Exception e) {
            tv_txt.setGravity(Gravity.CENTER);
            tv_txt.setText("无法预览此文件");
        }
    }
}
