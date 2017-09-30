package com.haoyu.app.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * Created by acer1 on 2017/1/10.
 * 教务咨询
 */
public class EducationConsultActivity extends BaseActivity {
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.phone_number)
    TextView mPhoneNumber;
    private int REQUEST_CODE = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_educational_consulting;
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        mPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPhone();
            }
        });
    }

    private void callPhone() {
        if (hasCallPhone()) {
            call();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
        }
    }

    private boolean hasCallPhone() {
        //判断是否6.0以上的手机   不是就不用
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private void call() {
        try {
            String phone = mPhoneNumber.getText().toString().trim();
            phone = phone.replaceAll("-", "");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            call();
        }
    }
}
