package com.example.august.cs450finalproject;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;

    private ArrayList<String> messages = new ArrayList<>();
    private HashMap<String, String> names = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Setup database
        database = FirebaseDatabase.getInstance().getReference();

        // Get the current User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Get intent stuff
        Intent receiveIntent = this.getIntent();
        String chatID = receiveIntent.getStringExtra("CHAT_ID");
        String friendId = receiveIntent.getStringExtra("CHATTING_WITH");

        // Map Current Users Id to name
        database.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> x = (HashMap<String, String>) dataSnapshot.getValue();
                String name = x.get("name");
                names.put(user.getUid(), name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("Users").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> x = (HashMap<String, String>) dataSnapshot.getValue();
                String name = x.get("name");
                names.put(friendId, name);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("Chat").child(chatID).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message m = ds.getValue(Message.class);
                    messages.add(m.getMsg());
                    adapter.setMessages(messages);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Recycler View 1
        recyclerView = findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleRVAdapter(this.messages);
        recyclerView.setAdapter(adapter);
    }
    // RECYCLER VIEW STUFF
    public class SimpleRVAdapter extends RecyclerView.Adapter<SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<String> dataSource;

        public SimpleRVAdapter(ArrayList<String> dataArgs) {
            dataSource = dataArgs;
        }

        @Override
        public SimpleRVAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_chat, parent, false);
            SimpleRVAdapter.SimpleViewHolder viewHolder = new SimpleRVAdapter.SimpleViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SimpleRVAdapter.SimpleViewHolder holder, int position) {
            holder.messageItem.setText(dataSource.get(position));
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }

        public void setMessages(ArrayList<String> list) {
            dataSource = list;
            notifyDataSetChanged();
        }

        class SimpleViewHolder extends RecyclerView.ViewHolder {
            public TextView messageItem;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                messageItem = (TextView) itemView.findViewById(R.id.chat_message_item);
                Context c = itemView.getContext();
            }
        }
    }
}
