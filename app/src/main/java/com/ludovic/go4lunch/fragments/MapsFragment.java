package com.ludovic.go4lunch.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.R;
import com.ludovic.go4lunch.RestaurantInformation;
import com.ludovic.go4lunch.api.RestHelper;
import com.ludovic.go4lunch.models.Restaurant;
import com.ludovic.go4lunch.utils.BaseActivity;
import com.ludovic.go4lunch.utils.ConvertDate;
import com.ludovic.go4lunch.utils.DataHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Nearby.ResultNearbySearch;

import static android.content.ContentValues.TAG;

public class MapsFragment extends Fragment implements   GoogleMap.OnMyLocationButtonClickListener,
                                                        GoogleMap.OnMyLocationClickListener,
                                                        OnMapReadyCallback  {


    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    private GoogleMap map;
    private View mView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker;
    private String lat;
    private String lng;
    private String today;
    private List<ResultNearbySearch> searchList = new ArrayList<>();

    private static final float DEFAULT_ZOOM = 15f;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.map, mapFragment,"map")
                .commit();

        return mView;
    }

    public void updateNearbyPlaces(List<ResultNearbySearch> googlePlacesResults){
        List<ResultNearbySearch> placesToShowId;
        placesToShowId = googlePlacesResults;
        displayNearbyPlaces(placesToShowId);
    }

    private void displayNearbyPlaces(List<ResultNearbySearch> tabIdRestaurant) {
        for (int i = 0; i < tabIdRestaurant.size(); i++) {
            ResultNearbySearch oneRestaurant = tabIdRestaurant.get(i);
            String restaurantName = oneRestaurant.getName();
            double restaurantLat = oneRestaurant.getGeometry().getLocation().getLatitude();
            double restaurantLng = oneRestaurant.getGeometry().getLocation().getLongitude();
            String restaurantPlaceId = oneRestaurant.getPlaceId();

            // we posts markers
            LatLng restaurantLatLng = new LatLng(restaurantLat, restaurantLng);
            updateLikeColorMarker( restaurantPlaceId, restaurantName, restaurantLatLng);

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    launchRestaurantDetail(marker);
                }
            });
        }
    }

    private void launchRestaurantDetail(Marker marker ) {
        String placeId = "restaurant_place_id";
        String ref = (String) marker.getTag();
        //Intent WVIntent = new Intent(getContext(), RestaurantInformation.class);
        //Id
       // WVIntent.putExtra(placeId, ref);
       // startActivity(WVIntent);
    }

    // Update Marker color

    private void updateLikeColorMarker(final String placeId, final String name, final LatLng latLng) {

        // The color of the pin is adjusted according to the user's choice
        final MarkerOptions markerOptions = new MarkerOptions();

        // By default we put red pins
        markerOptions.position(latLng)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        marker = map.addMarker(markerOptions);
        marker.setTag(placeId);


        RestHelper.getRestaurant(placeId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    Date dateRestaurantSheet;
                    if (restaurant != null) {
                        dateRestaurantSheet = restaurant.getDateCreated();
                        ConvertDate Date = new ConvertDate();
                        String dateRegistered = Date.getRegisteredDate(dateRestaurantSheet);

                        if (dateRegistered.equals(today)) {
                            int users = restaurant.getClientsTodayList().size();
                            if (users > 0) {
                                markerOptions.position(latLng)
                                        .title(name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                marker = map.addMarker(markerOptions);
                                marker.setTag(placeId);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        // Removes the default markers from the map to have a clean map background
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            Objects.requireNonNull(getContext()), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                Task locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location currentLocation = (Location) task.getResult();
                        assert currentLocation != null;
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        markerOptions.title("My position");
                        marker = map.addMarker(markerOptions);
                        lat = String.valueOf(currentLocation.getLatitude());
                        lng = String.valueOf(currentLocation.getLongitude());
                        DataHolder.getInstance().setCurrentLat(currentLocation.getLatitude());
                        DataHolder.getInstance().setCurrentLng(currentLocation.getLongitude());
                        DataHolder.getInstance().setCurrentPosition(lat + "," + lng);

                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_LONG).show();
                        LatLng mDefaultLocation = new LatLng(-34, 151);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            BaseActivity.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (BaseActivity.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    public void onResume() {
        super.onResume();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        } else {
            if (map != null) {
                this.enableMyLocation();
            }
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        BaseActivity.PermissionDeniedDialog
                .newInstance(true).show(getFragmentManager(), "dialog");
    }

}
