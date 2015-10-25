package com.example.nesh.mcc_calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alberto Vaccari on 24-Oct-15.
 */
public class EventAdapter extends ArrayAdapter<Event> {

    Context parentContext;
    String parentClassName;

    private static String string_to_update;

    public EventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        parentContext = context;

        String[] path = context.getClass().getName().split("\\.");
        parentClassName = path[path.length - 1];

    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final Event e = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        }
        // Lookup view for data population
        TextView eventTv = (TextView) convertView.findViewById(R.id.eventTv);
        ImageButton deleteEventBtn = (ImageButton) convertView.findViewById(R.id.deleteEventBtn);
        ImageButton showEventBtn = (ImageButton) convertView.findViewById(R.id.showEventBtn);
        ImageButton exportEventBtn = (ImageButton) convertView.findViewById(R.id.exportEventBtn);

        // Setup Date Formatter
        DateFormat format = new SimpleDateFormat("HH:mm:ss");

        // Populate the data into the template view using the data object
        eventTv.setText(StringUtils.abbreviate(e.getSummary(), 42) + "\t |\t" + format.format(e.getDateStart()));

        deleteEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (parentClassName.equals("MainActivity"))
                                    ((MainActivity) parentContext).deleteEvent(e.get_id());
                                else if (parentClassName.equals("ListEventsActivity"))
                                    ((ListEventsActivity) parentContext).deleteEvent(e.get_id());

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });

        showEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get view
                LayoutInflater li = LayoutInflater.from(parentContext);
                View promptsView = li.inflate(R.layout.create_event_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        parentContext);

                // set prompts.xml to alertdialog builder
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

                summaryEventEdit.setText(e.getSummary());
                descriptionEventEdit.setText(e.getDescription());
                locationEventEdit.setText(e.getLocation());
                dateStartEdit.setText(e.getDateStart().toString());
                dateEndEdit.setText(e.getDateEnd().toString());
                dateUntilEdit.setText(e.getUntil().toString());
                freqPicker.setText("" + e.getInterval());

                // Date Picker

                final Calendar myCalendarPicker = Calendar.getInstance();

                final DatePickerDialog.OnDateSetListener pickerDate = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendarPicker.set(Calendar.YEAR, year);
                        myCalendarPicker.set(Calendar.MONTH, monthOfYear);
                        myCalendarPicker.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        if (EventAdapter.string_to_update.equals("dateStart"))
                            dateStartEdit.setText(myCalendarPicker.getTime().toString());
                        else if (EventAdapter.string_to_update.equals("dateEnd"))
                            dateEndEdit.setText(myCalendarPicker.getTime().toString());
                        else if (EventAdapter.string_to_update.equals("dateUntil"))
                            dateUntilEdit.setText(myCalendarPicker.getTime().toString());

                        EventAdapter.string_to_update = "";
                    }

                };


                dateStartEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EventAdapter.string_to_update = "dateStart";
                        new DatePickerDialog(parentContext, pickerDate, myCalendarPicker
                                .get(Calendar.YEAR), myCalendarPicker.get(Calendar.MONTH),
                                myCalendarPicker.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });
                dateEndEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EventAdapter.string_to_update = "dateEnd";
                        new DatePickerDialog(parentContext, pickerDate, myCalendarPicker
                                .get(Calendar.YEAR), myCalendarPicker.get(Calendar.MONTH),
                                myCalendarPicker.get(Calendar.DAY_OF_MONTH)).show();

                    }
                });
                dateUntilEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EventAdapter.string_to_update = "dateUntil";
                        new DatePickerDialog(parentContext, pickerDate, myCalendarPicker
                                .get(Calendar.YEAR), myCalendarPicker.get(Calendar.MONTH),
                                myCalendarPicker.get(Calendar.DAY_OF_MONTH)).show();

                    }
                });

                // Select correct value from freq spinner
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

                int pos = 0;
                for (String s : parentContext.getResources().getStringArray(R.array.freq_list)) {
                    if (s.equals(freq))
                        break;
                    pos++;
                }
                freqSpinner.setSelection(pos);

                if (e.getVisibility().equals("PRIVATE"))
                    visibilityToggle.setChecked(true);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("UPDATE",
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
                                            Toast.makeText(parentContext, "Interval must be a number.", Toast.LENGTH_SHORT).show();
                                        }

                                        if (!summary.equals("") && !description.equals("") && !dateStart.equals("") && !dateEnd.equals("") && !dateUntil.equals("") && (interval.equals("0") || !freq.equals("Pick Time Period"))) {

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

                                                Event ev = new Event(e.get_id(), summary, description, location, visibility, freq, format.format(new Date(dateStart)).substring(0, 2).toUpperCase(), dateStart, dateEnd, dateUntil, interval);
                                                Log.d("Event_EDIT", ev.toString());

                                                if (parentClassName.equals("MainActivity"))
                                                    ((MainActivity) parentContext).updateEvent(ev);
                                                else if (parentClassName.equals("ListEventsActivity"))
                                                    ((ListEventsActivity) parentContext).updateEvent(ev);

                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }

                                        } else
                                            // TODO: Better feedback
                                            Toast.makeText(parentContext, "Error during the form check.", Toast.LENGTH_SHORT).show();


                                    }
                                })
                        .setNegativeButton("CANCEL",
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
        });

        exportEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");

                intent.putExtra(CalendarContract.Events.TITLE, e.getSummary());
                intent.putExtra(CalendarContract.Events.DESCRIPTION, e.getDescription());
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, e.getLocation());

                if (e.getVisibility().equals("PRIVATE"))
                    if (e.getVisibility().equals("PRIVATE"))
                        intent.putExtra(CalendarContract.Events.VISIBLE, 0);

                intent.putExtra(CalendarContract.Events.DTSTART, e.getDateStart());
                intent.putExtra(CalendarContract.Events.DTEND, e.getDateEnd());
                String freq;
                switch (e.getFreq()) {
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

                intent.putExtra(CalendarContract.Events.RRULE, "FREQ=" + freq + ";INTERVAL=" + e.getInterval() + ";UNTIL=" + e.getUntil() + ";WKST=" + e.getWeekStart());
                parentContext.startActivity(intent);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

}
