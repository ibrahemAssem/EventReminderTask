package com.example.calendartask;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;

import com.example.calendartask.Fragments.AddEventFragment;
import com.example.calendartask.Fragments.EventsFragments;
import com.example.calendartask.Fragments.ProfileFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    static Fragment currFragment;
    static ProgressDialog mProgress;

    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR };

    static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    ///SOME CODES NEED FOR CALLING GOOGLE API
    private static final String PREF_ACCOUNT_NAME = "accountName";
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_AUTHORIZATION = 1001;
    static String userName;

    static GoogleAccountCredential mCredential;
    private static com.google.api.services.calendar.Calendar mService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = new ProgressDialog(this);

        BottomNavigationView bottomNavigationView =  findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottomNavigationEventsMenuId:
                                currFragment = new EventsFragments(userName);
                                loadFragment(currFragment);
                                break;
                            case R.id.bottomNavigationAddEventMenuId:
                                currFragment = new AddEventFragment(userName);
                                loadFragment(currFragment);
                                break;
                            case R.id.bottomNavigationProfileMenuId:
                                currFragment = new ProfileFragment();
                                loadFragment(currFragment);

                                break;

                        }
                        return true;
                    }
                });



        ////INITIALIZING MY CREDENTIALS
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());



        ///here i will handle some states
        getResultsFromApi();

        ///internet Connection
        if(isDeviceOnline()) {
            currFragment = new EventsFragments(userName);
            loadFragment(currFragment);
        }
        else
        {
            Toast.makeText(this, "No internet Connection, Please check your internet and restart the App", Toast.LENGTH_SHORT).show();
        }

    }


    ////Main function to Load my fragments
    private void loadFragment(Fragment currFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
       // transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.frame_container, currFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    private void getResultsFromApi() {

        ///to check the availability of GoogePlayServices
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();

            ////no user logged in
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
           // mOutputText.setText("No network connection available.");
        }
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, " \"This app requires Google Play Services. Please install \" +\n" +
                            "\"Google Play Services on your device and relaunch this app.\");", Toast.LENGTH_SHORT).show();

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)

    ////if user already logged in , name will be saved in shared prefrence, if not saved then its new user, it will redirect you to new intent google logging
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                userName = accountName;
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    ////this function will be called from dialog add event to add the new event input
     public  void createEventAsync(final String summary, final String location, final String des, final DateTime startDate, final DateTime endDate, final EventAttendee[]
            eventAttendees)
    {
        mProgress.setMessage("Creating Event...");
        mProgress.setCanceledOnTouchOutside(false);

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground (Void...voids){
                try {
                    AddEvent(summary, location, des, startDate, endDate, eventAttendees);
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute (String s){
                super.onPostExecute(s);
                mProgress.dismiss();

                currFragment = new EventsFragments(userName);
                loadFragment(currFragment);

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.show();
            }
        }.execute();
    }


    private static void AddEvent(String summary, String location, String des, DateTime startDate, DateTime endDate, EventAttendee[] eventAttendees) throws IOException {
        Event event = new Event()
                .setSummary(summary)
                .setLocation(location)
                .setDescription(des);



        EventDateTime start = new EventDateTime()
                .setDateTime(startDate)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDate)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
        event.setRecurrence(Arrays.asList(recurrence));


        event.setAttendees(Arrays.asList(eventAttendees));

        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";


        if(mService!=null)
            event = mService.events().insert(calendarId, event).execute();

    }



    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        ///required but nothing to do here
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        ///required but nothing to do here
    }
}
