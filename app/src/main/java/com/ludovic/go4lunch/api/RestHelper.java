package com.ludovic.go4lunch.api;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ludovic.go4lunch.models.Restaurant;

import java.util.List;

public class RestHelper {

    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---
    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---
    public static Task<Void> createRestaurant(String restoId, String restoName, String address) {
        Restaurant restaurantToCreate = new Restaurant(restoName, address);
        return RestHelper.getRestaurantsCollection().document(restoId).set(restaurantToCreate);
    }

    // --- GET ---
    public static Task<DocumentSnapshot> getRestaurant(String restoId){
        return RestHelper.getRestaurantsCollection().document(restoId).get();
    }

    // --- UPDATE NAME---
    public static Task<Void> updateRestaurantName(String restaurantName, String restaurantId) {
        return RestHelper.getRestaurantsCollection().document(restaurantId).update("restaurantName", restaurantName);
    }

    // --- UPDATE TODAY'S USERS---
    public static Task<Void> updateClientsTodayList(List<String> clientsTodayList, String restaurantId) {
        return RestHelper.getRestaurantsCollection().document(restaurantId).update("clientsTodayList", clientsTodayList);
    }

}
