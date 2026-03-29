package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentEventDetailBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.EventViewModel;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback {

    private FragmentEventDetailBinding binding;
    private EventViewModel viewModel;
    private Event currentEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        setupToolbar();
        observeViewModel();
        loadEventFromArgs();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
        viewModel.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                currentEvent = event;
                populateEventDetail(event);
            }
        });
    }

    private void loadEventFromArgs() {
        if (getArguments() == null) return;
        String eventId = getArguments().getString("eventId");
        if (eventId != null) viewModel.loadEventById(eventId);
    }

    private void populateEventDetail(Event event) {
        binding.collapsingToolbar.setTitle(event.getTitle());
        binding.textEventTitle.setText(event.getTitle());
        binding.chipCategory.setText(event.getCategory());
        binding.textEventDate.setText(formatDateTime(event.getDate(), event.getTime()));
        binding.textVenueName.setText(event.getVenueName());
        binding.textVenueAddress.setText(event.getVenueAddress());
        binding.textEventDescription.setText(event.getDescription());
        binding.textPrice.setText(event.isFree()
                ? getString(R.string.label_free)
                : String.format(getString(R.string.format_price), event.getPrice())
        );
        Glide.with(this)
                .load(event.getImageUrl())
                .placeholder(R.drawable.ic_event_placeholder)
                .centerCrop()
                .into(binding.imageEventBanner);
        setupMap();
        binding.buttonBuy.setOnClickListener(v -> onBuyClicked());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (currentEvent == null) return;
        LatLng venueLocation = new LatLng(
                currentEvent.getVenueLatitude(),
                currentEvent.getVenueLongitude()
        );
        googleMap.addMarker(new MarkerOptions()
                .position(venueLocation)
                .title(currentEvent.getVenueName())
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 15f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    private void onBuyClicked() {
        // PayHere integration will be added later
    }

    private String formatDateTime(String date, String time) {
        if (date == null) return "";
        if (time == null) return date;
        return date + " • " + time;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
