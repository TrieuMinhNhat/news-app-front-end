package com.example.myapplication.ui.screens



import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.example.myapplication.enums.SourceType
import com.example.myapplication.models.FacebookPost
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.myapplication.R
import androidx.compose.ui.platform.LocalUriHandler
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookFeedList(
    posts: LazyPagingItems<FacebookPost>,
    refreshSignal: Int = 0,
    availableKeywords: List<String> = emptyList(),
    selectedKeyword: String? = null,
    onKeywordSelected: (String?) -> Unit = {}
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
                        label = { Text("Tất cả") }
                    )
                }

                items(availableKeywords) { keyword ->
                    FilterChip(
                        selected = selectedKeyword == keyword,
                        onClick = { onKeywordSelected(keyword) },
                        label = { Text(keyword) }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(
                    count = posts.itemCount,
                    key = { index -> posts[index]?.id ?: index }
                ) { index ->
                    posts[index]?.let { FacebookPostCard(it) }
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

@Composable
fun FacebookPostCard(post: FacebookPost) {
    var isExpanded by rememberSaveable(post.id) { mutableStateOf(false) }
    var isExpandable by rememberSaveable(post.id) { mutableStateOf(false) }
    var imageViewerStartIndex by rememberSaveable(post.id) { mutableStateOf<Int?>(null) }

    // 🔥 Safely handle potential nulls from the backend
    val safeImages = post.images ?: emptyList()
    val safeContent = post.content ?: ""
    val safeDate = post.crawledAt ?: ""
    val safeUrl = post.postUrl
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth().then(if(!safeUrl.isNullOrEmpty()) Modifier.clickable { uriHandler.openUri(safeUrl) }
        else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//                        Text(
//                            text = post.sourceName?.take(1)?.uppercase() ?: "?",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
                        val iconResId = when (post.sourceType) {
                            SourceType.THREADS -> R.drawable.ic_threads
                            else -> R.drawable.ic_facebook
                        }

                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = "Social Icon",
                            modifier = Modifier.size(46.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.sourceName ?: "Unknown Source",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatCrawledDate(safeDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
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
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { imageViewerStartIndex = 0 },
                        contentScale = ContentScale.Crop
                    )

                    if (safeImages.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
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
        }
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

private fun formatCrawledDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(raw) ?: return raw

        val output = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        output.format(date)
    } catch (_: Exception) {
        raw
    }
}
