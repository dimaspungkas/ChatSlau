package com.chatslau.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chatslau.R;
import com.chatslau.activity.DetailPostActivity;
import com.chatslau.activity.HistoryActivity;
import com.chatslau.model.Story;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapterHistory extends RecyclerView.Adapter<RecyclerViewAdapterHistory.ViewHolder>{

    //Deklarasi Variable
    //Deklarasi Variable
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private ArrayList<Story> listStory;
    private Context context;
    FirebaseDataListener listener;

    //Membuat Konstruktor, untuk menerima input dari Database
    public RecyclerViewAdapterHistory(ArrayList<Story> listStory, Context context) {
        this.listStory = listStory;
        this.context = context;
        listener = (HistoryActivity)context;
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
            like = itemView.findViewById(R.id.like);
            numLike = itemView.findViewById(R.id.numLike);
            kota = itemView.findViewById(R.id.kota);
            mTime = itemView.findViewById(R.id.tv_mTime);
            numComment = itemView.findViewById(R.id.numComment);
            BtnViewOption = itemView.findViewById(R.id.textViewOptions);
            ListItem = itemView.findViewById(R.id.list_item);
        }
    }

    @Override
    public RecyclerViewAdapterHistory.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Membuat View untuk Menyiapkan dan Memasang Layout yang Akan digunakan pada RecyclerView
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_story, parent, false);
        return new RecyclerViewAdapterHistory.ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerViewAdapterHistory.ViewHolder holder, final int position) {
        //Mengambil Nilai/Value yenag terdapat pada RecyclerView berdasarkan Posisi Tertentu
        final String story = listStory.get(position).getStory();
        final String kota = listStory.get(position).getKota();
        final String key = listStory.get(position).getKey();
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

        holder.Story.setText(s);
        holder.kota.setText(kota);
        holder.mTime.setText(getTimeAgo(listStory.get(position).getTimestamp()));

        isLiked(key, holder.like);
        nrLikes(holder.numLike, key);
        nrComment(holder.numComment, key);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(firebaseUser.getUid(), String.valueOf(true));
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
                popup.inflate(R.menu.menu_delete);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.btn_delete:
                                //handle menu1 click
                                listener.onDeleteData(listStory.get(position), position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }
        });

        holder.ListItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*
                  Kodingan untuk membuat fungsi Edit dan Delete,
                  yang akan dibahas pada Tutorial Berikutnya.
                 */
                return true;
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

    private void isLiked(String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
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
        reference.addValueEventListener(new ValueEventListener() {
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
