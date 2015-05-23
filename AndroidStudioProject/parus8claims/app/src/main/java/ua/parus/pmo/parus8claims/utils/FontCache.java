package ua.parus.pmo.parus8claims.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import java.util.Hashtable;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class FontCache {

    private static FontCache instance;
    private Context context;
    private boolean useCondensed;

    private FontCache(Context context) {
        this.context = context;
        this.useCondensed = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS).getBoolean(Constants
                .PREF_FONT,true);
    }

    public static FontCache getInstance(Context context) {
        if(instance==null){
            return instance = new FontCache(context);
        }else{
            return instance;
        }
    }

    private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public Typeface getTypeface(int fontType) {
        String fontName = (useCondensed) ? Constants.FONT_NAME_CONDENSED : Constants.FONT_NAME;
        switch (fontType) {
            case 0:
                fontName = fontName + Constants.FONT_REGULAR;
                break;
            case 1:
                fontName = fontName + Constants.FONT_BOLD;
                break;
            case 2:
                fontName = fontName + Constants.FONT_ITALIC;
                break;
            case 3:
                fontName = fontName + Constants.FONT_BOLD_ITALIC;
                break;
        }
        fontName = fontName + Constants.FONT_EXTENSION;
        Typeface typeface = fontCache.get(fontName);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(fontName, typeface);
        }
        return typeface;

    }


    public Typeface getTypeface(AttributeSet attrs) {
        int fontType = (attrs == null) ? 0 : attrs.getAttributeIntValue(Constants.ANDROID_SCHEMA,"textStyle",0);
        return getTypeface(fontType);
    }

    public String getMaterialFontName(boolean bold) {
        String fontName = (useCondensed) ? Constants.FONT_NAME_CONDENSED : Constants.FONT_NAME;
        fontName = fontName + (bold ? Constants.FONT_BOLD : Constants.FONT_REGULAR);
        return fontName + Constants.FONT_EXTENSION;
    }

    public Typeface getTypeface() {
        return getTypeface(null);
    }

    public void clear() {
        instance = null;
    }
}
