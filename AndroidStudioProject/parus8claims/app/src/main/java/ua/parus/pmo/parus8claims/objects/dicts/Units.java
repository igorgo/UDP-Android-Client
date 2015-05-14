package ua.parus.pmo.parus8claims.objects.dicts;

import android.app.ProgressDialog;
import android.content.ContentValues;
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

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.db.DatabaseWrapper;
import ua.parus.pmo.parus8claims.rest.RestRequest;

/**
 * Created  by igorgo on 18.04.2015.
 */

public class Units {
    private static final String TAG = Units.class.getSimpleName();
    private static final String TABLE_NAME = "units";
    private static final String COLUMN_ID = "id";
    private static final String TYPE_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COLUMN_NAME = "uname";
    private static final String TYPE_NAME = "TEXT";
    private static final String COLUMN_DEPS_CACHED = "apps_cached";
    private static final String TYPE_DEPS_CACHED = "INTEGER";
    private static final String COMMA_SEP = ", ";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + TYPE_ID + COMMA_SEP +
                    COLUMN_NAME + " " + TYPE_NAME + COMMA_SEP +
                    COLUMN_DEPS_CACHED + " " + TYPE_DEPS_CACHED +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_INSERT =
            "INSERT INTO " + TABLE_NAME + "("
                    + COLUMN_NAME + COMMA_SEP
                    + COLUMN_DEPS_CACHED
                    + ") VALUES (?,0)";

    private static final String SQL_UPDATE =
            "UPDATE " + TABLE_NAME
                    + " SET " + COLUMN_DEPS_CACHED + "=1"
                    + " WHERE " + COLUMN_ID + "=?";


    private static final String URL_DEPS = "dicts/units/deps/";
    private static final String URL_UNITS = "dicts/units/";
    private static final String REST_PARAM_PEPS_UNIT =  "unitname";

