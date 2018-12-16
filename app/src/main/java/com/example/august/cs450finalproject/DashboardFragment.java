package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DashboardFragment extends Fragment {

    // Instance Variables
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private DatabaseReference database;
    private GeoFire geofire;
    private Set<GeoQuery> geoQueries = new HashSet<>();

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<User> friendsNotInRadius = new ArrayList<>();
    private ValueEventListener userValueListener;
    private ValueEventListener friendOutRadiusValueListener;
    private boolean fetchedUserIds;
    private Set<String> userIdsWithListeners = new HashSet<>();
    private Set<String> friendOutIdsWithListeners = new HashSet<>();

    private int initialListSize;
    private Map<String, Location> userIdsToLocations = new HashMap<>();
    private int iterationCount;

    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;

    private RecyclerView recyclerView2;
    private SimpleRVAdapter adapter2;

    private OnFragmentInteractionListener mListener;

    private GeoLocation USERS_CURRENT_LOCATION;

    private final static int PERMISSION_REQUEST_CODE = 999;
    private LocationHandler handler = null;

    private final static String FRIEND = "friends";
    private HashSet<String> usersFriends = new HashSet<>();

    Thread locationThread;

    private FirebaseStorage firebaseStorage;
    private TextView noFriendsInRadius;
    private TextView noFriendsOutRadius;

    ProgressDialog progressDialog;


    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        adapter = new SimpleRVAdapter(this.users);
        adapter2 = new SimpleRVAdapter(this.friendsNotInRadius);
        setupFirebase();
        setupList();
        getCurrentUsersLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Recycler View 1
        recyclerView = rootView.findViewById(R.id.dashboard_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        recyclerView2 = rootView.findViewById(R.id.dashboard_recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView2.setAdapter(adapter2);

        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        noFriendsInRadius = rootView.findViewById(R.id.noFriendsInRadius);
        noFriendsOutRadius = rootView.findViewById(R.id.noFriendsOutRadius);

        // Get users friends
        getUsersFriends();

        return rootView;
    }

    private void getUsersFriends() {
        this.database.child("Friends").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue().equals(FRIEND)) {
                        System.out.println(snapshot.getKey() + " is a friend");
                        usersFriends.add(snapshot.getKey());
                    }
                }

                // WAIT TILL THE THREAD THAT IS GETTING THE USERS CURRENT LOCATION IS DONE!
                try {
                    locationThread.join(10000);
                    if (locationThread.isAlive()) {
                        System.out.println("IT TOOK TOO LONG, SO QUIT");
                        // When all friends are rendered, delete the message
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "COULD NOT GET USER'S LOCATION", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("FINISHED");
                        System.out.println("FINISHED");
                        Location l = new Location("to");
                        l.setLatitude(USERS_CURRENT_LOCATION.latitude);
                        l.setLongitude(USERS_CURRENT_LOCATION.longitude);
                        updateUserLocation(l);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Update user's current location in Firebase
    private void updateUserLocation(Location userLocation) {
        DatabaseReference locationReference = this.database.child("Locations");
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
                // After we tried to save all the users to DB; fetch all the users
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int radius = dataSnapshot.child("radius").getValue(Integer.class);
                        fetchUsers(radius);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    public void getCurrentUsersLocation() {
        locationThread = new Thread() {
            @Override
            public void run() {
                // get a new handler if there is no existing one
                if (handler == null) {
                    // check permissions
                    handler = new LocationHandler(getActivity());
                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                    }
                }
                Location location;

                /*
                do {
                    location = handler.getLocation();
                } while (location == null);
                USERS_CURRENT_LOCATION = new GeoLocation(location.getLatitude(), location.getLongitude());
                */

                // update this user's location with new location
                //TODO: REMOVE THESE AND MAKE IT DYNAMIC
                USERS_CURRENT_LOCATION = new GeoLocation(44.58964199, -75.16173201);
            }
        };
        locationThread.start();
    }

    /***
     * This function fetches everyone (for now) who are within a certain radius passed into the parameter.
     * It then attaches an even listener to all the "User" objects that are associated in the radius.
     * Make this so it only fetches friends
     */
    private void fetchUsers (int radius) {
        progressDialog.setMessage("LOADING PEOPLE IN AREA");
        // Get everyone within the user's radius
        // THIS IS A HARD CODED VALUE; HOW CAN WE MAKE IT DYNAMIC?? --> Pass in a constant that is the users current location?
        geofire = new GeoFire(database.child("Locations"));
        GeoQuery geoQuery = geofire.queryAtLocation(USERS_CURRENT_LOCATION, radius);
        System.out.println("QUERRYING ON " + USERS_CURRENT_LOCATION.latitude + ", " + USERS_CURRENT_LOCATION.longitude);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (usersFriends.contains(key)) {
                    System.out.println(key + "is in the area; Adding event listener to it");
                    Location to = new Location("to");
                    to.setLatitude(location.latitude);
                    to.setLongitude(location.longitude);
                    if (!fetchedUserIds) {
                        userIdsToLocations.put(key, to);
                    } else {
                        userIdsToLocations.put(key, to);
                        addUserListener(key);
                    }
                    // If the user was in the out of radius recycler view, REMOVE THEM
                    if (friendOutIdsWithListeners.contains(key)){
                        int position = getFriendOutPosition(key);
                        friendsNotInRadius.remove(position);
                        adapter2.notifyItemRemoved(position);
                        adapter2.notifyItemRangeChanged(position, friendsNotInRadius.size());
                        removeFriendOutListener(key);
                        friendOutIdsWithListeners.remove(key);
                    }
                }
            }

            @Override
            public void onKeyExited(String key) {
                if (usersFriends.contains(key)) {
                    System.out.println(key + "left the area; Removing event listener from it");
                    if (userIdsWithListeners.contains(key)) {
                        int position = getUserPosition(key);
                        users.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, users.size());
                        removeUserListener(key);
                        userIdsWithListeners.remove(key);
                        addFriendOutRadiusListener(key);
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(key + "moved in the area");
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!\n");
                initialListSize = userIdsToLocations.size();
                if (initialListSize == 0) {
                    fetchedUserIds = true;
                }
                iterationCount = 0;

                userIdsToLocations.keySet().forEach(this::addUserListener);
                for (String aFriendID: usersFriends) {
                    if (!userIdsToLocations.containsKey(aFriendID)) {
                        System.out.println("Friend " + aFriendID + " is not in the radius, but adding to all friend recycler view");
                        addFriendOutRadiusListener(aFriendID);

                    }
                }
                progressDialog.dismiss();
                if (userIdsToLocations.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noFriendsInRadius.setVisibility(View.VISIBLE);

                }
                if (friendOutIdsWithListeners.isEmpty()) {
                    recyclerView2.setVisibility(View.GONE);
                    noFriendsOutRadius.setVisibility(View.VISIBLE);

                }
            }

            private void addUserListener(String userId) {
                database.child("Users").child(userId)
                        .addValueEventListener(userValueListener);

                userIdsWithListeners.add(userId);
            }

            private void addFriendOutRadiusListener(String userId) {
                database.child("Users").child(userId)
                        .addValueEventListener(friendOutRadiusValueListener);

                friendOutIdsWithListeners.add(userId);
            }

            private void removeUserListener(String userId) {
                database.child("Users").child(userId)
                        .removeEventListener(userValueListener);

                userIdsWithListeners.remove(userId);
            }

            private void removeFriendOutListener(String userId) {
                database.child("Users").child(userId)
                        .removeEventListener(friendOutRadiusValueListener);

                friendOutIdsWithListeners.remove(userId);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

        geoQueries.add(geoQuery);
    }

    private void setupListeners() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setUuid(dataSnapshot.getKey());
                Location location = userIdsToLocations.get(dataSnapshot.getKey());
                u.setLat(location.getLatitude());
                u.setLng(location.getLongitude());
                u.setImageURL("https://firebasestorage.googleapis.com/v0/b/cs450finalproject-a2875.appspot.com/o/defaultProfile.png?alt=media&token=6cf85b3d-b9e5-47ff-85c3-5e62d4a495b9");
                // Get the users profile image
                database.child("URL").child(u.getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        u.setImageURL(dataSnapshot.getValue().toString());
                        if (users.contains(u)) {
                            userUpdated(u);
                        } else {
                            newUser(u);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            private void newUser(User u) {
                System.out.println("onDataChange: new user");
                iterationCount++;
                users.add(u);
                if (!fetchedUserIds && iterationCount == initialListSize) {
                    fetchedUserIds = true;
                    adapter.setUsers(users);
                } else if (fetchedUserIds) {
                    adapter.notifyItemChanged(getIndexOfNewUser(u));
                }
            }

            private void userUpdated(User u) {
                System.out.println("onDataChange: update");
                users.add(u);
                adapter.notifyItemChanged(getUserPosition(u.getUuid()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        friendOutRadiusValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setUuid(dataSnapshot.getKey());
                u.setImageURL("https://firebasestorage.googleapis.com/v0/b/cs450finalproject-a2875.appspot.com/o/defaultProfile.png?alt=media&token=6cf85b3d-b9e5-47ff-85c3-5e62d4a495b9");
                // Get the users profile image
                database.child("URL").child(u.getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        u.setImageURL(dataSnapshot.getValue().toString());
                        if (friendsNotInRadius.contains(u)) {
                            friendUpdated(u);
                        } else {
                            newFriend(u);
                        }
                    }

                    private void newFriend(User u) {
                        System.out.println("onDataChange: new user");
                        friendsNotInRadius.add(u);
                        adapter2.setUsers(friendsNotInRadius);
                    }

                    private void friendUpdated(User u) {
                        System.out.println("onDataChange: update");
                        friendsNotInRadius.add(u);
                        adapter2.notifyItemChanged(getFriendOutPosition(u.getUuid()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        setupListeners();
    }

    private void setupList() {
    }

    // Change thses so it gets by ID
    private int getIndexOfNewUser(User u) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUuid().equals(u.getUuid())) {
                return i;
            }
        }
        throw new RuntimeException();
    }

    // Change these so it gets by ID
    private int getUserPosition(String id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUuid().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int getFriendOutPosition(String id) {
        for (int i = 0; i < friendsNotInRadius.size(); i++) {
            if (friendsNotInRadius.get(i).getUuid().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void removeListeners() {
        for (GeoQuery geoQuery : geoQueries) {
            geoQuery.removeAllListeners();
        }

        for (String userId : userIdsWithListeners) {
            database.child("users").child(userId)
                    .removeEventListener(userValueListener);
        }
    }

    private double distance(double startLat, double startLong, double endLat, double endLong) {

        final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

        // TODO remove these printlns
        System.out.println("Calculating...");
        System.out.println("startLat " + startLat);
        System.out.println("startLon " + startLong);
        System.out.println("endLat " + endLat);
        System.out.println("endLong " + endLong);
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        System.out.println("Distance Travlled: " + EARTH_RADIUS * c + " [KM]");
        return EARTH_RADIUS * c; // <-- dist (in KILOMETERS)
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        removeListeners();
    }

    public User getUser(int position) {
        if (position > users.size() - 1) {
            return new User();
        } else {
            return users.get(position);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // FIREBASE STORAGE

    // RECYCLER VIEW STUFF
    public class SimpleRVAdapter extends RecyclerView.Adapter<SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<User> dataSource;

        public SimpleRVAdapter(ArrayList<User> dataArgs){
            dataSource = dataArgs;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
            SimpleViewHolder viewHolder = new SimpleViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            holder.friendListName.setText(dataSource.get(position).getName());
            if (userIdsWithListeners.contains(dataSource.get(position).getUuid())) {
                String dist = dataSource.get(position).getDistanceTo(USERS_CURRENT_LOCATION);
                holder.friendListDist.setText(dist);
            }
            downloadFromURL(dataSource.get(position).getImageURL(), holder.friendListImage);
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }

        public void setUsers(ArrayList<User> list) {
            dataSource = list;
            notifyDataSetChanged();
        }

        private void downloadFromURL(String url, ImageView friendListImage) {

            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);

            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // get the bitmap and then update the profile image
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    friendListImage.setImageDrawable(null);
                    friendListImage.setImageBitmap(bitmap);
                    friendListImage.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Try getting the default
                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/cs450finalproject-a2875.appspot.com/o/defaultProfile.png?alt=media&token=6cf85b3d-b9e5-47ff-85c3-5e62d4a495b9");
                    storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {

                            // get the bitmap and then update the profile image
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            friendListImage.setImageDrawable(null);
                            friendListImage.setImageBitmap(bitmap);
                            friendListImage.setEnabled(true);
                        }
                    });
                }
            });
        }

        /**
         * A Simple ViewHolder for the RecyclerView
         */
        class SimpleViewHolder extends RecyclerView.ViewHolder{
            public TextView friendListName;
            public TextView friendListDist;
            public ImageView friendListImage;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                friendListName = (TextView) itemView.findViewById(R.id.friend_list_item_name);
                friendListImage = (ImageView) itemView.findViewById(R.id.friend_list_item_image);
                friendListDist = itemView.findViewById(R.id.friend_list_item_distance);
                Context c = itemView.getContext();
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        User u = dataSource.get(getAdapterPosition());

                        if (userIdsWithListeners.contains(u.getUuid())) {
                            // only set the map button if the other user wants their location to be displayed
                            database.child("Users").child(u.getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
                                    builder1.setMessage(
                                            "Name: " + u.getName() + "\n" +
                                                    "Email: " + u.getEmail() + "\n" +
                                                    "Distance to you: " + u.getDistanceTo(USERS_CURRENT_LOCATION) + "\n"
                                    );
                                    builder1.setCancelable(true);

                                    builder1.setPositiveButton(
                                            "Back",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    if (dataSnapshot.child("display_location").getValue(Boolean.class)) {
                                        builder1.setNegativeButton(
                                                "See on Map",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();

                                                        // get the information about each user's location and pin them on the map
                                                        Intent intent = new Intent(getContext(), MapsActivity.class);
                                                        intent.putExtra("OTHER_LAT", String.valueOf(u.lat));
                                                        intent.putExtra("OTHER_LON", String.valueOf(u.lng));
                                                        intent.putExtra("USER_LAT", String.valueOf(USERS_CURRENT_LOCATION.latitude));
                                                        intent.putExtra("USER_LON", String.valueOf(USERS_CURRENT_LOCATION.longitude));
                                                        intent.putExtra("USER_ID", user.getUid());
                                                        intent.putExtra("OTHER_ID", u.uuid);
                                                        intent.putExtra("OTHER_NAME", u.name);
                                                        startActivity(intent);
                                                    }
                                                });
                                    }

                                    // Send message button
                                    builder1.setNeutralButton(
                                            "Send a Message",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                    String userId = user.getUid();
                                                    String friendId = u.getUuid();
                                                    String chatId = userId.compareTo(friendId) < 0 ? userId+"-"+friendId : friendId+"-"+userId;
                                                    intent.putExtra("CHAT_ID", chatId);
                                                    intent.putExtra("CHATTING_WITH", friendId);
                                                    intent.putExtra("USER_ID", user.getUid());

                                                    database.child("Chat").child(chatId).child("Users").child("User1").setValue(user.getUid());
                                                    database.child("Chat").child(chatId).child("Users").child("User2").setValue(friendId);

                                                    startActivity(intent);
                                                }
                                            });

                                    AlertDialog alert11 = builder1.create();
                                    alert11.show();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        } else if (friendOutIdsWithListeners.contains(u.getUuid())) {
                            // Do something when clicked
                        }
                    }
                });
            }
        }
    }
}
