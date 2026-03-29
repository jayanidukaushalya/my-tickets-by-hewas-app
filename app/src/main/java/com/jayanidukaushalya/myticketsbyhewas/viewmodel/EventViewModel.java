package com.jayanidukaushalya.myticketsbyhewas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.repository.EventRepository;

import java.util.List;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository eventRepository;
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public EventViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository();
    }

    public void loadAllEvents() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        eventRepository.fetchAllEvents(events, errorMessage);
        events.observeForever(result -> isLoading.postValue(false));
        errorMessage.observeForever(error -> {
            if (error != null) isLoading.postValue(false);
        });
    }

    public void loadEventById(String eventId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        eventRepository.fetchEventById(eventId, selectedEvent, errorMessage);
        selectedEvent.observeForever(result -> isLoading.postValue(false));
        errorMessage.observeForever(error -> {
            if (error != null) isLoading.postValue(false);
        });
    }

    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<Event> getSelectedEvent() { return selectedEvent; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
