package tv.danmaku.ijk.media.player.widget;

import android.view.View;
import android.widget.MediaController;

/**
 * 创建日期：2018/1/10.
 * 描述:媒体控制器
 * 作者:xiaoma
 */

public interface IMediaController {
    /**
     * 隐藏标题栏
     */
    void hide();

    /**
     * 判断是否显示
     */
    boolean isShowing();

    /**
     * 设置主播界面
     */
    void setAnchorView(View view);

    /**
     * 设置是否可用
     */
    void setEnabled(boolean enabled);

    /**
     * 设置媒体播放器
     */
    void setMediaPlayer(MediaController.MediaPlayerControl player);

    /**
     * 显示带超时时间
     */
    void show(int timeout);

    /**
     * 显示标题栏
     */
    void show();

    //----------
    // Extends
    //----------
    void showOnce(View view);
}
