package com.example.august.cs450finalproject;

import android.location.Location;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// Class defining the information available to a specific user
@IgnoreExtraProperties
public class User {

    private final static String LOGTAG = User.class.getSimpleName();

    // instance variables
    public String name;
    public String email;
    public String uuid;
    public Double lat;
    public Double lng;

    // No arg constructor
    public User () {}

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    // User constructor
    public User(String name, String email) {
        // assign the parameters to the instance variables
        this.name = name;
        this.email = email;
        this.uuid = null;
        this.lat = null;
        this.lng = null;

    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDistanceTo(GeoLocation userLocation) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(distance(this.lat, this.lng, userLocation.latitude, userLocation.longitude)) + "KM";
    }

    private double distance(double startLat, double startLong, double endLat, double endLong) {

        final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

        // TODO remove these printlns
        System.out.println("Calculating...");
        System.out.println("startLat " + startLat);
        System.out.println("startLon " + startLong);
        System.out.println("endLat " + endLat);
        System.out.println("endLong " + endLong);
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- dist (in KILOMETERS)
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
