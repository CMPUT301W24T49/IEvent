package com.example.qr.activities;

import static android.content.ContentValues.TAG;
import static com.example.qr.activities.MainActivity.androidId;
import static com.example.qr.utils.FirebaseUtil.uploadImageAndGetUrl;
import static com.example.qr.utils.GenericUtils.getLocationFromAddress;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.qr.R;
import com.example.qr.models.Event;
import com.example.qr.utils.FirebaseUtil;
import com.example.qr.utils.ImagePickerUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import com.google.android.libraries.places.api.Places;


import android.widget.ArrayAdapter;

import com.android.volley.RequestQueue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * OrganizerCreateEventFragment is a fragment that allows organizers to create a new event.
 */
public class OrganizerCreateEventFragment extends Fragment {
    private ImageView eventPoster;
    private EditText eventTitle, startDate, endDate, startTime, endTime, maxAttendees, description;
    private AutoCompleteTextView eventLocation;
    private Button btnUseExistingQr, btnGenerateQr, btnCancel;
    private Integer maxAttendeesValue;
    private ImagePickerUtil image;
    private CollectionReference eventsRef;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private RequestQueue requestQueue;
    private ArrayAdapter<String> placesAdapter;
    private PlacesClient placesClient;


    /**
     *
     * Organizer create event fragment
     *
     * @return public
     */
    public OrganizerCreateEventFragment() {

        // Required empty public constructor
    }

