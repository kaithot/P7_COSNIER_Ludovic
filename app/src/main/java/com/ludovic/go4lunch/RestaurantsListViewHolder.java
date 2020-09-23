package com.ludovic.go4lunch;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.Details.Period;
import com.ludovic.go4lunch.Details.RestaurantResult;
import com.ludovic.go4lunch.api.RestHelper;
import com.ludovic.go4lunch.models.Restaurant;
import com.ludovic.go4lunch.utils.ConvertDate;
import com.ludovic.go4lunch.utils.StarsAverage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class RestaurantsListViewHolder extends RecyclerView.ViewHolder{

    private TextView nameTextView, addressTextView, openTextView, proximityTextView, interestedPeopleTextView;
    private ImageView star1, star2, star3, photo;
    private LatLng myLatLng;
    private boolean textOK = false;
    private String today;

    private static final String TAG = "ListOfRestaurantsViewho";

    public RestaurantsListViewHolder(View itemView, final RestaurantsListAdapter.OnItemClickedListener listener, LatLng latLng) {
        super(itemView);

        myLatLng = latLng;

        nameTextView =  itemView.findViewById(R.id.restaurant_name);
        addressTextView =  itemView.findViewById(R.id.restaurant_address);
        openTextView =  itemView.findViewById(R.id.restaurant_openinghours);
        proximityTextView =  itemView.findViewById(R.id.restaurant_proximity);
        interestedPeopleTextView =  itemView.findViewById(R.id.restaurant_choices_ic);

        star1 =  itemView.findViewById(R.id.restaurant_star1);
        star2 =  itemView.findViewById(R.id.restaurant_star2);
        star3 =  itemView.findViewById(R.id.restaurant_star3);
        photo =  itemView.findViewById(R.id.restaurant_photo);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null) {
                    int position = getAdapterPosition();
                    if (position!= RecyclerView.NO_POSITION) {
                        listener.OnItemClicked(position);
                    }
                }
            }
        });
    }


    void updateWithDetailsRestaurants(RestaurantResult restaurantDetail, RequestManager glide) {
        // Name of restaurant
        this.nameTextView.setText(restaurantDetail.getName());

        //Address
        String address_short = restaurantDetail.getAddressComponents().get(0).getShortName() + ", " + restaurantDetail.getAddressComponents().get(1).getShortName();
        this.addressTextView.setText(address_short);

        // Opening hours
        openTextView.setTextColor(openTextView.getResources().getColor(R.color.colorOrange));
        if(restaurantDetail.getOpeninghours()!= null) {
            // default value that will be overwritten with today's schedules if the restaurant is open today
            isRestaurantOpen(restaurantDetail);
            textOK = false;
        } else {
            openTextView.setText(R.string.no_hours);
        }

        //Distance
        float distance;
        float results[] = new float[10];
        double restaurantLat = restaurantDetail.getGeometry().getLocation().getLat();
        double restaurantLng = restaurantDetail.getGeometry().getLocation().getLng();
        double myLatitude = myLatLng.latitude;
        double myLongitude = myLatLng.longitude;
        Location.distanceBetween(myLatitude, myLongitude, restaurantLat, restaurantLng,results);
        distance = results[0];
        String dist =  Math.round(distance)+"m";
        proximityTextView.setText(dist);

        // Assign the number of stars
        if (restaurantDetail.getRating()!= null) {
            Double rate = restaurantDetail.getRating();
            StarsAverage myRate = new StarsAverage(rate, star1, star2, star3);
        } else {
            StarsAverage myRate = new StarsAverage(0, star1, star2, star3);
        }

        // Images
        if (restaurantDetail.getPhotos() != null && !restaurantDetail.getPhotos().isEmpty()){
            glide.load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+restaurantDetail.getPhotos().get(0).getPhotoReference()+"&key="+R.string.maps_api_key).into(photo);
        } else {
            this.photo.setImageResource(R.drawable.ic_baseline_panorama_24);
        }

        // Number of interested colleagues
        // Set to 0 by default
        interestedPeopleTextView.setText("0");
        ConvertDate forToday = new ConvertDate();
        today = forToday.getTodayDate();

        RestHelper.getRestaurant(restaurantDetail.getPlaceId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);

                    // Date check
                    Date dateRestaurantSheet;
                    if (restaurant != null) {
                        dateRestaurantSheet = restaurant.getDateCreated();
                        ConvertDate myDate = new ConvertDate();
                        String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);
                        if (dateRegistered.equals(today)) {
                            // Number of interested colleagues
                            List<String> listUsers = restaurant.getClientsTodayList();
                            String textnb = String.valueOf(listUsers.size());
                            interestedPeopleTextView.setText(textnb);
                        }
                    }
                }
            }
        });
    }

    private void isRestaurantOpen(RestaurantResult restaurantDetail) {
        Calendar calendar = Calendar.getInstance();
        openTextView.setTextColor(openTextView.getResources().getColor(R.color.colorOrange));
        openTextView.setText(openTextView.getResources().getString(R.string.closed_today));

        for(Period period: restaurantDetail.getOpeninghours().getPeriods()){
            if(period.getClose() == null) {
                openTextView.setText(openTextView.getResources().getString(R.string.open_today));
            } else {
                String text;
                String textTime;
                if(period.getClose().getDay() == calendar.get(Calendar.DAY_OF_WEEK)-1&&!textOK) {
                    //textOK allows you to manage cases where there are several opening hours for the same day
                    ConvertDate hour = new ConvertDate();
                    switch (getOpeningHour(period)) {
                        case 1:
                            openTextView.setTextColor(openTextView.getResources().getColor(R.color.colorPrimary));
                            text = openTextView.getResources().getString(R.string.open_at);
                            textTime = hour.getHoursFormat(period.getOpen().getTime());
                            text+=textTime;
                            openTextView.setText(text);

                            break;
                        case 2:
                            openTextView.setTextColor(openTextView.getResources().getColor(R.color.colorMyGreen));
                            text = openTextView.getResources().getString(R.string.open_at);

                            textTime = hour.getHoursFormat(period.getClose().getTime());
                            text+=textTime;
                            openTextView.setText(text);

                            break;
                        case 3:
                            openTextView.setTextColor(openTextView.getResources().getColor(R.color.colorOrange));
                            openTextView.setText(openTextView.getResources().getString(R.string.closed_today));
                    }
                }
            }
        }
    }

    // Method that get opening hours from GooglePlaces
    private int getOpeningHour(Period period){

        Calendar calendar = Calendar.getInstance();
        int currentHour = 1200;
        if (calendar.get(Calendar.MINUTE)<10) {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + "0" +calendar.get(Calendar.MINUTE));
        } else {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE));
        }
        int closureHour = Integer.parseInt(period.getClose().getTime());
        int openHour = Integer.parseInt(period.getOpen().getTime());

        Log.d(TAG, "getOpeningHour: currenthour " +currentHour);
        if (currentHour<openHour) {
            textOK = true; // We are earlier than the first schedule so do not go compare with the second
            return 1;
        }
        else if (currentHour>openHour&&currentHour<closureHour) {
            textOK = true; // We are in the first time slot so do not go compare with the second
            return 2;
        }
        else return 3;
    }

}
