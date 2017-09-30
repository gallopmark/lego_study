package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.List;

/**
 * 创建日期：2017/1/10 on 9:23
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchAdapter extends BaseArrayRecyclerAdapter<DiscussEntity> {
    private Context mContext;
    private RequestClickCallBack requestClickCallBack;

    public TeachingResearchAdapter(Context mContext, List<DiscussEntity> mDatas) {
        super(mDatas);
        this.mContext = mContext;
    }

    public void setRequestClickCallBack(RequestClickCallBack requestClickCallBack) {
        this.requestClickCallBack = requestClickCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final DiscussEntity entity, final int position) {
        ImageView iv_ico = holder.obtainView(R.id.iv_ico);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_createTime = holder.obtainView(R.id.tv_createTime);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        RecyclerView fileRV = holder.obtainView(R.id.fileRV);
        View rl_support = holder.obtainView(R.id.rl_support);
        final TextView tv_support = holder.obtainView(R.id.tv_support);
        View rl_comment = holder.obtainView(R.id.rl_comment);
        final TextView tv_commnet = holder.obtainView(R.id.tv_comment);
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, iv_ico);
        } else {
            iv_ico.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        tv_createTime.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_title.setText(entity.getTitle());
        if (entity.getContent() != null) {
            Spanned spanned = Html.fromHtml(entity.getContent());
            tv_content.setText(spanned);
        } else
            tv_content.setText(null);
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            tv_support.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getSupportNum()));
            tv_commnet.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getReplyNum()));
        } else {
            tv_support.setText(String.valueOf(0));
            tv_commnet.setText(String.valueOf(0));
        }
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(mContext);
        layoutManager.setOrientation(FullyLinearLayoutManager.HORIZONTAL);
        fileRV.setLayoutManager(layoutManager);
        if (entity.getmFileInfos() != null && entity.getmFileInfos().size() > 0) {
            FileListAdapter adapter = new FileListAdapter(entity.getmFileInfos());
            fileRV.setAdapter(adapter);
        } else {
            fileRV.setVisibility(View.GONE);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.rl_support:
                        if (requestClickCallBack != null)
                            requestClickCallBack.support(entity, position);
                        break;
                    case R.id.rl_comment:
                        if (requestClickCallBack != null)
                            requestClickCallBack.comment(entity, position);
                        break;
                }
            }
        };
        rl_support.setOnClickListener(listener);
        rl_comment.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_research_item;
    }

    private class FileListAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {
        private int imageWidth;

        public FileListAdapter(List<MFileInfo> mDatas) {
            super(mDatas);
            imageWidth = ScreenUtils.getScreenWidth(mContext) / 4;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MFileInfo mFileInfo, int position) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageWidth);
            ImageView iv_file = holder.obtainView(R.id.iv_file);
            iv_file.setLayoutParams(params);
            GlideImgManager.loadImage(mContext,mFileInfo.getUrl(),R.drawable.app_default,R.drawable.app_default,iv_file);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.teaching_research_file_item;
        }
    }

    public interface RequestClickCallBack {
        void support(DiscussEntity entity, int position);

        void comment(DiscussEntity entity, int position);
    }
}
