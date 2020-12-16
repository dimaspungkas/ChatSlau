package com.chatslau.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatslau.R;
import com.chatslau.adapter.FirebaseRecyclerAdapter;
import com.chatslau.model.Chat;
import com.chatslau.model.pilih_nama;
import com.chatslau.notification.APIService;
import com.chatslau.notification.Client;
import com.chatslau.notification.Data;
import com.chatslau.notification.MyResponse;
import com.chatslau.notification.Sender;
import com.chatslau.notification.Token;
import com.chatslau.user.ProfileActivity;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomChatActivity extends AppCompatActivity {

    public static String TAG = "FirebaseUI.chat";
    private Firebase mRef;
    private Query mChatRef;
    private String mTime;
    private Button mSendButton;
    private EditText mMessageEdit;

    private RecyclerView mMessages;
    private FirebaseAuth auth;
    private FirebaseDatabase getDatabase;
    private DatabaseReference getReference;
    private FirebaseRecyclerAdapter<Chat, ChatHolder> mRecycleViewAdapter;
    private String RecId, nama, sender, receiver, senderId, receiverId, GetUserID, key_room, rec;
    APIService apiService;
    Boolean notify;
    private boolean mFromNotification;
    private CircleImageView profile_image;
    private TextView tvNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);
        Firebase.setAndroidContext(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(RoomChatActivity.this, RoomNameActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        tvNama = findViewById(R.id.tv_nama);
        profile_image = findViewById(R.id.profile_image);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);
        getDatabase = FirebaseDatabase.getInstance();
        getReference = getDatabase.getReference();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        GetUserID = user.getUid();
        getNama(GetUserID);

        final Bundle args = getIntent().getExtras();
        if (args != null) {
            mFromNotification = args.getBoolean("fromNotification");
        }
        if(mFromNotification){
            key_room = getIntent().getStringExtra("key");
            sender = getIntent().getStringExtra("sender");
            receiver = getIntent().getStringExtra("receiver");
            senderId = getIntent().getStringExtra("userId");
            receiverId = getIntent().getStringExtra("receiverId");
        }else {
            key_room = getIntent().getStringExtra("key_room");
            sender = getIntent().getStringExtra("sender");
            receiver = getIntent().getStringExtra("receiver");
            senderId = getIntent().getStringExtra("senderId");
            receiverId = getIntent().getStringExtra("receiverId");
        }

        if(GetUserID.equals(senderId)) {
            RecId = receiverId;
            rec = receiver;
        } else {
            RecId = senderId;
            rec = sender;
        }

        getReference.child("DataUser").child(RecId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                        String uri = pilihNama.getImage_url();
                        tvNama.setText(pilihNama.getNama());
                        if (uri.equals("default") || uri.isEmpty()) {
                            profile_image.setImageResource(R.drawable.ic_profile);
                        } else {
                            Glide.with(getApplicationContext())
                                    .load(pilihNama.getImage_url())
                                    .into(profile_image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomChatActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });

        mRef = new Firebase("https://chat-3126e.firebaseio.com/Chat/"+key_room);
        mChatRef = mRef.limitToLast(50);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = mMessageEdit.getText().toString();
                imBlockedORNot(RecId, GetUserID, message);
            }
        });

        mMessages = (RecyclerView) findViewById(R.id.messagesList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false);
        mMessages.setHasFixedSize(true);
        manager.setStackFromEnd(true);
        mMessages.setLayoutManager(manager);

        mRecycleViewAdapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>(Chat.class, R.layout.text_message, ChatHolder.class, mChatRef) {
            @Override
            public void populateViewHolder(ChatHolder chatView, Chat chat, int position) {
                final ProgressDialog pd = new ProgressDialog(RoomChatActivity.this);
                pd.setMessage("Memuat Pesan");
                pd.show();
                chatView.setText(chat.getMessage(), chat.getFormattedTime());

                if (GetUserID.equals(chat.getSenderId())) {
                    chatView.setIsSender(true);
                } else {
                    chatView.setIsSender(false);
                }
                pd.dismiss();
            }
        };

        mMessages.setAdapter(mRecycleViewAdapter);
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setIsSender(Boolean isSender) {
            FrameLayout left_arrow = (FrameLayout) mView.findViewById(R.id.left_arrow);
            FrameLayout right_arrow = (FrameLayout) mView.findViewById(R.id.right_arrow);
            RelativeLayout messageContainer = (RelativeLayout) mView.findViewById(R.id.message_container);
            LinearLayout message = (LinearLayout) mView.findViewById(R.id.message);

            if (isSender) {
                left_arrow.setVisibility(View.GONE);
                right_arrow.setVisibility(View.VISIBLE);
                messageContainer.setGravity(Gravity.RIGHT);
            } else {
                left_arrow.setVisibility(View.VISIBLE);
                right_arrow.setVisibility(View.GONE);
                messageContainer.setGravity(Gravity.LEFT);
            }
        }

        public void setText(String message, String time) {
            TextView messages = (TextView) mView.findViewById(R.id.message_text);
            TextView times = (TextView) mView.findViewById(R.id.time_text);
            messages.setText(message);
            times.setText(time);
        }
    }

    private void getNama(String id){
        getReference.child("DataUser").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pilih_nama pilihNama = snapshot.getValue(pilih_nama.class);
                        nama = pilihNama.getNama();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomChatActivity.this, "User Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification(String id_rec, final String username, final String message, String rec){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        com.google.firebase.database.Query query = tokens.orderByKey().equalTo(id_rec);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(GetUserID, key_room, username+": "+message,
                            "Ada yang ngechat nih!", id_rec, username, rec);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Toast.makeText(RoomChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RoomChatActivity.this, "Maaf, Chat Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void imBlockedORNot(String hisUid, String myUid, String message){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(RoomChatActivity.this, "Yah, sayang sekali kamu diblokir", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        checkIsBlocked(hisUid, myUid, message);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomChatActivity.this, "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIsBlocked(String hisUid, String myUid, String message){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(RoomChatActivity.this, "Dia kamu blokir :(", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        if (message.length() < 1) {
                            Toast.makeText(RoomChatActivity.this, "Chatnya ga boleh kosong yaa..", Toast.LENGTH_SHORT).show();
                        }else if(message.trim().matches("")) {
                            Toast.makeText(RoomChatActivity.this, "Jangan Kosongan Dong, Emangnya Baso?", Toast.LENGTH_SHORT).show();
                        }else{
                            Chat chat = new Chat(mMessageEdit.getText().toString(), nama, GetUserID, System.currentTimeMillis(), mTime);
                            mRef.push().setValue(chat, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null) {
                                        Log.e(TAG, firebaseError.toString());
                                    }
                                }
                            });
                            mMessageEdit.setText("");
                            final String msg = message;
                            getReference = FirebaseDatabase.getInstance().getReference("DataUser").child(GetUserID);
                            getReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(notify) {
                                        sendNotification(RecId, nama, msg, rec);
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomChatActivity.this, "Maaf, Chat Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}