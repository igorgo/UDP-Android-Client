package ua.parus.pmo.parus8claims.db;

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

import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created by igorgo on 15.04.2015.
 */
@SuppressWarnings("ALL")
public class BuildsORM {
    public static final String TABLE_NAME = "builds";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COLUMN_RN = "rn";
    private static final String COLUMN_PRN = "prn";
    private static final String COLUMN_CODE = "bcode";
    private static final String COLUMN_DATE = "bdate";
    private static final String TAG = "BuildsORM";
    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_RN_TYPE = "INTEGER PRIMARY KEY ";
    private static final String COLUMN_PRN_TYPE = "INTEGER ";
    private static final String COLUMN_CODE_TYPE = "TEXT";
    private static final String COLUMN_DATE_TYPE = "TEXT";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_RN + " " + COLUMN_RN_TYPE + COMMA_SEP +
                    COLUMN_PRN + " " + COLUMN_PRN_TYPE + COMMA_SEP +
                    COLUMN_CODE + " " + COLUMN_CODE_TYPE + COMMA_SEP +
                    COLUMN_DATE + " " + COLUMN_DATE_TYPE +
                    ")";

    private static void getCache(Context context, long releaseRn) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        boolean lAllOK = false;
        try {
            RestRequest restRequest = new RestRequest("dicts/builds/" + String.valueOf(releaseRn));
            JSONArray response = restRequest.getAllRows();
            if (response != null) {
                //db.delete(TABLE_NAME, null, null);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject c = response.getJSONObject(i);
                        Builds bld = new Builds();
                        bld.set_rn(c.getLong("r"));
                        bld.set_prn(c.getLong("p"));
                        bld.set_bcode(c.getString("c"));
                        bld.set_bdate(c.getString("d"));
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_RN, bld.get_rn());
                        values.put(COLUMN_PRN, bld.get_prn());
                        values.put(COLUMN_CODE, bld.get_bcode());
                        values.put(COLUMN_DATE, bld.get_bdate());
                        long relId = db.insert(TABLE_NAME, "null", values);
                        Log.i(TAG, "Inserted Build with RN: " + relId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                lAllOK = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        db.close();
        if (lAllOK) ReleasesORM.setReleaseCached(context, releaseRn);
    }

    private static boolean checkCashe(Context context, String pReleaseCode) {
        Releases lRel = ReleasesORM.getRelease(context, pReleaseCode);
        if (lRel == null) return false;
        if (lRel.get_builds_cached() == 0) {
            getCache(context, lRel.get_rn());
        }
        return true;
    }


    public static List<String> getBuildsCodes(Context context, String release, boolean mandatory) {
        List<String> lBuldsCodes = new ArrayList<>();
        List<Builds> lBuilds = getBuilds(context, release, mandatory, null);
        for (Builds lBuild : lBuilds) lBuldsCodes.add(lBuild.get_displayName());
        return lBuldsCodes;
    }

    private static List<Builds> getBuilds(Context context, String release, boolean mandatory, String nulltext) {
        List<Builds> lBuilds = new ArrayList<>();
        Builds lBuild;
        if (!mandatory) {
            lBuild = new Builds();
            lBuild.set_displayName(nulltext);
            lBuilds.add(lBuild);
        }
        if (checkCashe(context, release)) {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String lSql = "SELECT " +
                    "B." + COLUMN_RN + "," +
                    "B." + COLUMN_PRN + "," +
                    "B." + COLUMN_CODE + "," +
                    "B." + COLUMN_DATE + "," +
                    "R." + ReleasesORM.COLUMN_RELEASE +
                    " FROM " + TABLE_NAME + " B, " + ReleasesORM.TABLE_NAME + " R" +
                    " WHERE B." + COLUMN_PRN + "=R." + ReleasesORM.COLUMN_RN +
                    " AND R." + ReleasesORM.COLUMN_RELEASE + "= ? " +
                    " ORDER BY " + COLUMN_CODE + " DESC";

            Cursor cursor = db.rawQuery(lSql, new String[]{release});
            cursor.moveToFirst();
            int[] idx = {
                    cursor.getColumnIndex(COLUMN_RN),     // 0
                    cursor.getColumnIndex(COLUMN_PRN),    // 1
                    cursor.getColumnIndex(COLUMN_CODE),   // 2
                    cursor.getColumnIndex(COLUMN_DATE),   // 3
                    cursor.getColumnIndex(ReleasesORM.COLUMN_RELEASE) // 4
            };
            while (!cursor.isAfterLast()) {
                lBuild = new Builds();
                lBuild.set_rn(cursor.getLong(idx[0]));
                lBuild.set_prn(cursor.getLong(idx[1]));
                lBuild.set_bcode(cursor.getString(idx[2]));
                lBuild.set_bdate(cursor.getString(idx[3]));
                lBuild.set_displayName(cursor.getString(idx[4]) + "." + cursor.getString(idx[2]) +
                        " (" + cursor.getString(idx[3]) + ")");
                lBuilds.add(lBuild);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return lBuilds;

    }

    public static Builds getBuild(Context context, long releaseRn, long BuildRn) {
        Builds lBuild = null;
        Releases lRelease = ReleasesORM.getRelease(context, releaseRn);
        if (checkCashe(context, lRelease.get_release())) {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String lSql = "SELECT " +
                    "B." + COLUMN_RN + "," +
                    "B." + COLUMN_PRN + "," +
                    "B." + COLUMN_CODE + "," +
                    "B." + COLUMN_DATE +
                    " FROM " + TABLE_NAME + " B " +
                    " WHERE B." + COLUMN_RN + "= ? ";
            Cursor cursor = db.rawQuery(lSql, new String[]{String.valueOf(BuildRn)});
            cursor.moveToFirst();
            int[] idx = {
                    cursor.getColumnIndex(COLUMN_RN),     // 0
                    cursor.getColumnIndex(COLUMN_PRN),    // 1
                    cursor.getColumnIndex(COLUMN_CODE),   // 2
                    cursor.getColumnIndex(COLUMN_DATE),   // 3
            };
            lBuild = new Builds();
            lBuild.set_rn(cursor.getLong(idx[0]));
            lBuild.set_prn(cursor.getLong(idx[1]));
            lBuild.set_bcode(cursor.getString(idx[2]));
            lBuild.set_bdate(cursor.getString(idx[3]));
            lBuild.set_displayName(lRelease.get_release() + "." + cursor.getString(idx[2]) +
                    " (" + cursor.getString(idx[3]) + ")");
            cursor.close();
            db.close();
        }
        return lBuild;
    }
}
