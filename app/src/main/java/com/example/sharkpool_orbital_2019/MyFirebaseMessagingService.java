package com.example.sharkpool_orbital_2019;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.shadow.com.google.gson.JsonElement;
import com.sendbird.android.shadow.com.google.gson.JsonObject;
import com.sendbird.android.shadow.com.google.gson.JsonParser;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /*
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("newToken", s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }
    */
    public static int notif_id = 0;
    public String senderID = FirebaseAuth.getInstance().getUid();

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("SendBirdPush", token);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", token).apply();

        // Register a registration token to SendBird server.
        SendBird.registerPushTokenForCurrentUser(token, new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus ptrs, SendBirdException e) {
                if (e != null) {
                    Log.d("SendBirdPush","Failed to register.");
                    return;
                }

                if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                    Log.d("SendBirdPush", "A token registration is pending");
                    // A token registration is pending.
                    // Retry the registration after a connection has been successfully established.
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getData().get("message");
        JsonElement payload = new JsonParser().parse(remoteMessage.getData().get("sendbird"));

        Log.d("SendBirdPush", "Message received.");
        String sender = payload.getAsJsonObject().get("sender").getAsJsonObject().get("id").toString().substring(1,29);

        if (!sender.equals(senderID)){
            sendNotification(message, payload);
            NotificationObject notificationObject = new NotificationObject();
            notificationObject.initialize(payload);
        }

    }

    public void sendNotification(String message, JsonElement payload){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "Sharkpool")
                        .setSmallIcon(R.drawable.sharkpool_trans) //TODO: Uploading Small Logo
                        .setContentTitle("New message")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_MAX);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Sharkpool",
                    "Sharkpool",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notif_id, notificationBuilder.build());

        if (notif_id == 29) notif_id = 0; // displays 50 latest notifications
        else notif_id++;
    }

}