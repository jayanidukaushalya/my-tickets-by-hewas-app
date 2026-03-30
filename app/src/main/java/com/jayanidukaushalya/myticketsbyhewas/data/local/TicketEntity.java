package com.jayanidukaushalya.myticketsbyhewas.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jayanidukaushalya.myticketsbyhewas.data.model.Ticket;

/**
 * Room entity that mirrors the fields returned by GET /tickets/purchase.
 * Keyed by orderLine id, scoped to a single user (userId).
 */
@Entity(tableName = "tickets")
public class TicketEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String userId;
    public String eventId;
    public String eventTitle;
    public String eventDate;
    public String eventImageUrl;
    public String ticketName;
    public String purchaseDate;
    public String qrCode;
    public String status;
    public double price;
    public int qty;
    public String eventLocation;

    /** Convert to the Ticket model used by the rest of the app. */
    public Ticket toTicket() {
        Ticket t = new Ticket();
        t.setId(id);
        t.setEventId(eventId);
        t.setEventTitle(eventTitle);
        t.setEventDate(eventDate);
        t.setEventImageUrl(eventImageUrl);
        t.setUserId(userId);
        t.setTicketName(ticketName);
        t.setPurchaseDate(purchaseDate);
        t.setQrCode(qrCode);
        t.setStatus(status);
        t.setPrice(price);
        t.setQty(qty);
        t.setEventLocation(eventLocation);
        return t;
    }

    /** Build from the network Ticket model. */
    public static TicketEntity fromTicket(Ticket t) {
        TicketEntity e = new TicketEntity();
        e.id = t.getId() != null ? t.getId() : "";
        e.userId = t.getUserId();
        e.eventId = t.getEventId();
        e.eventTitle = t.getEventTitle();
        e.eventDate = t.getEventDate();
        e.eventImageUrl = t.getEventImageUrl();
        e.ticketName = t.getTicketName();
        e.purchaseDate = t.getPurchaseDate();
        e.qrCode = t.getQrCode();
        e.status = t.getStatus();
        e.price = t.getPrice();
        e.qty = t.getQty();
        e.eventLocation = t.getEventLocation();
        return e;
    }
}
