package ua.parus.pmo.parus8claims;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.om.filter.Filter;
import ua.parus.pmo.parus8claims.om.filter.FilterListAdapter;


public class FiltersActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = FiltersActivity.class.getSimpleName();

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate 1");
        setContentView(R.layout.activity_filters);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.pmo_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(R.string.stores_queries);
        Log.i(TAG, "onCreate 2");
        mListView = (ListView) findViewById(R.id.flFiltersList);
        Log.i(TAG, "onCreate 3");
        try {
            mListView.setAdapter(new FilterListAdapter(this));
            Log.i(TAG, "onCreate 4");
            mListView.setOnItemClickListener(this);
            Log.i(TAG, "onCreate 5");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filters, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(MainActivity.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_add_query:
                Intent iAdd = new Intent();
                setResult(MainActivity.RESULT_FILTERS_ADD_NEW, iAdd);
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        long viewId = view.getId();
        long lRN = ((Filter) adapterView.getAdapter().getItem(i)).filter_rn;
        if (viewId == R.id.flImageEdit) {
            Log.i("FilterListAct", "Click on Image  " + String.valueOf(lRN));
            Intent iFilterEdit = new Intent(this, FilterOneActivity.class);
            iFilterEdit.putExtra(FilterOneActivity.EXTRA_REQUEST_KEY, MainActivity.REQUEST_FILTER_EDIT);
            iFilterEdit.putExtra(FilterOneActivity.EXTRA_RN_KEY, lRN);
            Log.i(TAG, "Intent to start FilterOneActivity (reguest REQUEST_FILTER_EDIT).");
            startActivityForResult(iFilterEdit, MainActivity.REQUEST_FILTER_EDIT);
        } else {
            Intent lIntent = new Intent();
            lIntent.putExtra(Filter.PARAM_FILTER_RN, lRN);
            setResult(MainActivity.RESULT_FILTERS_SELECT, lIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != MainActivity.RESULT_CANCEL) {
            try {
                mListView.setAdapter(new FilterListAdapter(this));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
