package com.example.nesh.mcc_calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

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
        ImageButton eventBtn = (ImageButton) convertView.findViewById(R.id.eventBtn);

        // Setup Date Formatter
        DateFormat format = new SimpleDateFormat("HH:mm:ss");

        // Populate the data into the template view using the data object
        eventTv.setText(e.getSummary() + "\t|\t" + e.getLocation() + "\t|\t" +  format.format(e.getDateStart()));

        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(parentContext instanceof MainActivity){
                    ((MainActivity)parentContext).deleteEvent(e.get_id());
                }

            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

}
