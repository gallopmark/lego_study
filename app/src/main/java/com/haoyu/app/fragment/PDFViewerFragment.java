package com.haoyu.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.haoyu.app.lego.student.R;

import java.io.File;

/**
 * 创建日期：2017/11/16.
 * 描述:pdf文件查看
 * 作者:xiaoma
 */

public class PDFViewerFragment extends Fragment {

    private Activity context;
    private String filePath;
    private PDFView pdfView;
    private boolean isRead;
    private AlertDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
        Bundle bundle = getArguments();
        filePath = bundle.getString("filePath");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdfviewer, container, false);
        pdfView = view.findViewById(R.id.pdfView);
        openPdfFile();
        return view;
    }

    private void openPdfFile() {
        pdfView.setVisibility(View.VISIBLE);
        pdfView.fromFile(new File(filePath))
                .swipeHorizontal(true)
                .defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(false)
                .scrollHandle(new DefaultScrollHandle(context))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (!isRead) {
                            showGestureDialog();
                        }
                    }
                })
                .load();
    }

    private void showGestureDialog() {
        dialog = new AlertDialog.Builder(context, R.style.GestureDialog).create();
        dialog.show();
        View view = View.inflate(context, R.layout.dialog_gesture_tips, null);
        TextView tv_tips = view.findViewById(R.id.tv_tips);
        ImageView iv_center = view.findViewById(R.id.iv_center);
        tv_tips.setText("手势可放大缩小");
        iv_center.setImageResource(R.drawable.gesture_big);
        view.findViewById(R.id.bt_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRead = true;
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isRead = true;
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setContentView(view, params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pdfView.recycle();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
