package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentEventsBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.EventViewModel;

public class EventsFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private FragmentEventsBinding binding;
    private EventViewModel viewModel;
    private EventAdapter adapter;

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
        observeViewModel();
        binding.swipeRefresh.setOnRefreshListener(this::loadEvents);
        binding.buttonRetry.setOnClickListener(v -> loadEvents());
        binding.buttonRetryError.setOnClickListener(v -> loadEvents());
        loadEvents();
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(this);
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEvents.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (!isLoading) binding.swipeRefresh.setRefreshing(false);
        });
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events == null || events.isEmpty()) {
                showEmptyState();
            } else {
                showEventsList(events);
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) showError(error);
        });
    }

    private void loadEvents() {
        hideAllStates();
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadAllEvents();
    }

    private void showEventsList(java.util.List<Event> events) {
        hideAllStates();
        adapter.submitList(events);
        binding.recyclerEvents.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        hideAllStates();
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        hideAllStates();
        binding.textError.setText(message);
        binding.layoutError.setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerEvents.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    @Override
    public void onEventClick(Event event) {
        navigateToDetail(event.getId());
    }

    @Override
    public void onBuyClick(Event event) {
        navigateToDetail(event.getId());
    }

    private void navigateToDetail(String eventId) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        navController.navigate(R.id.action_events_to_event_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
