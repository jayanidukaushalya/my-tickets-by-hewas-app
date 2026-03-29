package com.jayanidukaushalya.myticketsbyhewas.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jayanidukaushalya.myticketsbyhewas.databinding.ActivityAuthBinding;

import java.util.function.Consumer;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private Consumer<ActivityResult> googleSignInResultConsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (googleSignInResultConsumer != null) {
                        googleSignInResultConsumer.accept(result);
                    }
                }
        );
    }

    public void launchGoogleSignIn(@NonNull Intent signInIntent, @NonNull Consumer<ActivityResult> onResult) {
        googleSignInResultConsumer = onResult;
        googleSignInLauncher.launch(signInIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleSignInResultConsumer = null;
        binding = null;
    }
}
