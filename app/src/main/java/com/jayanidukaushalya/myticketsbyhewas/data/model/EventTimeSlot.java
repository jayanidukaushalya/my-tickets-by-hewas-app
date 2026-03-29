package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventTimeSlot {

    @SerializedName("id")
    private String id;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("tickets")
    private List<EventTicket> tickets;

    public String getId() { return id; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public List<EventTicket> getTickets() { return tickets; }
}
