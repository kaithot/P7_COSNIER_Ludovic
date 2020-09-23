package com.ludovic.go4lunch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.maps.model.LatLng;
import com.ludovic.go4lunch.Details.RestaurantResult;

import java.util.List;

public class RestaurantsListAdapter extends RecyclerView.Adapter<RestaurantsListViewHolder> {

    private TextView nameTextView, addressTextView, openTextView, proximityTextView, loversTextView;
    private ImageView star1, star2, star3, photo;
    private LatLng myLatLng;
    private boolean textOK = false;
    private String today;

    private List<RestaurantResult> restaurantsList;
    private RequestManager glide;
    private OnItemClickedListener mListener;
    private int length;
    private LatLng latlng;

    public interface OnItemClickedListener{
        void OnItemClicked(int position);
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mListener = listener;
    }

    // Constructor
    public RestaurantsListAdapter(List<RestaurantResult> restoList, RequestManager glide, int length, LatLng latLng) {
        this.restaurantsList = restaurantsList;
        this.glide = glide;
        this.length =  length;
        this.latlng = latLng;
    }

    @NonNull
    @Override
    public RestaurantsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creates view holder and inflates its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantsListViewHolder(view, mListener, latlng);
    }

    // update view holder
    @Override
    public void onBindViewHolder(@NonNull RestaurantsListViewHolder viewHolder, int position) {
        viewHolder.updateWithDetailsRestaurants(this.restaurantsList.get(position), this.glide);
    }

    @Override
    public int getItemCount() {
        return length ;
    }
}

