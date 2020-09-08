package com.ludovic.go4lunch.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Restaurant {
    private String restName;
    private Date dateCreated;
    private String address;

    private List<String> clientsTodayList;

    public Restaurant() {}

    public Restaurant(String restName, String address) {
        this.restName = restName;
        this.clientsTodayList = new ArrayList<>();
        this.address = address;
    }

    // --- GETTERS ---
    public String getRestName() { return restName; }
    @ServerTimestamp
    public Date getDateCreated() { return dateCreated; }
    public List<String> getClientsTodayList() { return clientsTodayList; }
    public String getAddress() { return address;}

    // --- SETTERS ---
    public void setRestName(String restName) { this.restName = restName; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setClientsTodayList(List<String> clientsTodayList) { this.clientsTodayList = clientsTodayList; }
    public void setAddress(String address) {this.address = address;}
}
