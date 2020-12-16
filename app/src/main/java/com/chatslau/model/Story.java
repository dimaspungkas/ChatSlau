package com.chatslau.model;

import android.text.format.DateFormat;

import java.io.Serializable;

public class Story implements Serializable {

    private String key;
    private String uid;
    private String nama;
    private String story;
    private String kota;
    private long timestamp;
    private String formattedTime;
    private String key_story;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey_story() {
        return key_story;
    }

    public void setKey_story(String key_story) {
        this.key_story = key_story;
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

    public Story(){
        timestamp = 0;
    }

    public Story(String uid, String nama, String story, String kota, long time, String formattedTime,
                 String key_story){
        this.uid = uid;
        this.nama = nama;
        this.story = story;
        this.kota = kota;
        this.timestamp = time;
        this.formattedTime = formattedTime;
        this.key_story = key_story;
    }
}
