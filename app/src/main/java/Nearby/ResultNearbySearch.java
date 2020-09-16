package Nearby;

import android.provider.ContactsContract;

import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;

public class ResultNearbySearch {

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;
    @SerializedName("photos")
    @Expose
    private List<ContactsContract.CommonDataKinds.Photo> photos = null;
    @SerializedName("place_id")
    @Expose
    private String placeId;
    @SerializedName("plus_code")
    @Expose
    private PlusCode plusCode;
    @SerializedName("price_level")
    @Expose
    private Integer priceLevel;
    @SerializedName("rating")
    @Expose
    private Double rating;
    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("scope")
    @Expose
    private String scope;
    @SerializedName("types")
    @Expose
    private List<String> types = null;
    @SerializedName("user_ratings_total")
    @Expose
    private Integer userRatingsTotal;
    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    // --- GETTERS ----//
    public Geometry getGeometry() {
        return geometry;
    }
    public String getIcon() {
        return icon;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public OpeningHours getOpeningHours() {
        return openingHours;
    }
    public List<ContactsContract.CommonDataKinds.Photo> getPhotos() {
        return photos;
    }
    public String getPlaceId() {
        return placeId;
    }
    public PlusCode getPlusCode() {
        return plusCode;
    }
    public Integer getPriceLevel() {
        return priceLevel;
    }
    public Double getRating() {
        return rating;
    }
    public String getReference() {
        return reference;
    }
    public String getScope() {
        return scope;
    }
    public List<String> getTypes() {
        return types;
    }
    public Integer getUserRatingsTotal() {
        return userRatingsTotal;
    }
    public String getVicinity() {
        return vicinity;
    }
    // --- --- //

    // --- SETTERS --- //
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }
    public void setPhotos(List<ContactsContract.CommonDataKinds.Photo> photos) {
        this.photos = photos;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public void setPlusCode(PlusCode plusCode) {
        this.plusCode = plusCode;
    }
    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }
    public void setUserRatingsTotal(Integer userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
    // --- --- //
}

