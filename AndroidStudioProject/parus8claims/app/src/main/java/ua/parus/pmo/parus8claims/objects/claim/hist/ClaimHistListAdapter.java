package ua.parus.pmo.parus8claims.objects.claim.hist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.TextView;
import ua.parus.pmo.parus8claims.rest.RestRequest;


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

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
