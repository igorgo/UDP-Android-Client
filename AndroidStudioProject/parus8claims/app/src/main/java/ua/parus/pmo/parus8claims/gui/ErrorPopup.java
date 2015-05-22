package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.utils.Constants;

public class ErrorPopup implements DialogInterface.OnKeyListener {

    private final Context context;
    private final MaterialDialog.ButtonCallback buttonCallback;

    public ErrorPopup(final Context context, MaterialDialog.ButtonCallback buttonCallback) {
        this.context = context;
        this.buttonCallback = buttonCallback;
    }

    public void showErrorDialog(final String title, final String message) {
        MaterialDialog dialog = new MaterialDialogBuilder(context)
                .content(message)
                .positiveText(android.R.string.ok)
                .callback(buttonCallback != null ? buttonCallback : new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                                super.onPositive(dialog);
                            }
                        }
                )
                .title(TextUtils.isEmpty(title) ? context.getText(R.string.error_title) : title)
                .titleColorRes(R.color.NegoSatate)
                .icon(context.getResources().getDrawable(R.drawable.ic_alert))
//                .typeface(FontCache.getInstance(context).getMaterialFontName(true),FontCache.getInstance(context).getMaterialFontName(true))
                .build();
        dialog.setOnKeyListener(this);
        dialog.show();
    }


    @Override
    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
        //noinspection StatementWithEmptyBody
        if (i == KeyEvent.KEYCODE_BACK) {
            //disable the back button
        }
        return true;
    }
}
