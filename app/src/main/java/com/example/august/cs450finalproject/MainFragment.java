package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

import java.text.DecimalFormat;
import java.util.HashMap;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MainFragment extends Fragment implements Observer {

    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView Name;
    private TextView Email;
    private TextView Uid;
    private Button logout;

    private final static String LOGTAG = MainFragment.class.getSimpleName();


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

    // Firebase instance
    FirebaseDatabase database = null;

    // Required empty constructor
    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // set the Firebase instance
        this.database = FirebaseDatabase.getInstance();

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

        // Auth stuff
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


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

            // TODO FIX HOW LOCATION GETS UPDATED
            //updateUserLocation(lat, lon);

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
//        markedLocation = handler.getLocation();
//        String latString = "MARKED LATITUDE: " + Double.toString(markedLocation.getLatitude());
//        String lonString = "MARKED LONGITUDE: " + Double.toString(markedLocation.getLongitude());
//
//        markedLat.setText(latString);
//        markedLon.setText(lonString);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child("7BGC0zZhxePk4lnybtdpZeMAdvt2");
        GeoFire geoFire = new GeoFire(ref);

        // Set Location
        geoFire.setLocation("location", new GeoLocation(37.7853889, 2.4056973), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });

        // Get location
//        geoFire.getLocation("location", new LocationCallback() {
//            @Override
//            public void onLocationResult(String key, GeoLocation location) {
//                if (location != null) {
//                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
//                } else {
//                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.err.println("There was an error getting the GeoFire location: " + databaseError);
//            }
//        });

        // TODO REMOVE THIS TEST
//        addFriend("skinny boy");
//        addFriend("hello world");
    }

    // Read user from Firebase
    private void readUserFromDatabase(final String uniqueUserID, final UserCallback userCallback) {

        // access the user from the database and get specific components
        DatabaseReference usersReference = this.database.getReference("Users").child(uniqueUserID);


        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = (String) dataSnapshot.child("name").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                String uniqueID = (String) dataSnapshot.child("uniqueID").getValue();

                // TODO: Find a way to get this new User back to readUserFromDatabase()
                User readUser = new User(name, email);
                userCallback.onCallback(readUser);
                Log.d(LOGTAG, "Read user is: " + uniqueID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                userCallback.onCallback(null);
                Log.e(LOGTAG, "Encountered error while reading user");
            }
        });
    }

    // Update user's current location in Firebase
    private void updateUserLocation(final double currentLat, final double currentLon) {
        // access this user from the database and get specific components
        final DatabaseReference usersReference = this.database.getReference("Users").child(user.getUid());

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersReference.child("latitude").setValue(String.valueOf(currentLat));
                usersReference.child("longitude").setValue(String.valueOf(currentLon));

                Log.d(LOGTAG, "Updated lat/lon is: " + String.valueOf(currentLat) + "/" + String.valueOf(currentLon));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOGTAG, "Encountered error while updating location");
            }
        });
    }

    // Add a friend for this user
    private void addFriend(final String friendUserID) {
        // access this user from the database and get specific components
        DatabaseReference friendsReference = this.database.getReference("Friends").child(user.getUid());
        friendsReference.child(friendUserID).setValue("aLocation");
    }
}
