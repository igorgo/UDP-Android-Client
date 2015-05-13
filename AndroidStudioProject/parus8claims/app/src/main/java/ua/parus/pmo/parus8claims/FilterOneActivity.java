package ua.parus.pmo.parus8claims;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.db.ApplistORM;
import ua.parus.pmo.parus8claims.db.BuildsORM;
import ua.parus.pmo.parus8claims.db.ReleasesORM;
import ua.parus.pmo.parus8claims.db.UnitsORM;
import ua.parus.pmo.parus8claims.gui.InputDialog;
import ua.parus.pmo.parus8claims.gui.MultiSpinner;
import ua.parus.pmo.parus8claims.gui.SemicolonTokenizer;
import ua.parus.pmo.parus8claims.om.filter.Filter;


public class FilterOneActivity extends ActionBarActivity
        implements MultiSpinner.OnSetItemValueListener,
        MultiSpinner.OnValueChangedListener
{

    public static final String EXTRA_RN_KEY = "e-rn";
    public static final String EXTRA_REQUEST_KEY = "e-request";

    private static final String TAG = "FilterOneActivity";
    public static final String SPINNER_BUILD_TAGNAME = "build8";
    public static final String SPINNER_RELEASE_TAGNAME = "release8";
    public static final String SPINNER_VERSION_TAGNAME = "version8";
    private Holder holder;
    private Intent mResultIntent;
    private Filter filter;
    private int selfRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "Creating activity...");
        super.onCreate(savedInstanceState);
        selfRequest = getIntent().getIntExtra(EXTRA_REQUEST_KEY, 0);
        setContentView(R.layout.activity_filter_editor);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.pmo_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle(R.string.query_editor);
        filter = new Filter();
        filter.filter_rn = getIntent().getLongExtra(EXTRA_RN_KEY, 0);
        filter.readFromServer(this);
        holder = new Holder();
        holder.version8.setEditable(true);
        holder.version8.setTagName(SPINNER_VERSION_TAGNAME);
        holder.version8.setOnValueChangedListener(this);
        holder.release8.setEditable(true);
        holder.release8.setTagName(SPINNER_RELEASE_TAGNAME);
        holder.release8.setOnValueChangedListener(this);
        holder.build8.setEditable(true);
        holder.build8.setTagName(SPINNER_BUILD_TAGNAME);
        holder.build8.setOnSetItemValueListener(this);
        setAdapters();
        setFromFilterValues();
        holder.number.requestFocus();
        Log.i(TAG, "Activity created");

    }

    private void setFromFilterValues() {
        holder.number.setText(filter.condNumber);
        holder.version8.setValue(filter.condVersion);
        holder.release8.setValue(filter.condRelease);
        holder.build8.setValue(filter.condBuild);
        holder.application.setText(filter.condApplication);
        holder.unit.setText(filter.condUnit);
        holder.imInitiator.setChecked(filter.condImInitiator);
        holder.imExecutor.setChecked(filter.condImExecutor);
        holder.content.setText(filter.condContent);
    }

    private void setupVersions() {
        holder.version8.setItems(
                ReleasesORM.getVersions(this, true, ""),
                true
        );

    }

    private void setupReleases() {
        List<String> items = new ArrayList<>();
        if (holder.version8.isSingleSelected())
            items = ReleasesORM.getReleasesCodes(this, holder.version8.getValue(), true, null);
        holder.release8.setItems(
                items, true
        );
    }

    private void setupBuilds() {
        List<String> items = new ArrayList<>();
        if (holder.release8.isSingleSelected())
            items = BuildsORM.getBuildsCodes(this, holder.release8.getValue(), true);
        holder.build8.setItems(
                items, true
        );
    }

    private void setupApps() {
        holder.application.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        ApplistORM.getApps(this)
                )
        );
        holder.application.setTokenizer(new SemicolonTokenizer());
    }

    private void setupUnits() {
        holder.unit.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        UnitsORM.getUnits(this)
                )
        );
        holder.unit.setTokenizer(new SemicolonTokenizer());
    }

    private void setAdapters() {
        setupVersions();
        setupReleases();
        setupBuilds();
        setupApps();
        setupUnits();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(TAG, "Creating Options Menu");
        getMenuInflater().inflate(R.menu.menu_filter_editor, menu);
        menu.findItem(R.id.action_exec_query).setVisible(selfRequest == MainActivity.REQUEST_FILTER_ADD_NEW);
        menu.findItem(R.id.action_delete_query).setVisible(selfRequest == MainActivity.REQUEST_FILTER_EDIT);
        menu.findItem(R.id.action_save_query).setTitle(selfRequest == MainActivity.REQUEST_FILTER_EDIT ? R.string.save : R.string.save_n_exec);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(MainActivity.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    private void setFilterFromFields() {
        filter.condNumber = holder.number.getText().toString();
        filter.condUnit = holder.unit.getText().toString();
        filter.condRelease = holder.release8.getValue();
        filter.condApplication = holder.application.getText().toString();
        filter.condBuild = holder.build8.getValue();
        filter.condImExecutor = holder.imExecutor.isChecked();
        filter.condImInitiator = holder.imInitiator.isChecked();
        filter.condVersion = holder.version8.getValue();
        filter.condContent = holder.content.getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_query:
                new InputDialog(this,
                        getString(R.string.querys_name),
                        FilterOneActivity.this.filter.filter_name,
                        new InputDialog.ResultListener() {
                            @Override
                            public void onSetResult(boolean isPositive, String userInput) {
                                if (isPositive) {
                                    filter.filter_name = userInput;
                                    setFilterFromFields();
                                    filter.saveToServer(FilterOneActivity.this);
                                    mResultIntent = new Intent();
                                    //mResultIntent.putExtra(Filter.PARAM_FILTER_NAME, userInput);
                                    if (filter.filter_rn > 0)
                                        mResultIntent.putExtra(Filter.PARAM_FILTER_RN, filter.filter_rn);
                                    //setExtraResults();
                                    setResult(MainActivity.RESULT_FILTER_SAVE, mResultIntent);
                                    finish();
                                }
                            }
                        }
                );
                return true;
            case R.id.action_exec_query:
                setFilterFromFields();
                filter.saveToServer(FilterOneActivity.this);
                mResultIntent = new Intent();
                if (filter.filter_rn > 0)
                    mResultIntent.putExtra(Filter.PARAM_FILTER_RN, filter.filter_rn);
                //setExtraResults();
                setResult(MainActivity.RESULT_FILTER_EXEC, mResultIntent);
                finish();
                return true;
            case R.id.action_clear_query:
                filter.clear();
                setFromFilterValues();
                return true;
            case R.id.action_delete_query:
                filter.deleteOnServer(this);
                setResult(MainActivity.RESULT_FILTER_SAVE);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String onSetItemValue(MultiSpinner sender, String selected) {
        if (sender.getTagName().equals(SPINNER_BUILD_TAGNAME)) {
            return selected.substring(0, selected.indexOf("(") - 1);
        } else {
            return selected;
        }
    }

    @Override
    public void onValueChanged(MultiSpinner sender, String value) {
        if (sender.getTagName().equals(SPINNER_VERSION_TAGNAME)) {
            setupReleases();
        }
        if (sender.getTagName().equals(SPINNER_RELEASE_TAGNAME)) {
            setupBuilds();
        }

    }

    private class Holder {
        private final EditText number;
        private final MultiSpinner version8;
        private final MultiSpinner release8;
        private final MultiSpinner build8;
        private final MultiAutoCompleteTextView application;
        private final MultiAutoCompleteTextView unit;
        private final CheckBox imInitiator;
        private final CheckBox imExecutor;
        private final EditText content;

        public Holder() {
            this.number = (EditText) findViewById(R.id.feNumberEdit);
            this.version8 = (MultiSpinner) findViewById(R.id.feVersionSpinner);
            this.release8 = (MultiSpinner) findViewById(R.id.feReleaseSpinner);
            this.build8 = (MultiSpinner) findViewById(R.id.feBuildSpinner);
            this.application = (MultiAutoCompleteTextView) findViewById(R.id.feAppText);
            this.unit = (MultiAutoCompleteTextView) findViewById(R.id.feUnitsText);
            this.imInitiator = (CheckBox) findViewById(R.id.feImInit);
            this.imExecutor = (CheckBox) findViewById(R.id.feImExec);
            this.content = (EditText) findViewById(R.id.feContent);
        }
    }

}
