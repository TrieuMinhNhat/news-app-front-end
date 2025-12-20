package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // 1. Create a State to hold the current intent
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Initialize the state with the starting intent (Cold Start)
        if (intent?.hasExtra("article_id") == true) {
            currentIntent.value = intent
        }

        setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    AppNavGraph(navController = navController)

                    // Whenever currentIntent.value changes, this will re-run.
                    HandleNotificationIntent(
                        intentState = currentIntent,
                        navController = navController
                    )
                }
            }
        }

        askNotificationPermission()
    }

    // 4. Update the state when a new intent arrives (Warm Start / Background)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra("article_id")) {
            currentIntent.value = intent
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
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

@Composable
fun HandleNotificationIntent(
    intentState: androidx.compose.runtime.MutableState<Intent?>, // Pass State
    navController: NavHostController
) {
    val intent = intentState.value

    LaunchedEffect(intent) {
        if (intent != null) {
            val idString = intent.getStringExtra("article_id")
            val id = idString?.toIntOrNull()

            Log.d("DEEP_LINK", "Found ID: $idString")

            if (id != null) {
                try {
                    navController.navigate(Screen.Detail.createRoute(id)) {
                        // Clears back stack up to home so back button works nicely
                        // logic depends on your needs, but usually popUpTo Home is good
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }

                    // CRITICAL: Consume the intent so it doesn't trigger again
                    intentState.value = null

                } catch (e: Exception) {
                    Log.e("DEEP_LINK", "Nav Error: ${e.message}")
                }
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

// cd C:\Users\ADMIN\AppData\Local\Android\Sdk\platform-tools
//./adb connect 192.168.1.13:43405