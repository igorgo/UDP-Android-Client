package ua.parus.pmo.parus8claims.objects.dicts;

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

import ua.parus.pmo.parus8claims.db.DatabaseWrapper;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created  by igor-go on 14.04.2015.
 */
public class Releases {

    public static final String TABLE_NAME = "releases";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String COLUMN_RN = "rn";
    public static final String COLUMN_NAME = "name";
    private static final String COLUMN_VERSION = "version";
    private static final String COLUMN_BUILDS_CACHED = "builds_cached";
    private static final String TAG = Releases.class.getSimpleName();
    private static final String COMMA_SEP = ", ";
    private static final String TYPE_RN = "INTEGER PRIMARY KEY ";
    private static final String TYPE_VERSION = "TEXT";
    private static final String TYPE_RELEASE = "TEXT";
    private static final String TYPE_BUILDS_CACHED = "INTEGER";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_RN + " " + TYPE_RN + COMMA_SEP +
                    COLUMN_VERSION + " " + TYPE_VERSION + COMMA_SEP +
                    COLUMN_NAME + " " + TYPE_RELEASE + COMMA_SEP +
                    COLUMN_BUILDS_CACHED + " " + TYPE_BUILDS_CACHED +
                    ")";

    public static void RefreshCache(Context context) throws ConnectException {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        try {
            RestRequest restRequest = new RestRequest("dicts/releases/");
            JSONArray response = restRequest.getAllRows();
            if (response != null) {
                db.delete(TABLE_NAME, null, null);
                db.delete(Builds.TABLE_NAME, null, null);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject c = response.getJSONObject(i);
                        Release rel = new Release();
                        rel.rn = c.getLong("rn");
                        rel.version = c.getString("v");
                        rel.name = c.getString("r");
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_RN, rel.rn);
                        values.put(COLUMN_VERSION, rel.version);
                        values.put(COLUMN_NAME, rel.name);
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
        List<String> versions = new ArrayList<>();
        if (!mandatory) versions.add(nulltext);
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_VERSION + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_VERSION + " DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            versions.add(cursor.getString(cursor.getColumnIndex(COLUMN_VERSION)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return versions;
    }


    private static List<Release> getReleases(Context context, String version, boolean mandatory, String nulltext) {
        List<Release> releases = new ArrayList<>();
        Release release;
        if (!mandatory) {
            release = new Release();
            release.name = nulltext;
            release.version = version;
            releases.add(release);
        }
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String[] columns = {
                COLUMN_RN,              //0
                COLUMN_VERSION,         //1
                COLUMN_NAME,            //2
                COLUMN_BUILDS_CACHED    //3
        };
        String selectClause = "";
        for (String lColumn : columns) {
            selectClause = selectClause + lColumn + COMMA_SEP;
        }
        selectClause = "SELECT " + selectClause.substring(0, selectClause.length() - COMMA_SEP.length());
        String lFromClause = " FROM " + TABLE_NAME;
        String lWhereClouse;
        String[] queryParams;
        if (version != null) {
            lWhereClouse = " WHERE " + COLUMN_VERSION + "= ?";
            queryParams = new String[]{version};
        } else {
            lWhereClouse = "";
            queryParams = new String[]{};
        }
        String lOrderClause = " ORDER BY " + COLUMN_NAME + " DESC";
        String SQL = selectClause + lFromClause + lWhereClouse + lOrderClause;
        Cursor cursor = db.rawQuery(SQL, queryParams);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            release = new Release();
            release.rn = cursor.getLong(0);
            release.version = cursor.getString(1);
            release.name = cursor.getString(2);
            release.buildsCached = cursor.getInt(3);
            releases.add(release);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return releases;
    }



    public static void setReleaseCached(Context context, long releaseRn) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUILDS_CACHED, 1);
        db.update(TABLE_NAME, values, COLUMN_RN + " = ?", new String[]{String.valueOf(releaseRn)});
        db.close();
    }

    private static String getOneRowSql(String pByColumn) {
        String[] columns = {
                COLUMN_RN,              //0
                COLUMN_VERSION,         //1
                COLUMN_NAME,            //2
                COLUMN_BUILDS_CACHED    //3
        };
        String SQL = "";
        for (String column : columns) {
            SQL = SQL + column + COMMA_SEP;
        }
        SQL = SQL.substring(0, SQL.length() - COMMA_SEP.length());
        SQL = "SELECT " + SQL + " FROM " + TABLE_NAME + " WHERE " + pByColumn + "= ?";
        return SQL;
    }

    private static Release cursorToObject(Cursor cursor) {
        Release release = new Release();
        release.rn = cursor.getLong(0);
        release.version = cursor.getString(1);
        release.name = cursor.getString(2);
        release.buildsCached = cursor.getInt(3);
        return release;
    }

    public static Release getRelease(Context context, String releaseCode) {
        Release release = new Release();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String SQL = getOneRowSql(COLUMN_NAME);
        Cursor cursor = db.rawQuery(SQL, new String[]{releaseCode});
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            release = cursorToObject(cursor);
        }
        cursor.close();
        db.close();
        return release;
    }

    public static Release getRelease(Context context, long releaseRn) {
        Release release = new Release();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String SQL = getOneRowSql(COLUMN_RN);
        Cursor cursor = db.rawQuery(SQL, new String[]{String.valueOf(releaseRn)});
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            release = cursorToObject(cursor);
        }
        cursor.close();
        db.close();
        return release;
    }

    public static List<String> getReleasesNames(Context context, String version, boolean mandatory, String nulltext) {
        List<String> releasesNames = new ArrayList<>();
        List<Release> releases = getReleases(context, version, mandatory, nulltext);
        for (Release release : releases) releasesNames.add(release.name);
        return releasesNames;
    }

}
