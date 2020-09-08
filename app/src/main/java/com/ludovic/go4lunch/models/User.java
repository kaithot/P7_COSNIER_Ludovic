package com.ludovic.go4lunch.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class User {

    private String uid;
    private String username;
    private String userEmail;
    private String restToday;
    private String restTodayName;
    private String restDate;
    @Nullable
    private String urlPicture;
    private List<String> like;


    public User() { }

    public User(String uid, String username, String userEmail, String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.userEmail = userEmail;
        this.restToday = "";
        this.restTodayName = "";
        this.restDate = "";
        this.urlPicture = urlPicture;
        this.like = new ArrayList<>();

    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getUserEmail() { return  userEmail;}
    public String getRestToday() { return restToday;}
    public String getRestTodayName() {return restTodayName;}
    public String getRestDate() {return restDate;}
    public String getUrlPicture() { return urlPicture; }
    public List<String> getLike() { return like; }

    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail;}
    public void setRestToday(String restToday) {this.restToday = restToday;}
    public void setRestTodayName(String restTodayName) {this.restTodayName = restTodayName;}
    public void setRestDate(String restDate) {this.restDate = restDate;}
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setLike(List<String> like) {this.like = like;}
}