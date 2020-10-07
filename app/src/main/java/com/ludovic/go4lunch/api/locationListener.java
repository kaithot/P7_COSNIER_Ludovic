package com.ludovic.go4lunch.api;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;


public interface locationListener {

    public void mapReady();
    public void setMap(GoogleMap map);
    public void userLatLng(LatLng userLatLng);
}
