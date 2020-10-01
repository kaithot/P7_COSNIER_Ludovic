package com.ludovic.go4lunch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.Details.RestaurantResult;
import com.ludovic.go4lunch.api.ApiClient;
import com.ludovic.go4lunch.api.ApiInterface;
import com.ludovic.go4lunch.api.RestHelper;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.models.Restaurant;
import com.ludovic.go4lunch.models.User;
import com.ludovic.go4lunch.utils.ConvertDate;
import com.ludovic.go4lunch.utils.StarsAverage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.ludovic.go4lunch.Nearby.ListDetailResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantInformation  extends AppCompatActivity {

    private String WEB = "resto_web";
    private String PLACEIDRESTAURANT = "resto_place_id";
    private String restaurantToday;
    private List<String> listRestaurantLike= new ArrayList<>();
    private String restaurantAddress;
    private TextView nameTV;
    private TextView addressTV;
    private ImageView photoIV;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageView toPhone;
    private ImageView toWebsite;
    private ImageView likeThisRestaurant;
    private FloatingActionButton myRestaurantTodayBtn;

    private String restaurantTel;
    private String placeIdRestaurant;

    private String userId;
    private String restaurantName;
    private String lastRestaurantId;
    private String lastRestaurantDate;
    private String lastRestaurantName;
    private String today;

    private final static String TAG = "DETAILRESTOACTIVITY";

    private static final int REQUEST_CALL = 1;

    private WorkmatesAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_information);

        context = this;
        ConvertDate thisNoon = new ConvertDate();
        today = thisNoon.getTodayDate();
        userId = UserHelper.getCurrentUserId();
        placeIdRestaurant = getIntent().getStringExtra(PLACEIDRESTAURANT);

        //---------------------------------------------------------------------------------------------
        // RecyclerView
        //-----------------------------------------------------------------------------------------------
        recyclerView = findViewById(R.id.fragment_workmates_recyclerview);
        setupRecyclerView();

        //-----------------------------------------------------------------------------------------------

        Call<ListDetailResult> call;
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        call = apiService.getRestaurantDetail(getString(R.string.maps_api_key), placeIdRestaurant, "name,rating,photo,url,formatted_phone_number,website,formatted_address,id,geometry");
        call.enqueue(new Callback<ListDetailResult>() {
            @Override
            public void onResponse(@NonNull Call<ListDetailResult> call, @NonNull Response<ListDetailResult> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, "Code: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                ListDetailResult posts = response.body();
                RestaurantResult mResto;
                if (posts != null) {
                    mResto = posts.getResult();


                    //----------------------------------------------------------------------------------------
                    // Display infos on restaurant
                    //---------------------------------------------------------------------------------------
                    restaurantName = mResto.getName();
                    restaurantAddress = mResto.getFormattedAddress();
                    nameTV = findViewById(R.id.restaurant_name);
                    nameTV.setText(restaurantName);
                    addressTV = findViewById(R.id.restaurant_address);
                    addressTV.setText(restaurantAddress);

                    //------------------------------------------------------------------------------------------
                    // Rating
                    //-------------------------------------------------------------------------------------------
                    double Rate = mResto.getRating();
                    star1 =  findViewById(R.id.star1);
                    star2 =  findViewById(R.id.star2);
                    star3 =  findViewById(R.id.star3);
                    StarsAverage myRate = new StarsAverage (Rate, star1, star2, star3);

                    //-------------------------------------------------------------------------------------------
                    // Photo
                    //---------------------------------------------------------------------------------------------

                    photoIV =  findViewById(R.id.photo_restaurant);
                    if (mResto.getPhotos() != null && !mResto.getPhotos().isEmpty()){
                        String restaurantPhoto = mResto.getPhotos().get(0).getPhotoReference();
                        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + restaurantPhoto + "&key=" + getString(R.string.maps_api_key);
                        Glide.with(context).load(photoUrl).into(photoIV);
                    } else {
                        photoIV.setImageResource(R.drawable.ic_background);
                    }

                    //-----------------------------------------------------------------------------------------------
                    // Call
                    //-----------------------------------------------------------------------------------------------
                    //restaurantTel = restaurant.getPhone();
                    restaurantTel = "06 28 08 57 50";  // for tests
                    toPhone = findViewById(R.id.phone_button);
                    toPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            makePhoneCall();
                        }
                    });

                    //--------------------------------------------------------------------------------------------------
                    // Website
                    //-------------------------------------------------------------------------------------------------
                    final String restaurantWebsite = mResto.getWebsite();
                    toWebsite = findViewById(R.id.website_button);
                    toWebsite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (restaurantWebsite.equals("no-website")) {
                                Toast.makeText(RestaurantInformation.this, R.string.no_website, Toast.LENGTH_LONG).show();
                            } else {
                                Intent WVIntent = new Intent(RestaurantInformation.this, WebViewRestaurant.class);
                                WVIntent.putExtra(WEB, restaurantWebsite);
                                startActivity(WVIntent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListDetailResult> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, t.toString());
            }
        });

        //------------------------------------------------------------------------------------------------

        //-----------------------------------------------------------------------------------------
        // Like or not
        //-------------------------------------------------------------------------------------------
        likeThisRestaurant=  findViewById(R.id.like_button);
        // update view
        updateLikeView(placeIdRestaurant);
        likeThisRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update FireStore
                updateLikeInFirebase(placeIdRestaurant);
            }
        });

        //------------------------------------------------------------------------------------------
        // Choose this restaurant today
        //------------------------------------------------------------------------------------------
        myRestaurantTodayBtn = findViewById(R.id.restoToday_FloatingButton);
        // update view
        updateTodayView(placeIdRestaurant);
        myRestaurantTodayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update FireStore
                updateRestaurantTodayInFirebase(placeIdRestaurant, restaurantName);
            }
        });

    }

    //-------------------------------------------------------------------------------------------------
    // Phone call
    //------------------------------------------------------------------------------------------------
    private void makePhoneCall(){
        if (restaurantTel.trim().length()>0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);

            } else {
                String dial = "tel:"+restaurantTel;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }

        } else {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CALL) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this,R.string.no_permission_for_call, Toast.LENGTH_LONG).show();
            }
        }
    }

    //---------------------------------------------------------------------------------------------------
    // Update Firebase
    //---------------------------------------------------------------------------------------------------
    private void updateLikeInFirebase(final String idRestaurant) {
        Log.d(TAG, "updateLikeInFirebase: idRestaurant " +idRestaurant);
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "onSuccess: documentSnapshot exists");
                    listRestaurantLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getLike();
                    if (listRestaurantLike != null) {
                        if (listRestaurantLike.contains(idRestaurant)) {
                            Log.d(TAG, "onSuccess: remove restaurant");
                            listRestaurantLike.remove(idRestaurant);
                            likeThisRestaurant.setImageResource(R.drawable.ic_baseline_no_star_border_24);
                        } else {
                            Log.d(TAG, "onSuccess: add restaurant");
                            listRestaurantLike.add(idRestaurant);
                            likeThisRestaurant.setImageResource(R.drawable.ic_baseline_star_24);
                        }
                    }
                    UserHelper.updateLikedRest(listRestaurantLike, userId);
                }
            }
        });
    }

    private void updateRestaurantInUser(String id, String name, String date ) {
        UserHelper.updateTodayRest(id, userId);
        UserHelper.updateTodayRestName(name, userId);
        UserHelper.updateRestDate(date, userId);
    }

    private void removeUserInRestaurant(final String id, final String name){
        RestHelper.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);
                    // We check if the restaurant's card corresponds to the date of the day, otherwise it will have to be updated
                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();

                        ConvertDate Date = new ConvertDate();
                        String dateRegistered = Date.getRegisteredDate(dateRestoSheet);

                        if (dateRegistered.equals(today)) {
                            // the restaurant card of the day already exists so we remove the user
                            List<String> listUsersToday = new ArrayList<>();
                            listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.remove(userId);
                            RestHelper.updateClientsTodayList(listUsersToday, id);
                        } else {
                            // The restaurant card of the day did not exist so we update it empty
                            RestHelper.createRestaurant(id, name, restaurantAddress);
                        }
                    }
                }
            }
        });
    }

    private void addUserInRestaurant(final String id, final String name) {
        RestHelper.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestaurantSheet;
                    if (usersToday != null) {
                        dateRestaurantSheet = usersToday.getDateCreated();

                        ConvertDate myDate = new ConvertDate();
                        String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);
                        if (dateRegistered.equals(today)) {
                            List<String> listUsersToday = new ArrayList<>();
                            listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.add(userId);
                            RestHelper.updateClientsTodayList(listUsersToday, id);
                        }else {
                            RestHelper.createRestaurant(id, name, restaurantAddress);
                            updateUserTodayInFirebase(userId, id);
                        }
                    }
                } else {
                    RestHelper.createRestaurant(id, name, restaurantAddress);
                    updateUserTodayInFirebase(userId, id);
                }
            }
        });
    }

    private void updateUserTodayInFirebase(String myId, String myRestoId) {
        List<String> listUsersToday = new ArrayList<>();
        listUsersToday.add(myId);
        RestHelper.updateClientsTodayList(listUsersToday, myRestoId);
    }

    private void  updateRestaurantTodayInFirebase(final String restaurantChoiceId, final String restaurantChoiceName) {
        //Update User
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User myRestaurantToday = documentSnapshot.toObject(User.class);
                    if (myRestaurantToday != null) {
                        lastRestaurantId = myRestaurantToday.getRestToday();
                        lastRestaurantDate = myRestaurantToday.getRestDate();
                        lastRestaurantName = myRestaurantToday.getRestTodayName();

                        if (lastRestaurantId != null && lastRestaurantId.length() > 0 && lastRestaurantDate.equals(today)) {
                            // A restaurant had already been chosen today
                            // That was it, so we unselect it and remove it from User
                            if (lastRestaurantId.equals(restaurantChoiceId)) {
                                myRestaurantTodayBtn.setImageResource(R.drawable.ic_baseline_cancel_24);
                                updateRestaurantInUser("", "", today);
                                // This user is also removed from Restaurant from the guest list
                                removeUserInRestaurant(restaurantChoiceId, restaurantChoiceName);

                            } else {
                                // It was not this one so we replace it with the new choice in User
                                myRestaurantTodayBtn.setImageResource(R.drawable.ic_baseline_check_circle_24);
                                updateRestaurantInUser(restaurantChoiceId, restaurantChoiceName, today);
                                // We delete the user from the list of guests of his former restaurant chosen
                                removeUserInRestaurant(restaurantChoiceId, restaurantChoiceName);
                                // and we add the user in the list of guests of the new restaurant
                                addUserInRestaurant(restaurantChoiceId, restaurantChoiceName);
                            }
                        } else {
                            // No restaurant was registered, so we save this one in User
                            updateRestaurantInUser(restaurantChoiceId, restaurantChoiceName, today);
                            myRestaurantTodayBtn.setImageResource(R.drawable.ic_baseline_check_circle_24);
                            // and we add this guest to the restaurant list
                            addUserInRestaurant(restaurantChoiceId, restaurantChoiceName);
                        }
                    }
                }
            }
        });
    }

    private void updateLikeView(String id) {
        final String idLike=id;
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listRestaurantLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getLike();
                if(listRestaurantLike!=null) {
                    if (listRestaurantLike.contains(idLike)) {
                        likeThisRestaurant.setImageResource(R.drawable.ic_baseline_star_24);
                    } else {
                        likeThisRestaurant.setImageResource(R.drawable.ic_baseline_no_star_border_24);
                    }
                } else {
                    likeThisRestaurant.setImageResource(R.drawable.ic_baseline_no_star_border_24);
                }
            }
        });
    }

    private void updateTodayView(String id) {
        final String idToday = id;

        // Default values
        myRestaurantTodayBtn.setImageResource(R.drawable.ic_baseline_cancel_24);

        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                restaurantToday = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestToday();
                lastRestaurantDate = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestDate();

                if (restaurantToday != null && restaurantToday.length()>0&&lastRestaurantDate.equals(today)) { // We check that there is a restaurant registered and that it was registered today
                    if (restaurantToday.equals(idToday)) {
                        myRestaurantTodayBtn.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    }
                }
            }
        });
    }


    //-------------------------------------------------------------------------------------------------
    //Recyclerview
    //------------------------------------------------------------------------------------------------

    private void setupRecyclerView() {

        RestHelper.getRestaurant(placeIdRestaurant).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestaurantSheet;
                    if (usersToday != null) {
                        dateRestaurantSheet = usersToday.getDateCreated();
                        ConvertDate myDate = new ConvertDate();
                        String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);

                        if (dateRegistered.equals(today)) {
                            List<String> listId = usersToday.getClientsTodayList();

                            if (listId != null) {
                                adapter = new WorkmatesAdapter(listId, Glide.with(recyclerView), listId.size());
                                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                }
            }
        });

    }
}
