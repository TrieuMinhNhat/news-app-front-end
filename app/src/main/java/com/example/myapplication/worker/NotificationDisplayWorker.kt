package com.example.myapplication.worker

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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class NotificationDisplayWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE)?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "News update"
        val body = inputData.getString(KEY_BODY)?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "You have a new update"
        val articleId = inputData.getString(KEY_ARTICLE_ID)
        val notificationId = inputData.getString(KEY_NOTIFICATION_ID)
        val notificationType = inputData.getString(KEY_NOTIFICATION_TYPE)
        val keyword = inputData.getString(KEY_KEYWORD)
        val imageUrl = inputData.getString(KEY_IMAGE_URL)

        val bitmap = if (!imageUrl.isNullOrBlank()) {
            getBitmapFromUrl(imageUrl)
        } else {
            null
        }

        showNotification(title, body, articleId, notificationId, notificationType, keyword, bitmap)
        return Result.success()
    }

    private fun showNotification(
        title: String,
        body: String,
        articleId: String?,
        notificationId: String?,
        notificationType: String?,
        keyword: String?,
        bitmap: Bitmap?
    ) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "NEWS_CHANNEL_HEADS_UP"

        // Create the NotificationChannel (Required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Breaking News", // User-visible channel name
                NotificationManager.IMPORTANCE_HIGH // This ensures it pops up as a heads-up notification
            ).apply {
                description = "Important news alerts and updates"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Prepare the intent to open the app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (articleId != null) putExtra("article_id", articleId)
            if (notificationId != null) putExtra("notification_id", notificationId)
            if (notificationType != null) putExtra("notification_type", notificationType)
            if (keyword != null) putExtra("keyword", keyword)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            articleId?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the professional notification
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_monochrome) // MUST be a transparent/monochrome icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For backward compatibility < API 26
            .setContentIntent(pendingIntent)
            .setColor(applicationContext.getColor(R.color.white)) // Optional: Add your app's brand color here

        // Note: We removed builder.setSubText(appName) to fix the duplicate "Hot News" issue.

        // Handle the image gracefully
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setSummaryText(body) // Text shown when expanded
                    .bigLargeIcon(null as Bitmap?) // Hides the large icon thumbnail when expanded
            )
        } else {
            // Clean text fallback when there is no image
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
        }

        val notificationId = articleId?.hashCode() ?: (System.currentTimeMillis() / 1000L).toInt()
        manager.notify(notificationId, builder.build())
    }

    private suspend fun getBitmapFromUrl(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false) // Required if passing the bitmap directly to a notification
                .build()
            val result = loader.execute(request)
            (result.drawable as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            null // Failsafe in case the image download fails
        }
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ARTICLE_ID = "article_id"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_NOTIFICATION_TYPE = "notification_type"
        const val KEY_KEYWORD = "keyword"
        const val KEY_IMAGE_URL = "image_url"
    }
}