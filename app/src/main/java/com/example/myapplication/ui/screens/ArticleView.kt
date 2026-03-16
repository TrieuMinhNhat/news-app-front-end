package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.models.Article
import coil.compose.AsyncImage
import com.example.myapplication.viewmodel.NewsViewModel

/**
 * A screen that displays the full details of a news article.
 *
 * @param article The article to display.
 * @param onBackClicked Callback invoked when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int?,
    onBackClicked: () -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    //demo
    //val article =Article("1", "Global Tech Summit 2025", "The annual Global Tech Summit kicks off, bringing leaders from around the world to discuss the future of AI.", "https://placehold.co/600x400/EEE/333?text=Article+Image", "TechCrunch");
    val article by viewModel.articleDetail.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(articleId) {
        viewModel.fetchArticleDetail(articleId)
    }

    when {
        isLoading -> {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        }
        errorMessage != null -> {
            Text("Error: $errorMessage")
        }
        article != null -> {
            Scaffold(
                topBar = {

                    TopAppBar(
                        title = { Text(
                            " "
                        ) },
                        navigationIcon = {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                                IconButton(onClick = { /* TODO: share */ }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                                IconButton(onClick = { /* TODO: toggle bookmark */ }) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                                }

                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Article Image Placeholder
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            // In a real app, you'd use an image loading library like Coil:
                            val imageLink = article!!.imageUrl.firstOrNull() ?: "https://placehold.co/600x400?text=No+Image"
                             AsyncImage(
                                 model = imageLink,
                                 contentDescription = "Article image",
                                 modifier = Modifier.fillMaxSize(),
                                 contentScale = ContentScale.Crop
                             )

                        }
                    }

                    // 2. Title + Byline
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = article!!.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "By ${article!!.author} • ${article!!.source ?: "Unknown"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Article Body
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = article!!.description, // replace with full content when you add it
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 26.sp,           // excellent readability
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier.padding(bottom = 40.dp)
                            )
                        }
                    }
                }
            }
        }
    }

}

/**
 * Preview composable for the ArticleDetailScreen.
 */
@Preview(showBackground = true)
@Composable
fun ArticleDetailScreenPreview() {
    // A dummy article for the preview
//    val dummyArticle = Article(
//        id = "1",
//        title = "Global Tech Summit 2025",
//        description = "The annual Global Tech Summit kicks off, bringing leaders from around the world to discuss the future of AI.",
//        imageUrl = "https://placehold.co/600x400/EEE/333?text=Article+Image",
//        source = "TechCrunch"
//    )

    // You would wrap this in your app's theme, e.g., NewsAppTheme { ... }
//    MaterialTheme {
//        ArticleDetailScreen(
//            articleId = "abc",
//            onBackClicked = {}
//        )
//    }
}