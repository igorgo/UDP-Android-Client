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
 */
public class InputDialog {

    public InputDialog(Context context, String prompt, String value, final ResultListener resultListener) {
        @SuppressLint("InflateParams")
        View lPromptsView = LayoutInflater.from(context).inflate(R.layout.input_dialog, null);
        AlertDialog.Builder lBuilder = new AlertDialog.Builder(context);
        lBuilder.setView(lPromptsView);
        final EditText lInput = (EditText) lPromptsView.findViewById(R.id.editTextDialogUserInput);
        if (value != null && !value.isEmpty())
            lInput.setText(value);
        ((TextView) lPromptsView.findViewById(R.id.prompt_label)).setText(prompt);
        lBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resultListener.onSetResult(false, lInput.getText().toString());
            }
        });

        lBuilder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultListener.onSetResult(true, lInput.getText().toString());
                    }
                }
        );
        lBuilder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        AlertDialog lDialog = lBuilder.create();
        //lDialog.set
        lDialog.show();
    }

    public interface ResultListener {
        public void onSetResult(boolean isPositive, String userInput);
    }

}
