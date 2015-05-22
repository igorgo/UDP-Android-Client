package ua.parus.pmo.parus8claims.objects.claim;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.TextView;
import ua.parus.pmo.parus8claims.objects.dicts.Build;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.Release;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.rest.RestRequest;

public class Claim implements Serializable {
    public static final int TYPE_UPWORK = 1;
    public static final int TYPE_REBUKE = 2;
    public static final int TYPE_ERROR = 3;
    public static final int STATUS_TYPE_WAIT = 1;
    public static final int STATUS_TYPE_WORK = 2;
    public static final int STATUS_TYPE_DONE = 3;
    public static final int STATUS_TYPE_NEGATIVE = 4;
    public static final int EXECUTOR_TYPE_PERSON = 1;
    public static final int EXECUTOR_TYPE_GROUP = 2;

    @SuppressWarnings("unused")
    private static final String TAG = Claim.class.getSimpleName();
    private static final String FIELD_NUMBER = "s01";
    private static final String FIELD_TYPE = "n01";
    private static final String FIELD_STATE = "s02";
    private static final String FIELD_STATE_TYPE = "n02";
    private static final String FIELD_REG_DATE = "s03";
    private static final String FIELD_INITIATOR = "s04";
    private static final String FIELD_CHANGE_DATE = "s05";
    private static final String FIELD_EXECUTOR = "s06";
    private static final String FIELD_EXECUTOR_TYPE = "n03";
    private static final String FIELD_RELEASE_FOUND = "n04";
    private static final String FIELD_BUILD_FOUND = "n05";
    private static final String FIELD_RELEASE_FIX = "n06";
    private static final String FIELD_BUILD_FIX = "n07";
    private static final String FIELD_PRIORITY = "n08";
    private static final String FIELD_APPLICATION = "s07";
    private static final String FIELD_UNIT = "s08";
    private static final String FIELD_UNIT_FUNC = "s09";
    private static final String FIELD_DESCRIPTION = "s10";
    private static final String FIELD_CAN_UPDATE = "n09";
    private static final String FIELD_CAN_DELETE = "n10";
    private static final String FIELD_CAN_FORWARD = "n11";
    private static final String FIELD_CAN_SEND = "n12";
    private static final String FIELD_CAN_RETURN = "n13";
    private static final String FIELD_CAN_CLOSE = "n14";
    private static final String FIELD_CAN_ADD_NOTE = "n15";
    private static final String FIELD_CAN_ATTACH = "n16";
    private static final String URL_CLAIM = "claim/";
    private static final String PARAM_SESSION = "session";
    private static final String PARAM_RN = "rn";

    public long rn;
    public String number;
    public int type;
    public Release releaseFound;
    public Build buildFound;
    public Release releaseFix;
    public Build buildFix;
    public String releaseDisplayed;
    public boolean hasReleaseFix;
    public String registrationDate;
    public String application;
    public String unit;
    public String unitFunc;
    public String state;
    public int stateType;
    public String initiator;
    public String description;
    public String executor;
    public String changeDate;
    public int executorType;
    public boolean hasAttach;
    public int priority;
    public boolean canUpdate;
    public boolean canDelete;
    public boolean canForward;
    public boolean canSend;
    public boolean canReturn;
    public boolean canClose;
    public boolean canAddNote;
    public boolean canAttach;

    public void set_hasReleaseFix(int hasReleaseFix) {
        this.hasReleaseFix = (hasReleaseFix == 1);
    }

    public void set_hasAttach(int hasAttach) {
        this.hasAttach = (hasAttach == 1);
    }

    public void readClaimFromServer(Context context, String session, long rn) {
        this.rn = rn;
        readClaimFromServer(context, session);
    }

    @SuppressWarnings("WeakerAccess")
    public void readClaimFromServer(Context context, String session) {
        if (!((this.rn > 0) && (session != null) && (!session.isEmpty()))) return;
        try {
            RestRequest restRequest = new RestRequest(URL_CLAIM);
            restRequest.addInParam(PARAM_SESSION, session);
            restRequest.addInParam(PARAM_RN, String.valueOf(this.rn));
            JSONArray response = restRequest.getPageRows();
            if (response == null) return;
            if (!(response.length() > 0)) return;
            JSONObject c = response.getJSONObject(0);
            this.number = c.getString(FIELD_NUMBER);
            this.type = c.getInt(FIELD_TYPE);
            this.state = c.getString(FIELD_STATE);
            this.stateType = c.getInt(FIELD_STATE_TYPE);
            this.registrationDate = c.getString(FIELD_REG_DATE);
            this.initiator = c.getString(FIELD_INITIATOR);
            this.changeDate = c.getString(FIELD_CHANGE_DATE);
            this.executor = c.optString(FIELD_EXECUTOR);
            this.executorType = c.getInt(FIELD_EXECUTOR_TYPE);
            long releaseFound = c.optLong(FIELD_RELEASE_FOUND);
            if (releaseFound > 0) {
                this.releaseFound = ReleaseHelper.getRelease(context, releaseFound);
                long buildFound = c.optLong(FIELD_BUILD_FOUND);
                if (buildFound > 0) {
                    this.buildFound = BuildHelper.getBuild(context, releaseFound, buildFound);
                }
            }
            long releaseTo = c.optLong(FIELD_RELEASE_FIX);
            if (releaseTo > 0) {
                this.releaseFix = ReleaseHelper.getRelease(context, releaseTo);
                long buildTo = c.optLong(FIELD_BUILD_FIX);
                if (buildTo > 0) {
                    this.buildFix = BuildHelper.getBuild(context, releaseTo, buildTo);
                }
            }
            this.priority = c.getInt(FIELD_PRIORITY);
            this.application = c.optString(FIELD_APPLICATION);
            this.unit = c.optString(FIELD_UNIT);
            this.unitFunc = c.optString(FIELD_UNIT_FUNC);
            this.description = c.optString(FIELD_DESCRIPTION);
            this.canUpdate = c.getInt(FIELD_CAN_UPDATE) == 1;
            this.canDelete = c.getInt(FIELD_CAN_DELETE) == 1;
            this.canForward = c.getInt(FIELD_CAN_FORWARD) == 1;
            this.canSend = c.getInt(FIELD_CAN_SEND) == 1;
            this.canReturn = c.getInt(FIELD_CAN_RETURN) == 1;
            this.canClose = c.getInt(FIELD_CAN_CLOSE) == 1;
            this.canAddNote = c.getInt(FIELD_CAN_ADD_NOTE) == 1;
            this.canAttach = c.getInt(FIELD_CAN_ATTACH) == 1;
        } catch (MalformedURLException | JSONException | ConnectException e) {
            e.printStackTrace();
        }
    }

