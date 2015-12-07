package com.application.baatna.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextRegular extends EditText {

	public EditTextRegular(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
	}
	
	public EditTextRegular(Context context, AttributeSet attr) {
		super(context,attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
	}
	
	public EditTextRegular(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Regular));
	}
}