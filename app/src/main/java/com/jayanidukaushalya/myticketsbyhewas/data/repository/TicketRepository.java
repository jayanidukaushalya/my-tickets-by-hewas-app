package com.jayanidukaushalya.myticketsbyhewas.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.RetrofitClient;

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
                .enqueue(new Callback<List<Ticket>>() {
                    @Override
                    public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ticketsData.postValue(response.body());
                        } else {
                            errorData.postValue("Failed to load tickets. Code: " + response.code());
                        }
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Ticket>> call, Throwable throwable) {
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
