package com.example.nesh.mcc_calendar;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.util.CompatibilityHints;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */
public class MainActivity extends AppCompatActivity {

    private boolean undo = false;

    final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
    final CaldroidFragment caldroidFragment = new CaldroidFragment();
    private CaldroidFragment dialogCaldroidFragment;

    private ArrayList<Event> eventsList = new ArrayList<>();

    private DBHandler dbHandler = new DBHandler(this);


    // Testing
    // TODO: Replace with ListView
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Setup Events
        setupEvents();

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {

            Bundle args = new Bundle();

            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
            caldroidFragment.setArguments(args);
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                String eventsText = "";

                // Setup Date Formatter
                DateFormat format = new SimpleDateFormat("HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));

                for(Event e : getEvents(date)){
                    eventsText += " - " + e.getSummary() + "\t|\t" + e.getLocation() + "\t|\t" +  format.format(e.getDateStart()) +"\n";
                }

                textView.setText(eventsText);

            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
                    Toast.makeText(getApplicationContext(),
                            "Caldroid view is created", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);


        // TODO: REMOVE THIS
        // Testing Stuff
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------

        textView = (TextView) findViewById(R.id.textview);
        final Button customizeButton = (Button) findViewById(R.id.customize_button);

        // Customize the calendar
        customizeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (undo) {
                    customizeButton.setText("Customize");
                    textView.setText("");

                    // Reset calendar
                    caldroidFragment.clearDisableDates();
                    caldroidFragment.clearSelectedDates();
                    caldroidFragment.setMinDate(null);
                    caldroidFragment.setMaxDate(null);
                    caldroidFragment.setShowNavigationArrows(true);
                    caldroidFragment.setEnableSwipe(true);
                    caldroidFragment.refreshView();
                    undo = false;
                    return;
                }

                // Else
                undo = true;
                customizeButton.setText("Undo");
                Calendar cal = Calendar.getInstance();

                // Min date is last 7 days
                cal.add(Calendar.DATE, -7);
                Date minDate = cal.getTime();

                // Max date is next 7 days
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 14);
                Date maxDate = cal.getTime();

                // Set selected dates
                // From Date
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 2);
                Date fromDate = cal.getTime();

