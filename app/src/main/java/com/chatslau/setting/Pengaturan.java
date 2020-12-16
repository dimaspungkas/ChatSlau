package com.chatslau.setting;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chatslau.R;
import com.chatslau.activity.HistoryActivity;
import com.chatslau.activity.MainActivity;
import com.chatslau.activity.RoomNameActivity;
import com.chatslau.user.ProfileActivity;

public class Pengaturan extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_pertanyaan, tv_terms, tv_policy, tv_bantuan, tv_blocked, tv_saran, tv_contact;
    private AlertDialog.Builder dialog;
    private LayoutInflater inflater;
    private View dialogView;
    private String judul, isi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Pengaturan");
        
        tv_pertanyaan = findViewById(R.id.tv_pertanyaan);
        tv_terms = findViewById(R.id.tv_terms);
        tv_policy = findViewById(R.id.tv_policy);
        tv_bantuan = findViewById(R.id.tv_bantuan);
        tv_blocked = findViewById(R.id.tv_blocked);
        tv_saran = findViewById(R.id.tv_saran);
        tv_contact = findViewById(R.id.tv_contact);
        
        tv_pertanyaan.setOnClickListener(this);
        tv_terms.setOnClickListener(this);
        tv_policy.setOnClickListener(this);
        tv_bantuan.setOnClickListener(this);
        tv_blocked.setOnClickListener(this);
        tv_saran.setOnClickListener(this);
        tv_contact.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_pertanyaan:
                faq();
                break;
            case R.id.tv_terms:
                judul = getResources().getString(R.string.terms);
                isi = getResources().getString(R.string.tnc);
                isiTeks(judul, isi);
                break;
            case R.id.tv_policy:
                judul = getResources().getString(R.string.policy);
                isi = getResources().getString(R.string.privpolicy);
                isiTeks(judul, isi);
                break;
            case R.id.tv_bantuan:
                startActivity(new Intent(Pengaturan.this, Bantuan.class));
                break;
            case R.id.tv_blocked:
                startActivity(new Intent(Pengaturan.this, Blokir.class));
                break;
            case R.id.tv_saran:
                startActivity(new Intent(Pengaturan.this, Kritik_Saran.class));
                break;
            case R.id.tv_contact:
                //startActivity(new Intent(Pengaturan.this, RoomNameActivity.class));
                break;
        }
    }

    private void isiTeks(String judul, String isi) {
        dialog = new AlertDialog.Builder(Pengaturan.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.terms_n_conditions, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle(judul);
        dialog.setMessage(isi);
        dialog.show();
    }

    private void faq(){
        dialog = new AlertDialog.Builder(Pengaturan.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.pertanyaan_aplikasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle(R.string.pertanyaan);
        dialog.show();
    }
}