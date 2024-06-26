package com.example.qr.models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qr.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * An ArrayAdapter for displaying event objects.
 * Converts an ArrayList of Event objects into View items loaded into the ListView container. 
 * Each item in the list represents an individual event.
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;    // List of events
    private Context context;    // Current context

    /**
     * Constructs a new EventArrayAdapter.
     *
     * @param context The current context which is used to inflate the layout file.
     * @param events An ArrayList of Event objects to display in the list.
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }
    
    /**
     * Provides a view for an AdapterView.
     *
     * @param position The position in the list of data that should be displayed in the list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_event_list_view_info, parent, false);
        }

        Event event = events.get(position);
//        TextView eventName = view.findViewById(R.id.event_list_info);
//        eventName.setText(event.getTitle());
        TextView tvEventId = view.findViewById(R.id.tvEventId);
        TextView tvEventName = view.findViewById(R.id.tvEventName);

        tvEventId.setText(event.getId());
        tvEventName.setText(event.getTitle());

        return view;
    }
}
