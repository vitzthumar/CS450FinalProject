package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainFragment extends Fragment {

    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;

    private final static String LOGTAG = MainFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private final static int PERMISSION_REQUEST_CODE = 999;
    private LocationHandler handler = null;

    private GeoFire geofire;
    private GeoLocation USERS_CURRENT_LOCATION = null;

    // Firebase instance
    DatabaseReference database;
    private final static String FRIEND = "friends";
    private HashSet<String> usersFriends = new HashSet<>();
    private HashSet<String> friendsOfFriends = new HashSet<>();
    private ArrayList<User> friendsOfFriendsArray = new ArrayList<>();
    private HashMap<String, HashSet<String>> mutualFriendTracker = new HashMap<>();
    private ValueEventListener userValueListener;
    private boolean fetchedUserIds;
    private Map<String, Location> userIdsToLocations = new HashMap<>();
    private Set<String> userIdsWithListeners = new HashSet<>();
    private int initialListSize;
    private int iterationCount;
    private Set<GeoQuery> geoQueries = new HashSet<>();
    private TextView main_load_message;
    private Thread locationThread;

    private FirebaseStorage firebaseStorage;

    private EditText pendingET;
    private Button pendingBT;

    ProgressDialog progressDialog;

    // Required empty constructor
    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // set the Firebase instance
        this.database = FirebaseDatabase.getInstance().getReference();
        adapter = new SimpleRVAdapter(this.friendsOfFriendsArray);
        firebaseStorage = FirebaseStorage.getInstance();
        setupListeners();
        // Auth stuff
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        getInitialLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = rootView.findViewById(R.id.main_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Setup adapters here
        recyclerView.setAdapter(adapter);

        main_load_message = rootView.findViewById(R.id.main_load_message);


        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        this.pendingET = rootView.findViewById(R.id.pendingET);
        this.pendingBT = rootView.findViewById(R.id.pendingBT);
        pendingBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send a friend request by passing an email
                findUserIDFromEmail(pendingET.getText().toString());
                pendingET.setText(null);
            }
        });

        getFriends();

        return rootView;
    }

    private void getFriends() {
        this.database.child("Friends").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue().equals(FRIEND)) {
                        System.out.println(snapshot.getKey() + " is a friend");
                        usersFriends.add(snapshot.getKey());
                    }
                }
                progressDialog.setMessage("LOADING FRIENDS OF FRIENDS");
                getFriendsOfFriends();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Find a user's ID given their email
    private void findUserIDFromEmail(String userEmail) {
        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending Friend Request...");
        progressDialog.show();
        DatabaseReference usersReference = database.child("Users");
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot currentSnapshot: dataSnapshot.getChildren()) {
                    // check this child to see if its email matches the passed email
                    if (currentSnapshot.child("email").getValue(String.class).equals(userEmail)) {
                        // send this user a request given their ID
                        found = true;
                        sendFriendRequest(currentSnapshot.getKey(), userEmail);
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(getContext(), "No user with this email was found", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Send a friend request given a user's ID
    public void sendFriendRequest(String userID, String userEmail) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // re-enable the friend request button
                DatabaseReference friendsReference = database.child("Friends").child(userID);
                friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean hasFriend = false;
                        for (DataSnapshot currentSnapshot : dataSnapshot.getChildren()) {
                            if (currentSnapshot.getKey().equals(user.getUid())) {
                                hasFriend = true;
                            }
                        }
                        // check if the other user is already friends with this user
                        if (!hasFriend && !userEmail.equals(user.getEmail())) {
                            friendsReference.child(user.getUid()).setValue("pending");
                            Toast.makeText(getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!hasFriend) {
                                Toast.makeText(getContext(), "That's your email", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Request already sent!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    public void getFriendsOfFriends () {
        for (String friendsId : usersFriends) {
            this.database.child("Friends").child(friendsId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!(snapshot.getKey().equals(user.getUid())) && !(usersFriends.contains(snapshot.getKey()))) {
                            // If inside this block, the friend of friend is not a current friend
                            if (!(friendsOfFriends.contains(snapshot.getKey()))) {
                                // if inside this code block, it's the first time we are encountering this friend of friend; so render
                                System.out.println(snapshot.getKey() + " is a friend of friend");
                                friendsOfFriends.add(snapshot.getKey());
                            }
                            if (mutualFriendTracker.containsKey(snapshot.getKey())) {
                                mutualFriendTracker.get(snapshot.getKey()).add(friendsId);
                            } else {
                                HashSet<String> mutualFriends = new HashSet<>();
                                mutualFriends.add(friendsId);
                                mutualFriendTracker.put(snapshot.getKey(), mutualFriends);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        try {
            locationThread.join(10000);
            if (locationThread.isAlive()) {
                System.out.println("STILL RUNNING");
                main_load_message.setText("ERROR: Cannot load users location");
            } else {
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

    private void getInitialLocation() {
        locationThread = new Thread() {
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
                //TODO: REMOVE THESE AND MAKE IT DYNAMIC
                USERS_CURRENT_LOCATION = new GeoLocation(location.getLatitude(), location.getLongitude());
                //USERS_CURRENT_LOCATION = new GeoLocation(44.58964199, -75.16173201);
            }
        };
        locationThread.start();
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

    private void fetchUsers (int radius) {
        progressDialog.setMessage("GETTING FRIEND OF FRIENDS IN AREA");
        // Get everyone within the user's radius
        // THIS IS A HARD CODED VALUE; HOW CAN WE MAKE IT DYNAMIC?? --> Pass in a constant that is the users current location?
        geofire = new GeoFire(database.child("Locations"));
        GeoQuery geoQuery = geofire.queryAtLocation(USERS_CURRENT_LOCATION, radius);
        System.out.println("QUERRYING ON " + USERS_CURRENT_LOCATION.latitude + ", " + USERS_CURRENT_LOCATION.longitude);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (friendsOfFriends.contains(key)) {
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
                }
            }

            @Override
            public void onKeyExited(String key) {
                if (friendsOfFriends.contains(key)) {
                    System.out.println(key + "left the area; Removing event listener from it");
                    if (userIdsWithListeners.contains(key)) {
                        int position = getUserPosition(key);
                        friendsOfFriendsArray.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, friendsOfFriendsArray.size());
                        removeUserListener(key);
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
                    }
                }
                progressDialog.dismiss();
                main_load_message.setText("");
            }

            private void addUserListener(String userId) {
                database.child("Users").child(userId)
                        .addValueEventListener(userValueListener);

                userIdsWithListeners.add(userId);
            }

            private void removeUserListener(String userId) {
                database.child("Users").child(userId)
                        .removeEventListener(userValueListener);

                userIdsWithListeners.remove(userId);
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
                        if (friendsOfFriendsArray.contains(u)) {
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
                friendsOfFriendsArray.add(u);
                if (!fetchedUserIds && iterationCount == initialListSize) {
                    fetchedUserIds = true;
                    adapter.setUsers(friendsOfFriendsArray);
                } else if (fetchedUserIds) {
                    adapter.notifyItemChanged(getIndexOfNewUser(u));
                }
            }

            private void userUpdated(User u) {
                System.out.println("onDataChange: update");
                friendsOfFriendsArray.add(u);
                adapter.notifyItemChanged(getUserPosition(u.getUuid()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private int getIndexOfNewUser(User u) {
        for (int i = 0; i < friendsOfFriendsArray.size(); i++) {
            if (friendsOfFriendsArray.get(i).getUuid().equals(u.getUuid())) {
                return i;
            }
        }
        throw new RuntimeException();
    }

    private int getUserPosition(String id) {
        for (int i = 0; i < friendsOfFriendsArray.size(); i++) {
            if (friendsOfFriendsArray.get(i).getUuid().equals(id)) {
                return i;
            }
        }
        return -1;
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

    public class SimpleRVAdapter extends RecyclerView.Adapter<SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<User> dataSource;

        public SimpleRVAdapter(ArrayList<User> dataArgs){
            dataSource = dataArgs;
        }

        @Override
        public SimpleRVAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
            SimpleRVAdapter.SimpleViewHolder viewHolder = new SimpleRVAdapter.SimpleViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SimpleRVAdapter.SimpleViewHolder holder, int position) {
            holder.friendListName.setText(dataSource.get(position).getName());
            String dist = dataSource.get(position).getDistanceTo(USERS_CURRENT_LOCATION);
            holder.friendListDist.setText(dist);
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
                        StringBuilder sb = new StringBuilder();

                        database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (mutualFriendTracker.get(u.uuid).contains(ds.getKey())) {
                                        sb.append(ds.child("name").getValue());
                                        sb.append("\n");
                                    }
                                }
                                buildDialogBox(u, sb);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            private void buildDialogBox(User u, StringBuilder sb) {

                Context context = getContext();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                LayoutInflater inflater= LayoutInflater.from(getContext());
                View mutualFriends = inflater.inflate(R.layout.mutualfriendslist, null);
                TextView mutualFriendsTV = mutualFriends.findViewById(R.id.mutualFriendList_tv);
                mutualFriendsTV.setText(sb.toString());
                alertDialog.setTitle(u.getName());

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                // mutual friends
                final TextView mutualFriendsTitle = new TextView(context);
                mutualFriendsTitle.setPadding(30,10,30,5);
                int mutualFriendCount = mutualFriendTracker.get(u.uuid).size();
                String title;
                if (mutualFriendCount > 1) {
                    // more than 1 mutual friend
                    title = mutualFriendCount + " Mutual Friends";
                } else {
                    // 1 mutual friend
                    title = mutualFriendCount + " Mutual Friend";
                }
                mutualFriendsTitle.setText(title);
                //mutualFriendsTitle.setWidth();
                // add the views to the linear layout
                layout.addView(mutualFriendsTitle);
                layout.addView(mutualFriends);

                database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DataSnapshot otherDS = dataSnapshot.child(u.getUuid()).child("interests");
                        DataSnapshot myDS = dataSnapshot.child(user.getUid()).child("interests");
                        boolean[] interestArray = new boolean[8];
                        int i = 0;
                        for (DataSnapshot ds : otherDS.getChildren()) {
                            boolean value = ds.getValue(Boolean.class);
                            interestArray[i] = value;
                            i++;
                        }
                        i = 0;
                        List<String> commonInterests = new ArrayList<>();
                        for (DataSnapshot ds : myDS.getChildren()) {
                            boolean value = ds.getValue(Boolean.class);
                            if (value && interestArray[i]) {
                                // this interest is shared
                                commonInterests.add(ds.getKey());
                            }
                        }

                        // common interests
                        if (commonInterests.size() > 0) {

                            StringBuilder commonInterestBuilder = new StringBuilder("Common interests: ");

                            for (int count = 0; count < commonInterests.size(); count++) {
                                String interest = commonInterests.get(count);
                                if (count + 1 == commonInterests.size()) {
                                    commonInterestBuilder.append(" ");
                                    commonInterestBuilder.append(interest);
                                } else {
                                    commonInterestBuilder.append(" ");
                                    commonInterestBuilder.append(interest);
                                    commonInterestBuilder.append(" ");
                                    commonInterestBuilder.append(", ");
                                }
                            }

                            final TextView commonInterestsTV = new TextView(context);
                            commonInterestsTV.setPadding(30,30,30,30);
                            commonInterestsTV.setText(commonInterestBuilder);
                            //commonInterestsTV.setGravity(Gravity.CENTER);
                            layout.addView(commonInterestsTV);
                            //layout.setGravity(Gravity.CENTER)
                        }
                        alertDialog.setPositiveButton("Back", null);
                        alertDialog.setNeutralButton("Send Friend Request",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        sendFriendRequest(u.uuid, u.email);
                                        dialog.cancel();
                                    }
                                });
                        alertDialog.setView(layout);
                        AlertDialog alert = alertDialog.create();
                        alert.show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }
}
