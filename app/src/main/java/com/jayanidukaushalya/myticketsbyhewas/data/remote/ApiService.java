package com.jayanidukaushalya.myticketsbyhewas.data.remote;

import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiListData;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ApiResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseRequest;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * List events with optional server-side pagination, search and filters.
     *
     * @param search       free-text search term
     * @param page         1-based page number (default 1)
     * @param limit        page size (default 8 per API)
     * @param eventType    e.g. "conference", "festival"
     * @param scheduleType e.g. "single_day", "multi_day"
     * @param dateFrom     ISO-8601 start date filter
     * @param dateTo       ISO-8601 end date filter
     * @param field        sort field name
     * @param order        "asc" or "desc"
     */
    @GET("events")
    Call<ApiResponse<ApiListData<Event>>> getEvents(
            @Query("search") String search,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("eventType") java.util.List<String> eventType,
            @Query("scheduleType") String scheduleType,
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("field") String field,
            @Query("order") String order
    );

    @GET("events/{id}")
    Call<ApiResponse<Event>> getEventById(@Path("id") String eventId);

    @GET("tickets/validate/{ticketId}")
    Call<Ticket> validateTicket(
            @Path("ticketId") String ticketId,
            @Header("Authorization") String authToken
    );

    @GET("tickets/user/{userId}")
    Call<ApiResponse<ApiListData<Ticket>>> getUserTickets(
            @Path("userId") String userId,
            @Header("Authorization") String authToken
    );

    @POST("tickets/purchase")
    Call<Ticket> purchaseTicket(
            @Body PurchaseRequest request,
            @Header("Authorization") String authToken
    );
}
