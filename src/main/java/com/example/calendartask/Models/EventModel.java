package com.example.calendartask.Models;


/**
 * Created by Khushvinders on 21-Oct-16.
 */

public class EventModel {

    private String eventId;
    private String eventSummery;
    private String description;
    private String attendees;
    private String location;
    private String OwnerName;
    private String startDate;
    private String endDate;
    private WeatherModel weatherCondition;



    public String getOwnerName() {
        return OwnerName;
    }
    public void setOwnerName(String ownerName) {
        OwnerName = ownerName;
    }
    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventSummery() {
        return eventSummery;
    }

    public void setEventSummery(String eventSummery) {
        this.eventSummery = eventSummery;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttendees() {
        return attendees;
    }

    public void setAttendees(String attendees) {
        this.attendees = attendees;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }


    public WeatherModel getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(WeatherModel weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

}
