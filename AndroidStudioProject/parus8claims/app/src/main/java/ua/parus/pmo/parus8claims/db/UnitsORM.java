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
 * Created by igorgo on 18.04.2015.
 */
public class UnitsORM {
    private static final String TABLE_NAME = "units";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COLUMN_NAME = "uname";
    private static final String COLUMN_APPS_CACHED = "apps_cached";
    private static final String COLUMN_FUNCS_CACHED = "funcs_cached";
    private static final String TAG = "UnitsORM";
    private static final String COMMA_SEP = ", ";
    private static final String TYPE_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COLUMN_ID = "id";
    private static final String TYPE_NAME = "TEXT";
    private static final String TYPE_APPS_CACHED = "INTEGER";
    private static final String TYPE_FUNCS_CACHED = "INTEGER";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + TYPE_ID + COMMA_SEP +
                    COLUMN_NAME + " " + TYPE_NAME + COMMA_SEP +
                    COLUMN_APPS_CACHED + " " + TYPE_APPS_CACHED + COMMA_SEP +
                    COLUMN_FUNCS_CACHED + " " + TYPE_FUNCS_CACHED +
                    ")";

    public static void refreshCache(Context context) {
        final Context lCtx = context;
        final ProgressDialog lLoadDialog = ProgressDialog.show(lCtx, lCtx.getString(R.string.please_wait), lCtx.getString(R.string.loading_unitlist), true);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DatabaseWrapper databaseWrapper = new DatabaseWrapper(lCtx);
                        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
                        try {
                            RestRequest restRequest = new RestRequest("dicts/units/");
                            JSONArray response = restRequest.getAllRows();
                            if (response != null) {
                                db.delete(TABLE_NAME, null, null);
                                //TODO: delete details db.delete(BuildsORM.TABLE_NAME, null, null);
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject c = response.getJSONObject(i);
                                        ContentValues values = new ContentValues();
                                        values.put(COLUMN_NAME, c.getString("n"));
                                        values.put(COLUMN_APPS_CACHED, 0);
                                        values.put(COLUMN_FUNCS_CACHED, 0);
                                        long lUnitId = db.insert(TABLE_NAME, "null", values);
                                        Log.i(TAG, "Inserted new Unit with ID: " + lUnitId + " NAME: " + c.getString("n"));
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

    public static List<String> getUnits(Context context) {
        checkCache(context);
        List<String> unitsList = new ArrayList<>();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String lSql = "SELECT " + COLUMN_NAME +
                " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(lSql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            unitsList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return unitsList;
    }


}
