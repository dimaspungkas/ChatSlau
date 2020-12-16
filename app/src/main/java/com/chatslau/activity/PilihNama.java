package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatslau.R;
import com.chatslau.adapter.RecyclerViewAdapterImage;
import com.chatslau.model.pilih_nama;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PilihNama extends AppCompatActivity {

    private Button Blanjut;
    private DatabaseReference getReference;
    private String GetUserID;
    private TextView tvLokasi, tvNama, tvFoto, tvOK;
    private CircleImageView profilImage;
    private RadioButton rbCowo, rbCewe;
    private ImageButton btnAcak;
    private String nama, gender, city, lokasi;
    private String mUri = "default";
    private static final int REQUEST_LOCATION = 1;
    Geocoder geocoder;
    List<Address> addresses;
    LocationManager locationManager;
    double lat, longi;

    //Deklarasi Variable
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<pilih_nama, RecyclerViewAdapterImage> recyclerAdapter;
    private FirebaseRecyclerOptions<pilih_nama> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_nama);
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        profilImage = findViewById(R.id.profile_image);
        tvNama = findViewById(R.id.tv_nama);
        tvLokasi = findViewById(R.id.tv_lokasi);
        tvFoto = findViewById(R.id.ubah_foto);
        tvOK = findViewById(R.id.tv_ok);
        rbCowo = findViewById(R.id.rb_cowo);
        rbCewe = findViewById(R.id.rb_cewe);
        Blanjut = findViewById(R.id.btn_lanjut);
        btnAcak = findViewById(R.id.btn_acak);
        recyclerView = findViewById(R.id.rc_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        GetUserID = user.getUid();

        setNama();
        setLokasi();

        //Mendapatkan Referensi dari Firebase Storage
        getReference = FirebaseDatabase.getInstance().getReference();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //Digunakan untuk mengatur dan memposisikan item didalam RecyclerView
        recyclerView.setLayoutManager(layoutManager);
        //Digunakan agar ukuran RecyclerView tidak berubah saat menambahkan item atau menghapusnya
        recyclerView.setHasFixedSize(true);

        btnAcak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvNama.setText(NamaAcak());
            }
        });

        tvFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                tvFoto.setVisibility(View.GONE);
                tvOK.setVisibility(View.VISIBLE);
            }
        });

        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.INVISIBLE);
                tvOK.setVisibility(View.GONE);
                tvFoto.setVisibility(View.VISIBLE);
            }
        });

        Blanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PilihNama.this);
                alert.setMessage("Setelah Data Disimpan, Hanya Foto Profil Dan Lokasi 'Yang Tidak Dapat Ditemukan' Yang Dapat Diubah");
                alert.setPositiveButton("Lanjutkan",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                getGender();
                            }
                        });

                alert.setNegativeButton("Batal",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseReyclerViewAdapter();
    }

    //Method ini digunakan untuk mengubungkan data dari Database pada RecyclerView
    private void firebaseReyclerViewAdapter(){

        // Mengatur setelah opsi untuk FirebaseRecyclerAdapter
        options = new FirebaseRecyclerOptions.Builder<pilih_nama>()
                // Referensi Database yang akan digunakan beserta data Modelnya
                .setQuery(getReference.child("gambar"), pilih_nama.class)
                .setLifecycleOwner(this) //Untuk menangani perubahan siklus hidup pada Activity/Fragment
                .build();

        // Digunakan untuk menghubungkan View dengan data Models
        recyclerAdapter = new FirebaseRecyclerAdapter<pilih_nama, RecyclerViewAdapterImage>(options) {

            @Override
            protected void onBindViewHolder(@NonNull RecyclerViewAdapterImage holder, int position, @NonNull pilih_nama model) {
                //Mendapatkan data dari Database yang akan ditampilkan pada RecyclerView
                holder.setDisplayImage(model.getImage_url(), PilihNama.this);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mUri = model.getImage_url();
                        Glide.with(PilihNama.this)
                                .load(mUri)
                                .into(profilImage);
                    }
                });
            }

            @NonNull
            @Override
            public RecyclerViewAdapterImage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //Mengubungkan adapter dengan Layout yang akan digunakan
                return new RecyclerViewAdapterImage(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_image, parent, false));
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
    }

    private String NamaAcak(){
        String[] adjs = {"pencari", "pembenci", "penikmat", "pembuat", "pencetus", "pengurus"};
        String[] nouns = {"dosa", "cinta", "masalah", "kopi", "kerusuhan", "seblak"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        " " +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private void SimpanData(){
        nama = tvNama.getText().toString();
        lokasi = tvLokasi.getText().toString();
        if(lokasi.equals("Lokasi Tidak Dapat Ditemukan") || lokasi.equals("")){
            lokasi = "Unknown Location";
            city = "Unknown Location";
        }

        if(nama.isEmpty()){
            Toast.makeText(this, "Pilih Nama Dulu Bisa Kali", Toast.LENGTH_SHORT).show();
        }else {
            final ProgressDialog pd = new ProgressDialog(PilihNama.this);
            pd.setMessage("Menyimpan Data");
            pd.show();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference getReference;
            getReference = database.getReference();
            getReference.child("DataUser").child(GetUserID)
                    .setValue(new pilih_nama(nama, gender, city, lokasi, mUri))
                    .addOnSuccessListener(this, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            //Peristiwa ini terjadi saat user berhasil menyimpan datanya kedalam Database
                            pd.dismiss();
                            Toast.makeText(PilihNama.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PilihNama.this, MainActivity.class));
                            finish();
                        }
                    });
        }
    }

    private void setNama(){
        getReference = FirebaseDatabase.getInstance().getReference();
        getReference.child("DataUser").child(GetUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            startActivity(new Intent(PilihNama.this, MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(PilihNama.this, "Atur Dulu Profilmu Yaa", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PilihNama.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                PilihNama.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                PilihNama.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                lat = locationGPS.getLatitude();
                longi = locationGPS.getLongitude();
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                tvLokasi.setText(R.string.unable_lokasi);
            }
        }
    }

    private void setLokasi(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(lat, longi, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String address = addresses.get(0).getAddressLine(0);
                    city = addresses.get(0).getSubAdminArea();
                    tvLokasi.setText(address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void getGender(){
        if(rbCowo.isChecked()){
            gender = rbCowo.getText().toString();
            SimpanData();
        } else if(rbCewe.isChecked()) {
            gender = rbCewe.getText().toString();
            SimpanData();
        }else{
            Toast.makeText(this, "Pilih Gender Dulu Dong", Toast.LENGTH_SHORT).show();
        }
    }
}
