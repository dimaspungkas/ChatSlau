package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.model.Story;
import com.chatslau.model.pilih_nama;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostActivity extends AppCompatActivity {

    private EditText edtPost;
    private Button btnPost;
    private FirebaseAuth auth;
    private String GetUserID, nama, kota, mTime, post;
    private FirebaseDatabase getDatabase;
    private DatabaseReference getReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Posting yang bener");

        edtPost = findViewById(R.id.edt_post);
        btnPost = findViewById(R.id.btn_post);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        GetUserID = user.getUid();

        getNama();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post();
            }
        });

        edtPost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // If line account is higher than MAX_LINES, set text to lastText
                // and notify user (by Toast)
                if (edtPost.getLineCount() > 10) {
                    edtPost.setText(post);
                    Toast.makeText(PostActivity.this, "Maksimal 10 baris guys...", Toast.LENGTH_SHORT).show();
                } else {
                    post = edtPost.getText().toString();
                }
            }
        });
    }

    private void Post(){
        //Mendapatkan Instance dari Database
        getDatabase = FirebaseDatabase.getInstance();
        getReference = getDatabase.getReference(); // Mendapatkan Referensi dari Database
        if(post.length() < 10){
            //Jika panjang karakter kurang dari 10
            Toast.makeText(PostActivity.this, "Panjang Karakter Minimal 10", Toast.LENGTH_SHORT).show();
        }else if(post.trim().matches("")){
            Toast.makeText(this, "Dilarang Spam!", Toast.LENGTH_SHORT).show();
        }
        else {
        /*
        Jika lebih, maka data dapat diproses dan meyimpannya pada Database
        Menyimpan data referensi pada Database berdasarkan User ID dari masing-masing Akun
        */
            getReference.child("Story").push()
                    .setValue(new Story(GetUserID, nama, post, kota, System.currentTimeMillis(), mTime, null))
                    .addOnSuccessListener(this, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            //Peristiwa ini terjadi saat user berhasil menyimpan datanya kedalam Database
                            Toast.makeText(PostActivity.this, "Story Berhasil Dipost", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    private void getNama(){
        getDatabase = FirebaseDatabase.getInstance();
        getReference = getDatabase.getReference();
        getReference.child("DataUser").child(GetUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                        nama = pilihNama.getNama();
                        kota = pilihNama.getKota();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PostActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}