package com.chatslau.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.adapter.RecyclerViewAdapter;
import com.chatslau.model.Story;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StoryActivity extends AppCompatActivity implements RecyclerViewAdapter.FirebaseDataListener{

    //Deklarasi Variable untuk RecyclerView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    //Deklarasi Variable Database Reference dan ArrayList dengan Parameter Class Model kita.
    private DatabaseReference reference;
    private ArrayList<Story> dataStory;

    private FirebaseAuth auth;
    private FloatingActionButton btnStory;
    private SwipeRefreshLayout srlStory;
    private TextView tvNoStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Cerita Mereka");

        tvNoStory = findViewById(R.id.text_no_story);
        srlStory = findViewById(R.id.srlStory);
        btnStory = findViewById(R.id.btn_story);
        recyclerView = findViewById(R.id.rv_story);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        MyRecyclerView();

        srlStory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srlStory.setRefreshing(true);
                tvNoStory.setVisibility(View.VISIBLE);
                GetStory();
                srlStory.setRefreshing(false);
            }
        });

        btnStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoryActivity.this, PostActivity.class));
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
        tvNoStory.setVisibility(View.VISIBLE);
        GetStory();
    }

    //Berisi baris kode untuk mengambil data dari Database dan menampilkannya kedalam Adapter
    private void GetStory(){
        final ProgressDialog pd = new ProgressDialog(StoryActivity.this);
        pd.setMessage("Memuat Cerita");
        pd.show();
        reference.child("Story").orderByKey().limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Inisialisasi ArrayList
                        dataStory = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            //Mapping data pada DataSnapshot ke dalam objek mahasiswa
                            Story story = snapshot.getValue(Story.class);
                            String key = snapshot.getKey();

                            //Mengambil Primary Key, digunakan untuk proses Update dan Delete
                            story.setKey(key);
                            dataStory.add(story);
                            tvNoStory.setVisibility(View.GONE);
                            oldStory(key);
                        }

                        //Inisialisasi Adapter dan data Mahasiswa dalam bentuk Array
                        adapter = new RecyclerViewAdapter(dataStory, StoryActivity.this);

                        //Memasang Adapter pada RecyclerView
                        recyclerView.setAdapter(adapter);
                        pd.dismiss();
                        onStop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                      /*
                        Kode ini akan dijalankan ketika ada error dan
                        pengambilan data error tersebut lalu memprint error nya
                        ke LogCat
                       */
                        Toast.makeText(getApplicationContext(),"Data Gagal Dimuat", Toast.LENGTH_LONG).show();
                        Log.e("MyListActivity", databaseError.getDetails()+" "+databaseError.getMessage());
                    }
                });
    }

    private void oldStory(String key){
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        Query oldItems = reference.child("Story").orderByChild("timestamp").endAt(cutoff);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    oldComment(key);
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private void oldComment(String key){
        reference.child("Comment").child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(StoryActivity.this, "Komen dihapus", Toast.LENGTH_LONG).show();
            }
        });

        reference.child("Likes").child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(StoryActivity.this, "Komen dihapus", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDeleteData(Story story, final int position) {
        if(reference!=null){
            reference.child("Story").child(story.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(StoryActivity.this,"Story dihapus", Toast.LENGTH_LONG).show();
                }
            });

            reference.child("Comment").child(story.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Toast.makeText(HistoryActivity.this,"success delete comment", Toast.LENGTH_LONG).show();
                }
            });

            reference.child("Likes").child(story.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Toast.makeText(HistoryActivity.this,"success delete comment", Toast.LENGTH_LONG).show();
                }
            });
            GetStory();
        }
    }

    //Methode yang berisi kumpulan baris kode untuk mengatur RecyclerView
    private void MyRecyclerView() {
        //Menggunakan Layout Manager, Dan Membuat List Secara Vertical
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }
}