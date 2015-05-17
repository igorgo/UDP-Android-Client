package ua.parus.pmo.parus8claims.objects.filter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.Intents;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.InputDialog;
import ua.parus.pmo.parus8claims.gui.MultiSpinner;
import ua.parus.pmo.parus8claims.gui.SemicolonTokenizer;
import ua.parus.pmo.parus8claims.objects.dicts.ApplistHelper;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;


@SuppressWarnings("deprecation")
public class FilterEditActivity extends ActionBarActivity
        implements MultiSpinner.OnSetItemValueListener,
        MultiSpinner.OnValueChangedListener {

    private static final String SPINNER_BUILD_TAGNAME = "build8";
    private static final String SPINNER_RELEASE_TAGNAME = "release8";
    private static final String SPINNER_VERSION_TAGNAME = "version8";
    @SuppressWarnings("unused")
    private static final String TAG = FilterEditActivity.class.getSimpleName();
    private Holder holder;
    private Intent resultIntent;
    private Filter filter;
    private int selfRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        selfRequest = getIntent().getIntExtra(Intents.EXTRA_KEY_REQUEST, 0);
        setContentView(R.layout.activity_filter_editor);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.query_editor);
        }
        this.filter = new Filter();
        this.filter.filter_rn = getIntent().getLongExtra(Intents.EXTRA_KEY_RN, 0);
        this.filter.readFromServer(this);
        this.holder = new Holder();
        this.holder.version8.setEditable(true);
        this.holder.version8.setName(SPINNER_VERSION_TAGNAME);
        this.holder.version8.setOnValueChangedListener(this);
        this.holder.release8.setEditable(true);
        this.holder.release8.setName(SPINNER_RELEASE_TAGNAME);
        this.holder.release8.setOnValueChangedListener(this);
        this.holder.build8.setEditable(true);
        this.holder.build8.setName(SPINNER_BUILD_TAGNAME);
        this.holder.build8.setOnSetItemValueListener(this);
        setAdapters();
        setFromFilterValues();
        this.holder.number.requestFocus();

    }

    private void setFromFilterValues() {
        this.holder.number.setText(this.filter.condNumber);
        this.holder.version8.setValue(this.filter.condVersion);
        this.holder.release8.setValue(this.filter.condRelease);
        this.holder.build8.setValue(this.filter.condBuild);
        this.holder.application.setText(this.filter.condApplication);
        this.holder.unit.setText(this.filter.condUnit);
        this.holder.imInitiator.setChecked(this.filter.condImInitiator);
        this.holder.imExecutor.setChecked(this.filter.condImExecutor);
        this.holder.content.setText(this.filter.condContent);
    }

    private void setupVersions() {
        this.holder.version8.setItems(
                ReleaseHelper.getVersions(this, true, ""),
                true
        );

    }

    private void setupReleases() {
        List<String> items = new ArrayList<>();
        if (this.holder.version8.isSingleSelected()) {
            items = ReleaseHelper.getReleasesNames(this, holder.version8.getValue(), true, null);
        }
        this.holder.release8.setItems(
                items, true
        );
    }

    private void setupBuilds() {
        List<String> items = new ArrayList<>();
        if (this.holder.release8.isSingleSelected()) {
            items = BuildHelper.getBuildsDisplayNames(this, holder.release8.getValue(), true);
        }
        this.holder.build8.setItems(
                items, true
        );
    }

    private void setupApps() {
        this.holder.application.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_multiline_item,
                        R.id.item,
                        ApplistHelper.getAppsAll(this)
                )
        );
        this.holder.application.setTokenizer(new SemicolonTokenizer());
    }

    private void setupUnits() {
        this.holder.unit.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_multiline_item,
                        R.id.item,
                        UnitHelper.getUnits(this)
                )
        );
        this.holder.unit.setTokenizer(new SemicolonTokenizer());
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
        getMenuInflater().inflate(R.menu.menu_filter_editor, menu);
        menu.findItem(R.id.action_exec_query).setVisible(selfRequest == Intents.REQUEST_FILTER_ADD_NEW);
        menu.findItem(R.id.action_delete_query).setVisible(selfRequest == Intents.REQUEST_FILTER_EDIT);
        menu.findItem(R.id.action_save_query)
                .setTitle(selfRequest == Intents.REQUEST_FILTER_EDIT ? R.string.save : R.string.save_n_exec);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Intents.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    private void setFilterFromFields() {
        this.filter.condNumber = this.holder.number.getText().toString();
        this.filter.condUnit = this.holder.unit.getText().toString();
        this.filter.condRelease = this.holder.release8.getValue();
        this.filter.condApplication = this.holder.application.getText().toString();
        this.filter.condBuild = this.holder.build8.getValue();
        this.filter.condImExecutor = this.holder.imExecutor.isChecked();
        this.filter.condImInitiator = this.holder.imInitiator.isChecked();
        this.filter.condVersion = this.holder.version8.getValue();
        this.filter.condContent = this.holder.content.getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_query:
                new InputDialog(this,
                        getString(R.string.querys_name),
                        this.filter.filter_name,
                        new InputDialog.ResultListener() {
                            @Override
                            public void onSetResult(boolean isPositive, String userInput) {
                                FilterEditActivity that = FilterEditActivity.this;
                                if (isPositive) {
                                    that.filter.filter_name = userInput;
                                    setFilterFromFields();
                                    that.filter.saveToServer(that);
                                    ((ClaimApplication) that.getApplication()).getFilters().addReplaceFilter(that.filter);
                                    that.resultIntent = new Intent();
                                    //resultIntent.putExtra(Filter.PARAM_FILTER_NAME, userInput);
                                    if (that.filter.filter_rn > 0) {
                                        that.resultIntent.putExtra(Filter.PARAM_FILTER_RN, that.filter.filter_rn);
                                    }
                                    //setExtraResults();
                                    setResult(Intents.RESULT_NEED_SAVE_N_EXECUTE_FILTER, that.resultIntent);
                                    finish();
                                }
                            }
                        }
                );
                return true;
            case R.id.action_exec_query:
                setFilterFromFields();
                this.filter.saveToServer(this);
                this.resultIntent = new Intent();
                if (this.filter.filter_rn > 0) {
                    this.resultIntent.putExtra(Filter.PARAM_FILTER_RN, this.filter.filter_rn);
                }
                setResult(Intents.RESULT_NEED_EXECUTE_FILTER, this.resultIntent);
                finish();
                return true;
            case R.id.action_clear_query:
                this.filter.clear();
                setFromFilterValues();
                return true;
            case R.id.action_delete_query:
                this.filter.deleteOnServer(this);
                ((ClaimApplication) getApplication()).getFilters().deleteFilter(this.filter);
                setResult(Intents.RESULT_NEED_SAVE_N_EXECUTE_FILTER);
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String onSetItemValue(MultiSpinner sender, String selected) {
        if (sender.getName().equals(SPINNER_BUILD_TAGNAME)) {
            return selected.substring(0, selected.indexOf("(") - 1);
        } else {
            return selected;
        }
    }

    @Override
    public void onValueChanged(MultiSpinner sender, String value) {
        if (sender.getName().equals(SPINNER_VERSION_TAGNAME)) {
            setupReleases();
        }
        if (sender.getName().equals(SPINNER_RELEASE_TAGNAME)) {
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
