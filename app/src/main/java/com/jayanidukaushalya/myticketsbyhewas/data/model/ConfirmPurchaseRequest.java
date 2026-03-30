package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for confirming a purchase after payment.
 */
public class ConfirmPurchaseRequest {

    @SerializedName("orderSessionId")
    private final String orderSessionId;

    public ConfirmPurchaseRequest(String orderSessionId) {
        this.orderSessionId = orderSessionId;
    }

    public String getOrderSessionId() {
        return orderSessionId;
    }
}
