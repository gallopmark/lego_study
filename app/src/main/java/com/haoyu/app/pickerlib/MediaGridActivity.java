package com.haoyu.app.pickerlib;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.activity.AppMultiImageShowActivity;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/6/16 on 14:17
 * 描述:相册或视频
 * 作者:马飞奔 Administrator
 */
public class MediaGridActivity extends BaseActivity {
    private MediaGridActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.contentView)
    FrameLayout contentView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.bottomLayout)
    RelativeLayout bottomLayout;
    @BindView(R.id.rl_tips)
    RelativeLayout rl_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.bt_settings)
    Button bt_settings;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_STORAGE = 2;
    private boolean initMedia;
    private int TAKE_PHOTO = 3;
    private int TAKE_VIDEO = 4;
    private boolean isCrop, multiMode;
    private int selectType;
    private String cameraPath;

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Common.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) && !initMedia) {
            rl_tips.setVisibility(View.GONE);
            initMedia();
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_pickerlib_imagegrid;
    }

    @Override
    public void initView() {
        MediaOption option = MediaPicker.getInstance().getMediaOption();
        isCrop = option.isCrop();
        multiMode = option.isMultiMode();
        selectType = option.getSelectType();
        if (selectType == MediaOption.TYPE_VIDEO) {
            toolBar.setTitle_text(getResources().getString(R.string.myVideos));
        } else {
            toolBar.setTitle_text(getResources().getString(R.string.myPhotos));
        }
        if (!Common.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {   //如果没有申请sd卡权限
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            initMedia();
        }
    }

    private void initMedia() {
        contentView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, PixelFormat.dp2px(context, 2), false));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        MediaFolder mediaFolder = (MediaFolder) getIntent().getSerializableExtra("mediaFolder");
        if (mediaFolder == null) {
            getLatelyMedia();
        } else {
            toolBar.setTitle_text(mediaFolder.getName());
            boolean showCamera;
            if (mediaFolder.getPath() == null)
                showCamera = true;
            else
                showCamera = false;
            MediaGridAdapter adapter = new MediaGridAdapter(context, mediaFolder.getMediaItems(), showCamera);
            recyclerView.setAdapter(adapter);
            setAdapter(adapter);
        }
        initMedia = true;
    }

    private void getLatelyMedia() {
        if (selectType == MediaOption.TYPE_VIDEO) {
            toolBar.setTitle_text(getResources().getString(R.string.recentVideos));
        } else {
            toolBar.setTitle_text(getResources().getString(R.string.recentPhotos));
        }
        Flowable.just(context).map(new Function<MediaGridActivity, List<MediaItem>>() {
            @Override
            public List<MediaItem> apply(MediaGridActivity mediaGridActivity) throws Exception {
                if (selectType == MediaOption.TYPE_VIDEO) {
                    return MediaSource.getLatelyVideos(context);
                }
                return MediaSource.getLatelyImages(context);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<MediaItem>>() {
            @Override
            public void accept(List<MediaItem> mediaItems) throws Exception {
                MediaGridAdapter adapter = new MediaGridAdapter(context, mediaItems, true);
                recyclerView.setAdapter(adapter);
                setAdapter(adapter);
            }
        });
    }

    private void setAdapter(final MediaGridAdapter mAdapter) {
        mAdapter.setOnItemClickListener(new MediaGridAdapter.OnItemClickListener() {
            @Override
            public void onCamera() {
                if (!Common.checkPermission(context, Manifest.permission.CAMERA)) {
                    //第一请求权限被取消显示的判断，一般可以不写
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                } else {
                    if (selectType == MediaOption.TYPE_IMAGE) {
                        takePhoto();
                    } else {
                        takeVideo();
                    }
                }
            }

            @Override
            public void onSingleChoice(MediaItem item) {
                List<MediaItem> mSelects = new ArrayList<>();
                mSelects.add(item);
                preViewPhoto(mSelects);
            }

            @Override
            public void onOverChoice(int limit) {
                toast(context, "您最多可选择" + limit + "张图");
            }

            @Override
            public void onMultipleChoice(final List<MediaItem> mSelects) {
                preViewPhoto(mSelects);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMedia();
                } else {
                    rl_tips.setVisibility(View.VISIBLE);
                    tv_tips.setText("存储权限已被禁止，请重新打开存储权限！");
                }
                break;
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (selectType == MediaOption.TYPE_VIDEO) {
                        takeVideo();
                    } else {
                        takePhoto();
                    }
                } else {
                    tipsDialog();
                }
                break;
        }
    }

    private void tipsDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("照相机");
        dialog.setMessage("相机权限已被禁止，无法完成拍照，请重新打开相机权限。如若需要，请点击【确定】");
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.blow_gray));
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                Common.openSettings(context);
            }
        });
        dialog.show();
    }

    private void takePhoto() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File takeImageFile = new File(Constants.mediaCache);
                takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
                cameraPath = takeImageFile.getAbsolutePath();
                Uri imageUri;
                String authority = getPackageName() + ".provider";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    imageUri = FileProvider.getUriForFile(context, authority, takeImageFile);//通过FileProvider创建一个content类型的Uri
                } else {
                    imageUri = Uri.fromFile(takeImageFile);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        } catch (Exception e) {
            toastFullScreen("打开相机失败，请稍后再试！", false);
        }
    }

    private void takeVideo() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File takeVideoFile = new File(Constants.mediaCache);
                takeVideoFile = createFile(takeVideoFile, "VIDEO_", ".mp4");
                cameraPath = takeVideoFile.getAbsolutePath();
                Uri imageUri;
                String authority = getPackageName() + ".provider";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    imageUri = FileProvider.getUriForFile(context, authority, takeVideoFile);//通过FileProvider创建一个content类型的Uri
                } else {
                    imageUri = Uri.fromFile(takeVideoFile);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024 * 1024);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(intent, TAKE_VIDEO);
            }
        } catch (Exception e) {
            toastFullScreen("打开相机失败，请稍后再试！", false);
        }
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    private void preViewPhoto(final List<MediaItem> mSelects) {
        if (mSelects.size() > 0)
            bottomLayout.setVisibility(View.VISIBLE);
        else
            bottomLayout.setVisibility(View.GONE);
        TextView tv_preView = getView(bottomLayout, R.id.tv_preView);
        TextView tv_selected = getView(bottomLayout, R.id.tv_selected);
        if (selectType == MediaOption.TYPE_VIDEO) {    //如果是选择视频则隐藏预览按钮
            tv_preView.setVisibility(View.GONE);
        }
        tv_preView.setText(getResources().getString(R.string.preView) + "(" + mSelects.size() + ")");
        tv_selected.setText(getResources().getString(R.string.finish) + "(" + mSelects.size() + ")");
        tv_preView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                ArrayList<String> imgList = new ArrayList<>();
                for (MediaItem item : mSelects) {
                    imgList.add(item.getPath());
                }
                intent.putStringArrayListExtra("photos", imgList);
                startActivity(intent);
            }
        });
        tv_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!multiMode && isCrop) {
                    Intent intent = new Intent(context, ImageCropActivity.class);
                    intent.putExtra("imagePath", mSelects.get(0).getPath());
                    startActivityForResult(intent, MediaOption.REQUEST_CODE_CROP);
                } else {
                    if (MediaPicker.getInstance().getSelectMediaCallBack() != null) {
                        if (multiMode) {
                            MediaPicker.getInstance().getSelectMediaCallBack().onSelected(mSelects);
                        } else {
                            MediaPicker.getInstance().getSelectMediaCallBack().onSelected(mSelects.get(0).getPath());
                        }
                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
            if (isCrop) {
                Intent intent = new Intent(context, ImageCropActivity.class);
                intent.putExtra("imagePath", cameraPath);
                startActivityForResult(intent, MediaOption.REQUEST_CODE_CROP);
            } else {
                if (MediaPicker.getInstance().getSelectMediaCallBack() != null) {
                    if (!multiMode)
                        MediaPicker.getInstance().getSelectMediaCallBack().onSelected(cameraPath);
                    else {
                        List<MediaItem> mSelects = new ArrayList<>();
                        MediaItem item = new MediaItem();
                        item.setPath(cameraPath);
                        mSelects.add(item);
                        MediaPicker.getInstance().getSelectMediaCallBack().onSelected(mSelects);
                    }
                }
                finish();
            }
        } else if (resultCode == RESULT_OK && requestCode == TAKE_VIDEO) {
            if (MediaPicker.getInstance().getSelectMediaCallBack() != null) {
                MediaPicker.getInstance().getSelectMediaCallBack().onSelected(cameraPath);
            }
            finish();
        } else if (resultCode == RESULT_OK && requestCode == MediaOption.REQUEST_CODE_CROP && data != null) {
            String path = data.getStringExtra(MediaOption.EXTRA_IMAGE_CROP);
            if (MediaPicker.getInstance().getSelectMediaCallBack() != null) {
                MediaPicker.getInstance().getSelectMediaCallBack().onSelected(path);
            }
            finish();
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                if (Common.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    startActivity(new Intent(context, MediaFolderActivity.class));
                    overridePendingTransition(R.anim.fade_in, 0);
                }
                finish();
            }

            @Override
            public void onRightClick(View view) {
                finish();
            }
        });
        bt_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.openSettings(context);
            }
        });
        bottomLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibile) {
                if (visibile == View.VISIBLE) {
                    recyclerView.setClipToPadding(false);
                    recyclerView.setPadding(0, 0, 0, bottomLayout.getHeight());
                } else {
                    recyclerView.setClipToPadding(true);
                    recyclerView.setPadding(0, 0, 0, 0);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }
}
