package com.example.august.cs450finalproject;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

// Class defining the information available to a specific user
public class User {

    private final static String LOGTAG = User.class.getSimpleName();

    // instance variables
    private String name = null;
    private String email = null;
    private String password = null;
    private HashSet<String> friendIDs = null;
    private String latititude = null;
    private String longitude = null;

    // User constructor
    public User(String name, String email, String password, HashSet<String> friendIDs, Location location) {
        // assign the parameters to the instance variables
        this.name = name;
        this.email = email;
        this.password = password;
        this.friendIDs = friendIDs;
        this.latititude = Double.toString(location.getLatitude());
        this.longitude = Double.toString(location.getLongitude());
    }

    // Add this user to Firebase
    public void addUserToFirebase() {
        // create the user's JSON information
        HashMap<String, String> newUser = createUserJSON();
        // add that JSON information to Firebase
        writeToDatabase(newUser);
    }

    // Use this to create a JSON object for a User
    private HashMap<String, String> createUserJSON() {

        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put("Name", this.name);
        userData.put("Email", this.email);
        userData.put("Password", this.password);
        userData.put("Friends", " ");
        userData.put("Latitude", this.latititude);
        userData.put("Longitude", this.longitude);

        return userData;
    }

    // Write data to Firebase
    private void writeToDatabase(HashMap<String, String> newUser) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = database.getReference("Users");

        usersReference = usersReference.child(this.name);
        usersReference.setValue(newUser);
    }

    private void readFromDatabase() {
                /*
        // Read from the database
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(LOGTAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(LOGTAG, "Failed to read value.", error.toException());
            }
        });
        */
    }
}
