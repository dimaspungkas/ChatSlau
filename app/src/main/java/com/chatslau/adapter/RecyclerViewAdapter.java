package com.chatslau.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.activity.DetailPostActivity;
import com.chatslau.activity.RoomNameActivity;
import com.chatslau.activity.StoryActivity;
import com.chatslau.model.Story;
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

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    //Deklarasi Variable
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private ArrayList<Story> listStory;
    private Context context;
    FirebaseDataListener listener;

    //Membuat Konstruktor, untuk menerima input dari Database
    public RecyclerViewAdapter(ArrayList<Story> listStory, Context context) {
        this.listStory = listStory;
        this.context = context;
        listener = (StoryActivity)context;
    }

    //ViewHolder Digunakan Untuk Menyimpan Referensi Dari View-View
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView Story, numLike, BtnViewOption, kota, mTime, numComment;
        private LinearLayout ListItem;
        private ImageView like, comment;

        ViewHolder(View itemView) {
            super(itemView);
            //Menginisialisasi View-View yang terpasang pada layout RecyclerView kita
            Story = itemView.findViewById(R.id.story);
            numLike = itemView.findViewById(R.id.numLike);
            kota = itemView.findViewById(R.id.kota);
            mTime = itemView.findViewById(R.id.tv_mTime);
            numComment = itemView.findViewById(R.id.numComment);
            BtnViewOption = itemView.findViewById(R.id.textViewOptions);
            ListItem = itemView.findViewById(R.id.list_item);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Membuat View untuk Menyiapkan dan Memasang Layout yang Akan digunakan pada RecyclerView
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_story, parent, false);
        return new ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Mengambil Nilai/Value yenag terdapat pada RecyclerView berdasarkan Posisi Tertentu
        final String story = listStory.get(position).getStory();
        final String kota = listStory.get(position).getKota();
        final String key = listStory.get(position).getKey();
        final String uid = listStory.get(position).getUid();
        final String nama = listStory.get(position).getNama();
        final long time = listStory.get(position).getTimestamp();
        final String ftime = listStory.get(position).getFormattedTime();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String s = story;
        List<String> words = Arrays.asList("kontol", "kntl", "memek", "mmk", "ngentot", "ngewe", "ngntt",
                "jembut", "jmbt", "itil", "toket", "tete", "tt", "babi", "anjing", "monyet", "kunyuk",
                "bajingan", "asu", "bangsat", "bgst", "keparat", "bejad", "bejat", "brengsek", "tai",
                "perek", "pecun", "banci", "jablay", "tolol", "bego", "goblok", "idiot", "bo", "bu",
                "vcs", "cs", "peler", "pler", "pantek", "fwb", "ons", "having sex", "having seks",
                "have sex", "have seks", "hs", "tocil", "toge", "tobrut", "dirty", "ngaceng", "grepe",
                "masturb", "masturbasi", "coli", "bokep", "porno");
        for (String word : words) {
            Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
            s = rx.matcher(s).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
        }
        //Memasukan Nilai/Value kedalam View
        holder.Story.setText(s);
        holder.kota.setText(kota);
        holder.mTime.setText(getTimeAgo(listStory.get(position).getTimestamp()));

        isLiked(key, holder.like);
        nrLikes(holder.numLike, key);
        nrComment(holder.numComment, key);

        Story story1 = new Story(uid, nama, story, kota, time, ftime, key);

        FirebaseDatabase.getInstance().getReference().child("Story")
                .child(key).setValue(story1);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("uid", firebaseUser.getUid());
                hashMap.put("key", key);
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(key).child(firebaseUser.getUid()).setValue(hashMap);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(key).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.ListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(DetailPostActivity.getActIntent((Activity) context)
                        .putExtra("story", listStory.get(position)));
            }
        });

        holder.BtnViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.BtnViewOption);
                //inflating menu from xml resource
                if(uid.equals(firebaseUser.getUid())){
                    popup.inflate(R.menu.menu_delete);
                }else{
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
                                //handle menu1 click
                                imBlockedORNot(uid, firebaseUser.getUid(), position);
                                break;
                            case R.id.btn_report:
                                    final String[] report = {"Spam", "Story tidak pantas", "Story mengandung pornografi"};
                                    AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.CustomDialog);
                                    alert.setTitle("Lapor bang!")
                                            .setIcon(R.drawable.ic_baseline_report_24)
                                            .setItems(report, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case 0:
                                                            reportStory(uid, story, report[0]);
                                                            break;
                                                        case 1:
                                                            reportStory(uid, story, report[1]);
                                                            break;
                                                        case 2:
                                                            reportStory(uid, story, report[2]);
                                                            break;
                                                    }
                                                }
                                            })
                                            .setCancelable(true);
                                    alert.show();
                                break;
                            case R.id.btn_delete:
                                listener.onDeleteData(listStory.get(position), position);
                                return true;
                            case R.id.btn_blokir:
                                BlockUser(uid, firebaseUser.getUid());
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        //Menghitung Ukuran/Jumlah Data Yang Akan Ditampilkan Pada RecyclerView
        return listStory.size();
    }

    public interface FirebaseDataListener{
        void onDeleteData(Story story, int position);
    }

    private void reportStory(String getUid, String story, String report){
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
                                .putExtra("story", listStory.get(i)));
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

    private void isLiked(String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.likeon);
                    imageView.setTag("liked");
                }
                else{
                    imageView.setImageResource(R.drawable.likeof);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrComment(final TextView comment, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comment")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comment.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount()+"");
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