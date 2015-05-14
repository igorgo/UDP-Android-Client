package ua.parus.pmo.parus8claims.objects.claim.hist;

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

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
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
    private long claimRn;
    private String session;
    private ClaimHistListAdapter claimHistListAdapter;
    private Claim claim;
    private View infoView;
    private boolean histLoaded;
    private boolean infoLoaded;
    private boolean docsLoaded;
    private ClaimHistoryFragmentInterface claimHistoryFragmentInterface;
    private boolean hasDocs;

    public ClaimHistoryFragment() {
    }

    public static ClaimHistoryFragment newInstance(long claimRn, String session, boolean hasDocs) {
        ClaimHistoryFragment claimHistoryFragment = new ClaimHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RN, claimRn);
        args.putString(ARG_SESSION, session);
        args.putBoolean(ARG_HAS_DOCS, hasDocs);
        claimHistoryFragment.setArguments(args);
        return claimHistoryFragment;
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
            this.claimRn = getArguments().getLong(ARG_RN);
            this.session = getArguments().getString(ARG_SESSION);
            this.hasDocs = getArguments().getBoolean(ARG_HAS_DOCS, false);
        }
        this.histLoaded = false;
        this.infoLoaded = false;
        this.docsLoaded = !this.hasDocs;

        this.claim = new Claim();
        new GetClaimAsyncTask().execute(this.claimRn);
        new GetHistAsyncTask().execute(this.claimRn);
        if (this.hasDocs) new GetDocsAsyncTask().execute(this.claimRn);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.claimHistoryFragmentInterface = (ClaimHistoryFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ClaimHistoryFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.claimHistoryFragmentInterface = null;
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.infoView = getActivity().getLayoutInflater().inflate(R.layout.fragment_claim_info, null);
        View rootView = inflater.inflate(R.layout.fragment_claim_hist, container, false);
        LinearLayout progressFrame = (LinearLayout) rootView.findViewById(R.id.progress_container);
        progressFrame.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        FrameLayout listFrame = (FrameLayout) rootView.findViewById(R.id.list_container);
        listFrame.setId(INTERNAL_LIST_CONTAINER_ID);
        return rootView;
    }

    private void preListener() {
        if (this.infoLoaded && this.histLoaded && this.docsLoaded) {
            setListAdapter(this.claimHistListAdapter);
            if (this.claimHistoryFragmentInterface != null) {
                this.claimHistoryFragmentInterface.onHistoryLoaded(ClaimHistoryFragment.this, this.claim);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            if (this.claimHistoryFragmentInterface != null) {
                this.claimHistoryFragmentInterface.onDocumDownloadRequest((Attach) v.getTag());
            }
            Log.i(TAG, "Click on image of doc rn=" + String.valueOf(v.getTag()));
        }
    }

    public interface ClaimHistoryFragmentInterface {
        void onHistoryLoaded(ClaimHistoryFragment fragment, Claim claim);

        void onDocumDownloadRequest(Attach attach);
    }

    private class GetHistAsyncTask extends AsyncTask<Long, Void, Boolean> {

        private final ClaimHistoryFragment that = ClaimHistoryFragment.this;

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                that.claimHistListAdapter = new ClaimHistListAdapter(getActivity(), params[0]);
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
                that.histLoaded = true;
            that.preListener();

        }
    }

    private class GetClaimAsyncTask extends AsyncTask<Long, Void, Void> {

        private final ClaimHistoryFragment that = ClaimHistoryFragment.this;

        @Override
        protected Void doInBackground(Long... params) {
            that.claim.readClaimFromServer(getActivity(), that.session, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            that.claim.populateToView(that.infoView);
            that.getListView().addHeaderView(that.infoView);
            that.infoLoaded = true;
            that.preListener();
        }
    }

    @SuppressWarnings("ResourceType")
    private class GetDocsAsyncTask extends AsyncTask<Long, Void, JSONArray> {

        private final String URL_DOCS = "claimdocs/";
        private final String PARAM_SESSION = "session";
        private final String PARAM_PRN = "prn";

        @Override
        protected JSONArray doInBackground(Long... params) {

            try {
                RestRequest restRequest = new RestRequest(URL_DOCS);
                restRequest.addInParam(PARAM_SESSION, ClaimHistoryFragment.this.session);
                restRequest.addInParam(PARAM_PRN, String.valueOf(params[0]));
                JSONArray response = restRequest.getAllRows();
                if (response != null) return response;

            } catch (MalformedURLException | ConnectException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            if (response != null) {
                LinearLayout docsView = new LinearLayout(getActivity());
                docsView.setOrientation(LinearLayout.VERTICAL);
                docsView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
                int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                docsView.setPadding(padding, padding, padding, padding);

                LinearLayout headerLayout = new LinearLayout(getActivity());
                headerLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                TextView headerText = new TextView(getActivity());
                LinearLayout.LayoutParams lHeaderLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                headerText.setLayoutParams(lHeaderLayoutParams);
                headerText.setText(getString(R.string.attached_docums));
                headerText.setGravity(Gravity.CENTER);
                headerText.setTypeface(Typeface.DEFAULT_BOLD);
                headerLayout.addView(headerText);
                docsView.addView(headerLayout);

                for (int i = 0; i < response.length(); i++) {
                    JSONObject responseItem;
                    try {
                        responseItem = response.getJSONObject(i);
                        RelativeLayout docRowLayout = new RelativeLayout(getActivity());
                        docRowLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


                        ImageView downloadImage = new ImageView(getActivity());
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        downloadImage.setLayoutParams(layoutParams);
                        downloadImage.setImageResource(R.drawable.ic_action_download);
                        downloadImage.setId(1);
                        Attach attach = new Attach(responseItem.getString("s01"), responseItem.getLong("n01"));
                        downloadImage.setTag(attach);
                        downloadImage.setClickable(true);
                        downloadImage.setOnClickListener(ClaimHistoryFragment.this);

                        docRowLayout.addView(downloadImage);

                        TextView fileSizeTextView = new TextView(getActivity());
                        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        fileSizeTextView.setLayoutParams(layoutParams);
                        fileSizeTextView.setId(2);
                        fileSizeTextView.setText(readableFileSize(responseItem.getLong("n02")));
                        docRowLayout.addView(fileSizeTextView);

                        TextView fileNameTextView = new TextView(getActivity());
                        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        layoutParams.addRule(RelativeLayout.LEFT_OF, fileSizeTextView.getId());
                        layoutParams.addRule(RelativeLayout.RIGHT_OF, downloadImage.getId());
                        fileNameTextView.setLayoutParams(layoutParams);
                        fileNameTextView.setText(responseItem.getString("s01"));
                        docRowLayout.addView(fileNameTextView);

                        docsView.addView(docRowLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                ClaimHistoryFragment.this.getListView().addFooterView(docsView);
            }
            ClaimHistoryFragment.this.docsLoaded = true;
            ClaimHistoryFragment.this.preListener();
            //mCallback.onClaimRead(ClaimInfoFragment.this,this.claim);
        }
    }

    public class Attach {
        private final String fileName;
        private final long fileRn;

        public Attach(String fileName, long fileRn) {
            this.fileName = fileName;
            this.fileRn = fileRn;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileRn() {
            return fileRn;
        }
    }


}