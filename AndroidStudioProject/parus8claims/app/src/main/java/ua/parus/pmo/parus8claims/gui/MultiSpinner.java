package ua.parus.pmo.parus8claims.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import ua.parus.pmo.parus8claims.R;

@SuppressWarnings("unused")
public class MultiSpinner extends Spinner implements
        DialogInterface.OnCancelListener,
        View.OnLongClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = MultiSpinner.class.getSimpleName();
    private static final String EMPTY_STRING = "";
    private static final char SEMICOLON = ';';
    private static final String BUTTON_DESELECT_ALL_TEXT = "-";
    private static final String BUTTON_SELECT_ALL_TEXT = "+";
    private OnValueChangedListener onValueChangedListener;
    private OnItemSelectListener onItemSelectListener;
    private OnSetItemValueListener onSetItemValueListener;
    private boolean[] selected;
    private List<String> items;
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

    private void onOkDialog() {
        // обновляем тект в спиннере
        String oldValue = this.getValue();
        StringBuilder buffer = new StringBuilder();
        String currentValue;
        boolean someUnSelected = false;
        boolean someSelected = false;
        for (int i = 0; i < this.items.size(); i++) {
            if (this.selected[i]) {
                currentValue = this.items.get(i);
                if (this.onSetItemValueListener != null) {
                    currentValue = this.onSetItemValueListener.onSetItemValue(this, currentValue);
                }
                buffer.append(currentValue);
                buffer.append(this.itemSeparator);
                someSelected = true;
            } else {
                someUnSelected = true;
            }
        }
        String spinnerText;
        if (someUnSelected && someSelected) {
            spinnerText = buffer.toString();
            if (spinnerText.length() > 1)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 1);
        } else {
            if (someSelected) {
                spinnerText = this.textAllSelected;
            } else {
                spinnerText = this.textNoOneSelected;
            }
        }
        setAdapter(new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_text_item,
                new String[]{spinnerText}));
        if (this.onItemSelectListener != null) {
            this.onItemSelectListener.onItemsSelected(this, this.selected);
        }
        String newValue = this.getValue();
        if ((this.onValueChangedListener != null) && !oldValue.equals(newValue))
            this.onValueChangedListener.onValueChanged(this, newValue);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.setValue(this.getValue());
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
            if (this.items != null) {
                if (value.equals(this.textNoOneSelected)) {
                    for (int i = 0; i < this.items.size(); i++) {
                        this.selected[i] = false;
                    }
                } else if (value.equals(this.textAllSelected)) {
                    for (int i = 0; i < this.items.size(); i++) {
                        this.selected[i] = true;
                    }
                } else {
                    List<String> values = Arrays.asList(value.split("\\s*" + this.itemSeparator + "\\s*"));
                    for (int i = 0; i < this.items.size(); i++) {
                        this.selected[i] = values.contains(this.items.get(i));
                    }
                }
            }
            if (this.onValueChangedListener != null) {
                this.onValueChangedListener.onValueChanged(this, value);
            }
        }
    }

    @Override
    public boolean performClick() {
        if (this.items != null && this.items.size() > 0) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setAdapter(
                    new ArrayAdapter<>(
                            getContext(),
                            R.layout.dropdown_multiline_multi_choice_item,
                            android.R.id.text1,
                            this.items),
                    null);
            dialogBuilder.setPositiveButton(
                    android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MultiSpinner.this.onOkDialog();
                        }
                    }
            );
            dialogBuilder.setNegativeButton(
                    BUTTON_DESELECT_ALL_TEXT,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // пусто - назначим сразу после показа,
                            // иначе кнопка будет закрывать диалог
                        }
                    }
            );
            dialogBuilder.setNeutralButton(
                    BUTTON_SELECT_ALL_TEXT,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // пусто - назначим сразу после показа,
                            // иначе кнопка будет закрывать диалог
                        }
                    }
            );
            final AlertDialog dialog = dialogBuilder.create();
            ListView listView = dialog.getListView();
            listView.setAdapter(
                    new ArrayAdapter<>(
                            getContext(),
                            R.layout.dropdown_multiline_multi_choice_item,
                            android.R.id.text1,
                            this.items));
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setOnItemClickListener(this);
            listView.setDivider(null);
            listView.setDividerHeight(-1);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    for (int i = 0; i < MultiSpinner.this.items.size(); i++) {
                        ((AlertDialog) dialog).getListView().setItemChecked(i, selected[i]);
                    }
                }
            });
            dialog.setOnCancelListener(this);
            dialog.show();
            // теперь назначаем обработчики нажатия кнопок, которые не должны закрывать диалог.
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (int i = 0; i < MultiSpinner.this.items.size(); i++) {
                                dialog.getListView().setItemChecked(i, true);
                                MultiSpinner.this.selected[i] = true;
                            }
                        }
                    }
            );
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (int i = 0; i < MultiSpinner.this.items.size(); i++) {
                                dialog.getListView().setItemChecked(i, false);
                                MultiSpinner.this.selected[i] = false;
                            }
                        }
                    }
            );
        }
        return true;
    }

    public void clear() {
        this.setValue(this.textNoOneSelected);
    }

    public boolean isSingleSelected() {
        int counter = 0;
        for (boolean v : this.selected) if (v) counter++;
        return (counter == 1);
    }

    public void setAllCheckedText(String text) {
        this.textAllSelected = text;
    }

    public void setNoOneCheckedText(String text) {
        this.textNoOneSelected = text;
    }

    public void setItems(List<String> items, boolean preserveValue) {
        this.items = items;
        String oldValue = this.getValue();
        this.selected = new boolean[items.size()];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_text_item, new String[]{this.textNoOneSelected});
        setAdapter(adapter);
        String newValue = this.getValue();
        if (!oldValue.equals(newValue)) {
            if (preserveValue) this.setValue(oldValue);
            else if (this.onValueChangedListener != null)
                this.onValueChangedListener.onValueChanged(this, newValue);
        }
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

    public void setOnSetItemValueListener(OnSetItemValueListener listemerSetItemVal) {
        this.onSetItemValueListener = listemerSetItemVal;
    }

    @Override
    public boolean onLongClick(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        String currentText = this.getValue();
        if (!(currentText.equals(this.textAllSelected) || currentText.equals(this.textNoOneSelected)))
            input.setText(currentText);
        dialog.setView(input);
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MultiSpinner.this.setValue(input.getText().toString());
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        dialog.show();
        return false;
    }

    public String getName() {
        return this.selfName;
    }

    public void setName(String name) {
        this.selfName = name;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView checkedTextView = (CheckedTextView) view;
        selected[position] = checkedTextView.isChecked();
    }

    public interface OnValueChangedListener {
        void onValueChanged(MultiSpinner sender, String value);
    }

    public interface OnItemSelectListener {
        void onItemsSelected(MultiSpinner sender, boolean[] selected);
    }

    public interface OnSetItemValueListener {
        String onSetItemValue(MultiSpinner sender, String selected);
    }

}
