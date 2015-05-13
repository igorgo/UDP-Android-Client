package ua.parus.pmo.parus8claims.om.filter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igor-go on 23.04.2015.
 * ua.parus.pmo.parus8claims.om.filter
 */
public class FilterListAdapter extends BaseAdapter {

    private static final String FIELD_RN = "n01";
    private static final String FIELD_NAME = "s01";
    private static final String FIELD_EDITABLE = "s02";
    private static final String FIELD_EDITABLE_POSITIVE_VALUE = "Y";
    private static final String TAG = FilterListAdapter.class.getSimpleName();
    private static final String PARAM_SESSION = "P-SESSION";
    private static final String REST_URL = "filters/";
    private final List<Filter> mEntries = new ArrayList<>();
    private final Context mContext;
    private RestRequest mFiltersRequest;

    public FilterListAdapter(Context context) throws MalformedURLException {
        this.mContext = context;
        this.mFiltersRequest = new RestRequest(REST_URL);
        this.mFiltersRequest.addHeaderParam(
                PARAM_SESSION,
                ((ClaimApplication) this.mContext.getApplicationContext()).getSessionId()
        );
        Log.i(TAG,"Created");
        new FetchAsyncTask().execute();
    }

    @Override
    public int getCount() {
        return mEntries.size();
    }

    @Override
    public Object getItem(int i) {
        return mEntries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View lRow = convertView;
        FilterHolder lHolder;
        if (lRow == null) {
            LayoutInflater lInflater = ((Activity) mContext).getLayoutInflater();
            lRow = lInflater.inflate(R.layout.filter_list_item, parent, false);
            lHolder = new FilterHolder();
            lHolder.filterName = (TextView) lRow.findViewById(R.id.flFilterName);
            lHolder.flImageEdit = (ImageView) lRow.findViewById(R.id.flImageEdit);
            lRow.setTag(lHolder);
        } else {
            lHolder = (FilterHolder) lRow.getTag();
        }
        Filter lFilter = mEntries.get(position);
        lHolder.filterName.setText(lFilter.filter_name);
        if (lFilter.filter_editable) {
            lHolder.flImageEdit.setVisibility(View.VISIBLE);
            lHolder.flImageEdit.setClickable(true);
            lHolder.flImageEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ListView) parent).performItemClick(view, position, 0);
                    Log.i("FilterList", "ClickOnButton");
                }
            });
        } else lHolder.flImageEdit.setVisibility(View.INVISIBLE);
        return lRow;
    }

    static class FilterHolder {
        TextView filterName;
        ImageView flImageEdit;
    }

    private class FetchAsyncTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... voids) {
            return FilterListAdapter.this.mFiltersRequest.getAllRows();
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            if (response == null) return;
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject lResponseRow = response.getJSONObject(i);
                    Filter lFilter = new Filter();
                    lFilter.filter_rn = lResponseRow.getLong(FIELD_RN);
                    lFilter.filter_name = lResponseRow.getString(FIELD_NAME);
                    lFilter.filter_editable = lResponseRow.getString(FIELD_EDITABLE).equals(FIELD_EDITABLE_POSITIVE_VALUE);
                    mEntries.add(lFilter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        }
    }
}