    public static void refreshCache(Context context) {
        final Context lContext = context;
        final ProgressDialog loadDialog = ProgressDialog.show(lContext, lContext.getString(R.string.please_wait),
                lContext.getString(R.string.loading_unitlist), true);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DatabaseWrapper databaseWrapper = new DatabaseWrapper(lContext);
                        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
                        try {
                            RestRequest restRequest = new RestRequest(URL_UNITS);
                            JSONArray response = restRequest.getAllRows();
                            if (response != null) {
                                db.delete(TABLE_NAME, null, null);
                                db.delete(UnitApplists.TABLE_NAME, null, null);
                                db.delete(UnitFuncs.TABLE_NAME, null, null);
                                db.beginTransaction();
                                try {
                                    SQLiteStatement statement = db.compileStatement(SQL_INSERT);
                                    for (int i = 0; i < response.length(); i++) {
                                        try {
                                            JSONObject c = response.getJSONObject(i);
                                            statement.bindString(1, c.getString("n"));
                                            statement.execute();
                                            statement.clearBindings();
                                            //Log.i(TAG, "Inserted new Unit with NAME: " + c.getString("n"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }
                            }
                        } catch (MalformedURLException | ConnectException e) {
                            e.printStackTrace();
                        } finally {
                            db.close();
                            loadDialog.dismiss();
                        }
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

    public static List<String> getUnits(Context context) {
        checkCache(context);
        List<String> unitsList = new ArrayList<>();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String SQL = "SELECT " + COLUMN_NAME +
                " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_NAME;
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            unitsList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return unitsList;
    }

    private static Unit getUnitByName(Context context, String name){
        Unit unit = new Unit();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        String SQL = "SELECT " +
                COLUMN_ID + COMMA_SEP +
                COLUMN_NAME + COMMA_SEP +
                COLUMN_DEPS_CACHED +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME + "=?";
        Cursor cursor = db.rawQuery(SQL, new String[]{name});
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            unit.id = cursor.getInt(0);
            unit.name = cursor.getString(1);
            unit.depsCashed = cursor.getInt(2) == 1;
        }
        cursor.close();
        db.close();
        return unit;
    }

    private static void loadDepsFromServer(Context context, Unit unit) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getWritableDatabase();
        try {
            RestRequest restRequest = new RestRequest(URL_DEPS);
            restRequest.addInParam(REST_PARAM_PEPS_UNIT, unit.name);
            Log.i(TAG,"Request depends for " + unit.name);
            JSONArray items = restRequest.getAllRows();
            long app;
            ContentValues values;
            long newId;
            if (items != null) {
                Log.i(TAG, "Response has " + items.length() + " items;");
                List<Applist> apps = Applists.getApplistAll(context);
                db.beginTransaction();
                try {
                    SQLiteStatement stInsertApp = db.compileStatement(UnitApplists.SQL_INSERT);
                    SQLiteStatement stInsertFunc = db.compileStatement(UnitFuncs.SQL_INSERT);
                    for (int i = 0; i < items.length(); i++) {
                        try {
                            JSONObject item = items.getJSONObject(i);
                            if (item.getString("s01").equals("A")) {
                                app = -1;
                                for (int j = 0; j < apps.size(); j++) {
                                    if (apps.get(j).name.equals(item.getString("s02"))) {
                                        app = apps.get(j).id;
                                        break;
                                    }
                                }
                                //app = Applists.getAppByName(context, item.getString("s02"));
                                if (app >= 0) {
                                    stInsertApp.bindLong(1, unit.id);
                                    stInsertApp.bindLong(2, app);
                                    stInsertApp.execute();
                                    stInsertApp.clearBindings();
                                    /*values = new ContentValues();
                                    values.put(UnitApplists.COLUMN_APP, app.id);
                                    values.put(UnitApplists.COLUMN_UNIT, unit.id);
                                    newId = db.insert(UnitApplists.TABLE_NAME, "null", values);
                                    Log.i(TAG, "Inserted new Application dependency - " + values);*/
                                }
                            }
                            if (item.getString("s01").equals("F")) {
                                stInsertFunc.bindLong(1, unit.id);
                                stInsertFunc.bindString(2, item.getString("s02"));
                                stInsertFunc.execute();
                                stInsertFunc.clearBindings();
                               /* values = new ContentValues();
                                values.put(UnitFuncs.COLUMN_UNIT, unit.id);
                                values.put(UnitFuncs.COLUMN_FUNC, item.getString("s02"));
                                newId = db.insert(UnitFuncs.TABLE_NAME, "null", values);
                                Log.i(TAG, "Inserted new Unit function - " + values);*/
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i(TAG, "Set unit cached ");
                    SQLiteStatement stUpdateUnit = db.compileStatement(SQL_UPDATE);
                    stUpdateUnit.bindLong(1,unit.id);
                    stUpdateUnit.execute();
                    stUpdateUnit.clearBindings();
                    db.setTransactionSuccessful();
/*
                    values = new ContentValues();
                    values.put(COLUMN_DEPS_CACHED, 1);
                    db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(unit.id)});
*/
                } finally {
                    db.endTransaction();
                }
            }
        } catch (MalformedURLException | ConnectException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public static List<String> getUnitApps (Context context, String unitname) {
        List<String> appList = new ArrayList<>();
        Unit unit = getUnitByName(context, unitname);
        Log.i(TAG,"unit:" + unit.id + ", depsCashed:" + unit.depsCashed);
        if (unit.id >=0 ) {
            if (!unit.depsCashed) {
                loadDepsFromServer(context, unit);
            }
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String SQL = "SELECT " + Applists.COLUMN_NAME +
                    " FROM " + Applists.TABLE_NAME +
                    " WHERE " + Applists.COLUMN_ID + " IN (" +
                    " SELECT " + UnitApplists.COLUMN_APP +
                    " FROM " + UnitApplists.TABLE_NAME +
                    " WHERE " + UnitApplists.COLUMN_UNIT + "=?" +
                    " ) ORDER BY " + Applists.COLUMN_NAME;
            Cursor cursor = db.rawQuery(SQL, new String[]{String.valueOf(unit.id)});
            cursor.moveToFirst();
            Log.i(TAG,"Quering Apps:\n\tSQL: " + SQL + "\n\tcursor: " + cursor.getCount());
            while (!cursor.isAfterLast()) {
                appList.add(cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return appList;
    }

    public static List<String> getUnitFuncs (Context context, String unitname) {
        List<String> funcList = new ArrayList<>();
        Unit unit = getUnitByName(context,unitname);
        if (unit.id >=0 ) {
            if (!unit.depsCashed) {
                loadDepsFromServer(context, unit);
            }
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
            SQLiteDatabase db = databaseWrapper.getReadableDatabase();
            String SQL = "SELECT " + UnitFuncs.COLUMN_FUNC +
                    " FROM " + UnitFuncs.TABLE_NAME +
                    " WHERE " + UnitFuncs.COLUMN_UNIT + "=?" +
                    " ORDER BY " + UnitFuncs.COLUMN_ID;
            Cursor cursor = db.rawQuery(SQL, new String[]{String.valueOf(unit.id)});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                funcList.add(cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return funcList;
    }

}
