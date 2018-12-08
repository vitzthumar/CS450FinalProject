package com.example.august.cs450finalproject;

public class Message {

    private String from;
    private String msg;

    public Message () {}

    public Message (String from, String msg) {
        this.from = from;
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public String getMsg() {
        return msg;
    }
}
