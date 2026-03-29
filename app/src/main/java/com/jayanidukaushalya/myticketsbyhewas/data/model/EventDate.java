package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventDate {

    @SerializedName("id")
    private String id;

    @SerializedName("date")
    private String date;

    @SerializedName("timeSlots")
    private List<EventTimeSlot> timeSlots;

    public String getId() { return id; }
    public String getDate() { return date; }
    public List<EventTimeSlot> getTimeSlots() { return timeSlots; }
}
