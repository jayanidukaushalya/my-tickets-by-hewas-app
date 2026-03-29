package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemEventCardBinding;

import java.util.ArrayList;
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

            // Date — formatted using the first and last dates (for range) and first slot
            binding.textEventDate.setText(
                    EventScheduleFormatter.formatDateTimeDisplay(
                            event.getFirstDate(), 
                            event.getLastDate(), 
                            event.getFirstStartTime()));

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

        private String formatPrice(Event event) {
            if (event.isFree()) return "Free";
            String price = String.format(Locale.US, "LKR %,.0f", event.getLowestPrice());
            return event.hasMultiplePrices() ? "From " + price : price;
        }
    }
}
