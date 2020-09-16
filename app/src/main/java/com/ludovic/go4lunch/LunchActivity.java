package com.ludovic.go4lunch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.api.ApiClient;
import com.ludovic.go4lunch.api.ApiInterface;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.fragments.ListFragment;
import com.ludovic.go4lunch.fragments.MapsFragment;
import com.ludovic.go4lunch.fragments.WorkmatesFragment;
import com.ludovic.go4lunch.models.User;
import com.ludovic.go4lunch.utils.BaseActivity;

import java.util.List;

import Nearby.NearbyPlacesList;
import Nearby.ResultNearbySearch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LunchActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //FOR DESIGN
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    //FOR PROFILE
    private TextView userNameTextView;
    private ImageView avatarImageView;

    MapsFragment fragment = new MapsFragment();
    private Context mContext;
    private List<ResultNearbySearch> results;
    private String placeId = "place_id";
    private int radius;
    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lunch_activity);

        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));

        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureBottomNavigationView();
        this.updateUIWhenCreating();

    }

    @Override
    public void onBackPressed() {

        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_myresto:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_logout:
                this.signOutFromFirebase();
                break;
            default:
                break;
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // CONFIGURATION

    // Configure Toolbar
    private void configureToolBar() {

        this.toolbar = (Toolbar) findViewById(R.id.activity_lunch_toolbar);
        setSupportActionBar(toolbar);

    }

    // Configure Drawer Layout
    private void configureDrawerLayout() {

        this.drawerLayout = (DrawerLayout) findViewById(R.id.activity_lunch_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    // Configure NavigationView
    private void configureNavigationView() {

        this.navigationView = (NavigationView) findViewById(R.id.activity_lunch_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void configureBottomNavigationView() {

        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.main_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.activity_lunch_frame_layout,
                new MapsFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = fragment;
                    switch (item.getItemId()) {
                        case R.id.nav_map:
                            selectedFragment = new MapsFragment();
                            break;
                        case R.id.nav_list:
                            selectedFragment = new ListFragment();
                            break;
                        case R.id.nav_workmates:
                            selectedFragment = new WorkmatesFragment();
                            break;
                    }
                    assert selectedFragment != null;
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_lunch_frame_layout,
                           selectedFragment).commit();
                    return true;
                }
            };


    //---------------------
    //REST REQUESTS
    //---------------------
    // Create http requests (SignOut)
    private void signOutFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted());
    }

    // Create onCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted() {
        return aVoid -> {
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        };
    }

    //  Update UI when activity is creating
    private void updateUIWhenCreating(){

        userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.userName);
        avatarImageView = navigationView.getHeaderView(0).findViewById(R.id.avatar);

        if (this.getCurrentUser() != null){
            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(avatarImageView);
            }

            //Get username from Firebase
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            this.userNameTextView.setText(username);
        }
    }

    private void searchNearbyRestaurants(double mylat, double mylng){
        Log.d("info :", "searchNearbyRestaurants: ");
        String keyword = "";
        String key = getString(R.string.maps_api_key);
        String lat = String.valueOf(mylat);
        String lng = String.valueOf(mylng);

        String location = lat+","+lng;

        Call<NearbyPlacesList> call;
        ApiInterface googleMapService = ApiClient.getClient().create(ApiInterface.class);
        call = googleMapService.getNearBy(location, radius, type, keyword, key);
        call.enqueue(new Callback<NearbyPlacesList>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPlacesList> call, @NonNull Response<NearbyPlacesList> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        results = response.body().getResults();
                        fragment.updateNearbyPlaces(results);
                        
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPlacesList> call, @NonNull Throwable t) {
                Log.d("info", "onFailure: ");
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startDetailActivity() {
        String userId=  UserHelper.getCurrentUserId();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String lunch;
                if (user != null) {
                    lunch = user.getRestToday();
                    if (lunch.equals("")) {
                        Toast.makeText(mContext, R.string.no_lunch, Toast.LENGTH_LONG).show();
                    } else {
                        Intent WVIntent = new Intent(mContext, RestaurantInformation.class);
                        WVIntent.putExtra(placeId, lunch);
                        startActivity(WVIntent);
                    }
                }
            }
        });
    }


   @Override
   public int getFragmentLayout() {
       return R.layout.lunch_activity;
   }
    
}