package ua.parus.pmo.parus8claims.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

/**
 * Created by igorgo on 14.04.2015.
 * base on http://stackoverflow.com/questions/5015686/android-spinner-with-multiple-choice
 */
public class MultiSpinner extends Spinner implements
        DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnCancelListener,
        View.OnLongClickListener
{

    private OnValueChangedListener mOnValueChangedListener;
    private OnItemSelectListener mOnItemSelectListener;
    private OnSetItemValueListener mOnSetItemValueListener;
    private boolean[] mSelected;
    private List<String> mItems;
    private String mAllSelectedText;


    private String mTagName;
    private String mNoOneSelectedText;
    private boolean mEditable;
    private char mItemSeparator;

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        mOnValueChangedListener = onValueChangedListener;
    }

    public MultiSpinner(Context context) {
        super(context);
        this.mAllSelectedText = "";
        this.mNoOneSelectedText = "";
        mEditable = false;
        mItemSeparator = ';';
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mSelected[which] = isChecked;
    }


    private void onOkDialog() {
        // обновляем тект в спиннеое
        String lOldValue = getValue();
        StringBuilder lBuffer = new StringBuilder();
        String lCurrentValue;
        boolean someUnselected = false;
        boolean someSelected = false;
        for (int i = 0; i < mItems.size(); i++) {
            if (mSelected[i]) {
                lCurrentValue = mItems.get(i);
                if (mOnSetItemValueListener != null) {
                    lCurrentValue = mOnSetItemValueListener.onSetItemValue(this, lCurrentValue);
                }
                lBuffer.append(lCurrentValue);
                lBuffer.append(mItemSeparator);
                someSelected = true;
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected && someSelected) {
            spinnerText = lBuffer.toString();
            if (spinnerText.length() > 1)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 1);
        } else {
            if (someSelected) {
                spinnerText = mAllSelectedText;
            } else {
                spinnerText = mNoOneSelectedText;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{spinnerText});
        setAdapter(adapter);
        if (mOnItemSelectListener != null) {
            mOnItemSelectListener.onItemsSelected(this, mSelected);
        }
        String newValue = getValue();
        if (!lOldValue.equals(newValue)) mOnValueChangedListener.onValueChanged(this, newValue);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setValue(getValue());
    }


    private void init() {

    }


    public String getValue() {
        if (getAdapter() != null && getAdapter().getCount() > 0)
            return getAdapter().getItem(0).toString();
        else
            return mNoOneSelectedText;
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        String oldValue = getValue();
        if (oldValue == null) {
            oldValue = "";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{value});
        setAdapter(adapter);
        if (mItems != null) {
            if (value.equals(mNoOneSelectedText)) {
                for (int i = 0; i < mItems.size(); i++) {
                    mSelected[i] = false;
                }
            } else if (value.equals(mAllSelectedText)) {
                for (int i = 0; i < mItems.size(); i++) {
                    mSelected[i] = true;
                }
            } else {
                List<String> lVals = Arrays.asList(value.split("\\s*" + mItemSeparator + "\\s*"));
                for (int i = 0; i < mItems.size(); i++) {
                    mSelected[i] = lVals.contains(mItems.get(i));
                }
            }
        }
        if ( (mOnValueChangedListener != null) && !oldValue.equals(value)) {
            mOnValueChangedListener.onValueChanged(this, value);
        }
    }

    @Override
    public boolean performClick() {
        if (mItems != null && mItems.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMultiChoiceItems(
                    mItems.toArray(new CharSequence[mItems.size()]),
                    mSelected,
                    this);
            builder.setPositiveButton(
                    android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dialog.cancel();
                            onOkDialog();
                        }
                    }
            );
            builder.setNegativeButton(
                    "-",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );
            builder.setNeutralButton(
                    "+",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );

            builder.setOnCancelListener(this);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (int i = 0; i < mItems.size(); i++) {
                                dialog.getListView().setItemChecked(i, true);
                                mSelected[i] = true;
                            }
                        }
                    }
            );
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (int i = 0; i < mItems.size(); i++) {
                                dialog.getListView().setItemChecked(i, false);
                                mSelected[i] = false;
                            }
                        }
                    }
            );
        }
        return true;
    }


    public void clear() {
        setValue(mNoOneSelectedText);
    }

    public boolean isSingleSelected() {
        int cnt = 0;
        for (boolean v : mSelected) if (v) cnt++;
        return (cnt == 1);
    }

    public void setAllCheckedText(String text) {
        this.mAllSelectedText = text;
    }

    public void setNoOneCheckedText(String text) {
        this.mNoOneSelectedText = text;
    }


//    public void setItems(List<String> items, boolean preserveValue, ultiSpinnerListener listener) {
    public void setItems(List<String> items, boolean preserveValue) {
        this.mItems = items;
        //this.mMultiSpinnerListener = listener;
        String oldValue = getValue();
        mSelected = new boolean[items.size()];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{mNoOneSelectedText});
        setAdapter(adapter);
        String newValue = getValue();
        if (!oldValue.equals(newValue)) {
            if (preserveValue) setValue(oldValue);
            else if (mOnValueChangedListener != null) mOnValueChangedListener.onValueChanged(this, newValue);
        }
    }

    public void setEditable(boolean isEditable) {
        this.mEditable = isEditable;
        if (mEditable)
            this.setOnLongClickListener(this);
        else
            this.setOnLongClickListener(null);
    }

    public void setItemSeparator(char separator) {
        this.mItemSeparator = separator;
    }

    public void setOnSetItemValueListener(OnSetItemValueListener listemerSetItemVal) {
        this.mOnSetItemValueListener = listemerSetItemVal;
    }

    @Override
    public boolean onLongClick(View view) {
        AlertDialog.Builder lDialog = new AlertDialog.Builder(getContext());
        final EditText lInput = new EditText(getContext());
        String lCurrentText = getValue();
        if (!(lCurrentText.equals(mAllSelectedText) || lCurrentText.equals(mNoOneSelectedText)))
            lInput.setText(lCurrentText);
        lDialog.setView(lInput);
        lDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setValue(lInput.getText().toString());
            }
        });

        lDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        lDialog.show();

        return false;
    }

    public void setTagName(String tagName) {
        this.mTagName = tagName;
    }

    public String getTagName() {
        return mTagName;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.mOnItemSelectListener = onItemSelectListener;
    }

    public interface OnValueChangedListener {
        public void onValueChanged(MultiSpinner sender, String value);
    }

    public interface OnItemSelectListener    {
        public void onItemsSelected(MultiSpinner sender, boolean[] selected);
    }

    public interface OnSetItemValueListener {
        public String onSetItemValue(MultiSpinner sender, String selected);
    }

}
