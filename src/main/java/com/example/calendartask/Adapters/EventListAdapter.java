package com.example.calendartask.Adapters;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendartask.DetailedEventActivity;

import com.example.calendartask.Models.WeatherModel;
import com.example.calendartask.R;
import com.example.calendartask.Models.EventModel;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.io.IOException;
import java.util.Arrays;

import java.util.List;


public class EventListAdapter extends BaseAdapter {

    private Context context;
    private List<EventModel> scheduledEvents;
    private LayoutInflater inflater;
    private HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private static Boolean Deleted =false;
    ProgressDialog mProgress;

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR };


    public EventListAdapter(Context context, List<EventModel> scheduledEvents){
        this.context = context;
        this.scheduledEvents = scheduledEvents;
        inflater = LayoutInflater.from(this.context);
        mProgress = new ProgressDialog(this.context);
    }

    @Override
    public int getCount() {
        return scheduledEvents.size();
    }

    @Override
    public Object getItem(int i) {
        return scheduledEvents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final EventHolder eventHolder;


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.event_view_llayout, parent, false);
            eventHolder = new EventHolder(convertView);
            convertView.setTag(eventHolder);
        } else {
            eventHolder = (EventHolder) convertView.getTag();
        }


        final EventModel scheduledEvents = (EventModel) getItem(position);
        eventHolder.eventTitle.setText(scheduledEvents.getEventSummery());
        eventHolder.eventDes.setText(scheduledEvents.getDescription());
        eventHolder.eventAttendee.setText(scheduledEvents.getAttendees());
        eventHolder.eventStart.setText(scheduledEvents.getStartDate());
        eventHolder.eventEnd.setText(scheduledEvents.getEndDate());
        eventHolder.eventLocation.setText(scheduledEvents.getLocation());

        eventHolder.RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mCredential = GoogleAccountCredential.usingOAuth2(
                        EventListAdapter.this.context, Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());

                mCredential.setSelectedAccountName(scheduledEvents.getOwnerName());

                Calendar service = new Calendar.Builder(transport, jsonFactory, mCredential)
                        .setApplicationName("applicationName").build();

                    createEventAsync(scheduledEvents.getEventId(),service);
                    Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                    if (Deleted) {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    }



            }
        });


        /////to start the activity for one event clicked
        eventHolder.viewEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openDetailedEvent = new Intent(context, DetailedEventActivity.class);
                openDetailedEvent.putExtra("Title",eventHolder.eventTitle.getText().toString());
                openDetailedEvent.putExtra("Des",eventHolder.eventDes.getText().toString());
                openDetailedEvent.putExtra("Attendees",eventHolder.eventAttendee.getText().toString());
                openDetailedEvent.putExtra("StartTime",eventHolder.eventStart.getText().toString());
                openDetailedEvent.putExtra("EndTime",eventHolder.eventEnd.getText().toString());
                openDetailedEvent.putExtra("Location",eventHolder.eventLocation.getText().toString());
                context.startActivity(openDetailedEvent);

            }
        });




        return convertView;
    }




     public  void createEventAsync (final String eventId, final Calendar service)
    {

        new AsyncTask<Void, Void, String>() {

            private Exception mLastError = null;
            private boolean FLAG = false;



            @Override
            protected String doInBackground (Void...voids){
                try {
                    mProgress.setMessage("Removing the event..");
                    mProgress.setCanceledOnTouchOutside(false);
                    service.events().delete("primary", eventId).execute();
                    context.sendBroadcast(new Intent("start.fragment.action"));
                    Deleted = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute (String s){
                super.onPostExecute(s);
                mProgress.hide();
            }

            @Override
            protected void onPreExecute() {
                //mOutputText.setText("");
                mProgress.show();
            }

        }.execute();


    }

    private class EventHolder {
        TextView eventTitle, eventDes, eventAttendee, eventStart, eventEnd, eventLocation;
        ImageView RemoveBtn,viewEventImage,acceptEventBtn,RejectEventBtn;

        public EventHolder(View item) {
            eventTitle = (TextView) item.findViewById(R.id.eventTitle);
            eventDes = (TextView) item.findViewById(R.id.eventDes);
            eventAttendee = (TextView) item.findViewById(R.id.eventAttendee);
            eventStart = (TextView) item.findViewById(R.id.eventStart);
            eventEnd = (TextView) item.findViewById(R.id.eventEnd);
            eventLocation = (TextView) item.findViewById(R.id.eventLocation);
            RemoveBtn = (ImageView) item.findViewById(R.id.RemoveEvent);
            viewEventImage = (ImageView) item.findViewById(R.id.ViewEventDetailsBtnID);
            acceptEventBtn = (ImageView) item.findViewById(R.id.AcceptEventBtn);
            RejectEventBtn = (ImageView) item.findViewById(R.id.RejectEventBtn);

        }
    }
}
