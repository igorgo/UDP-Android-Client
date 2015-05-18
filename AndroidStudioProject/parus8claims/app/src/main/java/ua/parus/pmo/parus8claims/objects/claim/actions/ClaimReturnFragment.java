package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

    @SuppressWarnings("unused")
    private static final String TAG = ClaimReturnFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    private static final String ARG_PARAM2 = "session";
    private static Claim claim;
    private static String session;
    public Holder holder;
    private View rootView;
    private ProgressDialog progressDialog;


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
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setMessage(getString(R.string.please_wait));
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_claim_return, container, false);
        this.holder = new Holder();
        new GetReturnMessage().execute();
        return rootView;
    }

    class Holder {
        public final EditText note;
        public final TextView message;

        public Holder() {
            note = (EditText) rootView.findViewById(R.id.noteEdit);
            message = (TextView) rootView.findViewById(R.id.returnMessage);
        }
    }

    private class GetReturnMessage extends AsyncTask<Void, Void, String> {
        @Override protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override protected String doInBackground(Void... voids) {
            RestRequest restRequest;
            try {
                restRequest = new RestRequest("return/", "GET");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                JSONArray items = restRequest.getAllRows();
                if (items != null && items.length() > 0) {
                    JSONObject item = items.getJSONObject(0);
                    return item.optString("s01");
                }
            } catch (MalformedURLException | JSONException | ConnectException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (!TextUtils.isEmpty(s)) {
                holder.message.setText(s);
            }
            super.onPostExecute(s);
        }
    }
}
