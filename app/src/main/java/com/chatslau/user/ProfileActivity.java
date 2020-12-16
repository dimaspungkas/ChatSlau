package com.chatslau.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatslau.R;
import com.chatslau.activity.PilihNama;
import com.chatslau.activity.RoomChatActivity;
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
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView image_profile;
    private TextView tvProfile, tvNama, tvGender, tvLokasi, tvOK, setLokasi;

    private DatabaseReference getReference;
    private FirebaseUser fuser;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<pilih_nama, RecyclerViewAdapterImage> recyclerAdapter;
    private FirebaseRecyclerOptions<pilih_nama> options;

    private static final int REQUEST_LOCATION = 1;
    Geocoder geocoder;
    List<Address> addresses;
    LocationManager locationManager;
    double lat, longi;
    private String nama, gender, city, lokasi, mUri;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        image_profile = findViewById(R.id.profile_image);
        tvProfile = findViewById(R.id.ubah_foto);
        tvNama = findViewById(R.id.tv_nama);
        tvGender = findViewById(R.id.tv_gender);
        tvLokasi = findViewById(R.id.tv_lokasi);
        tvOK = findViewById(R.id.tv_ok);
        setLokasi = findViewById(R.id.setLokasi);
        btnUpdate = findViewById(R.id.btn_update);
        recyclerView = findViewById(R.id.rc_image);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        getReference = FirebaseDatabase.getInstance().getReference();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //Digunakan untuk mengatur dan memposisikan item didalam RecyclerView
        recyclerView.setLayoutManager(layoutManager);
        //Digunakan agar ukuran RecyclerView tidak berubah saat menambahkan item atau menghapusnya
        recyclerView.setHasFixedSize(true);

        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                tvProfile.setVisibility(View.GONE);
                tvOK.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.INVISIBLE);
            }
        });

        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOK.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
                tvProfile.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.VISIBLE);
            }
        });

        setLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLokasi();
                btnUpdate.setVisibility(View.VISIBLE);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        firebaseReyclerViewAdapter();
    }

    private void loadProfile(){
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Memuat Profil");
        pd.show();
        getReference.child("DataUser").child(fuser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                        city = pilihNama.getKota();
                        mUri = pilihNama.getImage_url();
                        tvNama.setText(pilihNama.getNama());
                        tvGender.setText(pilihNama.getGender());
                        tvLokasi.setText(pilihNama.getLokasi());

                        if (mUri.equals("default") || mUri.isEmpty()) {
                            mUri = "default";
                            image_profile.setImageResource(R.drawable.ic_profile);
                        } else {
                            Glide.with(ProfileActivity.this)
                                    .load(mUri)
                                    .into(image_profile);
                        }
                        pd.dismiss();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void simpanData(){
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Memperbarui Data");
        pd.show();
        nama = tvNama.getText().toString();
        gender = tvGender.getText().toString();
        lokasi = tvLokasi.getText().toString();
        if(lokasi.equals("Lokasi Tidak Dapat Ditemukan") || lokasi.equals("")){
            lokasi = "Unknown Location";
            city = "Unknown Location";
        }
        getReference.child("DataUser").child(fuser.getUid())
                .setValue(new pilih_nama(nama, gender, city, lokasi, mUri))
                .addOnSuccessListener(this, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        //Peristiwa ini terjadi saat user berhasil menyimpan datanya kedalam Database
                        pd.dismiss();
                        Toast.makeText(ProfileActivity.this, "Data Diperbarui", Toast.LENGTH_SHORT).show();
                    }
                });
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
                holder.setDisplayImage(model.getImage_url(), ProfileActivity.this);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mUri = model.getImage_url();
                        Glide.with(ProfileActivity.this)
                                .load(mUri)
                                .into(image_profile);
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
        GPSTracker  gpstracker=new GPSTracker(ProfileActivity.this);
        if (ActivityCompat.checkSelfPermission(
                ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                ProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                lat = gpstracker.getLatitude();
                longi = gpstracker.getLongitude();
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                tvLokasi.setText(R.string.unable_lokasi);
            }
        }
    }

    private void setLokasi(){
        GPSTracker  gpstracker=new GPSTracker(ProfileActivity.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            gpstracker.getLocation();
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
}