package com.example.nesh.mcc_calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Alberto Vaccari on 24-Oct-15.
 */
public class EventAdapter extends ArrayAdapter<Event> {

    Context parentContext;

    public EventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        parentContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

        // Setup Date Formatter
        DateFormat format = new SimpleDateFormat("HH:mm:ss");

        // Populate the data into the template view using the data object
        eventTv.setText(e.getSummary() + "\t|\t" + e.getLocation() + "\t|\t" +  format.format(e.getDateStart()));

        deleteEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if (parentContext instanceof MainActivity) {
                                    ((MainActivity) parentContext).deleteEvent(e.get_id());
                                }
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

                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();

            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

}
