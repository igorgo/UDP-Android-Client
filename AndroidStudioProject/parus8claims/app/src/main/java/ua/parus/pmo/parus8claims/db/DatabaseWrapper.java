package ua.parus.pmo.parus8claims.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ua.parus.pmo.parus8claims.objects.dicts.Applists;
import ua.parus.pmo.parus8claims.objects.dicts.Builds;
import ua.parus.pmo.parus8claims.objects.dicts.Releases;
import ua.parus.pmo.parus8claims.objects.dicts.UnitApplists;
import ua.parus.pmo.parus8claims.objects.dicts.UnitFuncs;
import ua.parus.pmo.parus8claims.objects.dicts.Units;

/**
 * Created  by igorgo on 11.04.2015.
 */

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
        db.execSQL(Applists.SQL_CREATE_TABLE);
        db.execSQL(Releases.SQL_CREATE_TABLE);
        db.execSQL(Builds.SQL_CREATE_TABLE);
        db.execSQL(Units.SQL_CREATE_TABLE);
        db.execSQL(UnitApplists.SQL_CREATE_TABLE);
        db.execSQL(UnitFuncs.SQL_CREATE_TABLE);
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] created");
    }

    /**
     * Called when the DATABASE_VERSION is increased.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrade database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "]...");
        Log.i(TAG, "Dropping tables...");
        db.execSQL(Applists.SQL_DROP_TABLE);
        db.execSQL(Releases.SQL_DROP_TABLE);
        db.execSQL(Builds.SQL_DROP_TABLE);
        db.execSQL(Units.SQL_DROP_TABLE);
        db.execSQL(UnitApplists.SQL_DROP_TABLE);
        db.execSQL(UnitFuncs.SQL_DROP_TABLE);
        onCreate(db);
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] upgraded");
    }
}
