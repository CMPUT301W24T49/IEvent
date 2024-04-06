package com.example.qr.activities;

import static com.example.qr.activities.MainActivity.androidId;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.qr.R;
import com.example.qr.models.Event;
import com.example.qr.models.EventArrayAdapter;
import com.example.qr.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventListFragment extends Fragment {
    
    ArrayList<Event> eventDataList;
    EventArrayAdapter eventArrayAdapter;


    public OrganizerEventListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        
        // Button initialization
        ListView listView = view.findViewById(R.id.listview_events);
        Button btnClose = view.findViewById(R.id.close_btn);
        
        eventDataList = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(getContext(), eventDataList);
        listView.setAdapter(eventArrayAdapter);

        fetchData();

        // Event list onclick directs to AttendeeListFragment
        listView.setOnItemClickListener((adapterView, view1, position, rowId) -> {

            // Get position and Id of event clicked
            Event event = eventDataList.get(position);

            // Send eventId to AttendeeListFragment
            // Adapted from answer given by João Marcos
            // https://stackoverflow.com/questions/24555417/how-to-send-data-from-one-fragment-to-another-fragment
            Bundle args = new Bundle();
            args.putSerializable("Event", event);

            OrganizerEventDetailFragment organizerEventDetailFragment = new OrganizerEventDetailFragment();
            organizerEventDetailFragment.setArguments(args); // Pass data to organizerEventDetailFragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, organizerEventDetailFragment)
                        .addToBackStack(null)  // Optional: Add transaction to back stack
                        .commit();
            }
        });
        
        // Close button going back previous screen
        btnClose.setOnClickListener(v -> {
            // Check if fragment is added to an activity and if activity has a FragmentManager
            if (isAdded() && getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    // Fetch events from Firebase and add them to eventList
    private void fetchData() {
        // Citation: OpenAI, ChatGPT 4, 2024
        // Prompt: How would i use fetchCollection to fetch event data?
        FirebaseUtil.fetchCollection("Events", Event.class, new FirebaseUtil.OnCollectionFetchedListener<Event>() {
            @Override
            public void onCollectionFetched(List<Event> eventList) {
                // Handle the fetched events

                //only add the events which have the same organizerid
                for(Event event : eventList){
                    if(event.getOrganizerId().equals(androidId)){
                        eventDataList.add(event);
                    }
                }

                eventArrayAdapter.notifyDataSetChanged();   // Update event array adapter
                Log.d("EventListFragment", "Fetched " + eventList.size() + " events");
            }

            @Override
            public void onError(Exception e) {
            }
            // End of citation

        });
    }
}
