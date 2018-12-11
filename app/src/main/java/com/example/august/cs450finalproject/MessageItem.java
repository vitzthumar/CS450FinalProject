package com.example.august.cs450finalproject;

public class MessageItem {

    private String friendId;
    private String friendName;
    private String imageURL;

    MessageItem () {}

    public MessageItem(String friendId, String friendName, String imageURL) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.imageURL = imageURL;

    }

    public String getFriendId() {
        return friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public String getImageURL() {
        return imageURL;
    }
}
