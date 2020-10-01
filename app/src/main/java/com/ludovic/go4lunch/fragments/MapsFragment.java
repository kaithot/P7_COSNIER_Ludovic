package com.ludovic.go4lunch.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.ludovic.go4lunch.R;
import com.ludovic.go4lunch.RestaurantInformation;
import com.ludovic.go4lunch.api.RestHelper;
import com.ludovic.go4lunch.models.Restaurant;
import com.ludovic.go4lunch.utils.BaseActivity;
import com.ludovic.go4lunch.utils.ConvertDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.ludovic.go4lunch.Nearby.ResultNearbySearch;

public class MapsFragment extends SupportMapFragment implements
                                                        GoogleMap.OnMyLocationButtonClickListener,
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
    private Marker marker;
    private double lat;
    private double lng;
    private String today;

    private static final float DEFAULT_ZOOM = 15f;
    private final String TAG = MapsFragment.class.getSimpleName();


    public interface locationListener {
        public void mapReady();
        public void setMap(GoogleMap map);
    }

    public locationListener myLocationListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void updateNearbyPlaces(List<ResultNearbySearch> ResultNearbySearch){
        List<ResultNearbySearch> placesToShowId;
        placesToShowId = ResultNearbySearch;
        displayNearbyPlaces(placesToShowId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myLocationListener = (locationListener) getActivity();

        ConvertDate forToday = new ConvertDate();
        today = forToday.getTodayDate();

      getMapAsync(this);
        // Default position before geolocation
        lat = 47.3936417;
        lng = 0.6805983;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
        myLocationListener.setMap(map);
        myLocationListener.mapReady();



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

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    public void enableMyLocation(LatLng userLatLng) {
        // update location with geolocation
        lat = userLatLng.latitude;
        lng = userLatLng.longitude;

        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));

        }}

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "Position updating  ...", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void displayNearbyPlaces(List<ResultNearbySearch> arrayIdRestaurant) {

        Log.d(TAG, "displayNearbyPLaces "+arrayIdRestaurant.size());
        for (int i = 0; i < arrayIdRestaurant.size(); i++) {
            ResultNearbySearch oneRestaurant = arrayIdRestaurant.get(i);
            String restaurantName = oneRestaurant.getName();
            double restaurantLat = oneRestaurant.getGeometry().getLocation().getLat();
            double restaurantLng = oneRestaurant.getGeometry().getLocation().getLng();
            String restaurantPlaceId = oneRestaurant.getPlaceId();

            // we posts pins
            LatLng restaurantLatLng = new LatLng(restaurantLat, restaurantLng);
            updateLikeColorMarker(restaurantPlaceId, restaurantName, restaurantLatLng);

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    launchRestaurantDetail(marker);
                }
            });
        }
    }

    // Update Marker color
    private void updateLikeColorMarker(final String placeId, final String name, final LatLng latLng) {

        Log.d(TAG, "add marker "+name +latLng);
        // The color of the pin is adjusted according to the user's choice
        final MarkerOptions markerOptions = new MarkerOptions();

        // By default we put red pins
        markerOptions.position(latLng)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        marker = map.addMarker(markerOptions);
        marker.setTag(placeId);
        Log.d(TAG, "red marker ");


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
                                Log.d(TAG, "add green marker");
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

    //--------------------------------------------------------------------------------------------------------------------
    //manages the click on the info bubble
    //--------------------------------------------------------------------------------------------------------------------
    private void launchRestaurantDetail(Marker marker ) {
        String PLACEIDRESTO = "resto_place_id";
        String ref = (String) marker.getTag();
        Intent WVIntent = new Intent(getContext(), RestaurantInformation.class);
        //Id
        WVIntent.putExtra(PLACEIDRESTO, ref);
        startActivity(WVIntent);
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (BaseActivity.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.

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
