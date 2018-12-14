package com.example.august.cs450finalproject;

import java.util.HashMap;

public class Message {

    private String from;
    private String msg;
    private String unixTime;
    private HashMap<String, String> read;

    public Message () {}

    public Message (String from, String msg, String unixTime, HashMap<String, String> read) {
        this.from = from;
        this.msg = msg;
        this.unixTime = unixTime;
        this.read = read;
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

    public HashMap<String, String> getRead() {

        return read;
    }
}
