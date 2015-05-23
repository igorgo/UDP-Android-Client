package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.parus.pmo.parus8claims.R;

@SuppressWarnings("unused")
public class MultiSpinner extends Spinner implements
        View.OnLongClickListener,
        MaterialDialog.ListCallbackMultiChoice {

    private static final String TAG = MultiSpinner.class.getSimpleName();
    private static final String EMPTY_STRING = "";
    private static final char SEMICOLON = ';';
    private static final String BUTTON_DESELECT_ALL_TEXT = "-";
    private static final String BUTTON_SELECT_ALL_TEXT = "+";
    private OnValueChangedListener onValueChangedListener;
    private List<String> displayItems;
    private List<String> valueItems;
    private String textAllSelected;
    private String textNoOneSelected;
    private String selfName;
    private char itemSeparator;

    public MultiSpinner(Context context) {
        super(context);
        init();
    }

    public MultiSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    private void init() {
        this.textAllSelected = EMPTY_STRING;
        this.textNoOneSelected = EMPTY_STRING;
        this.setOnLongClickListener(null);
        this.itemSeparator = SEMICOLON;
    }

    public String getValue() {
        if (getAdapter() != null && getAdapter().getCount() > 0)
            return getAdapter().getItem(0).toString();
        else
            return this.textNoOneSelected;
    }

    public void setValue(String value) {
        if (value == null) {
            value = EMPTY_STRING;
        }
        String oldValue = this.getValue();
        if (oldValue == null) {
            oldValue = EMPTY_STRING;
        }
        if (!oldValue.equals(value)) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    R.layout.spinner_text_item,
                    new String[]{value});
            setAdapter(adapter);
            if (this.onValueChangedListener != null) {
                this.onValueChangedListener.onValueChanged(this, value);
            }
        }
    }
// todo: not return alltext
    @Override
    public boolean performClick() {
        if (this.displayItems != null && this.displayItems.size() > 0) {
            List<Integer> selectedIndeces = new ArrayList<>();
            String value = this.getValue();
            if (!TextUtils.isEmpty(value) && !value.equals(this.textNoOneSelected)) {
                if (value.equals(this.textAllSelected)) {
                    for (int i = 0; i < this.displayItems.size(); i++) {
                        selectedIndeces.add(i);
                    }
                } else {
                    List<String> values = Arrays.asList(value.split("\\s*" + this.itemSeparator + "\\s*"));
                    for (int i = 0; i < this.valueItems.size(); i++) {
                        if (values.contains(this.valueItems.get(i))) {
                            selectedIndeces.add(i);
                        }
                    }
                }
            }
            new MaterialDialogBuilder(getContext())
                    .autoDismiss(false)
                    .items(displayItems.toArray(new CharSequence[displayItems.size()]))
                    .positiveText(android.R.string.ok)
                    .negativeText(BUTTON_DESELECT_ALL_TEXT)
                    .neutralText(BUTTON_SELECT_ALL_TEXT)
                    .itemsCallbackMultiChoice(selectedIndeces.toArray(new Integer[selectedIndeces.size()]), this)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Log.d(TAG, "onPositive");
                            dialog.dismiss();
                            super.onPositive(dialog);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            Log.d(TAG, "onNegative");
                            dialog.setSelectedIndices(new Integer[]{});
                            super.onNegative(dialog);
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            Log.d(TAG, "onNeutral");
                            List<Integer> selectedIndeces = new ArrayList<>();
                            for (int i = 0; i < displayItems.size(); i++) {
                                selectedIndeces.add(i);
                            }
                            dialog.setSelectedIndices(selectedIndeces.toArray(new Integer[selectedIndeces.size()]));
                            super.onNeutral(dialog);
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            setValue(getValue());
                        }
                    })
                    .show();
        }
        return true;
    }

    public void clear() {
        this.setValue(this.textNoOneSelected);
    }

    public boolean isSingleSelected() {
        String value = getValue();
        List<String> values = Arrays.asList(value.split("\\s*" + this.itemSeparator + "\\s*"));
        int counter = 0;
        for (int i = 0; i < this.valueItems.size(); i++) {
            if (values.contains(this.valueItems.get(i))) {
                counter++;
            }
        }
        return (counter == 1);
    }

    public void setAllCheckedText(String text) {
        this.textAllSelected = text;
    }

    public void setNoOneCheckedText(String text) {
        this.textNoOneSelected = text;
    }

    public void setItems(List<String> displayItems, List<String> valueItems, boolean preserveValue) {
        this.displayItems = displayItems;
        this.valueItems = valueItems;
        String oldValue = this.getValue();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_text_item, new String[]{this.textNoOneSelected});
        setAdapter(adapter);
        String newValue = this.getValue();
        if (!oldValue.equals(newValue)) {
            if (preserveValue) {
                this.setValue(oldValue);
            } else {
                if (this.onValueChangedListener != null)
                    this.onValueChangedListener.onValueChanged(this, newValue);
            }
        }
    }

    public void setItems(List<String> items, boolean preserveValue) {
        this.setItems(items, items, preserveValue);
    }

    public void setEditable(boolean isEditable) {
        if (isEditable)
            this.setOnLongClickListener(this);
        else
            this.setOnLongClickListener(null);
    }

    public void setItemSeparator(char separator) {
        this.itemSeparator = separator;
    }

    @Override
    public boolean onLongClick(View view) {
        String currentText = this.getValue();
        new MaterialDialogBuilder(getContext())
                .input(
                        null,
                        (currentText.equals(this.textAllSelected) || currentText.equals(this.textNoOneSelected)) ? null : currentText,
                        true,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                setValue(charSequence.toString());
                            }
                        })
                .negativeText(android.R.string.cancel)
                .show();
        return false;
    }

    public String getName() {
        return this.selfName;
    }

    public void setName(String name) {
        this.selfName = name;
    }

    @Override
    public boolean onSelection(MaterialDialog materialDialog, Integer[] integers, CharSequence[] charSequences) {
        String oldValue = this.getValue();
        StringBuilder buffer = new StringBuilder();
        String spinnerText;
        if (integers.length == 0) {
            spinnerText = this.textNoOneSelected;
        } else if (integers.length == valueItems.size()) {
            spinnerText = this.textAllSelected;
        } else {
            for (Integer integer : integers) {
                buffer.append(valueItems.get(integer));
                buffer.append(this.itemSeparator);
            }
            spinnerText = buffer.toString();
            if (spinnerText.length() > 1)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 1);
        }
        setAdapter(new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_text_item,
                new String[]{spinnerText}));
        String newValue = this.getValue();
        if ((this.onValueChangedListener != null) && !oldValue.equals(newValue))
            this.onValueChangedListener.onValueChanged(this, newValue);
        return false;
    }

    public interface OnValueChangedListener {
        void onValueChanged(MultiSpinner sender, String value);
    }

}
