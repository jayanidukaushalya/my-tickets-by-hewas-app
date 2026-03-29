package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;

/**
 * Customer API client interface for managing customer records.
 * Provides methods to create customers, retrieve customer data, and link Firebase UIDs.
 */
public interface CustomerApiClient {

    /**
     * Get customer record for the authenticated user by Firebase UID.
     *
     * @param token Firebase ID token for authentication
     * @param callback Callback to handle success or failure
     */
    void getCustomerByUid(String token, CustomerCallback callback);

    /**
     * Link an existing guest customer record to a Firebase UID.
     *
     * @param email Customer email to link
     * @param firstName First name (optional; required by API when linking)
     * @param lastName Last name (optional; required by API when linking)
     * @param token Firebase ID token for authentication
     * @param callback Callback to handle success or failure
     */
    void linkCustomerToFirebaseUid(String email, String firstName, String lastName, String token, CustomerCallback callback);

    /**
     * Update the authenticated customer's first and last name.
     *
     * @param token Firebase ID token
     * @param firstName First name
     * @param lastName Last name
     * @param phone Contact number
     * @param callback Callback to handle success or failure
     */
    void updateCustomerProfile(String token, String firstName, String lastName, String phone, CustomerCallback callback);

    /**
     * Callback interface for customer API operations.
     */
    interface CustomerCallback {
        void onSuccess(Customer customer);
        void onFailure(String errorMessage);
    }
}
