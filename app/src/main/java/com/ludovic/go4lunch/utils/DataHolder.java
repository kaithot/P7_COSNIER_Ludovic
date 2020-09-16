package com.ludovic.go4lunch.utils;

import java.util.List;

public class DataHolder {

    private String currentPosition;
    private double currentLat;
    private double currentLng;
    private String restaurantPosition;
    private int distance;
    private String placeId;
    private String restaurantId;
    private String restaurantName;
    private String userUid;
    private List<String> stringList;
    private String radius = "10000";
    private static final DataHolder ourInstance = new DataHolder();

    public static DataHolder getInstance() {
        return ourInstance;
    }

    private DataHolder() {
    }

    // --- GETTERS ----//
    public String getCurrentPosition() {
        return currentPosition;
    }
    public String getRestaurantPosition() {
        return restaurantPosition;
    }
    public String getPlaceId() {
        return placeId;
    }
    public List<String> getStringList() {
        return stringList;
    }
    public int getDistance() {
        return distance;
    }
    public String getRadius() {
        return radius;
    }
    public String getRestaurantId() {
        return restaurantId;
    }
    public double getCurrentLat() {
        return currentLat;
    }
    public double getCurrentLng() {
        return currentLng;
    }
    public String getUserUid() {
        return userUid;
    }
    public String getRestaurantName() {
        return restaurantName;
    }
    // --- --- //

    // --- SETTERS --- //
    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }
    public void setRestaurantPosition(String restaurantPosition) {
        this.restaurantPosition = restaurantPosition;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    public void setRadius(String radius) {
        this.radius = radius;
    }
    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }
    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    // --- --- //
}
