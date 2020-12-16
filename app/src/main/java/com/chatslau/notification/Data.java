package com.chatslau.notification;

public class Data {
    private String user;
    private String key;
    private String body;
    private String title;
    private String sented;
    private String sender;
    private String receiver;

    public Data(String user, String key, String body, String title, String sented, String sender, String receiver) {
        this.user = user;
        this.key = key;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Data() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
