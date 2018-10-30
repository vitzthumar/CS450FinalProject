package com.example.august.cs450finalproject;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

// Class defining the information available to a specific user
public class User {

    public static final String TAG = "TEST_TAG";

    // instance variables
    private String name = null;
    private String email = null;
    private String password = null;
    private HashSet<String> friendIDs = null;
    //private HashSet<String> friendsOfFriends = null;
    private Location location = null;

    // User constructor
    public User(String name, String email, HashSet<String> friendIDs, Location location) {
        // assign the parameters to the instance variables
        this.name = name;
        this.email = email;
        this.friendIDs = friendIDs;
        this.location = location;
    }

    // Method used to update this user's location
    private void updateLocation(Location mostRecentLocation) {
        this.location = mostRecentLocation;
    }

    public void writeToDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("test");

        String concatStr = this.name + " " + this.email + this.location.toString();

        myRef.setValue(concatStr);

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}
