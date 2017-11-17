package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.PixelFormat;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

/**
 * 创建日期：2017/11/16.
 * 描述:课程学习教学课件文本编辑器
 * 作者:xiaoma
 */

public class CoursewareEditorFragment extends Fragment {
    private Context context;
    private String editor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        editor = bundle.getString("editor");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        int dp_12 = PixelFormat.dp2px(context, 12);
        tv.setPadding(dp_12, dp_12, dp_12, dp_12);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        openEditor(tv);
        return tv;
    }

    private void openEditor(TextView tv) {
        Html.ImageGetter imageGetter = new HtmlHttpImageGetter(tv, Constants.REFERER, true);
        if (editor != null) {
            Spanned spanned = Html.fromHtml(editor, imageGetter, null);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(spanned);
        } else {
            tv.setText(null);
        }
    }
}
