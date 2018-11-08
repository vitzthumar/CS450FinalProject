package com.example.august.cs450finalproject;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// Class defining the information available to a specific user
@IgnoreExtraProperties
public class User {

    private final static String LOGTAG = User.class.getSimpleName();

    // instance variables
    public String name = null;
    public String email = null;
    private Set<String> friendIDs = null;
    private String latititude = null;
    private String longitude = null;

    // User constructor
    public User(String uniqueID, String name, String email) {
        // assign the parameters to the instance variables
        //this.uniqueID = uniqueID;
        this.name = name;
        this.email = email;
        this.latititude = "NOT_UPDATED";
        this.longitude = "NOT_UPDATED";
    }

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
}
