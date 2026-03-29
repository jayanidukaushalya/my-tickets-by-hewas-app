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
            binding.textEventTitle.setText(event.getTitle());
            binding.textEventDate.setText(formatDateTime(event.getDate(), event.getTime()));
            binding.textEventVenue.setText(event.getVenueName());
            binding.textEventPrice.setText(formatPrice(event));
            binding.chipCategory.setText(event.getCategory());
            Glide.with(binding.imageEventBanner.getContext())
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.ic_event_placeholder)
                    .centerCrop()
                    .into(binding.imageEventBanner);
            binding.getRoot().setOnClickListener(v -> listener.onEventClick(event));
            binding.buttonBuy.setOnClickListener(v -> listener.onBuyClick(event));
        }

        private String formatDateTime(String date, String time) {
            if (date == null) return "";
            if (time == null) return date;
            return date + " • " + time;
        }

        private String formatPrice(Event event) {
            if (event.isFree()) return "Free";
            return String.format("LKR %.2f", event.getPrice());
        }
    }
}