    @Override

/**
 *
 * On create
 *
 * @param savedInstanceState  the saved instance state.
 */
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // citation: OpenAI, ChatGPT 4, 2024: How to use google maps places API  to autofill a textbox
        // showing a list of potential locations based on the text in the textbox android studio
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), "AIzaSyAgri4iQIBsBRP3ZWB9slBTckBpw0kEmVk");
        }
        placesClient = Places.createClient(getContext());
        placesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        // end citation

        // Initialize the ActivityResultLauncher for photo picking
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the photo picker
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                ImageView profileImageView = requireView().findViewById(R.id.ivEventPoster);
                profileImageView.setImageURI(uri);
                profileImageView.setTag(uri);

            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

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


        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        super.onCreate(savedInstanceState);

        // Initialize views
        eventPoster = view.findViewById(R.id.ivEventPoster);
        eventTitle = view.findViewById(R.id.eventTitle);
        eventLocation = view.findViewById(R.id.eventLocation);
        startDate = view.findViewById(R.id.startDate);
        endDate = view.findViewById(R.id.endDate);
        startTime = view.findViewById(R.id.startTime);
        endTime = view.findViewById(R.id.endTime);
        maxAttendees = view.findViewById(R.id.maxAttendees);
        description = view.findViewById(R.id.eventDescription);

        btnUseExistingQr = view.findViewById(R.id.btnUseExistingQr);
        btnGenerateQr = view.findViewById(R.id.btnGenerateQr);
        btnCancel = view.findViewById(R.id.btnCancel);

        setUpDateTimePicker(startDate, dateFormatter);
        setUpDateTimePicker(endDate, dateFormatter);
        setUpDateTimePicker(startTime, timeFormatter);
        setUpDateTimePicker(endTime, timeFormatter);

        eventLocation.setAdapter(placesAdapter);

        eventPoster.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest());
        });

        setupAutocompleteTextView();

        // buttons
        btnUseExistingQr.setOnClickListener(v -> {
            if (eventTitle.getText().toString().isEmpty() || eventLocation.getText().toString().isEmpty()
                    || startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()
                    || startTime.getText().toString().isEmpty() || endTime.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please fill empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // convert location to string
            String locationString = eventLocation.getText().toString();
            // if location is  valid location and string
            checkLocationValidity(locationString, isValid -> {
                if (isValid) {
                    // if valid get the location
                    LatLng location = getLocation();
                    if (location != null) {
                        // if location exists
                        long currentTimeMillis = System.currentTimeMillis();
                        String eventId = Long.toString(currentTimeMillis);
                        String eventQr = "qr" + Long.toString(currentTimeMillis);
                        String eventPromo = "qrp" + Long.toString(currentTimeMillis);
                        // value setting for maxAttendees blank or not
                        if(maxAttendees.getText().toString().isEmpty()) {
                            maxAttendeesValue = 9999;
                        } else {
                            maxAttendeesValue = Integer.parseInt(maxAttendees.getText().toString());
                        }

                        Event event = new Event(eventId, eventTitle.getText().toString(), description.getText().toString(), androidId,
                                new Date(startDate.getText().toString()), new Date(endDate.getText().toString()),
                                startTime.getText().toString(), endTime.getText().toString(), new GeoPoint(location.getLatitude(),
                                location.getLongitude()), eventQr, eventPromo, "", maxAttendeesValue);

                        if(eventPoster.getTag() != null) {
                            Bundle args = new Bundle();
                            args.putSerializable("Event", event);
                            args.putParcelable("ImageUri", (Parcelable) eventPoster.getTag());
                            OrganizerReuseQrCodeFragment organizerReuseQrCodeFragment = new OrganizerReuseQrCodeFragment();
                            organizerReuseQrCodeFragment.setArguments(args);
                            // if item in buffer then do transaction to next screen
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, organizerReuseQrCodeFragment)
                                        .addToBackStack(null)  // Optional: Add transaction to back stack
                                        .commit();
                            }

                        }else {
                            Bundle args = new Bundle();
                            args.putSerializable("Event", event);
                            OrganizerReuseQrCodeFragment organizerReuseQrCodeFragment = new OrganizerReuseQrCodeFragment();
                            organizerReuseQrCodeFragment.setArguments(args);
                            // if item in buffer then do transaction to next screen
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, organizerReuseQrCodeFragment)
                                        .addToBackStack(null)  // Optional: Add transaction to back stack
                                        .commit();
                            }
                        }


                    } else {
                        Toast.makeText(getContext(), "Invalid location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid location", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnGenerateQr.setOnClickListener(v -> {
            // Check if textviews are empty
            if (eventTitle.getText().toString().isEmpty() || eventLocation.getText().toString().isEmpty()
                    || startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()
                    || startTime.getText().toString().isEmpty() || endTime.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please fill empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // convert location to string
            String locationString = eventLocation.getText().toString();
            // if location is  valid location and string
            checkLocationValidity(locationString, isValid -> {
                if (isValid) {
                    // if valid get the location
                    LatLng location = getLocation();
                    if (location != null) {
                        // if location exists
                        long currentTimeMillis = System.currentTimeMillis();
                        String eventId = Long.toString(currentTimeMillis);
                        String eventQr = "qr" + Long.toString(currentTimeMillis);
                        String eventPromo = "qrp" + Long.toString(currentTimeMillis);
                        // value setting for maxAttendees blank or not
                        if(maxAttendees.getText().toString().isEmpty()) {
                            maxAttendeesValue = 9999;
                        } else {
                            maxAttendeesValue = Integer.parseInt(maxAttendees.getText().toString());
                        }

                        Event event = new Event(eventId, eventTitle.getText().toString(), description.getText().toString(), androidId,
                                new Date(startDate.getText().toString()), new Date(endDate.getText().toString()),
                                startTime.getText().toString(), endTime.getText().toString(), new GeoPoint(location.getLatitude(),
                                location.getLongitude()), eventQr, eventPromo, "", maxAttendeesValue);

                        if(eventPoster.getTag() != null) {
                            Uri profilePicture = (Uri) eventPoster.getTag();
                            uploadImageAndGetUrl(profilePicture, UUID.randomUUID().toString(), downloadUrl -> {
                                Log.d("CreateEvent", "Uploaded image: " + downloadUrl.toString());
                                event.setEventPoster(downloadUrl.toString());
                                FirebaseUtil.addEvent(event,
                                        aVoid -> {
                                            // GPT given code to switch back to organizer screen
                                            switchToOrganizerFragment();
                                            Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                        }, e -> {
                                            // else toast that it failed to create
                                            Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                                        });

                            }, e -> {
                                Log.e("CreateEvent", "Failed to upload image", e);
                                FirebaseUtil.addEvent(event,
                                        aVoid -> {
                                            // GPT given code to switch back to organizer screen
                                            switchToOrganizerFragment();
                                            Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                        }, ee -> {
                                            // else toast that it failed to create
                                            Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                                        });
                            });
                        }else {
                            FirebaseUtil.addEvent(event,
                                    aVoid -> {
                                        // GPT given code to switch back to organizer screen
                                        switchToOrganizerFragment();
                                        Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                    }, ee -> {
                                        // else toast that it failed to create
                                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                                    });
                        }


                    } else {
                        Toast.makeText(getContext(), "Invalid location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid location", Toast.LENGTH_SHORT).show();
                }
            });
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
                OrganizerFragment organizerFragment = new OrganizerFragment();

                // Perform the fragment transaction
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager(); // Use getFragmentManager() in a Fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Replace the current fragment with OrganizerFragment. Assume R.id.fragment_container is the ID of your FrameLayout
                fragmentTransaction.replace(R.id.fragment_container, organizerFragment);
                // fragmentTransaction.addToBackStack(null); // Optional: Add this transaction to the back stack
                fragmentTransaction.commit(); // Commit the transaction
            }
        });
        // end citation

        return view;
    }

    // citation: OpenAI, ChatGPT 4, 2024: How to use google maps places API  to autofill a textbox
    // showing a list of potential locations based on the text in the textbox android studio

    /**
     *
     * Setup autocomplete text view
     *
     */
    private void setupAutocompleteTextView() {

        eventLocation.addTextChangedListener(new TextWatcher() {
            @Override

/**
 *
 * Before text changed
 *
 * @param s  the s.
 * @param start  the start.
 * @param count  the count.
 * @param after  the after.
 */
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override

/**
 *
 * On text changed
 *
 * @param s  the s.
 * @param start  the start.
 * @param before  the before.
 * @param count  the count.
 */
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                updateAutocompletePredictions(s.toString());
            }

            @Override

/**
 *
 * After text changed
 *
 * @param s  the s.
 */
            public void afterTextChanged(Editable s) {

            }
        });
    }
    // end citation


    /**
     *
     * Update autocomplete predictions
     *
     * @param query  the query.
     */
    private void updateAutocompletePredictions(String query) {

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getFullText(null).toString());
            }
            placesAdapter.clear();
            placesAdapter.addAll(suggestions);
            placesAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting autocomplete predictions", e));
    }
    // end citation

    // Utility method to set up date and time pickers

    /**
     *
     * Sets the up date time picker
     *
     * @param editText  the edit text.  It is final
     * @param formatter  the formatter.  It is final
     */
    private void setUpDateTimePicker(final EditText editText, final SimpleDateFormat formatter) {

        final Calendar calendar = Calendar.getInstance();
        editText.setOnClickListener(v -> {
            if (formatter == dateFormatter) { // Date picker
                new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(formatter.format(calendar.getTime()));
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            } else { // Time picker
                new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    editText.setText(formatter.format(calendar.getTime()));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    // citation: OpenAI, ChatGPT 4, 2024: how do I check if a user input is a valid location
    // in android studio

    /**
     *
     * Check location validity
     *
     * @param locationString  the location string.
     * @param callback  the callback.
     */
    private void checkLocationValidity(String locationString, Consumer<Boolean> callback) {

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(getContext());
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationString, 1);
                boolean isValid = addresses != null && !addresses.isEmpty();
                getActivity().runOnUiThread(() -> callback.accept(isValid));
            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> callback.accept(false));
            }
        }).start();
    }


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
    // end of citation

    /**
     *
     * Gets the location
     *
     * @return the location
     */
    private LatLng getLocation() {

        return getLocationFromAddress( getActivity()  ,eventLocation.getText().toString());

    }
}
