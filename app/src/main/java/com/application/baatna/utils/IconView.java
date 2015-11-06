package com.application.baatna.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class IconView extends TextView {
	public IconView(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Icons));
	}
	
	public IconView(Context context, AttributeSet attr) {
		super(context,attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Icons));
	}
	
	public IconView(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Icons));
	}
}