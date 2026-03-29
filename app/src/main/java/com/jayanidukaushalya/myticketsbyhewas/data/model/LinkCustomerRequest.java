package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for linking an existing customer to a Firebase UID.
 */
public class LinkCustomerRequest {

    @SerializedName("email")
    private String email;

    public LinkCustomerRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
