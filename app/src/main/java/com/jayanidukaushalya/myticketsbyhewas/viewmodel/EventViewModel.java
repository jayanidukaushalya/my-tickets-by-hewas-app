package com.jayanidukaushalya.myticketsbyhewas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class EventViewModel extends AndroidViewModel {

    private static final int PAGE_SIZE = 8;

    private final EventRepository eventRepository;

    // ── Pagination state ─────────────────────────────────────────────────────
    private int currentPage = 1;
    private int totalItems  = 0;
    private boolean isLastPage = false;

    // ── Filter / search state ────────────────────────────────────────────────
    private String searchQuery     = null;
    private final List<String> eventTypeFilter = new ArrayList<>();
    private String scheduleTypeFilter = null;

    // ── Exposed LiveData ─────────────────────────────────────────────────────
    private final MutableLiveData<List<Event>> events       = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event>       selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean>     isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean>     isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<String>      errorMessage = new MutableLiveData<>();

    // Internal single-page sink used by the repository callback
    private final MutableLiveData<List<Event>>  pageSink  = new MutableLiveData<>();
    private final MutableLiveData<Integer>      totalSink = new MutableLiveData<>();

    public EventViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository();

        // Merge page results into the accumulating events list
        pageSink.observeForever(page -> {
            if (page == null) return;
            if (currentPage == 1) {
                events.postValue(new ArrayList<>(page));
            } else {
                List<Event> existing = events.getValue();
                List<Event> merged = new ArrayList<>(existing != null ? existing : new ArrayList<>());
                merged.addAll(page);
                events.postValue(merged);
            }
            isLoading.postValue(false);
            isLoadingMore.postValue(false);
        });

        totalSink.observeForever(total -> {
            if (total == null) return;
            totalItems = total;
            List<Event> current = events.getValue();
            isLastPage = (current != null && current.size() >= totalItems);
        });

        errorMessage.observeForever(msg -> {
            if (msg != null) {
                isLoading.postValue(false);
                isLoadingMore.postValue(false);
            }
        });
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Reset to page 1 and reload (used on fresh load / swipe-refresh). */
    public void refresh() {
        currentPage = 1;
        totalItems  = 0;
        isLastPage  = false;
        isLoading.setValue(true);
        errorMessage.setValue(null);
        fetchPage(1);
    }

    /** Load next page when user scrolls to the end. No-ops if already loading or on last page. */
    public void loadNextPage() {
        Boolean loading = isLoading.getValue();
        Boolean loadingMore = isLoadingMore.getValue();
        if (Boolean.TRUE.equals(loading) || Boolean.TRUE.equals(loadingMore) || isLastPage) return;
        currentPage++;
        isLoadingMore.setValue(true);
        fetchPage(currentPage);
    }

    /** Apply a free-text search and reset to page 1. */
    public void setSearch(@Nullable String query) {
        searchQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        refresh();
    }

    /** Toggle an eventType filter in the list. */
    public void toggleEventTypeFilter(String eventType, boolean enabled) {
        if (enabled) {
            if (!eventTypeFilter.contains(eventType)) {
                eventTypeFilter.add(eventType);
            }
        } else {
            eventTypeFilter.remove(eventType);
        }
        refresh();
    }

    /** Toggle a scheduleType filter (pass null to clear). */
    public void setScheduleTypeFilter(@Nullable String scheduleType) {
        scheduleTypeFilter = scheduleType;
        refresh();
    }

    public void loadEventById(String eventId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        eventRepository.fetchEventById(eventId, selectedEvent, errorMessage);
        selectedEvent.observeForever(result -> isLoading.postValue(false));
        errorMessage.observeForever(error -> { if (error != null) isLoading.postValue(false); });
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public LiveData<List<Event>> getEvents()       { return events; }
    public LiveData<Event> getSelectedEvent()      { return selectedEvent; }
    public LiveData<Boolean> getIsLoading()        { return isLoading; }
    public LiveData<Boolean> getIsLoadingMore()    { return isLoadingMore; }
    public LiveData<String> getErrorMessage()      { return errorMessage; }
    public boolean isLastPage()                    { return isLastPage; }
    public String getSearchQuery()                 { return searchQuery; }
    public List<String> getEventTypeFilter()       { return eventTypeFilter; }
    public String getScheduleTypeFilter()          { return scheduleTypeFilter; }

    // ── Internal ──────────────────────────────────────────────────────────────
    private void fetchPage(int page) {
        eventRepository.fetchEvents(
                page,
                searchQuery,
                eventTypeFilter,
                scheduleTypeFilter,
                pageSink,
                totalSink,
                errorMessage
        );
    }
}
