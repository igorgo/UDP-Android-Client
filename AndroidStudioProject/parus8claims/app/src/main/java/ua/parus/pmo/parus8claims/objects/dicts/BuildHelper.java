package ua.parus.pmo.parus8claims.objects.dicts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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

public class BuildHelper {
    public static final String TABLE_NAME = "builds";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String COLUMN_RN = "rn";
    private static final String COLUMN_PRN = "prn";
    private static final String COLUMN_CODE = "bcode";
    private static final String COLUMN_DATE = "bdate";
    private static final String TAG = BuildHelper.class.getSimpleName();
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
    private static final String SQL_INSERT =
            "INSERT INTO " + TABLE_NAME + "("
                    + COLUMN_RN + COMMA_SEP
                    + COLUMN_PRN + COMMA_SEP
                    + COLUMN_CODE + COMMA_SEP
                    + COLUMN_DATE
                    + ") VALUES (?,?,?,?)";

    private static final String REST_URL = "dicts/builds/";
    private static final String FIELD_RN = "r";
    private static final String FIELD_PRN = "p";
    private static final String FIELD_MNEMO = "c";
    private static final String FIELD_BUILD_DATE = "d";

    private static void getCache(Context context, long releaseRn) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        boolean successFlag = false;
        try {
            RestRequest restRequest = new RestRequest(REST_URL + String.valueOf(releaseRn));
            JSONArray items = restRequest.getAllRows();
            if (items != null) {
                db.beginTransaction();
                try {
                    SQLiteStatement statement = db.compileStatement(SQL_INSERT);
                    for (int i = 0; i < items.length(); i++) {
                        try {
                            JSONObject item = items.getJSONObject(i);
                            statement.bindLong(1, item.getLong(FIELD_RN));
                            statement.bindLong(2, item.getLong(FIELD_PRN));
                            statement.bindString(3, item.getString(FIELD_MNEMO));
                            statement.bindString(4, item.getString(FIELD_BUILD_DATE));
                            statement.execute();
                            statement.clearBindings();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    successFlag = true;
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        } catch (MalformedURLException | ConnectException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        if (successFlag) ReleaseHelper.setReleaseCached(context, releaseRn);
    }

    private static boolean checkCashe(Context context, String pReleaseCode) {
        Release lRel = ReleaseHelper.getRelease(context, pReleaseCode);
        if (lRel == null) return false;
        if (lRel.buildsCached == 0) {
            getCache(context, lRel.rn);
        }
        return true;
    }

    public static List<String> getBuildsDisplayNames(Context context, String release, boolean mandatory) {
        List<String> lBuldsCodes = new ArrayList<>();
        if (release != null) {
            List<Build> lBuilds = getBuilds(context, release, mandatory);
            for (Build lBuild : lBuilds) lBuldsCodes.add(lBuild.displayName);
        }
        return lBuldsCodes;
    }

    public static List<String> getBuildsCodes(Context context, String release, boolean mandatory) {
        List<String> lBuldsCodes = new ArrayList<>();
        if (release != null) {
            List<Build> lBuilds = getBuilds(context, release, mandatory);
            for (Build lBuild : lBuilds) lBuldsCodes.add(buildName(release, lBuild));
        }
        return lBuldsCodes;
    }


    private static String buildDisplayName(String release, Build build) {
        if (build != null) {
            return release + "." + build.mnemo + " (" + build.buildDate + ")";
        }
        return null;
    }

    public static String buildName(String release, Build build) {
        if (build != null) {
            return release + "." + build.mnemo;
        }
        return null;
    }

    private static List<Build> getBuilds(Context context, String release, boolean mandatory) {
        List<Build> builds = new ArrayList<>();
        if (release == null) {
            return builds;
        }
        Build build;
        if (!mandatory) {
            build = new Build();
            build.displayName = "";
            builds.add(build);
        }
        if (checkCashe(context, release)) {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String SQL = "SELECT " +
                    "B." + COLUMN_RN + "," +
                    "B." + COLUMN_PRN + "," +
                    "B." + COLUMN_CODE + "," +
                    "B." + COLUMN_DATE + "," +
                    "R." + ReleaseHelper.COLUMN_NAME +
                    " FROM " + TABLE_NAME + " B, " + ReleaseHelper.TABLE_NAME + " R" +
                    " WHERE B." + COLUMN_PRN + "=R." + ReleaseHelper.COLUMN_RN +
                    " AND R." + ReleaseHelper.COLUMN_NAME + "= ? " +
                    " ORDER BY " + COLUMN_CODE + " DESC";
            Log.i(TAG, "SQL = " + SQL);
            Log.i(TAG, "release = " + release);

            Cursor cursor = db.rawQuery(SQL, new String[]{release});
            cursor.moveToFirst();
            Log.i(TAG, "cursor.cnt = " + String.valueOf(cursor.getCount()));
            int[] indeces = {
                    cursor.getColumnIndex(COLUMN_RN),     // 0
                    cursor.getColumnIndex(COLUMN_PRN),    // 1
                    cursor.getColumnIndex(COLUMN_CODE),   // 2
                    cursor.getColumnIndex(COLUMN_DATE),   // 3
                    cursor.getColumnIndex(ReleaseHelper.COLUMN_NAME) // 4
            };
            while (!cursor.isAfterLast()) {
                build = new Build();
                build.rn = cursor.getLong(indeces[0]);
                build.prn = cursor.getLong(indeces[1]);
                build.mnemo = cursor.getString(indeces[2]);
                build.buildDate = cursor.getString(indeces[3]);
                build.displayName = buildDisplayName(cursor.getString(indeces[4]), build);
                builds.add(build);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return builds;

    }

    public static Build getBuild(Context context, long releaseRn, long buildRn) {
        Build build = null;
        Release release = ReleaseHelper.getRelease(context, releaseRn);
        if (checkCashe(context, release.name)) {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String SQL = "SELECT " +
                    "B." + COLUMN_RN + "," +
                    "B." + COLUMN_PRN + "," +
                    "B." + COLUMN_CODE + "," +
                    "B." + COLUMN_DATE +
                    " FROM " + TABLE_NAME + " B " +
                    " WHERE B." + COLUMN_RN + "= ? ";
            Cursor cursor = db.rawQuery(SQL, new String[]{String.valueOf(buildRn)});
            cursor.moveToFirst();
            int[] indeces = {
                    cursor.getColumnIndex(COLUMN_RN),     // 0
                    cursor.getColumnIndex(COLUMN_PRN),    // 1
                    cursor.getColumnIndex(COLUMN_CODE),   // 2
                    cursor.getColumnIndex(COLUMN_DATE),   // 3
            };
            build = new Build();
            build.rn = cursor.getLong(indeces[0]);
            build.prn = cursor.getLong(indeces[1]);
            build.mnemo = cursor.getString(indeces[2]);
            build.buildDate = cursor.getString(indeces[3]);
            build.displayName = buildDisplayName(release.name, build);
            cursor.close();
            db.close();
        }
        return build;
    }
}
