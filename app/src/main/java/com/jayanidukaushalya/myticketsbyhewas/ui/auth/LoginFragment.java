package com.jayanidukaushalya.myticketsbyhewas.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        setupGoogleSignInClient();
        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.textForgotPassword.setOnClickListener(v -> sendPasswordReset());
        binding.buttonGoogleLogin.setOnClickListener(v -> startGoogleSignIn());
        binding.textRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void navigateToRegister() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_login_to_register);
    }

    private void setupGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void startGoogleSignIn() {
        if (!(requireActivity() instanceof AuthActivity)) {
            showError(getString(R.string.label_error));
            return;
        }
        showLoading(true);
        AuthActivity authActivity = (AuthActivity) requireActivity();
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            if (!isAdded() || binding == null) {
                return;
            }
            Intent signInIntent = googleSignInClient.getSignInIntent();
            authActivity.launchGoogleSignIn(signInIntent, this::onGoogleSignInActivityResult);
        });
    }

    private void onGoogleSignInActivityResult(ActivityResult result) {
        if (!isAdded() || binding == null) {
            return;
        }
        Intent data = result.getData();
        if (data == null) {
            showLoading(false);
            return;
        }
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleGoogleSignInResult(task);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        if (binding == null || !isAdded()) {
            return;
        }
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) {
                showLoading(false);
                showError(getString(R.string.error_login_failed));
                return;
            }
            String idToken = account.getIdToken();
            if (idToken == null || idToken.isEmpty()) {
                showLoading(false);
                showError(getString(R.string.error_login_failed));
                return;
            }
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            showLoading(false);
            if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                return;
            }
            showError(getString(R.string.error_login_failed));
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (!isAdded()) {
                        return;
                    }
                    showLoading(false);
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) {
                        return;
                    }
                    showLoading(false);
                    showError(getString(R.string.error_login_failed));
                });
    }

    private void attemptLogin() {
        String email = getEmail();
        String password = getPassword();
        if (!isEmailValid(email)) {
            binding.inputLayoutEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!isPasswordValid(password)) {
            binding.inputLayoutPassword.setError(getString(R.string.error_password_too_short));
            return;
        }
        clearErrors();
        showLoading(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (!isAdded()) {
                        return;
                    }
                    showLoading(false);
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) {
                        return;
                    }
                    showLoading(false);
                    showError(getString(R.string.error_login_failed));
                });
    }

    private void sendPasswordReset() {
        String email = getEmail();
        if (!isEmailValid(email)) {
            binding.inputLayoutEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email);
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

    private boolean isEmailValid(String email) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void clearErrors() {
        binding.inputLayoutEmail.setError(null);
        binding.inputLayoutPassword.setError(null);
        binding.textError.setVisibility(View.GONE);
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) {
            return;
        }
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!isLoading);
    }

    private void showError(String message) {
        if (binding == null) {
            return;
        }
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
