package com.example.august.cs450finalproject;

public class MessageItem {

    private String friendId;
    private String friendName;

    MessageItem () {}

    public MessageItem(String friendId, String friendName) {
        this.friendId = friendId;
        this.friendName = friendName;
    }

    public String getFriendId() {
        return friendId;
    }

    public String getFriendName() {
        return friendName;
    }
}
