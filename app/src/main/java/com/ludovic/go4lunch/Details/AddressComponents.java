package com.ludovic.go4lunch.Details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressComponents {

    @SerializedName("long_name")
    @Expose
    private String longName;
    @SerializedName("short_name")
    @Expose
    private String shortName;
    @SerializedName("types")
    @Expose
    private List<String> types = null;

    // --- GETTERS ----//
    public String getLongName() {
        return longName;
    }
    public String getShortName() {
        return shortName;
    }
    public List<String> getTypes() {
        return types;
    }

    // --- SETTERS --- //
    public void setLongName(String longName) {
        this.longName = longName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }
}
