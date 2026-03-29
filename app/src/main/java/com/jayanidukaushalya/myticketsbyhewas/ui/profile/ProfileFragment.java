package com.jayanidukaushalya.myticketsbyhewas.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.databinding.DialogEditProfileBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentProfileBinding;
import com.jayanidukaushalya.myticketsbyhewas.ui.auth.AuthActivity;
import com.jayanidukaushalya.myticketsbyhewas.ui.events.TicketAdapter;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private TicketAdapter ticketAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        setupRecyclerView();
        observeViewModel();
        binding.buttonSignIn.setOnClickListener(v -> openAuthActivity());
        binding.buttonSignOut.setOnClickListener(v -> viewModel.signOut());
        binding.buttonBrowseEvents.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_events);
            }
        });
        binding.buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshAuthState();
    }

    private void setupRecyclerView() {
        ticketAdapter = new TicketAdapter();
        binding.recyclerTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTickets.setAdapter(ticketAdapter);
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::renderAuthState);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
        viewModel.getProfileCustomer().observe(getViewLifecycleOwner(), customer ->
                renderProfileDetails(customer, viewModel.getCurrentUser().getValue())
        );
        viewModel.getUserTickets().observe(getViewLifecycleOwner(), tickets -> {
            if (tickets == null || tickets.isEmpty()) {
                binding.layoutNoTickets.setVisibility(View.VISIBLE);
                binding.recyclerTickets.setVisibility(View.GONE);
            } else {
                binding.layoutNoTickets.setVisibility(View.GONE);
                binding.recyclerTickets.setVisibility(View.VISIBLE);
                ticketAdapter.submitList(tickets);
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
        viewModel.getProfileSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Snackbar.make(binding.getRoot(), R.string.label_profile_updated, Snackbar.LENGTH_SHORT).show();
                viewModel.consumeProfileSaveSuccess();
            }
        });
    }

    private void renderAuthState(FirebaseUser user) {
        if (user == null) {
            binding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
            binding.layoutLoggedIn.setVisibility(View.GONE);
        } else {
            binding.layoutNotLoggedIn.setVisibility(View.GONE);
            binding.layoutLoggedIn.setVisibility(View.VISIBLE);
            viewModel.loadCustomerProfile();
            viewModel.loadUserTickets();
            renderProfileDetails(viewModel.getProfileCustomer().getValue(), user);
        }
    }

    private void renderProfileDetails(@Nullable Customer customer, @Nullable FirebaseUser user) {
        if (user == null) {
            return;
        }
        binding.textUserName.setText(formatDisplayName(customer, user));
        String email = customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()
                ? customer.getEmail()
                : (user.getEmail() != null ? user.getEmail() : "");
        binding.textUserEmail.setText(email);
    }

    @NonNull
    private String formatDisplayName(@Nullable Customer customer, @NonNull FirebaseUser user) {
        if (customer != null) {
            String fn = customer.getFirstName() != null ? customer.getFirstName().trim() : "";
            String ln = customer.getLastName() != null ? customer.getLastName().trim() : "";
            String combined = (fn + " " + ln).trim();
            if (!combined.isEmpty()) {
                return combined;
            }
        }
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        return getString(R.string.label_profile_placeholder_name);
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());
        Customer c = viewModel.getProfileCustomer().getValue();
        if (c != null) {
            if (c.getFirstName() != null) {
                dialogBinding.editFirstName.setText(c.getFirstName());
            }
            if (c.getLastName() != null) {
                dialogBinding.editLastName.setText(c.getLastName());
            }
            if (c.getPhone() != null) {
                dialogBinding.editPhone.setText(c.getPhone());
            }
        } else {
            FirebaseUser u = viewModel.getCurrentUser().getValue();
            if (u != null && u.getDisplayName() != null && !u.getDisplayName().isEmpty()) {
                String[] parts = u.getDisplayName().trim().split("\\s+", 2);
                dialogBinding.editFirstName.setText(parts[0]);
                if (parts.length > 1) {
                    dialogBinding.editLastName.setText(parts[1]);
                }
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_edit_profile)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save_profile, (d, w) -> {
                    String fn = dialogBinding.editFirstName.getText() != null
                            ? dialogBinding.editFirstName.getText().toString() : "";
                    String ln = dialogBinding.editLastName.getText() != null
                            ? dialogBinding.editLastName.getText().toString() : "";
                    String phone = dialogBinding.editPhone.getText() != null
                            ? dialogBinding.editPhone.getText().toString() : "";
                    viewModel.updateCustomerProfile(fn, ln, phone);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void openAuthActivity() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
