package com.haoyu.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.haoyu.app.lego.student.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 鉴于经常用到获取验证码倒计时按钮 在网上也没有找到理想的 自己写一个
 *
 *
 * @author yung
 *         <P>
 *         2015年1月14日[佛祖保佑 永无BUG]
 *         <p>
 *         PS: 由于发现timer每次cancle()之后不能重新schedule方法,所以计时完毕只恐timer.
 *         每次开始计时的时候重新设置timer, 没想到好办法初次下策
 *         注意把该类的onCreate()onDestroy()和activity的onCreate()onDestroy()同步处理
 *
 */
public class TimeButton extends Button implements OnClickListener {
	private long lenght = 60 * 1000;// 倒计时长度,这里给了默认60秒
	private String textafter = "秒后重新获取";
	private String textbefore = "获取验证码";
	private final String TIME = "time";
	private final String CTIME = "ctime";
	private OnClickListener mOnclickListener;
	private Timer timer;
	private TimerTask timerTask;
	private long time;
	private Context context;

	Map<String, Long> map = new HashMap<String, Long>();

	public TimeButton(Context context) {
		super(context);
		this.context = context;
		setOnClickListener(this);

	}

	public TimeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setOnClickListener(this);
	}

	@SuppressLint("HandlerLeak")
	Handler hanler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			TimeButton.this.setText(time / 1000 + textafter);
			time -= 1000;
			if (time < 0) {
				TimeButton.this.setEnabled(true);
				TimeButton.this.setText(textbefore);
				TimeButton.this.setTextColor(ContextCompat.getColor(context,
						R.color.defaultColor));
				clearTimer();
			}
		}
	};

	private void initTimer() {
		time = lenght;
		timer = new Timer();
		timerTask = new TimerTask() {

			@Override
			public void run() {
				hanler.sendEmptyMessage(0x01);
			}
		};
	}

	private void clearTimer() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		if (timer != null)
			timer.cancel();
		timer = null;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		if (l instanceof TimeButton) {
			super.setOnClickListener(l);
		} else
			this.mOnclickListener = l;
	}

	@Override
	public void onClick(View v) {
		if (mOnclickListener != null)
			mOnclickListener.onClick(v);
		initTimer();
		this.setText(time / 1000 + textafter);
		this.setEnabled(false);
		this.setTextColor(ContextCompat.getColor(context, R.color.gray));
		timer.schedule(timerTask, 0, 1000);
		// t.scheduleAtFixedRate(task, delay, period);
	}

	/**
	 * 和activity的onDestroy()方法同步
	 */
	public void onDestroy() {
		if (map == null)
			map = new HashMap<String, Long>();
		map.put(TIME, time);
		map.put(CTIME, System.currentTimeMillis());
		clearTimer();
	}

	/**
	 * 和activity的onCreate()方法同步
	 */
	public void onCreate(Bundle bundle) {
		if (map == null)
			return;
		if (map.size() <= 0)// 这里表示没有上次未完成的计时
			return;
		long time = System.currentTimeMillis() - map.get(CTIME) - map.get(TIME);
		map.clear();
		if (time > 0)
			return;
		else {
			initTimer();
			this.time = Math.abs(time);
			timer.schedule(timerTask, 0, 1000);
			this.setText(time + textafter);
			this.setEnabled(false);
		}
	}

	/** * 设置计时时候显示的文本 */
	public TimeButton setTextAfter(String text1) {
		this.textafter = text1;
		return this;
	}

	/** * 设置点击之前的文本 */
	public TimeButton setTextBefore(String text0) {
		this.textbefore = text0;
		this.setText(textbefore);
		return this;
	}

	/**
	 * 设置到计时长度
	 *
	 * @param lenght
	 *            时间 默认毫秒
	 * @return
	 */
	public TimeButton setLenght(long lenght) {
		this.lenght = lenght;
		return this;
	}
	/*

*
*/
}