package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for reserving tickets.
 * Used for both guest and authenticated users.
 */
public class ReserveTicketsRequest {

    @SerializedName("ticketId")
    private final String ticketId;

    @SerializedName("qty")
    private final int qty;

    @SerializedName("email")
    private final String email;

    @SerializedName("firstName")
    private final String firstName;

    @SerializedName("lastName")
    private final String lastName;

    /**
     * Constructor for guest users (includes email and name).
     */
    public ReserveTicketsRequest(String ticketId, int qty, String email, String firstName, String lastName) {
        this.ticketId = ticketId;
        this.qty = qty;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor for authenticated users (no email/name required).
     */
    public ReserveTicketsRequest(String ticketId, int qty) {
        this.ticketId = ticketId;
        this.qty = qty;
        this.email = null;
        this.firstName = null;
        this.lastName = null;
    }

    public String getTicketId() {
        return ticketId;
    }

    public int getQty() {
        return qty;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
