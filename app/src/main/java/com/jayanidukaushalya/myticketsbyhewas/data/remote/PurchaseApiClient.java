package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Purchase;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReserveTicketsRequest;

import java.util.List;

/**
 * Purchase API client interface for managing ticket purchases.
 * Provides methods to reserve tickets, confirm purchases, and retrieve purchase history.
 */
public interface PurchaseApiClient {

    /**
     * Reserve one or more ticket line items for purchase (Step 1 of 2).
     */
    void reserveTicketsBulk(List<ReserveTicketsRequest.Item> items, String email, String firstName, String lastName, String phone, String token, ReservationCallback callback);

    /**
     * Confirm one or more reserved sessions after payment (Step 2 of 2).
     */
    void confirmPurchases(List<String> sessionIds, String token, PurchaseCallback callback);

    /**
     * Get purchase history for the authenticated user.
     * Requires authentication token.
     *
     * @param token Firebase ID token for authentication
     * @param callback Callback to handle success or failure
     */
    void getPurchaseHistory(String token, PurchaseHistoryCallback callback);

    /**
     * Callback interface for reservation operations.
     */
    interface ReservationCallback {
        void onSuccess(ReservationResponse reservation);
        void onFailure(String errorMessage);
    }

    /**
     * Callback interface for purchase confirmation operations.
     */
    interface PurchaseCallback {
        void onSuccess(PurchaseResponse purchase);
        void onFailure(String errorMessage);
    }

    /**
     * Callback interface for purchase history operations.
     */
    interface PurchaseHistoryCallback {
        void onSuccess(List<Purchase> purchases);
        void onFailure(String errorMessage);
    }
}
