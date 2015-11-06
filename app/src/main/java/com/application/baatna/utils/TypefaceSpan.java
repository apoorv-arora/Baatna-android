package com.application.baatna.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Style a {@link Spannable} with a custom {@link Typeface}.
 * 
 * @author Tristan Waddington
 */
public class TypefaceSpan extends MetricAffectingSpan {
 
    private Typeface mTypeface;
    private int mTextColor;
    private float mTextSize;
 
    /**
     * Load the {@link Typeface} and apply to a {@link Spannable}.
     */
    public TypefaceSpan(Context context, String typefaceName, int color, float size) {
        
    	if(typefaceName.equals(CommonLib.BOLD_FONT_FILENAME))
    		mTypeface = CommonLib.getTypeface(context, CommonLib.Bold);
    	else
    		mTypeface = CommonLib.getTypeface(context, CommonLib.Regular);
    		
    	//mTypeface = sTypefaceCache.get(typefaceName);
        mTextColor = color;
        mTextSize = size;
 
    }
 
    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);
        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
 
    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);
        tp.setColor(mTextColor);
        tp.setTextSize(mTextSize);
        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}