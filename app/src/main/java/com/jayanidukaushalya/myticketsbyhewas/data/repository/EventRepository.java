package com.jayanidukaushalya.myticketsbyhewas.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiListData;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private static final int PAGE_SIZE = 8;

    private final RetrofitClient retrofitClient;

    public EventRepository() {
        retrofitClient = RetrofitClient.getInstance();
    }

    /**
     * Fetches one page of events with all supported filters.
     *
     * @param page         1-based page number
     * @param search       nullable search query
     * @param eventType    nullable event type filter
     * @param scheduleType nullable schedule type filter
     * @param eventsData   LiveData that receives the fetched list
     * @param totalData    LiveData that receives the total count from the API
     * @param errorData    LiveData that receives an error message on failure
     */
    public void fetchEvents(
            int page,
            @Nullable String search,
            @Nullable java.util.List<String> eventType,
            @Nullable String scheduleType,
            @Nullable String dateFrom,
            @Nullable String dateTo,
            MutableLiveData<List<Event>> eventsData,
            MutableLiveData<Integer> totalData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService().getEvents(
                nullIfEmpty(search),
                page,
                PAGE_SIZE,
                eventType != null && eventType.isEmpty() ? null : eventType,
                nullIfEmpty(scheduleType),
                nullIfEmpty(dateFrom),
                nullIfEmpty(dateTo),
                null, // field
                null  // order
        ).enqueue(new Callback<ApiResponse<ApiListData<Event>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<ApiListData<Event>>> call,
                    @NonNull Response<ApiResponse<ApiListData<Event>>> response
            ) {
                ApiResponse<ApiListData<Event>> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    ApiListData<Event> data = body.getData();
                    List<Event> results = data.getResults();
                    eventsData.postValue(results != null ? results : Collections.emptyList());
                    totalData.postValue(data.getTotal());
                } else if (body != null && body.getError() != null) {
                    errorData.postValue(body.getError());
                } else {
                    errorData.postValue("Failed to load events. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ApiListData<Event>>> call, @NonNull Throwable throwable) {
                errorData.postValue(throwable.getMessage());
            }
        });
    }

    public void fetchEventById(
            String eventId,
            MutableLiveData<Event> eventData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService().getEventById(eventId).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                ApiResponse<Event> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    eventData.postValue(body.getData());
                } else if (body != null && body.getError() != null) {
                    errorData.postValue(body.getError());
                } else {
                    errorData.postValue("Event not found. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Event>> call, @NonNull Throwable throwable) {
                errorData.postValue(throwable.getMessage());
            }
        });
    }

    @Nullable
    private static String nullIfEmpty(@Nullable String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
