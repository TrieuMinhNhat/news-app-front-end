package com.example.myapplication.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.MyFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
/**
 * A screen that fetches and displays the current FCM token for debugging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBackClicked: () -> Unit
) {
    var token by remember { mutableStateOf("Loading token...") }
    val context = LocalContext.current

    // Fetch the token when the screen is first composed
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                token = "Failed to get token: ${task.exception?.message}"
                return@addOnCompleteListener
            }

            val fetchedToken = task.result ?: return@addOnCompleteListener
            token = fetchedToken

            // Gửi lên server
            val json = JSONObject().apply {
                put("token", fetchedToken)
                put("device_name", Build.MODEL)
            }

//            val request = JsonObjectRequest(
//                Request.Method.POST,
//                "http://192.168.1.6:8000/api/register_token/",
//                json,
//                { response -> Log.d("API", "Registered successfully: $response") },
//                { error -> Log.e("API", "Error: ${error.message}") }
//            )
//
//            Volley.newRequestQueue(context).add(request)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Info") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "FCM Registration Token",
                style = MaterialTheme.typography.titleMedium
            )

            // Display token in a read-only text field to allow selection
            OutlinedTextField(
                value = token,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Button to copy the token to the clipboard
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("FCM Token", token)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Create, contentDescription = "Copy")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Token")
            }
        }
    }
}