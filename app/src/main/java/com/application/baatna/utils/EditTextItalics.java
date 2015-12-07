package com.application.baatna.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by akshitgupta on 07/12/15.
 */
public class EditTextItalics extends EditText {

    public EditTextItalics(Context context) {
        super(context);
        setTypeface(CommonLib.getTypeface(context, CommonLib.LightItalic));
    }

    public EditTextItalics(Context context, AttributeSet attr) {
        super(context,attr);
        setTypeface(CommonLib.getTypeface(context, CommonLib.LightItalic));
    }

    public EditTextItalics(Context context, AttributeSet attr, int i) {
        super(context,attr,i);
        setTypeface(CommonLib.getTypeface(context, CommonLib.LightItalic));
    }



}
