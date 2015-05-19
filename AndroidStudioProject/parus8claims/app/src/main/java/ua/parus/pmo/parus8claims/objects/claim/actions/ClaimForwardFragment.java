package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.gui.SimpleSpinner;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class ClaimForwardFragment extends Fragment implements SimpleSpinner.OnValueChangedListener {

    @SuppressWarnings("unused")
    private static final String TAG = ClaimSendFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    private static final String ARG_PARAM2 = "session";
    private static Claim claim;
    private static String session;
    public Holder holder;
    private View rootView;
    private ProgressDialog progressDialog;

    public ClaimForwardFragment() {
        // Required empty public constructor
    }

    public static ClaimForwardFragment newInstance(Claim claim, String session) {
        ClaimForwardFragment fragment = new ClaimForwardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, claim);
        args.putString(ARG_PARAM2, session);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            claim = (Claim) getArguments().getSerializable(ARG_PARAM1);
            session = getArguments().getString(ARG_PARAM2);
        }
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setMessage(getString(R.string.please_wait));
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_claim_forward, container, false);
        this.holder = new Holder();
        new GetStatesTask().execute();
        return rootView;
    }

    @Override public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
        if (sender.getId() == holder.state.getId()) {
            holder.releaseFix.setOnValueChangedListener(this);
            holder.releaseFix.setItems(
                    ReleaseHelper.getReleasesNames(
                            getActivity(), null, false, ""),
                    claim.releaseFix == null ? null : claim.releaseFix.name);
            holder.priority.setText(String.valueOf(claim.priority));
            boolean isPmo = ((ClaimApplication) getActivity().getApplication()).isPmoUser();
            boolean toInstall = holder.state.getValueDisplay().equals("Включ в ИНСТ");
            boolean toReview = holder.state.getValueDisplay().equals("На рассмотрении");
            boolean toTest = holder.state.getValueDisplay().equals("ТестПроверка");
            boolean fromInit = claim.state.equals("Инициировано");
            boolean needSetRelFix = fromInit && (toReview || toTest) && isPmo;
            boolean needSetBldFix = toInstall && isPmo;
            holder.groupFix.setVisibility(needSetRelFix || needSetBldFix ? View.VISIBLE : View.GONE);
            holder.buildFix.setEnabled(needSetBldFix);
            new GetSendTask().execute(valueLong);
        }
        if (sender.getId() == holder.releaseFix.getId()) {
            if ((valueString == null) || valueString.isEmpty()) {
                holder.buildFix.setEnabled(false);
                holder.buildFix.clear();
            } else {
                new GetBuildsTask().execute(valueString);
            }
        }
    }

    private class GetBuildsTask extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private List<String> DisplayNames;
        private List<String> Codes;
        private String releaseFix;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            releaseFix = params[0];
            DisplayNames = BuildHelper.getBuildsDisplayNames(getActivity(), releaseFix, false);
            Codes = BuildHelper.getBuildsCodes(getActivity(), releaseFix, false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            holder.buildFix.setItemsStringVals(DisplayNames, Codes,
                    claim.buildFix == null ? "" : BuildHelper.buildName(releaseFix, claim.buildFix));
            holder.buildFix.setEnabled(true);
            super.onPostExecute(aVoid);
        }
    }

    class Holder {
        public final SimpleSpinner state;
        public final SimpleSpinner send;
        public final EditText note;
        public final LinearLayout groupFix;
        public final SimpleSpinner releaseFix;
        public final SimpleSpinner buildFix;
        public final EditText priority;

        public Holder() {
            state = (SimpleSpinner) rootView.findViewById(R.id.stateSpinner);
            send = (SimpleSpinner) rootView.findViewById(R.id.sendSpinner);
            note = (EditText) rootView.findViewById(R.id.noteEdit);
            groupFix = (LinearLayout) rootView.findViewById(R.id.groupFix);
            releaseFix = (SimpleSpinner) rootView.findViewById(R.id.releaseFixSpinner);
            buildFix = (SimpleSpinner) rootView.findViewById(R.id.buildFixSpinner);
            priority = (EditText) rootView.findViewById(R.id.priorityEdit);
            groupFix.setVisibility(View.GONE);
            state.setOnValueChangedListener(ClaimForwardFragment.this);
        }
    }

    private class GetSendTask extends AsyncTask<Long, Void, Integer> {
        final List<String> execsD = new ArrayList<>();
        final List<String> execsV = new ArrayList<>();
        String error;

        @Override protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override protected Integer doInBackground(Long... longs) {
            try {
                RestRequest restRequest = new RestRequest("nextsend/", "GET");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                restRequest.addInParam("point", String.valueOf(longs[0]));
                JSONArray items = restRequest.getAllRows();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        execsV.add(item.optString("s01"));
                        execsD.add(item.getString("s02"));
                    }
                }
                return 0;
            } catch (MalformedURLException | JSONException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPostExecute(Integer result) {
            progressDialog.dismiss();
            if (result == -1) {
                new ErrorPopup(getActivity(), null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                if (execsV.size() > 0) {
                    holder.send.setItemsStringVals(execsD, execsV, execsV.get(0));
                }
            }
            super.onPostExecute(result);
        }
    }

    private class GetStatesTask extends AsyncTask<Void, Void, Integer> {
        final List<String> statesD = new ArrayList<>();
        final List<Long> statesV = new ArrayList<>();
        String error;

        @Override protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override protected Integer doInBackground(Void... voids) {
            try {
                RestRequest restRequest = new RestRequest("nextpoint/", "GET");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                JSONArray items = restRequest.getAllRows();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        statesV.add(item.getLong("n01"));
                        statesD.add(item.getString("s01"));
                    }
                }
                return 0;
            } catch (MalformedURLException | JSONException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPostExecute(Integer result) {
            progressDialog.dismiss();
            if (result == -1) {
                new ErrorPopup(getActivity(), null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                if (statesV.size() > 0) {
                    holder.state.setItemsLongVals(statesD, statesV, statesV.get(0));
                }
            }
            super.onPostExecute(result);
        }
    }
}
