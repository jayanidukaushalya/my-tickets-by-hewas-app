package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.view.LayoutInflater;
import android.view.View;
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
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.listener = listener;
    }

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
        return new TicketViewHolder(binding, listener);
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
        private final OnTicketClickListener listener;

        TicketViewHolder(ItemTicketCardBinding binding, OnTicketClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Ticket ticket) {
            binding.getRoot().setOnClickListener(v -> {
                // Already used tickets should not be actionable.
                if (ticket.isUsed()) return;
                if (listener != null) listener.onTicketClick(ticket);
            });
            binding.getRoot().setEnabled(!ticket.isUsed());
            binding.getRoot().setAlpha(ticket.isUsed() ? 0.6f : 1f);
            binding.textEventTitle.setText(ticket.getEventTitle());
            binding.textEventDate.setText(ticket.getEventDate());
            
            // Display ticket name (e.g., Regular, VIP) if available
            if (ticket.getTicketName() != null && !ticket.getTicketName().isEmpty()) {
                binding.textTicketName.setVisibility(View.VISIBLE);
                binding.textTicketName.setText(ticket.getTicketName());
            } else {
                binding.textTicketName.setVisibility(View.GONE);
            }
            
            if (ticket.getPurchaseDate() != null) {
                binding.textPurchaseDate.setText(
                        binding.getRoot().getContext().getString(
                                R.string.label_purchased_on, ticket.getPurchaseDate()
                        )
                );
            }
            String pricePerTicket = binding.getRoot().getContext().getString(R.string.format_price, ticket.getPrice());
            binding.textTicketQty.setText((ticket.getQty() == 1 ? "1 Ticket" : ticket.getQty() + " Tickets") + " | " + pricePerTicket);
            
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
                binding.textTicketStatus.setVisibility(View.GONE);
                return;
            }

            binding.textTicketStatus.setVisibility(View.VISIBLE);
            if (ticket.isUsed()) {
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
