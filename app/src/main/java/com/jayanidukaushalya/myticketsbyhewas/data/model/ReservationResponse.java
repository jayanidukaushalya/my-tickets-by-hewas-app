package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for ticket reservation.
 */
public class ReservationResponse {

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("expireAt")
    private String expireAt;

    @SerializedName("qty")
    private int qty;

    @SerializedName("ticket")
    private TicketInfo ticket;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public TicketInfo getTicket() {
        return ticket;
    }

    public void setTicket(TicketInfo ticket) {
        this.ticket = ticket;
    }

    /**
     * Nested ticket information in reservation response.
     */
    public static class TicketInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private String price;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
