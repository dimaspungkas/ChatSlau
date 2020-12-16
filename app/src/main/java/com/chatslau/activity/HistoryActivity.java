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
import com.chatslau.adapter.RecyclerViewAdapterHistory;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewAdapterHistory.FirebaseDataListener {

    //Deklarasi Variable untuk RecyclerView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    //Deklarasi Variable Database Reference dan ArrayList dengan Parameter Class Model kita.
    private DatabaseReference reference;
    private ArrayList<Story> dataStory;
    private FirebaseAuth auth;
    private TextView noHistory;
    private SwipeRefreshLayout srlStory;
    private FloatingActionButton btnStory;
    //private Spinner spHistory;
    //private String[] daftarHistory = {"Pilih History", "Story kamu", "Story yang kamu suka", "Story yang kamu komentarin"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Masa Lalumu");

        //spHistory = findViewById(R.id.sp_history);
        btnStory = findViewById(R.id.btn_story);
        srlStory = findViewById(R.id.srlStory);
        noHistory = findViewById(R.id.text_no_history);
        recyclerView = findViewById(R.id.rv_history);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        MyRecyclerView();

        /*final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout
        .simple_spinner_dropdown_item, daftarHistory);
        spHistory.setAdapter(arrayAdapter);
        */
        //Mengeset listener untuk mengetahui event/aksi saat item dipilih
/*        spHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int i, long l) {
                String daftarHistory = arrayAdapter.getItem(i);
                if(daftarHistory.equals("Story kamu")){
                    GetStory();
                }else if(daftarHistory.equals("Story yang kamu suka")){
                    getUidLike();
                }else if(daftarHistory.equals("Story yang kamu komentarin")){

                }else{
                    Toast.makeText(HistoryActivity.this, "Pilih history kamu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });
*/

        btnStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, PostActivity.class));
            }
        });

        srlStory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srlStory.setRefreshing(true);
                noHistory.setVisibility(View.VISIBLE);
                GetStory();
                //spHistory.setSelection(1);
                srlStory.setRefreshing(false);
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
        //spHistory.setSelection(1);
        GetStory();
    }

    //Berisi baris kode untuk mengambil data dari Database dan menampilkannya kedalam Adapter
    private void GetStory(){
        noHistory.setVisibility(View.VISIBLE);
        final ProgressDialog pd = new ProgressDialog(HistoryActivity.this);
        pd.setMessage("Memuat Masa Lalumu");
        pd.show();
        //Mendapatkan Referensi Database
        reference.child("Story").orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Inisialisasi ArrayList
                        dataStory = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            //Mapping data pada DataSnapshot ke dalam objek mahasiswa
                            Story story = snapshot.getValue(Story.class);

                            //Mengambil Primary Key, digunakan untuk proses Update dan Delete
                            if (story != null) {
                                noHistory.setVisibility(View.GONE);
                                story.setKey(snapshot.getKey());
                            }
                            dataStory.add(story);
                        }

                        adapter = new RecyclerViewAdapterHistory(dataStory, HistoryActivity.this);
                        //Memasang Adapter pada RecyclerView
                        recyclerView.setAdapter(adapter);
                        pd.dismiss();
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
/*
    private void getUidLike(){
        reference.child("Likes").orderByChild(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataStory = new ArrayList<>();
                        if(dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Story story = snapshot.getValue(Story.class);
                                noHistory.setVisibility(View.GONE);
                                story.setKey(snapshot.getKey());
                                dataStory.add(story);
                            }

                        }else{
                            dataStory.clear();
                            noHistory.setVisibility(View.VISIBLE);
                            noHistory.setText("Tidak ada story yang kamu suka");
                        }
                            adapter = new RecyclerViewAdapterHistory(dataStory, HistoryActivity.this);
                            //Memasang Adapter pada RecyclerView
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
*/
    //Methode yang berisi kumpulan baris kode untuk mengatur RecyclerView
    private void MyRecyclerView() {
        //Menggunakan Layout Manager, Dan Membuat List Secara Vertical
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onDeleteData(Story story, final int position) {
        if(reference!=null){
            reference.child("Story").child(story.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(HistoryActivity.this,"Story dihapus", Toast.LENGTH_LONG).show();
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
}