package ua.parus.pmo.parus8claims.objects.filter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.Intents;
import ua.parus.pmo.parus8claims.R;

//TODO: set filter on AsyncTask

@SuppressWarnings("deprecation")
public class FiltersActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = FiltersActivity.class.getSimpleName();
    private ListView filtersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.stores_queries);
        }
        this.filtersListView = (ListView) findViewById(R.id.flFiltersList);
        this.filtersListView.setOnItemClickListener(this);
        try {
            FilterListAdapter adapter;
            if ((adapter = ((ClaimApplication) getApplication()).getFilters()) == null) {
                adapter = new FilterListAdapter(this);
                ((ClaimApplication) getApplication()).setFilters(adapter);
            }
            this.filtersListView.setAdapter(adapter);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filters, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Intents.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_query:
                Intent iAdd = new Intent();
                setResult(Intents.RESULT_NEED_ADD_NEW_FILTER, iAdd);
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        long viewId = view.getId();
        long rn = ((Filter) adapterView.getAdapter().getItem(i)).filter_rn;
        if (viewId == R.id.flImageEdit) {
            Intent intentFilterEdit = new Intent(this, FilterEditActivity.class);
            intentFilterEdit.putExtra(Intents.EXTRA_KEY_REQUEST, Intents.REQUEST_FILTER_EDIT);
            intentFilterEdit.putExtra(Intents.EXTRA_KEY_RN, rn);
            startActivityForResult(intentFilterEdit, Intents.REQUEST_FILTER_EDIT);
        } else {
            Intent intentResult = new Intent();
            intentResult.putExtra(Filter.PARAM_FILTER_RN, rn);
            setResult(Intents.RESULT_FILTER_SELECTED, intentResult);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Intents.REQUEST_FILTER_EDIT)
            if (resultCode != Intents.RESULT_CANCEL) {
                try {
                    FilterListAdapter adapter;
                    if ((adapter = ((ClaimApplication) getApplication()).getFilters()) == null) {
                        adapter = new FilterListAdapter(this);
                        ((ClaimApplication) getApplication()).setFilters(adapter);
                    }
                    this.filtersListView.setAdapter(adapter);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
    }
}
