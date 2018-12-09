package com.example.august.cs450finalproject;

public class Message {

    private String from;
    private String msg;
    private String unixTime;

    public Message () {}

    public Message (String from, String msg, String unixTime) {
        this.from = from;
        this.msg = msg;
        this.unixTime = unixTime;
    }

    public String getFrom() {
        return from;
    }

    public String getMsg() {
        return msg;
    }

    public String getUnixTime() {
        return unixTime;
    }
}
