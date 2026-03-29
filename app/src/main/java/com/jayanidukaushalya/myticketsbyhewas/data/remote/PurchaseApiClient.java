package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Purchase;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;

import java.util.List;

/**
 * Purchase API client interface for managing ticket purchases.
 * Provides methods to reserve tickets, confirm purchases, and retrieve purchase history.
 */
public interface PurchaseApiClient {

    /**
     * Reserve tickets for purchase (Step 1 of 2).
     * For guest users: provide email, firstName, and lastName.
     * For authenticated users: provide token and omit customer details.
     *
     * @param ticketId Ticket ID to reserve
     * @param qty Quantity of tickets to reserve
     * @param email Customer email (required for guest users, null for authenticated)
     * @param firstName Customer first name (optional for guest users, null for authenticated)
     * @param lastName Customer last name (optional for guest users, null for authenticated)
     * @param token Firebase ID token (null for guest users, required for authenticated)
     * @param callback Callback to handle success or failure
     */
    void reserveTickets(String ticketId, int qty, String email, String firstName, String lastName, String token, ReservationCallback callback);

    /**
     * Confirm a purchase after payment (Step 2 of 2).
     *
     * @param sessionId Session ID from the reservation response
     * @param token Optional Firebase ID token for authenticated users (null for guest)
     * @param callback Callback to handle success or failure
     */
    void confirmPurchase(String sessionId, String token, PurchaseCallback callback);

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
