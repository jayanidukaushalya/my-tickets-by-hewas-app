package com.jayanidukaushalya.myticketsbyhewas.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TicketDao {

    /** Replace all cached tickets for a user (full sync). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<TicketEntity> tickets);

    /** Return all cached tickets for a given user, newest purchase first. */
    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY purchaseDate DESC")
    List<TicketEntity> getTicketsForUser(String userId);

    /** Return a single cached ticket by its orderLine id. */
    @Query("SELECT * FROM tickets WHERE id = :ticketId LIMIT 1")
    TicketEntity getTicketById(String ticketId);

    /** Remove all cached tickets for a user (used on sign-out). */
    @Query("DELETE FROM tickets WHERE userId = :userId")
    void deleteTicketsForUser(String userId);

    /** Update a single ticket's status in the cache (e.g. marked used after validation). */
    @Query("UPDATE tickets SET status = :status WHERE id = :ticketId")
    void updateStatus(String ticketId, String status);
}
