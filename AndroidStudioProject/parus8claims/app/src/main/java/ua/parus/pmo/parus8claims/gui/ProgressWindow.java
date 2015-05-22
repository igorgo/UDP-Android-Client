package ua.parus.pmo.parus8claims.gui;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.parus.pmo.parus8claims.R;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */

public class ProgressWindow {

    private static ProgressWindow instance;
    private MaterialDialog dialog;

    public ProgressWindow(Context context, CharSequence message) {
        this.dialog = new MaterialDialogBuilder(context)
                .cancelable(false)
                .content(message)
                .progress(true,0)
                .show();
    }

    public ProgressWindow(Context context, int resId) {
        this(context, context.getText(resId));
    }

    public ProgressWindow(Context context) {
        this(context, R.string.please_wait);
    }

    public void setMessage(CharSequence message) {
        dialog.setContent(message);
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
