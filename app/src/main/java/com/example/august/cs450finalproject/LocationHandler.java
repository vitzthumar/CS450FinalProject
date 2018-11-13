package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.security.Provider;
import java.util.Observable;

import static android.location.Criteria.ACCURACY_FINE;

public class LocationHandler extends Observable implements LocationListener {

    // handles location services for the device
    private LocationManager locationManager;
    private Activity activity;

    public LocationHandler(Activity activity) {
        this.activity = activity;
        this.locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    public void onLocationChanged(Location location) {
        setChanged();
        notifyObservers(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    protected Location getLocation() {
        if (this.activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

            return location;
        }
        return null;
    }
}
