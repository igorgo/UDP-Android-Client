package ua.parus.pmo.parus8claims.objects.claim.actions;


import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import android.widget.RadioGroup;

import java.util.List;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.InputFilterMinMax;
import ua.parus.pmo.parus8claims.gui.MultiSpinner;
import ua.parus.pmo.parus8claims.gui.SemicolonTokenizer;
import ua.parus.pmo.parus8claims.gui.SimpleSpinner;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;


public class ClaimAddFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = ClaimAddFragment.class.getSimpleName();
    private static final int DEFAULT_PRIORITY = 5;
    public Holder holder;
    private View rootView;
    private boolean isPmoUser;

    public ClaimAddFragment() {
        // Required empty public constructor
    }

    public static ClaimAddFragment newInstance() {
        return new ClaimAddFragment() ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isPmoUser = ((ClaimApplication) getActivity().getApplication()).isPmoUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_claim_add, container, false);
        this.holder = new Holder();
        this.holder.initFields();
        return this.rootView;
    }

    private class GetBuildsTask extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private List<String> DisplayNames;
        private List<String> Codes;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            DisplayNames = BuildHelper.getBuildsDisplayNames(getActivity(), params[0], true);
            Codes = BuildHelper.getBuildsCodes(getActivity(), params[0], true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            holder.build.setItemsStringVals(DisplayNames, Codes, "");
            holder.build.setEnabled(true);
            super.onPostExecute(aVoid);
        }
    }

    class Holder {
        public final RadioGroup type;
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
        private final ClaimAddFragment that;
        private boolean needRefreshUnitApps = true;
        private boolean needRefreshUnitFunc = true;



        public Holder() {
            View view = ClaimAddFragment.this.rootView;
            that = ClaimAddFragment.this;
            this.type = (RadioGroup) view.findViewById(R.id.typeRadioGroup);
            this.release = (SimpleSpinner) view.findViewById(R.id.releaseSpinner);
            this.build = (SimpleSpinner) view.findViewById(R.id.buildSpinner);
            this.releaseFix = (SimpleSpinner) view.findViewById(R.id.releaseFixSpinner);
            this.buildFix = (SimpleSpinner) view.findViewById(R.id.buildFixSpinner);
            this.priority = (EditText) view.findViewById(R.id.priorityEdit);
            this.priority.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10")});
            this.content = (EditText) view.findViewById(R.id.contentEdit);
            this.unit = (MultiAutoCompleteTextView) view.findViewById(R.id.unitsText);
            this.unitApp = (MultiSpinner) view.findViewById(R.id.appSpinner);
            this.unitFunc = (SimpleSpinner) view.findViewById(R.id.funcSpinner);
            this.groupFix = (LinearLayout) view.findViewById(R.id.groupFix);
        }



        private void initFields() {
            this.release.setOnValueChangedListener(
                    new SimpleSpinner.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(SimpleSpinner sender, String valueString, Long valueLong) {
                            if ((valueString == null) || valueString.isEmpty()) {
                                build.setEnabled(false);
                                build.clear();
                            } else {
                                new GetBuildsTask().execute(valueString);
                            }
                        }
                    }
            );
            this.release.setItems(
                    ReleaseHelper.getReleasesNames(
                            getActivity(), null, true, ""),
                    "");
            this.releaseFix.setItems(
                    ReleaseHelper.getReleasesNames(
                            getActivity(), null, false, ""),
                    "");
            ((View) this.buildFix.getParent()).setVisibility(View.GONE);
            this.priority.setText(String.valueOf(DEFAULT_PRIORITY));
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
            this.groupFix.setVisibility(isPmoUser ? View.VISIBLE : View.GONE);


        }
    }
}


