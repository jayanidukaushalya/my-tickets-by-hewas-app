package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    @SerializedName("payment")
    private PaymentInfo payment;

    @SerializedName("sessionIds")
    private List<String> sessionIds;

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

    public PaymentInfo getPayment() {
        return payment;
    }

    public void setPayment(PaymentInfo payment) {
        this.payment = payment;
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }

    public void setSessionIds(List<String> sessionIds) {
        this.sessionIds = sessionIds;
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

    public static class PaymentInfo {
        @SerializedName("provider")
        private String provider;

        @SerializedName("sandbox")
        private boolean sandbox;

        @SerializedName("merchantId")
        private String merchantId;

        @SerializedName("merchantSecret")
        private String merchantSecret;

        @SerializedName("notifyUrl")
        private String notifyUrl;

        @SerializedName("orderId")
        private String orderId;

        @SerializedName("itemsDescription")
        private String itemsDescription;

        @SerializedName("currency")
        private String currency;

        @SerializedName("amount")
        private String amount;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public boolean isSandbox() {
            return sandbox;
        }

        public void setSandbox(boolean sandbox) {
            this.sandbox = sandbox;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getMerchantSecret() {
            return merchantSecret;
        }

        public void setMerchantSecret(String merchantSecret) {
            this.merchantSecret = merchantSecret;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getItemsDescription() {
            return itemsDescription;
        }

        public void setItemsDescription(String itemsDescription) {
            this.itemsDescription = itemsDescription;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}
