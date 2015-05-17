package ua.parus.pmo.parus8claims.objects.claim.actions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.SimpleSpinner;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class ClaimSendFragment extends Fragment {

    private static final String TAG = ClaimSendFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    private static final String ARG_PARAM2 = "session";
    private static Claim claim;
    private static String session;
    public Holder holder;
    private View rootView;

    public ClaimSendFragment() {
        // Required empty public constructor
    }

    public static ClaimSendFragment newInstance(Claim claim, String session) {
        ClaimSendFragment fragment = new ClaimSendFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, claim);
        args.putString(ARG_PARAM2, session);
        fragment.setArguments(args);
        return fragment;
    }

    private static List<String> getExecutors() {
        List<String> executors = new ArrayList<>();
        try {
            RestRequest restRequest = new RestRequest("send/", "GET");
            restRequest.addInParam("session", session);
            restRequest.addInParam("rn", String.valueOf(claim.rn));
            JSONArray items = restRequest.getAllRows();
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    executors.add(item.getString("s01"));
                }
            }
        } catch (MalformedURLException | JSONException | ConnectException e) {
            e.printStackTrace();
        }
        return executors;
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
        rootView = inflater.inflate(R.layout.fragment_claim_send, container, false);
        this.holder = new Holder();
        return rootView;
    }

    class Holder {
        public final SimpleSpinner send;
        public final EditText note;

        public Holder() {
            send = (SimpleSpinner) rootView.findViewById(R.id.sendSpinner);
            note = (EditText) rootView.findViewById(R.id.noteEdit);
            List<String> executorsV = new ArrayList<>();
            List<String> executorsD = new ArrayList<>();
            try {
                RestRequest restRequest = new RestRequest("send/", "GET");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                JSONArray items = restRequest.getAllRows();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        executorsV.add(item.getString("s01"));
                        executorsD.add(item.getString("s02"));
                    }
                }
            } catch (MalformedURLException | JSONException | ConnectException e) {
                e.printStackTrace();
            }
            if (executorsV.size() > 0) {
                send.setItemsStringVals(executorsD, executorsV, executorsV.get(0));
            }
        }
    }
}
