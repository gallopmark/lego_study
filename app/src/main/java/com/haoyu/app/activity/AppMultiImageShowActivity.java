package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.view.TouchImageView;

import java.util.List;

import butterknife.BindView;

/**
 * 创建日期：2017/2/23 on 9:53
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppMultiImageShowActivity extends BaseActivity {
    private AppMultiImageShowActivity context = this;
    @BindView(R.id.show_img_viewPager)
    ViewPager viewPager;
    private List<String> imgList;
    @BindView(R.id.showimg_text)
    TextView numText;

    /**
     * 记录当前页卡
     */
    private int current = 0;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_show_image;
    }

    @Override
    public void initView() {
        // 图片地址
        imgList = getIntent().getStringArrayListExtra("photos");
        current = getIntent().getIntExtra("position", 0);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new ImagePageAdapter());
        //设置当前选中项
        viewPager.setCurrentItem(current, true);
        numText.setText(current + 1 + "/" + imgList.size());
    }

    @Override
    public void setListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                current = arg0;
                numText.setText(arg0 + 1 + "/" + imgList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class ImagePageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.app_multi_image_item, null);
            TouchImageView imageView = view.findViewById(R.id.touchImageView);
            ImageView iv_videoType = view.findViewById(R.id.iv_videoType);
            final String path = imgList.get(position);
            GlideImgManager.loadImage(context,path,R.drawable.ic_placeholder,R.drawable.ic_placeholder,imageView);
            if (path != null && path.length() > 0 && MediaFile.isVideoFileType(path)) {
                iv_videoType.setVisibility(View.VISIBLE);
            } else {
                iv_videoType.setVisibility(View.GONE);
            }
            iv_videoType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("videoUrl", path);
                    intent.putExtra("fileName", Common.getFileName(path));
                    startActivity(intent);
                }
            });
            container.addView(view);
            setListener(imageView);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return imgList.size();
        }
    }

    private void setListener(TouchImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }
}
