package com.jayanidukaushalya.myticketsbyhewas.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private final RetrofitClient retrofitClient;

    public EventRepository() {
        retrofitClient = RetrofitClient.getInstance();
    }

    public void fetchAllEvents(
            MutableLiveData<List<Event>> eventsData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService().getAllEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventsData.postValue(response.body());
                } else {
                    errorData.postValue("Failed to load events. Code: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable throwable) {
                errorData.postValue(throwable.getMessage());
            }
        });
    }

    public void fetchEventById(
            String eventId,
            MutableLiveData<Event> eventData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService().getEventById(eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventData.postValue(response.body());
                } else {
                    errorData.postValue("Event not found. Code: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<Event> call,@NonNull Throwable throwable) {
                errorData.postValue(throwable.getMessage());
            }
        });
    }
}
