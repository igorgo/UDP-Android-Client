package ua.parus.pmo.parus8claims.objects.filter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ProgressWindow;
import ua.parus.pmo.parus8claims.utils.Constants;


@SuppressWarnings("deprecation")
public class FiltersActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, Handler.Callback {

    private static final int MSG_ADAPTER_READY = 1;
    @SuppressWarnings("unused")
    private static final String TAG = FiltersActivity.class.getSimpleName();
    private ListView filtersListView;
    private FilterListAdapter adapter;
    private Handler handler;
    private FiltersActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
        setContentView(R.layout.activity_filters);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.stores_queries);
        }
        this.handler = new Handler(this);
        this.filtersListView = (ListView) findViewById(R.id.flFiltersList);
        this.filtersListView.setOnItemClickListener(this);
        initFab();
        setAdapter();
    }

    private void setAdapter() {
        if ((adapter = ((ClaimApplication) getApplication()).getFilters()) == null) {
            adapter = new FilterListAdapter(this);
            new AsyncLoadFilters().execute();
        } else {
            handler.sendEmptyMessage(MSG_ADAPTER_READY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_filters, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_add_query:
                Intent iAdd = new Intent();
                setResult(Constants.RESULT_NEED_ADD_NEW_FILTER, iAdd);
                finish();
                return true;*/
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void initFab() {
        AddFloatingActionButton fabAddButton = (AddFloatingActionButton) findViewById(R.id.fab);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iAdd = new Intent();
                setResult(Constants.RESULT_NEED_ADD_NEW_FILTER, iAdd);
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        long viewId = view.getId();
        long rn = ((Filter) adapterView.getAdapter().getItem(i)).filter_rn;
        if (viewId == R.id.flImageEdit) {
            Intent intentFilterEdit = new Intent(this, FilterEditActivity.class);
            intentFilterEdit.putExtra(Constants.EXTRA_KEY_REQUEST, Constants.REQUEST_FILTER_EDIT);
            intentFilterEdit.putExtra(Constants.EXTRA_KEY_RN, rn);
            startActivityForResult(intentFilterEdit, Constants.REQUEST_FILTER_EDIT);
        } else {
            Intent intentResult = new Intent();
            intentResult.putExtra(Filter.PARAM_FILTER_RN, rn);
            setResult(Constants.RESULT_FILTER_SELECTED, intentResult);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_FILTER_EDIT) {
            if (resultCode != Constants.RESULT_CANCEL) {
                setAdapter();
            }
        }
    }

    @Override public boolean handleMessage(Message message) {
        if (message.what == MSG_ADAPTER_READY) {
            filtersListView.setAdapter(adapter);
        }
        return false;
    }

    private class AsyncLoadFilters extends AsyncTask<Void, Void, Void> {
        private ProgressWindow pw;

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected Void doInBackground(Void... voids) {
            adapter.loadFromServer();
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            ((ClaimApplication) getApplication()).setFilters(adapter);
            pw.dismiss();
            handler.sendEmptyMessage(MSG_ADAPTER_READY);
            super.onPostExecute(aVoid);
        }
    }
}
