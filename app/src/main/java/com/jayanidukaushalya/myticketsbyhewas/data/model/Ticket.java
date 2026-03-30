package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

public class Ticket {

    public static final String STATUS_VALID = "valid";
    public static final String STATUS_USED = "used";
    public static final String STATUS_EXPIRED = "expired";

    @SerializedName("id")
    private String id;

    @SerializedName("eventId")
    private String eventId;

    @SerializedName("eventTitle")
    private String eventTitle;

    @SerializedName("eventDate")
    private String eventDate;

    @SerializedName("eventImageUrl")
    private String eventImageUrl;

    @SerializedName("userId")
    private String userId;

    @SerializedName("ticketName")
    private String ticketName;

    @SerializedName("purchaseDate")
    private String purchaseDate;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("status")
    private String status;

    @SerializedName("price")
    private double price;

    @SerializedName("qty")
    private int qty;

    @SerializedName("eventLocation")
    private String eventLocation;

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getEventDate() { return eventDate; }
    public String getEventImageUrl() { return eventImageUrl; }
    public String getUserId() { return userId; }
    public String getTicketName() { return ticketName; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getQrCode() { return qrCode; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public String getEventLocation() { return eventLocation; }

    public boolean isValid() { return STATUS_VALID.equals(status); }
    public boolean isUsed() { return STATUS_USED.equals(status); }
    public boolean isExpired() { return STATUS_EXPIRED.equals(status); }
}
