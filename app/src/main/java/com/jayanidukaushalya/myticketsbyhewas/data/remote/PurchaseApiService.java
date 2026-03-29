package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ConfirmPurchaseRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Purchase;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReserveTicketsRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit service interface for purchase-related API endpoints.
 */
public interface PurchaseApiService {

    /**
     * Reserve tickets for purchase (Step 1 of 2).
     * Supports both guest and authenticated users.
     *
     * @param authToken Optional Bearer token for authenticated users
     * @param request Reservation request containing ticket ID, quantity, and optional customer info
     * @return API response containing the reservation details and session ID
     */
    @POST("api/tickets/purchase/reserve")
    Call<ApiResponse<ReservationResponse>> reserveTickets(
            @Header("Authorization") String authToken,
            @Body ReserveTicketsRequest request
    );

    /**
     * Confirm a purchase after payment (Step 2 of 2).
     * Can be used by both guest and authenticated users.
     *
     * @param authToken Optional Bearer token for authenticated users
     * @param request Confirmation request containing the session ID
     * @return API response containing the confirmed purchase details
     */
    @POST("api/tickets/purchase/confirm")
    Call<ApiResponse<PurchaseResponse>> confirmPurchase(
            @Header("Authorization") String authToken,
            @Body ConfirmPurchaseRequest request
    );

    /**
     * Get purchase history for the authenticated user.
     * Requires authentication.
     *
     * @param authToken Bearer token for authentication
     * @return API response containing the list of purchases
     */
    @GET("api/purchases/history")
    Call<ApiResponse<List<Purchase>>> getPurchaseHistory(
            @Header("Authorization") String authToken
    );
}
