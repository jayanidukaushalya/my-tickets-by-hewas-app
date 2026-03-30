package com.jayanidukaushalya.myticketsbyhewas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.repository.TicketRepository;

public class AdminViewModel extends AndroidViewModel {

    private final TicketRepository ticketRepository;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<Ticket> validatedTicket = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminViewModel(@NonNull Application application) {
        super(application);
        ticketRepository = new TicketRepository(application);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void validateTicket(String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            errorMessage.setValue("Please enter a ticket ID.");
            return;
        }
        isLoading.setValue(true);
        errorMessage.setValue(null);
        validatedTicket.setValue(null);
        if (firebaseAuth.getCurrentUser() == null) {
            errorMessage.setValue("Admin must be logged in to validate tickets.");
            isLoading.setValue(false);
            return;
        }
        firebaseAuth.getCurrentUser().getIdToken(false)
                .addOnSuccessListener(result -> {
                    ticketRepository.validateTicket(
                            ticketId.trim(),
                            result.getToken(),
                            validatedTicket,
                            errorMessage
                    );
                    validatedTicket.observeForever(ticket -> isLoading.postValue(false));
                    errorMessage.observeForever(error -> {
                        if (error != null) isLoading.postValue(false);
                    });
                })
                .addOnFailureListener(e -> {
                    isLoading.postValue(false);
                    errorMessage.postValue(e.getMessage());
                });
    }

    public void clearResult() {
        validatedTicket.setValue(null);
        errorMessage.setValue(null);
    }

    public LiveData<Ticket> getValidatedTicket() { return validatedTicket; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
