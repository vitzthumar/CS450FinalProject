package com.example.august.cs450finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.function.DoubleToLongFunction;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get the information on each user's location
        Intent receiveIntent = this.getIntent();
        Double userLat = Double.parseDouble(receiveIntent.getStringExtra("USER_LAT"));
        Double userLon = Double.parseDouble(receiveIntent.getStringExtra("USER_LON"));
        Double otherLat = Double.parseDouble(receiveIntent.getStringExtra("OTHER_LAT"));
        Double otherLon = Double.parseDouble(receiveIntent.getStringExtra("OTHER_LON"));
        String otherName = getIntent().getStringExtra("OTHER_NAME");

        // add a marker at both users' locations
        LatLng userLocation = new LatLng(userLat, userLon);
        LatLng otherLocation = new LatLng(otherLat, otherLon);

        mMap.addMarker(new MarkerOptions().position(userLocation).title("My location"));
        mMap.addMarker(new MarkerOptions().position(otherLocation).title(otherName + "'s location"));

        // position and zoom the camera to capture both points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);
        builder.include(otherLocation);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.2); // offset from edges of the map 10% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        googleMap.animateCamera(cu);
    }
}
