package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.support.annotation.NonNull;

import ua.parus.pmo.parus8claims.utils.FontCache;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class MaterialDialogBuilder extends com.afollestad.materialdialogs.MaterialDialog.Builder {

    public MaterialDialogBuilder(@NonNull Context context) {
        super(context);
        typeface(FontCache.getInstance(context).getMaterialFontName(true), FontCache.getInstance(context)
                                                                                    .getMaterialFontName(true));
    }
}
