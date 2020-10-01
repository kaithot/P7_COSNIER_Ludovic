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
    public static Task<Void> createRestaurant(String restId, String restName, String address) {
        Restaurant restaurantToCreate = new Restaurant(restName, address);
        return RestHelper.getRestaurantsCollection().document(restId).set(restaurantToCreate);
    }

    // --- GET ---
    public static Task<DocumentSnapshot> getRestaurant(String restId){
        return RestHelper.getRestaurantsCollection().document(restId).get();
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
