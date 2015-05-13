package ua.parus.pmo.parus8claims;

/**
 * Created  by igorgo on 03.05.2015.
 */
public class Intents {

    public static final String EXTRA_KEY_CLAIM = "claim";
    public static final String EXTRA_KEY_REQUEST = "request";
    public static final String EXTRA_KEY_RN = "rn";
    public static final String EXTRA_KEY_HAS_DOCS = "hasdocs";
    public static final String EXTRA_KEY_SESSION = "session";
    public static final String EXTRA_KEY_CLAIM_LIST_POS = "listpos";

    public static final int RESULT_CANCEL = 200;
    public static final int RESULT_NEED_EXECUTE_FILTER = 201;
    public static final int RESULT_NEED_SAVE_N_EXECUTE_FILTER = 202;
    public static final int RESULT_FILTER_SELECTED = 203;
    public static final int RESULT_NEED_ADD_NEW_FILTER = 204;
    public static final int RESULT_CLAIM_UPDATED = 205;
    public static final int RESULT_CLAIM_ADDED = 206;
    public static final int RESULT_CLAIM_DELETED = 207;

    public static final int REQUEST_CLAIM_VIEW = 101;
    public static final int REQUEST_CLAIM_EDIT = 102;
    public static final int REQUEST_FILTERS_VIEW = 103;
    public static final int REQUEST_FILTER_ADD_NEW = 104;
    public static final int REQUEST_FILTER_EDIT = 105;
    public static final int REQUEST_SETTINGS = 106;
    public static final int REQUEST_CLAIM_ADD = 107;
    public static final int REQUEST_CLAIM_NOTE = 108;
    public static final int REQUEST_SELECT_FILE = 109;
    public static final int REQUEST_CLAIM_SEND = 110;
    public static final int REQUEST_CLAIM_RETURN = 111;
    public static final int REQUEST_CLAIM_FORWARD = 112;
}
