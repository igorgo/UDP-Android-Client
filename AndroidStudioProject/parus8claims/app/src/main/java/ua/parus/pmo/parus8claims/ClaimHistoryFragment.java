package ua.parus.pmo.parus8claims;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.DecimalFormat;

import ua.parus.pmo.parus8claims.om.claim.Claim;
import ua.parus.pmo.parus8claims.om.claim.hist.ClaimHistListAdapter;
import ua.parus.pmo.parus8claims.rest.RestRequest;

import static android.widget.LinearLayout.LayoutParams;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link ClaimHistoryFragmentInterface}
 * interface.
 */
public class ClaimHistoryFragment extends ListFragment implements View.OnClickListener {


    private static final String TAG = ClaimHistoryFragment.class.getSimpleName();
    private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;
    private static final String ARG_RN = "claimRn";
    private static final String ARG_SESSION = "session";
    private static final String ARG_HAS_DOCS = "hasDocs";
    private long mClaimRn;
    private String mSession;
    private ClaimHistListAdapter mHistAdapter;
    private Claim mClaim;

    private View mInfoView;

    private boolean histLoaded;
    private boolean infoLoaded;
    private boolean docsLoaded;

    private ClaimHistoryFragmentInterface mListener;
    private boolean mHasDocs;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ClaimHistoryFragment() {
    }

