package com.example.myapplication.ui.screens



import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.example.myapplication.enums.SourceType
import com.example.myapplication.models.FacebookPost
import com.example.myapplication.R
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.helper.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookFeedList(
    posts: LazyPagingItems<FacebookPost>,
    refreshSignal: Int = 0,
    availableKeywords: List<String> = emptyList(),
    selectedKeyword: String? = null,
    onKeywordSelected: (String?) -> Unit = {},
    savedPostIds: Set<Long> = emptySet(),
    onToggleSavedPost: ((FacebookPost) -> Unit)? = null
) {

    val isRefreshing = posts.loadState.refresh is LoadState.Loading
    val listState = rememberLazyListState()

    LaunchedEffect(refreshSignal) {
        if (refreshSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (availableKeywords.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedKeyword == null,
                        onClick = { onKeywordSelected(null) },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                items(availableKeywords) { keyword ->
                    FilterChip(
                        selected = selectedKeyword == keyword,
                        onClick = { onKeywordSelected(keyword) },
                        label = { Text(keyword) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { posts.refresh() },
            modifier = Modifier.weight(1f)
        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (posts.loadState.refresh is LoadState.Error) {
                    val errorState = posts.loadState.refresh as LoadState.Error
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Lỗi kết nối: ${errorState.error.localizedMessage}")
                            Button(onClick = { posts.retry() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                } else {
                    items(
                        count = posts.itemCount,
                        key = { index -> posts[index]?.id ?: index }
                    ) { index ->
                        posts[index]?.let { post ->
                            FacebookPostCard(
                                post = post,
                                isSaved = savedPostIds.contains(post.id),
                                onToggleSaved = onToggleSavedPost?.let { handler -> { handler(post) } }
                            )
                        }
                    }

                    if (posts.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacebookPostCard(
    post: FacebookPost,
    isSaved: Boolean = false,
    onToggleSaved: (() -> Unit)? = null
) {
    var isExpanded by rememberSaveable(post.id) { mutableStateOf(false) }
    var isExpandable by rememberSaveable(post.id) { mutableStateOf(false) }
    var imageViewerStartIndex by rememberSaveable(post.id) { mutableStateOf<Int?>(null) }

    // 🔥 Safely handle potential nulls from the backend
    val safeImages = post.images ?: emptyList()
    val safeContent = post.content ?: ""
    val safeDate = post.crawledAt ?: ""
    val safeUrl = post.postUrl
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isClickable = !safeUrl.isNullOrEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val iconResId = when (post.sourceType) {
                    SourceType.THREADS -> R.drawable.ic_threads
                    else -> R.drawable.ic_facebook
                }

                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Social Icon",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.sourceName ?: "Unknown Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = TimeFormatter.formatRelativeTime(safeDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (onToggleSaved != null) {
                IconButton(
                    onClick = {
                        onToggleSaved()
                        val message = if (isSaved) "Đã bỏ lưu" else "Đã lưu bài đăng"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Bỏ lưu bài đăng" else "Lưu bài đăng",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isClickable) {
                IconButton(
                    onClick = { uriHandler.openUri(safeUrl.orEmpty()) }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Mở bài gốc",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = safeContent,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layoutResult ->
                if (!isExpanded) {
                    isExpandable = layoutResult.hasVisualOverflow
                }
            }
        )

        if (isExpandable) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Thu gọn" else "Xem thêm",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Start
                )
            }
        }

        // 🔥 Use safeImages instead of post.images
        if (safeImages.isNotEmpty()) {
            Box {
                AsyncImage(
                    model = safeImages.first(),
                    contentDescription = "Post Image",
                    modifier =  Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { imageViewerStartIndex = 0 },
                    contentScale = ContentScale.Crop
                )

                if (safeImages.size > 1) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "1/${safeImages.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }

    imageViewerStartIndex?.let { startIndex ->
        FullScreenImageViewer(
            images = safeImages,
            initialIndex = startIndex,
            onDismiss = { imageViewerStartIndex = null }
        )
    }
}

@Composable
private fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var dragOffsetY by rememberSaveable { mutableStateOf(0f) }
        val dismissThresholdPx = 220f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            dragOffsetY += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            if (abs(dragOffsetY) > dismissThresholdPx) {
                                onDismiss()
                            } else {
                                dragOffsetY = 0f
                            }
                        },
                        onDragCancel = {
                            dragOffsetY = 0f
                        }
                    )
                }
        ) {
            val pagerState = rememberPagerState(
                initialPage = initialIndex.coerceIn(0, images.lastIndex),
                pageCount = { images.size }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = "Post image ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "${pagerState.currentPage + 1}/${images.size}",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }
}

