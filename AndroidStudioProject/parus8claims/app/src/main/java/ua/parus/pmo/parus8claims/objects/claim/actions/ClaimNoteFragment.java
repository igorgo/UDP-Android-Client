package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.EditText;

public class ClaimNoteFragment extends Fragment {
    public EditText note;

    public ClaimNoteFragment() {
        // Required empty public constructor
    }

    public static ClaimNoteFragment newInstance() {
        ClaimNoteFragment fragment = new ClaimNoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_note, container, false);
        this.note = (EditText) rootView.findViewById(R.id.noteEdit);
        return rootView;
    }
}
