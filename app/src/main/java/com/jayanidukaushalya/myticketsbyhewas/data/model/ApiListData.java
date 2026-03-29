package com.jayanidukaushalya.myticketsbyhewas.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Standard API shape for list responses: {@code data: { results, total }}.
 */
public class ApiListData<T> {

    @SerializedName("results")
    private List<T> results;

    @SerializedName("total")
    private int total;

    public List<T> getResults() {
        return results;
    }

    public int getTotal() {
        return total;
    }
}
