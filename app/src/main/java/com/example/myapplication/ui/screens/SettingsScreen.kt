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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.UserPreferences
import kotlinx.coroutines.launch
import com.example.myapplication.data.AppRefreshBus
private fun normalizeApiInput(input: String): String? {
    val cleaned = input
        .trim()
        .removePrefix("http://")
        .removePrefix("https://")
        .trimEnd('/')

    if (cleaned.isBlank()) return null

    val ipPortRegex = Regex(
        pattern = """^((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d):([1-9]\d{0,4})$"""
    )

    if (!ipPortRegex.matches(cleaned)) return null

    val port = cleaned.substringAfterLast(":").toIntOrNull() ?: return null
    if (port !in 1..65535) return null

    return "http://$cleaned/"
}

private fun extractHostPort(url: String): String {
    return url
        .trim()
        .removePrefix("http://")
        .removePrefix("https://")
        .trimEnd('/')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val currentBaseUrl by userPreferences.apiBaseUrl.collectAsState(initial = BuildConfig.API_BASE_URL)
    var token by remember { mutableStateOf("Loading token...") }
    var baseUrlInput by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var lastRenderedBaseUrl by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            token = if (task.isSuccessful) task.result else "Error: ${task.exception?.message}"
        }
    }

    LaunchedEffect(currentBaseUrl) {
        val currentHostPort = extractHostPort(currentBaseUrl)
        val lastHostPort = extractHostPort(lastRenderedBaseUrl)
        if (baseUrlInput.isBlank() || baseUrlInput == lastHostPort) {
            baseUrlInput = currentHostPort
        }
        lastRenderedBaseUrl = currentBaseUrl
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
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

            OutlinedTextField(
                value = token,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text("Copy Token")
            }

            Spacer(modifier = Modifier.padding(top = 4.dp))

            Text(
                "API Server",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = baseUrlInput,
                onValueChange = { baseUrlInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("IP address and port") },
                placeholder = { Text("Vi du: 192.168.1.10:8000") },
//                supportingText = {
//                    Text("Không cần nhập http:// hoặc / ở cuối.")
//                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri
                )
            )

            Text(
                text = "API hiện tại: $currentBaseUrl",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val normalized = normalizeApiInput(baseUrlInput)

                        if (normalized == null) {
                            Toast.makeText(
                                context,
                                "Ví dụ 192.168.1.10:8000",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        scope.launch {
                            userPreferences.saveApiBaseUrl(normalized)
                            baseUrlInput = extractHostPort(normalized)
                            AppRefreshBus.refreshAllApis()
                            Toast.makeText(
                                context,
                                "API URL updated: $normalized",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackClicked()
                        }
                    }
                ) {
                    Text("Apply")
                }

                Button(
                    onClick = {
                        baseUrlInput = extractHostPort(BuildConfig.API_BASE_URL)

                        scope.launch {
                            userPreferences.saveApiBaseUrl(BuildConfig.API_BASE_URL)
                            AppRefreshBus.refreshAllApis()
                            Toast.makeText(
                                context,
                                "API URL reset to build config",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackClicked()
                        }
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
