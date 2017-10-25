package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.compressor.Compressor;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.CropImageView;
import com.haoyu.app.pickerlib.GridSpacingItemDecoration;
import com.haoyu.app.pickerlib.MediaItem;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/1/10 on 13:45
 * 描述: 创建研说
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCreateSSActivity extends BaseActivity {
    private TeachingResearchCreateSSActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<MediaItem> icoItems = new ArrayList<>();
    private ArrayMap<Integer, String> resultMap = new ArrayMap<>();
    private GridAdapter adapter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_create_ss;
    }

    @Override
    public void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, PixelFormat.dp2px(context, 2), false));
        adapter = new GridAdapter(icoItems);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (title.length() == 0)
                    showMaterialDialog("提示", "请输入标题");
                else if (content.length() == 0)
                    showMaterialDialog("提示", "请输入描述内容");
                else {
                    if (icoItems.size() > 0)
                        uploadPhotos(title, content);
                    else
                        commit(title, content);
                }
            }
        });
        et_title.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = et_title.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > et_title.getWidth()
                        - et_title.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    et_title.setSelection(et_title.getText().length());//将光标移至文字末尾
                }
                return false;
            }
        });
    }

    private void pickerPicture() {
        if (icoItems.size() >= 9)
            toast(context, "您最多可选9张图");
        else {
            int limit = 9 - icoItems.size();
            MediaOption option = new MediaOption.Builder()
                    .setSelectType(MediaOption.TYPE_IMAGE)
                    .isMultiMode(true)
                    .setShowCamera(false)
                    .setSelectLimit(limit)
                    .setStyle(CropImageView.Style.CIRCLE)
                    .build();
            MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
                @Override
                public void onSelected(final List<MediaItem> mSelects) {
                    showTipDialog();
                    Flowable.fromArray(mSelects).map(new Function<List<MediaItem>, List<MediaItem>>() {
                        @Override
                        public List<MediaItem> apply(List<MediaItem> mSelects) {
                            List<MediaItem> items = new ArrayList<>();
                            for (int i = 0; i < mSelects.size(); i++) {
                                String filePath = mSelects.get(i).getPath();
                                File file = new File(filePath);
                                File target = new Compressor(context).setMaxWidth(400).setMaxHeight(320).setQuality(100)
                                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                        .setDestinationDirectoryPath(Constants.compressor)
                                        .compressToFile(file);
                                mSelects.get(i).setPath(target.getAbsolutePath());
                                items.add(mSelects.get(i));
                            }
                            return items;
                        }
                    }).subscribe(new Consumer<List<MediaItem>>() {
                        @Override
                        public void accept(List<MediaItem> mediaItems) throws Exception {
                            hideTipDialog();
                            icoItems.addAll(mediaItems);
                            if (icoItems.size() < 9)
                                adapter.setShowAdd(true);
                            else
                                adapter.setShowAdd(false);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    private void uploadPhotos(final String title, final String content) {
        final String url = Constants.OUTRT_NET + "/m/file/uploadFileUeditor";
        final FileUploadDialog uploadDialog = new FileUploadDialog(context, "", "图片上传");
        uploadDialog.setCancelable(false);
        uploadDialog.setCanceledOnTouchOutside(false);
        uploadDialog.show();
        final Disposable disposable = Flowable.fromCallable(new Callable<ArrayMap<Integer, String>>() {
            @Override
            public ArrayMap<Integer, String> call() throws Exception {
                return uploadPhotos(url, uploadDialog);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayMap<Integer, String>>() {
            @Override
            public void accept(ArrayMap<Integer, String> result) throws Exception {
                uploadDialog.dismiss();
                resultMap = result;
                commit(title, content);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                uploadDialog.dismiss();
            }
        });
        uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
                disposable.dispose();
            }
        });
    }

    private ArrayMap<Integer, String> uploadPhotos(String url, final FileUploadDialog uploadDialog) {
        ArrayMap<Integer, String> resultMap = new ArrayMap<>();
        for (int i = 0; i < icoItems.size(); i++) {
            final int position = i;
            try {
                String filePath = icoItems.get(position).getPath();
                final String fileName = Common.getFileName(filePath);
                File file = new File(filePath);
                String json = OkHttpClientManager.post(context, url, file, file.getName(), new OkHttpClientManager.ProgressListener() {
                    @Override
                    public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                        Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<long[]>() {
                                    @Override
                                    public void accept(long[] params) throws Exception {
                                        uploadDialog.setUploadProgressBar(params[0], params[1]);
                                        uploadDialog.setUploadText(params[0], params[1]);
                                        uploadDialog.setFileName(fileName);
                                    }
                                });
                    }
                });
                BaseResponseResult<String> result = new Gson().fromJson(json, BaseResponseResult.class);
                if (result != null && result.getResponseData() != null) {
                    resultMap.put(position, result.getResponseData());
                }
            } catch (Exception e) {
                continue;
            }
        }
        return resultMap;
    }

    /*创建研说*/
    private void commit(String title, final String content) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/discussion/cmts";
        Map<String, String> map = new HashMap<>();
        map.put("discussionRelations[0].relation.id", "cmts");
        map.put("discussionRelations[0].relation.type", "discussion");
        StringBuilder sb = new StringBuilder();
        sb.append("<p>" + content + "</p>");
        if (resultMap.size() > 0) {
            for (String src : resultMap.values()) {
                sb.append(src);
            }
        }
        map.put("title", title);
        map.put("content", sb.toString());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<DiscussResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(DiscussResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    DiscussEntity entity = response.getResponseData();
                    if (entity.getCreator() == null) {
                        MobileUser creator = new MobileUser();
                        creator.setId(getUserId());
                        creator.setAvatar(getAvatar());
                        creator.setRealName(getRealName());
                        entity.setCreator(creator);
                    } else {
                        if (entity.getCreator().getId() == null || (entity.getCreator().getId() != null && entity.getCreator().getId().toLowerCase().equals("null")))
                            entity.getCreator().setId(getUserId());
                        if (entity.getCreator().getAvatar() == null || (entity.getCreator().getAvatar() != null && entity.getCreator().getAvatar().toLowerCase().equals("null")))
                            entity.getCreator().setAvatar(getAvatar());
                        if (entity.getCreator().getRealName() == null || (entity.getCreator().getRealName() != null && entity.getCreator().getRealName().toLowerCase().equals("null")))
                            entity.getCreator().setRealName(getRealName());
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_STUDY_SAYS;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("发表成功", true);
                    finish();
                } else {
                    onNetWorkError(context);
                }
            }
        }, map));
    }

    private class GridAdapter extends BaseArrayRecyclerAdapter<MediaItem> {
        private int imageSize;
        private int TYPE_ITEM = 1, TYPE_ADD = 2;
        private boolean showAdd = true;

        public GridAdapter(List<MediaItem> mDatas) {
            super(mDatas);
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
            int cols = screenWidth / densityDpi;
            cols = cols < 3 ? 3 : cols;
            int columnSpace = (int) (4 * context.getResources().getDisplayMetrics().density);
            this.imageSize = (screenWidth - columnSpace * (cols - 1)) / cols;
        }

        public void setShowAdd(boolean showAdd) {
            this.showAdd = showAdd;
        }

        @Override
        public int getItemCount() {
            if (showAdd)
                return mDatas.size() + 1;
            return mDatas.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (showAdd && position == mDatas.size())
                return TYPE_ADD;
            return TYPE_ITEM;

        }

        @Override
        public int bindView(int viewtype) {
            if (viewtype == TYPE_ITEM)
                return R.layout.grid_image_item;
            else
                return R.layout.grid_image_add;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MediaItem item, final int position) {
            if (holder.getItemViewType() == TYPE_ITEM) {
                holder.itemView.setLayoutParams(new FrameLayout.LayoutParams(imageSize, imageSize));
                final String path = item.getPath();
                ImageView image = holder.obtainView(R.id.image);
                ImageView iv_delete = holder.obtainView(R.id.iv_delete);
                GlideImgManager.loadImage(context, path, R.drawable.app_default, R.drawable.app_default, image);
                iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatas.remove(position);
                        if (mDatas.size() < 9)
                            setShowAdd(true);
                        else
                            setShowAdd(false);
                        notifyDataSetChanged();
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> imgList = new ArrayList<>();
                        for (int i = 0; i < mDatas.size(); i++)
                            imgList.add(mDatas.get(i).getPath());
                        Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                        intent.putStringArrayListExtra("photos", imgList);
                        intent.putExtra("position", position);
                        context.startActivity(intent);
                        context.overridePendingTransition(R.anim.zoom_in, 0);
                    }
                });
            } else {
                holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pickerPicture();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        File file = new File(Constants.compressor);
        if (file.exists())
            file.delete();
        super.onDestroy();
    }
}
