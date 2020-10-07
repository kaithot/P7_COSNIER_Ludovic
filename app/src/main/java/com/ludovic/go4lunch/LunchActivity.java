package com.ludovic.go4lunch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.api.ApiClient;
import com.ludovic.go4lunch.api.ApiInterface;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.api.locationListener;
import com.ludovic.go4lunch.fragments.ListFragment;
import com.ludovic.go4lunch.fragments.MapsFragment;
import com.ludovic.go4lunch.fragments.WorkmatesFragment;
import com.ludovic.go4lunch.models.User;
import com.ludovic.go4lunch.utils.BaseActivity;

import java.util.List;

import com.ludovic.go4lunch.Nearby.NearbyPlacesList;
import com.ludovic.go4lunch.Nearby.ResultNearbySearch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LunchActivity  extends BaseActivity
                            implements  NavigationView.OnNavigationItemSelectedListener,
                                        locationListener {

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    //FOR DESIGN
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    //FOR PROFILE
    private TextView userNameTextView;
    private ImageView avatarImageView;

    MapsFragment fragment1 = new MapsFragment();
    ListFragment fragment2 = new ListFragment();
    WorkmatesFragment fragment3 = new WorkmatesFragment();

    private Context mContext;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private static final float DEFAULT_ZOOM = 17;
    private List<ResultNearbySearch> results;
    private String placeId = "place_id";
    private int radius = 1000;
    private String type = "restaurant";
    private LatLng myLatLng;

    private final String TAG = LunchActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lunch_activity);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mContext = this;
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
                this.startDetailActivity();
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
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {

                        case R.id.nav_map:
                            selectedFragment = fragment1;
                            fragment1 = new MapsFragment();
                            break;
                        case R.id.nav_list:
                            selectedFragment = fragment2;
                            fragment2 = new ListFragment();
                            break;
                        case R.id.nav_workmates:
                            selectedFragment = fragment3;
                            fragment3 = new WorkmatesFragment();
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

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void permissions() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                Task locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location currentLocation = (Location) task.getResult();
                        assert currentLocation != null;

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
                        searchNearbyRestaurants(currentLocation.getLatitude(), currentLocation.getLongitude());
                        userLatLng(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));

                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        Toast.makeText(this, "unable to get current location", Toast.LENGTH_LONG).show();
                        LatLng mDefaultLocation = new LatLng(-34, 151);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            BaseActivity.requestPermission((AppCompatActivity) this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    public void searchNearbyRestaurants(double latitude, double longitude){
        Log.d(TAG, "searchNearbyRestaurants: ");
        //String keyword = "";
        String key = getString(R.string.maps_api_key);
        String lat = String.valueOf(latitude);
        String lng = String.valueOf(longitude);

        String location = lat+","+lng;
        Call<NearbyPlacesList> call;
        Log.d(TAG, "location "+location);
        ApiInterface googleMapService = ApiClient.getClient().create(ApiInterface.class);
        call = googleMapService.getNearBy(location, radius, type, key);
        call.enqueue(new Callback<NearbyPlacesList>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPlacesList> call, @NonNull Response<NearbyPlacesList> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        results = response.body().getResults();
                        fragment1.updateNearbyPlaces(results, map);
                        fragment2.results=results;

                        Log.d(TAG, "success: "+results.toString());
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPlacesList> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
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

    @Override
    public void mapReady() {
        permissions();
    }

    @Override
    public void setMap(GoogleMap map) {
        this.map = map;
    }

    @Override
    public void userLatLng(LatLng userLatLng) {
        this.myLatLng = userLatLng;
    }


}