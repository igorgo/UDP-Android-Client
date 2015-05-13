package ua.parus.pmo.parus8claims.objects.claim.hist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
 * Created by igorgo on 26.04.2015.
 */
@SuppressWarnings("ALL")
public class ClaimHistListAdapter extends BaseAdapter {
    public static final String TAG = ClaimHistListAdapter.class.getSimpleName();

    private static final String PARAM_SESSION = "session";
    private static final String PARAM_PRN = "prn";
    private static final String REST_URL = "claimhist/";

    private final List<ClaimHist> entries = new ArrayList<>();
    private final Context context;

    public ClaimHistListAdapter(Context context, long prn) throws MalformedURLException {
        this.context = context;
        RestRequest historyRequest = new RestRequest(REST_URL);
        historyRequest.addInParam(PARAM_SESSION, ((ClaimApplication) this.context.getApplicationContext()).getSessionId());
        historyRequest.addInParam(PARAM_PRN, String.valueOf(prn));
        JSONArray response = null;
        try {
            response = historyRequest.getAllRows();
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        if (response == null) return;
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject lResponseItem = response.getJSONObject(i);
                ClaimHist lClaimHist = new ClaimHist();
                lClaimHist.action = lResponseItem.getInt("n01");
                lClaimHist.flag = lResponseItem.getString("s01");
                lClaimHist.dateHist = lResponseItem.getString("s02");
                lClaimHist.who = lResponseItem.getString("s03");
                lClaimHist.newState = lResponseItem.optString("s04");
                lClaimHist.whom = lResponseItem.optString("s05");
                lClaimHist.textContent = lResponseItem.optString("s06");
                entries.add(lClaimHist);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return entries.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View lRow = convertView;
        HistoryHolder lHolder;
        if (lRow == null) {
            LayoutInflater lInflater = ((Activity) context).getLayoutInflater();
            int layoutResourceId = R.layout.list_item_history;
            lRow = lInflater.inflate(layoutResourceId, parent, false);
            lHolder = new HistoryHolder();
            lHolder.itemHeader = (TextView) lRow.findViewById(R.id.hliHead);
            lHolder.itemBody = (TextView) lRow.findViewById(R.id.hliBody);
            lRow.setTag(lHolder);
        } else {
            lHolder = (HistoryHolder) lRow.getTag();
        }
        ClaimHist lClaimHist = entries.get(position);
        lHolder.itemHeader.setText(lClaimHist.actionAsText(context));
        if (lClaimHist.textContent != null && !lClaimHist.textContent.isEmpty()) {
            lHolder.itemBody.setVisibility(View.VISIBLE);
            lHolder.itemBody.setText(lClaimHist.textContent);
            if (lClaimHist.flag.equals(ClaimHist.FLAG_COMMENT_AUTHOR)) {
                lHolder.itemBody.setTextAppearance(context, R.style.buble_author);
                lHolder.itemBody.setBackgroundResource(R.drawable.thanks_bubbles_orange);
            }
            if (lClaimHist.flag.equals(ClaimHist.FLAG_COMMENT_OTHER)) {
                lHolder.itemBody.setTextAppearance(context, R.style.buble_nonauthor);
                lHolder.itemBody.setBackgroundResource(R.drawable.thanks_bubbles_green);
            }
        } else {
            lHolder.itemBody.setVisibility(View.GONE);
        }
        return lRow;
    }

    static class HistoryHolder {
        TextView itemHeader;
        TextView itemBody;
    }


}
