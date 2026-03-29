package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("events")
    Call<List<Event>> getAllEvents();

    @GET("events/{id}")
    Call<Event> getEventById(@Path("id") String eventId);

    @GET("tickets/validate/{ticketId}")
    Call<Ticket> validateTicket(
            @Path("ticketId") String ticketId,
            @Header("Authorization") String authToken
    );

    @GET("tickets/user/{userId}")
    Call<List<Ticket>> getUserTickets(
            @Path("userId") String userId,
            @Header("Authorization") String authToken
    );

    @POST("tickets/purchase")
    Call<Ticket> purchaseTicket(
            @Body PurchaseRequest request,
            @Header("Authorization") String authToken
    );
}
