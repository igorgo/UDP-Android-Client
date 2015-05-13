package ua.parus.pmo.parus8claims.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.List;

import ua.parus.pmo.parus8claims.R;

/**
 * Created  by  igorgo on 01.05.2015.
 */
public class SimpleSpinner extends Spinner implements DialogInterface.OnClickListener{
    private static final String TAG =  SimpleSpinner.class.getSimpleName();
    private static final String EMPTY_STRING = "";

    private List<String> displayItems;
    private List<String> valueStringItems;
    private List<Long> valueLongItems;
    private ValueType valueType;
    private Long valueLong;
    private String valueString;
    private int selected;
    private String selfName;
    private OnValueChangedListener onValueChangedListener;

    public SimpleSpinner(Context context) {
        super(context, MODE_DIALOG);
        valueLong = null;
        valueString = null;
        selected = -1;
    }

    public SimpleSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        valueLong = null;
        valueString = null;
        selected = -1;
    }

    public void setItemsStringVals(List<String> displayItems, List<String> valueItems, String defaultValue) {
        this.displayItems = displayItems;
        this.valueStringItems = valueItems;
        this.valueType = ValueType.STRING;
        if (defaultValue == null) {
            this.clear();
        } else {
            setValue(defaultValue);
        }
    }

    public void setItems(List<String> displayItems, String defaultValue) {
        this.displayItems = displayItems;
        this.valueStringItems = displayItems;
        this.valueType = ValueType.STRING;
        if (defaultValue == null) {
            this.clear();
        } else {
            setValue(defaultValue);
        }

    }

    public void setItemsLongVals(List<String> displayItems, List<Long> valueItems, Long defaultValue) {
        this.displayItems = displayItems;
        this.valueLongItems = valueItems;
        this.valueType = ValueType.LONG;
        if (defaultValue == null) {
            this.clear();
        } else {
            setValue(defaultValue);
        }
    }


    public void clear() {
        if (this.valueType == ValueType.LONG && this.valueLongItems.size() > 0) {
            this.setValue(this.valueLongItems.get(0));
        }
        if(this.valueType == ValueType.STRING && this.valueStringItems.size() > 0) {
            this.setValue(valueStringItems.get(0));
        }
    }

    public void setValue(String value) {
        String displayItem = EMPTY_STRING;
        String oldValue = this.getValueString();
        this.selected = -1;
        if (this.valueType == ValueType.LONG) {
            return;
        }
        if (value != null) {
            if (this.valueStringItems != null) {
                for (int i = 0; i < this.valueStringItems.size(); i++) {
                    if (this.valueStringItems.get(i).equals(value)) {
                        displayItem = this.displayItems.get(i);
                        this.selected = i;
                        break;
                    }
                }
            }
        }
        this.valueString = value;
        this.setValueText(displayItem);
        if (this.onValueChangedListener != null) {
            if ((oldValue == null && value != null) || (oldValue != null && (value == null || !oldValue.equals(value) )) )  {
                this.onValueChangedListener.onValueChanged(this, value, null );
            }
        }
    }

    public void setValue(Long value) {
        String displayItem = EMPTY_STRING;
        Long oldValue = this.getValueLong();
        this.selected = -1;
        if (this.valueType == ValueType.STRING) {
            return;
        }
        if (value != null) {
            if (this.valueLongItems != null) {
                for (int i = 0; i < this.valueLongItems.size(); i++) {
                    if (this.valueLongItems.get(i).equals(value)) {
                        displayItem = this.displayItems.get(i);
                        this.selected = i;
                        break;
                    }
                }
            }
        }
        this.valueLong = value;
        this.setValueText(displayItem);

        if ((this.onValueChangedListener != null) && !( (oldValue==null ? (long) -1 : oldValue) == (value==null ? (long) -1 : value) ) ) {
            this.onValueChangedListener.onValueChanged(this, null, value );
        }
    }


    public String getValueDisplay() {
        return this.displayItems.get(this.selected);
    }


    @Override
    public boolean performClick() {
        Log.i(TAG, "performClick");
        if (this.displayItems != null && this.displayItems.size() > 0) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            //dialogBuilder.setSingleChoiceItems( this.displayItems.toArray(new CharSequence[this.displayItems.size()]), this.selected, this);
            dialogBuilder.setSingleChoiceItems(new ArrayAdapter<>(
                    getContext(),
                    R.layout.dropdown_multiline_single_choice_item,
                    android.R.id.text1,
                    this.displayItems),this.selected, this);
            /*dialogBuilder.setAdapter (
                    new ArrayAdapter<>(
                            getContext(),
                            R.layout.dropdown_multiline_item,
                            R.id.item,
                            this.displayItems));*/

            //dialogBuilder.setSingleChoiceItems( new SimpleAdapter(getContext(),this.displayItems), 2, this);
            /*dialogBuilder.setAdapter(new ArrayAdapter<>(
                    getContext(),
                    R.layout.spinner_text_item,
                    this.displayItems.toArray()), this);*/
            AlertDialog dialog = dialogBuilder.create();
            //dialog.getListView().setSelector(R.drawable.apptheme_btn_radio_holo_light);


            //dialog.getListView().setSelection(this.selected);
            dialog.show();
        }

        return true;
    }



    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.i(TAG, "onClick");
        if (this.valueType == ValueType.LONG && valueLongItems != null && valueLongItems.size() > 0) {
            setValue(valueLongItems.get(which));
            dialog.dismiss();
            return;
        }
        if (this.valueType == ValueType.STRING && valueStringItems != null && valueStringItems.size() > 0){
            setValue(valueStringItems.get(which));
            dialog.dismiss();
            return;
        }
    }

    private void setValueText(String displayValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_text_item,
                new String[]{displayValue});
        setAdapter(adapter);
    }

    public String getValueString() {
        return valueString;
    }

    public Long getValueLong() {
        return valueLong;
    }

    public String getName() {
        return selfName;
    }

    public void setName(String name) {
        this.selfName = name;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    private enum ValueType {LONG, STRING}

    public interface OnValueChangedListener {
        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong);
    }
}
