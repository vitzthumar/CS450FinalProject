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
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;

import java.text.DecimalFormat;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MainFragment extends Fragment {

    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private final static String LOGTAG = MainFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private final static int PERMISSION_REQUEST_CODE = 999;
    private LocationHandler handler = null;

    private EditText enterUserEmail = null;
    private Button sendFriendRequest = null;

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
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Auth stuff
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        /*
        // initialize the views
        enterUserEmail = rootView.findViewById(R.id.sendRequestEditText);
        sendFriendRequest = rootView.findViewById(R.id.sendRequestButton);
        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                // TODO: FIX THE FRIEND REQUEST


                Thread friendRequestThread = new Thread() {
                    @Override
                    public void run() {
                        // send the the user whose email is in the text field a friend request
                        String userEmail = enterUserEmail.getText().toString();
                        if (!userEmail.equals("")) {
                            // disable the friend request button
                            sendFriendRequest.setEnabled(false);
                            // find the ID given the email
                            findUserIDFromEmail(userEmail);
                        }
                    }
                };
                friendRequestThread.start();
            }
        });
        */

        getInitialLocation();

        return rootView;
    }

    // Find a user's ID given their email
    private void findUserIDFromEmail(String userEmail) {

        DatabaseReference usersReference = database.getReference().child("Users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot currentSnapshot: dataSnapshot.getChildren()) {
                    // check this child to see if its email matches the passed email
                    if (currentSnapshot.child("Email").hasChild(userEmail)) {
                        // send this user a request given their ID
                        sendFriendRequest(currentSnapshot.getKey());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // re-enable the friend request button
                sendFriendRequest.setEnabled(true);
            }
        });
    }

    // Send a friend request given a user's ID
    private void sendFriendRequest(String userID) {
        DatabaseReference friendsReference = database.getReference().child("Friends").child(user.getUid());
        friendsReference.child(userID).setValue("pending");
    }


    private void getInitialLocation() {

        Thread locationThread = new Thread() {
            @Override
            public void run() {
                // get a new handler if there is no existing one
                if (handler == null) {
                    // check permissions
                    handler = new LocationHandler(getActivity());
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                    }
                }
                Location location = handler.getLocation();
                do {
                    location = handler.getLocation();
                } while (location == null);

                // update this user's location with new location
                updateUserLocation(location);
            }
        };
        locationThread.start();
    }

    // Update user's current location in Firebase
    private void updateUserLocation(Location userLocation) {
        DatabaseReference locationReference = this.database.getReference("Locations");
        GeoFire geoFire = new GeoFire(locationReference);

        // Set Location
        GeoLocation location;
        location = new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude());
        geoFire.setLocation(user.getUid(), location, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


/*
    // Add a friend for this user
    private void addFriend(String friendUserID) {
        // access this user from the database and get specific components
        DatabaseReference friendsReference = this.database.getReference("Friends").child(user.getUid());
        friendsReference.child(friendUserID).setValue(true);
    }*/
}
