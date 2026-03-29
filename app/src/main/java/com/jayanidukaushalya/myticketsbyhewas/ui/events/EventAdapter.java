package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemEventCardBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onBuyClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private final ItemEventCardBinding binding;

        EventViewHolder(ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event) {
            // Title
            binding.textEventTitle.setText(event.getName());

            // Category chip — show eventType, fall back to scheduleType
            String category = event.getEventType();
            if (category == null || category.isEmpty()) category = event.getScheduleType();
            binding.chipCategory.setText(category != null ? category.replace("_", " ") : "");

            // Date — parse the ISO date from the first eventDate entry
            binding.textEventDate.setText(formatDate(event.getFirstDate(), event.getFirstStartTime()));

            // Venue — nested location.venue
            String venue = event.getVenueName();
            binding.textEventVenue.setText(venue != null ? venue : "");

            // Price — derived from nested tickets
            binding.textEventPrice.setText(formatPrice(event));

            // Banner image
            Glide.with(binding.imageEventBanner.getContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.ic_event_placeholder)
                    .centerCrop()
                    .into(binding.imageEventBanner);

            binding.getRoot().setOnClickListener(v -> listener.onEventClick(event));
            binding.buttonBuy.setOnClickListener(v -> listener.onBuyClick(event));
        }

        /**
         * Formats an ISO-8601 date+time pair into a human-readable string.
         * e.g. "Mar 25, 2026 • 10:30 AM"
         */
        private String formatDate(String isoDate, String isoTime) {
            // Try to use the time field first as it carries the most precise info
            String source = isoTime != null ? isoTime : isoDate;
            if (source == null) return "";
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat outDate = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                SimpleDateFormat outTime = new SimpleDateFormat("h:mm a", Locale.US);
                Date parsed = in.parse(source);
                if (parsed == null) return source;
                if (isoTime != null) {
                    // If we have a date too, show both with bullet separator
                    if (isoDate != null) {
                        try {
                            Date dateParsed = in.parse(isoDate);
                            if (dateParsed != null) {
                                return outDate.format(dateParsed) + " • " + outTime.format(parsed);
                            }
                        } catch (ParseException ignored) {}
                    }
                    return outTime.format(parsed);
                }
                return outDate.format(parsed);
            } catch (ParseException e) {
                return source;
            }
        }

        private String formatPrice(Event event) {
            if (event.isFree()) return "Free";
            double p = event.getLowestPrice();
            // Format with thousands separator: e.g. "LKR 50,000"
            return String.format(Locale.US, "LKR %,.0f", p);
        }
    }
}
