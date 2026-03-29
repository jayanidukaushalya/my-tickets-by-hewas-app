package com.jayanidukaushalya.myticketsbyhewas.data.service;

/**
 * Interface for secure token storage operations.
 * Implementations should use platform-specific secure storage mechanisms.
 */
public interface TokenStorage {

    /**
     * Save an authentication token securely.
     *
     * @param token The Firebase ID token to store
     * @throws TokenStorageException if storage operation fails
     */
    void saveToken(String token) throws TokenStorageException;

    /**
     * Retrieve the stored authentication token.
     *
     * @return The stored token, or null if no token is stored
     * @throws TokenStorageException if retrieval operation fails
     */
    String getToken() throws TokenStorageException;

    /**
     * Clear the stored authentication token.
     *
     * @throws TokenStorageException if clear operation fails
     */
    void clearToken() throws TokenStorageException;

    /**
     * Exception thrown when token storage operations fail.
     */
    class TokenStorageException extends Exception {
        public TokenStorageException(String message) {
            super(message);
        }

        public TokenStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
