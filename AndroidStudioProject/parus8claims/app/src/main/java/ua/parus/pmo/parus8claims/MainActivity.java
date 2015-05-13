package ua.parus.pmo.parus8claims;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.db.ApplistORM;
import ua.parus.pmo.parus8claims.db.ReleasesORM;
import ua.parus.pmo.parus8claims.db.UnitsORM;
import ua.parus.pmo.parus8claims.gui.AutoScrollListView;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.om.claim.Claim;
import ua.parus.pmo.parus8claims.om.claim.ClaimListAdapter;
import ua.parus.pmo.parus8claims.om.filter.Filter;
import ua.parus.pmo.parus8claims.rest.RestRequest;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {


    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_FILTERS = 2;
    public static final int REQUEST_FILTER_ADD_NEW = 3;
    public static final int REQUEST_FILTER_EDIT = 4;
    private static final int REQUEST_CLAIM_VIEW = 5;
    public static final int RESULT_CANCEL = 1;
    public static final int RESULT_FILTERS_SELECT = 2;
    public static final int RESULT_FILTERS_ADD_NEW = 3;
    public static final int RESULT_FILTER_SAVE = 4;
    public static final int RESULT_FILTER_EXEC = 5;
    //public static final String SELF_REQUEST_EXTRA = "self-request";
    private static final String KEY_CONDITION = "CURRENT_CONDITION";
    private static final String TAG = "MainActivity";
    //public static final int RESULT_FILTER_EDIT_OK = 5;
    private static final String NEXT_PROC1 = "GET_MY";
    private ClaimApplication mApplication;
    private AutoScrollListView mClaimsListView;
    private Long mCurrentConditionRn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "OnCreate started...");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.i(TAG, "Setting content layout");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentConditionRn = savedInstanceState.getLong(KEY_CONDITION, 0);
        }


        Log.i(TAG, "Adding Logo to action bar");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.pmo_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mClaimsListView = (AutoScrollListView) findViewById(R.id.lvMaMyClaim);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClaimsListView.setLoadingView(layoutInflater.inflate(R.layout.loading_view_row, null));
        mClaimsListView.setOnItemClickListener(this);


        mApplication = (ClaimApplication) this.getApplication();
        if (!mApplication.isCacheRefreched()) {
            ReleasesORM.RefreshCache(this);
            UnitsORM.checkCache(this);
            ApplistORM.checkCache(this);
            mApplication.setCacheRefreched();
        }

        Log.i(TAG, "Call login to UDP");
        loginUdp();
        //getMyClaims();

    }


    void getMyClaims(Long cond, Long newrn) {
        Log.i(TAG, "getMyClaims started...");
        //ListView lvMaMyClaim = (ListView) findViewById(R.id.lvMaMyClaim);
        if (mApplication.claimsIsCached()) {
            Log.i(TAG, "Load Claims From Cache");
            ClaimListAdapter lClaimAdapter = mApplication.getClaimCache();
            mClaimsListView.setAdapter(lClaimAdapter);
        } else {
            try {
                Log.i(TAG, "Load Claims From Inet");
                ClaimListAdapter lClaimAdapter = new ClaimListAdapter(this, cond, newrn);
                mClaimsListView.setAdapter(lClaimAdapter);
                lClaimAdapter.onScrollNext();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loginUdp() {
        Log.i(TAG, "loginUdp started");
        if (mApplication.getSessionId() == null) {
            Log.i(TAG, "Session not set.");
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            Log.i(TAG, "Read preferences.");
            if ((sharedPrefs.getString("username", null) == null) || (sharedPrefs.getString("password", null) == null)
                    || (sharedPrefs.getString("username", null).isEmpty()) || (sharedPrefs.getString("password", null).isEmpty())
                    ) {
                Log.i(TAG, "Some preferences not set.");
                Intent i = new Intent(this, SettingsActivity.class);
                Log.i(TAG, "Intent to start SettingsActivity (reguest REQUEST_SETTINGS).");
                startActivityForResult(i, REQUEST_SETTINGS);
                return;
            }
            Log.i(TAG, "All preferences are set. Call async Login.");
            new AsyncLogin().execute(sharedPrefs.getString("username", null), sharedPrefs.getString("password", null), NEXT_PROC1);
        } else {
            Log.i(TAG, "Session are set yet. Call next process.");
            if (MainActivity.NEXT_PROC1.equals(NEXT_PROC1)) getMyClaims(mCurrentConditionRn, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Activity result returned.");
        switch (requestCode) {
            case REQUEST_SETTINGS:
                Log.i(TAG, "It's from SettingsActivity. Call loginUdp.");
                loginUdp();
                break;
            case REQUEST_FILTERS:
                Log.i(TAG, "It's from FiltersActivity.");
                switch (resultCode) {
                    case RESULT_FILTERS_ADD_NEW:
                        Log.i(TAG, "Request for new query has been received.");
                        Intent iFilterAdd = new Intent(this, FilterOneActivity.class);
                        iFilterAdd.putExtra(FilterOneActivity.EXTRA_REQUEST_KEY, REQUEST_FILTER_ADD_NEW);
                        Log.i(TAG, "Intent to start FilterOneActivity (reguest REQUEST_FILTER_ADD_NEW).");
                        startActivityForResult(iFilterAdd, REQUEST_FILTER_ADD_NEW);
                        break;
                    case RESULT_FILTERS_SELECT:
                        mCurrentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (mCurrentConditionRn == 0) mCurrentConditionRn = null;
                        getMyClaims(mCurrentConditionRn, null);
                        break;
                }
                break;
            case REQUEST_FILTER_ADD_NEW:
                Log.i(TAG, "It's from FilterEditorActivity.");
                switch (resultCode) {
                    case RESULT_FILTER_SAVE:
                    case RESULT_FILTER_EXEC:
                        Log.i(TAG, "Request for save and exec query has been received.");
                        mCurrentConditionRn = data.getLongExtra(Filter.PARAM_FILTER_RN, 0);
                        if (mCurrentConditionRn == 0) mCurrentConditionRn = null;
                        Log.i(TAG, "Query has been stored with RN " + String.valueOf(mCurrentConditionRn));
                        Log.i(TAG, "Clear Claims cache");
                        //mApplication.clearClaimsCache();
                        getMyClaims(mCurrentConditionRn, null);
                        break;
                }
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
                Log.i(TAG, "The «Settings» menu item selected");
                Intent iSettings = new Intent(this, SettingsActivity.class);
                Log.i(TAG, "Intent to start SettingsActivity (reguest REQUEST_SETTINGS).");
                startActivityForResult(iSettings, REQUEST_SETTINGS);
                return true;
            case R.id.action_search:
                Log.i(TAG, "The «Search» menu item selected");
                Intent iFilters = new Intent(this, FiltersActivity.class);
                Log.i(TAG, "Intent to start FiltersActivity (reguest REQUEST_FILTERS).");
                startActivityForResult(iFilters, REQUEST_FILTERS);
                return true;
            //TODO: добавление рекламации
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.i(TAG, "Save Claims To Cache");
        mApplication.saveClaimAdapter((ClaimListAdapter) mClaimsListView.getAdapter());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Claim lClaim = (Claim) adapterView.getAdapter().getItem(i);
        long lRn = lClaim.rn;
        boolean lHd = lClaim.hasAttach;
        if (lRn > 0) {
            Intent lClaimViewIntent = new Intent(this, ClaimActivity.class);
            lClaimViewIntent.putExtra(ClaimActivity.EXTRA_RN_KEY, lRn);
            lClaimViewIntent.putExtra(ClaimActivity.EXTRA_HAS_DOCS_KEY, lHd);
            startActivityForResult(lClaimViewIntent, MainActivity.REQUEST_CLAIM_VIEW);
        }
    }


    private class AsyncLogin extends AsyncTask<String, Void, JSONObject> {
        final Toast ts = Toast.makeText(getApplicationContext(), getText(R.string.authorizing), Toast.LENGTH_SHORT);
        String nextProc;

        @Override
        protected JSONObject doInBackground(String... params) {
            nextProc = params[2];
            ts.show();
            JSONObject response = null;
            RestRequest loginRequest;
            try {
                loginRequest = new RestRequest("login/", "POST");
                loginRequest.addHeaderParamBase64("P-USER", params[0]);
                loginRequest.addHeaderParamBase64("P-PASS", params[1]);
                response = loginRequest.getJsonContent();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            ts.cancel();
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response == null) return;
            if (response.optString("ERROR") != null && !response.optString("ERROR").isEmpty()) {

                ErrorPopup errorPopup = new ErrorPopup(MainActivity.this);
                errorPopup.showErrorDialog(getString(R.string.error_title), response.optString("ERROR"));
                return;
            } else {
                mApplication.setSessionId(response.optString("SESSONID"));
                mApplication.setPmoUser(response.optInt("PPP") == 1);
            }
            if (nextProc.equals(NEXT_PROC1)) getMyClaims(mCurrentConditionRn, null);
        }
    }
}