    public void populateToView(View view) {
        TextView textView;
        ImageView imageView;
        if ((textView = (TextView) view.findViewById(R.id.fciNumberText)) != null) {
            textView.setText(this.number);
        }
        if ((imageView = (ImageView) view.findViewById(R.id.fciTypeImage)) != null) {
            switch (this.type) {
                case Claim.TYPE_UPWORK:
                    imageView.setImageResource(R.drawable.ic_workup);
                    break;
                case Claim.TYPE_REBUKE:
                    imageView.setImageResource(R.drawable.ic_warn);
                    break;
                case Claim.TYPE_ERROR:
                    imageView.setImageResource(R.drawable.ic_error);
                    break;
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciTypeText)) != null) {
            switch (this.type) {
                case Claim.TYPE_UPWORK:
                    textView.setText(R.string.claim_type_addon);
                    break;
                case Claim.TYPE_REBUKE:
                    textView.setText(R.string.claim_type_rebuke);
                    break;
                case Claim.TYPE_ERROR:
                    textView.setText(R.string.claim_type_error);
                    break;
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciInitiatorText)) != null) {
            textView.setText(this.initiator);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciRegDateText)) != null) {
            textView.setText(this.registrationDate);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciApplicationText)) != null) {
            textView.setText(this.application);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciUnitText)) != null) {
            textView.setText(this.unit);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciUnitFuncText)) != null) {
            if (this.unitFunc == null || this.unitFunc.isEmpty()) {
                ((View) textView.getParent()).setVisibility(View.GONE);
            } else {
                textView.setText(this.unitFunc);
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciReleaseFromText)) != null) {
            if (this.releaseFound != null) {
                if (this.buildFound != null)
                    textView.setText(this.buildFound.displayName);
                else
                    textView.setText(this.releaseFound.name);
            } else {
                ((View) textView.getParent()).setVisibility(View.GONE);
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciStatusText)) != null) {
            textView.setText(this.state);
            switch (this.stateType) {
                case Claim.STATUS_TYPE_WAIT:
                    textView.setTextAppearance(view.getContext(), R.style.claim_state_wait);
                    break;
                case Claim.STATUS_TYPE_WORK:
                    textView.setTextAppearance(view.getContext(), R.style.claim_state_work);
                    break;
                case Claim.STATUS_TYPE_DONE:
                    textView.setTextAppearance(view.getContext(), R.style.claim_state_done);
                    break;
                case Claim.STATUS_TYPE_NEGATIVE:
                    textView.setTextAppearance(view.getContext(), R.style.claim_state_negotive);
                    break;
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciChangeDateText)) != null) {
            textView.setText(this.changeDate);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciExecutorText)) != null) {
            if (this.executorType > 0) {
                textView.setText(this.executor);
                if ((imageView = (ImageView) view.findViewById(R.id.fciExecutorImage)) != null) {
                    if (this.executorType == 1) {
                        imageView.setImageResource(R.drawable.ic_action_person);
                    } else {
                        imageView.setImageResource(R.drawable.ic_action_group);
                    }
                }
            } else {
                ((View) textView.getParent()).setVisibility(View.GONE);
            }
        }
        if ((textView = (TextView) view.findViewById(R.id.fciPriorityText)) != null) {
            textView.setText(String.valueOf(this.priority));
            if (this.priority < 5)
                textView.setTextAppearance(view.getContext(), R.style.claim_state_done);
            else if (this.priority > 5)
                textView.setTextAppearance(view.getContext(), R.style.claim_state_negotive);
            else
                textView.setTextAppearance(view.getContext(), R.style.claim_state_wait);
        }
        if ((textView = (TextView) view.findViewById(R.id.fciReleaseToText)) != null) {
            if (this.releaseFix != null) {
                if (this.buildFix != null)
                    textView.setText(this.buildFix.displayName);
                else
                    textView.setText(this.releaseFix.name);
            } else {
                ((View) textView.getParent()).setVisibility(View.GONE);
            }
        }
    }
}
