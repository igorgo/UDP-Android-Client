package ua.parus.pmo.parus8claims.objects.dicts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ua.parus.pmo.parus8claims.db.DatabaseWrapper;

public class ApplistHelper {


    public static final String FIELD_NAME = "n";
    public static final String TABLE_NAME = "applist";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "appname";
    public static final String SQL_INSERT =
            "INSERT INTO " + TABLE_NAME + "("
                    + COLUMN_NAME
                    + ") VALUES (?)";
    public static final String REST_URL = "dicts/applist/";
    @SuppressWarnings("unused")
    private static final String TAG = ApplistHelper.class.getSimpleName();
    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COLUMN_NAME_TYPE = "TEXT";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_NAME + " " + COLUMN_NAME_TYPE +
                    ")";


    public static List<String> getAppsAll(Context context) {
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

    public static List<Applist> getApplistAll(Context context) {
        List<Applist> appList = new ArrayList<>();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase db = databaseWrapper.getReadableDatabase();
        String SQL = "SELECT " + COLUMN_ID + COMMA_SEP + COLUMN_NAME +
                " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Applist app = new Applist();
            app.id = cursor.getInt(0);
            app.name = cursor.getString(1);
            appList.add(app);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return appList;
    }
}
