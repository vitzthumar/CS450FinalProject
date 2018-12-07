package com.example.august.cs450finalproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;

    private ArrayList<String> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent receiveIntent = this.getIntent();
        String chattingWith = receiveIntent.getStringExtra("CHATTING_WITH");
        this.setTitle(chattingWith);

        this.messages.add("ssgsgsgsggausushuchucheuipchieuwcbwiebcwiebciwbciobwcewbchebcheuheussgsgsgsggausushuchucheuipchieuwcbwiebcwiebciwbciobwcewbchebcheuheussgsgsgsggausushuchucheuipchieuwcbwiebcwiebciwbciobwcewbchebcheuheussgsgsgsggausushuchucheuipchieuwcbwiebcwiebciwbciobwcewbchebcheuheu");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");
        this.messages.add("HELLO");
        this.messages.add("Whats up");
        this.messages.add("You there?");
        this.messages.add("yoyo");
        this.messages.add("hey yo");
        this.messages.add("mellow");

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

        public void setUsers(ArrayList<String> list) {
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
