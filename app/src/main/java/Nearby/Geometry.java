package Nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

 public class Geometry {

    @SerializedName("location")
    @Expose
    private Nearby.Location location;

    public Nearby.Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
