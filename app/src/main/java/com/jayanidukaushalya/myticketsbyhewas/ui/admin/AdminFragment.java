package com.jayanidukaushalya.myticketsbyhewas.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentAdminBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.AdminViewModel;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    private AdminViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        observeViewModel();
        binding.buttonValidate.setOnClickListener(v -> validateTicket());
        binding.buttonScanQr.setOnClickListener(v -> onScanQrClicked());
        binding.editTicketId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateTicket();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonValidate.setEnabled(!isLoading);
        });
        viewModel.getValidatedTicket().observe(getViewLifecycleOwner(), ticket -> {
            if (ticket != null) showValidationResult(ticket);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) showValidationError(error);
        });
    }

    private void validateTicket() {
        String ticketId = binding.editTicketId.getText() != null
                ? binding.editTicketId.getText().toString()
                : "";
        viewModel.validateTicket(ticketId);
    }

    private void showValidationResult(Ticket ticket) {
        binding.textAwaiting.setVisibility(View.GONE);
        binding.cardResult.setVisibility(View.VISIBLE);
        if (ticket.isValid()) {
            binding.textResultStatus.setText(getString(R.string.label_ticket_valid));
            binding.textResultStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.ticket_valid)
            );
            binding.cardResult.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.tertiary_container)
            );
        } else if (ticket.isUsed()) {
            binding.textResultStatus.setText(getString(R.string.label_ticket_used));
            binding.textResultStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.ticket_used)
            );
            binding.cardResult.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.surface_variant)
            );
        } else {
            binding.textResultStatus.setText(getString(R.string.label_ticket_expired));
            binding.textResultStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.ticket_expired)
            );
            binding.cardResult.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.error_container)
            );
        }
        binding.textResultEvent.setText(ticket.getEventTitle());
        binding.textResultHolder.setText(ticket.getEventDate());
    }

    private void showValidationError(String error) {
        binding.textAwaiting.setVisibility(View.GONE);
        binding.cardResult.setVisibility(View.VISIBLE);
        binding.textResultStatus.setText(getString(R.string.label_ticket_not_found));
        binding.textResultStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.error)
        );
        binding.textResultEvent.setText(error);
        binding.textResultHolder.setText("");
        binding.cardResult.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.error_container)
        );
    }

    private void onScanQrClicked() {
        // QR scanner integration will be added later
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
