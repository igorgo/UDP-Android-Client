package ua.parus.pmo.parus8claims.rest;

import android.os.Environment;
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
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RestRequest {

    public static final String BASE_URL = "http://pmo.parus.ua/apex/rest/udp/";
    private static final String DEFAULT_HTTP_METHOD = "GET";
    private static final String UNICODE_CHARSET_NAME = "UTF-8";
    private static final String TAG = RestRequest.class.getSimpleName();
//    public static final String BASE_URL = "http://192.168.7.4:7777/apex/rest/udp/";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_NEXT = "next";
    private static final String ITEM_REF = "$ref";
    private static final int MEGABYTE = 1024 * 1024;
    private URL startUrl;
    private URL nextUrl;
    private String httpMethod;
    private int pageSize;
    private boolean firstTime;
    private Map<String, String> inParams;


    public RestRequest(String url, String httpMethod) throws MalformedURLException {
        this(url);
        this.httpMethod = httpMethod;
    }

    @SuppressWarnings("SameParameterValue")
    public RestRequest(String url, int pageSize) throws MalformedURLException {
        this(url, DEFAULT_HTTP_METHOD);
        this.pageSize = pageSize;
    }

    public RestRequest(String url) throws MalformedURLException {
        this.startUrl = new URL(BASE_URL + url);
        this.inParams = new HashMap<>();
        this.pageSize = 50;
        this.httpMethod = DEFAULT_HTTP_METHOD;
        this.resetPagination();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean hasNextPage() {
        return this.nextUrl != null;
    }

    private void resetPagination() {
        this.nextUrl = null;
        this.firstTime = true;
    }

    boolean valueIsNull(String value) {
        return value == null || value.isEmpty() || value.toUpperCase().equals("NULL");
    }

    public void addInParam(String paramName, String value) {
        if (this.valueIsNull(value)) return;
        this.inParams.put(paramName, value);
    }

    private HttpURLConnection getHttpConnection() throws IOException {
        HttpURLConnection connection = null;
        if (this.firstTime || this.hasNextPage()) {

            if (this.hasNextPage()) {
                connection = (HttpURLConnection) this.nextUrl.openConnection();
                Log.d(TAG, "Open connection to " + this.nextUrl.toString());
            } else {
                connection = (HttpURLConnection) this.startUrl.openConnection();
                Log.d(TAG, "Open connection to " + this.startUrl.toString());
            }
            connection.setConnectTimeout(3000);
            connection.setRequestMethod(this.httpMethod);

            if (this.httpMethod.equals(DEFAULT_HTTP_METHOD)) {
                for (Map.Entry<String, String> iEntry : this.inParams.entrySet()) {
                    connection.addRequestProperty(iEntry.getKey(), URLEncoder.encode(iEntry.getValue(), UNICODE_CHARSET_NAME));
                }
            } else {
                String params = "";
                for (Map.Entry<String, String> iEntry : this.inParams.entrySet()) {
                    if (!params.isEmpty()) params = params + "&";
                    params = params + iEntry.getKey() + "=" + URLEncoder.encode(iEntry.getValue(), UNICODE_CHARSET_NAME);
                }
                connection.setDoOutput(true); // Triggers POST.
                connection.setRequestProperty("Accept-Charset", UNICODE_CHARSET_NAME);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + UNICODE_CHARSET_NAME);
                OutputStream output = connection.getOutputStream();
                output.write(params.getBytes(UNICODE_CHARSET_NAME));
            }
        }
        return connection;
    }

    @SuppressWarnings("WeakerAccess")
    public String getStringContent() throws ConnectException {
        HttpURLConnection connection;
        StringBuilder content = new StringBuilder();
        try {
            if ((connection = this.getHttpConnection()) != null) {
                // заворачиваем ответ HttpURLConnection в BufferedReader
                Log.d(TAG, "Fetching data from " + connection.getURL().toString());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                String line;
                // читаем ответ HttpURLConnection через BufferedReader
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                Log.d(TAG, "The data is fetched from " + connection.getURL().toString());
                connection.disconnect();
            }
        } catch (ConnectException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new ConnectException();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "other exception", e);
            e.printStackTrace();
        }
        return content.toString();
    }

    public JSONObject getJsonContent() throws ConnectException {
        JSONObject jsonContent = null;
        String stringContent = this.getStringContent();
        if (stringContent != null) {
            try {
                jsonContent = new JSONObject(stringContent);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return jsonContent;
    }

    public JSONArray getPageRows() throws ConnectException {
        JSONObject nextPageReference;
        JSONArray rows = null;
        JSONObject jsonContent = this.getJsonContent();
        if (jsonContent != null) {
            try {
                nextPageReference = jsonContent.optJSONObject(TAG_NEXT);
                rows = jsonContent.getJSONArray(TAG_ITEMS);
                if (nextPageReference != null && rows.length() == this.pageSize) {
                    this.nextUrl = new URL(nextPageReference.getString(ITEM_REF));
                } else {
                    this.nextUrl = null;
                }
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            this.nextUrl = null;
        }
        return rows;
    }

    private JSONArray concatJsonArrays(JSONArray toArray, JSONArray fromArray) throws JSONException {
        for (int i = 0; i < fromArray.length(); i++) {
            toArray.put(fromArray.get(i));
        }
        return toArray;
    }

    public JSONArray getAllRows() throws ConnectException {
        JSONArray rows = null;
        try {
            rows = this.getPageRows();
            while (this.hasNextPage()) {
                rows = this.concatJsonArrays(rows, this.getPageRows());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public File getFile(String aFileName) {
        HttpURLConnection connection;
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, aFileName);
        try {
            if ((connection = this.getHttpConnection()) != null) {
                connection.connect();
                //noinspection ResultOfMethodCallIgnored
                path.mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                InputStream inputStream = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[MEGABYTE];
                int bufferLength;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}


