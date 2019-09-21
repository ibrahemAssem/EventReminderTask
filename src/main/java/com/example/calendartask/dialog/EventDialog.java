package com.example.calendartask.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import com.example.calendartask.MainActivity;
import com.example.calendartask.R;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventAttendee;
import java.util.Calendar;
import java.util.Date;


public class EventDialog extends DialogFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    private TimePicker startTime;
    private DatePicker startDate;
    private TimePicker endTime;
    private DatePicker endDate;
    final Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);
    private Button createEvent;
    private Button cancelEvent;
    private EditText eventTitle;
    private EditText eventDes;
    private EditText eventLocation;
    private EditText eventAttendee;
    private EventAttendee eventAttendeeEmail[];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);

        eventTitle = (EditText)view.findViewById(R.id.new_event_title);
        eventDes = (EditText)view.findViewById(R.id.new_event_description);
        eventLocation = (EditText)view.findViewById(R.id.new_event_location);
        eventAttendee = (EditText)view.findViewById(R.id.new_event_attendee);

        startDate = (DatePicker)view.findViewById(R.id.new_event_start_date);
        startTime = (TimePicker) view.findViewById(R.id.new_event_start_time);

        endTime = (TimePicker)view.findViewById(R.id.new_event_end_time);
        endDate = (DatePicker) view.findViewById(R.id.new_event_end_date);



        createEvent = (Button)view.findViewById(R.id.create_new_event);
        cancelEvent = (Button)view.findViewById(R.id.cancel_new_event);

        createEvent.setOnClickListener(this);
        cancelEvent.setOnClickListener(this);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancel_new_event:
                dismiss();
                break;
            case R.id.create_new_event:


                ///nothing to explain here, just adding the data to new object.
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.set(Calendar.DAY_OF_MONTH, startDate.getDayOfMonth());
                startCalendar.set(Calendar.MONTH, startDate.getMonth());
                startCalendar.set(Calendar.YEAR, startDate.getYear());
                startCalendar.set(Calendar.HOUR_OF_DAY, startTime.getCurrentMinute());
                startCalendar.set(Calendar.MINUTE, startTime.getCurrentMinute());
                Date startDate = startCalendar.getTime();

                DateTime start = new DateTime(startDate);


                Calendar endCalendar = Calendar.getInstance();
                endCalendar.set(Calendar.DAY_OF_MONTH, endDate.getDayOfMonth());
                endCalendar.set(Calendar.MONTH, endDate.getMonth());
                endCalendar.set(Calendar.YEAR, endDate.getYear());
                endCalendar.set(Calendar.HOUR_OF_DAY, endTime.getCurrentMinute());
                endCalendar.set(Calendar.MINUTE, endTime.getCurrentMinute());
                Date endDate = endCalendar.getTime();
                DateTime end = new DateTime(endDate);

                if(!eventAttendee.getText().toString().equalsIgnoreCase("")) {
                    String email[] = eventAttendee.getText().toString().trim().split(",");
                    eventAttendeeEmail = new EventAttendee[email.length];
                    int i = 0;
                    for (String s : email) {
                        EventAttendee eventAttendee = new EventAttendee();
                        eventAttendee.setEmail(s);
                        eventAttendeeEmail[i] = eventAttendee;
                        i++;
                    }
                }

                StringBuffer buffer = new StringBuffer(eventTitle.getText().toString()+"\n");
                buffer.append("\n");
                buffer.append(eventDes.getText().toString());

                ///here to call the function from main activity to add this event, didnt work from dialog due to some google api requirements
                ((MainActivity)getActivity()).createEventAsync(eventTitle.getText().toString(), eventLocation.getText().toString(), eventDes.getText().toString(), start, end, eventAttendeeEmail );
                dismiss();
                break;
        }
    }

    private String startDateString, endDateString;
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        switch (datePicker.getId()){
            case R.id.new_event_start_date:
                startDateString  = i+" , "+i1+" , "+i2;
                System.out.println(startDateString);
                break;
            case R.id.new_event_end_date:
                endDateString  = i+" , "+i1+" , "+i2;
                System.out.println(endDateString);
                break;
        }
    }

    private String startTimeString, endTimeString;
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        switch (timePicker.getId()){
            case R.id.new_event_start_time:
                startTimeString = i+", "+i1;
                System.out.println(startTimeString);
                break;
            case R.id.new_event_end_time:
                endTimeString = i+", "+i1;
                System.out.println(startTimeString);
                break;
        }
    }
}
