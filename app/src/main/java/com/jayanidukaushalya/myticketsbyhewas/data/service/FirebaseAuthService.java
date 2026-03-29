package com.jayanidukaushalya.myticketsbyhewas.data.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Firebase implementation of the AuthService interface.
 * Handles all Firebase Authentication operations including email/password and Google OAuth.
 */
public class FirebaseAuthService implements AuthService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                // Get ID token after successful signup
                                user.getIdToken(false)
                                        .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> tokenTask) {
                                                if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                                    String token = tokenTask.getResult().getToken();
                                                    callback.onSuccess(new AuthService.AuthResult(user, token));
                                                } else {
                                                    callback.onFailure(translateError(tokenTask.getException()));
                                                }
                                            }
                                        });
                            } else {
                                callback.onFailure("User creation failed");
                            }
                        } else {
                            callback.onFailure(translateError(task.getException()));
                        }
                    }
                });
    }

    @Override
    public void signInWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                // Get ID token after successful sign-in
                                user.getIdToken(false)
                                        .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> tokenTask) {
                                                if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                                    String token = tokenTask.getResult().getToken();
                                                    callback.onSuccess(new AuthService.AuthResult(user, token));
                                                } else {
                                                    callback.onFailure(translateError(tokenTask.getException()));
                                                }
                                            }
                                        });
                            } else {
                                callback.onFailure("Sign in failed");
                            }
                        } else {
                            callback.onFailure(translateError(task.getException()));
                        }
                    }
                });
    }

    @Override
    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                // Get Firebase ID token after successful Google sign-in
                                user.getIdToken(false)
                                        .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> tokenTask) {
                                                if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                                    String token = tokenTask.getResult().getToken();
                                                    callback.onSuccess(new AuthService.AuthResult(user, token));
                                                } else {
                                                    callback.onFailure(translateError(tokenTask.getException()));
                                                }
                                            }
                                        });
                            } else {
                                callback.onFailure("Google sign in failed");
                            }
                        } else {
                            callback.onFailure(translateError(task.getException()));
                        }
                    }
                });
    }

    @Override
    public void sendPasswordResetEmail(String email, PasswordResetCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(translateError(task.getException()));
                        }
                    }
                });
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    @Override
    public void signOut() {
        firebaseAuth.signOut();
    }

    @Override
    public void getIdToken(TokenCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onFailure("No authenticated user");
            return;
        }

        // Force refresh to ensure token is valid
        user.getIdToken(false)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult().getToken();
                            if (token != null) {
                                callback.onSuccess(token);
                            } else {
                                callback.onFailure("Token is null");
                            }
                        } else {
                            callback.onFailure(translateError(task.getException()));
                        }
                    }
                });
    }

    /**
     * Translates Firebase error codes to user-friendly messages.
     *
     * @param exception The exception from Firebase
     * @return User-friendly error message
     */
    private String translateError(Exception exception) {
        if (exception == null) {
            return "An unknown error occurred";
        }

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            switch (errorCode) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "This email is already registered. Please sign in instead.";
                case "ERROR_INVALID_EMAIL":
                    return "Please enter a valid email address.";
                case "ERROR_WEAK_PASSWORD":
                    return "Password must be at least 8 characters long.";
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with this email.";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect email or password.";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Too many failed attempts. Please try again later.";
                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "Network error. Please check your connection.";
                case "ERROR_USER_DISABLED":
                    return "This account has been disabled.";
                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "This sign-in method is not enabled.";
                default:
                    return "Authentication failed: " + authException.getMessage();
            }
        }

        return exception.getMessage() != null ? exception.getMessage() : "An error occurred";
    }
}
