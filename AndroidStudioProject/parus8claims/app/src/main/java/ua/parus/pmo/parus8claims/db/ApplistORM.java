package ua.parus.pmo.parus8claims.db;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igorgo on 13.04.2015.
 * ${PACKAGE_NAME}
 */
public class ApplistORM {

    private static final String TAG = "ApplistORM";

    private static final String TABLE_NAME = "applist";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME_TYPE = "TEXT";
    private static final String COLUMN_NAME = "appname";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_NAME + " " + COLUMN_NAME_TYPE +
                    ")";

    public static void refreshCache(Context context) {
        final Context lCtx = context;
        final ProgressDialog lLoadDialog = ProgressDialog.show(lCtx, lCtx.getString(R.string.please_wait), lCtx.getString(R.string.loading_applist), true);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DatabaseWrapper databaseWrapper = new DatabaseWrapper(lCtx);
                        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
                        try {
                            RestRequest restRequest = new RestRequest("dicts/applist/");
                            JSONArray response = restRequest.getAllRows();
                            if (response != null) {
                                db.delete(TABLE_NAME, null, null);
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject c = response.getJSONObject(i);
                                        ContentValues values = new ContentValues();
                                        values.put(COLUMN_NAME, c.getString("n"));
                                        long lAppId = db.insert(TABLE_NAME, "null", values);
                                        Log.i(TAG, "Inserted new Unit with ID: " + lAppId + " NAME: " + c.getString("n"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        db.close();
                        lLoadDialog.dismiss();
                    }
                }
        ).start();
    }

    public static void checkCache(Context context) {
        String lSQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        Cursor cursor = db.rawQuery(lSQL, null);
        cursor.moveToFirst();
        int lCnt = cursor.getInt(0);
        cursor.close();
        db.close();
        if (lCnt == 0) refreshCache(context);
    }


    public static List<String> getApps(Context context) {
        checkCache(context);
        List<String> appList = new ArrayList<>();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String lSql = "SELECT " + COLUMN_NAME +
                " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(lSql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            appList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return appList;
    }


}
