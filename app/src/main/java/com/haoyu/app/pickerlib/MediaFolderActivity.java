package com.haoyu.app.pickerlib;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.AppToolBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/6/16 on 10:54
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MediaFolderActivity extends BaseActivity {
    private MediaFolderActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.tv_error)
    TextView tv_error;
    private List<MediaFolder> mediaFolders = new ArrayList<>();
    private MediaFolderAdapter folderAdapter;
    private int selectType;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_pickerlib_imagefolder;
    }

    @Override
    public void initView() {
        selectType = MediaPicker.getInstance().getMediaOption().getSelectType();
        if (selectType == MediaOption.TYPE_VIDEO) {
            toolBar.setTitle_text("我的视频");
            tv_error.setText("加载视频失败~");
            tv_empty.setText("没有视频~");
        } else {
            toolBar.setTitle_text("我的相册");
            tv_error.setText("加载相册失败~");
            tv_empty.setText("没有相片~");
        }
        recyclerView.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.VERTICAL,
                PixelFormat.dp2px(context, 1), ContextCompat.getColor(context, R.color.spaceColor)));
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        folderAdapter = new MediaFolderAdapter(context, mediaFolders);
        recyclerView.setAdapter(folderAdapter);
    }

    @Override
    public void initData() {
        addSubscription(Flowable.just(context).map(new Function<MediaFolderActivity, List<MediaItem>>() {
            @Override
            public List<MediaItem> apply(MediaFolderActivity context) throws Exception {
                if (selectType == MediaOption.TYPE_VIDEO)
                    return MediaSource.getLatelyVideos(context);
                return MediaSource.getLatelyImages(context);
            }
        }).map(new Function<List<MediaItem>, List<MediaFolder>>() {
            @Override
            public List<MediaFolder> apply(List<MediaItem> mediaItems) throws Exception {
                if (mediaItems.size() > 0) {
                    MediaFolder imageFolder = new MediaFolder();
                    if (selectType == MediaOption.TYPE_VIDEO) {
                        imageFolder.setName("最近视频");
                    } else {
                        imageFolder.setName("最近照片");
                    }
                    imageFolder.setFirstImagePath(mediaItems.get(0).getPath());
                    imageFolder.setMediaItems(mediaItems);
                    mediaFolders.add(imageFolder);
                }
                if (selectType == MediaOption.TYPE_VIDEO)
                    return MediaSource.getVideoFolders(context);
                return MediaSource.getImageFolders(context);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<MediaFolder>>() {
            @Override
            public void accept(List<MediaFolder> mDatas) throws Exception {
                mediaFolders.addAll(mDatas);
                if (mediaFolders.size() == 0) {
                    tv_empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else
                    folderAdapter.notifyDataSetChanged();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                tv_error.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }));
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        folderAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, MediaGridActivity.class);
                intent.putExtra("mediaFolder", mediaFolders.get(position));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_bottom_out);
    }
}
