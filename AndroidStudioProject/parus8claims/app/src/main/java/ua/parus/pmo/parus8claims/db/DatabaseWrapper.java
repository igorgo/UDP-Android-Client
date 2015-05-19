package ua.parus.pmo.parus8claims.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ua.parus.pmo.parus8claims.objects.dicts.ApplistHelper;
import ua.parus.pmo.parus8claims.objects.dicts.BuildHelper;
import ua.parus.pmo.parus8claims.objects.dicts.ReleaseHelper;
import ua.parus.pmo.parus8claims.objects.dicts.UnitHelper;


public class DatabaseWrapper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseWrapper.class.getSimpleName();
    private static final String DATABASE_NAME = "Claim.db";
    private static final int DATABASE_VERSION = 7;

    public DatabaseWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called if the database named DATABASE_NAME doesn't exist in order to create it.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "]...");
        Log.i(TAG, "Creating tables...");
        db.execSQL(ApplistHelper.SQL_CREATE_TABLE);
        db.execSQL(ReleaseHelper.SQL_CREATE_TABLE);
        db.execSQL(BuildHelper.SQL_CREATE_TABLE);
        db.execSQL(UnitHelper.SQL_CREATE_TABLE);
        db.execSQL(UnitHelper.UnitApplists.SQL_CREATE_TABLE);
        db.execSQL(UnitHelper.UnitFuncs.SQL_CREATE_TABLE);
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] created");
    }

    /**
     * Called when the DATABASE_VERSION is increased.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrade database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "]...");
        Log.i(TAG, "Dropping tables...");
        db.execSQL(ApplistHelper.SQL_DROP_TABLE);
        db.execSQL(ReleaseHelper.SQL_DROP_TABLE);
        db.execSQL(BuildHelper.SQL_DROP_TABLE);
        db.execSQL(UnitHelper.SQL_DROP_TABLE);
        db.execSQL(UnitHelper.UnitApplists.SQL_DROP_TABLE);
        db.execSQL(UnitHelper.UnitFuncs.SQL_DROP_TABLE);
        onCreate(db);
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] upgraded");
    }
}
