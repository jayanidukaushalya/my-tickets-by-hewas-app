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
import com.google.firebase.auth.FirebaseUser;
import com.jayanidukaushalya.myticketsbyhewas.R;
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
    }

    private void renderAuthState(FirebaseUser user) {
        if (user == null) {
            binding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
            binding.layoutLoggedIn.setVisibility(View.GONE);
        } else {
            binding.layoutNotLoggedIn.setVisibility(View.GONE);
            binding.layoutLoggedIn.setVisibility(View.VISIBLE);
            binding.textUserName.setText(
                    user.getDisplayName() != null ? user.getDisplayName() : "User"
            );
            binding.textUserEmail.setText(user.getEmail());
            viewModel.loadUserTickets();
        }
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
