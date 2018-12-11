package com.example.august.cs450finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.HashSet;

public class MessagesFragment extends Fragment {
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;
    private TextView messageLoader;
    private ArrayList<MessageItem> messages = new ArrayList<>();
    private HashSet<String> renderedFriends = new HashSet<>();
    private FirebaseStorage firebaseStorage;

    private OnFragmentInteractionListener mListener;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup database
        database = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        // Get the current User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        // UI stuff
        messageLoader = rootView.findViewById(R.id.message_fragment_loader);
        messageLoader.setText("LOADING...");

        database.child("Chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String[] users = ds.getKey().split("-");
                    if (users[0].equals(user.getUid())) {
                        // Do something
                        if (renderedFriends.contains(users[1])) {
                            adapter.notifyItemChanged(getFriendPosition(users[1]));
                        } else {
                            // ACCESS Friends Name
                            database.child("Users").child(users[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, String> x = (HashMap<String, String>) dataSnapshot.getValue();
                                    String name = x.get("name");

                                    // Get the Profile Image
                                    database.child("URL").child(users[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            messages.add(new MessageItem(users[1], name, dataSnapshot.getValue().toString()));
                                            adapter.setMessageItem(messages);
                                            renderedFriends.add(users[1]);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    } else if (users[1].equals(user.getUid())) {
                        if (renderedFriends.contains(users[0])) {
                            adapter.notifyItemChanged(getFriendPosition(users[0]));
                        } else {
                            // ACCESS Friends Name
                            database.child("Users").child(users[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, String> x = (HashMap<String, String>) dataSnapshot.getValue();
                                    String name = x.get("name");

                                    // Get the Profile Image
                                    database.child("URL").child(users[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            messages.add(new MessageItem(users[0], name, dataSnapshot.getValue().toString()));
                                            adapter.setMessageItem(messages);
                                            renderedFriends.add(users[0]);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Recycler View
        recyclerView = rootView.findViewById(R.id.messages_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SimpleRVAdapter(this.messages);
        recyclerView.setAdapter(adapter);

        messageLoader.setText("");

        return rootView;
    }

    private int getFriendPosition (String id) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getFriendId().equals(id)) {
                return i;
            }
        }
        return -1;
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
    public class SimpleRVAdapter extends RecyclerView.Adapter<SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<MessageItem> dataSource;

        public SimpleRVAdapter(ArrayList<MessageItem> dataArgs){
            dataSource = dataArgs;
        }

        @Override
        public SimpleRVAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            SimpleRVAdapter.SimpleViewHolder viewHolder = new SimpleRVAdapter.SimpleViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SimpleRVAdapter.SimpleViewHolder holder, int position) {
            holder.friendsName.setText(dataSource.get(position).getFriendName());
            downloadFromURL(dataSource.get(position).getImageURL(), holder.friendsImage);
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }

        public void setMessageItem(ArrayList<MessageItem> list) {
            dataSource = list;
            notifyDataSetChanged();
        }

        private void downloadFromURL(String url, ImageView friendsImage) {

            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);

            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // get the bitmap and then update the profile image
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    friendsImage.setImageDrawable(null);
                    friendsImage.setImageBitmap(bitmap);
                    friendsImage.setEnabled(true);
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
                            friendsImage.setImageDrawable(null);
                            friendsImage.setImageBitmap(bitmap);
                            friendsImage.setEnabled(true);
                        }
                    });
                }
            });
        }

        /**
         * A Simple ViewHolder for the RecyclerView
         */
        class SimpleViewHolder extends RecyclerView.ViewHolder{
            public TextView friendsName;
            public ImageView friendsImage;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                friendsName = (TextView) itemView.findViewById(R.id.message_friends_name);
                friendsImage = (ImageView) itemView.findViewById(R.id.message_item_image);
                Context c = itemView.getContext();
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String friendId = dataSource.get(getAdapterPosition()).getFriendId();
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        String userId = user.getUid();
                        String chatId = userId.compareTo(friendId) < 0 ? userId+"-"+friendId : friendId+"-"+userId;
                        intent.putExtra("CHAT_ID", chatId);
                        intent.putExtra("CHATTING_WITH", friendId);
                        intent.putExtra("USER_ID", user.getUid());

                        database.child("Chat").child(chatId).child("Users").child("User1").setValue(user.getUid());
                        database.child("Chat").child(chatId).child("Users").child("User2").setValue(friendId);

                        startActivity(intent);
                    }
                });
            }
        }
    }
}
