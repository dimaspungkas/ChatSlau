package com.chatslau.setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.chatslau.R;
import com.chatslau.activity.RoomNameActivity;
import com.chatslau.adapter.RecyclerViewAdapterBlokir;
import com.chatslau.adapter.RecyclerViewAdapterRoom;
import com.chatslau.model.RoomName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Blokir extends AppCompatActivity {

    private RecyclerView rvBlokir;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<RoomName> dataBlokir;
    private String GetUserId;
    private TextView noBlokir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blokir);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Pengguna Terblokir");

        rvBlokir = findViewById(R.id.rv_blokir);
        noBlokir = findViewById(R.id.noBlokir);

        GetUserId = FirebaseAuth.getInstance().getUid();

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvBlokir.setLayoutManager(layoutManager);
        rvBlokir.setHasFixedSize(true);
        getBlocked();

    }

    private void getBlocked(){
        final ProgressDialog pd = new ProgressDialog(Blokir.this);
        pd.setMessage("Memuat Daftar User");
        pd.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(GetUserId).child("BlockedUsers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataBlokir = new ArrayList<>();
                        if(snapshot.exists()){
                        for(DataSnapshot ds: snapshot.getChildren()) {
                                RoomName roomName = ds.getValue(RoomName.class);
                                roomName.setKey(ds.getKey());
                                dataBlokir.add(roomName);
                            }
                        }else{
                            noBlokir.setVisibility(View.VISIBLE);
                        }
                        adapter = new RecyclerViewAdapterBlokir(dataBlokir, Blokir.this);
                        //Memasang Adapter pada RecyclerView
                        rvBlokir.setAdapter(adapter);
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}