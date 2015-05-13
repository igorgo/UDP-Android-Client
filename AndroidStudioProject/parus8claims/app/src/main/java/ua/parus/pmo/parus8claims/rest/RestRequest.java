package ua.parus.pmo.parus8claims.rest;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by igorgo on 13.04.2015.
 * ${PACKAGE_NAME}
 */
public class RestRequest {

    private static final String DEFAULT_HTTP_METHOD = "GET";
    private static final String UNICODE_CHARSET_NAME = "UTF-8";
    private static final String TAG = RestRequest.class.getSimpleName();
    private static final String BASE_URL = "http://pmo.parus.ua/apex/rest/udp/";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_NEXT = "next";
    private static final String ITEM_REF = "$ref";
    private static final int MEGABYTE = 1024 * 1024;
    private URL mStartUrl;
    private URL mNextUrl;
    private String mHttpMethod;
    private int mPageSize;
    private boolean mFirstTime;
    private Map<String, String> mHeaderMap;

    public RestRequest(String url, String httpMethod) throws MalformedURLException {
        this(url);
        mHttpMethod = httpMethod;
    }

    public RestRequest(String url, int pageSize) throws MalformedURLException {
        this(url, DEFAULT_HTTP_METHOD);
        mPageSize = pageSize;
    }

    public RestRequest(String url) throws MalformedURLException {
        mStartUrl = new URL(BASE_URL + url);
        mHeaderMap = new HashMap<>();
        mPageSize = 50;
        mHttpMethod = DEFAULT_HTTP_METHOD;
        resetPagination();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean hasNextPage() {
        return mNextUrl != null;
    }

    private void resetPagination() {
        mNextUrl = null;
        mFirstTime = true;
    }

    boolean valueIsNull(String value) {
        return value == null || value.isEmpty() || value.toUpperCase().equals("NULL");
    }

    public void addHeaderParamBase64(String paramName, String value) {
        if (valueIsNull(value)) return;
        byte[] lData = new byte[0];
        try {
            lData = value.getBytes(UNICODE_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String lValueBase64 = Base64.encodeToString(lData, Base64.DEFAULT);
        addHeaderParam(paramName, lValueBase64);
    }

    public void addHeaderParam(String paramName, String value) {
        if (valueIsNull(value)) return;
        mHeaderMap.put(paramName, value);
    }

    private HttpURLConnection getHttpConnection() throws IOException {
        HttpURLConnection lConnection = null;
        if (mFirstTime || hasNextPage()) {
            if (hasNextPage()) {
                lConnection = (HttpURLConnection) mNextUrl.openConnection();
            } else {
                lConnection = (HttpURLConnection) mStartUrl.openConnection();
            }
            lConnection.setRequestMethod(mHttpMethod);
            for (Map.Entry<String, String> iEntry : mHeaderMap.entrySet()) {
                lConnection.addRequestProperty(iEntry.getKey(), iEntry.getValue());
            }
        }
        return lConnection;
    }

    @SuppressWarnings("WeakerAccess")
    public String getStringContent() {
        HttpURLConnection lConnection;
        StringBuilder lContent = new StringBuilder();
        try {
            if ((lConnection = getHttpConnection()) != null) {
                // заворачиваем ответ HttpURLConnection в BufferedReader
                BufferedReader lReader = new BufferedReader(
                        new InputStreamReader(lConnection.getInputStream())
                );
                String lLine;
                // читаем ответ HttpURLConnection через BufferedReader
                while ((lLine = lReader.readLine()) != null) {
                    lContent.append(lLine);
                }
                lReader.close();
            }
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "other exception", e);
            e.printStackTrace();
        }
        return lContent.toString();
    }

    public JSONObject getJsonContent() {
        JSONObject lJsonContent = null;
        String lStringContent = getStringContent();
        if (lStringContent != null) {
            try {
                lJsonContent = new JSONObject(lStringContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return lJsonContent;
    }

    public JSONArray getPageRows() {
        JSONObject lNextPageReference;
        JSONArray lRows = null;
        JSONObject lJsonContent = getJsonContent();
        if (lJsonContent != null) {
            try {
                lNextPageReference = lJsonContent.optJSONObject(TAG_NEXT);
                lRows = lJsonContent.getJSONArray(TAG_ITEMS);
                if (lNextPageReference != null && lRows.length() == mPageSize) {
                    mNextUrl = new URL(lNextPageReference.getString(ITEM_REF));
                } else {
                    mNextUrl = null;
                }
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            mNextUrl = null;
        }
        return lRows;
    }

    private JSONArray concatJsonArrays(JSONArray toArray, JSONArray fromArray) throws JSONException {
        for (int i = 0; i < fromArray.length(); i++) {
            toArray.put(fromArray.get(i));
        }
        return toArray;
    }

    public JSONArray getAllRows() {
        JSONArray lRows = null;
        try {
            lRows = getPageRows();
            while (hasNextPage()) {
                lRows = concatJsonArrays(lRows, getPageRows());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lRows;
    }

    public File getFile(String aFileName) {
        HttpURLConnection lConnection;
        File lPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File lFile = new File(lPath, aFileName);
        try {
            if ((lConnection = getHttpConnection()) != null) {
                lConnection.connect();
                //noinspection ResultOfMethodCallIgnored
                lPath.mkdirs();
                //noinspection ResultOfMethodCallIgnored
                lFile.createNewFile();
                InputStream lInputStream = lConnection.getInputStream();
                FileOutputStream lFileOutputStream = new FileOutputStream(lFile);
                //int lTotalSize = lConnection.getContentLength();
                byte[] lBuffer = new byte[MEGABYTE];
                int lBufferLength;
                while ((lBufferLength = lInputStream.read(lBuffer)) > 0) {
                    lFileOutputStream.write(lBuffer, 0, lBufferLength);
                }
                lFileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lFile;
    }
}


