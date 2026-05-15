package com.example.myapplication.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.UserPreferences
import kotlinx.coroutines.launch

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
    val userPreferences = remember { UserPreferences(context) }
    val currentBaseUrl by userPreferences.apiBaseUrl.collectAsState(initial = BuildConfig.API_BASE_URL)
    var baseUrlInput by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    // ADD THIS BLOCK:
    LaunchedEffect(Unit) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result
            } else {
                token = "Error: ${task.exception?.message}"
            }
        }
    }
    LaunchedEffect(currentBaseUrl) {
        if (baseUrlInput.isBlank()) {
            baseUrlInput = currentBaseUrl
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

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                "API Base URL",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = baseUrlInput,
                onValueChange = { baseUrlInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                "Current: $currentBaseUrl",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val rawValue = baseUrlInput.trim()
                        if (rawValue.isBlank()) {
                            Toast.makeText(context, "Base URL cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!rawValue.startsWith("http://") && !rawValue.startsWith("https://")) {
                            Toast.makeText(context, "Base URL must start with http:// or https://", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val normalized = if (rawValue.endsWith("/")) rawValue else "$rawValue/"
                        scope.launch {
                            userPreferences.saveApiBaseUrl(normalized)
                            Toast.makeText(context, "API Base URL updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Apply")
                }
                Button(
                    onClick = {
                        baseUrlInput = BuildConfig.API_BASE_URL
                        scope.launch {
                            userPreferences.saveApiBaseUrl(BuildConfig.API_BASE_URL)
                            Toast.makeText(context, "API Base URL reset", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}