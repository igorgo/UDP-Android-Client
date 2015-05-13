package ua.parus.pmo.parus8claims.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by igorgo on 11.04.2015.
 */
@SuppressWarnings("ALL")
class DatabaseWrapper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseWrapper";

    private static final String DATABASE_NAME = "Claim.db";
    private static final int DATABASE_VERSION = 4;


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
        db.execSQL(ApplistORM.SQL_CREATE_TABLE);
        db.execSQL(ReleasesORM.SQL_CREATE_TABLE);
        db.execSQL(BuildsORM.SQL_CREATE_TABLE);
        db.execSQL(UnitsORM.SQL_CREATE_TABLE);
        //TTODO: add creator database
        //db.close();
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] created");
    }

    /**
     * Called when the DATABASE_VERSION is increased.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrade database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "]...");
        Log.i(TAG, "Dropping tables...");
        db.execSQL(ApplistORM.SQL_DROP_TABLE);
        db.execSQL(ReleasesORM.SQL_DROP_TABLE);
        db.execSQL(BuildsORM.SQL_DROP_TABLE);
        db.execSQL(UnitsORM.SQL_DROP_TABLE);
        //TTODO: add upgrading database
        onCreate(db);
        Log.i(TAG, "Database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "] upgraded");
    }
}
