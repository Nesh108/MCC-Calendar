package com.example.nesh.mcc_calendar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.util.List;
import java.util.Locale;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */
public class ListEventsActivity extends AppCompatActivity {

    private ArrayList<Event> eventsList = new ArrayList<>();

    private DBHandler dbHandler = new DBHandler(this);

    private ListView eventsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        eventsListView = (ListView) findViewById(R.id.eventsListView);

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Toast.makeText(ListEventsActivity.this, "UNKNOWN PLACEHOLDER: " + eventsList.get(position).getDescription(), Toast.LENGTH_LONG).show();
            }
        });

        eventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {

                Event e = eventsList.get(position);

                // get view
                LayoutInflater li = LayoutInflater.from(ListEventsActivity.this);
                View promptsView = li.inflate(R.layout.show_event_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ListEventsActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final TextView summaryEventTV = (TextView) promptsView
                        .findViewById(R.id.summaryEventTV);
                final TextView descriptionEventTV = (TextView) promptsView
                        .findViewById(R.id.descriptionEventTV);
                final TextView locationEventTV = (TextView) promptsView
                        .findViewById(R.id.locationEventTV);
                final TextView dateStartTV = (TextView) promptsView
                        .findViewById(R.id.dateStartTV);
                final TextView dateEndTV = (TextView) promptsView
                        .findViewById(R.id.dateEndTV);
                final TextView dateUntilTV = (TextView) promptsView
                        .findViewById(R.id.dateUntilTV);
                final TextView intervalTV = (TextView) promptsView
                        .findViewById(R.id.intervalTV);
                final TextView visibilityTV = (TextView) promptsView
                        .findViewById(R.id.visibilityTV);

                summaryEventTV.setText(Html.fromHtml("<b>Summary:</b> " + e.getSummary()));
                descriptionEventTV.setText(Html.fromHtml("<b>Description:</b> " + e.getDescription()));
                locationEventTV.setText(Html.fromHtml("<b>Location:</b> " + e.getLocation()));
                dateStartTV.setText(Html.fromHtml("<b>Event Start:</b> " + e.getDateStart().toString()));
                dateEndTV.setText(Html.fromHtml("<b>Event End:</b> " + e.getDateEnd().toString()));
                dateUntilTV.setText(Html.fromHtml("<b>Until:</b> " + e.getUntil().toString()));

                String freq;
                switch (e.getFreq()) {
                    case "DAILY":
                        freq = "Day";
                        break;
                    case "WEEKLY":
                        freq = "Week";
                        break;
                    case "MONTHLY":
                        freq = "Month";
                        break;
                    case "YEARLY":
                        freq = "Year";
                        break;
                    default:
                        freq = "Pick Time Period";
                        break;
                }

                if (e.getInterval() != 1)
                    freq += "s";
                intervalTV.setText(Html.fromHtml("<b>Interval:</b> Repeat every " + e.getInterval() + " " + freq));

                visibilityTV.setText(Html.fromHtml("<b>Visibility:</b> " + e.getVisibility()));

                // set dialog message
                alertDialogBuilder
                        .setCancelable(true)
                        .setNegativeButton("CLOSE",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            }
        });
        // Setup Events
        setupEvents();
        showEventList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_events, menu);
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
            Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_synchronize) {
            getCalendar();
            return true;
        } else if (id == R.id.action_calendar_view) {
            Intent intent = new Intent(ListEventsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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

        SendSynchronizeRequest job = new SendSynchronizeRequest();
        job.execute(getResources().getString(R.string.username));
    }


    private class SendSynchronizeRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_synch_uri) + "?user=" + params[0]);

            client.addHeader("content-type", "application/json");

            return client.executeGet();
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                processEvents(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void processEvents(String events) {
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


                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyyMMdd'T'HHmmss");

                Date dateStart = sdf.parse(component.getProperty("DTSTART").getValue().replaceAll("Z$", "+0000"));
                Date dateEnd = sdf.parse(component.getProperty("DTEND").getValue().replaceAll("Z$", "+0000"));
                Date until = sdf.parse(component.getProperty("RRULE").getValue().split(";")[2].split("=")[1].replaceAll("Z$", "+0000"));

                Log.d("Date", dateStart.toString());
                Log.d("Date", dateEnd.toString());
                Log.d("Date", until.toString());

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
            showEventList();


        } catch (ParserException | IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setupEvents() {
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

    }

    private void showEventList() {
        EventAdapter eventAdapter = new EventAdapter(ListEventsActivity.this, eventsList);
        eventsListView.setAdapter(eventAdapter);
    }


    protected void deleteEvent(String id) {

        DeleteEventRequest job = new DeleteEventRequest();
        job.execute(id);
    }

    private class DeleteEventRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_events_uri) + params[0]);

            // Specifying that the key-value pairs are sent in the JSON format
            client.addHeader("Content-type", "application/x-www-form-urlencoded");

            // Basic Authentication, From: http://blog.leocad.io/basic-http-authentication-on-android/
            String credentials = getResources().getString(R.string.username) + ":" + getResources().getString(R.string.password);
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            client.addHeader("Authorization", "Basic " + base64EncodedCredentials + " ");
            Log.d("AUTH", base64EncodedCredentials);

            return client.executeDelete();
        }

        @Override
        protected void onPostExecute(String message) {

            Log.d("EVENT_DELETE", message);

            if (message.contains("successfully deleted")) {
                String id = message.split(" ")[1];
                Log.d("EVENT_DELETE", "ID: " + id);
                dbHandler.deleteEvent(id);

                // Rremove event
                eventsList.remove(eventsList.indexOf(new Event(id)));

                // Refresh the calendar
                showEventList();


            }

        }

    }

    protected void updateEvent(Event e){

        UpdateEventRequest job = new UpdateEventRequest();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

        job.execute(new String[]{e.get_id(), e.getSummary(), e.getDescription(), sdf.format(e.getDateStart()), sdf.format(e.getDateEnd()), e.getLocation(), e.getFreq(), "" + e.getInterval(), sdf.format(e.getUntil()), e.getWeekStart()});

    }

    private class UpdateEventRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_events_uri) + params[0]);
            client.addParam("name", params[1]);
            client.addParam("description", params[2]);
            client.addParam("dateStart", params[3]);
            client.addParam("dateEnd", params[4]);
            client.addParam("location", params[5]);
            client.addParam("recurFreq", params[6]);
            client.addParam("recurInterval", params[7]);
            client.addParam("recurUntil", params[8]);
            client.addParam("recurWeekStart", params[9]);

            for (String s : params)
                Log.d("PARAMS", s);

            // Specifying that the key-value pairs are sent in the JSON format
            client.addHeader("Content-type", "application/x-www-form-urlencoded");

            // Basic Authentication, From: http://blog.leocad.io/basic-http-authentication-on-android/
            String credentials = getResources().getString(R.string.username) + ":" + getResources().getString(R.string.password);
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            client.addHeader("Authorization", "Basic " + base64EncodedCredentials + " ");
            Log.d("AUTH", base64EncodedCredentials);

            return client.executePut();
        }

        @Override
        protected void onPostExecute(String message) {

            Log.d("EVENT_UPDATE", message);
            if (message.contains("successfully")) {
                String id = message.split(" ")[1];
                Log.d("EVENT_UPDATE", "ID: " + id);

                dbHandler.deleteEvent(id);

                // Make copy of the event to be removed and remove it
                eventsList.remove(eventsList.indexOf(new Event(id)));

                // Refresh the calendar
                getCalendar();
                showEventList();

            }
        }
    }
}
