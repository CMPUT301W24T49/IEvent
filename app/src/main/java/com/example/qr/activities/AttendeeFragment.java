package com.example.qr.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.qr.R;

public class AttendeeFragment extends Fragment {

    public AttendeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_attendee, container, false);

        Button btnClose = view.findViewById(R.id.btn_close);
        Button btnMyEvents = view.findViewById(R.id.btnMyEvents);
        Button btnBrowseEvents = view.findViewById(R.id.btnBrowseEvents);
        Button btnSettings = view.findViewById(R.id.btnSettings);

        btnBrowseEvents.setOnClickListener(v -> {
            AttendeeBrowseEventListFragment eventListFragment = new AttendeeBrowseEventListFragment();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventListFragment)
                        .addToBackStack(null)  // Optional: Add transaction to back stack
                        .commit();
            }
        });

        btnMyEvents.setOnClickListener(v -> {
            AttendeeMyEventListFragment myEventListFragment = new AttendeeMyEventListFragment();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myEventListFragment)
                        .addToBackStack(null)  // Optional: Add transaction to back stack
                        .commit();
            }
        });

        btnSettings.setOnClickListener(v -> {
            AttendeeSettingsFragment attendeeSettingsFragment = new AttendeeSettingsFragment();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, attendeeSettingsFragment)
                        .addToBackStack(null)  // Optional: Add transaction to back stack
                        .commit();
            }
        });

        btnClose.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                getActivity().onBackPressed();
            }
        });


        // Inflate the layout for this fragment
        return view;
    }
}
