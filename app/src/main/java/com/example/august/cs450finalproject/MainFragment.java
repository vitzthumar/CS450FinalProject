package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.HashSet;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class MainFragment extends Fragment implements Observer {

    private OnFragmentInteractionListener mListener;
    private final static int PERMISSION_REQUEST_CODE = 999;
    private LocationHandler handler = null;

    // views
    TextView currentLat;
    TextView currentLon;
    Button markLocation;
    TextView markedLat;
    TextView markedLon;
    TextView distanceFromMark;
    Location currentLocation = null;
    Location markedLocation = null;

    // Required empty constructor
    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (handler == null) {
            this.handler = new LocationHandler(getActivity());
            this.handler.addObserver(this);
        }

        // check permissions
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // find the views by ID
        currentLat = rootView.findViewById(R.id.currentLatitude);
        currentLon = rootView.findViewById(R.id.currentLongitude);
        markLocation = rootView.findViewById(R.id.markLocation);
        markLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // execute a method every time this button is clicked
                markCurrentLocation();
            }
        });
        markedLat = rootView.findViewById(R.id.markedLatitude);
        markedLon = rootView.findViewById(R.id.markedLongitude);
        distanceFromMark = rootView.findViewById(R.id.distanceFromMark);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof LocationHandler) {
            currentLocation = (Location) o;
            final double lat = currentLocation.getLatitude();
            final double lon = currentLocation.getLongitude();
            String latString = "CURRENT LATITUDE: " + Double.toString(lat);
            String lonString = "CURRENT LONGITUDE: " + Double.toString(lon);

            currentLat.setText(latString);
            currentLon.setText(lonString);
        }

        if (markedLocation != null) {
            double startLat = currentLocation.getLatitude();
            double endLat = markedLocation.getLatitude();
            double startLon = currentLocation.getLongitude();
            double endLon = currentLocation.getLatitude();

            double dLat  = Math.toRadians((endLat - startLat));
            double dLon = Math.toRadians((endLon - startLon));

            startLat = Math.toRadians(startLat);
            endLat   = Math.toRadians(endLat);

            double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double distanceInMeters = (6371 * c) / 1000;
            String distanceString = "DISTANCE FROM MARK: " + distanceInMeters + " METERS";
            // set the text on the text view
            distanceFromMark.setText(distanceString);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // Method used to mark the user's current location
    private void markCurrentLocation() {
        // TODO: Fill this method
        markedLocation = handler.getLocation();
        String latString = "MARKED LATITUDE: " + Double.toString(markedLocation.getLatitude());
        String lonString = "MARKED LONGITUDE: " + Double.toString(markedLocation.getLongitude());

        markedLat.setText(latString);
        markedLon.setText(lonString);

        //TODO: THIS IS A TEST
        User newUser = new User("TESTNAME", "TESTEMAIL", new HashSet<String>(), currentLocation);
        newUser.writeToDatabase();
    }


}
