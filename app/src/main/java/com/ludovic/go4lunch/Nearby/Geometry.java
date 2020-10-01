package com.ludovic.go4lunch.Nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

 public class Geometry {

    @SerializedName("location")
    @Expose
    private com.ludovic.go4lunch.Nearby.Location location;

    public com.ludovic.go4lunch.Nearby.Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
