package com.chatslau.model;

import android.text.format.DateFormat;

import java.io.Serializable;

public class Comment implements Serializable {
    private String key;
    private String nama_comment;
    private String comment;
    private String uid;
    private long timestamp;
    private String formattedTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getnama_comment() {
        return nama_comment;
    }

    public void setnama_comment(String nama_comment) {
        this.nama_comment = nama_comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTime(long time) {
        this.timestamp = time;

        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - time;

        if(timeDifference < oneDayInMillis){
            formattedTime = DateFormat.format("hh:mm a", time).toString();
        }else{
            formattedTime = DateFormat.format("dd MMM - hh:mm a", time).toString();
        }
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getFormattedTime(){
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - timestamp;

        if(timeDifference < oneDayInMillis){
            return DateFormat.format("hh:mm a", timestamp).toString();
        }else{
            return DateFormat.format("dd MMM - hh:mm a", timestamp).toString();
        }
    }

    public Comment(){
        timestamp = 0;
    }

    public Comment(String nama_comment, String comment, String uid, long time, String formattedTime){
        this.nama_comment = nama_comment;
        this.comment = comment;
        this.uid = uid;
        this.timestamp = time;
        this.formattedTime = formattedTime;
    }
}
