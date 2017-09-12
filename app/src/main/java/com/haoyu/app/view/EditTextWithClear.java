package com.haoyu.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 带删除按钮的edittext，并且屏幕表情输入
 * 
 * @author xiaoma
 *
 */
public class EditTextWithClear extends EditText {
	private Drawable drawableRight;
	private boolean showClearIcon;
	// 输入表情前的光标位置
	private int cursorPos;
	// 输入表情前EditText中的文本
	private String inputAfterText;
	// 是否重置了EditText的内容
	private boolean resetText;

	private Context mContext;

	public EditTextWithClear(Context context) {
		super(context);
		this.mContext = context;
		initEditText();
	}

	public EditTextWithClear(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initEditText();
	}

	public EditTextWithClear(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.mContext = context;
		initEditText();
	}

	// 初始化edittext 控件
	private void initEditText() {
		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int before, int count) {
				if (!resetText) {
					cursorPos = getSelectionEnd();
					// 这里用s.toString()而不直接用s是因为如果用s，
					// 那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
					// inputAfterText也就改变了，那么表情过滤就失败了
					inputAfterText = s.toString();
				}

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!resetText) {
					if (count >= 2) {// 表情符号的字符长度最小为2
						CharSequence input = s.subSequence(cursorPos, cursorPos
								+ count);
						if (containsEmoji(input.toString())) {
							resetText = true;
							Toast.makeText(mContext, "不支持输入Emoji表情符号",
									Toast.LENGTH_SHORT).show();
							// 是表情符号就将文本还原为输入表情符号之前的内容
							setText(inputAfterText);
							CharSequence text = getText();
							if (text instanceof Spannable) {
								Spannable spanText = (Spannable) text;
								Selection.setSelection(spanText, text.length());
							}
						}
					}
				} else {
					resetText = false;
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	/**
	 * 检测是否有emoji表情
	 *
	 * @param source
	 * @return
	 */
	public static boolean containsEmoji(String source) {
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);
			if (!isEmojiCharacter(codePoint)) { // 如果不能匹配,则该字符是Emoji表情
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否是Emoji
	 *
	 * @param codePoint
	 *            比较的单个字符
	 * @return
	 */
	private static boolean isEmojiCharacter(char codePoint) {
		return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
				|| (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
				|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
				|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}

	protected void onDraw(Canvas canvas) {
		if ((showClearIcon) && (drawableRight != null)) {
			float f1 = getHeight() - drawableRight.getIntrinsicHeight()
					- getPaddingBottom();
			float f2 = getWidth() - drawableRight.getIntrinsicWidth()
					- getPaddingRight();
			canvas.save();
			canvas.translate(f2, f1);
			drawableRight.draw(canvas);
			canvas.restore();
		}
		super.onDraw(canvas);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		Drawable[] arrayOfDrawable = getCompoundDrawables();
		drawableRight = arrayOfDrawable[2];
		setCompoundDrawablesWithIntrinsicBounds(arrayOfDrawable[0],
				arrayOfDrawable[1], null, arrayOfDrawable[3]);
	}

	protected void onTextChanged(CharSequence s, int start, int before,
			int count) {
		super.onTextChanged(s, start, before, count);
		if (s.length() == 0) {
			showClearIcon = false;
			return;
		}
		showClearIcon = true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if ((event.getAction() == 1)
				&& (showClearIcon)
				&& (drawableRight != null)
				&& (event.getX() >= getWidth()
						- drawableRight.getIntrinsicWidth() - getPaddingRight())
				&& (event.getY() >= getHeight()
						- drawableRight.getIntrinsicHeight()
						- getPaddingBottom())) {
			setText("");
			setInputType(getInputType());
			return true;
		}
		return super.onTouchEvent(event);
	}
}

