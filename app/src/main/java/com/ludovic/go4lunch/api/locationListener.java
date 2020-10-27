package com.ludovic.go4lunch.api;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public interface locationListener {

    public void mapReady();
    public void setMap(GoogleMap map);
    public void userLatLng(LatLng userLatLng);
    public void launchRestaurantDetail(Marker marker);
}
