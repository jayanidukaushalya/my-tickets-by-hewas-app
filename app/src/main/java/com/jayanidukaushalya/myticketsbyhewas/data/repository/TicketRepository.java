package com.jayanidukaushalya.myticketsbyhewas.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiListData;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketRepository {

    private final RetrofitClient retrofitClient;

    public TicketRepository() {
        retrofitClient = RetrofitClient.getInstance();
    }

    public void fetchUserTickets(
            String userId,
            String authToken,
            MutableLiveData<List<Ticket>> ticketsData,
            MutableLiveData<String> errorData,
            @Nullable Runnable onComplete
    ) {
        retrofitClient.getApiService()
                .getUserTickets(userId, "Bearer " + authToken)
                .enqueue(new Callback<ApiResponse<ApiListData<Ticket>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ApiListData<Ticket>>> call,
                            Response<ApiResponse<ApiListData<Ticket>>> response
                    ) {
                        ApiResponse<ApiListData<Ticket>> body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                            List<Ticket> list = body.getData().getResults();
                            ticketsData.postValue(list != null ? list : Collections.emptyList());
                        } else if (body != null && body.getError() != null) {
                            errorData.postValue(body.getError());
                        } else {
                            errorData.postValue("Failed to load tickets. Code: " + response.code());
                        }
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<ApiListData<Ticket>>> call, Throwable throwable) {
                        errorData.postValue(throwable.getMessage());
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    public void validateTicket(
            String ticketId,
            String authToken,
            MutableLiveData<Ticket> ticketData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService()
                .validateTicket(ticketId, "Bearer " + authToken)
                .enqueue(new Callback<Ticket>() {
                    @Override
                    public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ticketData.postValue(response.body());
                        } else {
                            errorData.postValue("Ticket not found. Code: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Ticket> call, Throwable throwable) {
                        errorData.postValue(throwable.getMessage());
                    }
                });
    }
}
