package com.example.newsapp.ui.topics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // This import is fine to keep
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Topic

/**
 * A screen that allows the user to manage their topic subscriptions.
 *
 * @param onBackClicked Callback invoked when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    onBackClicked: () -> Unit
) {
    // Dummy list of topics for UI development.
    // In a real app, this woul d come from a ViewModel.
    val allTopics = remember {
        listOf(
            Topic("tech", "Technology"),
            Topic("science", "Science"),
            Topic("sports", "Sports"),
            Topic("business", "Business"),
            Topic("health", "Health"),
            Topic("entertainment", "Entertainment"),
            Topic("world", "World News")
        )
    }

    // This state map would be managed by a ViewModel in a real app,
    // likely synced with Firebase.
    // We use rememberSaveable to hold the subscription state locally for now.
    val subscriptionStates = remember { // <-- Changed from rememberSaveable
        // Let's default to being subscribed to Tech and Sports for the preview
        mutableStateMapOf(
            "tech" to true,
            "science" to false,
            "sports" to true,
            "business" to false,
            "health" to false,
            "entertainment" to false,
            "world" to false
        )
    }

    // State for keyword subscriptions
    val keywordStates = remember {
        // Default with some keywords for preview
        mutableStateListOf("AI", "Jetpack Compose", "Android")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Topics") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        TopicsList(
            topics = allTopics,
            subscriptionStates = subscriptionStates,
            onSubscriptionChanged = { topicId, isSubscribed ->
                subscriptionStates[topicId] = isSubscribed
                // In a real app, you'd call:
                // viewModel.updateSubscription(topicId, isSubscribed)
                // This would then update Firebase Messaging topics.
            },
            keywords = keywordStates,
            onAddKeyword = { keyword ->
                if (keyword.isNotBlank() && !keywordStates.contains(keyword)) {
                    keywordStates.add(keyword)
                }
            },
            onRemoveKeyword = { keyword ->
                keywordStates.remove(keyword)
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Displays the list of topics and their subscription switches.
 */
@OptIn(ExperimentalLayoutApi::class) // Added for FlowRow
@Composable
fun TopicsList(
    topics: List<Topic>,
    subscriptionStates: Map<String, Boolean>,
    onSubscriptionChanged: (String, Boolean) -> Unit,
    keywords: List<String>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Subscribe to topics to get personalized news and notifications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- Add the new Keyword Subscription item ---
        item {
            KeyWordSubscription(
                keywords = keywords,
                onAddKeyword = onAddKeyword,
                onRemoveKeyword = onRemoveKeyword,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider()
        }
        // --- End of new item ---

        items(topics) { topic ->
            TopicItem(
                topicName = topic.name,
                isSubscribed = subscriptionStates[topic.id] ?: false,
                onSubscribedChanged = { isSubscribed ->
                    onSubscriptionChanged(topic.id, isSubscribed)
                }
            )
            Divider()
        }
    }
}

/**
 * A new composable for managing keyword subscriptions.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun KeyWordSubscription(
    keywords: List<String>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Keyword Subscriptions",
            style = MaterialTheme.typography.titleLarge
        )

        // Input field for adding new keywords
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Add a keyword") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        onAddKeyword(text)
                        text = "" // Clear the field
                        keyboardController?.hide() // Hide keyboard
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add keyword"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    onAddKeyword(text)
                    text = "" // Clear the field
                    keyboardController?.hide() // Hide keyboard
                }
            )
        )

        // Display the list of subscribed keywords as chips
        if (keywords.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keywords.forEach { keyword ->
                    InputChip(
                        selected = false,
                        onClick = { /* Not interactive, only remove is */ },
                        label = { Text(keyword) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemoveKeyword(keyword) },
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $keyword"
                                )
                            }
                        }
                    )
                }
            }
        } else {
            Text(
                text = "You haven't added any keywords yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/**
 * A single row in the topics list.
 */
@Composable
fun TopicItem(
    topicName: String,
    isSubscribed: Boolean,
    onSubscribedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = topicName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isSubscribed,
            onCheckedChange = onSubscribedChanged,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * Preview composable for the TopicsScreen.
 */
@Preview(showBackground = true)
@Composable
fun TopicsScreenPreview() {
    // You would wrap this in your app's theme, e.g., NewsAppTheme { ... }
    MaterialTheme {
        TopicsScreen(
            onBackClicked = {}
        )
    }
}