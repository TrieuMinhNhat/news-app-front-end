package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.myapplication.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.SavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int?,
    onBackClicked: () -> Unit,
    viewModel: NewsViewModel = hiltViewModel(),
    savedViewModel: SavedViewModel = hiltViewModel()
) {
    val article by viewModel.articleDetail.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val savedArticleIds by savedViewModel.savedArticleIds.collectAsState()
    val context = LocalContext.current

    var ttsState by remember { mutableStateOf(NewsTtsUiState()) }
    val ttsManager = remember {
        NewsTtsManager(context) { newState -> ttsState = newState }
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.release() }
    }

    LaunchedEffect(articleId) {
        ttsManager.stop()
        viewModel.fetchArticleDetail(articleId)
        savedViewModel.refresh()
    }

    LaunchedEffect(ttsState.message) {
        val message = ttsState.message
        if (
            !message.isNullOrBlank() &&
            (ttsState.status == NewsTtsStatus.Error || ttsState.status == NewsTtsStatus.Unsupported)
        ) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không thể tải bài viết. Vui lòng thử lại.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        article != null -> {
            val currentArticle = article!!
            val articleTextForTts = buildString {
                append(currentArticle.title.trim())
                append(" - ")
                currentArticle.summary
                    ?.takeIf { it.isNotBlank() }
                    ?.let { append(it.trim()).append(" - ") }
                append(currentArticle.description.trim())
            }.trim()

            val isSaved = savedArticleIds.contains(currentArticle.id)

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Bài viết",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    val url = currentArticle.link?.trim().orEmpty()
                                    if (url.isNotEmpty()) {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "${currentArticle.title}\nĐọc thêm tại: $url")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ bài viết"))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                            }

                            IconButton(
                                onClick = {
                                    val url = currentArticle.link?.trim().orEmpty()
                                    if (url.isNotEmpty()) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Mở bài gốc")
                            }

                            IconButton(
                                onClick = {
                                    savedViewModel.toggleArticle(currentArticle.id)
                                    Toast.makeText(
                                        context,
                                        if (isSaved) "Đã bỏ lưu" else "Đã lưu bài viết",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isSaved) "Bỏ lưu" else "Lưu"
                                )
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
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageLink = currentArticle.imageUrl.firstOrNull().orEmpty()
                            AsyncImage(
                                model = imageLink.ifEmpty { R.drawable.ic_image_placeholder },
                                contentDescription = "Article image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_broken_image)
                            )

                            currentArticle.source
                                ?.takeIf { it.isNotBlank() }
                                ?.let { source ->
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = source,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currentArticle.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val author = currentArticle.author?.takeIf { it.isNotBlank() } ?: "Hot News"
                                Text(
                                    text = author,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                currentArticle.source
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { source ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• $source",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                            }

                            NewsTtsCard(
                                state = ttsState,
                                onPlayPause = { ttsManager.playOrPause(articleTextForTts) },
                                onStop = ttsManager::stop
                            )
                        }
                    }

                    item {
                        Text(
                            text = currentArticle.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Start
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsTtsCard(
    state: NewsTtsUiState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nghe bài viết",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (state.status) {
                            NewsTtsStatus.Initializing -> "Đang chuẩn bị giọng đọc..."
                            NewsTtsStatus.Ready -> state.message ?: "Sẵn sàng đọc"
                            NewsTtsStatus.Speaking -> "Đang đọc ${state.currentChunk}/${state.totalChunks}"
                            NewsTtsStatus.Paused -> "Đã tạm dừng"
                            NewsTtsStatus.Error -> state.message ?: "Không thể đọc bài viết"
                            NewsTtsStatus.Unsupported -> state.message ?: "Không hỗ trợ tiếng Việt"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = onPlayPause,
                    enabled = state.status != NewsTtsStatus.Initializing && state.status != NewsTtsStatus.Unsupported,
                    label = {
                        Text(
                            text = when {
                                state.isSpeaking -> "Tạm dừng"
                                state.isPaused -> "Tiếp tục"
                                else -> "Đọc"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                state.isSpeaking -> Icons.Default.Pause
                                state.isPaused -> Icons.Default.PlayArrow
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            if (state.status == NewsTtsStatus.Speaking || state.status == NewsTtsStatus.Paused) {
                val progress = if (state.totalChunks > 0) {
                    state.currentChunk.toFloat() / state.totalChunks.toFloat()
                } else {
                    0f
                }

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dừng")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleDetailScreenPreview() {
    // Preview intentionally left empty because this screen depends on Hilt ViewModels.
}