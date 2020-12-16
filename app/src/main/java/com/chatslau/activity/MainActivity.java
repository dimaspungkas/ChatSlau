package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.model.pilih_nama;
import com.chatslau.notification.Token;
import com.chatslau.setting.Bantuan;
import com.chatslau.setting.Pengaturan;
import com.chatslau.user.Login;
import com.chatslau.user.ProfileActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Deklarasi Variable
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseDatabase getDatabase;
    private DatabaseReference getReference;
    private String GetUserID;
    private TextView nama_user;
    private CardView story, chatroom, history, profile;
    private InterstitialAd interstitialAd;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        story = findViewById(R.id.story);
        chatroom = findViewById(R.id.chatroom);
        history = findViewById(R.id.history);
        profile = findViewById(R.id.profile);
        nama_user = findViewById(R.id.text_nama);

        story.setOnClickListener(this);
        chatroom.setOnClickListener(this);
        history.setOnClickListener(this);
        profile.setOnClickListener(this);

        //Instance Firebasee Auth
        auth = FirebaseAuth.getInstance();

        //Menambahkan Listener untuk mengecek apakah user telah logout / keluar
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //Jika Iya atau Null, maka akan berpindah pada halaman Login
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //Toast.makeText(MainActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                } else {
                    setNama();
                }
            }
        };

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Inisialisasi Banner Ads
        AdView adView = findViewById(R.id.adView);
        // Create the InterstitialAd and set the adUnitId.
        interstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        interstitialAd.setAdUnitId(AD_UNIT_ID);
        adView.loadAd(new AdRequest.Builder().build());
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        //Toast.makeText(MainActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Toast.makeText(MainActivity.this, "onAdFailedToLoad() with error: " +
                                loadAdError.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainActivity.this, StoryActivity.class));
                    }
                });

        //Membuat Event Pada Siklus Hidup Iklan
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                //Kode disini akan di eksekusi saat Iklan Ditutup
                //Toast.makeText(getApplicationContext(), "Iklan Dititup", Toast.LENGTH_SHORT).show();
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                //Kode disini akan di eksekusi saat Iklan Gagal Dimuat
                Toast.makeText(getApplicationContext(), "Iklan Gagal Dimuat: "+adError.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                //Kode disini akan di eksekusi saat Pengguna Meniggalkan Aplikasi/Membuka Aplikasi Lain
                //Toast.makeText(getApplicationContext(), "Iklan Ditinggalkan", Toast.LENGTH_SHORT).show();
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                //Kode disini akan di eksekusi saat Pengguna Mengklik Iklan
                //Toast.makeText(getApplicationContext(), "Iklan Diklik", Toast.LENGTH_SHORT).show();
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                //Kode disini akan di eksekusi saat Iklan Selesai Dimuat
                //Toast.makeText(getApplicationContext(), "Iklan Selesai Dimuat", Toast.LENGTH_SHORT).show();
                super.onAdLoaded();
            }

        });
    }

    //Menerapkan Listener
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //Melepaskan Litener
    @Override
    protected void onStop() {
        super.onStop();
        if(authListener != null){
            auth.removeAuthStateListener(authListener);
        }
    }

    private void setNama(){
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Mengambil Data");
        pd.show();
        getReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = auth.getCurrentUser();
        GetUserID = user.getUid();
        getReference.child("DataUser").child(GetUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                            nama_user.setText(pilihNama.getNama());
                            updateToken(FirebaseInstanceId.getInstance().getToken(), GetUserID);
                        }else {
                            startActivity(new Intent(MainActivity.this, PilihNama.class));
                            Toast.makeText(MainActivity.this, "Atur Dulu Profilmu Yaa", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StoryActivity.class));
        }
    }

    private void updateToken(String token, String userId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(userId).setValue(token1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.story:
                showInterstitial();
                break;
            case R.id.chatroom:
                startActivity(new Intent(MainActivity.this, RoomNameActivity.class));
                break;
            case R.id.history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_logout, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.btn_logout) {
            auth.signOut();
        }

        if(item.getItemId()==R.id.btn_setting){
            startActivity(new Intent(MainActivity.this, Pengaturan.class));
        }

        return super.onOptionsItemSelected(item);
    }
}