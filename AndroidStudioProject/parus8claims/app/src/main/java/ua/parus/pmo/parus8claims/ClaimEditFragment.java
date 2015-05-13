package ua.parus.pmo.parus8claims;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.parus.pmo.parus8claims.om.claim.Claim;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClaimEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ClaimEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClaimEditFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "claim";

    private Claim mClaim;

    private OnFragmentInteractionListener mListener;
    private View mRootView;

    // TODO: Rename and change types and number of parameters
    public static ClaimEditFragment newInstance(Claim claim) {
        ClaimEditFragment fragment = new ClaimEditFragment();
        Bundle args = new Bundle();
        args.putSerializable (ARG_PARAM1, claim);
        fragment.setArguments(args);
        return fragment;
    }

    public ClaimEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClaim = (Claim) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_claim_edit, container, false);
        ((TextView) mRootView.findViewById(R.id.ttt)).setText(mClaim.buildFound.get_displayName());
        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
