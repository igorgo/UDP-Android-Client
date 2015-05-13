package ua.parus.pmo.parus8claims.objects.filter;

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

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igor-go on 23.04.2015.
 * ua.parus.pmo.parus8claims.objects.filter
 */
public class FilterListAdapter extends BaseAdapter {

    private static final String FIELD_RN = "n01";
    private static final String FIELD_NAME = "s01";
    private static final String FIELD_EDITABLE = "s02";
    private static final String FIELD_EDITABLE_POSITIVE_VALUE = "Y";
    private static final String TAG = FilterListAdapter.class.getSimpleName();
    private static final String PARAM_SESSION = "session";
    private static final String REST_URL = "filters/";
    private final List<Filter> entries = new ArrayList<>();
    private final Context context;
    private RestRequest filtersRequest;

    public FilterListAdapter(Context context) throws MalformedURLException {
        this.context = context;
        this.filtersRequest = new RestRequest(REST_URL);
        this.filtersRequest.addInParam(
                PARAM_SESSION,
                ((ClaimApplication) this.context.getApplicationContext()).getSessionId()
        );
        Log.i(TAG,"Created");
        new FetchAsyncTask().execute();
    }

    @Override
    public int getCount() {
        return this.entries.size();
    }

    @Override
    public Object getItem(int i) {
        return this.entries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        FilterHolder holder;
        if (row == null) {
            LayoutInflater layoutInflater = ((Activity) this.context).getLayoutInflater();
            row = layoutInflater.inflate(R.layout.list_item_filter, parent, false);
            holder = new FilterHolder();
            holder.filterName = (TextView) row.findViewById(R.id.flFilterName);
            holder.flImageEdit = (ImageView) row.findViewById(R.id.flImageEdit);
            row.setTag(holder);
        } else {
            holder = (FilterHolder) row.getTag();
        }
        Filter filter = this.entries.get(position);
        holder.filterName.setText(filter.filter_name);
        if (filter.filter_editable) {
            holder.flImageEdit.setVisibility(View.VISIBLE);
            holder.flImageEdit.setClickable(true);
            holder.flImageEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ListView) parent).performItemClick(view, position, 0);
                    Log.i("FilterList", "ClickOnButton");
                }
            });
        } else holder.flImageEdit.setVisibility(View.INVISIBLE);
        return row;
    }

    static class FilterHolder {
        TextView filterName;
        ImageView flImageEdit;
    }

    public void deleteFilter(Filter filter) {
        if (filter.filter_rn > 0) {
            int j =-1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).filter_rn == filter.filter_rn) {
                    j=i;
                    break;
                }
            }
            if (j>=0) {
                entries.remove(j);
            }
        }

    }

    public void addReplaceFilter(Filter filter){
        if (filter.filter_rn > 0) {
            int j =-1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).filter_rn == filter.filter_rn) {
                    j=i;
                    break;
                }
            }
            filter.filter_editable = true;
            if (j>=0) {
                entries.set(j,filter);
            } else {
                entries.add(filter);
            }
        }
    }

    private class FetchAsyncTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                return FilterListAdapter.this.filtersRequest.getAllRows();
            } catch (ConnectException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            if (response == null) return;
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject item = response.getJSONObject(i);
                    Filter filter = new Filter();
                    filter.filter_rn = item.getLong(FIELD_RN);
                    filter.filter_name = item.getString(FIELD_NAME);
                    filter.filter_editable = item.getString(FIELD_EDITABLE).equals(FIELD_EDITABLE_POSITIVE_VALUE);
                    FilterListAdapter.this.entries.add(filter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        }
    }
}
