package com.example.calendartask.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.FragmentManager;


import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.calendartask.Adapters.EventListAdapter;
import com.example.calendartask.MainActivity;
import com.example.calendartask.Models.EventModel;
import com.example.calendartask.Models.WeatherModel;
import com.example.calendartask.R;
import com.example.calendartask.dialog.EventDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class EventsFragments extends Fragment implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR };

    String userName = "";
    ProgressDialog mProgress;
    ListView eventListView;
    private List<EventModel> scheduledEventsList = new ArrayList<EventModel>();
    private EventListAdapter eventListAdapter;


    static final int REQUEST_AUTHORIZATION = 1001;

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    public EventsFragments() {
        // Required empty public constructor
    }
    ////costumed constructor to recieve the username whenever i want to use this fragment , so i can retrieve all the related event.
    public EventsFragments(String username)
    {
        this.userName = username;
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgress = new ProgressDialog(this.getContext());


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events_fragments, container, false);
        eventListView = (ListView)  view.findViewById(R.id.eventList);


        mProgress.setMessage("Syncing with calendar..");
        mProgress.setCanceledOnTouchOutside(false);


        ////this handler will call the method to retrive all the events each 30 sec.
        final Handler handler = new Handler();
        final int delay = 30*1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){

                new MakeRequestTask(mCredential).execute();
                handler.postDelayed(this, delay);
            }
        }, delay);

        mCredential.setSelectedAccountName(userName);

        new MakeRequestTask(mCredential).execute();

        return view;

    }



    ////just to handle any back or exit button whenever fragment oppened again.
    @Override
    public void onStart() {
        super.onStart();
        mProgress.setMessage("Syncing with calendar..");
        mProgress.setCanceledOnTouchOutside(false);


        mCredential.setSelectedAccountName(userName);

        new MakeRequestTask(mCredential).execute();
    }



    private com.google.api.services.calendar.Calendar mService = null;
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private Exception mLastError = null;
        private boolean FLAG = false;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                getDataFromApi();
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private void getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();

            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            EventModel scheduledEvents;
            scheduledEventsList.clear();
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                DateTime end = event.getEnd().getDateTime();
                if (end == null) {
                    end = event.getEnd().getDate();
                }
                scheduledEvents = new EventModel();
                scheduledEvents.setOwnerName(mCredential.getSelectedAccountName());

                scheduledEvents.setEventId(event.getId());
                scheduledEvents.setDescription(event.getDescription());
                scheduledEvents.setEventSummery(event.getSummary());
                scheduledEvents.setLocation(event.getLocation());
                scheduledEvents.setStartDate(start.toString());
                scheduledEvents.setEndDate(end.toString());

                StringBuffer stringBuffer = new StringBuffer();
                if(event.getAttendees()!=null) {
                    for (EventAttendee eventAttendee : event.getAttendees()) {
                        if(eventAttendee.getEmail()!=null)
                            stringBuffer.append(eventAttendee.getEmail() + "  ,  ");
                    }
                    scheduledEvents.setAttendees(stringBuffer.toString());
                }
                else{
                    scheduledEvents.setAttendees("");
                }
                scheduledEventsList.add(scheduledEvents);

                System.out.println("-----"+event.getDescription()+", "+event.getId()+", "+event.getLocation());
                System.out.println(event.getAttendees());
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {

          // mProgress.hide();
            System.out.println("--------------------"+scheduledEventsList.size());
            if (scheduledEventsList.size()<=0) {
                Toast.makeText(getContext(), "You have no Up-coming events", Toast.LENGTH_SHORT).show();
            } else {
                mProgress.hide();
                eventListAdapter = new EventListAdapter(getContext(), scheduledEventsList);
                eventListView.setAdapter(eventListAdapter);

            }
        }

        @Override
        protected void onCancelled() {

           mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                      //      + mLastError.getMessage());
                    Toast.makeText(getContext(), mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }



    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }





}