    public static ClaimHistoryFragment newInstance(long claimRn, String session, boolean hasDocs) {
        ClaimHistoryFragment fragment = new ClaimHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RN, claimRn);
        args.putString(ARG_SESSION, session);
        args.putBoolean(ARG_HAS_DOCS, hasDocs);
        fragment.setArguments(args);
        return fragment;
    }

    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mClaimRn = getArguments().getLong(ARG_RN);
            mSession = getArguments().getString(ARG_SESSION);
            mHasDocs = getArguments().getBoolean(ARG_HAS_DOCS, false);
        }
        histLoaded = false;
        infoLoaded = false;
        docsLoaded = !mHasDocs;

        mClaim = new Claim();
        new GetHistAsyncTask().execute(mClaimRn);
        new GetClaimAsyncTask().execute(mClaimRn);
        if (mHasDocs) new GetDocsAsyncTask().execute(mClaimRn);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ClaimHistoryFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ClaimHistoryFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInfoView = getActivity().getLayoutInflater().inflate(R.layout.fragment_claim_info, null);
        View mRootView = inflater.inflate(R.layout.claim_hist_fragment, container, false);
        LinearLayout pframe = (LinearLayout) mRootView.findViewById(R.id.progress_container);
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        FrameLayout lframe = (FrameLayout) mRootView.findViewById(R.id.list_container);
        lframe.setId(INTERNAL_LIST_CONTAINER_ID);
        return mRootView;
    }

    private void assignViewValues() {
        ((TextView) mInfoView.findViewById(R.id.fciNumberText)).setText(mClaim.number);

        switch (mClaim.type) {
            case Claim.TYPE_UPWORK:
                ((ImageView) mInfoView.findViewById(R.id.fciTypeImage)).setImageResource(R.drawable.ic_workup);
                ((TextView) mInfoView.findViewById(R.id.fciTypeText)).setText(R.string.claim_type_addon);
                break;
            case Claim.TYPE_REBUKE:
                ((ImageView) mInfoView.findViewById(R.id.fciTypeImage)).setImageResource(R.drawable.ic_warn);
                ((TextView) mInfoView.findViewById(R.id.fciTypeText)).setText(R.string.claim_type_rebuke);
                break;
            case Claim.TYPE_ERROR:
                ((ImageView) mInfoView.findViewById(R.id.fciTypeImage)).setImageResource(R.drawable.ic_error);
                ((TextView) mInfoView.findViewById(R.id.fciTypeText)).setText(R.string.claim_type_error);
                break;
        }
        //((TextView) mInfoView.findViewById(R.id.fciInitiatorText)).setText(mClaim.initiator);
        //((TextView) mInfoView.findViewById(R.id.fciRegDateText)).setText(mClaim.registrationDate);
        ((TextView) mInfoView.findViewById(R.id.fciApplicationText)).setText(mClaim.application);
        ((TextView) mInfoView.findViewById(R.id.fciUnitText)).setText(mClaim.unit);
        if (mClaim.unitFunc == null || mClaim.unitFunc.isEmpty()) {

            mInfoView.findViewById(R.id.fciUnitFuncContainer).setVisibility(View.GONE);
            // ((TextView) mInfoView.findViewById(R.id.fciUnitFuncLabel)).setVisibility(View.GONE);
            //((TextView) mInfoView.findViewById(R.id.fciUnitFuncText)).setVisibility(View.GONE);
        } else {
            ((TextView) mInfoView.findViewById(R.id.fciUnitFuncText)).setText(mClaim.unitFunc);
        }
        if (mClaim.releaseFound != null) {
            if (mClaim.buildFound != null)
                ((TextView) mInfoView.findViewById(R.id.fciReleaseFromText)).setText(mClaim.buildFound.get_displayName());
            else
                ((TextView) mInfoView.findViewById(R.id.fciReleaseFromText)).setText(mClaim.releaseFound.get_release());
        }
        TextView lv = (TextView) mInfoView.findViewById(R.id.fciStatusText);
        lv.setText(mClaim.state);
        switch (mClaim.stateType) {
            case Claim.STATUS_TYPE_WAIT:
                lv.setTextAppearance(getActivity(), R.style.claim_state_wait);
                break;
            case Claim.STATUS_TYPE_WORK:
                lv.setTextAppearance(getActivity(), R.style.claim_state_work);
                break;
            case Claim.STATUS_TYPE_DONE:
                lv.setTextAppearance(getActivity(), R.style.claim_state_done);
                break;
            case Claim.STATUS_TYPE_NEGATIVE:
                lv.setTextAppearance(getActivity(), R.style.claim_state_negotive);
                break;
        }
        ((TextView) mInfoView.findViewById(R.id.fciChangeDateText)).setText(mClaim.changeDate);
        if (mClaim.executorType > 0) {
            ((TextView) mInfoView.findViewById(R.id.fciExecutorText)).setText(mClaim.executor);
            if (mClaim.executorType == 1) {
                ((ImageView) mInfoView.findViewById(R.id.fciExecutorImage)).setImageResource(R.drawable.ic_action_person);
            } else {
                ((ImageView) mInfoView.findViewById(R.id.fciExecutorImage)).setImageResource(R.drawable.ic_action_group);
            }
        } else {
            mInfoView.findViewById(R.id.fciExecutorContainer).setVisibility(View.GONE);
            /*((TextView) mInfoView.findViewById(R.id.fciExecutorText)).setVisibility(View.GONE);
            ((TextView) mInfoView.findViewById(R.id.fciExecutorLabel)).setVisibility(View.GONE);
            ((ImageView) mInfoView.findViewById(R.id.fciTypeImage)).setVisibility(View.GONE);
            ((TextView) mInfoView.findViewById(R.id.fciPriorityText)).setVisibility(View.GONE);
            ((TextView) mInfoView.findViewById(R.id.fciPriorityLabel)).setVisibility(View.GONE);*/
        }
        lv = (TextView) mInfoView.findViewById(R.id.fciPriorityText);
        lv.setText(String.valueOf(mClaim.priority));
        if (mClaim.priority < 5)
            lv.setTextAppearance(getActivity(), R.style.claim_state_done);
        else if (mClaim.priority > 5)
            lv.setTextAppearance(getActivity(), R.style.claim_state_negotive);
        else
            lv.setTextAppearance(getActivity(), R.style.claim_state_wait);
        if (mClaim.releaseFix != null) {
            if (mClaim.buildFix != null)
                ((TextView) mInfoView.findViewById(R.id.fciReleaseToText)).setText(mClaim.buildFix.get_displayName());
            else
                ((TextView) mInfoView.findViewById(R.id.fciReleaseToText)).setText(mClaim.releaseFix.get_release());
        } else {
            mInfoView.findViewById(R.id.fciReleaseToContainer).setVisibility(View.GONE);
            //((TextView) mInfoView.findViewById(R.id.fciReleaseToText)).setVisibility(View.GONE);
            //((TextView) mInfoView.findViewById(R.id.fciReleaseToLabel)).setVisibility(View.GONE);
        }
        //((TextView) mInfoView.findViewById(R.id.fciDescription)).setText(mClaim.description);

    }

    private void preListener() {
        if (infoLoaded && histLoaded && docsLoaded) {
            setListAdapter(mHistAdapter);
            if (mListener != null) {
                mListener.onHistoryLoaded(ClaimHistoryFragment.this, mClaim);
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            if (mListener != null) {
                mListener.onDocumDownloadRequest((Attach) v.getTag());
            }
            Log.i(TAG, "Click on image of doc rn=" + String.valueOf(v.getTag()));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ClaimHistoryFragmentInterface {
        public void onHistoryLoaded(ClaimHistoryFragment fragment, Claim claim);

        public void onDocumDownloadRequest(Attach attach);
    }

    private class GetHistAsyncTask extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                mHistAdapter = new ClaimHistListAdapter(getActivity(), params[0]);
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result)
                histLoaded = true;
            preListener();

        }
    }

    private class GetClaimAsyncTask extends AsyncTask<Long, Void, Void> {


        @Override
        protected Void doInBackground(Long... params) {
            mClaim.readClaimFromServer(getActivity(), mSession, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            assignViewValues();
            ClaimHistoryFragment.this.getListView().addHeaderView(mInfoView);
            infoLoaded = true;
            preListener();
            //mCallback.onClaimRead(ClaimInfoFragment.this,mClaim);
        }
    }

    @SuppressWarnings("ResourceType")
    private class GetDocsAsyncTask extends AsyncTask<Long, Void, JSONArray> {

        private final String URL_DOCS = "claimdocs/";
        private final String PARAM_SESSION = "PSESSION";
        private final String PARAM_PRN = "PPRN";
        private final String TOKEN_FILE = "@@";

        /*private String readableFileSize(long size) {
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }*/

        @Override
        protected JSONArray doInBackground(Long... params) {

            try {
                RestRequest lRestRequest = new RestRequest(URL_DOCS);
                lRestRequest.addHeaderParam(PARAM_SESSION, mSession);
                lRestRequest.addHeaderParam(PARAM_PRN, String.valueOf(params[0]));
                JSONArray response = lRestRequest.getAllRows();
                if (response != null) return response;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            if (response != null) {
                LinearLayout mDocsView = new LinearLayout(getActivity());
                mDocsView.setOrientation(LinearLayout.VERTICAL);
                mDocsView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
                int lPad = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                mDocsView.setPadding(lPad, lPad, lPad, lPad);

                LinearLayout lHeaderLayout = new LinearLayout(getActivity());
                lHeaderLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                TextView lHeaderText = new TextView(getActivity());
                LinearLayout.LayoutParams lHeaderLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                lHeaderText.setLayoutParams(lHeaderLayoutParams);
                lHeaderText.setText(getString(R.string.attached_docums));
                lHeaderText.setGravity(Gravity.CENTER);
                lHeaderText.setTypeface(Typeface.DEFAULT_BOLD);
                lHeaderLayout.addView(lHeaderText);
                mDocsView.addView(lHeaderLayout);

                for (int i = 0; i < response.length(); i++) {
                    JSONObject lResponseItem;
                    try {
                        lResponseItem = response.getJSONObject(i);
                        RelativeLayout lRelativeLayout = new RelativeLayout(getActivity());
                        lRelativeLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                        RelativeLayout.LayoutParams lLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


                        ImageView lImageView = new ImageView(getActivity());
                        lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        //lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        lImageView.setLayoutParams(lLayoutParams);
                        lImageView.setImageResource(R.drawable.ic_action_download);
                        lImageView.setId(1);
                        Attach lAttach = new Attach(lResponseItem.getString("s01"), lResponseItem.getLong("n01"));
                        lImageView.setTag(lAttach);
                        lImageView.setClickable(true);
                        lImageView.setOnClickListener(ClaimHistoryFragment.this);

                        lRelativeLayout.addView(lImageView);

                        TextView lFileTextViewFs = new TextView(getActivity());
                        lLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        lLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        //lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        lFileTextViewFs.setLayoutParams(lLayoutParams);
                        lFileTextViewFs.setId(2);
                        lFileTextViewFs.setText(readableFileSize(lResponseItem.getLong("n02")));
                        lRelativeLayout.addView(lFileTextViewFs);

                        TextView lFileTextViewFn = new TextView(getActivity());
                        lLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        lLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        //lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        //lLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        lLayoutParams.addRule(RelativeLayout.LEFT_OF, lFileTextViewFs.getId());
                        lLayoutParams.addRule(RelativeLayout.RIGHT_OF, lImageView.getId());
                        lFileTextViewFn.setLayoutParams(lLayoutParams);
                        lFileTextViewFn.setText(lResponseItem.getString("s01"));
                        lRelativeLayout.addView(lFileTextViewFn);

                        mDocsView.addView(lRelativeLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                ClaimHistoryFragment.this.getListView().addFooterView(mDocsView);
            }
            docsLoaded = true;
            preListener();
            //mCallback.onClaimRead(ClaimInfoFragment.this,mClaim);
        }
    }

    public class Attach {
        private final String mFileName;
        private final long mFileRn;

        public Attach(String mFileName, long mFileRn) {
            this.mFileName = mFileName;
            this.mFileRn = mFileRn;
        }

        public String getFileName() {
            return mFileName;
        }

        public long getFileRn() {
            return mFileRn;
        }
    }


}