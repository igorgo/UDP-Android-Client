package ua.parus.pmo.parus8claims.objects.claim.actions;


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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.SimpleSpinner;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.objects.dicts.Builds;
import ua.parus.pmo.parus8claims.objects.dicts.Releases;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class ClaimForwardFragment extends Fragment {

    private static final String TAG = ClaimSendFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    private static final String ARG_PARAM2 = "session";
    private static Claim claim;
    private static String session;
    private View rootView;
    public Holder holder;


    public static ClaimForwardFragment newInstance(Claim claim, String session) {
        ClaimForwardFragment fragment = new ClaimForwardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, claim);
        args.putString(ARG_PARAM2, session);
        fragment.setArguments(args);
        return fragment;
    }

    public ClaimForwardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            claim = (Claim) getArguments().getSerializable(ARG_PARAM1);
            session = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_claim_forward, container, false);
        this.holder = new Holder();
        return rootView;
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
            groupFix = (LinearLayout)  rootView.findViewById(R.id.groupFix);
            releaseFix = (SimpleSpinner) rootView.findViewById(R.id.releaseFixSpinner);
            buildFix = (SimpleSpinner) rootView.findViewById(R.id.buildFixSpinner);
            priority = (EditText) rootView.findViewById(R.id.priorityEdit);
            groupFix.setVisibility(View.GONE);


            state.setOnValueChangedListener(
                    new SimpleSpinner.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
                            List<String> execsD = new ArrayList<>();
                            List<String> execsV = new ArrayList<>();
                            try {
                                RestRequest restRequest = new RestRequest("nextsend/","GET");
                                restRequest.addInParam("session", session);
                                restRequest.addInParam("rn", String.valueOf(claim.rn));
                                restRequest.addInParam("point", String.valueOf(valueLong));
                                JSONArray items = restRequest.getAllRows();
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject item = items.getJSONObject(i);
                                        execsV.add(item.getString("s01"));
                                        execsD.add(item.getString("s02"));
                                    }
                                }
                            } catch (MalformedURLException | JSONException e) {
                                e.printStackTrace();
                            }
                            if (execsV.size() > 0) {
                                send.setItemsStringVals(execsD, execsV, execsV.get(0));
                            }

                            releaseFix.setOnValueChangedListener(
                                    new SimpleSpinner.OnValueChangedListener() {
                                        @Override
                                        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
                                            if ((valueString == null) || valueString.isEmpty()) {
                                                buildFix.setEnabled(false);
                                                buildFix.clear();
                                            } else {
                                                buildFix.setEnabled(true);
                                                buildFix.setItemsStringVals(
                                                        Builds.getBuildsDisplayNames(getActivity(), valueString, false),
                                                        Builds.getBuildsCodes(getActivity(), valueString, false),
                                                        claim.buildFix == null ? "" : Builds.buildName(valueString, claim.buildFix)
                                                );
                                            }
                                        }
                                    }
                            );
                            releaseFix.setItems(
                                    Releases.getReleasesNames(
                                            getActivity(), null, false, ""),
                                    claim.releaseFix == null ? null : claim.releaseFix.name);
                            priority.setText(String.valueOf(claim.priority));


                            boolean isPmo = ((ClaimApplication) getActivity().getApplication()).isPmoUser();
                            boolean toInstall = state.getValueDisplay().equals("Включ в ИНСТ");
                            boolean toReview = state.getValueDisplay().equals("На рассмотрении");
                            boolean toTest = state.getValueDisplay().equals("ТестПроверка");
                            boolean fromInit = claim.state.equals("Инициировано");

                            boolean needSetRelFix = fromInit && (toReview || toTest ) && isPmo;
                            boolean needSetBldFix = toInstall && isPmo;

                            groupFix.setVisibility(needSetRelFix  || needSetBldFix ?   View.VISIBLE : View.GONE);
                            buildFix.setEnabled( needSetBldFix );
                        }
                    }
            );
            List<String> statesD = new ArrayList<>();
            List<Long> statesV = new ArrayList<>();
            try {
                RestRequest restRequest = new RestRequest("nextpoint/","GET");
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
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            if (statesV.size() > 0) {
                state.setItemsLongVals(statesD, statesV, statesV.get(0));
            }

        }
    }

}
