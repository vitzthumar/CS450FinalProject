package com.example.august.cs450finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {

    // Instance Variables
    private TextView dashboard_tv;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference database;

    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;
    private ValueEventListener userValueListener;

    private ArrayList<User> pendingFriends = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public NotificationFragment() {
        // Required empty public constructor
    }

    private EditText pendingET;
    private Button pendingBT;

    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        setupListeners();
        adapter = new SimpleRVAdapter(this.pendingFriends);
        database = FirebaseDatabase.getInstance().getReference();

        // get all the pending users
        DatabaseReference friendsReference = database.child("Friends").child(user.getUid());
        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot currentSnapshot: dataSnapshot.getChildren()) {
                    // is the user a pending friend request?
                    if (currentSnapshot.getValue().equals("pending")) {
                        addUserListener(currentSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addUserListener(String userId) {
        database.child("Users").child(userId)
                .addValueEventListener(userValueListener);
    }

    private void setupListeners() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setUuid(dataSnapshot.getKey());
                if (!pendingFriends.contains(u)) {
                    newUser(u);
                }
            }

            private void newUser(User u) {
                pendingFriends.add(u);
                adapter.setUsers(pendingFriends);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = rootView.findViewById(R.id.notification_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup adapters here
        adapter = new SimpleRVAdapter(pendingFriends);
        recyclerView.setAdapter(adapter);

        this.pendingET = rootView.findViewById(R.id.pendingET);
        this.pendingBT = rootView.findViewById(R.id.pendingBT);
        pendingBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send a friend request by passing an email
                findUserIDFromEmail(pendingET.getText().toString());
            }
        });
        return rootView;
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
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // RECYCLER VIEW STUFF

    /**
     * A Simple Adapter for the RecyclerView
     */
    public class SimpleRVAdapter extends RecyclerView.Adapter<NotificationFragment.SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<User> dataSource;

        public SimpleRVAdapter(ArrayList<User> dataArgs){
            dataSource = dataArgs;
        }

        @Override
        public NotificationFragment.SimpleRVAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
            NotificationFragment.SimpleRVAdapter.SimpleViewHolder viewHolder = new NotificationFragment.SimpleRVAdapter.SimpleViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(NotificationFragment.SimpleRVAdapter.SimpleViewHolder holder, int position) {
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
                                        "Email: " + u.getEmail() + "\n"

                        );
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNeutralButton("Accept",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        updateRelationship(u.uuid, "friends");
                                        removeUserFromRV(u.uuid);
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "Decline",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        updateRelationship(u.uuid, "declined");
                                        removeUserFromRV(u.uuid);




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

    private void updateRelationship(String userID, String relationship) {
        // access this user from the database and get specific components
        DatabaseReference friendsReference = database.child("Friends").child(user.getUid());

        if (relationship == "declined") {
            // if declined, remove the user from the Friends list
            friendsReference.child(userID).removeValue();
        } else {
            // update the user to the new relationship
            friendsReference.child(userID).setValue(relationship);
            // update from the other user's end as well
            friendsReference = database.child("Friends").child(userID);
            friendsReference.child(user.getUid()).setValue(relationship);
        }
    }

    // Remove a user entry from the pending request recycler view
    private void removeUserFromRV(String id) {
        for (int i = 0; i < pendingFriends.size(); i++) {
            if (pendingFriends.get(i).getUuid().equals(id)) {
                pendingFriends.remove(i);
                adapter.notifyItemRemoved(i);
                adapter.notifyItemRangeChanged(i, pendingFriends.size());
            }
        }
    }


    // Find a user's ID given their email
    private void findUserIDFromEmail(String userEmail) {
        DatabaseReference usersReference = database.child("Users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot currentSnapshot: dataSnapshot.getChildren()) {
                    // check this child to see if its email matches the passed email
                    if (currentSnapshot.child("email").getValue(String.class).equals(userEmail)) {
                        // send this user a request given their ID
                        sendFriendRequest(currentSnapshot.getKey(), userEmail);
                        break;
                    }
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
                        for (DataSnapshot currentSnapshot: dataSnapshot.getChildren()) {
                            if (currentSnapshot.getKey().equals(user.getUid())) {
                                hasFriend = true;
                            }
                        }
                        // check if the other user is already friends with this user
                        if (!hasFriend && !userEmail.equals(user.getEmail())) {
                            friendsReference.child(user.getUid()).setValue("pending");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

}
