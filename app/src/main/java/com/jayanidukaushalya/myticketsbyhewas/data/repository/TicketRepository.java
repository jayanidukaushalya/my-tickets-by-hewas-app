package com.jayanidukaushalya.myticketsbyhewas.data.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.jayanidukaushalya.myticketsbyhewas.data.local.AppDatabase;
import com.jayanidukaushalya.myticketsbyhewas.data.local.TicketDao;
import com.jayanidukaushalya.myticketsbyhewas.data.local.TicketEntity;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiListData;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketRepository {

    private final RetrofitClient retrofitClient;
    private final TicketDao ticketDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public TicketRepository(Context context) {
        retrofitClient = RetrofitClient.getInstance();
        ticketDao = AppDatabase.getInstance(context).ticketDao();
    }

    /**
     * Offline-first ticket list for a signed-in user.
     *
     * 1. Immediately posts the cached (Room) tickets to [ticketsData] so the UI
     *    renders instantly even without a network connection.
     * 2. Fires a background network sync against GET /tickets/purchase.
     * 3. On success, writes the fresh list to Room and re-posts to [ticketsData].
     * 4. On network failure the cached data from step 1 is still shown —
     *    [errorData] receives an informational message so the UI can optionally
     *    display a "last synced" hint.
     */
    public void syncAndLoadUserTickets(
            String userId,
            String authToken,
            MutableLiveData<List<Ticket>> ticketsData,
            MutableLiveData<String> errorData,
            @Nullable Runnable onComplete
    ) {
        // Step 1 – serve cache immediately so UI is never blank.
        ioExecutor.execute(() -> {
            List<TicketEntity> cached = ticketDao.getTicketsForUser(userId);
            List<Ticket> cachedTickets = new ArrayList<>();
            for (TicketEntity e : cached) {
                cachedTickets.add(e.toTicket());
            }
            if (!cachedTickets.isEmpty()) {
                ticketsData.postValue(cachedTickets);
            }
        });

        // Step 2 – network sync.
        retrofitClient.getApiService()
                .getUserTickets("Bearer " + authToken)
                .enqueue(new Callback<ApiResponse<ApiListData<Ticket>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ApiListData<Ticket>>> call,
                            Response<ApiResponse<ApiListData<Ticket>>> response
                    ) {
                        ApiResponse<ApiListData<Ticket>> body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                            List<Ticket> fresh = body.getData().getResults();
                            if (fresh == null) fresh = Collections.emptyList();

                            // Step 3 – write to Room and push fresh data to UI.
                            final List<Ticket> finalFresh = fresh;
                            ioExecutor.execute(() -> {
                                // Replace this user's tickets in Room.
                                ticketDao.deleteTicketsForUser(userId);
                                List<TicketEntity> entities = new ArrayList<>();
                                for (Ticket t : finalFresh) {
                                    TicketEntity entity = TicketEntity.fromTicket(t);
                                    // Ensure userId is stamped on every entity.
                                    if (entity.userId == null) entity.userId = userId;
                                    entities.add(entity);
                                }
                                if (!entities.isEmpty()) {
                                    ticketDao.upsertAll(entities);
                                }
                                ticketsData.postValue(finalFresh);
                                if (onComplete != null) onComplete.run();
                            });
                        } else {
                            String msg = (body != null && body.getError() != null)
                                    ? body.getError()
                                    : "Sync failed (code " + response.code() + "). Showing cached data.";
                            errorData.postValue(msg);
                            if (onComplete != null) onComplete.run();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ApiListData<Ticket>>> call, Throwable throwable) {
                        // Network offline – cached data already posted; just inform UI.
                        errorData.postValue("Offline – showing your last synced tickets.");
                        if (onComplete != null) onComplete.run();
                    }
                });
    }

    /**
     * Returns a single cached ticket by its orderLine id.
     * Runs on a background thread; call from a ViewModel/executor accordingly.
     */
    public void getTicketById(
            String ticketId,
            MutableLiveData<Ticket> ticketData
    ) {
        ioExecutor.execute(() -> {
            TicketEntity entity = ticketDao.getTicketById(ticketId);
            if (entity != null) {
                ticketData.postValue(entity.toTicket());
            }
        });
    }

    /**
     * Clears all locally cached tickets for a user. Call on sign-out so another
     * user signing in on the same device cannot see stale data.
     */
    public void clearCacheForUser(String userId) {
        ioExecutor.execute(() -> ticketDao.deleteTicketsForUser(userId));
    }

    public void validateTicket(
            String ticketId,
            String authToken,
            MutableLiveData<Ticket> ticketData,
            MutableLiveData<String> errorData
    ) {
        retrofitClient.getApiService()
                .validateTicket(ticketId, "Bearer " + authToken)
                .enqueue(new Callback<Ticket>() {
                    @Override
                    public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Ticket t = response.body();
                            ticketData.postValue(t);
                            // Keep local cache in sync.
                            if (t.getId() != null) {
                                ioExecutor.execute(() -> ticketDao.updateStatus(t.getId(), t.getStatus()));
                            }
                        } else {
                            errorData.postValue("Ticket not found. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Ticket> call, Throwable throwable) {
                        errorData.postValue(throwable.getMessage());
                    }
                });
    }
}
