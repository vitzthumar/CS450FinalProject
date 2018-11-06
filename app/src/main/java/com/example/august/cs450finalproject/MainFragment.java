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

        // Auth stuff
        Email = (TextView)rootView.findViewById(R.id.profileEmail);
        Uid = (TextView)rootView.findViewById(R.id.profileUid);
        mAuth = FirebaseAuth.getInstance();
        logout = (Button)rootView.findViewById(R.id.button_logout);
        user = mAuth.getCurrentUser();

        if (user != null){
            String email = user.getEmail();
            String uid = user.getUid();
            Email.setText(email);
            Uid.setText(uid);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user !=null){
                    mAuth.signOut();
                    getActivity().finish();
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
            }
        });

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
        Thread thread = new Thread(){
            public void run(){

                writeUserToDatabase("August Vitzthum", "vitzthum.ar@gmail.com", "password");
                writeUserToDatabase("Nevaan Perera", "nevaan9@gmail.com", "password");
                writeUserToDatabase("Dasha Alekseeva", "dalek15@stlawu.edu", "password");
                writeUserToDatabase("Tali Makovsky", "tmako17@stlawu.edu", "12345");
                readUserFromDatabase("vitzthumargmailcom", new UserCallback() {
                    @Override
                    public void onCallback(User readUser) {
                        // this is where we get the user read from Firebase
                        Log.e(LOGTAG, "USER NAME = " + readUser.name);
                    }
                });
            }
        };
        thread.start();
    }




    // Read user from Firebase
    private void readUserFromDatabase(final String uniqueUserID, final UserCallback userCallback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // access the user from the database and get specific components
        DatabaseReference usersReference = database.getReference("Users").child(uniqueUserID);


        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = (String) dataSnapshot.child("name").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                String password = (String) dataSnapshot.child("password").getValue();
                String uniqueID = (String) dataSnapshot.child("uniqueID").getValue();

                // TODO: Find a way to get this new User back to readUserFromDatabase()
                User readUser = new User(uniqueID, name, email, password);
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


    // Write a new user to Firebase
    private void writeUserToDatabase(String name, String email, String password) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = database.getReference("Users");

        // generate this user's unique ID by stripping the email of all irregular symbols
        String uniqueUserID = email.replaceAll("[!#$%&'*+-/=?^_`{|}~@.]","");

        // create the new user that will be added from the supplied parameters
        User newUser = new User(uniqueUserID, name, email, password);

        // set the value in the database under the unique ID
        usersReference = usersReference.child(uniqueUserID);
        usersReference.setValue(newUser);
    }
}
