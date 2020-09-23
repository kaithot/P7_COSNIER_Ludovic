package com.ludovic.go4lunch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import java.util.List;

class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    private List<String> clientsList;
    private RequestManager glide;
    private int length;

    // Constructor
    public WorkmatesAdapter(List<String> clientsList, RequestManager glide, int length) {
        this.clientsList = clientsList;
        this.glide = glide;
        this.length =  length;
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creates view holder and inflates its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_workmate, parent, false);
        return new WorkmatesViewHolder(view, context);
    }

    // update view holder
    @Override
    public void onBindViewHolder(@NonNull WorkmatesViewHolder viewHolder, int position) {
        viewHolder.updateWithDetails(this.clientsList.get(position), this.glide);
    }

    // return the total count of items in the list
    @Override
    public int getItemCount() {
        return length ;
    }
}
