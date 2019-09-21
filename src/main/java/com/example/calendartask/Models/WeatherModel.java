package com.example.calendartask.Models;

import com.google.gson.annotations.SerializedName;

public class WeatherModel {

    private float temperature;

    private String cityName;

    private int humidity;

    private int weather;

    private int windspeed;


    private String icon;



    public WeatherModel(float temprature, int humidity, String icon) {
        this.temperature = temprature;
        this.humidity = humidity;
        this.icon = icon;
    }

    public float getTemprature() {
        return temperature;
    }

    public void setTemprature(float temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityname) {
        this.cityName = cityname;
    }


    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public int getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(int windspeed) {
        this.windspeed = windspeed;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


}
