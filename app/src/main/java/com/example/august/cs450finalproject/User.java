package com.example.august.cs450finalproject;

import android.location.Location;

import java.util.HashSet;

// Class defining the information available to a specific user
public class User {

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

}
