package ua.parus.pmo.parus8claims.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import ua.parus.pmo.parus8claims.utils.FontCache;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class TextView extends android.widget.TextView {
    public TextView(Context context) {
        super(context);
        setFont(context,null);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont(context,attrs);
    }

    private void setFont(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            Typeface tf = FontCache.getInstance(context).getTypeface(attrs);
            if (tf != null) {
                setTypeface(tf);
            }
        }
    }

    @Override
    public void setTextAppearance(@NonNull Context context, int resid) {
        super.setTextAppearance(context, resid);
        if (!isInEditMode()) {
            int style = 0;
            Typeface tf = getTypeface();
            if (tf != null) {
                if (tf.isBold()) style++;
                if (tf.isItalic()) style = style + 2;
            }
            tf = FontCache.getInstance(context).getTypeface(style);
            if (tf != null) {
                setTypeface(tf);
            }
        }
    }
}
