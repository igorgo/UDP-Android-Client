package ua.parus.pmo.parus8claims.utils;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public interface Constants {

    final String PACKAGE = "ua.parus.pmo.parus8claims";
    final String PREFS_NAME = PACKAGE + "_preferences";
    final String FONT_BOLD = "RobotoCondensed-Bold.ttf";
    final String FONT_REGULAR = "RobotoCondensed-Regular.ttf";



    // intents' extra keys
    final String EXTRA_KEY_CLAIM = "claim";
    final String EXTRA_KEY_REQUEST = "request";
    final String EXTRA_KEY_RN = "rn";
    final String EXTRA_KEY_HAS_DOCS = "hasdocs";
    final String EXTRA_KEY_SESSION = "session";
    final String EXTRA_KEY_CLAIM_LIST_POS = "listpos";

    // intents' results
    final int RESULT_CANCEL = 200;
    final int RESULT_NEED_EXECUTE_FILTER = 201;
    final int RESULT_NEED_SAVE_N_EXECUTE_FILTER = 202;
    final int RESULT_FILTER_SELECTED = 203;
    final int RESULT_NEED_ADD_NEW_FILTER = 204;
    final int RESULT_CLAIM_UPDATED = 205;
    final int RESULT_CLAIM_ADDED = 206;
    final int RESULT_CLAIM_DELETED = 207;
    final int RESULT_NEED_REFRESH_DICTIONARIES_CACHE = 208;

    // intents' requests
    final int REQUEST_CLAIM_VIEW = 101;
    final int REQUEST_CLAIM_EDIT = 102;
    final int REQUEST_FILTERS_VIEW = 103;
    final int REQUEST_FILTER_ADD_NEW = 104;
    final int REQUEST_FILTER_EDIT = 105;
    final int REQUEST_SETTINGS = 106;
    final int REQUEST_CLAIM_ADD = 107;
    final int REQUEST_CLAIM_NOTE = 108;
    final int REQUEST_SELECT_FILE = 109;
    final int REQUEST_CLAIM_SEND = 110;
    final int REQUEST_CLAIM_RETURN = 111;
    final int REQUEST_CLAIM_FORWARD = 112;

    //preferences
    final String PREF_PASSWORD = "password";
    final String PREF_USERNAME = "username";
    final String PREF_RESET_CACHE = "cache";

}
