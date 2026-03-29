package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.LinkCustomerRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.UpdateCustomerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;

/**
 * Retrofit service interface for customer-related API endpoints.
 */
public interface CustomerApiService {

    /**
     * Get customer record for the authenticated user.
     *
     * @param authToken Bearer token for authentication
     * @return API response containing the customer record
     */
    @GET("customers/me")
    Call<ApiResponse<Customer>> getCustomerByUid(@Header("Authorization") String authToken);

    /**
     * Update first and last name for the authenticated customer.
     */
    @PATCH("customers/me")
    Call<ApiResponse<Customer>> updateCustomerProfile(
            @Header("Authorization") String authToken,
            @Body UpdateCustomerRequest request
    );

    /**
     * Link an existing guest customer record to a Firebase UID.
     *
     * @param authToken Bearer token for authentication
     * @param request Link request containing the email to link
     * @return API response containing the updated customer record
     */
    @PUT("customers/link")
    Call<ApiResponse<Customer>> linkCustomerToFirebaseUid(
            @Header("Authorization") String authToken,
            @Body LinkCustomerRequest request
    );
}
