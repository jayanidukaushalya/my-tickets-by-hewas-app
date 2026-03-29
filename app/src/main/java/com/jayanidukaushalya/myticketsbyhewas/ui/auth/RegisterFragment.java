package com.jayanidukaushalya.myticketsbyhewas.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        binding.buttonRegister.setOnClickListener(v -> attemptRegister());
        binding.textLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void attemptRegister() {
        String email = getEmail();
        String password = getPassword();
        String confirmPassword = getConfirmPassword();

        if (!isEmailValid(email)) {
            binding.inputLayoutEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!isPasswordValid(password)) {
            binding.inputLayoutPassword.setError(getString(R.string.error_password_too_short));
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError("Passwords do not match");
            return;
        }

        clearErrors();
        showLoading(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    showLoading(false);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError(e.getMessage() != null ? e.getMessage() : "Registration failed");
                });
    }

    private void navigateToLogin() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private String getEmail() {
        return binding.editEmail.getText() != null
                ? binding.editEmail.getText().toString().trim()
                : "";
    }

    private String getPassword() {
        return binding.editPassword.getText() != null
                ? binding.editPassword.getText().toString()
                : "";
    }

    private String getConfirmPassword() {
        return binding.editConfirmPassword.getText() != null
                ? binding.editConfirmPassword.getText().toString()
                : "";
    }

    private boolean isEmailValid(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void clearErrors() {
        binding.inputLayoutEmail.setError(null);
        binding.inputLayoutPassword.setError(null);
        binding.inputLayoutConfirmPassword.setError(null);
        binding.textError.setVisibility(View.GONE);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!isLoading);
    }

    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
