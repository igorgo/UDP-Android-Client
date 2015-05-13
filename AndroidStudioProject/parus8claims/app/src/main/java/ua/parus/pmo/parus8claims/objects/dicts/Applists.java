package ua.parus.pmo.parus8claims.objects.dicts;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.db.DatabaseWrapper;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igorgo on 13.04.2015.
 * ${PACKAGE_NAME}
 */
public class Applists {


    private static final String FIELD_NAME = "n";
    private static final String TAG = Applists.class.getSimpleName();
    public static final String TABLE_NAME = "applist";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME_TYPE = "TEXT";
    public static final String COLUMN_NAME = "appname";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_NAME + " " + COLUMN_NAME_TYPE +
                    ")";
    private static final String REST_URL = "dicts/applist/";

    public static void refreshCache(Context context) {
        final Context lContext = context;
        final ProgressDialog loadDialog = ProgressDialog.show(lContext, lContext.getString(R.string.please_wait), lContext.getString(R.string.loading_applist), true);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DatabaseWrapper databaseWrapper = new DatabaseWrapper(lContext);
                        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
                        try {
                            RestRequest restRequest = new RestRequest(REST_URL);
                            JSONArray items = restRequest.getAllRows();
                            if (items != null) {
                                db.delete(TABLE_NAME, null, null);
                                for (int i = 0; i < items.length(); i++) {
                                    try {
                                        JSONObject item = items.getJSONObject(i);
                                        ContentValues values = new ContentValues();
                                        values.put(COLUMN_NAME, item.getString(FIELD_NAME));
                                        long lAppId = db.insert(TABLE_NAME, "null", values);
                                        Log.i(TAG, "Inserted new Application with ID: " + lAppId + " NAME: " + item.getString("n"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (MalformedURLException | ConnectException e) {
                            e.printStackTrace();
                        }
                        db.close();
                        loadDialog.dismiss();
                    }
                }
        ).start();
    }

    public static void checkCache(Context context) {
        String SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        int counter = cursor.getInt(0);
        cursor.close();
        db.close();
        if (counter == 0) refreshCache(context);
    }

    public static List<String> getAppsAll(Context context) {
        checkCache(context);
        List<String> appList = new ArrayList<>();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String SQL = "SELECT " + COLUMN_NAME +
                " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            appList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return appList;
    }

    public static Applist getAppByName(Context context, String name) {
        Applist app = new Applist();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        String SQL = "SELECT " + COLUMN_ID + COMMA_SEP + COLUMN_NAME +
                " FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "=?";
        Cursor cursor = db.rawQuery(SQL, new String[]{name});
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            app.id = cursor.getInt(0);
            app.name = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return app;
    }

}
