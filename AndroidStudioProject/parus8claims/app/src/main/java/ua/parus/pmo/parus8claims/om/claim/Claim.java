package ua.parus.pmo.parus8claims.om.claim;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.db.Builds;
import ua.parus.pmo.parus8claims.db.BuildsORM;
import ua.parus.pmo.parus8claims.db.Releases;
import ua.parus.pmo.parus8claims.db.ReleasesORM;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igorgo on 20.04.2015.
 */
@SuppressWarnings("DefaultFileTemplate")
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
    private static final String PARAM_SESSION = "PSESSION";
    private static final String PARAM_RN = "PRN";

    public long rn;
    public String number;
    public int type;
    public Releases releaseFound;
    public Builds buildFound;
    public Releases releaseFix;
    public Builds buildFix;
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
            RestRequest lRestRequest = new RestRequest(URL_CLAIM);
            lRestRequest.addHeaderParam(PARAM_SESSION, session);
            lRestRequest.addHeaderParam(PARAM_RN, String.valueOf(this.rn));
            JSONArray response = lRestRequest.getPageRows();
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
            long lReleaseFound = c.optLong(FIELD_RELEASE_FOUND);
            if (lReleaseFound > 0) {
                this.releaseFound = ReleasesORM.getRelease(context, lReleaseFound);
                long lBuildFound = c.optLong(FIELD_BUILD_FOUND);
                if (lBuildFound > 0) {
                    this.buildFound = BuildsORM.getBuild(context, lReleaseFound, lBuildFound);
                }
            }
            long lRelTo = c.optLong(FIELD_RELEASE_FIX);
            if (lRelTo > 0) {
                this.releaseFix = ReleasesORM.getRelease(context, lRelTo);
                long lBldTo = c.optLong(FIELD_BUILD_FIX);
                if (lBldTo > 0) {
                    this.buildFix = BuildsORM.getBuild(context, lRelTo, lBldTo);
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

        } catch (MalformedURLException|JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
