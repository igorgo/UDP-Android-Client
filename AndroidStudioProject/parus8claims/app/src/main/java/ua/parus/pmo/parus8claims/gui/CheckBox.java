package ua.parus.pmo.parus8claims.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;

import ua.parus.pmo.parus8claims.utils.FontCache;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class CheckBox extends android.widget.CheckBox {
    public CheckBox(Context context) {
        super(context);
        setFont(context, null);
    }

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context, attrs);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont(context, attrs);
    }

    private void setFont(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            Typeface tf = FontCache.getInstance(context).getTypeface(attrs);
            if (tf != null) {
                setTypeface(tf);
            }
        }
    }
}
