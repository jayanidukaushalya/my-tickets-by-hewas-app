package com.jayanidukaushalya.myticketsbyhewas.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentTicketDetailBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.ProfileViewModel;

public class TicketDetailFragment extends Fragment {

    private FragmentTicketDetailBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTicketDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Share the same ViewModel instance as ProfileFragment so the cache is warm.
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        loadTicketData();
    }

    private void loadTicketData() {
        Bundle args = getArguments();
        if (args == null) return;

        String ticketId = args.getString("ticketId");

        // Fast path: ProfileFragment passes the full snapshot when navigating from the list.
        boolean hasSnapshot =
                args.getString("eventTitle") != null
                        && args.getString("eventDate") != null
                        && args.getString("purchaseDate") != null
                        && args.containsKey("qty")
                        && args.containsKey("price");

        if (hasSnapshot) {
            renderTicketUiFromArgs(args);
            return;
        }

        // Slow path (e.g. from Checkout where only ticketId is passed):
        // look up the ticket in the local Room cache via ProfileViewModel.
        if (ticketId == null || ticketId.isEmpty()) return;

        MutableLiveData<Ticket> ticketData = new MutableLiveData<>();
        ticketData.observe(getViewLifecycleOwner(), ticket -> {
            if (ticket != null) {
                renderTicketUiFromTicket(ticket);
            } else {
                Snackbar.make(binding.getRoot(),
                        "Ticket details not available offline. Please sync.",
                        Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.loadCachedTicket(ticketId, ticketData);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void renderTicketUiFromArgs(Bundle args) {
        renderTicketUi(
                args.getString("eventId"),
                args.getString("eventTitle"),
                args.getString("eventDate"),
                args.getString("purchaseDate"),
                args.getString("status"),
                args.getString("ticketName"),
                args.getString("eventLocation"),
                args.containsKey("price") ? args.getDouble("price") : 0.0,
                args.containsKey("qty") ? args.getInt("qty") : 0
        );
    }

    private void renderTicketUiFromTicket(Ticket ticket) {
        renderTicketUi(
                ticket.getEventId(),
                ticket.getEventTitle(),
                ticket.getEventDate(),
                ticket.getPurchaseDate(),
                ticket.getStatus(),
                ticket.getTicketName(),
                ticket.getEventLocation(),
                ticket.getPrice(),
                ticket.getQty()
        );
    }

    private void renderTicketUi(
            @Nullable String eventId,
            @Nullable String eventTitle,
            @Nullable String eventDate,
            @Nullable String purchaseDate,
            @Nullable String status,
            @Nullable String ticketName,
            @Nullable String eventLocation,
            double price,
            int qty
    ) {
        binding.textEventTitle.setText(eventTitle != null ? eventTitle : "");
        binding.textEventDate.setText(eventDate != null ? eventDate : "");
        binding.textPurchaseDate.setText(purchaseDate != null ? purchaseDate : "");
        binding.textTicketQty.setText(qty + " Tickets Purchased");
        binding.textTicketPrice.setText(getString(R.string.format_price, price * qty));

        if (ticketName != null && !ticketName.isEmpty()) {
            binding.textTicketName.setVisibility(View.VISIBLE);
            binding.textTicketName.setText(ticketName);
        } else {
            binding.textTicketName.setVisibility(View.GONE);
        }

        if (eventLocation != null && !eventLocation.isEmpty()) {
            binding.layoutLocation.setVisibility(View.VISIBLE);
            binding.textEventLocation.setText(eventLocation);
        } else {
            binding.layoutLocation.setVisibility(View.GONE);
        }

        // White square QR placeholder.
        binding.imageQrCode.setImageDrawable(null);

        applyStatusStyle(status != null ? status : Ticket.STATUS_EXPIRED);

        if (eventLocation != null && !eventLocation.isEmpty()) {
            binding.buttonOpenInMaps.setVisibility(View.VISIBLE);
            binding.buttonOpenInMaps.setOnClickListener(v -> {
                String uriString = "geo:0,0?q=" + Uri.encode(eventLocation);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
                    intent.setPackage(null);
                }
                startActivity(intent);
            });
        } else {
            binding.buttonOpenInMaps.setVisibility(View.GONE);
            binding.buttonOpenInMaps.setOnClickListener(null);
        }

        if (eventId != null && !eventId.isEmpty()) {
            binding.buttonViewEvent.setOnClickListener(v -> {
                Bundle navArgs = new Bundle();
                navArgs.putString("eventId", eventId);
                NavHostFragment.findNavController(this).navigate(R.id.nav_event_detail, navArgs);
            });
        } else {
            binding.buttonViewEvent.setOnClickListener(null);
        }
    }

    private void applyStatusStyle(String status) {
        if (Ticket.STATUS_VALID.equals(status)) {
            binding.textTicketStatus.setVisibility(View.GONE);
            return;
        }

        binding.textTicketStatus.setVisibility(View.VISIBLE);

        int backgroundRes;
        int textColorRes;
        String statusLabel;

        if (Ticket.STATUS_USED.equals(status)) {
            backgroundRes = R.drawable.bg_ticket_status_used;
            textColorRes = R.color.ticket_used;
            statusLabel = "Used";
        } else {
            backgroundRes = R.drawable.bg_ticket_status_expired;
            textColorRes = R.color.ticket_expired;
            statusLabel = "Expired";
        }

        binding.textTicketStatus.setBackground(
                ContextCompat.getDrawable(requireContext(), backgroundRes));
        binding.textTicketStatus.setTextColor(
                ContextCompat.getColor(requireContext(), textColorRes));
        binding.textTicketStatus.setText(statusLabel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
