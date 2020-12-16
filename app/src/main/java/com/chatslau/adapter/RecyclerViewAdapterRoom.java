package com.chatslau.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatslau.R;
import com.chatslau.activity.RoomChatActivity;
import com.chatslau.activity.RoomNameActivity;
import com.chatslau.model.Chat;
import com.chatslau.model.RoomName;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapterRoom extends RecyclerView.Adapter<RecyclerViewAdapterRoom.ViewHolder>{

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    //Deklarasi Variable
    private ArrayList<RoomName> listRoom;
    private Context context;
    FirebaseDataListener listener;
    long last_timestamp;
    String last_time;

    //Membuat Konstruktor, untuk menerima input dari Database
    public RecyclerViewAdapterRoom(ArrayList<RoomName> listRoom, Context context) {
        this.listRoom = listRoom;
        this.context = context;
        listener = (RoomNameActivity)context;
    }

    //ViewHolder Digunakan Untuk Menyimpan Referensi Dari View-View
    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView roomName, lastChat, tvTime;
        private LinearLayout ListItem;

        ViewHolder(View itemView) {
            super(itemView);
            //Menginisialisasi View-View yang terpasang pada layout RecyclerView kita
            roomName = itemView.findViewById(R.id.tv_roomName);
            lastChat = itemView.findViewById(R.id.tv_lastChat);
            tvTime = itemView.findViewById(R.id.tv_time);
            ListItem = itemView.findViewById(R.id.list_item);
        }
    }

    @Override
    public RecyclerViewAdapterRoom.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Membuat View untuk Menyiapkan dan Memasang Layout yang Akan digunakan pada RecyclerView
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_room, parent, false);
        return new RecyclerViewAdapterRoom.ViewHolder(V);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerViewAdapterRoom.ViewHolder holder, final int position) {
        //Mengambil Nilai/Value yenag terdapat pada RecyclerView berdasarkan Posisi Tertentu
        String key = listRoom.get(position).getKey();
        String sender = listRoom.get(position).getSender();
        String senderId = listRoom.get(position).getSenderId();
        String receiver = listRoom.get(position).getReceiver();
        String receiverId = listRoom.get(position).getReceiverId();
        String getUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String hisUid;

        if (getUid.equals(senderId)) {
            holder.roomName.setText(receiver);
            hisUid = receiverId;
        } else {
            holder.roomName.setText(sender);
            hisUid = senderId;
        }

        lastMessage(key, holder.lastChat, holder.tvTime, sender, receiver, senderId, receiverId);
        checkIsBlocked(hisUid, getUid, position);

        holder.ListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listRoom.get(position).isBlocked()){
                    Toast.makeText(context, "Buka blokirnya dulu dong!", Toast.LENGTH_SHORT).show();
                    return;
                }
                imBlockedORNot(hisUid, getUid, key, sender, senderId, receiver, receiverId);
            }
        });

        holder.ListItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String[] action = {"Hapus Chat", "blokir / buka blokir"};
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setItems(action,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                //Menggunakan interface untuk mengirim data mahasiswa, yang akan dihapus
                                listener.onDeleteData(listRoom.get(position), position);
                                break;
                            case 1:
                                if(listRoom.get(position).isBlocked()){
                                    unBlockUser(hisUid, getUid);
                                }else{
                                    blockUser(hisUid, getUid);
                                }
                                break;
                        }
                    }
                });
                alert.create();
                alert.show();
                return true;
            }
        });
    }

    private void lastMessage(String key, final TextView lastChat, final TextView mTime, String sender,
                             String receiver, String senderId, String receiverId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Chat").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat != null) {
                        lastChat.setText(chat.getMessage());
                        last_timestamp = chat.getTimestamp();
                        last_time = chat.getFormattedTime();
                        mTime.setText(getTimeAgo(last_timestamp, last_time));
                    }else{
                        lastChat.setText(R.string.no_chat);
                    }
                }
                FirebaseDatabase.getInstance().getReference().child("Room-Name")
                        .child(key).setValue(new RoomName(sender, receiver, senderId, receiverId, last_timestamp,
                        last_time, false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void imBlockedORNot(String hisUid, String myUid, String key, String sender, String senderId,
                                String receiver, String receiverId){
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
                        Intent I = new Intent(context, RoomChatActivity.class);
                        I.putExtra("key_room", key);
                        I.putExtra("sender", sender);
                        I.putExtra("senderId", senderId);
                        I.putExtra("receiver", receiver);
                        I.putExtra("receiverId", receiverId);
                        context.startActivity(I);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUid, String myUid, final int i){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                listRoom.get(i).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUid, String myUid){
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

    private void unBlockUser(String hisUid, String myUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Block");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
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

    public static String getTimeAgo(long time, String last_time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS) {
            return last_time;
        } else if (diff < 48 * HOUR_MILLIS) {
            return "kemarin";
        } else {
            return diff / DAY_MILLIS + " hari yang lalu";
        }
    }

    @Override
    public int getItemCount() {
        //Menghitung Ukuran/Jumlah Data Yang Akan Ditampilkan Pada RecyclerView
        return listRoom.size();
    }

    public interface FirebaseDataListener{
        void onDeleteData(RoomName roomName, int position);
    }
}
