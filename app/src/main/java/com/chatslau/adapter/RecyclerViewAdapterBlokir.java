package com.chatslau.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.activity.PilihNama;
import com.chatslau.model.RoomName;
import com.chatslau.model.pilih_nama;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapterBlokir extends RecyclerView.Adapter<RecyclerViewAdapterBlokir.ViewHolder>{

    //Deklarasi Variable
    private ArrayList<RoomName> listRoom;
    private Context context;

    //Membuat Konstruktor, untuk menerima input dari Database
    public RecyclerViewAdapterBlokir(ArrayList<RoomName> listRoom, Context context) {
        this.listRoom = listRoom;
        this.context = context;
    }

    //ViewHolder Digunakan Untuk Menyimpan Referensi Dari View-View
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUser;
        private LinearLayout ListItem;

        ViewHolder(View itemView) {
            super(itemView);
            //Menginisialisasi View-View yang terpasang pada layout RecyclerView kita
            tvUser = itemView.findViewById(R.id.tv_user);
            ListItem = itemView.findViewById(R.id.list_item);
        }
    }

    @Override
    public RecyclerViewAdapterBlokir.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Membuat View untuk Menyiapkan dan Memasang Layout yang Akan digunakan pada RecyclerView
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_blokir, parent, false);
        return new RecyclerViewAdapterBlokir.ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerViewAdapterBlokir.ViewHolder holder, final int position) {
        //Mengambil Nilai/Value yenag terdapat pada RecyclerView berdasarkan Posisi Tertentu
        String key = listRoom.get(position).getKey();
        String getUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("DataUser").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                holder.tvUser.setText(pilihNama.getNama());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.ListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] action = {"Buka Blokir"};
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setItems(action,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                unBlockUser(getUid, key);
                                break;
                        }
                    }
                });
                alert.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        //Menghitung Ukuran/Jumlah Data Yang Akan Ditampilkan Pada RecyclerView
        return listRoom.size();
    }

    private void unBlockUser(String myuid, String hisuid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myuid).child("BlockedUsers").orderByChild("uid").equalTo(hisuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Berhasil buka blok", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Gagal buka blokir: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
