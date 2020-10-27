package com.ludovic.go4lunch.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.ludovic.go4lunch.Details.RestaurantResult;
import com.ludovic.go4lunch.LunchActivity;
import com.ludovic.go4lunch.R;
import com.ludovic.go4lunch.RestaurantInformation;
import com.ludovic.go4lunch.RestaurantsListAdapter;
import com.ludovic.go4lunch.api.ApiClient;
import com.ludovic.go4lunch.api.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import com.ludovic.go4lunch.Nearby.ListDetailResult;
import com.ludovic.go4lunch.Nearby.ResultNearbySearch;
import com.ludovic.go4lunch.api.locationListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ListFragment extends Fragment {

    public List<ResultNearbySearch> results;
    private RecyclerView mRecyclerView;

    private final String TAG = ListFragment.class.getSimpleName();
    private String PLACEIDRESTO = "resto_place_id";

    private RestaurantsListAdapter adapter;
    private RestaurantResult mRestaurant;
    private ArrayList<RestaurantResult> restaurantsList = new ArrayList<>();

    public locationListener myLocationListener;
    private LatLng myLatLng;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_list,  null);
        mRecyclerView = view.findViewById(R.id.fragment_restaurants_recyclerview);
        myLocationListener = (locationListener) getContext();
     if (results != null) {
         Log.e(TAG, "launch updateNearbyPlaces");
         updateNearbyPlaces(results);
     }
        return view;
    }

    public void updateNearbyPlaces(final List<ResultNearbySearch> googlePlacesResults) {

        myLocationListener.userLatLng(myLatLng);
        mRestaurant = null;
        restaurantsList = new ArrayList<>();

        // Call to the Google Places API
        Call<ListDetailResult> call;
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        for (int i = 0; i < googlePlacesResults.size(); i++) {
            call = apiService.getRestaurantDetail(getActivity().getString(R.string.maps_api_key), googlePlacesResults.get(i).getPlaceId(), "name,rating,photo,url,formatted_phone_number,website,address_component,id,geometry,place_id,opening_hours");

            call.enqueue(new Callback<ListDetailResult>() {
                @Override
                public void onResponse(@NonNull Call<ListDetailResult> call, @NonNull Response<ListDetailResult> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: error");
                        return;
                    }

                    ListDetailResult posts = response.body();
                    if (posts != null) {
                        mRestaurant = posts.getResult();
                        // fill the recyclerview
                        restaurantsList.add(mRestaurant);

                        adapter = new RestaurantsListAdapter(restaurantsList, Glide.with(mRecyclerView), googlePlacesResults.size(),myLatLng);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRecyclerView.setAdapter(adapter);

                        // Launch RestaurantInformation when user clicks on an articles item
                        adapter.setOnItemClickedListener(new RestaurantsListAdapter.OnItemClickedListener() {

                            public void OnItemClicked(int position) {
                                Intent WVIntent = new Intent(getContext(), RestaurantInformation.class);
                                WVIntent.putExtra(PLACEIDRESTO, restaurantsList.get(position).getPlaceId());
                                startActivity(WVIntent);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ListDetailResult> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, t.toString());
                }
            });
        }
    }
}