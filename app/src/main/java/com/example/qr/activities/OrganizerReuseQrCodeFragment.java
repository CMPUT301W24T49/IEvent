package com.example.qr.activities;

import static com.example.qr.activities.MainActivity.androidId;
import static com.example.qr.utils.FirebaseUtil.uploadImageAndGetUrl;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.qr.R;
import com.example.qr.models.Event;
import com.example.qr.models.EventSpinnerAdapter;
import com.example.qr.models.Notification;
import com.example.qr.models.SharedViewModel;
import com.example.qr.utils.FirebaseUtil;
import com.example.qr.utils.GenerateQRCode;
import com.google.firebase.firestore.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;


/**
 * The class Organizer reuse qr code fragment extends fragment
 */
public class OrganizerReuseQrCodeFragment extends Fragment {

    private Button btnCancel, chooseQrCode;
    private Spinner eventSpinner;

    private ImageView eventQrCode;
    private ArrayList<Event> eventDataList;

    private ArrayAdapter<Event> eventAdapter;

    private Event selectedEvent;
    private Uri imageUri;

    private String qrCode;
    private String qrpCode;
    private String oldID;
    private Integer prevMaxAttendees;

    private Date prevStartDate, prevEndDate;


    /**
     *
     * Organizer reuse qr code fragment
     *
     * @return public
     */
    public OrganizerReuseQrCodeFragment() {

        // Required empty public constructor
    }


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


        View view = inflater.inflate(R.layout.fragment_reuse_qr, container, false);
        super.onCreate(savedInstanceState);

        btnCancel = view.findViewById(R.id.btnCancel);

        eventSpinner = view.findViewById(R.id.eventSpinner);

        eventQrCode = view.findViewById(R.id.ivEventPoster);

        chooseQrCode = view.findViewById(R.id.chooseQrCodeButton);

        eventDataList = new ArrayList<>();

        //get event object and uri if it exists

        selectedEvent = (Event) getArguments().getSerializable("Event");

        if(getArguments().getParcelable("ImageUri") != null){
            imageUri = (Uri) getArguments().getParcelable("ImageUri");
        }

        // Setup ArrayAdapter using the default spinner layout and your events list
        eventAdapter = new EventSpinnerAdapter(getContext(), eventDataList);
        // Apply the adapter to your Spinner
        eventSpinner.setAdapter(eventAdapter);

        fetchData();

        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

/**
 *
 * On item selected
 *
 * @param parent  the parent.
 * @param view  the view.
 * @param position  the position.
 * @param id  the id.
 */
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Check if the placeholder "Select one:" is selected
                if(position == 0) {
                    // Placeholder selected, do nothing or reset the QR code image
                    eventQrCode.setImageResource(android.R.color.transparent); // Hide QR code
                    return;
                }

                // Your existing logic for when a real event is selected

                qrCode = eventDataList.get(position).getQrCode();
                qrpCode = eventDataList.get(position).getQrpCode();
                oldID = eventDataList.get(position).getId();
                if(qrCode != null) {
                    Bitmap img = GenerateQRCode.generateQR(qrCode);
                    eventQrCode.setImageBitmap(img);
                }
            }

            @Override

/**
 *
 * On nothing selected
 *
 * @param parent  the parent.
 */
            public void onNothingSelected(AdapterView<?> parent) {

                // Do nothing
            }
        });

        // citation: OpenAI, ChatGPT 4, 2024: How do I click a button to change fragments
        // in android studio
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override

