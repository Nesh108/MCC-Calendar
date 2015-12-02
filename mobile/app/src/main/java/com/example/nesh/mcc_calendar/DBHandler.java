package com.example.nesh.mcc_calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */
public class DBHandler {

    // Database fields
    private SQLiteDatabase database;
    private SQLiteEventHelper dbHelper;
    private String[] allColumns = {SQLiteEventHelper.COLUMN_ID, SQLiteEventHelper.COLUMN_SUMMARY, SQLiteEventHelper.COLUMN_DESCR,
            SQLiteEventHelper.COLUMN_LOC, SQLiteEventHelper.COLUMN_VIS, SQLiteEventHelper.COLUMN_FREQ,
            SQLiteEventHelper.COLUMN_WEEKSTART, SQLiteEventHelper.COLUMN_DATESTART, SQLiteEventHelper.COLUMN_DATEEND,
            SQLiteEventHelper.COLUMN_UNTIL, SQLiteEventHelper.COLUMN_INTERVAL};

    public DBHandler(Context context) {
        dbHelper = new SQLiteEventHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createEvent(Event e) {
        ContentValues values = new ContentValues();
        values.put(SQLiteEventHelper.COLUMN_ID, e.get_id());
        values.put(SQLiteEventHelper.COLUMN_SUMMARY, e.getSummary());
        values.put(SQLiteEventHelper.COLUMN_DESCR, e.getDescription());
        values.put(SQLiteEventHelper.COLUMN_LOC, e.getLocation());
        values.put(SQLiteEventHelper.COLUMN_VIS, e.getVisibility());
        values.put(SQLiteEventHelper.COLUMN_FREQ, e.getFreq());
        values.put(SQLiteEventHelper.COLUMN_WEEKSTART, e.getWeekStart());
        values.put(SQLiteEventHelper.COLUMN_DATESTART, e.getDateStart().toString());
        values.put(SQLiteEventHelper.COLUMN_DATEEND, e.getDateEnd().toString());
        values.put(SQLiteEventHelper.COLUMN_UNTIL, e.getUntil().toString());
        values.put(SQLiteEventHelper.COLUMN_INTERVAL, "" + e.getInterval());


        long insertId = database.insert(SQLiteEventHelper.TABLE_EVENTS, null,
                values);

        Log.d("DB_Event", "Inserted event. ID: " + insertId);

    }

    public void deleteEvent(Event e) {
        String id = e.get_id();
        database.delete(SQLiteEventHelper.TABLE_EVENTS, SQLiteEventHelper.COLUMN_ID
                + " = '" + id + "'", null);
    }

    public void deleteEvent(String id) {
        database.delete(SQLiteEventHelper.TABLE_EVENTS, SQLiteEventHelper.COLUMN_ID
                + " = '" + id + "'", null);
    }

    public ArrayList<Event> getAllEvents() throws ParseException {
        ArrayList<Event> events = new ArrayList<>();

        Cursor cursor = database.query(SQLiteEventHelper.TABLE_EVENTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                Event event = cursorToEvent(cursor);
                events.add(event);
                Log.d("DB_EVENT", "Fetched: " + event.toString());
            }
            catch(Exception e){}
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return events;
    }

    // Converts cursor to an Event
    private Event cursorToEvent(Cursor cursor) throws ParseException {
        Event event;

        try{
            event = new Event(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10));
        }
        catch (IllegalArgumentException ex){

            String dtstart = cursor.getString(7);
            String dtend = cursor.getString(8);
            String until = cursor.getString(9);

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyyMMdd'T'HHmmss");

            Date dateStart = sdf.parse(dtstart);
            Date dateEnd = sdf.parse(dtend);
            Date dateUntil = sdf.parse(until);

            event = new Event(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),dateStart, dateEnd, dateUntil, Integer.parseInt(cursor.getString(10)));

        }

        return event;
    }

    public void clerDatabase(Context context) {
        new SQLiteEventHelper(context).onUpgrade(database, 1, 1);
    }
}
