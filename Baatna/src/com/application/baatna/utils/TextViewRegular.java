package com.application.baatna.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewRegular extends TextView {
	public TextViewRegular(Context context) {
		super(context);
		
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
		setPaintFlags(getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
		
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Regular));
		//setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		//setTypeface(getTypeface().NORMAL);
		//setTypeface(getTypeface(), Typeface.NORMAL);
	}
	
	public TextViewRegular(Context context, AttributeSet attr) {
		super(context,attr);
		
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Regular));
		setPaintFlags(getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
		
		//setTypeface(getTypeface().DEFAULT);
		//setTypeface(getTypeface(), Typeface.NORMAL);
		//setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}
	
	public TextViewRegular(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Regular));
		setPaintFlags(getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
		
		//setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		//setTypeface(getTypeface().DEFAULT);
		//setTypeface(getTypeface(), Typeface.NORMAL);
	}

}