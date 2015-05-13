package ua.parus.pmo.parus8claims.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * Created by igor-go on 14.04.2015.
 * ua.parus.pmo.parus8claims.gui
 */
public class ErrorPopup implements DialogInterface.OnKeyListener {

    private final Context context;
    private DialogInterface.OnClickListener onClickListener;

    public ErrorPopup(final Context context , DialogInterface.OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
    }

    public void showErrorDialog(final String title, final String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton("Close", onClickListener != null ? onClickListener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.setOnKeyListener(this);
        alertDialog.show();
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
