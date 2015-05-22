package ua.parus.pmo.parus8claims.gui;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.parus.pmo.parus8claims.utils.Constants;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */

public class ProgressWindow {

    private MaterialDialog dialog;

    public ProgressWindow(Context context, CharSequence message) {
        this.dialog = new MaterialDialog.Builder(context)
                .cancelable(false)
                .content(message)
                .progress(true,0)
                .typeface(Constants.FONT_BOLD,Constants.FONT_REGULAR)
                .show();
    }

    public ProgressWindow(Context context, int resId) {
        this(context, context.getText(resId));
    }

    public void setMessage(CharSequence message) {
        dialog.setContent(message);
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
