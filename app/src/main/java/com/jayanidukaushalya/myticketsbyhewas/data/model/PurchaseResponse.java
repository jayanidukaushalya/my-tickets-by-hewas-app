package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for POST /tickets/purchase/confirm (paid order + line items).
 */
public class PurchaseResponse {

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("purchases")
    private List<ConfirmedLine> purchases;

    @SerializedName("count")
    private int count;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ConfirmedLine> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<ConfirmedLine> purchases) {
        this.purchases = purchases;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static class ConfirmedLine {
        @SerializedName("purchaseId")
        private String purchaseId;

        @SerializedName("ticketId")
        private String ticketId;

        @SerializedName("qty")
        private int qty;

        @SerializedName("price")
        private String price;

        @SerializedName("isActivated")
        private boolean isActivated;

        @SerializedName("createdAt")
        private String createdAt;

        public String getPurchaseId() {
            return purchaseId;
        }

        public String getTicketId() {
            return ticketId;
        }

        public int getQty() {
            return qty;
        }

        public String getPrice() {
            return price;
        }

        public boolean isActivated() {
            return isActivated;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
}
