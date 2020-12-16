package com.chatslau.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.chatslau.R;
import com.chatslau.activity.DetailPostActivity;
import com.chatslau.activity.MainActivity;
import com.chatslau.activity.RoomChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null && sented.equals(firebaseUser.getUid())){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                sendOreoNotification(remoteMessage);
            }else {
                sendNotification(remoteMessage);
            }
        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage){
        String user = remoteMessage.getData().get("user");
        String key = remoteMessage.getData().get("key");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sender");
        String receiver = remoteMessage.getData().get("receiver");
        String sented = remoteMessage.getData().get("sented");
        Intent intent;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        if(title.equals("Ada yang komen nih!")) {
            intent = new Intent(this, DetailPostActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("sstory", user);
            bundle.putString("key", key);
            bundle.putString("storyUid", sented);
            bundle.putString("kota", sender);
            bundle.putString("mTime", receiver);
            bundle.putBoolean("fromNotification", true);
            intent.putExtras(bundle);
        }else{
            intent = new Intent(this, RoomChatActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", user);
            bundle.putString("key", key);
            bundle.putString("sender", sender);
            bundle.putString("receiver", receiver);
            bundle.putString("receiverId", sented);
            bundle.putBoolean("fromNotification", true);
            intent.putExtras(bundle);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, j,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_ONE_SHOT);
        Uri defaulSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaulSound, String.valueOf(R.drawable.ic_chat));

        int i = 0;
        if(j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String key = remoteMessage.getData().get("key");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sender");
        String receiver = remoteMessage.getData().get("receiver");
        String sented = remoteMessage.getData().get("sented");
        Intent intent;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        if(title.equals("Ada yang komen nih!")) {
            intent = new Intent(this, DetailPostActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("sstory", user);
            bundle.putString("key", key);
            bundle.putString("storyUid", sented);
            bundle.putString("kota", sender);
            bundle.putString("mTime", receiver);
            bundle.putBoolean("fromNotification", true);
            intent.putExtras(bundle);
        }else{
            intent = new Intent(this, RoomChatActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", user);
            bundle.putString("key", key);
            bundle.putString("sender", sender);
            bundle.putString("receiver", receiver);
            bundle.putString("receiverId", sented);
            bundle.putBoolean("fromNotification", true);
            intent.putExtras(bundle);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, j,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_ONE_SHOT);
        Uri defaulSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaulSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if(j>0){
            i = j;
        }

        noti.notify(i, builder.build());
    }
}
