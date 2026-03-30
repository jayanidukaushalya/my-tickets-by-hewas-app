package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for ticket reservation (step 1).
 */
public class ReservationResponse {

    @SerializedName("orderSessionId")
    private String orderSessionId;

    @SerializedName("expireAt")
    private String expireAt;

    @SerializedName("qty")
    private int qty;

    @SerializedName("totalAmount")
    private String totalAmount;

    @SerializedName("payment")
    private PaymentInfo payment;

    public String getOrderSessionId() {
        return orderSessionId;
    }

    public void setOrderSessionId(String orderSessionId) {
        this.orderSessionId = orderSessionId;
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

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentInfo getPayment() {
        return payment;
    }

    public void setPayment(PaymentInfo payment) {
        this.payment = payment;
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
