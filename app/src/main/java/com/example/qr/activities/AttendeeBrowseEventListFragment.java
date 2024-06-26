package com.example.qr.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.qr.R;
import com.example.qr.models.Event;
import com.example.qr.models.EventArrayAdapter;
import com.example.qr.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * AttendeeBrowseEventListFragment displays a list of events that an attendee can browse and sign up for.
 */
public class AttendeeBrowseEventListFragment extends Fragment {

    ArrayList<Event> eventDataList;
    EventArrayAdapter eventArrayAdapter;

    RelativeLayout fragmentLayout;

    public AttendeeBrowseEventListFragment() {

        // Required empty public constructor
    }

    @Override

/**
 *
 * On create view
 *
 * @param inflater  the inflater.
 * @param container  the container.
 * @param savedInstanceState  the saved instance state.
 * @return View
 */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        fragmentLayout = view.findViewById(R.id.fragment_event_list_layout);

        // Button initialization
        ListView listView = view.findViewById(R.id.listview_events);
        Button btnClose = view.findViewById(R.id.btn_close_event_list);

        eventDataList = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(getContext(), eventDataList);
        listView.setAdapter(eventArrayAdapter);

        fetchData();

        // Event list onclick directs to AttendeeListFragment
        listView.setOnItemClickListener((adapterView, view1, position, rowId) -> {

            // Get position and Id of event clicked
            Event event = eventDataList.get(position);

            // Send eventId to AttendeeListFragment
            // Adapted from answer given by JoÃ£o Marcos
            // https://stackoverflow.com/questions/24555417/how-to-send-data-from-one-fragment-to-another-fragment
            Bundle args = new Bundle();
            args.putSerializable("Event", event);
            args.putSerializable("NoSignUp", false);

            AttendeeEventDetailFragment attendeeEventDetailFragment = new AttendeeEventDetailFragment();
            attendeeEventDetailFragment.setArguments(args); // Pass data to attendeeListFragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, attendeeEventDetailFragment)
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

        fragmentLayout.setVisibility(View.GONE);
        return view;
    }

    // Fetch events from Firebase and add them to eventList

    /**
     *
     * Fetch data
     *
     */
    private void fetchData() {

        // Citation: OpenAI, ChatGPT 4, 2024
        // Prompt: How would i use fetchCollection to fetch event data?
        FirebaseUtil.fetchCollection("Events", Event.class, new FirebaseUtil.OnCollectionFetchedListener<Event>() {
            @Override

/**
 *
 * On collection fetched
 *
 * @param eventList  the event list.
 */
            public void onCollectionFetched(List<Event> eventList) {

                // Handle the fetched events

                eventDataList.addAll(eventList);   // Add events to data list

                eventArrayAdapter.notifyDataSetChanged(); // Update event array adapter

                fragmentLayout.setVisibility(View.VISIBLE);
                Log.d("EventListFragment", "Fetched " + eventList.size() + " events");
            }

            @Override

/**
 *
 * On error
 *
 * @param e  the e.
 */
            public void onError(Exception e) {

            }
            // End of citation

        });
    }
}
