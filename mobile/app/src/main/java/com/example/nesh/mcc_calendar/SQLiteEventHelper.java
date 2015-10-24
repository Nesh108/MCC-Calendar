package com.example.nesh.mcc_calendar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */
public class SQLiteEventHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_DESCR = "description";
    public static final String COLUMN_LOC = "location";
    public static final String COLUMN_VIS = "visibility";
    public static final String COLUMN_FREQ = "freq";
    public static final String COLUMN_WEEKSTART = "weekStart";
    public static final String COLUMN_DATESTART = "dateStart";
    public static final String COLUMN_DATEEND = "dateEnd";
    public static final String COLUMN_UNTIL = "until";
    public static final String COLUMN_INTERVAL = "interval";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE table "
            + TABLE_EVENTS + "("
            + COLUMN_ID + " TEXT PRIMARY KEY" + ", "
            + COLUMN_SUMMARY + " TEXT NOT NULL" + ", "
            + COLUMN_DESCR + " TEXT NOT NULL" + ", "
            + COLUMN_LOC + " TEXT NOT NULL" + ", "
            + COLUMN_VIS + " TEXT NOT NULL" + ", "
            + COLUMN_FREQ + " TEXT NOT NULL" + ", "
            + COLUMN_WEEKSTART + " TEXT NOT NULL" + ", "
            + COLUMN_DATESTART + " TEXT NOT NULL" + ", "
            + COLUMN_DATEEND + " TEXT NOT NULL" + ", "
            + COLUMN_UNTIL + " TEXT NOT NULL" + ", "
            + COLUMN_INTERVAL + " TEXT NOT NULL"
            + ");";

    public SQLiteEventHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(SQLiteEventHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(database);
    }
}
