package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";
    private static final String CHANNEL_ID = "NEWS_CHANNEL_HEADS_UP";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // TODO: Gửi token đến backend Django
        sendTokenToBackend(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "News Update";
        String body = "Check out the latest news";
        String articleId = null;

        // EXTRACT EVERYTHING FROM DATA
        if (message.getData().size() > 0) {
            articleId = message.getData().get("article_id");

            // If you switch to Data-only payload, title/body come from here too
            if (message.getData().containsKey("title")) {
                title = message.getData().get("title");
            }
            if (message.getData().containsKey("body")) {
                body = message.getData().get("body");
            }
        }

        // If you kept notification payload for foreground, fallback (optional)
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        showNotification(title, body, articleId);
    }

    private void showNotification(String title, String message, String articleId) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "NEWS_CHANNEL";



        // Notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "News Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("High priority news alerts");
            channel.enableVibration(true); // <--- CRITICAL: Heads-up often needs vibration
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Show on lock screen
            manager.createNotificationChannel(channel);
        }

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // PendingIntent mở App + truyền article_id
        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = createDeepLink(articleId, requestCode);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(sound)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private PendingIntent createDeepLink(String articleId, int requestCode) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (articleId != null) {
            intent.putExtra("article_id", articleId);
        }

        return PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void sendTokenToBackend(String token) {
        Log.d(TAG, "TODO: send token to backend = " + token);
    }
}
