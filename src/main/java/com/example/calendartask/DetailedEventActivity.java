package com.example.calendartask;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.calendartask.Models.WeatherModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DetailedEventActivity extends AppCompatActivity {

    ProgressDialog mProgress;
    RelativeLayout relativeLayout;
    WeatherModel weatherCondition;
    TextView TitleTV,DescriptionTV,LocationTV,AttendeesTV,StartTimeTV,EndTimeTV,HumidityTV,TempretureTV;
    String titleString,DescriptionString,LocationString,attendeesString,StartTimeString,EndTimeString;
    ImageView weatherIcon;
    private String API_URL = "https://samples.openweathermap.org/data/2.5/weather?q=";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    List<String> cities = Arrays.asList("London","Hurzuf","Novinki","London","London","Kathmandu","Merida","Vinogradovo","Cherkizovo","Lichtenrade");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);
        setViews();
        getExtras();
        setData();
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting weather status..");
        mProgress.show();
        mProgress.setCanceledOnTouchOutside(false);
        CallMyApiMethod();



    }


    private void setData() {
        TitleTV.setText(titleString);
        DescriptionTV.setText(DescriptionString);
        LocationTV.setText(LocationString);
        AttendeesTV.setText(attendeesString);
        StartTimeTV.setText(StartTimeString);
        EndTimeTV.setText(EndTimeString);
    }

    private void getExtras() {

        titleString = getIntent().getStringExtra("Title");
        DescriptionString = getIntent().getStringExtra("Des");
        LocationString = getIntent().getStringExtra("Location");
        attendeesString = getIntent().getStringExtra("Attendees");
        StartTimeString  = getIntent().getStringExtra("StartTime");
        EndTimeString  = getIntent().getStringExtra("EndTime");


    }

    void setViews()
    {
        TitleTV = findViewById(R.id.detailedEventTitle);
        DescriptionTV = findViewById(R.id.detailedEventDes);
        LocationTV = findViewById(R.id.detailedEventLocation);
        AttendeesTV = findViewById(R.id.detailedEventAttendee);
        StartTimeTV = findViewById(R.id.detailedEventStart);
        EndTimeTV = findViewById(R.id.detailedEventEnd);
        HumidityTV = findViewById(R.id.humidityTextView);
        TempretureTV = findViewById(R.id.tempratureTextView);
        weatherIcon = findViewById(R.id.weatherIcon);
        relativeLayout = findViewById(R.id.containerRelativeLayout);

    }


    ///Request to call the weather api to retrieve the weather state for specific city
    private void CallMyApiMethod() {

        API_URL = "https://samples.openweathermap.org/data/2.5/weather?q=";
        API_URL+=LocationString;
        API_URL+="&appid="+getResources().getString(R.string.open_weather_maps_app_id);

        RequestQueue queue = Volley.newRequestQueue(this);
//                Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                       ParseData(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

// Add the request to the RequestQueue.y
        queue.add(stringRequest);
    }



    ////HELPERS FUNCTION TO PARSE RESPONSE
    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }
    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }
    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }


    private void ParseData (String response)
    {
        try {
            JSONObject jObj = new JSONObject(response);

            // its An Array , but I will use only the first value
            JSONArray jArr = jObj.getJSONArray("weather");
            JSONObject JSONWeather = jArr.getJSONObject(0);
            String icon = getString("icon", JSONWeather);
            JSONObject mainObj =getObject("main", jObj);
            float temp = getFloat("temp",mainObj)- 273;
            int humidity =  getInt("humidity",mainObj);

            weatherCondition = new WeatherModel(temp,humidity,icon);
            mProgress.hide();
            relativeLayout.setVisibility(View.VISIBLE);
            HumidityTV.setText(String.valueOf(weatherCondition.getHumidity()));

            ///just to handle style for showing text
            if(weatherCondition.getTemprature()>=35F)
                TempretureTV.setTextColor(getColor(R.color.hotwather));
            else
                TempretureTV.setTextColor(getColor(R.color.iceblue));


            TempretureTV.setText(String.valueOf(weatherCondition.getTemprature()));
            IMG_URL+=weatherCondition.getIcon()+ ".png";
            Picasso.get().load(IMG_URL).into(weatherIcon);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
