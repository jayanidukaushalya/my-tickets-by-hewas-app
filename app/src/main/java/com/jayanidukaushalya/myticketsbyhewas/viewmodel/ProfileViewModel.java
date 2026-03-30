package com.jayanidukaushalya.myticketsbyhewas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClient;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClientImpl;
import com.jayanidukaushalya.myticketsbyhewas.data.repository.TicketRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final FirebaseAuth firebaseAuth;
    private final TicketRepository ticketRepository;
    private final CustomerApiClient customerApi;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Customer> profileCustomer = new MutableLiveData<>();
    private final MutableLiveData<List<Ticket>> userTickets = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileSaveSuccess = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        firebaseAuth = FirebaseAuth.getInstance();
        ticketRepository = new TicketRepository(application);
        customerApi = CustomerApiClientImpl.getInstance();
        currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    public void refreshAuthState() {
        currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    /** Loads customer profile from the API (firstName, lastName, email). */
    public void loadCustomerProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            profileCustomer.setValue(null);
            return;
        }
        user.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                profileCustomer.postValue(null);
                return;
            }
            customerApi.getCustomerByUid(token, new CustomerApiClient.CustomerCallback() {
                @Override
                public void onSuccess(Customer customer) {
                    profileCustomer.postValue(customer);
                }

                @Override
                public void onFailure(String message) {
                    profileCustomer.postValue(null);
                }
            });
        }).addOnFailureListener(e -> profileCustomer.postValue(null));
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
            ticketRepository.syncAndLoadUserTickets(
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

    /** Look up a single ticket from the local cache. Used by TicketDetailFragment. */
    public void loadCachedTicket(String ticketId, MutableLiveData<Ticket> ticketData) {
        ticketRepository.getTicketById(ticketId, ticketData);
    }

    public void updateCustomerProfile(String firstName, String lastName, String phone) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        isLoading.setValue(true);
        user.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                isLoading.postValue(false);
                errorMessage.postValue(getApplication().getString(R.string.label_error));
                return;
            }
            customerApi.updateCustomerProfile(token, firstName != null ? firstName.trim() : "",
                    lastName != null ? lastName.trim() : "",
                    phone != null ? phone.trim() : "",
                    new CustomerApiClient.CustomerCallback() {
                        @Override
                        public void onSuccess(Customer customer) {
                            profileCustomer.postValue(customer);
                            isLoading.postValue(false);
                            profileSaveSuccess.postValue(true);
                        }

                        @Override
                        public void onFailure(String message) {
                            isLoading.postValue(false);
                            errorMessage.postValue(message);
                        }
                    });
        }).addOnFailureListener(e -> {
            isLoading.postValue(false);
            errorMessage.postValue(e.getMessage());
        });
    }

    public void signOut() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            ticketRepository.clearCacheForUser(user.getUid());
        }
        firebaseAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getApplication().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(getApplication(), gso).signOut();
        currentUser.setValue(null);
        userTickets.setValue(null);
        profileCustomer.setValue(null);
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void consumeProfileSaveSuccess() {
        profileSaveSuccess.setValue(null);
    }

    public LiveData<Boolean> getProfileSaveSuccess() {
        return profileSaveSuccess;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Customer> getProfileCustomer() {
        return profileCustomer;
    }

    public LiveData<List<Ticket>> getUserTickets() {
        return userTickets;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}
