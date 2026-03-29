package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Event {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String image;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("scheduleType")
    private String scheduleType;

    @SerializedName("languages")
    private String languages;

    @SerializedName("eventDates")
    private List<EventDate> eventDates;

    @SerializedName("location")
    private EventLocation location;

    // ── Convenience helpers ──────────────────────────────────────────────────

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public String getEventType() { return eventType; }
    public String getScheduleType() { return scheduleType; }
    public String getLanguages() { return languages; }
    public List<EventDate> getEventDates() { return eventDates; }
    public EventLocation getLocation() { return location; }

    /** Returns the ISO date string of the first event date, or null. */
    public String getFirstDate() {
        if (eventDates == null || eventDates.isEmpty()) return null;
        return eventDates.get(0).getDate();
    }

    /** Returns the startTime of the first time slot of the first date, or null. */
    public String getFirstStartTime() {
        if (eventDates == null || eventDates.isEmpty()) return null;
        List<EventTimeSlot> slots = eventDates.get(0).getTimeSlots();
        if (slots == null || slots.isEmpty()) return null;
        return slots.get(0).getStartTime();
    }

    /** Returns the venue name from the nested location, or null. */
    public String getVenueName() {
        return location != null ? location.getVenue() : null;
    }

    /** Returns the venue address from the nested location, or null. */
    public String getVenueAddress() {
        return location != null ? location.getAddress() : null;
    }

    /**
     * Returns the lowest ticket price across all dates/slots, or 0 if none.
     * The API returns price as a String (e.g. "50000").
     */
    public double getLowestPrice() {
        if (eventDates == null) return 0;
        double min = Double.MAX_VALUE;
        boolean found = false;
        for (EventDate date : eventDates) {
            if (date.getTimeSlots() == null) continue;
            for (EventTimeSlot slot : date.getTimeSlots()) {
                if (slot.getTickets() == null) continue;
                for (EventTicket ticket : slot.getTickets()) {
                    double p = ticket.getPriceAsDouble();
                    if (p < min) { min = p; found = true; }
                }
            }
        }
        return found ? min : 0;
    }

    public boolean isFree() { return getLowestPrice() <= 0; }
}
