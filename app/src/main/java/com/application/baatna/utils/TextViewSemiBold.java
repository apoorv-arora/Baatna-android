package com.application.baatna.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewSemiBold extends TextView {
	public TextViewSemiBold(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}
	
	public TextViewSemiBold(Context context, AttributeSet attr) {
		super(context,attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}
	
	public TextViewSemiBold(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}

}