package com.ludovic.go4lunch.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.ludovic.go4lunch.LunchActivity;
import com.ludovic.go4lunch.Nearby.ResultNearbySearch;
import com.ludovic.go4lunch.R;
import com.ludovic.go4lunch.api.RestHelper;
import com.ludovic.go4lunch.api.locationListener;
import com.ludovic.go4lunch.models.Restaurant;
import com.ludovic.go4lunch.utils.BaseActivity;
import com.ludovic.go4lunch.utils.ConvertDate;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
                                                        GoogleMap.OnMyLocationButtonClickListener {

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
    private String today;

    public LunchActivity activity;

    private final String TAG = MapsFragment.class.getSimpleName();

    public locationListener myLocationListener;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setOnMyLocationButtonClickListener(MapsFragment.this);
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
            map.setOnMarkerClickListener(MapsFragment.this);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void updateNearbyPlaces(List<ResultNearbySearch> ResultNearbySearch, GoogleMap map){
        List<ResultNearbySearch> placesToShowId;
        placesToShowId = ResultNearbySearch;
        displayNearbyPlaces(placesToShowId, map);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myLocationListener = (locationListener) getActivity();
        ConvertDate forToday = new ConvertDate();
        today = forToday.getTodayDate();

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "Position updating  ...", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void displayNearbyPlaces(List<ResultNearbySearch> arrayIdRestaurant,GoogleMap map) {

        Log.d(TAG, "displayNearbyPLaces "+arrayIdRestaurant.size());
        for (int i = 0; i < arrayIdRestaurant.size(); i++) {
            ResultNearbySearch oneRestaurant = arrayIdRestaurant.get(i);
            String restaurantName = oneRestaurant.getName();
            double restaurantLat = oneRestaurant.getGeometry().getLocation().getLat();
            double restaurantLng = oneRestaurant.getGeometry().getLocation().getLng();
            String restaurantPlaceId = oneRestaurant.getPlaceId();

            // we posts pins
            LatLng restaurantLatLng = new LatLng(restaurantLat, restaurantLng);
            updateLikeColorMarker(restaurantPlaceId, restaurantName, restaurantLatLng,map);

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    myLocationListener.launchRestaurantDetail(marker);
                }
            });
        }
    }

    private void updateLikeColorMarker(final String placeId, final String name, final LatLng latLng, GoogleMap map) {

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

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (myLocationListener != null) {
            myLocationListener.launchRestaurantDetail(marker);
        }else {
            Log.d(TAG, "error mylocationlistener null "+getActivity()+" "+getContext());
        }
        return true;
    }
}