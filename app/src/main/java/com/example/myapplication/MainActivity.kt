package com.example.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.ui.theme.NewsAppTheme

class MainActivity : ComponentActivity() {

    // 🔥 SINGLE EVENT STATE
    private val mainEvent = mutableStateOf<MainEvent?>(null)

    private val updateNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mainEvent.value = MainEvent.RefreshNotification
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Cold start (app mở từ notification)
        intent?.getStringExtra("article_id")?.let {
            mainEvent.value = MainEvent.NotificationArrived(it)
        }

        setContent {
            NewsAppTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    mainEvent = mainEvent
                )
            }
        }

        askNotificationPermission()
    }

    // ✅ App quay lại foreground
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            this,
            updateNotificationReceiver,
            IntentFilter("com.example.myapplication.UPDATE_NOTIFICATIONS"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(updateNotificationReceiver)
        } catch (_: Exception) {}
    }

    // ✅ Warm start / background → foreground
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("article_id")?.let {
            mainEvent.value = MainEvent.NotificationArrived(it)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}
//cd C:\Users\ADMIN\AppData\Local\Android\Sdk\platform-tools
// ./adb connect 192.168.1.4:39869