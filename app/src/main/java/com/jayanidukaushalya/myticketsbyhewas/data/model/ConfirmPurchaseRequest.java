package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request model for confirming a purchase.
 */
public class ConfirmPurchaseRequest {

    @SerializedName("sessionIds")
    private final List<String> sessionIds;

    public ConfirmPurchaseRequest(List<String> sessionIds) {
        this.sessionIds = sessionIds;
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }
}
