package com.example.myapplication.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.worker.TokenWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val imageUrl = data["image"] // Server cần gửi kèm field này nếu muốn hiện ảnh

        // Cần chạy coroutine để tải ảnh (nếu có)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val bitmap = if (!imageUrl.isNullOrEmpty()) getBitmapFromUrl(imageUrl) else null
            showNotification(title, body, articleId, bitmap)
        }
        val intent = Intent("com.example.myapplication.UPDATE_NOTIFICATIONS")
        intent.setPackage(packageName) // Chỉ gửi cho app của mình
        sendBroadcast(intent)
    }

    private fun showNotification(title: String, body: String, articleId: String?, bitmap: Bitmap?) {
        val channelId = "NEWS_CHANNEL_HEADS_UP"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "News Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Breaking news notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // --- XỬ LÝ NAVIGATION (QUAN TRỌNG) ---
        val intent = Intent(this, MainActivity::class.java).apply {
            //flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (articleId != null) putExtra("article_id", articleId)
        }

        // TaskStackBuilder tạo "lịch sử giả" để nút Back hoạt động đúng
//        val pendingIntent: PendingIntent = TaskStackBuilder.create(this).run {
//            addNextIntentWithParentStack(intent)
//            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            articleId?.hashCode() ?: 0, // Request Code khác nhau để không bị ghi đè nếu có nhiều noti
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Thay bằng icon app của bạn
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        // Nếu có ảnh, hiển thị dạng BigPicture
        if (bitmap != null) {
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            builder.setLargeIcon(bitmap)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Hàm tải ảnh dùng thư viện Coil (đã có sẵn trong project của bạn)
    private suspend fun getBitmapFromUrl(url: String): Bitmap? {
        val loader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false) // Bắt buộc false cho Notification
            .build()
        val result = loader.execute(request)
        return (result.drawable as? BitmapDrawable)?.bitmap
    }
}