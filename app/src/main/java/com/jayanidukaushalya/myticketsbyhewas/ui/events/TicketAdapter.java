package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemTicketCardBinding;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<Ticket> tickets = new ArrayList<>();

    public void submitList(List<Ticket> newTickets) {
        tickets.clear();
        tickets.addAll(newTickets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTicketCardBinding binding = ItemTicketCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TicketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {

        private final ItemTicketCardBinding binding;

        TicketViewHolder(ItemTicketCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Ticket ticket) {
            binding.textEventTitle.setText(ticket.getEventTitle());
            binding.textEventDate.setText(ticket.getEventDate());
            if (ticket.getPurchaseDate() != null) {
                binding.textPurchaseDate.setText(
                        binding.getRoot().getContext().getString(
                                R.string.label_purchased_on, ticket.getPurchaseDate()
                        )
                );
            }
            Glide.with(binding.imageEventThumbnail.getContext())
                    .load(ticket.getEventImageUrl())
                    .placeholder(R.drawable.ic_event_placeholder)
                    .centerCrop()
                    .into(binding.imageEventThumbnail);
            applyStatusStyle(ticket);
        }

        private void applyStatusStyle(Ticket ticket) {
            int backgroundRes;
            int textColorRes;
            String statusLabel;
            if (ticket.isValid()) {
                backgroundRes = R.drawable.bg_ticket_status_valid;
                textColorRes = R.color.ticket_valid;
                statusLabel = "Valid";
            } else if (ticket.isUsed()) {
                backgroundRes = R.drawable.bg_ticket_status_used;
                textColorRes = R.color.ticket_used;
                statusLabel = "Used";
            } else {
                backgroundRes = R.drawable.bg_ticket_status_expired;
                textColorRes = R.color.ticket_expired;
                statusLabel = "Expired";
            }
            binding.textTicketStatus.setBackground(
                    ContextCompat.getDrawable(binding.getRoot().getContext(), backgroundRes)
            );
            binding.textTicketStatus.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), textColorRes)
            );
            binding.textTicketStatus.setText(statusLabel);
        }
    }
}
