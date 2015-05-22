package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.graphics.Typeface;
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

    private void setFont(Context context, AttributeSet attrs) {
        Typeface tf = FontCache.getInstance(context).getTypeface(attrs);
        if (tf != null) {
            setTypeface(tf);
        }
    }



}
