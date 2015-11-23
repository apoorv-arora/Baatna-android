package com.application.baatna.utils.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NoSwipeViewPager extends ViewPager {

	boolean mSwipeable = false;
	private MenuTouchListener mMenuTouchListener;

	public void setSwipeable(boolean swipeable) {
		this.mSwipeable = swipeable;
	}

	public NoSwipeViewPager(Context context) {
		super(context);
	}

	public NoSwipeViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setMenuTouchedListener(MenuTouchListener listener) {
		this.mMenuTouchListener = listener;
	}

	@Override
	protected boolean canScroll(View v, boolean arg1, int arg2, int arg3, int arg4) {

		if (v != this
				&& v instanceof ViewPager /*
											 * && !(v instanceof NoSwipeViewPager)
											 */) {
			return true;
		}

		return super.canScroll(v, arg1, arg2, arg3, arg4);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (mMenuTouchListener != null) {
			mMenuTouchListener.menuTouched(arg0);
		}

		if (mSwipeable)
			return super.onInterceptTouchEvent(arg0);

		return mSwipeable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mSwipeable)
			return super.onTouchEvent(event);

		return mSwipeable;
	}

	public interface MenuTouchListener {

		public void menuTouched(MotionEvent e);

	}

}