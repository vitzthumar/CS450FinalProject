package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Observable;

public class LocationHandler extends Observable implements LocationListener {

    // handles location services for the device
    private LocationManager locationManager;
    private Activity activity;

    public LocationHandler(Activity activity) {
        this.activity = activity;
        this.locationManager = (LocationManager) this.activity.getSystemService(
                Context.LOCATION_SERVICE);

        if (this.activity.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0,
                    this);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0,
                    this);
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    5000,
                    0,
                    this);

            // check for initial GPS coordinate
            Location l =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (l != null) {
                setChanged();
                notifyObservers(l);
                return;
            }

            l = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (l != null) {
                setChanged();
                notifyObservers(l);
                return;
            }

            l = locationManager.getLastKnownLocation(
                    LocationManager.PASSIVE_PROVIDER);
            if (l != null) {
                setChanged();
                notifyObservers(l);
                return;
            }
        }
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
        if (this.activity.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0,
                    this);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0,
                    this);
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    5000,
                    0,
                    this);

            // check for initial GPS coordinate
            Location l =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (l != null) {
                return l;
            }

            l = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (l != null) {
                return l;
            }

            l = locationManager.getLastKnownLocation(
                    LocationManager.PASSIVE_PROVIDER);
            if (l != null) {
                return l;
            }
        }

        return null;
    }
}
