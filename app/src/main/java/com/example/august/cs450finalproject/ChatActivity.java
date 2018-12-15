package com.example.august.cs450finalproject;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private SimpleRVAdapter adapter;
    private ImageView sendButton;
    private EditText messageArea;
    private String chatID;
    private String friendId;
    private String userID;

    private ArrayList<Message> messages = new ArrayList<>();
    private HashMap<String, String> names = new HashMap<>();
    private HashSet<String> oldMessages = new HashSet<>();

    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Setup database
        database = FirebaseDatabase.getInstance().getReference();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message m = ds.getValue(Message.class);
                    String oldMsgHash = m.getFrom()+"@"+m.getUnixTime();
                    if (m.getRead().get(userID).equals("FALSE")) {
                        database.child("Chat").child(chatID).child("Messages").child(ds.getKey()).child("read").child(userID).setValue("TRUE");
                    }
                    if (oldMessages.contains(oldMsgHash)) {
                        // Old Message
                        adapter.notifyItemChanged(getMessagePosition(m.getUnixTime()));
                    } else {
                        // new message
                        messages.add(m);
                        adapter.setMessages(messages);
                        oldMessages.add(oldMsgHash);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Get the current User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Get intent stuff
        Intent receiveIntent = this.getIntent();
        chatID = receiveIntent.getStringExtra("CHAT_ID");
        friendId = receiveIntent.getStringExtra("CHATTING_WITH");
        userID = receiveIntent.getStringExtra("USER_ID");

        // Load the UI
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.chat_messageArea);

        // Attach a listener to the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageArea.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                messageArea.setText("");
                String unixTime = String.valueOf(System.currentTimeMillis() / 1000L);
                HashMap<String, String> read = new HashMap<>();
                read.put(userID, "TRUE");
                read.put(friendId, "FALSE");
                Message message = new Message(userID, msg, unixTime, read);
                database.child("Chat").child(chatID).child("Messages").push().setValue(message);
            }
        });

        database.child("Users").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> x = (HashMap<String, String>) dataSnapshot.getValue();
                String name = x.get("name");
                names.put(friendId, name);

                // Load the chat items
                database.child("Chat").child(chatID).child("Messages").addValueEventListener(valueEventListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Recycler View
        recyclerView = findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleRVAdapter(this.messages);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.child("Chat").child(chatID).child("Messages").removeEventListener(valueEventListener);
    }

    private int getMessagePosition (String unixTime) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getUnixTime().equals(unixTime)) {
                return i;
            }
        }
        return -1;
    }
    // RECYCLER VIEW STUFF
    public class SimpleRVAdapter extends RecyclerView.Adapter<SimpleRVAdapter.SimpleViewHolder> {
        private ArrayList<Message> dataSource;

        public SimpleRVAdapter(ArrayList<Message> dataArgs) {
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
            holder.messageItem.setText(dataSource.get(position).getMsg());
            holder.friendName.setText("");
            // holder.chatName.setText(names.get(dataSource.get(position).getFrom()));
            if (dataSource.get(position).getFrom().equals(userID)){
                holder.messageItem.setTextColor(Color.BLACK);
                holder.messageItem.setBackgroundColor(Color.GRAY);

                // Alight the chat box
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                holder.messageLayout.setLayoutParams(params);
            } else if (dataSource.get(position).getFrom().equals(friendId)) {
                holder.messageItem.setTextColor(getResources().getColor(R.color.appWhite));
                holder.messageItem.setBackgroundColor(getResources().getColor(R.color.appOrange));
                holder.friendName.setText(names.get(friendId));

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                holder.messageLayout.setLayoutParams(params);
            }

            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1);
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }

        public void setMessages(ArrayList<Message> list) {
            dataSource = list;
            notifyDataSetChanged();
        }

        class SimpleViewHolder extends RecyclerView.ViewHolder {
            public TextView messageItem;
            public TextView friendName;
            public LinearLayout messageLayout;
            public RelativeLayout relativeLayout;
            // public TextView chatName;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                messageItem = (TextView) itemView.findViewById(R.id.chat_message_item);
                friendName = (TextView) itemView.findViewById(R.id.chat_message_name);
                messageLayout = (LinearLayout) itemView.findViewById(R.id.chat_messageLayout);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.chat_realativeLayout);
                // chatName = (TextView) itemView.findViewById(R.id.chat_name);
                Context c = itemView.getContext();
            }
        }
    }
}
