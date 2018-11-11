package com.example.august.cs450finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.databinding.DataBindingUtil;

public class DashboardFragment extends Fragment {

    // Instance Variables
    private TextView dashboard_tv;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private DatabaseReference database;
    private GeoFire geofire;
    private Set<GeoQuery> geoQueries = new HashSet<>();

    private ArrayList<User> users = new ArrayList<>();
    private ValueEventListener userValueListener;
    private boolean fetchedUserIds;
    private Set<String> userIdsWithListeners = new HashSet<>();

    private int initialListSize;
    private Map<String, Location> userIdsToLocations = new HashMap<>();
    private int iterationCount;

    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;

    private OnFragmentInteractionListener mListener;

    private GeoLocation USERS_CURRENT_LOCATION;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        adapter = new SimpleRVAdapter(this.users);
        USERS_CURRENT_LOCATION = getCurrentUsersLocation();
        setupFirebase();
        setupList();
        fetchUsers(100);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = rootView.findViewById(R.id.dashboard_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup adapters here
        recyclerView.setAdapter(adapter);

        //dashboard_tv = (TextView)rootView.findViewById(R.id.dashboard_text_view);


        return rootView;
    }

    /***
     * This function fetches everyone (for now) who are within a certain radius passed into the parameter.
     * It then attaches an even listener to all the "User" objects that are associated in the radius.
     * Make this so it only fetches friends
     */
    private void fetchUsers (int radius) {
        // Get everyone within 100KM
        // THIS IS A HARD CODED VALUE; HOW CAN WE MAKE IT DYNAMIC?? --> Pass in a constant that is the users current location?
        GeoQuery geoQuery = geofire.queryAtLocation(USERS_CURRENT_LOCATION, radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
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

            @Override
            public void onKeyExited(String key) {
                System.out.println(key + "left the area; Removing event listener from it");
                if (userIdsWithListeners.contains(key)) {
                    int position = getUserPosition(key);
                    users.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, users.size());
                    removeUserListener(key);
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
                if (users.contains(u)) {
                    userUpdated(u);
                } else {
                    newUser(u);
                }
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
    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference();
        geofire = new GeoFire(database.child("Locations"));
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

    private void removeListeners() {
        for (GeoQuery geoQuery : geoQueries) {
            geoQuery.removeAllListeners();
        }

        for (String userId : userIdsWithListeners) {
            database.child("users").child(userId)
                    .removeEventListener(userValueListener);
        }
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

    public GeoLocation getCurrentUsersLocation() {
        return new GeoLocation(40.712776, -74.005974);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // RECYCLER VIEW STUFF

    /**
     * A Simple Adapter for the RecyclerView
     */
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
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }

        public void setUsers(ArrayList<User> list) {
            dataSource = list;
            notifyDataSetChanged();
        }

        /**
         * A Simple ViewHolder for the RecyclerView
         */
        class SimpleViewHolder extends RecyclerView.ViewHolder{
            public TextView friendListName;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                friendListName = (TextView) itemView.findViewById(R.id.friend_list_item_name);
                Context c = itemView.getContext();
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        User u = dataSource.get(getAdapterPosition());
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
                        builder1.setMessage(
                                "Name: " + u.getName() + "\n" +
                                "Email: " + u.getEmail() + "\n" +
                                "Distance to you: " + u.getDistanceTo(USERS_CURRENT_LOCATION) + "\n"
                        );
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "See on Map",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });
            }
        }
    }
}
