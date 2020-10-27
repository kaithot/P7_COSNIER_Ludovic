package com.ludovic.go4lunch;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Ludovic Cosnier 10/10/2020
 */
public class Myapplication extends Application {
    private  Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void onCreate() {
        super.onCreate();

    }
}
