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

    private final Context mContext;

    public ErrorPopup(final Context context) {
        this.mContext = context;
    }

    public void showErrorDialog(final String title, final String message) {
        AlertDialog.Builder lAlertDialog = new AlertDialog.Builder(mContext);
        lAlertDialog.setTitle(title);
        lAlertDialog.setMessage(message);
        lAlertDialog.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        lAlertDialog.setOnKeyListener(this);
        lAlertDialog.show();
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
