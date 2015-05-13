package ua.parus.pmo.parus8claims.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ua.parus.pmo.parus8claims.R;

/**
 * Created by igorgo on 19.04.2015.
 *
 */
public class InputDialog {

    public InputDialog(Context context, String prompt, String value, final ResultListener resultListener) {
        @SuppressLint("InflateParams")
        View promptView = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(promptView);
        final EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
        if (value != null && !value.isEmpty())
            editText.setText(value);
        ((TextView) promptView.findViewById(R.id.prompt_label)).setText(prompt);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resultListener.onSetResult(false, editText.getText().toString());
            }
        });

        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultListener.onSetResult(true, editText.getText().toString());
                    }
                }
        );
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        AlertDialog alertDialog = builder.create();
        //alertDialog.set
        alertDialog.show();
    }

    public interface ResultListener {
        public void onSetResult(boolean isPositive, String userInput);
    }

}
