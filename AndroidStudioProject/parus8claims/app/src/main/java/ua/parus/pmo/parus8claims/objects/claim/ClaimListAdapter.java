package ua.parus.pmo.parus8claims.objects.claim;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import ua.parus.pmo.parus8claims.gui.AutoScrollListAdapter;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class ClaimListAdapter extends AutoScrollListAdapter {

    private static final String FIELD_RN = "n01";
    private static final String FIELD_NTYPE = "n02";
    private static final String FIELD_HAS_RELEASE_FIX = "n03";
    private static final String FIELD_NUMBER = "s01";
    private static final String FIELD_RELASE_DISPLAYED = "s02";
    private static final String FIELDS_REG_DATE = "s03";
    private static final String FIELD_UNIT = "s04";
    private static final String FIELD_APPLICATION = "s05";
    private static final String FIELD_STATE = "s06";
    private static final String FIELD_STATE_TYPE = "n04";
    private static final String FIELD_INITIATOR = "s07";
    private static final String FIELD_DESRIPTION = "s08";
    private static final String FIELD_EXECUTOR = "s09";
    private static final String FIELD_CHANGE_DATE = "s10";
    private static final String FIELD_PRIORITY = "n05";
    private static final String FIELD_HAS_ATTACH = "n06";
    private static final String FIELD_EXECUTOR_TYPE = "n07";
    private static final String PARAM_SESSION = "session";
    private static final String PARAM_COND_RN = "cond";
    private static final String PARAM_ADDED_RN = "newrn";
    private static final String REST_URL = "claims/";
    private static final int PAGE_SIZE = 25;
    private final Context context;
    private final List<Claim> entries = new ArrayList<>();
    private RestRequest claimsRestRequest;


    public ClaimListAdapter(Context context, Long storedCondition, Long addedClaimRn) throws MalformedURLException {
        this.context = context;
        claimsRestRequest = new RestRequest(REST_URL, PAGE_SIZE);
        claimsRestRequest.addInParam(PARAM_SESSION, ((ClaimApplication) context.getApplicationContext()).getSessionId());
        claimsRestRequest.addInParam(PARAM_COND_RN, String.valueOf(storedCondition));
        claimsRestRequest.addInParam(PARAM_ADDED_RN, String.valueOf(addedClaimRn));

    }

    public static void initHolder(ClaimHolder holder, View row) {
        holder.mNumber = (TextView) row.findViewById(R.id.cliNumber);
        holder.mRelease = (TextView) row.findViewById(R.id.ctvRelease);
        holder.mTypeImage = (ImageView) row.findViewById(R.id.сliTypeImage);
        holder.mRegDate = (TextView) row.findViewById(R.id.ctvRegDate);
        holder.mUnitcode = (TextView) row.findViewById(R.id.ctvUnitcode);
        holder.mState = (TextView) row.findViewById(R.id.ctvState);
        holder.mInitiator = (TextView) row.findViewById(R.id.ctvInitiator);
        holder.mDescription = (TextView) row.findViewById(R.id.ctvDescription);
        holder.mAttachImage = (ImageView) row.findViewById(R.id.civAttach);
        holder.mPriority = (TextView) row.findViewById(R.id.ctvPriority);
        holder.mExecutor = (TextView) row.findViewById(R.id.ctvExecutor);
        holder.mExecType = (ImageView) row.findViewById(R.id.civExecType);
        holder.mChangeDate = (TextView) row.findViewById(R.id.ctvChangeDate);
        row.setTag(holder);
    }

    public static void populateHolder(Context context, ClaimHolder holder, Claim claim) {
        holder.mNumber.setText("№ " + claim.number);
        holder.mRelease.setText(claim.releaseDisplayed);
        switch (claim.type) {
            case Claim.TYPE_UPWORK:
                holder.mTypeImage.setImageResource(R.drawable.ic_workup);
                break;
            case Claim.TYPE_REBUKE:
                holder.mTypeImage.setImageResource(R.drawable.ic_warn);
                break;
            case Claim.TYPE_ERROR:
                holder.mTypeImage.setImageResource(R.drawable.ic_error);
                break;
        }
        if (claim.hasReleaseFix) {
            holder.mRelease.setTextAppearance(context, R.style.release_fixed);
            holder.mRelease.setBackgroundResource(R.drawable.release_bg_accept);
        } else {
            holder.mRelease.setTextAppearance(context, R.style.release_found);
            holder.mRelease.setBackgroundResource(R.drawable.release_bg_notaccept);
        }
        holder.mRegDate.setText(claim.registrationDate);
        holder.mUnitcode.setText(claim.unit);
        holder.mState.setText(claim.state);
        switch (claim.stateType) {
            case Claim.STATUS_TYPE_WAIT:
                holder.mState.setTextAppearance(context, R.style.claim_state_wait);
                break;
            case Claim.STATUS_TYPE_WORK:
                holder.mState.setTextAppearance(context, R.style.claim_state_work);
                break;
            case Claim.STATUS_TYPE_DONE:
                holder.mState.setTextAppearance(context, R.style.claim_state_done);
                break;
            case Claim.STATUS_TYPE_NEGATIVE:
                holder.mState.setTextAppearance(context, R.style.claim_state_negotive);
                break;
        }
        holder.mInitiator.setText("@" + claim.initiator);
        holder.mDescription.setText(claim.description);
        if (claim.hasAttach) holder.mAttachImage.setVisibility(View.VISIBLE);
        else holder.mAttachImage.setVisibility(View.INVISIBLE);
        int priority = claim.priority;
        holder.mPriority.setText(String.valueOf(priority));
        if (priority < 5)
            holder.mPriority.setTextAppearance(context, R.style.claim_state_done);
        else if (priority > 5)
            holder.mPriority.setTextAppearance(context, R.style.claim_state_negotive);
        else
            holder.mPriority.setTextAppearance(context, R.style.claim_state_wait);
        if (claim.executorType == Claim.EXECUTOR_TYPE_PERSON) {
            holder.mExecType.setImageResource(R.drawable.ic_action_person);
            holder.mExecType.setVisibility(View.VISIBLE);
        } else if (claim.executorType == Claim.EXECUTOR_TYPE_GROUP) {
            holder.mExecType.setImageResource(R.drawable.ic_action_group);
            holder.mExecType.setVisibility(View.VISIBLE);
        } else {
            holder.mExecType.setVisibility(View.INVISIBLE);
        }
        holder.mExecutor.setText(claim.executor);
        holder.mChangeDate.setText(claim.changeDate);
    }

    @Override
    public View getAutoScrollListView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ClaimHolder holder;
        if (row == null) {
            LayoutInflater layoutInflater = ((Activity) this.context).getLayoutInflater();
            row = layoutInflater.inflate(R.layout.list_item_claim, parent, false);
            holder = new ClaimHolder();
            initHolder(holder, row);
        } else {
            holder = (ClaimHolder) row.getTag();
        }
        Claim claim = this.entries.get(position);
        populateHolder(this.context, holder, claim);
        return row;
    }

    @Override
    public void onScrollNext() {
        new FetchAsyncTask().execute();
    }

    @Override
    public int getCount() {
        return this.entries.size();
    }


    public void setItem(int i, Claim claim) {
        entries.set(i, claim);
    }

    @Override
    public Object getItem(int i) {
        return this.entries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public static class ClaimHolder {
        TextView mNumber;
        TextView mRelease;
        ImageView mTypeImage;
        ImageView mAttachImage;
        TextView mRegDate;
        TextView mUnitcode;
        TextView mState;
        TextView mInitiator;
        TextView mDescription;
        TextView mPriority;
        ImageView mExecType;
        TextView mExecutor;
        TextView mChangeDate;
    }

    private class FetchAsyncTask extends AsyncTask<Void, Void, Integer> {

        public static final int EMPTY_RESPONSE = -1;
        public static final int END_OF_LIST = 0;
        public static final int HAS_MORE = 1;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ClaimListAdapter.this.lock();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONArray response = claimsRestRequest.getPageRows();
                if (isCancelled() || response == null) {
                    return EMPTY_RESPONSE;
                } else {
                    List<Claim> claims = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                            JSONObject item = response.getJSONObject(i);
                            Claim claim = new Claim();
                            claim.rn = item.getLong(FIELD_RN);
                            claim.type = item.getInt(FIELD_NTYPE);
                            claim.set_hasReleaseFix(item.getInt(FIELD_HAS_RELEASE_FIX));
                            claim.number = item.getString(FIELD_NUMBER);
                            claim.releaseDisplayed = item.optString(FIELD_RELASE_DISPLAYED);
                            claim.registrationDate = item.getString(FIELDS_REG_DATE);
                            claim.unit = item.optString(FIELD_UNIT);
                            claim.application = item.optString(FIELD_APPLICATION);
                            claim.state = item.optString(FIELD_STATE);
                            claim.stateType = item.optInt(FIELD_STATE_TYPE);
                            claim.initiator = item.getString(FIELD_INITIATOR);
                            claim.description = item.optString(FIELD_DESRIPTION);
                            claim.executor = item.optString(FIELD_EXECUTOR);
                            claim.changeDate = item.optString(FIELD_CHANGE_DATE);
                            claim.priority = item.optInt(FIELD_PRIORITY);
                            claim.set_hasAttach(item.getInt(FIELD_HAS_ATTACH));
                            claim.executorType = item.getInt(FIELD_EXECUTOR_TYPE);
                            claims.add(claim);
                    }
                    ClaimListAdapter.this.entries.addAll(claims);
                    if (claimsRestRequest.hasNextPage())
                        return HAS_MORE;
                    else
                        return END_OF_LIST;
                }
            } catch (ConnectException |  JSONException e ) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == EMPTY_RESPONSE) {
                notifyEndOfList();
            } else {
                notifyDataSetChanged();
                if (result == HAS_MORE) notifyHasMore();
                if (result == END_OF_LIST) notifyEndOfList();
            }
            notifyEmptyList(ClaimListAdapter.this.entries.size() == 0);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ClaimListAdapter.this.notifyEndOfList();
        }
    }
}
