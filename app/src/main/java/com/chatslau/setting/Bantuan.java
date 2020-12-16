package com.chatslau.setting;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.activity.HistoryActivity;
import com.chatslau.activity.MainActivity;
import com.chatslau.activity.RoomNameActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class Bantuan extends AppCompatActivity implements View.OnClickListener {

    private ImageButton img_story, img_chat, img_history, img_profile;
    private TextView tvstory, tvchat, tvhistory, tvprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bantuan);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Bantuan");

        img_story = findViewById(R.id.img_story);
        img_chat = findViewById(R.id.img_chat);
        img_history = findViewById(R.id.img_history);
        img_profile = findViewById(R.id.img_profile);
        tvstory = findViewById(R.id.tv_story);
        tvchat = findViewById(R.id.tv_roomchat);
        tvhistory = findViewById(R.id.tv_history);
        tvprofile = findViewById(R.id.tv_profile);

        img_story.setOnClickListener(this);
        img_chat.setOnClickListener(this);
        img_history.setOnClickListener(this);
        img_profile.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_story:
                if(tvstory.getVisibility() == View.VISIBLE){
                    tvstory.setVisibility(View.GONE);
                }else {
                    tvstory.setVisibility(View.VISIBLE);
                    tvchat.setVisibility(View.GONE);
                    tvhistory.setVisibility(View.GONE);
                    tvprofile.setVisibility(View.GONE);
                }
                break;
            case R.id.img_chat:
                if(tvchat.getVisibility() == View.VISIBLE){
                    tvchat.setVisibility(View.GONE);
                }else {
                    tvstory.setVisibility(View.GONE);
                    tvchat.setVisibility(View.VISIBLE);
                    tvhistory.setVisibility(View.GONE);
                    tvprofile.setVisibility(View.GONE);
                }
                break;
            case R.id.img_history:
                if(tvhistory.getVisibility() == View.VISIBLE){
                    tvhistory.setVisibility(View.GONE);
                }else {
                    tvstory.setVisibility(View.GONE);
                    tvchat.setVisibility(View.GONE);
                    tvhistory.setVisibility(View.VISIBLE);
                    tvprofile.setVisibility(View.GONE);
                }
                break;
            case R.id.img_profile:
                if(tvprofile.getVisibility() == View.VISIBLE){
                    tvprofile.setVisibility(View.GONE);
                }else {
                    tvstory.setVisibility(View.GONE);
                    tvchat.setVisibility(View.GONE);
                    tvhistory.setVisibility(View.GONE);
                    tvprofile.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}