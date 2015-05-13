package ua.parus.pmo.parus8claims.om.filter;

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
 * ua.parus.pmo.parus8claims.om.filter
 */
public class Filter {

    public static final String PARAM_FILTER_RN = "P-FILTER-RN";
    private static final String REST_READ_METHOD = "GET";
    private static final String REST_SAVE_METHOD = "PUT";
    private static final String REST_DELETE_METHOD = "DELETE";
    private static final String PARAM_SESSION = "P-SESSION";
    private static final String PARAM_FILTER_NAME = "P-FILTER-NAME";
    private static final String PARAM_CLAIM_NUMB = "P-CLAIM-NUMB";
    private static final String PARAM_CLAIM_VERS = "P-CLAIM-VERS";
    private static final String PARAM_CLAIM_RELEASE = "P-CLAIM-RELEASE";
    private static final String PARAM_CLAIM_BUILD = "P-CLAIM-BUILD";
    private static final String PARAM_CLAIM_UNIT = "P-CLAIM-UNIT";
    private static final String PARAM_CLAIM_APP = "P-CLAIM-APP";
    private static final String PARAM_CLAIM_IM_INIT = "P-CLAIM-IM-INIT";
    private static final String PARAM_CLAIM_IM_EXEC = "P-CLAIM-IM-PERF";
    private static final String PARAM_CLAIM_CONTENT = "P-CLAIM-CONTENT";
    private static final String PARAM_OUT_RN = "P-OUT-RN";
    private static final String PARAM_ERROR = "P-ERROR";
    private static final String REST_URL = "filter/";
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
            RestRequest lRequest = new RestRequest(REST_URL, REST_READ_METHOD);
            ClaimApplication lApplication = (ClaimApplication) ((Activity) context).getApplication();
            lRequest.addHeaderParam(PARAM_SESSION, lApplication.getSessionId());
            if (filter_rn > 0)
                lRequest.addHeaderParam(PARAM_FILTER_RN, String.valueOf(filter_rn));
            JSONObject lJsonFilter = lRequest.getJsonContent();
            if (lJsonFilter == null) return;
            if (lJsonFilter.optString(PARAM_ERROR) != null && !lJsonFilter.optString(PARAM_ERROR).isEmpty()) {
                ErrorPopup errorPopup = new ErrorPopup(context);
                errorPopup.showErrorDialog(context.getString(R.string.error_title), lJsonFilter.optString(PARAM_ERROR));
            } else {
                filter_name = lJsonFilter.optString(PARAM_FILTER_NAME);
                condNumber = lJsonFilter.optString(PARAM_CLAIM_NUMB);
                condUnit = lJsonFilter.optString(PARAM_CLAIM_UNIT);
                condApplication = lJsonFilter.optString(PARAM_CLAIM_APP);
                condVersion = lJsonFilter.optString(PARAM_CLAIM_VERS);
                condRelease = lJsonFilter.optString(PARAM_CLAIM_RELEASE);
                condBuild = lJsonFilter.optString(PARAM_CLAIM_BUILD);
                condImInitiator = lJsonFilter.optInt(PARAM_CLAIM_IM_INIT) == 1;
                condImExecutor = lJsonFilter.optInt(PARAM_CLAIM_IM_EXEC) == 1;
                condContent = lJsonFilter.optString(PARAM_CLAIM_CONTENT);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        condNumber = null;
        condUnit = null;
        condApplication = null;
        condVersion = null;
        condRelease = null;
        condBuild = null;
        condImInitiator = false;
        condImInitiator = false;
        condContent = null;
    }

    public void saveToServer(Context context) {
        try {
            RestRequest lRequest = new RestRequest(REST_URL, REST_SAVE_METHOD);
            lRequest.addHeaderParam(
                    PARAM_SESSION,
                    ((ClaimApplication) ((Activity) context).getApplication()).getSessionId()
            );
            if ((filter_rn > 0))
                lRequest.addHeaderParam(PARAM_FILTER_RN, String.valueOf(filter_rn));
            lRequest.addHeaderParamBase64(PARAM_FILTER_NAME, filter_name);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_NUMB, condNumber);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_VERS, condVersion);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_RELEASE, condRelease);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_BUILD, condBuild);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_UNIT, condUnit);
            lRequest.addHeaderParamBase64(PARAM_CLAIM_APP, condApplication);
            lRequest.addHeaderParam(PARAM_CLAIM_IM_INIT, String.valueOf(condImInitiator ? 1 : 0));
            lRequest.addHeaderParam(PARAM_CLAIM_IM_EXEC, String.valueOf(condImExecutor ? 1 : 0));
            lRequest.addHeaderParamBase64(PARAM_CLAIM_CONTENT, condContent);
            JSONObject lResponse = lRequest.getJsonContent();
            if (lResponse == null) return;
            if (lResponse.optString(PARAM_ERROR) != null && !lResponse.optString(PARAM_ERROR).isEmpty()) {
                new ErrorPopup(context).showErrorDialog(context.getString(R.string.error_title), lResponse.optString(PARAM_ERROR));
            } else {
                filter_rn = lResponse.optLong(PARAM_OUT_RN);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void deleteOnServer(Context context) {
        try {
            RestRequest lRequest = new RestRequest(REST_URL, REST_DELETE_METHOD);
            lRequest.addHeaderParam(
                    PARAM_SESSION,
                    ((ClaimApplication) ((Activity) context).getApplication()).getSessionId()
            );
            lRequest.addHeaderParam(PARAM_FILTER_RN, String.valueOf(filter_rn));
            JSONObject response = lRequest.getJsonContent();
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
