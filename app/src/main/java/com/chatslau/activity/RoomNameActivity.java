package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.adapter.RecyclerViewAdapterRoom;
import com.chatslau.model.Comment;
import com.chatslau.model.RoomName;
import com.chatslau.model.Story;
import com.chatslau.model.pilih_nama;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RoomNameActivity extends AppCompatActivity implements RecyclerViewAdapterRoom.FirebaseDataListener {

    private TextView noRoom;
    private RecyclerView rvRoom;
    private String nama, receiver, GetUserId, receiverId, mTime;
    private DatabaseReference getReference;
    private FirebaseAuth auth;

    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<RoomName> dataRoom;
    private SwipeRefreshLayout srlRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Daftar Pesan");

        noRoom = findViewById(R.id.text_no_room);
        rvRoom = findViewById(R.id.rv_room);
        srlRoom = findViewById(R.id.srlroom);

        getReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        GetUserId = auth.getUid();

        MyRecyclerView();
        getNama(GetUserId);

        final Story story = (Story) getIntent().getSerializableExtra("story");
        if(story != null) {
            receiverId = story.getUid();
            receiver = story.getNama();
            cekListChat();
        }

        final Comment comment = (Comment) getIntent().getSerializableExtra("comment");
        if(comment != null) {
            receiverId = comment.getUid();
            receiver = comment.getnama_comment();
            cekListChat();
        }

        srlRoom.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srlRoom.setRefreshing(true);
                noRoom.setVisibility(View.VISIBLE);
                getListChat();
                srlRoom.setRefreshing(false);
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Inisialisasi Banner Ads
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());

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

    @Override
    protected void onResume() {
        super.onResume();
        noRoom.setVisibility(View.VISIBLE);
        getListChat();
    }

    private void setListChat(String sName, String rName, String sUid, String rUid){
        getReference.child("Room-Name").push().setValue(new RoomName(sName, rName, sUid, rUid,
                System.currentTimeMillis(), mTime, false))
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RoomNameActivity.this, "Chat Orangnya!", Toast.LENGTH_SHORT).show();
                    }
                });
        cekListChat();
    }

    private void getListChat(){
        noRoom.setVisibility(View.VISIBLE);
        final ProgressDialog pd = new ProgressDialog(RoomNameActivity.this);
        pd.setMessage("Memuat Daftar Pesan");
        pd.show();
        getReference.child("Room-Name").orderByChild("timestamp").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Inisialisasi ArrayList
                dataRoom = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //Mapping data pada DataSnapshot ke dalam objek mahasiswa
                    RoomName roomName = snapshot.getValue(RoomName.class);
                    String senderId = snapshot.child("senderId").getValue().toString();
                    String receiverId = snapshot.child("receiverId").getValue().toString();

                    if(GetUserId.equals(senderId) || GetUserId.equals(receiverId)) {
                        //Mengambil Primary Key, digunakan untuk proses Update dan Delete
                        noRoom.setVisibility(View.GONE);
                        roomName.setKey(snapshot.getKey());
                        dataRoom.add(roomName);
                    }
                }

                //Inisialisasi Adapter dan data Mahasiswa dalam bentuk Array
                adapter = new RecyclerViewAdapterRoom(dataRoom, RoomNameActivity.this);

                //Memasang Adapter pada RecyclerView
                rvRoom.setAdapter(adapter);
                pd.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Data Gagal Dimuat", Toast.LENGTH_LONG).show();
                Log.e("MyListActivity", databaseError.getDetails()+" "+databaseError.getMessage());
            }
        });
    }

    private void cekListChat(){
        getReference.child("Room-Name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //Mapping data pada DataSnapshot ke dalam objek mahasiswa
                            RoomName roomName = snapshot.getValue(RoomName.class);
                            String key = snapshot.getKey();
                            String sendId = snapshot.child("senderId").getValue().toString();
                            String recId = snapshot.child("receiverId").getValue().toString();
                            String send = snapshot.child("sender").getValue().toString();
                            String rec = snapshot.child("receiver").getValue().toString();

                            if (GetUserId.equals(sendId) && receiverId.equals(recId) ||
                                    GetUserId.equals(recId) && receiverId.equals(sendId)) {
                                Intent I = new Intent(RoomNameActivity.this, RoomChatActivity.class);
                                I.putExtra("key_room", key);
                                I.putExtra("sender", send);
                                I.putExtra("senderId", sendId);
                                I.putExtra("receiver", rec);
                                I.putExtra("receiverId", recId);
                                startActivity(I);
                                return;
                            }
                        }
                        setListChat(nama, receiver, GetUserId, receiverId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Data Gagal Dimuat", Toast.LENGTH_LONG).show();
                        Log.e("MyListActivity", databaseError.getDetails()+" "+databaseError.getMessage());
                    }
                });
    }

    private void getNama(String id){
        getReference.child("DataUser").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                        nama = pilihNama.getNama();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomNameActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Methode yang berisi kumpulan baris kode untuk mengatur RecyclerView
    private void MyRecyclerView() {
        //Menggunakan Layout Manager, Dan Membuat List Secara Vertical
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvRoom.setLayoutManager(layoutManager);
        rvRoom.setHasFixedSize(true);
    }

    public static Intent getActIntent(Activity activity) {
        // kode untuk pengambilan Intent
        return new Intent(activity, RoomNameActivity.class);
    }

    @Override
    public void onDeleteData(RoomName roomName, int position) {
        if(getReference!=null){
            getReference.child("Chat").child(roomName.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    getReference.child("Room-Name").child(roomName.getKey()).removeValue();
                    Toast.makeText(RoomNameActivity.this,"Chat dihapus", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RoomNameActivity.this, "Gagal Hapus Chat", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}