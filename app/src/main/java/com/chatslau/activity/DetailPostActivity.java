package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.adapter.RecyclerViewAdapterComment;
import com.chatslau.model.Comment;
import com.chatslau.model.Story;
import com.chatslau.model.pilih_nama;
import com.chatslau.notification.APIService;
import com.chatslau.notification.Client;
import com.chatslau.notification.Data;
import com.chatslau.notification.MyResponse;
import com.chatslau.notification.Sender;
import com.chatslau.notification.Token;
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

public class DetailPostActivity extends AppCompatActivity implements RecyclerViewAdapterComment.FirebaseDataListener {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private TextView tv_story, tv_like, tv_kota, tv_mTime, numComment;
    private ImageView like;
    private EditText edtComment;
    private ImageButton btnSend;
    private FirebaseAuth auth;
    private String GetUserID, nama, comment, sstory, storyUid, key, kota, cTime;
    private long mTime;
    private FirebaseDatabase getDatabase;
    private DatabaseReference getReference;
    private ArrayList<Comment> dataComment;

    private RecyclerView rvComment;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    APIService apiService;
    Boolean notify;
    private boolean mFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Ceritanya....");

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        tv_story = findViewById(R.id.story);
        tv_like = findViewById(R.id.numLike);
        like = findViewById(R.id.like);
        tv_kota = findViewById(R.id.kota);
        tv_mTime = findViewById(R.id.tv_mTime);
        numComment = findViewById(R.id.numComment);
        rvComment = findViewById(R.id.rv_comment);
        edtComment = findViewById(R.id.edt_comment);
        btnSend = findViewById(R.id.send_comment);

        getDatabase = FirebaseDatabase.getInstance();
        getReference = getDatabase.getReference();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        GetUserID = user.getUid();

        MyRecyclerView();
        getNama();

        final Bundle args = getIntent().getExtras();
        if (args != null) {
            mFromNotification = args.getBoolean("fromNotification");
        }
        if(mFromNotification){
            storyUid = getIntent().getStringExtra("storyUid");
            key = getIntent().getStringExtra("key");
            sstory = getIntent().getStringExtra("sstory");
            kota = getIntent().getStringExtra("kota");
            mTime = Long.parseLong(getIntent().getStringExtra("mTime"));
        }

        final Story story = (Story) getIntent().getSerializableExtra("story");
        if(story != null){
            key = story.getKey();
            sstory = story.getStory();
            kota = story.getKota();
            mTime = story.getTimestamp();
            storyUid = story.getUid();
        }

        String s = sstory;
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
        tv_story.setText(s);
        tv_kota.setText(kota);
        tv_mTime.setText(getTimeAgo(mTime));

        getComment(key);
        isLiked(key, like);
        nrLikes(tv_like, key);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(GetUserID, String.valueOf(true));
                hashMap.put("key", key);
                if(like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(key).child(GetUserID).setValue(hashMap);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(key).child(GetUserID).removeValue();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment(story);
            }
        });

        edtComment.addTextChangedListener(new TextWatcher() {
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
                if (edtComment.getLineCount() > 5) {
                    edtComment.setText(comment);
                    Toast.makeText(DetailPostActivity.this, "Maksimal 5 baris guys...", Toast.LENGTH_SHORT).show();
                } else {
                    comment = edtComment.getText().toString();
                }
            }
        });
    }

    private void sendComment(Story story){
        notify = true;
        String key = story.getKey();
        if(comment.length() > 0){
            getReference.child("Comment").child(key).push()
                    .setValue(new Comment(nama, comment, GetUserID, System.currentTimeMillis(), cTime))
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DetailPostActivity.this, "Comment Berhasil", Toast.LENGTH_SHORT).show();
                        }
                    });
            edtComment.setText("");
            getReference.child("DataUser").child(GetUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(notify && !GetUserID.equals(storyUid)) {
                        sendNotification(storyUid);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailPostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Comment Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
        }
    }

    private void getComment(String key){
        getReference.child("Comment").child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Inisialisasi ArrayList
                        dataComment = new ArrayList<>();
                        numComment.setText(dataSnapshot.getChildrenCount()+"");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            //Mapping data pada DataSnapshot ke dalam objek mahasiswa
                            Comment comment = snapshot.getValue(Comment.class);

                            //Mengambil Primary Key, digunakan untuk proses Update dan Delete
                            comment.setKey(snapshot.getKey());
                            dataComment.add(comment);
                        }

                        //Inisialisasi Adapter dan data Mahasiswa dalam bentuk Array
                        adapter = new RecyclerViewAdapterComment(dataComment, DetailPostActivity.this);

                        //Memasang Adapter pada RecyclerView
                        rvComment.setAdapter(adapter);
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

    private void getNama(){
        getReference.child("DataUser").child(GetUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                nama = pilihNama.getNama();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailPostActivity.this, "User Tidak Ditemukan",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteData(Comment comment, final int position) {
        if(getReference!=null && GetUserID.equals(storyUid)) {
            getReference.child("Comment").child(key).child(comment.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(DetailPostActivity.this, "Komen dihapus", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(this, "Postingan orang jangan diotak-atik!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification(String id_rec){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        com.google.firebase.database.Query query = tokens.orderByKey().equalTo(id_rec);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(sstory, key, "Bales komennya dong!",
                            "Ada yang komen nih!", storyUid, kota, String.valueOf(mTime));
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Toast.makeText(DetailPostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

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

    public static Intent getActIntent(Activity activity) {
        // kode untuk pengambilan Intent
        return new Intent(activity, DetailPostActivity.class);
    }

    //Methode yang berisi kumpulan baris kode untuk mengatur RecyclerView
    private void MyRecyclerView() {
        //Menggunakan Layout Manager, Dan Membuat List Secara Vertical
        layoutManager = new LinearLayoutManager(this);
        rvComment.setLayoutManager(layoutManager);
        rvComment.setHasFixedSize(true);
    }
}