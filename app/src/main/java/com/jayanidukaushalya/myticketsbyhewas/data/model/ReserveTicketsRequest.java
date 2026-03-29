package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request model for reserving tickets.
 * Used for both guest and authenticated users.
 */
public class ReserveTicketsRequest {

    @SerializedName("email")
    private final String email;

    @SerializedName("firstName")
    private final String firstName;

    @SerializedName("lastName")
    private final String lastName;

    @SerializedName("phone")
    private final String phone;

    @SerializedName("items")
    private final List<Item> items;

    public static class Item {
        @SerializedName("ticketId")
        private final String ticketId;

        @SerializedName("qty")
        private final int qty;

        public Item(String ticketId, int qty) {
            this.ticketId = ticketId;
            this.qty = qty;
        }

        public String getTicketId() {
            return ticketId;
        }

        public int getQty() {
            return qty;
        }
    }

    public ReserveTicketsRequest(List<Item> items, String email, String firstName, String lastName, String phone) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.items = items;
    }

    public ReserveTicketsRequest(List<Item> items, String phone) {
        this.email = null;
        this.firstName = null;
        this.lastName = null;
        this.phone = phone;
        this.items = items;
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

    public String getPhone() {
        return phone;
    }

    public List<Item> getItems() {
        return items;
    }
}
