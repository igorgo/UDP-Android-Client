package ua.parus.pmo.parus8claims;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.db.DatabaseWrapper;
import ua.parus.pmo.parus8claims.gui.AutoScrollListPageListener;
import ua.parus.pmo.parus8claims.gui.AutoScrollListView;
import ua.parus.pmo.parus8claims.gui.EditText;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.gui.MaterialDialogBuilder;
import ua.parus.pmo.parus8claims.gui.ProgressWindow;
import ua.parus.pmo.parus8claims.gui.TextView;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.objects.claim.ClaimActivity;
import ua.parus.pmo.parus8claims.objects.claim.ClaimListAdapter;
import ua.parus.pmo.parus8claims.objects.claim.actions.ClaimActionActivity;
import ua.parus.pmo.parus8claims.objects.dicts.ApplistHelper;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;
import ua.parus.pmo.parus8claims.objects.filter.Filter;
import ua.parus.pmo.parus8claims.objects.filter.FilterEditActivity;
import ua.parus.pmo.parus8claims.objects.filter.FiltersActivity;
import ua.parus.pmo.parus8claims.rest.RestRequest;
import ua.parus.pmo.parus8claims.utils.Constants;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        AutoScrollListPageListener, Handler.Callback {

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_CONDITION = "CURRENT_CONDITION";
    private static final int MSG_CONNECT_ERROR = -1;
    private static final int MSG_RELEASES_HAVE_REFRESHED = 1;
    private static final int MSG_DICTIONARIES_HAVE_CACHED = 2;
    private static final int MSG_AUTH_ERROR = 3;
    private static final int MSG_LOGGED = 4;
    private Handler handler;
    private ClaimApplication application;
    private AutoScrollListView claimsListView;
    private Long currentConditionRn = null;
    private TextView emptyText;
    private FloatingActionsMenu fab;
    private LinearLayout progress;
    private MainActivity instance;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        handler = new Handler(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            this.currentConditionRn = savedInstanceState.getLong(KEY_CONDITION, 0);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setTitle(R.string.claims);
        }
        this.claimsListView = (AutoScrollListView) findViewById(R.id.lvMaMyClaim);
        this.emptyText = (TextView) findViewById(R.id.nodata);
        this.progress = (LinearLayout) findViewById(R.id.progress_container);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.claimsListView.setLoadingView(layoutInflater.inflate(R.layout.list_item_loading, null));
        this.claimsListView.setOnItemClickListener(this);
        this.application = (ClaimApplication) this.getApplication();
        initFab();
        if (isCredentialsSet()) {
            cacheRelease();
        } else {
            LayoutInflater inflater = getLayoutInflater();
            final View v = inflater.inflate(R.layout.login_dialog, null);
            final EditText passwordEditText = (EditText) v.findViewById(R.id.password_request);
            final EditText usernameEditText = (EditText) v.findViewById(R.id.username_request);

            MaterialDialog dialog = new MaterialDialogBuilder(this)
                    .autoDismiss(false)
                    .title(R.string.pref_header_account)
                    .customView(v, false)
                    .positiveText(android.R.string.ok)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override public void onNeutral(MaterialDialog dialog) {
                            prefs.edit().putString(Constants.PREF_USERNAME, "guest")
                                 .commit();
                            prefs.edit().putString(Constants.PREF_PASSWORD, "guest")
                                 .commit();
                            if (isCredentialsSet())
                                super.onPositive(dialog);
                            dialog.dismiss();
                            cacheRelease();
                        }

                        @Override public void onPositive(MaterialDialog dialog) {
                            prefs.edit().putString(Constants.PREF_USERNAME, usernameEditText.getText().toString())
                                 .commit();
                            prefs.edit().putString(Constants.PREF_PASSWORD, passwordEditText.getText().toString())
                                 .commit();
                            if (isCredentialsSet())
                                super.onPositive(dialog);
                            dialog.dismiss();
                            cacheRelease();


                        }
                    })
                    .cancelable(false)
                    .neutralText(R.string.guest_enter)
                    .build();

            dialog.show();
        }
    }
    private void initFab() {
        AddFloatingActionButton fabAddButton = (AddFloatingActionButton) findViewById(R.id.fab);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    addClaim();
            }
        });
    }

    private void cacheRelease() {
        if (application.isNotCacheRefreshed()) {
            new ReleasesCashRefresher().execute();
        } else {
            handler.sendEmptyMessage(MSG_RELEASES_HAVE_REFRESHED);
        }
    }

    private void cacheDictionaries(boolean force) {
        if (!force && UnitHelper.isUnitsCached(this)) {
            handler.sendEmptyMessage(MSG_DICTIONARIES_HAVE_CACHED);
        } else {
            new DictionariesCashRefresher().execute();
        }

    }


    private void getClaims(Long cond, Long newrn) {
        try {
            claimsListView.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            ClaimListAdapter claimListAdapter = new ClaimListAdapter(this, cond, newrn);
            this.claimsListView.setAdapter(claimListAdapter);
            claimListAdapter.setAutoScrollListPageListener(this);
            claimListAdapter.onScrollNext();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void loginToUdp() {
        if (this.application.getSessionId() == null) {
            new LoginAsyncTask().execute();
        } else {
            handler.sendEmptyMessage(MSG_LOGGED);
        }
    }

    public boolean isCredentialsSet() {
        return !TextUtils.isEmpty(prefs.getString(Constants.PREF_USERNAME, null))
               && !TextUtils.isEmpty(prefs.getString(Constants.PREF_PASSWORD, null));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_SETTINGS:
                switch (resultCode) {
                    case Constants.RESULT_CANCEL:
                        cacheRelease();
                        break;
                    case Constants.RESULT_NEED_REFRESH_DICTIONARIES_CACHE:
                        cacheDictionaries(true);
                        break;
                }
                break;
            case Constants.REQUEST_FILTERS_VIEW:
                switch (resultCode) {
                    case Constants.RESULT_NEED_ADD_NEW_FILTER:
                        Intent intentAddFilter = new Intent(this, FilterEditActivity.class);
                        intentAddFilter.putExtra(Constants.EXTRA_KEY_REQUEST,
                                Constants.REQUEST_FILTER_ADD_NEW);
                        startActivityForResult(intentAddFilter, Constants.REQUEST_FILTER_ADD_NEW);
                        break;
                    case Constants.RESULT_FILTER_SELECTED:
                        this.currentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (this.currentConditionRn == 0) this.currentConditionRn = null;
                        this.getClaims(this.currentConditionRn, null);
                        break;
                }
                break;
            case Constants.REQUEST_FILTER_ADD_NEW:
                switch (resultCode) {
                    case Constants.RESULT_NEED_SAVE_N_EXECUTE_FILTER:
                    case Constants.RESULT_NEED_EXECUTE_FILTER:
                        this.currentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (this.currentConditionRn == 0) this.currentConditionRn = null;
                        this.getClaims(this.currentConditionRn, null);
                        break;
                }
            case Constants.REQUEST_CLAIM_ADD:
                if (resultCode == Constants.RESULT_CLAIM_ADDED) {
                    Long newrn = data.getLongExtra(Constants.EXTRA_KEY_RN, 0);
                    if (newrn != 0) {
                        this.getClaims(this.currentConditionRn, newrn);
                    }
                }
                break;
            case Constants.REQUEST_CLAIM_VIEW:
                if (resultCode == Constants.RESULT_CLAIM_DELETED) {
                    this.getClaims(this.currentConditionRn, null);
                }
                if (resultCode == Constants.RESULT_CANCEL) {
                    int listPos = data.getIntExtra(Constants.EXTRA_KEY_CLAIM_LIST_POS, -1);
                    if (listPos > -1) {
                        Claim claim = (Claim) data.getSerializableExtra(Constants.EXTRA_KEY_CLAIM);
                        Claim oldClaim = (Claim) claimsListView.getAdapter().getItem(listPos);
                        if (claim.releaseFix != null) {
                            if (claim.buildFix != null) {
                                oldClaim.releaseDisplayed = claim.buildFix.displayName;
                            } else {
                                oldClaim.releaseDisplayed = claim.releaseFix.name;
                            }
                        } else {
                            if (claim.releaseFound != null) {
                                if (claim.buildFound != null) {
                                    oldClaim.releaseDisplayed = claim.buildFound.displayName;
                                } else {
                                    oldClaim.releaseDisplayed = claim.releaseFound.name;
                                }
                            }
                        }
                        oldClaim.hasReleaseFix = claim.buildFix != null;
                        oldClaim.unit = claim.unit;
                        oldClaim.state = claim.state;
                        oldClaim.stateType = claim.stateType;
                        oldClaim.description = claim.description;
                        oldClaim.hasAttach = claim.hasAttach;
                        oldClaim.priority = claim.priority;
                        oldClaim.executorType = claim.executorType;
                        oldClaim.executor = claim.executor;
                        oldClaim.changeDate = claim.changeDate;
                        ((ClaimListAdapter) ((HeaderViewListAdapter) claimsListView.getAdapter()).getWrappedAdapter())
                                .setItem(listPos, oldClaim);
                        View row = claimsListView.getChildAt(listPos - claimsListView.getFirstVisiblePosition());
                        ClaimListAdapter.ClaimHolder holder = new ClaimListAdapter.ClaimHolder();
                        ClaimListAdapter.initHolder(holder, row);
                        ClaimListAdapter.populateHolder(this, holder, oldClaim);
                    }
                }
                break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(intentSettings, Constants.REQUEST_SETTINGS);
                return true;
            case R.id.action_search:
                Intent intentSearch = new Intent(this, FiltersActivity.class);
                startActivityForResult(intentSearch, Constants.REQUEST_FILTERS_VIEW);
                return true;
            case R.id.action_refresh:
                this.getClaims(this.currentConditionRn, null);
                return true;
            /*case R.id.action_add_claim:
                addClaim();
                return true;*/

        }
        return super.onOptionsItemSelected(item);
    }

    private void addClaim() {
        Intent intentAdd = new Intent(this, ClaimActionActivity.class);
        intentAdd.putExtra(Constants.EXTRA_KEY_CLAIM, new Claim());
        intentAdd.putExtra(Constants.EXTRA_KEY_REQUEST, Constants.REQUEST_CLAIM_ADD);
        intentAdd.putExtra(Constants.EXTRA_KEY_SESSION, this.application.getSessionId());
        startActivityForResult(intentAdd, Constants.REQUEST_CLAIM_ADD);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Claim claim = (Claim) adapterView.getAdapter().getItem(i);
        long rn = claim.rn;
        if (rn > 0) {
            Intent intentViewClaim = new Intent(this, ClaimActivity.class);
            intentViewClaim.putExtra(Constants.EXTRA_KEY_RN, rn);
            intentViewClaim.putExtra(Constants.EXTRA_KEY_CLAIM_LIST_POS, i);
            intentViewClaim.putExtra(Constants.EXTRA_KEY_HAS_DOCS, claim.hasAttach);
            startActivityForResult(intentViewClaim, Constants.REQUEST_CLAIM_VIEW);
        }
    }

    @Override
    public void onListEnd() {
        this.claimsListView.onListEnd();
    }

    @Override
    public void onHasMore() {
        this.claimsListView.onHasMore();
    }

    @Override
    public void onEmptyList(boolean empty) {
        this.claimsListView.onEmptyList(empty);
        if (empty) {
            this.progress.setVisibility(View.GONE);
            this.claimsListView.setVisibility(View.GONE);
            this.emptyText.setVisibility(View.VISIBLE);
        } else {
            this.progress.setVisibility(View.GONE);
            this.claimsListView.setVisibility(View.VISIBLE);
            this.emptyText.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CONNECT_ERROR:
                finish();
                System.exit(0);
                break;
            case MSG_RELEASES_HAVE_REFRESHED:
                cacheDictionaries(false);
                break;
            case MSG_DICTIONARIES_HAVE_CACHED:
                loginToUdp();
                break;
            case MSG_LOGGED:
                getClaims(currentConditionRn, null);
                break;
        }
        return false;
    }


    private class LoginAsyncTask extends AsyncTask<Void, Void, Integer> {
        public static final String REST_URL = "login/";
        public static final String HTTP_METHOD = "POST";
        public static final String REST_PARAM_USER = "user";
        public static final String REST_PARAM_PASSWORD = "pass";
        public static final String REST_RESPONSE_FIELD_ERROR = "ERROR";
        public static final String REST_RESPONSE_FIELD_SESSION_ID = "SESSONID";
        public static final String REST_RESPONSE_FIELD_PMO_FLAG = "PPP";
        String error;
        ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance,R.string.authorizing);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String user = prefs.getString(Constants.PREF_USERNAME, "");
                String pass = prefs.getString(Constants.PREF_PASSWORD, "");
                JSONObject response;
                RestRequest loginRequest;
                loginRequest = new RestRequest(REST_URL, HTTP_METHOD);
                loginRequest.addInParam(REST_PARAM_USER, user);
                loginRequest.addInParam(REST_PARAM_PASSWORD, pass);

                response = loginRequest.getJsonContent();
                if (response == null) {
                    error = getString(R.string.connection_time_out);
                    return -1;
                }
                String serverError = response.optString(REST_RESPONSE_FIELD_ERROR);
                if (!TextUtils.isEmpty(serverError)) {
                    error = serverError;
                    return -1;
                }
                instance.application.setSessionId(response.optString(REST_RESPONSE_FIELD_SESSION_ID));
                instance.application.setPmoUser(response.optInt(REST_RESPONSE_FIELD_PMO_FLAG) == 1);
            } catch (MalformedURLException | ConnectException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(instance, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        handler.sendEmptyMessage(MSG_AUTH_ERROR);
                        super.onPositive(dialog);
                    }
                }).showErrorDialog(null, TextUtils.isEmpty(error) ? getString(R.string.server_unreachable) : error);
            } else {
                handler.sendEmptyMessage(MSG_LOGGED);
            }
            super.onPostExecute(result);
        }
    }

    private class ReleasesCashRefresher extends AsyncTask<Void, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance,R.string.loading_releases);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(instance);
            SQLiteDatabase db = databaseWrapper.getWritableDatabase();
            try {
                RestRequest restRequest = new RestRequest("dicts/releases/");
                JSONArray response = restRequest.getAllRows();
                if (response != null) {
                    db.delete(ReleaseHelper.TABLE_NAME, null, null);
                    db.delete(BuildHelper.TABLE_NAME, null, null);
                    db.beginTransaction();
                    try {
                        SQLiteStatement statement = db.compileStatement(ReleaseHelper.SQL_INSERT);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject c = response.getJSONObject(i);
                            statement.bindLong(1, c.getLong("rn"));
                            statement.bindString(2, c.getString("v"));
                            statement.bindString(3, c.getString("r"));
                            statement.execute();
                            statement.clearBindings();
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
            } catch (MalformedURLException | ConnectException | JSONException e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
                return -1;
            } finally {
                db.close();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(instance, new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        handler.sendEmptyMessage(MSG_CONNECT_ERROR);
                        super.onPositive(dialog);
                    }
                }).showErrorDialog(null, TextUtils.isEmpty(error) ? getString(R.string.server_unreachable) : error);
            } else {
                application.setCacheRefreshed();
                handler.sendEmptyMessage(MSG_RELEASES_HAVE_REFRESHED);
            }
            super.onPostExecute(result);
        }
    }

    private class DictionariesCashRefresher extends AsyncTask<Void, String, Integer> {
        String error;
        ProgressWindow pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pw = new ProgressWindow(instance,R.string.loading_unitlist);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            publishProgress(getString(R.string.loading_unitlist));
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(instance);
            SQLiteDatabase db = databaseWrapper.getWritableDatabase();
            try {
                RestRequest restRequest = new RestRequest(UnitHelper.URL_UNITS);
                JSONArray response = restRequest.getAllRows();
                if (response != null) {
                    db.delete(UnitHelper.TABLE_NAME, null, null);
                    db.delete(UnitHelper.UnitApplists.TABLE_NAME, null, null);
                    db.delete(UnitHelper.UnitFuncs.TABLE_NAME, null, null);
                    db.beginTransaction();
                    try {
                        SQLiteStatement statement = db.compileStatement(UnitHelper.SQL_INSERT);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject c = response.getJSONObject(i);
                            statement.bindString(1, c.getString("n"));
                            statement.execute();
                            statement.clearBindings();
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
                publishProgress(getString(R.string.loading_applist));
                restRequest = new RestRequest(ApplistHelper.REST_URL);
                JSONArray items = restRequest.getAllRows();
                if (items != null) {
                    db.delete(ApplistHelper.TABLE_NAME, null, null);
                    db.beginTransaction();
                    try {
                        SQLiteStatement statement = db.compileStatement(ApplistHelper.SQL_INSERT);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            statement.bindString(1, item.getString(ApplistHelper.FIELD_NAME));
                            statement.execute();
                            statement.clearBindings();
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
            } catch (MalformedURLException | ConnectException | JSONException e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
                return -1;
            } finally {
                db.close();
            }
            return 0;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            if (!TextUtils.isEmpty(values[0])) {
                pw.setMessage(values[0]);
            }
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(instance, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        handler.sendEmptyMessage(MSG_CONNECT_ERROR);
                        super.onPositive(dialog);
                    }
                }).showErrorDialog(null, TextUtils.isEmpty(error) ? getString(R.string.server_unreachable) : error);
            } else {
                handler.sendEmptyMessage(MSG_DICTIONARIES_HAVE_CACHED);
            }
            super.onPostExecute(result);
        }
    }


}
