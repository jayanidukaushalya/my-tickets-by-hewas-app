package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import androidx.annotation.NonNull;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiListData;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ConfirmPurchaseRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Purchase;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReserveTicketsRequest;

import java.util.Collections;
import java.util.List;

import com.jayanidukaushalya.myticketsbyhewas.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Implementation of PurchaseApiClient using Retrofit.
 * Handles purchase-related API operations with proper error handling and token management.
 */
public class PurchaseApiClientImpl implements PurchaseApiClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static PurchaseApiClientImpl instance;
    private final PurchaseApiService apiService;

    private PurchaseApiClientImpl() {
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

        apiService = retrofit.create(PurchaseApiService.class);
    }

    /**
     * Get singleton instance of PurchaseApiClientImpl.
     *
     * @return Singleton instance
     */
    public static synchronized PurchaseApiClientImpl getInstance() {
        if (instance == null) {
            instance = new PurchaseApiClientImpl();
        }
        return instance;
    }

    @Override
    public void reserveTicketsBulk(List<ReserveTicketsRequest.Item> items, String email, String firstName, String lastName, String phone, String token, ReservationCallback callback) {
        ReserveTicketsRequest request;
        String authHeader = null;

        if (token != null && !token.isEmpty()) {
            request = new ReserveTicketsRequest(items, phone);
            authHeader = "Bearer " + token;
        } else {
            request = new ReserveTicketsRequest(items, email, firstName, lastName, phone);
        }

        apiService.reserveTickets(authHeader, request).enqueue(new Callback<ApiResponse<ReservationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReservationResponse>> call, @NonNull Response<ApiResponse<ReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ReservationResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to reserve tickets";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReservationResponse>> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void confirmPurchase(String orderSessionId, String token, PurchaseCallback callback) {
        ConfirmPurchaseRequest request = new ConfirmPurchaseRequest(orderSessionId);
        String authHeader = (token != null && !token.isEmpty()) ? "Bearer " + token : null;

        apiService.confirmPurchase(authHeader, request).enqueue(new Callback<ApiResponse<PurchaseResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PurchaseResponse>> call, @NonNull Response<ApiResponse<PurchaseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PurchaseResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to confirm purchase";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PurchaseResponse>> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getPurchaseHistory(String token, PurchaseHistoryCallback callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication required to view purchase history");
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getPurchaseHistory(authHeader).enqueue(new Callback<ApiResponse<ApiListData<Purchase>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<ApiListData<Purchase>>> call,
                    @NonNull Response<ApiResponse<ApiListData<Purchase>>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiListData<Purchase>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Purchase> list = apiResponse.getData().getResults();
                        callback.onSuccess(list != null ? list : Collections.emptyList());
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to retrieve purchase history";
                        callback.onFailure(errorMessage);
                    }
                } else {
                    callback.onFailure(getErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ApiListData<Purchase>>> call, @NonNull Throwable t) {
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
                return "Resource not found.";
            case 409:
                return "Tickets are no longer available.";
            case 410:
                return "Purchase session has expired. Please start over.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "An error occurred. Please try again.";
        }
    }
}
