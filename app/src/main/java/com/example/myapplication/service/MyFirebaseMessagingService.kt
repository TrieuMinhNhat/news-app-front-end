package com.example.myapplication.service


import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.worker.NotificationDisplayWorker
import com.example.myapplication.worker.TokenWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Dùng WorkManager để gửi token đảm bảo thành công 100%
        val workRequest = OneTimeWorkRequestBuilder<TokenWorker>()
            .setInputData(workDataOf("token" to token))
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Lấy dữ liệu
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "News App"
        val body = data["body"] ?: message.notification?.body ?: "Tin mới"
        val articleId = data["article_id"]
        val notificationId = data["notification_id"]
        val notificationType = data["type"]
        val keyword = data["keyword"]
        val imageUrl = data["image_url"] ?: data["image"] // Support both keys

        val notificationWork = OneTimeWorkRequestBuilder<NotificationDisplayWorker>()
            .setInputData(
                workDataOf(
                    NotificationDisplayWorker.KEY_TITLE to title,
                    NotificationDisplayWorker.KEY_BODY to body,
                    NotificationDisplayWorker.KEY_ARTICLE_ID to articleId,
                    NotificationDisplayWorker.KEY_NOTIFICATION_ID to notificationId,
                    NotificationDisplayWorker.KEY_NOTIFICATION_TYPE to notificationType,
                    NotificationDisplayWorker.KEY_KEYWORD to keyword,
                    NotificationDisplayWorker.KEY_IMAGE_URL to imageUrl
                )
            )
            .build()
        WorkManager.getInstance(this).enqueue(notificationWork)

        val intent = Intent("com.example.myapplication.UPDATE_NOTIFICATIONS")
        intent.setPackage(packageName) // Chỉ gửi cho app của mình
        sendBroadcast(intent)
    }
}