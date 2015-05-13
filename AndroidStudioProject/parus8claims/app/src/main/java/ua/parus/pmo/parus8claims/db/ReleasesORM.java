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
 * Created by igor-go on 14.04.2015.
 */
public class ReleasesORM {

    public static final String TABLE_NAME = "releases";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String COLUMN_RN = "rn";
    private static final String COLUMN_VERSION = "version";
    public static final String COLUMN_RELEASE = "release";
    private static final String COLUMN_BUILDS_CACHED = "builds_cached";
    private static final String TAG = "ReleasesORM";
    private static final String COMMA_SEP = ", ";
    private static final String TYPE_RN = "INTEGER PRIMARY KEY ";
    private static final String TYPE_VERSION = "TEXT";
    private static final String TYPE_RELEASE = "TEXT";
    private static final String TYPE_BUILDS_CACHED = "INTEGER";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_RN + " " + TYPE_RN + COMMA_SEP +
                    COLUMN_VERSION + " " + TYPE_VERSION + COMMA_SEP +
                    COLUMN_RELEASE + " " + TYPE_RELEASE + COMMA_SEP +
                    COLUMN_BUILDS_CACHED + " " + TYPE_BUILDS_CACHED +
                    ")";

    public static void RefreshCache(Context context) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        try {
            RestRequest restRequest = new RestRequest("dicts/releases/");
            JSONArray response = restRequest.getAllRows();
            if (response != null) {
                db.delete(TABLE_NAME, null, null);
                db.delete(BuildsORM.TABLE_NAME, null, null);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject c = response.getJSONObject(i);
                        Releases rel = new Releases();
                        rel.set_rn(c.getLong("rn"));
                        rel.set_vesion(c.getString("v"));
                        rel.set_release(c.getString("r"));
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_RN, rel.get_rn());
                        values.put(COLUMN_VERSION, rel.get_vesion());
                        values.put(COLUMN_RELEASE, rel.get_release());
                        values.put(COLUMN_BUILDS_CACHED, 0);
                        long relId = db.insert(TABLE_NAME, "null", values);
                        Log.i(TAG, "Inserted new Release with RN: " + relId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        db.close();
    }


    public static List<String> getVersions(Context context, boolean mandatory, String nulltext) {
        List<String> verses = new ArrayList<>();
        if (!mandatory) verses.add(nulltext);
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_VERSION + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_VERSION + " DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            verses.add(cursor.getString(cursor.getColumnIndex(COLUMN_VERSION)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return verses;
    }


    private static List<Releases> getReleases(Context context, String version, boolean mandatory, String nulltext) {
        List<Releases> lReleases = new ArrayList<>();
        Releases lRelease;
        if (!mandatory) {
            lRelease = new Releases();
            lRelease.set_release(nulltext);
            lRelease.set_vesion(version);
            lReleases.add(lRelease);
        }
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String[] lColumns = {
                COLUMN_RN,              //0
                COLUMN_VERSION,         //1
                COLUMN_RELEASE,         //2
                COLUMN_BUILDS_CACHED    //3
        };
        String lSelectClause = "";
        for (String lColumn : lColumns) {
            lSelectClause = lSelectClause + lColumn + COMMA_SEP;
        }
        lSelectClause = "SELECT " + lSelectClause.substring(0, lSelectClause.length() - COMMA_SEP.length());
        String lFromClause = " FROM " + TABLE_NAME;
        String lWhereClouse = " WHERE " + COLUMN_VERSION + "= ?";
        String lOrderClause = " ORDER BY " + COLUMN_RELEASE + " DESC";
        String lSql = lSelectClause + lFromClause + lWhereClouse + lOrderClause;
        Cursor cursor = db.rawQuery(lSql, new String[]{version});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            lRelease = new Releases();
            lRelease.set_rn(cursor.getLong(0));
            lRelease.set_vesion(cursor.getString(1));
            lRelease.set_release(cursor.getString(2));
            lRelease.set_builds_cached(cursor.getInt(3));
            lReleases.add(lRelease);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return lReleases;
    }

    public static void setReleaseCached(Context context, long releaseRn) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        ContentValues lValues = new ContentValues();
        lValues.put(COLUMN_BUILDS_CACHED, 1);
        db.update(TABLE_NAME, lValues, COLUMN_RN + " = ?", new String[]{String.valueOf(releaseRn)});
        db.close();
    }


    private static String getOneRowSql(String pByColumn) {
        String[] lColumns = {
                COLUMN_RN,              //0
                COLUMN_VERSION,         //1
                COLUMN_RELEASE,         //2
                COLUMN_BUILDS_CACHED    //3
        };
        String lSQL = "";
        for (String lColumn : lColumns) {
            lSQL = lSQL + lColumn + COMMA_SEP;
        }
        lSQL = lSQL.substring(0, lSQL.length() - COMMA_SEP.length());
        lSQL = "SELECT " + lSQL + " FROM " + TABLE_NAME + " WHERE " + pByColumn + "= ?";
        return lSQL;
    }

    private static Releases cursorToObject(Cursor cursor) {
        Releases lRelease = new Releases();
        lRelease.set_rn(cursor.getLong(0));
        lRelease.set_vesion(cursor.getString(1));
        lRelease.set_release(cursor.getString(2));
        lRelease.set_builds_cached(cursor.getInt(3));
        return lRelease;
    }

    public static Releases getRelease(Context context, String releaseCode) {
        Releases lRelease = new Releases();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String lSql = getOneRowSql(COLUMN_RELEASE);
        Cursor cursor = db.rawQuery(lSql, new String[]{releaseCode});
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            lRelease = cursorToObject(cursor);
        }
        cursor.close();
        db.close();
        return lRelease;
    }

    public static Releases getRelease(Context context, long releaseRn) {
        Releases lRelease = new Releases();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String lSql = getOneRowSql(COLUMN_RN);
        Cursor cursor = db.rawQuery(lSql, new String[]{String.valueOf(releaseRn)});
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            lRelease = cursorToObject(cursor);
        }
        cursor.close();
        db.close();
        return lRelease;
    }

    public static List<String> getReleasesCodes(Context context, String version, boolean mandatory, String nulltext) {
        List<String> lReleasesCodes = new ArrayList<>();
        List<Releases> lReleases = getReleases(context, version, mandatory, nulltext);
        for (Releases lRelease : lReleases) lReleasesCodes.add(lRelease.get_release());
        return lReleasesCodes;
    }

}
