package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import androidx.annotation.NonNull;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.CreateCustomerRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.LinkCustomerRequest;
import com.jayanidukaushalya.myticketsbyhewas.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Implementation of CustomerApiClient using Retrofit.
 * Handles customer-related API operations with proper error handling and token management.
 */
public class CustomerApiClientImpl implements CustomerApiClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static CustomerApiClientImpl instance;
    private final CustomerApiService apiService;

    private CustomerApiClientImpl() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CustomerApiService.class);
    }

    /**
     * Get singleton instance of CustomerApiClientImpl.
     *
     * @return Singleton instance
     */
    public static synchronized CustomerApiClientImpl getInstance() {
        if (instance == null) {
            instance = new CustomerApiClientImpl();
        }
        return instance;
    }

    @Override
    public void createCustomer(String firebaseUid, String email, String firstName, String lastName, CustomerCallback callback) {
        CreateCustomerRequest request = new CreateCustomerRequest(firebaseUid, email, firstName, lastName);

        apiService.createCustomer(request).enqueue(new Callback<ApiResponse<Customer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Customer>> call, @NonNull Response<ApiResponse<Customer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Customer> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to create customer";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Customer>> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getCustomerByUid(String token, CustomerCallback callback) {
        String authHeader = "Bearer " + token;

        apiService.getCustomerByUid(authHeader).enqueue(new Callback<ApiResponse<Customer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Customer>> call, @NonNull Response<ApiResponse<Customer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Customer> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to retrieve customer";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Customer>> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void linkCustomerToFirebaseUid(String email, String token, CustomerCallback callback) {
        String authHeader = "Bearer " + token;
        LinkCustomerRequest request = new LinkCustomerRequest(email);

        apiService.linkCustomerToFirebaseUid(authHeader, request).enqueue(new Callback<ApiResponse<Customer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Customer>> call, @NonNull Response<ApiResponse<Customer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Customer> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to link customer";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Customer>> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Convert HTTP status codes to user-friendly error messages.
     *
     * @param statusCode HTTP status code
     * @return User-friendly error message
     */
    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Authentication failed. Please sign in again.";
            case 404:
                return "Customer not found.";
            case 409:
                return "Customer already exists with this information.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "An error occurred. Please try again.";
        }
    }
}