                // To Date
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 3);
                Date toDate = cal.getTime();

                // Set disabled dates
                ArrayList<Date> disabledDates = new ArrayList<>();
                for (int i = 5; i < 8; i++) {
                    cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, i);
                    disabledDates.add(cal.getTime());
                }

                // Customize
                caldroidFragment.setMinDate(minDate);
                caldroidFragment.setMaxDate(maxDate);
                caldroidFragment.setDisableDates(disabledDates);
                caldroidFragment.setSelectedDates(fromDate, toDate);
                caldroidFragment.setShowNavigationArrows(false);
                caldroidFragment.setEnableSwipe(false);

                caldroidFragment.refreshView();

                // Move to date
                // cal = Calendar.getInstance();
                // cal.add(Calendar.MONTH, 12);
                // caldroidFragment.moveToDate(cal.getTime());

                String text = "Today: " + formatter.format(new Date()) + "\n";
                text += "Min Date: " + formatter.format(minDate) + "\n";
                text += "Max Date: " + formatter.format(maxDate) + "\n";
                text += "Select From Date: " + formatter.format(fromDate)
                        + "\n";
                text += "Select To Date: " + formatter.format(toDate) + "\n";
                for (Date date : disabledDates) {
                    text += "Disabled Date: " + formatter.format(date) + "\n";
                }

                textView.setText(text);
            }
        });

        Button showDialogButton = (Button) findViewById(R.id.show_dialog_button);

        final Bundle state = savedInstanceState;
        showDialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Setup caldroid to use as dialog
                dialogCaldroidFragment = new CaldroidFragment();
                dialogCaldroidFragment.setCaldroidListener(listener);

                // If activity is recovered from rotation
                final String dialogTag = "CALDROID_DIALOG_FRAGMENT";
                if (state != null) {
                    dialogCaldroidFragment.restoreDialogStatesFromKey(
                            getSupportFragmentManager(), state,
                            "DIALOG_CALDROID_SAVED_STATE", dialogTag);
                    Bundle args = dialogCaldroidFragment.getArguments();
                    if (args == null) {
                        args = new Bundle();
                        dialogCaldroidFragment.setArguments(args);
                    }
                } else {
                    // Setup arguments
                    Bundle bundle = new Bundle();
                    // Setup dialogTitle
                    dialogCaldroidFragment.setArguments(bundle);
                }

                dialogCaldroidFragment.show(getSupportFragmentManager(),
                        dialogTag);
            }
        });

        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "LOLLANDIA", Toast.LENGTH_LONG).show();
            return true;
        }
        else if (id == R.id.action_example) {
            getCalendar();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void getCalendar() {

        SendRESTRequest job = new SendRESTRequest();
        job.execute("nesh");
    }

    private class SendRESTRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_base_uri) + "?user=" + params[0]);  //Write your url here
            // client.addParam("Name", "Bhavit");

            client.addHeader("content-type", "application/json"); // Here I am specifying that the key-value pairs are sent in the JSON format

            return client.executeGet();
        }

        @Override
        protected void onPostExecute(String message) {
            processEvents(message);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void processEvents(String events){
        try {
                /*JSONObject response = new JSONObject(message);
                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();*/


            InputStream in = new ByteArrayInputStream(events.getBytes(StandardCharsets.UTF_8));

            CalendarBuilder builder = new CalendarBuilder();

            // To fix 'Unparseable date' error
            CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);

            net.fortuna.ical4j.model.Calendar calendar = builder.build(in);

            // Go through events in iCal file
            for (Object o : calendar.getComponents()) {
                Component component = (Component) o;
                Log.d("ICal", "Component [" + component.getName() + "]");

                // Getting data from iCal to create a new event
                String _id = component.getProperty("UID").getValue();
                String summary = component.getProperty("SUMMARY").getValue();
                String description = component.getProperty("DESCRIPTION").getValue();
                String location = component.getProperty("LOCATION").getValue();
                String visibility = component.getProperty("CLASS").getValue();
                String freq = component.getProperty("RRULE").getValue().split(";")[0].split("=")[1];
                String weekStart = component.getProperty("RRULE").getValue().split(";")[1].split("=")[1];

                int interval = Integer.parseInt(component.getProperty("RRULE").getValue().split(";")[3].split("=")[1]);

                net.fortuna.ical4j.model.Date dateStart = new net.fortuna.ical4j.model.Date(component.getProperty("DTSTART").getValue());
                net.fortuna.ical4j.model.Date dateEnd = new net.fortuna.ical4j.model.Date(component.getProperty("DTEND").getValue());
                net.fortuna.ical4j.model.Date until = new net.fortuna.ical4j.model.Date(component.getProperty("RRULE").getValue().split(";")[2].split("=")[1]);

                // Create a new event
                Event e = new Event(_id, summary, description, location, visibility, freq, weekStart, dateStart, dateEnd, until, interval);

                // Check if event is not already present
                if (!eventsList.contains(e)) {

                    // Add event to list
                    eventsList.add(e);

                    // Add event to the local DB
                    dbHandler.createEvent(e);

                } else
                    Log.d("iCal", "Event " + e.get_id() + " already in the list. Not added to calendar.");
            }

            // Show fetched events in the calendar
            showEvents();


        } catch (ParserException | ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setupEvents(){
        try {
            dbHandler.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fetch already existing events
        try {
            eventsList = dbHandler.getAllEvents();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        showEvents();
    }

    private void showEvents(){

        // Add event to calendar
        for(Event e : eventsList) {

            for(Date d : getDatesRange(e))
                caldroidFragment.setBackgroundResourceForDate(R.color.red, d);

            Log.d("iCal", "Event " + e.toString() + " added to calendar.");
        }

        // Refresh calendar view
        caldroidFragment.refreshView();
    }

    private ArrayList<Date> getDatesRange(Event e){
        ArrayList<Date> dates = new ArrayList<>();

        // TODO: Do a better job than this
        // Calculate what are the dates in which the event happens
        dates.add(e.getDateStart());
        dates.add(e.getUntil());

        return dates;
    }

    // Finds all the events of a specific date
    private ArrayList<Event> getEvents(Date d){
        ArrayList<Event> events = new ArrayList<>();

        String refDate = new SimpleDateFormat("dd/MM/yyyy").format(d);

        for(Event e : eventsList){
            for(Date d1 : getDatesRange(e))
            {
                String date = new SimpleDateFormat("dd/MM/yyyy").format(d1);
                if(refDate.equals(date))
                    events.add(e);

            }
        }

        return events;
    }
}
