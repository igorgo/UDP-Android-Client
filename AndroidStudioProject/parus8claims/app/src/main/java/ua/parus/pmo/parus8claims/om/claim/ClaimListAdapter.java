package ua.parus.pmo.parus8claims.om.claim;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.AutoScrollListAdapter;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igor-go on 20.04.2015.
 * ua.parus.pmo.parus8claims.om.claim
 */
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
    private static final String PARAM_SESSION = "P-SESSION";
    private static final String PARAM_COND_RN = "P-COND-RN";
    private static final String PARAM_ADDED_RN = "P-NEW-RN";
    private static final String REST_URL = "claims/";
    private static final int PAGE_SIZE = 25;
    private final Context mContext;
    private final List<Claim> mEntries = new ArrayList<>();
    private RestRequest mClaimsRestRequest;

    public ClaimListAdapter(Context context, Long storedCondition, Long addedCalimRn) throws MalformedURLException {
        this.mContext = context;
        this.mClaimsRestRequest = new RestRequest(REST_URL, PAGE_SIZE);
        this.mClaimsRestRequest.addHeaderParam(PARAM_SESSION, ((ClaimApplication) this.mContext.getApplicationContext()).getSessionId());
        this.mClaimsRestRequest.addHeaderParam(PARAM_COND_RN, String.valueOf(storedCondition));
        this.mClaimsRestRequest.addHeaderParam(PARAM_ADDED_RN, String.valueOf(addedCalimRn));
    }

    @Override
    public View getAutoScrollListView(int position, View convertView, ViewGroup parent) {
        View lRow = convertView;
        ClaimHolder lHolder;
        if (lRow == null) {
            LayoutInflater lInflater = ((Activity) mContext).getLayoutInflater();
            lRow = lInflater.inflate(R.layout.claim_list_item, parent, false);
            lHolder = new ClaimHolder();
            lHolder.mNumber = (TextView) lRow.findViewById(R.id.cliNumber);
            lHolder.mRelease = (TextView) lRow.findViewById(R.id.ctvRelease);
            lHolder.mTypeImage = (ImageView) lRow.findViewById(R.id.сliTypeImage);
            lHolder.mRegDate = (TextView) lRow.findViewById(R.id.ctvRegDate);
            lHolder.mUnitcode = (TextView) lRow.findViewById(R.id.ctvUnitcode);
            //lHolder.mApplication = (TextView)lRow.findViewById(R.id.ctvApplication);
            lHolder.mState = (TextView) lRow.findViewById(R.id.ctvState);
            lHolder.mInitiator = (TextView) lRow.findViewById(R.id.ctvInitiator);
            lHolder.mDescription = (TextView) lRow.findViewById(R.id.ctvDescription);
            lHolder.mAttachImage = (ImageView) lRow.findViewById(R.id.civAttach);
            lHolder.mPriority = (TextView) lRow.findViewById(R.id.ctvPriority);
            lHolder.mExecutor = (TextView) lRow.findViewById(R.id.ctvExecutor);
            lHolder.mExecType = (ImageView) lRow.findViewById(R.id.civExecType);
            lHolder.mChangeDate = (TextView) lRow.findViewById(R.id.ctvChangeDate);
            lRow.setTag(lHolder);
        } else {
            lHolder = (ClaimHolder) lRow.getTag();
        }
        Claim lClaim = mEntries.get(position);
        lHolder.mNumber.setText("№ " + lClaim.number);
        lHolder.mRelease.setText(lClaim.releaseDisplayed);
        switch (lClaim.type) {
            case Claim.TYPE_UPWORK:
                lHolder.mTypeImage.setImageResource(R.drawable.ic_workup);
                break;
            case Claim.TYPE_REBUKE:
                lHolder.mTypeImage.setImageResource(R.drawable.ic_warn);
                break;
            case Claim.TYPE_ERROR:
                lHolder.mTypeImage.setImageResource(R.drawable.ic_error);
                break;
        }
        if (lClaim.hasReleaseFix) {
            lHolder.mRelease.setTextAppearance(mContext, R.style.release_fixed);
            lHolder.mRelease.setBackgroundResource(R.drawable.release_bg_accept);
        } else {
            lHolder.mRelease.setTextAppearance(mContext, R.style.release_found);
            lHolder.mRelease.setBackgroundResource(R.drawable.release_bg_notaccept);
        }
        lHolder.mRegDate.setText(lClaim.registrationDate);
        lHolder.mUnitcode.setText(lClaim.unit);
//        lHolder.mApplication.setText(lClaim.get_application());
        lHolder.mState.setText(lClaim.state);
        switch (lClaim.stateType) {
            case Claim.STATUS_TYPE_WAIT:
                lHolder.mState.setTextAppearance(mContext, R.style.claim_state_wait);
                break;
            case Claim.STATUS_TYPE_WORK:
                lHolder.mState.setTextAppearance(mContext, R.style.claim_state_work);
                break;
            case Claim.STATUS_TYPE_DONE:
                lHolder.mState.setTextAppearance(mContext, R.style.claim_state_done);
                break;
            case Claim.STATUS_TYPE_NEGATIVE:
                lHolder.mState.setTextAppearance(mContext, R.style.claim_state_negotive);
                break;
        }
        lHolder.mInitiator.setText("@" + lClaim.initiator);
        lHolder.mDescription.setText(lClaim.description);
        if (lClaim.hasAttach) lHolder.mAttachImage.setVisibility(View.VISIBLE);
        else lHolder.mAttachImage.setVisibility(View.INVISIBLE);
        int lPriority = lClaim.priority;
        lHolder.mPriority.setText(String.valueOf(lPriority));
        if (lPriority < 5)
            lHolder.mPriority.setTextAppearance(mContext, R.style.claim_state_done);
        else if (lPriority > 5)
            lHolder.mPriority.setTextAppearance(mContext, R.style.claim_state_negotive);
        else
            lHolder.mPriority.setTextAppearance(mContext, R.style.claim_state_wait);
        if (lClaim.executorType == Claim.EXECUTOR_TYPE_PERSON) {
            lHolder.mExecType.setImageResource(R.drawable.ic_action_person);
            lHolder.mExecType.setVisibility(View.VISIBLE);
        } else if (lClaim.executorType == Claim.EXECUTOR_TYPE_GROUP) {
            lHolder.mExecType.setImageResource(R.drawable.ic_action_group);
            lHolder.mExecType.setVisibility(View.VISIBLE);
        } else {
            lHolder.mExecType.setVisibility(View.INVISIBLE);
        }
        lHolder.mExecutor.setText(lClaim.executor);
        lHolder.mChangeDate.setText(lClaim.changeDate);
        return lRow;
    }

    @Override
    public void onScrollNext() {
        Log.i(TAG, "onScrollNext called");
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

    static class ClaimHolder {
        TextView mNumber;
        TextView mRelease;
        ImageView mTypeImage;
        ImageView mAttachImage;
        TextView mRegDate;
        TextView mUnitcode;
        //TextView mApplication;
        TextView mState;
        TextView mInitiator;
        TextView mDescription;
        TextView mPriority;
        ImageView mExecType;
        TextView mExecutor;
        TextView mChangeDate;
    }

    private class FetchAsyncTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ClaimListAdapter.this.lock();
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            return ClaimListAdapter.this.mClaimsRestRequest.getPageRows();
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            if (isCancelled() || response == null) {
                ClaimListAdapter.this.notifyEndOfList();
            } else {
                List<Claim> lListClaims = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject lResponseRow = response.getJSONObject(i);
                        Claim lClaim = new Claim();
                        lClaim.rn = lResponseRow.getLong(FIELD_RN);
                        lClaim.type = lResponseRow.getInt(FIELD_NTYPE);
                        lClaim.set_hasReleaseFix(lResponseRow.getInt(FIELD_HAS_RELEASE_FIX));
                        lClaim.number = lResponseRow.getString(FIELD_NUMBER);
                        lClaim.releaseDisplayed = lResponseRow.optString(FIELD_RELASE_DISPLAYED);
                        lClaim.registrationDate = lResponseRow.getString(FIELDS_REG_DATE);
                        lClaim.unit = lResponseRow.optString(FIELD_UNIT);
                        lClaim.application = lResponseRow.optString(FIELD_APPLICATION);
                        lClaim.state = lResponseRow.optString(FIELD_STATE);
                        lClaim.stateType = lResponseRow.optInt(FIELD_STATE_TYPE);
                        lClaim.initiator = lResponseRow.getString(FIELD_INITIATOR);
                        lClaim.description = lResponseRow.optString(FIELD_DESRIPTION);
                        lClaim.executor = lResponseRow.optString(FIELD_EXECUTOR);
                        lClaim.changeDate = lResponseRow.optString(FIELD_CHANGE_DATE);
                        lClaim.priority = lResponseRow.optInt(FIELD_PRIORITY);
                        lClaim.set_hasAttach(lResponseRow.getInt(FIELD_HAS_ATTACH));
                        lClaim.executorType = lResponseRow.getInt(FIELD_EXECUTOR_TYPE);
                        lListClaims.add(lClaim);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ClaimListAdapter.this.mEntries.addAll(lListClaims);
                notifyDataSetChanged();
                if (ClaimListAdapter.this.mClaimsRestRequest.hasNextPage()) {
                    ClaimListAdapter.this.notifyHasMore();
                } else {
                    ClaimListAdapter.this.notifyEndOfList();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ClaimListAdapter.this.notifyEndOfList();
        }
    }
}
