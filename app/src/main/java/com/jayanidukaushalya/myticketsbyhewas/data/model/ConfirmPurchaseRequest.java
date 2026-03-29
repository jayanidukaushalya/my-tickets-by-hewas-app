package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for confirming a purchase.
 */
public class ConfirmPurchaseRequest {

    @SerializedName("sessionId")
    private final String sessionId;

    public ConfirmPurchaseRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
