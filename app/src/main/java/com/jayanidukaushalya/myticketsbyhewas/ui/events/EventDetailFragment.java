package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.color.MaterialColors;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.model.EventDate;
import com.jayanidukaushalya.myticketsbyhewas.data.model.EventTimeSlot;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentEventDetailBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemScheduleDayBlockBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.EventViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback {

    private FragmentEventDetailBinding binding;
    private EventViewModel viewModel;
    private Event currentEvent;
    private LatLng venueLatLng;

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
        getChildFragmentManager().setFragmentResultListener(
                TicketSelectionBottomSheet.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> navigateToCheckout(result)
        );
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
        binding.collapsingToolbar.setTitle(event.getName());
        binding.textEventTitle.setText(event.getName());

        String cat = event.getEventType();
        if (cat == null || cat.isEmpty()) cat = event.getScheduleType();
        binding.chipCategory.setText(cat != null ? cat.replace("_", " ") : "");

        binding.textEventDescription.setText(event.getDescription() != null ? event.getDescription() : "");
        binding.textEventDescription.setVisibility(
                event.getDescription() != null && !event.getDescription().isEmpty()
                        ? View.VISIBLE : View.GONE);

        buildScheduleSection(event);

        binding.textVenueName.setText(event.getVenueName() != null ? event.getVenueName() : "");
        binding.textVenueAddress.setText(event.getVenueAddress() != null ? event.getVenueAddress() : "");

        populatePriceBar(event);

        Glide.with(this)
                .load(event.getImage())
                .placeholder(R.drawable.ic_event_placeholder)
                .centerCrop()
                .into(binding.imageEventBanner);

        setupMap();
        binding.buttonBuy.setOnClickListener(v -> onBuyClicked());
    }

    // ── Schedule section ────────────────────────────────────────────────────

    private void buildScheduleSection(Event event) {
        LinearLayout container = binding.layoutScheduleContent;
        container.removeAllViews();

        List<EventDate> dates = event.getEventDates();
        if (dates == null || dates.isEmpty()) return;

        String scheduleType = event.getScheduleType() != null ? event.getScheduleType() : "";

        if ("multi_day".equals(scheduleType)) {
            for (EventDate date : dates) {
                List<EventTimeSlot> slots = date.getTimeSlots();
                List<EventTimeSlot> safeSlots = slots != null ? slots : new ArrayList<>();
                inflateDayBlock(container, date.getDate(), safeSlots);
            }
        } else {
            Map<String, List<EventTimeSlot>> merged = mergeSlotsByDate(dates);
            for (Map.Entry<String, List<EventTimeSlot>> entry : merged.entrySet()) {
                inflateDayBlock(container, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Merges time slots for the same calendar day so duplicate API rows do not repeat the date.
     */
    private Map<String, List<EventTimeSlot>> mergeSlotsByDate(List<EventDate> dates) {
        Map<String, List<EventTimeSlot>> map = new LinkedHashMap<>();
        for (EventDate d : dates) {
            String key = d.getDate() != null ? d.getDate() : "";
            map.computeIfAbsent(key, k -> new ArrayList<>());
            if (d.getTimeSlots() != null) {
                map.get(key).addAll(d.getTimeSlots());
            }
        }
        return map;
    }

    private void inflateDayBlock(LinearLayout container, @Nullable String isoDate,
                                 List<EventTimeSlot> slots) {
        ItemScheduleDayBlockBinding dayBinding = ItemScheduleDayBlockBinding.inflate(
                LayoutInflater.from(requireContext()), container, false);
        dayBinding.textScheduleDayDate.setText(EventScheduleFormatter.formatDate(isoDate, true));
        LinearLayout slotsLayout = dayBinding.layoutScheduleTimeSlots;
        if (slots.isEmpty()) {
            container.addView(dayBinding.getRoot());
            return;
        }
        for (int i = 0; i < slots.size(); i++) {
            EventTimeSlot slot = slots.get(i);
            TextView tv = new TextView(requireContext());
            tv.setText(EventScheduleFormatter.formatTimeRange(slot.getStartTime(), slot.getEndTime()));
            TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            int muted = MaterialColors.getColor(requireContext(),
                    com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY);
            tv.setTextColor(muted);
            tv.setBackgroundResource(R.drawable.bg_schedule_time_pill);
            int padH = dpToPx(14);
            int padV = dpToPx(10);
            tv.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = dpToPx(8);
            tv.setLayoutParams(lp);
            slotsLayout.addView(tv);
        }
        container.addView(dayBinding.getRoot());
    }

    // ── Price bar ───────────────────────────────────────────────────────────

    private void populatePriceBar(Event event) {
        if (event.isFree()) {
            binding.textPriceLabel.setText(getString(R.string.label_tickets_available).toUpperCase());
            binding.textPrice.setText(R.string.label_free);
        } else {
            double price = event.getLowestPrice();
            String formatted = String.format(Locale.US, "LKR %,.0f", price);
            if (event.hasMultiplePrices()) {
                binding.textPriceLabel.setText("FROM");
                binding.textPrice.setText(formatted);
            } else {
                binding.textPriceLabel.setText("PRICE");
                binding.textPrice.setText(formatted);
            }
        }
    }

    // ── Map ─────────────────────────────────────────────────────────────────

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (currentEvent == null || currentEvent.getLocation() == null) return;
        double lat, lng;
        try {
            lat = Double.parseDouble(currentEvent.getLocation().getLatitude());
            lng = Double.parseDouble(currentEvent.getLocation().getLongitude());
        } catch (NumberFormatException | NullPointerException e) {
            return;
        }
        LatLng venueLocation = new LatLng(lat, lng);
        venueLatLng = venueLocation;
        googleMap.addMarker(new MarkerOptions()
                .position(venueLocation)
                .title(currentEvent.getVenueName())
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 15f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        if (binding != null) {
            binding.buttonOpenInMaps.setVisibility(View.VISIBLE);
            binding.buttonOpenInMaps.setOnClickListener(v -> openInExternalMaps());
        }
    }

    private void openInExternalMaps() {
        if (venueLatLng == null || currentEvent == null || currentEvent.getLocation() == null) {
            return;
        }
        String label = currentEvent.getVenueName();
        if (label == null || label.isEmpty()) {
            label = currentEvent.getLocation().getAddress();
        }
        if (label == null) {
            label = "";
        }
        String uriString = "geo:" + venueLatLng.latitude + "," + venueLatLng.longitude +
                "?q=" + Uri.encode(venueLatLng.latitude + "," + venueLatLng.longitude + " (" + label + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
            intent.setPackage(null);
        }
        startActivity(intent);
    }

    // ── Buy flow ─────────────────────────────────────────────────────────────

    private void onBuyClicked() {
        if (currentEvent == null) return;
        TicketSelectionBottomSheet sheet = TicketSelectionBottomSheet.newInstance();
        sheet.show(getChildFragmentManager(), TicketSelectionBottomSheet.TAG);
    }

    private void navigateToCheckout(Bundle selection) {
        Bundle args = new Bundle();
        args.putString("ticketsJson", selection.getString("ticketsJson"));
        NavHostFragment.findNavController(this).navigate(
                R.id.action_event_detail_to_checkout,
                args
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
