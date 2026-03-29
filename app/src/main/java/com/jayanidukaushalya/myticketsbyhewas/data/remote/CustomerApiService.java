package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.CreateCustomerRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.LinkCustomerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Retrofit service interface for customer-related API endpoints.
 */
public interface CustomerApiService {

    /**
     * Create a new customer record linked to a Firebase UID.
     *
     * @param request Customer creation request containing Firebase UID and user details
     * @return API response containing the created customer
     */
    @POST("api/customers")
    Call<ApiResponse<Customer>> createCustomer(@Body CreateCustomerRequest request);

    /**
     * Get customer record for the authenticated user.
     *
     * @param authToken Bearer token for authentication
     * @return API response containing the customer record
     */
    @GET("api/customers/me")
    Call<ApiResponse<Customer>> getCustomerByUid(@Header("Authorization") String authToken);

    /**
     * Link an existing guest customer record to a Firebase UID.
     *
     * @param authToken Bearer token for authentication
     * @param request Link request containing the email to link
     * @return API response containing the updated customer record
     */
    @PUT("api/customers/link")
    Call<ApiResponse<Customer>> linkCustomerToFirebaseUid(
            @Header("Authorization") String authToken,
            @Body LinkCustomerRequest request
    );
}
