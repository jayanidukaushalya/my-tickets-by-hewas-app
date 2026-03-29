package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

public class Ticket {

    public static final String STATUS_VALID = "valid";
    public static final String STATUS_USED = "used";
    public static final String STATUS_EXPIRED = "expired";

    @SerializedName("id")
    private String id;

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("event_image_url")
    private String eventImageUrl;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("purchase_date")
    private String purchaseDate;

    @SerializedName("qr_code")
    private String qrCode;

    @SerializedName("status")
    private String status;

    @SerializedName("price")
    private double price;

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getEventDate() { return eventDate; }
    public String getEventImageUrl() { return eventImageUrl; }
    public String getUserId() { return userId; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getQrCode() { return qrCode; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }

    public boolean isValid() { return STATUS_VALID.equals(status); }
    public boolean isUsed() { return STATUS_USED.equals(status); }
    public boolean isExpired() { return STATUS_EXPIRED.equals(status); }
}
