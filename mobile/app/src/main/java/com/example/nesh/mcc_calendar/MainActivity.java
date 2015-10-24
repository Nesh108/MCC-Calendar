package com.example.nesh.mcc_calendar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import me.everything.providers.android.calendar.CalendarProvider;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */
public class MainActivity extends AppCompatActivity {

    final CaldroidFragment caldroidFragment = new CaldroidFragment();
    private CaldroidFragment dialogCaldroidFragment;

    private ArrayList<Event> eventsList = new ArrayList<>();
    ArrayList<Event> eventsOnDate;

    private DBHandler dbHandler = new DBHandler(this);

    private ListView eventsListView;

    private Date selectedDate;

    private String usr, pass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (PrefUtils.getFromPrefs(this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__").equals("__UNKNOWN__")) {
            showLoginDialog();
        }
        else
            Toast.makeText(MainActivity.this, "Welcome back " + PrefUtils.getFromPrefs(this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__"), Toast.LENGTH_SHORT).show();


        // CALENDAR TEST
        CalendarProvider calendarProvider = new CalendarProvider(this);
        List<me.everything.providers.android.calendar.Calendar> calendars = calendarProvider.getCalendars().getList();

        for (me.everything.providers.android.calendar.Calendar c : calendars) {
            Log.d("CALENDAR", c.displayName);
            for (me.everything.providers.android.calendar.Event e : calendarProvider.getEvents(c.id).getList())
                Log.d("EVENTS", e.title + " - " + e.description);
        }

        /////

        // Setup Events
        setupEvents();

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // else activity is created from fresh
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
                selectedDate = date;
                showEventList();
            }

            @Override
            public void onChangeMonth(int month, int year) {
            }

            @Override
            public void onLongClickDate(Date date, View view) {

                selectedDate = date;

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View promptsView = li.inflate(R.layout.create_event_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.this);

                // set xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText summaryEventEdit = (EditText) promptsView
                        .findViewById(R.id.summaryEventEdit);
                final EditText descriptionEventEdit = (EditText) promptsView
                        .findViewById(R.id.descriptionEventEdit);
                final EditText locationEventEdit = (EditText) promptsView
                        .findViewById(R.id.locationEventEdit);
                final EditText dateStartEdit = (EditText) promptsView
                        .findViewById(R.id.dateStartEdit);
                final EditText dateEndEdit = (EditText) promptsView
                        .findViewById(R.id.dateEndEdit);
                final EditText dateUntilEdit = (EditText) promptsView
                        .findViewById(R.id.dateUntilEdit);

                final EditText freqPicker = (EditText) promptsView
                        .findViewById(R.id.intervalPicker);
                final Spinner freqSpinner = (Spinner) promptsView
                        .findViewById(R.id.freqSpinner);

                final ToggleButton visibilityToggle = (ToggleButton) promptsView
                        .findViewById(R.id.visibilityToggle);

                dateStartEdit.setText(date.toString());

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Create",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String summary = summaryEventEdit.getText().toString();
                                        String description = descriptionEventEdit.getText().toString();
                                        String location = locationEventEdit.getText().toString();
                                        String dateStart = dateStartEdit.getText().toString();
                                        String dateEnd = dateEndEdit.getText().toString();
                                        String dateUntil = dateUntilEdit.getText().toString();
                                        String interval = "" + freqPicker.getText().toString();
                                        String freq = freqSpinner.getSelectedItem().toString();
                                        String visibility = "PUBLIC";

                                        if (visibilityToggle.isChecked())
                                            visibility = "PRIVATE";

                                        try {
                                            Integer.parseInt(interval);
                                        } catch (Exception e) {
                                            Toast.makeText(MainActivity.this, "Interval must be a number.", Toast.LENGTH_SHORT).show();
                                        }

                                        if (!summary.equals("") && !dateStart.equals("") && !dateEnd.equals("") && !dateUntil.equals("") && (interval.equals("0") || !freq.equals("Pick Time Period"))) {

                                            DateFormat format = new SimpleDateFormat("EE");
                                            try {

                                                switch (freq) {
                                                    case "Day":
                                                        freq = "DAILY";
                                                        break;
                                                    case "Week":
                                                        freq = "WEEKLY";
                                                        break;
                                                    case "Month":
                                                        freq = "MONTHLY";
                                                        break;
                                                    case "Year":
                                                        freq = "YEARLY";
                                                        break;
                                                    default:
                                                        freq = "DAILY";
                                                        break;
                                                }

                                                Event e = new Event("", summary, description, location, visibility, freq, format.format(new Date(dateStart)).substring(0, 2).toUpperCase(), dateStart, dateEnd, dateUntil, interval);
                                                Log.d("Event_CREATE", e.toString());
                                                createEvent(e);

                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }

                                        } else
                                            // TODO: Better feedback
                                            Toast.makeText(MainActivity.this, "Error during the form check.", Toast.LENGTH_SHORT).show();


                                    }
                                })
                        .setNegativeButton("Discard",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {

                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);


        eventsListView = (ListView) findViewById(R.id.eventsListView);

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Toast.makeText(MainActivity.this, "UNKNOWN PLACEHOLDER: " + eventsOnDate.get(position).getDescription(), Toast.LENGTH_LONG).show();
            }
        });

        eventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {

                Event e = eventsOnDate.get(position);

                // get view
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View promptsView = li.inflate(R.layout.show_event_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.this);

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


        // TODO: REMOVE THIS
        // Testing Stuff
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------

   /*
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
        });*/

        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------
        //-------------------------------------------------------------------------------------

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }

        if (dialogCaldroidFragment != null) {
            dialogCaldroidFragment.saveStatesToKey(outState,
                    "DIALOG_CALDROID_SAVED_STATE");
        }
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
            Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_synchronize) {
            getCalendar();
            return true;
        } else if (id == R.id.action_list_view) {
            Intent intent = new Intent(MainActivity.this, ListEventsActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_import) {
            Toast.makeText(this, "Import Wizard...", Toast.LENGTH_LONG).show();
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
        job.execute(PrefUtils.getFromPrefs(this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__"));
    }

    protected void createEvent(Event e) {

        CreateEventRequest job = new CreateEventRequest();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

        job.execute(new String[]{e.getSummary(), e.getDescription(), sdf.format(e.getDateStart()), sdf.format(e.getDateEnd()), e.getLocation(), e.getFreq(), "" + e.getInterval(), sdf.format(e.getUntil()), e.getWeekStart(), e.getVisibility()});

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

    private class CreateEventRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_events_uri));
            client.addParam("name", params[0]);
            client.addParam("description", params[1]);
            client.addParam("dateStart", params[2]);
            client.addParam("dateEnd", params[3]);
            client.addParam("location", params[4]);
            client.addParam("recurFreq", params[5]);
            client.addParam("recurInterval", params[6]);
            client.addParam("recurUntil", params[7]);
            client.addParam("recurWeekStart", params[8]);
            client.addParam("scope", params[9]);

            for (String s : params)
                Log.d("PARAMS", s);

            // Specifying that the key-value pairs are sent in the JSON format
            client.addHeader("Content-type", "application/x-www-form-urlencoded");

            // Basic Authentication, From: http://blog.leocad.io/basic-http-authentication-on-android/
            String credentials = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__") + ":" + PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "__UNKNOWN__");
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            client.addHeader("Authorization", "Basic " + base64EncodedCredentials + " ");
            Log.d("AUTH", base64EncodedCredentials);


            return client.executePost();
        }

        @Override
        protected void onPostExecute(String message) {

            Log.d("EVENT_CREATE", message);
            getCalendar();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void processEvents(String events) {
        try {

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

                // Description
                String description;
                try {
                    description = component.getProperty("DESCRIPTION").getValue();
                } catch (Exception e) {
                    description = "";
                }

                // Location
                String location;
                try {
                    location = component.getProperty("LOCATION").getValue();
                } catch (Exception e) {
                    location = "";
                }

                // Visibility
                String visibility;
                try {
                    visibility = component.getProperty("CLASS").getValue();
                } catch (Exception e) {
                    visibility = "";
                }

                // Frequency
                String freq;
                try {
                    freq = component.getProperty("RRULE").getValue().split(";")[0].split("=")[1];
                } catch (Exception e) {
                    freq = "";
                }

                // Week Start
                String weekStart;
                try {
                    weekStart = component.getProperty("RRULE").getValue().split(";")[1].split("=")[1];
                } catch (Exception e) {
                    weekStart = "0";
                }

                // Interval
                int interval;
                try {
                    interval = Integer.parseInt(component.getProperty("RRULE").getValue().split(";")[3].split("=")[1]);
                } catch (Exception e) {
                    interval = 0;
                }

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
            showEvents();
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

        showEvents();
    }

    private void showEvents() {

        // Add event to calendar
        for (Event e : eventsList) {

            for (Date d : getDatesRange(e))
                if (e.getVisibility().equals("PUBLIC"))
                    caldroidFragment.setBackgroundResourceForDate(R.color.blue, d);
                else
                    caldroidFragment.setBackgroundResourceForDate(R.color.red, d);

            Log.d("iCal", "Event " + e.toString() + " added to calendar.");
        }

        // Refresh calendar view
        caldroidFragment.refreshView();
    }

    private ArrayList<Date> getDatesRange(Event e) {
        ArrayList<Date> dates = new ArrayList<>();

        // TODO: Do a better job than this
        // Calculate what are the dates in which the event happens
        dates.add(e.getDateStart());
        dates.add(e.getUntil());

        return dates;
    }

    private void showEventList() {
        eventsOnDate = getEvents(selectedDate);
        EventAdapter eventAdapter = new EventAdapter(MainActivity.this, eventsOnDate);
        eventsListView.setAdapter(eventAdapter);
    }

    // Finds all the events of a specific date
    private ArrayList<Event> getEvents(Date d) {
        ArrayList<Event> events = new ArrayList<>();

        if (d == null)
            return events;

        String refDate = new SimpleDateFormat("dd/MM/yyyy").format(d);

        for (Event e : eventsList) {
            for (Date d1 : getDatesRange(e)) {
                String date = new SimpleDateFormat("dd/MM/yyyy").format(d1);
                if (refDate.equals(date) && !events.contains(e))
                    events.add(e);

            }
        }

        return events;
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
            String credentials = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__") + ":" + PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__");
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

                // If the event is in the event list
                if (eventsList.indexOf(new Event(id)) != -1) {
                    // Make copy of the event to be removed and remove it
                    Event oldEvent = eventsList.remove(eventsList.indexOf(new Event(id)));

                    // Clear all the related dates
                    for (Date d : getDatesRange(oldEvent))
                        caldroidFragment.clearBackgroundResourceForDate(d);

                    // Refresh the calendar
                    showEvents();
                    showEventList();

                }

            }

        }
    }

    protected void updateEvent(Event e) {

        UpdateEventRequest job = new UpdateEventRequest();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

        job.execute(new String[]{e.get_id(), e.getSummary(), e.getDescription(), sdf.format(e.getDateStart()), sdf.format(e.getDateEnd()), e.getLocation(), e.getFreq(), "" + e.getInterval(), sdf.format(e.getUntil()), e.getWeekStart(), e.getVisibility()});

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
            client.addParam("scope", params[10]);

            for (String s : params)
                Log.d("PARAMS", s);

            // Specifying that the key-value pairs are sent in the JSON format
            client.addHeader("Content-type", "application/x-www-form-urlencoded");

            // Basic Authentication, From: http://blog.leocad.io/basic-http-authentication-on-android/
            String credentials = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__") + ":" + PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__");
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
                showEvents();
                showEventList();

            }
        }
    }

    protected void testLogin(String username, String password) {

        TestLoginRequest job = new TestLoginRequest();
        job.execute(username, password);
    }

    private class TestLoginRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            RestClient client = new RestClient(getResources().getString(R.string.rest_events_uri));

            // Specifying that the key-value pairs are sent in the JSON format
            client.addHeader("Content-type", "application/x-www-form-urlencoded");

            // Basic Authentication, From: http://blog.leocad.io/basic-http-authentication-on-android/
            String credentials = params[0] + ":" + params[1];
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            client.addHeader("Authorization", "Basic " + base64EncodedCredentials + " ");
            Log.d("AUTH", base64EncodedCredentials);

            Log.d("TEST", params[0] + " = " + params[1]);
            return client.executePost();
        }

        @Override
        protected void onPostExecute(String message) {

            if (message.contains("Unauthorized")) {
                Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                showLoginDialog();
            } else {
                Toast.makeText(MainActivity.this, "Welcome " + usr, Toast.LENGTH_SHORT).show();
                // Saving user credentials on successful login case
                PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, usr);
                PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, pass);

                usr = null;
                pass = null;
            }


        }
    }

    private void showAboutDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.about_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showLoginDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.login_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText usernameEdit = (EditText) promptsView
                .findViewById(R.id.usernameEdit);
        final EditText passwordEdit = (EditText) promptsView
                .findViewById(R.id.passwordEdit);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Login",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                usr = usernameEdit.getText().toString();
                                pass = passwordEdit.getText().toString();

                                Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                                testLogin(usr, pass);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
