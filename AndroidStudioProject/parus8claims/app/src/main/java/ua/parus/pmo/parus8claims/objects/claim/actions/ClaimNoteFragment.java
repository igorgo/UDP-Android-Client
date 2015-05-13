package ua.parus.pmo.parus8claims.objects.claim.actions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.objects.claim.Claim;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClaimNoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClaimNoteFragment extends Fragment {
    private static final String ARG_PARAM1 = "claim";
    private View rootView;
    public EditText note;


    public static ClaimNoteFragment newInstance() {
        ClaimNoteFragment fragment = new ClaimNoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ClaimNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_claim_note, container, false);
        this.note = (EditText)  rootView.findViewById(R.id.noteEdit);
        return this.rootView;
    }


}
