package com.chatslau.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatslau.R;
import com.chatslau.activity.DetailPostActivity;
import com.chatslau.activity.RoomNameActivity;
import com.chatslau.model.Comment;
import com.chatslau.model.pilih_nama;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapterComment extends RecyclerView.Adapter<RecyclerViewAdapterComment.ViewHolder>{

    //Deklarasi Variable
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private ArrayList<Comment> listComment;
    private Context context;
    FirebaseDataListener listener;

    //Membuat Konstruktor, untuk menerima input dari Database
    public RecyclerViewAdapterComment(ArrayList<Comment> listComment, Context context) {
        this.listComment = listComment;
        this.context = context;
        listener = (DetailPostActivity)context;
    }

    //ViewHolder Digunakan Untuk Menyimpan Referensi Dari View-View
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView comment, namaC, mTime, BtnViewOption;
        private CircleImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView);
            //Menginisialisasi View-View yang terpasang pada layout RecyclerView kita
            comment = itemView.findViewById(R.id.tv_comment);
            namaC = itemView.findViewById(R.id.tv_nama);
            mTime = itemView.findViewById(R.id.tv_mTime);
            profile_image = itemView.findViewById(R.id.profile_image);
            BtnViewOption = itemView.findViewById(R.id.textViewOptions);
        }
    }

    @Override
    public RecyclerViewAdapterComment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Membuat View untuk Menyiapkan dan Memasang Layout yang Akan digunakan pada RecyclerView
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_comment, parent, false);
        return new RecyclerViewAdapterComment.ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerViewAdapterComment.ViewHolder holder, final int position) {
        //Mengambil Nilai/Value yenag terdapat pada RecyclerView berdasarkan Posisi Tertentu
        final String comment = listComment.get(position).getComment();
        final String namaC = listComment.get(position).getnama_comment();
        final String uid = listComment.get(position).getUid();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String c = comment;
        List<String> words = Arrays.asList("kontol", "kntl", "memek", "mmk", "ngentot", "ngewe", "ngntt",
                "jembut", "jmbt", "itil", "toket", "tete", "tt", "babi", "anjing", "monyet", "kunyuk",
                "bajingan", "asu", "bangsat", "bgst", "keparat", "bejad", "bejat", "brengsek", "tai",
                "perek", "pecun", "banci", "jablay", "tolol", "bego", "goblok", "idiot", "bo", "bu",
                "vcs", "cs", "peler", "pler", "pantek", "fwb", "ons", "having sex", "having seks",
                "have sex", "have seks", "hs", "tocil", "toge", "tobrut", "dirty", "ngaceng", "grepe",
                "masturb", "masturbasi", "coli", "bokep", "porno");
        for (String word : words) {
            Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
            c = rx.matcher(c).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
        }

        holder.comment.setText(c);
        holder.namaC.setText(namaC);
        holder.mTime.setText(getTimeAgo(listComment.get(position).getTimestamp()));
        setImage(uid, holder.profile_image);

        holder.BtnViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.BtnViewOption);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_delete);
                if(!uid.equals(firebaseUser.getUid())) {
                    popup.inflate(R.menu.menu_story);
                    popup.inflate(R.menu.menu_report);
                    popup.inflate(R.menu.menu_blokir);
                }
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.btn_chat:
                                imBlockedORNot(listComment.get(position).getUid(), firebaseUser.getUid(), position);
                                break;
                            case R.id.btn_delete:
                                //handle menu1 click
                                listener.onDeleteData(listComment.get(position), position);
                                break;
                            case R.id.btn_blokir:
                                BlockUser(uid, firebaseUser.getUid());
                                break;
                            case R.id.btn_report:
                                final String[] report = {"Spam", "Comment tidak pantas", "Comment mengandung pornografi"};
                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setTitle("Lapor bang!")
                                        .setIcon(R.drawable.ic_baseline_report_24)
                                        .setItems(report, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case 0:
                                                        reportComment(uid, comment, report[0]);
                                                        break;
                                                    case 1:
                                                        reportComment(uid, comment, report[1]);
                                                        break;
                                                    case 2:
                                                        reportComment(uid, comment, report[2]);
                                                        break;
                                                }
                                            }
                                        })
                                        .setCancelable(true);
                                alert.show();
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });
    }

    public interface FirebaseDataListener{
        void onDeleteData(Comment comment, int position);
    }

    @Override
    public int getItemCount() {
        //Menghitung Ukuran/Jumlah Data Yang Akan Ditampilkan Pada RecyclerView
        return listComment.size();
    }

    private void reportComment(String getUid, String story, String report){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", getUid);
        hashMap.put("story", story);
        hashMap.put("report", report);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Report");
        ref.push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Berhasil Dilaporkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Gagal Dilaporkan: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setImage(String myUid, CircleImageView profile_image){
        DatabaseReference getReference = FirebaseDatabase.getInstance().getReference();
        getReference.child("DataUser").child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pilih_nama pilihNama = dataSnapshot.getValue(pilih_nama.class);
                if (pilihNama.getImage_url().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_profile);
                } else {
                    Glide.with(context)
                            .load(pilihNama.getImage_url())
                            .into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void imBlockedORNot(String hisUid, String myUid, final int i){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, "Yah, sayang sekali kamu diblokir", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        checkIsBlocked(hisUid, myUid, i);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUid, String myUid, final int i){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, "Dia kamu blokir :(", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        context.startActivity(RoomNameActivity.getActIntent((Activity) context)
                                .putExtra("comment", listComment.get(i)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void BlockUser(String hisUid, String myUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot ds: snapshot.getChildren()){
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
                        }else{
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("uid", hisUid);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
                            ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Berhasil Diblokir", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Gagal Diblokir: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "baru saja";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 menit yang lalu";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " menit yang lalu";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1 jam yang lalu";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " jam yang lalu";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "kemarin";
        } else {
            return diff / DAY_MILLIS + " hari yang lalu";
        }
    }
}