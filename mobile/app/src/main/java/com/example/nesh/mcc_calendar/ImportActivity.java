package com.example.nesh.mcc_calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;

public class ImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Calendar providers
        final CalendarProvider calendarProvider = new CalendarProvider(this);
        final List<Calendar> calendars = calendarProvider.getCalendars().getList();

        // View
        final Intent intent = getIntent();
        TextView titleTV = (TextView) findViewById(R.id.titleTV);
        final ListView importListView = (ListView) findViewById(R.id.importListView);
        importListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Buttons
        Button backBtn = (Button) findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ImportActivity.this, ImportActivity.class);
                startActivity(i);
                finish();
            }
        });

        Button confirmBtn = (Button) findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SparseBooleanArray checked = importListView.getCheckedItemPositions();
                for (int i = 0; i < calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().size(); i++) {
                    if (checked.get(i)) {
                        String ev_title = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).title;
                        String ev_description = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).description;
                        String ev_location = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).eventLocation;
                        Date ev_dtstart = new Date(calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).dTStart);
                        Date ev_dtend = new Date(calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).dTend);
                        String ev_visibility = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).visible ? "PUBLIC" : "PRIVATE";
                        String ev_rrule = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(i).rRule;

                        String ev_freq = "DAILY";
                        Date ev_until = ev_dtend;
                        int ev_interval = 0;

                        if (ev_rrule != null) {
                            Log.d("RRULE_IMPORT", ev_rrule);
                        }

                        if(ev_description.equals(""))
                            ev_description = "No description.";

                        DateFormat format = new SimpleDateFormat("EE");
                        Event e = new Event("", ev_title, ev_description, ev_location, ev_visibility, ev_freq, format.format(ev_dtstart).substring(0, 2).toUpperCase(), ev_dtstart, ev_dtend, ev_until, ev_interval);

                        Log.d("IMPORT", e.toString());
                        createEvent(e);
                    }
                }

                Intent in = new Intent(ImportActivity.this, MainActivity.class);
                in.putExtra("FORCE_SYNCH", true);
                startActivity(in);
                finish();

            }
        });

        // Calendar already picked
        if (intent.hasExtra("Calendar_Name") && intent.hasExtra("Calendar_ID")) {
            titleTV.setText("Pick events from '" + intent.getStringExtra("Calendar_Name") + "' to import:");

            backBtn.setVisibility(View.VISIBLE);
            confirmBtn.setVisibility(View.VISIBLE);

            final String[] eventList = new String[calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().size()];
            int i = 0;
            for (me.everything.providers.android.calendar.Event e : calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList()) {
                eventList[i] = e.title + " | " + e.description;
                i++;
            }

            ArrayAdapter<String> eventsArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, eventList);
            importListView.setAdapter(eventsArrayAdapter);

            // Pick a calendar and show events
            importListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos,
                                        long id) {

                    String event_name = calendarProvider.getEvents(intent.getLongExtra("Calendar_ID", 0)).getList().get(pos).title;
                    Toast.makeText(ImportActivity.this, "Picked: " + event_name, Toast.LENGTH_SHORT).show();

                }
            });

        } else    // Calendar not picked
        {
            titleTV.setText("Choose calendar:");

            backBtn.setVisibility(View.INVISIBLE);
            confirmBtn.setVisibility(View.INVISIBLE);

            String[] calendarList = new String[calendars.size()];
            int i = 0;
            for (me.everything.providers.android.calendar.Calendar c : calendars) {
                calendarList[i] = c.displayName;
                i++;
            }

            ArrayAdapter<String> calendarArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, calendarList);
            importListView.setAdapter(calendarArrayAdapter);

            // Pick a calendar and show events
            importListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos,
                                        long id) {

                    String calendar_name = calendars.get(pos).displayName;
                    Long calendar_ID = calendars.get(pos).id;

                    Intent i = new Intent(ImportActivity.this, ImportActivity.class);
                    i.putExtra("Calendar_Name", calendar_name);
                    i.putExtra("Calendar_ID", calendar_ID);
                    startActivity(i);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_import, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(ImportActivity.this);
        View promptsView = li.inflate(R.layout.about_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ImportActivity.this);

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

    protected void createEvent(Event e) {

        CreateEventRequest job = new CreateEventRequest();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

        job.execute(new String[]{e.getSummary(), e.getDescription(), sdf.format(e.getDateStart()), sdf.format(e.getDateEnd()), e.getLocation(), e.getFreq(), "" + e.getInterval(), sdf.format(e.getUntil()), e.getWeekStart(), e.getVisibility()});

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
            String credentials = PrefUtils.getFromPrefs(ImportActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__") + ":" + PrefUtils.getFromPrefs(ImportActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "__UNKNOWN__");
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            client.addHeader("Authorization", "Basic " + base64EncodedCredentials + " ");
            Log.d("AUTH", base64EncodedCredentials);

            return client.executePost();
        }

        @Override
        protected void onPostExecute(String message) {

            Log.d("EVENT_CREATE", message);
        }
    }
}
