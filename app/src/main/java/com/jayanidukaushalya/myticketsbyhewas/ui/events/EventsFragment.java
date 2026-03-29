package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentEventsBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.EventViewModel;

import java.util.List;

public class EventsFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private static final long SEARCH_DEBOUNCE_MS = 500L;

    private FragmentEventsBinding binding;
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;
    private String activeScheduleType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        observeViewModel();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
        binding.buttonRetry.setOnClickListener(v -> viewModel.refresh());
        binding.buttonRetryError.setOnClickListener(v -> viewModel.refresh());

        viewModel.refresh();
    }

    // ── RecyclerView & infinite scroll ───────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new EventAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerEvents.setLayoutManager(layoutManager);
        binding.recyclerEvents.setAdapter(adapter);

        binding.recyclerEvents.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                int total = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (!viewModel.isLastPage() && lastVisible >= total - 3) {
                    viewModel.loadNextPage();
                }
            }
        });
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private void setupSearch() {
        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                cancelPendingSearch();
                viewModel.setSearch(v.getText() != null ? v.getText().toString() : null);
                return true;
            }
            return false;
        });

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                cancelPendingSearch();
                final String text = s != null ? s.toString() : "";
                searchDebounceRunnable = () -> viewModel.setSearch(text);
                searchHandler.postDelayed(searchDebounceRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void cancelPendingSearch() {
        if (searchDebounceRunnable != null) {
            searchHandler.removeCallbacks(searchDebounceRunnable);
            searchDebounceRunnable = null;
        }
    }

    // ── Filter chips ─────────────────────────────────────────────────────────

    private void setupFilterChips() {
        setupEventTypeChip(binding.chipFilterConference, "conference");
        setupEventTypeChip(binding.chipFilterFestival, "festival");
        setupEventTypeChip(binding.chipFilterConcert, "concert");
        setupEventTypeChip(binding.chipFilterSport, "sport");
        setupEventTypeChip(binding.chipFilterExhibition, "exhibition");
        setupScheduleTypeChip(binding.chipFilterSingleDay, "single_day");
        setupScheduleTypeChip(binding.chipFilterMultiDay, "multi_day");
    }

    private void setupEventTypeChip(Chip chip, String eventType) {
        chip.setOnCheckedChangeListener((v, isChecked) ->
                viewModel.toggleEventTypeFilter(eventType, isChecked));
    }

    private void setupScheduleTypeChip(Chip chip, String scheduleType) {
        chip.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                uncheckScheduleTypeChips(chip.getId());
                activeScheduleType = scheduleType;
            } else if (scheduleType.equals(activeScheduleType)) {
                activeScheduleType = null;
            }
            viewModel.setScheduleTypeFilter(activeScheduleType);
        });
    }

    private void uncheckScheduleTypeChips(int exceptId) {
        int[] scheduleChipIds = {
                R.id.chip_filter_single_day,
                R.id.chip_filter_multi_day
        };
        for (int id : scheduleChipIds) {
            if (id != exceptId) {
                Chip c = binding.getRoot().findViewById(id);
                if (c != null) {
                    c.setChecked(false);
                }
            }
        }
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            if (!Boolean.TRUE.equals(isLoading)) {
                binding.swipeRefresh.setRefreshing(false);
            }
            applyEventsContentState();
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> applyEventsContentState());

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            } else {
                binding.layoutError.setVisibility(View.GONE);
                applyEventsContentState();
            }
        });
    }

    /**
     * Keeps list / empty in sync with loading. LiveData can deliver {@code events} before
     * {@code isLoading} is false; the old logic skipped UI updates and left the list hidden.
     * When there are items, always show the list (including stale data while refreshing).
     */
    private void applyEventsContentState() {
        if (binding == null) {
            return;
        }
        List<Event> events = viewModel.getEvents().getValue();
        boolean loading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
        if (events != null && !events.isEmpty()) {
            showEventsList(events);
            return;
        }
        if (loading) {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerEvents.setVisibility(View.GONE);
            return;
        }
        showEmptyState();
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    private void showEventsList(List<Event> events) {
        hideContentStates();
        adapter.submitList(events);
        binding.recyclerEvents.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        hideContentStates();
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        hideContentStates();
        binding.progressBar.setVisibility(View.GONE);
        binding.textError.setText(message);
        binding.layoutError.setVisibility(View.VISIBLE);
    }

    private void hideContentStates() {
        binding.recyclerEvents.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Override
    public void onEventClick(Event event) {
        navigateToDetail(event.getId());
    }

    @Override
    public void onBuyClick(Event event) {
        navigateToDetail(event.getId());
    }

    private void navigateToDetail(String eventId) {
        NavController nav = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        nav.navigate(R.id.action_events_to_event_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
