package com.jayanidukaushalya.myticketsbyhewas.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Android implementation of TokenStorage using EncryptedSharedPreferences.
 * Uses Android Keystore for secure token storage with automatic encryption.
 */
public class AndroidTokenStorage implements TokenStorage {

    private static final String PREFS_FILE_NAME = "auth_token_prefs";
    private static final String TOKEN_KEY = "firebase_id_token";

    private final SharedPreferences sharedPreferences;

    /**
     * Creates an AndroidTokenStorage instance with encrypted storage.
     *
     * @param context Application context
     * @throws TokenStorageException if initialization fails
     */
    public AndroidTokenStorage(Context context) throws TokenStorageException {
        try {
            // Create or retrieve the master key for encryption
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create encrypted shared preferences
            this.sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new TokenStorageException("Failed to initialize secure storage", e);
        }
    }

    @Override
    public void saveToken(String token) throws TokenStorageException {
        if (token == null || token.isEmpty()) {
            throw new TokenStorageException("Token cannot be null or empty");
        }

        try {
            sharedPreferences.edit()
                    .putString(TOKEN_KEY, token)
                    .apply();
        } catch (Exception e) {
            throw new TokenStorageException("Failed to save token", e);
        }
    }

    @Override
    public String getToken() throws TokenStorageException {
        try {
            return sharedPreferences.getString(TOKEN_KEY, null);
        } catch (Exception e) {
            throw new TokenStorageException("Failed to retrieve token", e);
        }
    }

    @Override
    public void clearToken() throws TokenStorageException {
        try {
            sharedPreferences.edit()
                    .remove(TOKEN_KEY)
                    .apply();
        } catch (Exception e) {
            throw new TokenStorageException("Failed to clear token", e);
        }
    }
}
