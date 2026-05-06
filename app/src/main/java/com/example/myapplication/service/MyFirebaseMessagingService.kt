package com.example.myapplication.service





import android.content.Intent

import android.util.Log

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

        // NOTE:

        // - If the backend includes a `notification` payload, Android may auto-display it when the app is backgrounded.

        // - To ensure *your* code runs 100% of the time (and to avoid OS-managed duplicates), prefer DATA-ONLY messages.

        //   That means: send only `data: {...}` and omit the top-level `notification: {...}` object.

        val data = message.data



        if (message.notification != null) {

            Log.w(

                TAG,

                "FCM message contains a notification payload. Consider switching backend to data-only payloads to avoid OS auto-notifications in background."

            )

        }



        // Data-first extraction (expected for data-only payloads)

        val title = data["title"]?.trim().takeUnless { it.isNullOrEmpty() }

            ?: message.notification?.title?.trim().takeUnless { it.isNullOrEmpty() }

            ?: "News App"

        val body = data["body"]?.trim().takeUnless { it.isNullOrEmpty() }

            ?: message.notification?.body?.trim().takeUnless { it.isNullOrEmpty() }

            ?: "Tin mới"



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



    private companion object {

        private const val TAG = "MyFirebaseMsgService"

    }

}