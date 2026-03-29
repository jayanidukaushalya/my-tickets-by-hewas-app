package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;

/**
 * Customer API client interface for managing customer records.
 * Provides methods to create customers, retrieve customer data, and link Firebase UIDs.
 */
public interface CustomerApiClient {

    /**
     * Create a new customer record linked to a Firebase UID.
     *
     * @param firebaseUid Firebase user ID
     * @param email Customer email address
     * @param firstName Customer first name (optional)
     * @param lastName Customer last name (optional)
     * @param callback Callback to handle success or failure
     */
    void createCustomer(String firebaseUid, String email, String firstName, String lastName, CustomerCallback callback);

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
     * @param token Firebase ID token for authentication
     * @param callback Callback to handle success or failure
     */
    void linkCustomerToFirebaseUid(String email, String token, CustomerCallback callback);

    /**
     * Callback interface for customer API operations.
     */
    interface CustomerCallback {
        void onSuccess(Customer customer);
        void onFailure(String errorMessage);
    }
}
