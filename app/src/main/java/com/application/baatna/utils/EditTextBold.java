package com.application.baatna.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextBold extends EditText {
	public EditTextBold(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}

	public EditTextBold(Context context, AttributeSet attr) {
		super(context, attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}

	public EditTextBold(Context context, AttributeSet attr, int i) {
		super(context, attr, i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
	}
}