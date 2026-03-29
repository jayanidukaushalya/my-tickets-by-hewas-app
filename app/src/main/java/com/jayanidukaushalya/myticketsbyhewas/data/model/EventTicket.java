package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

public class EventTicket {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private String price;

    @SerializedName("qty")
    private int qty;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getQty() { return qty; }

    /** Returns numeric price value, or 0 if null/unparseable. */
    public double getPriceAsDouble() {
        if (price == null) return 0;
        try { return Double.parseDouble(price); } catch (NumberFormatException e) { return 0; }
    }
}
