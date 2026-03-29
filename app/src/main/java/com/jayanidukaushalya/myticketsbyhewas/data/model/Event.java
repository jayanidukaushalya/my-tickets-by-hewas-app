package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("venue_name")
    private String venueName;

    @SerializedName("venue_address")
    private String venueAddress;

    @SerializedName("venue_latitude")
    private double venueLatitude;

    @SerializedName("venue_longitude")
    private double venueLongitude;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("category")
    private String category;

    @SerializedName("available_tickets")
    private int availableTickets;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getVenueName() { return venueName; }
    public String getVenueAddress() { return venueAddress; }
    public double getVenueLatitude() { return venueLatitude; }
    public double getVenueLongitude() { return venueLongitude; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getAvailableTickets() { return availableTickets; }

    public boolean isFree() { return price <= 0; }
}
