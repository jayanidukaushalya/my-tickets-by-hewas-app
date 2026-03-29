package com.jayanidukaushalya.myticketsbyhewas.data.service;

import com.google.firebase.auth.FirebaseUser;

/**
 * Authentication service interface for Firebase Authentication operations.
 * Provides methods for user signup, signin, password reset, and session management.
 */
public interface AuthService {

    /**
     * Sign up a new user with email and password.
     *
     * @param email User's email address
     * @param password User's password (minimum 8 characters)
     * @param callback Callback to handle success or failure
     */
    void signUpWithEmail(String email, String password, AuthCallback callback);

    /**
     * Sign in an existing user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @param callback Callback to handle success or failure
     */
    void signInWithEmail(String email, String password, AuthCallback callback);

    /**
     * Sign in with Google OAuth.
     *
     * @param idToken Google ID token obtained from Google Sign-In
     * @param callback Callback to handle success or failure
     */
    void signInWithGoogle(String idToken, AuthCallback callback);

    /**
     * Send password reset email to the specified email address.
     *
     * @param email User's email address
     * @param callback Callback to handle success or failure
     */
    void sendPasswordResetEmail(String email, PasswordResetCallback callback);

    /**
     * Get the currently authenticated user.
     *
     * @return FirebaseUser if authenticated, null otherwise
     */
    FirebaseUser getCurrentUser();

    /**
     * Sign out the current user.
     */
    void signOut();

    /**
     * Get the ID token for the current user with automatic refresh.
     *
     * @param callback Callback to handle token retrieval
     */
    void getIdToken(TokenCallback callback);

    /**
     * Callback interface for authentication operations.
     */
    interface AuthCallback {
        void onSuccess(AuthResult result);
        void onFailure(String errorMessage);
    }

    /**
     * Callback interface for password reset operations.
     */
    interface PasswordResetCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Callback interface for token retrieval.
     */
    interface TokenCallback {
        void onSuccess(String token);
        void onFailure(String errorMessage);
    }

    /**
     * Result object containing user and token information.
     */
    class AuthResult {
        private final FirebaseUser user;
        private final String token;

        public AuthResult(FirebaseUser user, String token) {
            this.user = user;
            this.token = token;
        }

        public FirebaseUser getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}
