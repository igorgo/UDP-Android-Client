package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.InputFilterMinMax;
import ua.parus.pmo.parus8claims.gui.MultiSpinner;
import ua.parus.pmo.parus8claims.gui.SemicolonTokenizer;
import ua.parus.pmo.parus8claims.gui.SimpleSpinner;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;


public class ClaimEditFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = ClaimEditFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "claim";
    public Holder holder;
    private Claim claim;
    private View rootView;
    private boolean isPmoUser;

    public ClaimEditFragment() {
        // Required empty public constructor
    }

    public static ClaimEditFragment newInstance(Claim claim) {
        ClaimEditFragment claimEditFragment = new ClaimEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, claim);
        claimEditFragment.setArguments(args);
        return claimEditFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.claim = (Claim) getArguments().getSerializable(ARG_PARAM1);
        }
        this.isPmoUser = ((ClaimApplication) getActivity().getApplication()).isPmoUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_claim_edit, container, false);
        this.claim.populateToView(rootView);
        this.holder = new Holder();
        this.holder.populateFromClaim(this.claim);
        return this.rootView;
    }

    class Holder {
        public final SimpleSpinner release;
        public final SimpleSpinner build;
        public final SimpleSpinner releaseFix;
        public final SimpleSpinner buildFix;
        public final EditText priority;
        public final MultiAutoCompleteTextView unit;
        public final MultiSpinner unitApp;
        public final SimpleSpinner unitFunc;
        public final EditText content;
        private final LinearLayout groupFix;
        private final ClaimEditFragment that;
        private boolean needRefreshUnitApps = true;
        private boolean needRefreshUnitFunc = true;

        public Holder() {
            View view = ClaimEditFragment.this.rootView;
            that = ClaimEditFragment.this;
            this.release = (SimpleSpinner) view.findViewById(R.id.releaseSpinner);
            this.build = (SimpleSpinner) view.findViewById(R.id.buildSpinner);
            this.releaseFix = (SimpleSpinner) view.findViewById(R.id.releaseFixSpinner);
            this.buildFix = (SimpleSpinner) view.findViewById(R.id.buildFixSpinner);
            this.priority = (EditText) view.findViewById(R.id.priorityEdit);
            this.content = (EditText) view.findViewById(R.id.contentEdit);
            this.priority.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10")});
            this.unit = (MultiAutoCompleteTextView) view.findViewById(R.id.unitsText);
            this.unitApp = (MultiSpinner) view.findViewById(R.id.appSpinner);
            this.unitFunc = (SimpleSpinner) view.findViewById(R.id.funcSpinner);
            this.groupFix = (LinearLayout) view.findViewById(R.id.groupFix);
        }

        private void populateFromClaim(Claim claim) {
            this.release.setOnValueChangedListener(
                    new SimpleSpinner.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
                            build.setItemsStringVals(
                                    BuildHelper.getBuildsDisplayNames(getActivity(), release.getValueString(), true),
                                    BuildHelper.getBuildsCodes(getActivity(), release.getValueString(), true),
                                    BuildHelper.buildName(release.getValueString(), that.claim.buildFound)
                            );
                        }
                    }
            );
            this.release.setItems(
                    ReleaseHelper.getReleasesNames(
                            getActivity(), null, true, ""),
                    claim.releaseFound.name);
            this.releaseFix.setOnValueChangedListener(
                    new SimpleSpinner.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
                            if ((valueString == null) || valueString.isEmpty()) {
                                buildFix.setEnabled(false);
                                buildFix.clear();
                            } else {
                                buildFix.setEnabled(true);
                                buildFix.setItemsStringVals(
                                        BuildHelper.getBuildsDisplayNames(getActivity(), valueString, false),
                                        BuildHelper.getBuildsCodes(getActivity(), valueString, false),
                                        that.claim.buildFix == null ? "" : BuildHelper.buildName(valueString, that.claim.buildFix)
                                );
                            }
                        }
                    }
            );
            this.releaseFix.setItems(
                    ReleaseHelper.getReleasesNames(
                            getActivity(), null, false, ""),
                    claim.releaseFix == null ? null : claim.releaseFix.name);
            this.priority.setText(String.valueOf(claim.priority));
            this.unit.setAdapter(
                    new ArrayAdapter<>(
                            that.getActivity(),
                            R.layout.dropdown_multiline_item,
                            R.id.item,
                            UnitHelper.getUnits(that.getActivity())
                    )
            );
            this.unit.setTokenizer(new SemicolonTokenizer());
            this.unit.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            needRefreshUnitApps = true;
                            unitApp.setValue(getString(R.string.all_ass_with_unit));
                            needRefreshUnitFunc = true;
                            unitFunc.setValue("");
                        }
                    }
            );
            this.unit.setText(claim.unit);
            this.unitApp.setAllCheckedText(getString(R.string.all_ass_with_unit));
            this.unitApp.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (needRefreshUnitApps && event.getAction() == MotionEvent.ACTION_DOWN) {
                                String s = unitApp.getValue();
                                unitApp.setItems(UnitHelper.getUnitApps(getActivity(), unit.getText().toString()), true);
                                unitApp.setValue(s);
                                needRefreshUnitApps = false;
                            }
                            return false;
                        }
                    }
            );
            this.unitApp.setValue(claim.application);
            this.unitFunc.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (needRefreshUnitFunc && event.getAction() == MotionEvent.ACTION_DOWN) {
                                String s = unitFunc.getValueString();
                                unitFunc.setItems(UnitHelper.getUnitFuncs(getActivity(), unit.getText().toString()), s);
                                needRefreshUnitFunc = false;
                            }
                            return false;
                        }
                    }
            );
            this.unitFunc.setValue(claim.unitFunc);
            this.content.setText(claim.description);
            this.groupFix.setVisibility(isPmoUser ? View.VISIBLE : View.GONE);
        }
    }
}
