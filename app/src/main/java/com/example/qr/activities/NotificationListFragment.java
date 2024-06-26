package com.example.qr.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.qr.R;
import com.example.qr.models.Event;
import com.example.qr.models.EventArrayAdapter;
import com.example.qr.models.Notification;
import com.example.qr.models.NotificationArrayAdapter;
import com.example.qr.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationListFragment displays a list of notifications for a specific event.
 */
public class NotificationListFragment extends Fragment {
    private String eventID;
    private Event event;
    ArrayList<Notification> notificationsDataList;
    NotificationArrayAdapter notificationArrayAdapter;

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

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventID = (String) getArguments().getSerializable("event_key");
            event = (Event) getArguments().getSerializable("event");

        }
        View view = inflater.inflate(R.layout.notification_list_view, container, false);

        ListView listView = view.findViewById(R.id.listview_notification);
//        Button btnClose = view.findViewById(R.id.btn_close_event_list);


        Button btnClose = view.findViewById(R.id.btn_close_event_list);
        // Close button going back previous screen
        btnClose.setOnClickListener(v -> {
            // Check if fragment is added to an activity and if activity has a FragmentManager
            if (isAdded() && getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        notificationsDataList = new ArrayList<>();
        notificationArrayAdapter = new NotificationArrayAdapter(getContext(), notificationsDataList);
        listView.setAdapter(notificationArrayAdapter);
        fetchData();
        TextView eventTitle = view.findViewById(R.id.event_title_notification);
        eventTitle.setText(event.getTitle());


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

/**
 *
 * On item click
 *
 * @param adapterView  the adapter view.
 * @param view  the view.
 * @param position  the position.
 * @param id  the id.
 */
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                listView.setItemChecked(position, true);
                Notification clickedNotification = (Notification) adapterView.getAdapter().getItem(position);
                Boolean readStatus = clickedNotification.getReadStatus();
                if (readStatus == null) {
                    readStatus = Boolean.FALSE; // Set a default value if null
                }
                clickedNotification.setReadStatus(!readStatus);
                notificationArrayAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }


    /**
     *
     * Fetch data
     *
     */
    private void fetchData() {

        String pathToNotifications = "Events/" + eventID + "/Notifications";
        FirebaseUtil.fetchCollection(pathToNotifications, Notification.class, new FirebaseUtil.OnCollectionFetchedListener<Notification>() {
            @Override

/**
 *
 * On collection fetched
 *
 * @param notificationList  the notification list.
 */
            public void onCollectionFetched(List<Notification> notificationList) {

                notificationsDataList.addAll(notificationList);
                notificationArrayAdapter.notifyDataSetChanged();   // Update notification array adapter
                Log.d("NotificationFragment", "Fetched " + notificationList.size() + " notifications for eventID: " + eventID);
            }
            @Override

/**
 *
 * On error
 *
 * @param e  the e.
 */
            public void onError(Exception e) {

                Log.e("NotificationFragment", "Error fetching notifications for eventID: " + eventID, e);
            }
        });
    }



}
