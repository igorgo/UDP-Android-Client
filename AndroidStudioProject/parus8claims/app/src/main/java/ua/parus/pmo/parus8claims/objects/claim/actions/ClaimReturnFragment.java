package ua.parus.pmo.parus8claims.objects.claim.actions;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class ClaimReturnFragment extends Fragment {

    private static final String TAG = ClaimReturnFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    private static final String ARG_PARAM2 = "session";
    private static Claim claim;
    private static String session;
    public Holder holder;
    private View rootView;

    public ClaimReturnFragment() {
        // Required empty public constructor
    }

    public static ClaimReturnFragment newInstance(Claim claim, String session) {
        ClaimReturnFragment fragment = new ClaimReturnFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_claim_return, container, false);
        this.holder = new Holder();
        return rootView;
    }


    class Holder {
        public final EditText note;
        public final TextView message;
        public Holder() {
            note = (EditText) rootView.findViewById(R.id.noteEdit);
            message = (TextView) rootView.findViewById(R.id.returnMessage);
            RestRequest restRequest = null;
            try {
                restRequest = new RestRequest("return/","GET");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                JSONArray items = restRequest.getAllRows();
                if (items != null && items.length()>0 ) {
                    JSONObject item = items.getJSONObject(0);
                    message.setText(item.optString("s01"));
                }
            } catch (MalformedURLException | JSONException | ConnectException e) {
                e.printStackTrace();
            }
        }
    }


}
