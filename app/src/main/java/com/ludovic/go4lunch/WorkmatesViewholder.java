package com.ludovic.go4lunch;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.models.User;

class WorkmatesViewHolder extends RecyclerView.ViewHolder{
    private TextView nameTextView ;
    private ImageView photo;
    private Context mContext;

    WorkmatesViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        nameTextView = itemView.findViewById(R.id.workmates_TextView);
        photo = itemView.findViewById(R.id.workmates_ImageView);
    }

    void updateWithDetails(final String clientId, final RequestManager glide) {

        UserHelper.getUser(clientId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User client = documentSnapshot.toObject(User.class);
                String text;
                if (client != null) {
                    text = client.getUsername() + mContext.getResources().getString(R.string.isjoining);
                    nameTextView.setText(text);
                    nameTextView.setTypeface(null, Typeface.NORMAL);
                    nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
                }

                // Images
                if (client != null) {
                    if (client.getUrlPicture() != null) {
                        if (client.getUrlPicture().length()>0){
                            glide.load(client.getUrlPicture())
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(photo);
                        } else {
                            photo.setImageResource(R.drawable.ic_baseline_people_24);
                        }
                    }
                }
            }
        });
    }
}
