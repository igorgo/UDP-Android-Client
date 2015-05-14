package ua.parus.pmo.parus8claims;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.gui.AutoScrollListPageListener;
import ua.parus.pmo.parus8claims.gui.AutoScrollListView;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.objects.claim.ClaimActivity;
import ua.parus.pmo.parus8claims.objects.claim.ClaimListAdapter;
import ua.parus.pmo.parus8claims.objects.claim.actions.ClaimActionActivity;
import ua.parus.pmo.parus8claims.objects.dicts.Applists;
import ua.parus.pmo.parus8claims.objects.dicts.Releases;
import ua.parus.pmo.parus8claims.objects.dicts.Units;
import ua.parus.pmo.parus8claims.objects.filter.Filter;
import ua.parus.pmo.parus8claims.objects.filter.FilterOneActivity;
import ua.parus.pmo.parus8claims.objects.filter.FiltersActivity;
import ua.parus.pmo.parus8claims.rest.RestRequest;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
                                                               AutoScrollListPageListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_CONDITION = "CURRENT_CONDITION";
    private ClaimApplication application;
    private AutoScrollListView claimsListView;
    private Long currentConditionRn = null;
    private TextView emptyText;
    private LinearLayout progress;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            this.currentConditionRn = savedInstanceState.getLong(KEY_CONDITION, 0);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.pmo_logo);
            actionBar.setDisplayUseLogoEnabled(true);
        }
        this.claimsListView = (AutoScrollListView) findViewById(R.id.lvMaMyClaim);
        this.emptyText = (TextView) findViewById(R.id.nodata);
        this.progress = (LinearLayout) findViewById(R.id.progress_container);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.claimsListView.setLoadingView(layoutInflater.inflate(R.layout.list_item_loading, null));
        this.claimsListView.setOnItemClickListener(this);
        this.application = (ClaimApplication) this.getApplication();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ProgressDialog connectDialog = ProgressDialog
                .show(MainActivity.this, getString(R.string.please_wait), getString(R.string.connect_to_server), true);
        boolean rc = true;
        if (application.isNotCacheRefreched()) {
            try {
                rc = false;
                Releases.RefreshCache(MainActivity.this);
                rc = true;
            } catch (ConnectException e) {
                e.printStackTrace();
            }
            if (connectDialog.isShowing()) connectDialog.dismiss();
            if (rc) {
                Units.checkCache(MainActivity.this);
                Applists.checkCache(MainActivity.this);
                application.setCacheRefreched();
            }
        }
        if (connectDialog.isShowing()) connectDialog.dismiss();
        if (rc) {
            Log.d(TAG, "Call login to UDP");
            loginToUdp();
        } else {
            ErrorPopup errorPopup = new ErrorPopup(MainActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            });
            errorPopup.showErrorDialog(getString(R.string.error_title), getString(R.string.server_unreachable));
        }

    }

    private void getClaims(Long cond, Long newrn) {
        try {
            Log.d(TAG, "Load Claims From Inet");
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
            Log.d(TAG, "Session not set.");
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String user = sharedPrefs.getString(SettingsActivity.PREF_USERNAME, "");
            String pass = sharedPrefs.getString(SettingsActivity.PREF_PASSWORD, "");

            if ((user == null) || (pass == null) || (user.isEmpty()) || (pass).isEmpty()) {
                Log.i(TAG, "Some preferences not set.");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Intents.REQUEST_SETTINGS);
                return;
            }
            new LoginAsyncTask().execute(user, pass, null);
        } else {
            Log.d(TAG, "Session are set yet. Call next process.");
            this.getClaims(this.currentConditionRn, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Intents.REQUEST_SETTINGS:
                Log.d(TAG, "It's from SettingsActivity. Call loginToUdp.");
                this.loginToUdp();
                break;
            case Intents.REQUEST_FILTERS_VIEW:
                Log.d(TAG, "It's from FiltersActivity.");
                switch (resultCode) {
                    case Intents.RESULT_NEED_ADD_NEW_FILTER:
                        Log.d(TAG, "Request for new query has been received.");
                        Intent intentAddFilter = new Intent(this, FilterOneActivity.class);
                        intentAddFilter.putExtra(Intents.EXTRA_KEY_REQUEST,
                                Intents.REQUEST_FILTER_ADD_NEW);
                        startActivityForResult(intentAddFilter, Intents.REQUEST_FILTER_ADD_NEW);
                        break;
                    case Intents.RESULT_FILTER_SELECTED:
                        Log.d(TAG, "Request for existing query has been received.");
                        this.currentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (this.currentConditionRn == 0) this.currentConditionRn = null;
                        this.getClaims(this.currentConditionRn, null);
                        break;
                }
                break;
            case Intents.REQUEST_FILTER_ADD_NEW:
                Log.d(TAG, "It's from FilterEditorActivity.");
                switch (resultCode) {
                    case Intents.RESULT_NEED_SAVE_N_EXECUTE_FILTER:
                    case Intents.RESULT_NEED_EXECUTE_FILTER:
                        Log.d(TAG, "Request for save and exec query has been received.");
                        this.currentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (this.currentConditionRn == 0) this.currentConditionRn = null;
                        this.getClaims(this.currentConditionRn, null);
                        break;
                }
            case Intents.REQUEST_CLAIM_ADD:
                if (resultCode == Intents.RESULT_CLAIM_ADDED) {
                    Long newrn = data.getLongExtra(Intents.EXTRA_KEY_RN, 0);
                    if (newrn != 0) {
                        this.getClaims(this.currentConditionRn, newrn);
                    }
                }
                break;
            case Intents.REQUEST_CLAIM_VIEW:
                if (resultCode == Intents.RESULT_CLAIM_DELETED) {
                    this.getClaims(this.currentConditionRn, null);
                }
                if (resultCode == Intents.RESULT_CANCEL) {
                    int listPos = data.getIntExtra(Intents.EXTRA_KEY_CLAIM_LIST_POS, -1);
                    if (listPos > -1) {
                        Claim claim = (Claim) data.getSerializableExtra(Intents.EXTRA_KEY_CLAIM);
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
                Log.d(TAG, "The «Settings» menu item selected");
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(intentSettings, Intents.REQUEST_SETTINGS);
                return true;
            case R.id.action_search:
                Log.d(TAG, "The «Search» menu item selected");
                Intent intentSearch = new Intent(this, FiltersActivity.class);
                startActivityForResult(intentSearch, Intents.REQUEST_FILTERS_VIEW);
                return true;
            case R.id.action_refresh:
                Log.d(TAG, "The «Refresh» menu item selected");
                this.getClaims(this.currentConditionRn, null);
                return true;
            case R.id.action_add_claim:
                Log.d(TAG, "The «Search» menu item selected");
                Intent intentAdd = new Intent(this, ClaimActionActivity.class);
                intentAdd.putExtra(Intents.EXTRA_KEY_CLAIM, new Claim());
                intentAdd.putExtra(Intents.EXTRA_KEY_REQUEST, Intents.REQUEST_CLAIM_ADD);
                intentAdd.putExtra(Intents.EXTRA_KEY_SESSION, this.application.getSessionId());
                startActivityForResult(intentAdd, Intents.REQUEST_CLAIM_ADD);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Claim claim = (Claim) adapterView.getAdapter().getItem(i);
        long rn = claim.rn;
        if (rn > 0) {
            Intent intentViewClaim = new Intent(this, ClaimActivity.class);
            intentViewClaim.putExtra(Intents.EXTRA_KEY_RN, rn);
            intentViewClaim.putExtra(Intents.EXTRA_KEY_CLAIM_LIST_POS, i);
            intentViewClaim.putExtra(Intents.EXTRA_KEY_HAS_DOCS, claim.hasAttach);
            startActivityForResult(intentViewClaim, Intents.REQUEST_CLAIM_VIEW);
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
            Log.d(TAG, "List is empty");
            this.progress.setVisibility(View.GONE);
            this.claimsListView.setVisibility(View.GONE);
            this.emptyText.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "List not empty");
            this.progress.setVisibility(View.GONE);
            this.claimsListView.setVisibility(View.VISIBLE);
            this.emptyText.setVisibility(View.GONE);
        }
    }


    private class LoginAsyncTask extends AsyncTask<String, Void, JSONObject> {
        public static final String REST_URL = "login/";
        public static final String HTTP_METHOD = "POST";
        public static final String REST_PARAM_USER = "user";
        public static final String REST_PARAM_PASSWORD = "pass";
        public static final String REST_RESPONSE_FIELD_ERROR = "ERROR";
        public static final String REST_RESPONSE_FIELD_SESSION_ID = "SESSONID";
        public static final String REST_RESPONSE_FIELD_PMO_FLAG = "PPP";
        final MainActivity that = MainActivity.this;
        final ProgressDialog loadDialog = ProgressDialog
                .show(MainActivity.this, getString(R.string.please_wait), getString(R.string.authorizing), true);

        @Override
        protected JSONObject doInBackground(String... params) {
            loadDialog.show();
            JSONObject response = null;
            RestRequest loginRequest;
            try {
                loginRequest = new RestRequest(REST_URL, HTTP_METHOD);
                loginRequest.addInParam(REST_PARAM_USER, params[0]);
                loginRequest.addInParam(REST_PARAM_PASSWORD, params[1]);

                response = loginRequest.getJsonContent();
            } catch (MalformedURLException | ConnectException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            loadDialog.cancel();
            if (response == null) {
                ErrorPopup errorPopup = new ErrorPopup(MainActivity.this, null);
                errorPopup.showErrorDialog(getString(R.string.error_title), getString(R.string.connection_time_out));
                return;
            }
            String error = response.optString(REST_RESPONSE_FIELD_ERROR);
            if (!TextUtils.isEmpty(error)) {
                ErrorPopup errorPopup = new ErrorPopup(MainActivity.this, null);
                errorPopup.showErrorDialog(getString(R.string.error_title), error);
                return;
            } else {
                that.application.setSessionId(response.optString(REST_RESPONSE_FIELD_SESSION_ID));
                that.application.setPmoUser(response.optInt(REST_RESPONSE_FIELD_PMO_FLAG) == 1);
            }
            that.getClaims(that.currentConditionRn, null);
        }
    }
}
