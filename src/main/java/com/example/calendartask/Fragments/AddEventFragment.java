package com.example.calendartask.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.calendartask.R;
import com.example.calendartask.dialog.EventDialog;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;


public class AddEventFragment extends Fragment {



   static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private static com.google.api.services.calendar.Calendar mService = null;
    String userName;

    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR };
    static GoogleAccountCredential mCredential;


    /** IT WILL BE EMPTY FRAGMENT JUST TO HOLD THE DIALOG FOR ADDING NEW EVENT, NO USING FOR IT*/
    public AddEventFragment() {
        // Required empty public constructor
    }


    public AddEventFragment(String username) {
        this.userName = username;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        EventDialog eventDialog = new EventDialog();
        eventDialog.show(getFragmentManager(),"dialog");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        mCredential.setSelectedAccountName(userName);

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        return view;
    }

}

