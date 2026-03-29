package com.jayanidukaushalya.myticketsbyhewas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.repository.TicketRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final FirebaseAuth firebaseAuth;
    private final TicketRepository ticketRepository;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<Ticket>> userTickets = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        firebaseAuth = FirebaseAuth.getInstance();
        ticketRepository = new TicketRepository();
        currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    public void refreshAuthState() {
        currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    public void loadUserTickets() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("User not logged in.");
            return;
        }
        isLoading.setValue(true);
        user.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            ticketRepository.fetchUserTickets(
                    user.getUid(),
                    token,
                    userTickets,
                    errorMessage,
                    () -> isLoading.postValue(false)
            );
        }).addOnFailureListener(e -> {
            isLoading.postValue(false);
            errorMessage.postValue(e.getMessage());
        });
    }

    public void signOut() {
        firebaseAuth.signOut();
        currentUser.setValue(null);
        userTickets.setValue(null);
    }

    public LiveData<FirebaseUser> getCurrentUser() { return currentUser; }
    public LiveData<List<Ticket>> getUserTickets() { return userTickets; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}
