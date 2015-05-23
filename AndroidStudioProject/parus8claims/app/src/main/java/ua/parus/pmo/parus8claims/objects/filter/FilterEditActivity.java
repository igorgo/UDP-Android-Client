package ua.parus.pmo.parus8claims.objects.filter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import ua.parus.pmo.parus8claims.gui.MultiAutoCompleteTextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.CheckBox;
import ua.parus.pmo.parus8claims.gui.EditText;
import ua.parus.pmo.parus8claims.gui.MaterialDialogBuilder;
import ua.parus.pmo.parus8claims.gui.MultiSpinner;
import ua.parus.pmo.parus8claims.gui.ProgressWindow;
import ua.parus.pmo.parus8claims.gui.SemicolonTokenizer;
import ua.parus.pmo.parus8claims.objects.dicts.ApplistHelper;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;
import ua.parus.pmo.parus8claims.utils.Constants;


@SuppressWarnings("deprecation")
public class FilterEditActivity extends ActionBarActivity
        implements MultiSpinner.OnValueChangedListener {

    private static final String SPINNER_BUILD_TAGNAME = "build8";
    private static final String SPINNER_RELEASE_TAGNAME = "release8";
    private static final String SPINNER_VERSION_TAGNAME = "version8";
    @SuppressWarnings("unused")
    private static final String TAG = FilterEditActivity.class.getSimpleName();
    private Holder holder;
    private Intent resultIntent;
    private Filter filter;
    private int selfRequest;
    private FilterEditActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        instance = this;
        selfRequest = getIntent().getIntExtra(Constants.EXTRA_KEY_REQUEST, 0);
        setContentView(R.layout.activity_filter_editor);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.query_editor);
        }
        this.filter = new Filter();
        this.filter.filter_rn = getIntent().getLongExtra(Constants.EXTRA_KEY_RN, 0);
        new ReadTask().execute();
    }

    private void onAfterRead() {
        this.holder = new Holder();
        this.holder.version8.setEditable(true);
        this.holder.version8.setName(SPINNER_VERSION_TAGNAME);
        this.holder.version8.setOnValueChangedListener(this);
        this.holder.release8.setEditable(true);
        this.holder.release8.setName(SPINNER_RELEASE_TAGNAME);
        this.holder.release8.setOnValueChangedListener(this);
        this.holder.build8.setEditable(true);
        this.holder.build8.setName(SPINNER_BUILD_TAGNAME);
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
        new SetupBuildsTask().execute();
        setupApps();
        setupUnits();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter_editor, menu);
        menu.findItem(R.id.action_exec_query).setVisible(selfRequest == Constants.REQUEST_FILTER_ADD_NEW);
        menu.findItem(R.id.action_delete_query).setVisible(selfRequest == Constants.REQUEST_FILTER_EDIT);
        menu.findItem(R.id.action_save_query)
                .setTitle(selfRequest == Constants.REQUEST_FILTER_EDIT ? R.string.save : R.string.save_n_exec);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_CANCEL, intent);
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
                new MaterialDialogBuilder(this)
                        .input(getText(R.string.querys_name), this.filter.filter_name, false,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                        instance.filter.filter_name = charSequence.toString();
                                        setFilterFromFields();
                                        new SaveTask().execute(false);
                                    }
                                })
                        .negativeText(android.R.string.cancel)
                        .show();
                return true;
            case R.id.action_exec_query:
                setFilterFromFields();
                new SaveTask().execute(true);
                return true;
            case R.id.action_clear_query:
                this.filter.clear();
                setFromFilterValues();
                return true;
            case R.id.action_delete_query:
                new DeleteTask().execute();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onAfterSave(boolean onlyExec) {
        if (!onlyExec) {
            ((ClaimApplication) getApplication()).getFilters().addReplaceFilter(filter);
        }
        resultIntent = new Intent();
        if (filter.filter_rn > 0) {
            resultIntent.putExtra(Filter.PARAM_FILTER_RN, filter.filter_rn);
        }
        setResult(onlyExec ? Constants.RESULT_NEED_EXECUTE_FILTER : Constants.RESULT_NEED_SAVE_N_EXECUTE_FILTER, resultIntent);
        finish();
    }

    private void onAfterDelete() {
        ((ClaimApplication) getApplication()).getFilters().deleteFilter(this.filter);
        setResult(Constants.RESULT_NEED_SAVE_N_EXECUTE_FILTER);
        finish();
    }

    @Override
    public void onValueChanged(MultiSpinner sender, String value) {
        if (sender.getName().equals(SPINNER_VERSION_TAGNAME)) {
            setupReleases();
        }
        if (sender.getName().equals(SPINNER_RELEASE_TAGNAME)) {
            new SetupBuildsTask().execute();
        }

    }

    private class ReadTask extends AsyncTask<Void,Void,Void> {
        private ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pw.dismiss();
            onAfterRead();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            filter.readFromServer(instance);
            return null;
        }
    }

    private class SetupBuildsTask extends AsyncTask<Void,Void,Void> {
        private ProgressWindow pw;
        private List<String> dItems = new ArrayList<>();
        private List<String> vItems = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (holder.release8.isSingleSelected())
                pw = new ProgressWindow(instance);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (holder.release8.isSingleSelected()) {
                dItems = BuildHelper.getBuildsDisplayNames(instance, holder.release8.getValue(), true);
                vItems = BuildHelper.getBuildsCodes(instance, holder.release8.getValue(), true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (pw != null) {
                pw.dismiss();
            }
            holder.build8.setItems(
                    dItems, vItems, true
            );
            super.onPostExecute(aVoid);
        }
    }

    private class SaveTask extends AsyncTask<Boolean,Void,Boolean> {
        private ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            pw.dismiss();
            onAfterSave(aBoolean);
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            filter.saveToServer(instance);
            return params[0];
        }
    }

    private class DeleteTask extends AsyncTask<Void,Void,Void> {
        private ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pw.dismiss();
            onAfterDelete();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            filter.deleteOnServer(instance);
            return null;
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
