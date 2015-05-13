package ua.parus.pmo.parus8claims.objects.filter;

import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igor-go on 22.04.2015.
 * ua.parus.pmo.parus8claims.objects.filter
 */
public class Filter {

    public static final String PARAM_FILTER_RN = "filterrn";
    private static final String REST_READ_METHOD = "GET";
    private static final String REST_SAVE_METHOD = "POST";
    private static final String REST_DELETE_METHOD = "POST";
    private static final String PARAM_SESSION = "session";
    private static final String PARAM_FILTER_NAME = "filtername";
    private static final String PARAM_CLAIM_NUMB = "claimnumb";
    private static final String PARAM_CLAIM_VERS = "claimvers";
    private static final String PARAM_CLAIM_RELEASE = "claimrelease";
    private static final String PARAM_CLAIM_BUILD = "claimbuild";
    private static final String PARAM_CLAIM_UNIT = "claimunit";
    private static final String PARAM_CLAIM_APP = "claimapp";
    private static final String PARAM_CLAIM_IM_INIT = "claimiminit";
    private static final String PARAM_CLAIM_IM_EXEC = "claimimperf";
    private static final String PARAM_CLAIM_CONTENT = "claimcontent";
    private static final String PARAM_OUT_RN = "outrn";
    private static final String PARAM_ERROR = "error";
    private static final String REST_READ_URL = "filter/";
    private static final String REST_SAVE_URL = "filter/save/";
    private static final String REST_DELETE_URL = "filter/delete/";
    public String filter_name;
    public long filter_rn;
    public boolean filter_editable;
    public String condNumber;
    public String condUnit;
    public String condApplication;
    public String condVersion;
    public String condRelease;
    public String condBuild;
    public boolean condImInitiator;
    public boolean condImExecutor;
    public String condContent;

    public void readFromServer(Context context) {
        try {
            RestRequest restRequest = new RestRequest(REST_READ_URL, REST_READ_METHOD);
            ClaimApplication claimApplication = (ClaimApplication) ((Activity) context).getApplication();
            restRequest.addInParam(PARAM_SESSION, claimApplication.getSessionId());
            if (this.filter_rn > 0)
                restRequest.addInParam(PARAM_FILTER_RN, String.valueOf(this.filter_rn));
            JSONObject item = restRequest.getJsonContent();
            if (item == null) return;
            if (item.optString(PARAM_ERROR) != null && !item.optString(PARAM_ERROR).isEmpty()) {
                ErrorPopup errorPopup = new ErrorPopup(context);
                errorPopup.showErrorDialog(context.getString(R.string.error_title), item.optString(PARAM_ERROR));
            } else {
                this.filter_name = item.optString(PARAM_FILTER_NAME);
                this.condNumber = item.optString(PARAM_CLAIM_NUMB);
                this.condUnit = item.optString(PARAM_CLAIM_UNIT);
                this.condApplication = item.optString(PARAM_CLAIM_APP);
                this.condVersion = item.optString(PARAM_CLAIM_VERS);
                this.condRelease = item.optString(PARAM_CLAIM_RELEASE);
                this.condBuild = item.optString(PARAM_CLAIM_BUILD);
                this.condImInitiator = item.optInt(PARAM_CLAIM_IM_INIT) == 1;
                this.condImExecutor = item.optInt(PARAM_CLAIM_IM_EXEC) == 1;
                this.condContent = item.optString(PARAM_CLAIM_CONTENT);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        this.condNumber = null;
        this.condUnit = null;
        this.condApplication = null;
        this.condVersion = null;
        this.condRelease = null;
        this.condBuild = null;
        this.condImInitiator = false;
        this.condImExecutor = false;
        this.condContent = null;
    }

    public void saveToServer(Context context) {
        try {
            RestRequest restRequest = new RestRequest(REST_SAVE_URL, REST_SAVE_METHOD);
            restRequest.addInParam(
                    PARAM_SESSION,
                    ((ClaimApplication) ((Activity) context).getApplication()).getSessionId()
            );
            if ((this.filter_rn > 0))
                restRequest.addInParam(PARAM_FILTER_RN, String.valueOf(this.filter_rn));
            restRequest.addInParam(PARAM_FILTER_NAME, this.filter_name);
            restRequest.addInParam(PARAM_CLAIM_NUMB, this.condNumber);
            restRequest.addInParam(PARAM_CLAIM_VERS, this.condVersion);
            restRequest.addInParam(PARAM_CLAIM_RELEASE, this.condRelease);
            restRequest.addInParam(PARAM_CLAIM_BUILD, this.condBuild);
            restRequest.addInParam(PARAM_CLAIM_UNIT, this.condUnit);
            restRequest.addInParam(PARAM_CLAIM_APP, this.condApplication);
            restRequest.addInParam(PARAM_CLAIM_IM_INIT, String.valueOf(this.condImInitiator ? 1 : 0));
            restRequest.addInParam(PARAM_CLAIM_IM_EXEC, String.valueOf(this.condImExecutor ? 1 : 0));
            restRequest.addInParam(PARAM_CLAIM_CONTENT, this.condContent);
            JSONObject response = restRequest.getJsonContent();
            if (response == null) return;
            if (response.optString(PARAM_ERROR) != null && !response.optString(PARAM_ERROR).isEmpty()) {
                new ErrorPopup(context).showErrorDialog(context.getString(R.string.error_title), response.optString(PARAM_ERROR));
            } else {
                this.filter_rn = response.optLong(PARAM_OUT_RN);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void deleteOnServer(Context context) {
        try {
            RestRequest restRequest = new RestRequest(REST_DELETE_URL, REST_DELETE_METHOD);
            restRequest.addInParam(
                    PARAM_SESSION,
                    ((ClaimApplication) ((Activity) context).getApplication()).getSessionId()
            );
            restRequest.addInParam(PARAM_FILTER_RN, String.valueOf(this.filter_rn));
            JSONObject response = restRequest.getJsonContent();
            if (response == null) return;
            if (response.optString(PARAM_ERROR) != null
                    && !response.optString(PARAM_ERROR).isEmpty()) {
                new ErrorPopup(context).showErrorDialog(context.getString(R.string.error_title), response.optString(PARAM_ERROR));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
