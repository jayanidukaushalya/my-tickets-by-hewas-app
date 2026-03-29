package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

public class PurchaseRequest {

    @SerializedName("event_id")
    private final String eventId;

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("quantity")
    private final int quantity;

    public PurchaseRequest(String eventId, String userId, int quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }

    public String getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public int getQuantity() { return quantity; }
}
