package com.ludovic.go4lunch.api;


import Nearby.ListDetailResult;
import Nearby.NearbyPlacesList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

        @GET("details/json?")
        Call<ListDetailResult> getRestaurantDetail(@Query("key") String apiKey,
                                                   @Query("placeid") String restaurantId,
                                                   @Query("fields") String fields);

        @GET("nearbysearch/json?")
        Call<NearbyPlacesList> getNearBy(@Query("location") String location,
                                         @Query("radius") int radius,
                                         @Query("type") String type,
                                         @Query("keyword") String keyword,
                                         @Query("key") String key);
}