/**
 *
 * On click
 *
 * @param v  the v.
 */
            public void onClick(View v) {

                // Create a new instance of OrganizerFragment
                OrganizerCreateEventFragment organizerCreateEventFragment = new OrganizerCreateEventFragment();

                // Perform the fragment transaction
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.fragment_container, organizerCreateEventFragment);

                fragmentTransaction.commit(); // Commit the transaction
            }
        });

        chooseQrCode.setOnClickListener(v -> {

            if(eventSpinner.getSelectedItemPosition() == 0){
                Toast.makeText(getContext(), "Please select an event", Toast.LENGTH_SHORT).show();
                return;
            }

            if(qrCode != null) {
                selectedEvent.setQrCode(qrCode);
            }
            if(qrpCode != null) {
                selectedEvent.setQrpCode(qrpCode);
            }


            if(imageUri != null) {
                uploadImageAndGetUrl(imageUri, UUID.randomUUID().toString(), downloadUrl -> {
                    Log.d("CreateEvent", "Uploaded image: " + downloadUrl.toString());
                    selectedEvent.setEventPoster(downloadUrl.toString());
                    FirebaseUtil.addEvent(selectedEvent,
                            aVoid -> {
                                // GPT given code to switch back to organizer screen
                                switchToOrganizerFragment();
                                Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                FirebaseUtil.deleteEvent(oldID, a -> { }, e -> {});
                            }, e -> {
                                // else toast that it failed to create
                                Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                            });

                }, e -> {
                    Log.e("CreateEvent", "Failed to upload image", e);
                    FirebaseUtil.addEvent(selectedEvent,
                            aVoid -> {
                                // GPT given code to switch back to organizer screen
                                switchToOrganizerFragment();
                                FirebaseUtil.getFCMTokenID(selectedEvent);
                                Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                FirebaseUtil.shareFCMToken((selectedEvent.getQrCode()).substring(2), selectedEvent.getId());
                                String message = selectedEvent.getTitle() + "'s details has been updated by organizer.";
                                Notification notification = new Notification("notification" + System.currentTimeMillis(), selectedEvent.getId(), message, new Date(), false);
                                FirebaseUtil.addNotification(notification, c -> {}, d -> {});

                                FirebaseUtil.deleteEvent(oldID, a -> { }, ee -> {});
                            }, ee -> {
                                // else toast that it failed to create
                                Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                            });
                });
            }else {
                FirebaseUtil.addEvent(selectedEvent,
                        aVoid -> {
                            // GPT given code to switch back to organizer screen
                            switchToOrganizerFragment();
                            Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUtil.getFCMTokenID(selectedEvent);
                            FirebaseUtil.shareFCMToken((selectedEvent.getQrCode()).substring(2), selectedEvent.getId());
                            String message = selectedEvent.getTitle() + "'s details has been updated by organizer.";
                            Notification notification = new Notification("notification" + System.currentTimeMillis(), selectedEvent.getId(), message, new Date(), false);
                            FirebaseUtil.addNotification(notification, c -> {}, e -> {});

                            FirebaseUtil.deleteEvent(oldID, a -> { }, e -> {});
                        }, ee -> {
                            // else toast that it failed to create
                            Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                        });
            }
            // DO NOT REMOVE THIS. ITS FOR PUSH NOTIFICATION

        });
        // end citation

        return view;
    }


    /**
     *
     * Fetch data
     *
     */
    private void fetchData() {

        Event placeholderEvent = new Event();
        placeholderEvent.setTitle("Select one:");
        placeholderEvent.setId("");
        eventDataList.add(placeholderEvent);

        FirebaseUtil.fetchCollection("Events", Event.class, new FirebaseUtil.OnCollectionFetchedListener<Event>() {
            @Override

/**
 *
 * On collection fetched
 *
 * @param eventList  the event list.
 */
            public void onCollectionFetched(List<Event> eventList) {

                Date now = new Date(); // Current date and time

                for (Event event : eventList) {
                    if (event.getOrganizerId().equals(androidId)) {
                        if (isEventBeforeCurrentTime(event.getEndDate(), event.getEndTime())) {
                            eventDataList.add(event);
                        }
                    }
                }

                eventAdapter.notifyDataSetChanged();
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

                // Handle any errors here
            }
        });
    }

    // citation: Open MP, GPT 4.0, April 2024: How do I check for current runtime date compared to
    // the endDate and endTime based on *examples from database*

    /**
     *
     * Is event before current time
     *
     * @param endDate  the end date.
     * @param endTime  the end time.
     * @return boolean
     */
    private boolean isEventBeforeCurrentTime(Date endDate, String endTime) {

        if (endDate == null || endTime == null) {
            return false; // Ensure that both date and time are not null.
        }

        // Current date and time setup.
        Calendar nowCal = Calendar.getInstance();
        Date now = nowCal.getTime();

        // Set up calendar with the event's end date.
        Calendar eventDateCal = Calendar.getInstance();
        eventDateCal.setTime(endDate);
        eventDateCal.set(Calendar.SECOND, 0);
        eventDateCal.set(Calendar.MILLISECOND, 0);

        // Remove time parts for day comparison.
        Calendar eventDateOnly = (Calendar) eventDateCal.clone();
        Calendar nowDateOnly = (Calendar) nowCal.clone();
        eventDateOnly.set(Calendar.HOUR_OF_DAY, 0);
        eventDateOnly.set(Calendar.MINUTE, 0);
        nowDateOnly.set(Calendar.HOUR_OF_DAY, 0);
        nowDateOnly.set(Calendar.MINUTE, 0);

        // Check if the event date is strictly before today's date.
        if (eventDateOnly.before(nowDateOnly)) {
            return true;
        }

        // Check time only if it's the same day.
        if (eventDateOnly.compareTo(nowDateOnly) == 0) {
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date endTimeParsed = sdfTime.parse(endTime);
                Calendar endTimeCal = Calendar.getInstance();
                endTimeCal.setTime(endTimeParsed);

                // Set time from endTime to eventDateCal for precise comparison.
                eventDateCal.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
                eventDateCal.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));

                // Now compare the full eventDateCal (with adjusted end time) to nowCal.
                return eventDateCal.before(nowCal);
            } catch (ParseException e) {
                Log.e("Event", "Error parsing time: " + endTime, e);
                return false;
            }
        }

        // If not before and not the same day, it's not past.
        return false;
    }
    // end citation



    /**
     *
     * Switch to organizer fragment
     *
     */
    private void switchToOrganizerFragment() {

        OrganizerFragment organizerFragment = new OrganizerFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, organizerFragment);
        fragmentTransaction.commit();
    }
}
